package org.vaadin.firitin.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.dom.DomListenerRegistration;
import com.vaadin.flow.dom.Element;
import elemental.json.JsonObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.logging.Logger;

/**
 * A helper to detect and observe changes for the size of the given component.
 * Allows you for example to easily configure Grid columns for different
 * devices or swap component implementations based on the screen size/orientation.
 *
 */
public class ResizeObserver {

    public interface SizeChangeListener {
        void onChange(Dimensions observation);
    }

    public record Dimensions(
            int x,
            int y,
            int width,
            int height,
            int top,
            int right,
            int bottom,
            int left
    ) {}

    private record ComponentMapping(int id, Component component, ArrayList<SizeChangeListener> listeners) {
        private ComponentMapping(int id, Component component) {
            this(id, component, new ArrayList<>());
        }
    }

    private Map<Component,Integer> componentToId = new HashMap<>();
    private Map<Integer,ComponentMapping> idToComponentMapping = new HashMap<>();
    private int nextId = 0;

    private static ObjectMapper om = new ObjectMapper();

    private final Element uiElement;
    private final DomListenerRegistration reg;

    public static ResizeObserver of(UI ui) {
        ResizeObserver resizeObserver = ComponentUtil.getData(ui, ResizeObserver.class);
        if(resizeObserver == null) {
            resizeObserver = new ResizeObserver(ui);
            ComponentUtil.setData(ui, ResizeObserver.class, resizeObserver);
        }
        return resizeObserver;
    }

    public static ResizeObserver get() {
        return ResizeObserver.of(UI.getCurrent());
    }

    private ResizeObserver(UI ui) {
        this.uiElement = ui.getElement();
        uiElement.executeJs("""
                var el = this;
                el._resizeObserver = new ResizeObserver((entries) => {
                  const sizes = {};
                  for (const entry of entries) {
                    if (entry.target.isConnected && entry.contentBoxSize) {
                      const id = entry.target._resizeObserverId;
                      const contentBoxSize = entry.contentBoxSize[0];
                      sizes[id] = JSON.stringify(entry.contentRect);
                    } else {
                      console.log("Ignoring resize event for detached element " + entry.target._resizeObserverId +  ", TODO: cleanup??");
                    }
                  }
                  const event = new Event("element-resize");
                  event.dimensions = sizes;
                  el.dispatchEvent(event);
                });
                el._resizeObserverElements = {};
                """);
        reg = uiElement.addEventListener("element-resize", event -> {
                    JsonObject object = event.getEventData().getObject("event.dimensions");
                    for(String idx : object.keys()) {
                        String json = object.getString(idx);
                        try {
                            Dimensions dimensions = om.readValue(json, Dimensions.class);
                            ComponentMapping componentMapping = idToComponentMapping.get(Integer.valueOf(idx));
                            if(componentMapping != null) {
                                new ArrayList<>(componentMapping.listeners()).forEach(l -> l.onChange(dimensions));
                            } else {
                                // Timing issue in Flow navigation can make this happen, simply ignore
                                Logger.getLogger(ResizeObserver.class.getName()).fine("Resize listener called for component that is already de-registered, id:" + idx);
                            }
                        } catch (JsonProcessingException e) {
                            throw new RuntimeException(e);
                        }
                    }
                })
                .addEventData("event.dimensions")
                .debounce(100); // Wait a tiny bit for a pause while resizing, otherwise it will choke the connection for no reason...
    }

    private ComponentMapping getComponentMapping(Component component) {
        return idToComponentMapping.computeIfAbsent(
                componentToId.computeIfAbsent(component, c -> nextId++), id -> mapComponent(component, id));
    }

    private ComponentMapping mapComponent(Component c, Integer id) {
        Runnable register = () -> {
            var componentElement = c.getElement();
            uiElement.executeJs("""
                    const el = $0;
                    const id = $1;
                    if(el instanceof HTMLElement) {
                        el._resizeObserverId = id;
                        this._resizeObserver.observe(el);
                        this._resizeObserverElements[id] = el;
                    } else {
                        throw new Error("el not Element, Flow bug?");
                    }
                """, componentElement, id).then(jsonvalue -> {
            }, s -> {
                if(s.contains("el not Element, Flow bug") && !c.isAttached()) {
                    // Ignore, TODO should be fixed in Flow!?
                    Logger.getLogger(ResizeObserver.class.getName()).fine("Flow bug!? Got a null reference of element to JS execution:" + s);
                } else {
                    throw new RuntimeException("Error adding size observer:" + s);
                }
            });
        };
        if(c.isAttached()) {
            register.run();
        } else {
            c.addAttachListener(e -> {
                register.run();
                e.unregisterListener();
            });
        }
        c.addDetachListener(e -> {
            if(e.getUI().isClosing()) {
                // UI itself is being closed, no need to do clean up
                return;
            }
            idToComponentMapping.remove(id);
            componentToId.remove(c);
            // Note sure how browsers have implemented ResizeObserver, but manually cleaning
            // up elements registered to the observer to avoid memory leaks
            uiElement.executeJs("""
                    const el = this._resizeObserverElements[$0];
                    if(el) {
                        delete this._resizeObserverElements[$0];
                        this._resizeObserver.unobserve(el);
                    }
                """, id);
            e.unregisterListener();
        });

        return new ComponentMapping(id, c);
    }

    public ResizeObserver observe(Component c, SizeChangeListener listener) {
        getComponentMapping(c).listeners().add(listener);
        return this;
    }

    public ResizeObserver withDebounceTimeout(int timeout) {
        reg.debounce(timeout);
        return this;
    }

}
