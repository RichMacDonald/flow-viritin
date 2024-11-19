package org.vaadin.firitin.resizeobserver;

import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.shared.Registration;
import org.vaadin.firitin.rad.PrettyPrinter;
import org.vaadin.firitin.util.ResizeObserver;

@Route
public class ResizeObserverUnobserve extends VerticalLayout {

    private Registration registration;

    public ResizeObserverUnobserve() {
        setSizeFull();
        var results = new Div();
        Checkbox vaadinWay = new Checkbox("Vaadin core style") {
            {
                addValueChangeListener(e -> {
                    if (e.getValue()) {
                        registration = ResizeObserver.get().addResizeListener(ResizeObserverUnobserve.this, event -> {
                            results.removeAll();
                            results.add(PrettyPrinter.toVaadin(event.getDimensions()));
                        });
                    } else {
                        registration.remove();
                    }
                });

            }
        };

        var simple = new Checkbox("Observe/unobserve") {
            {
                ResizeObserver.SizeChangeListener listener = observation -> {
                    results.removeAll();
                    results.add(PrettyPrinter.toVaadin(observation));
                };
                addValueChangeListener(event -> {
                    if (event.getValue()) {
                        ResizeObserver.get().observe(ResizeObserverUnobserve.this, listener);
                    } else {
                        ResizeObserver.get().unobserve(ResizeObserverUnobserve.this, listener);
                    }
                });
            }
        };

        add(vaadinWay, simple, results);

    }
}
