package org.vaadin.firitin.resizeobserver;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.shared.Registration;
import org.vaadin.firitin.components.RichText;
import org.vaadin.firitin.rad.PrettyPrinter;
import org.vaadin.firitin.util.style.LumoProps;

@Route
public class ResizeObserverCoreApiDrafting extends VerticalLayout implements ResizeObservable {

    private Registration registration;

    public ResizeObserverCoreApiDrafting() {
        setSizeFull();
        add(new RichText().withMarkDown("""
            This is a test of a potential API to Flow core (HasSize interface).
            The ResizeObserver itself could be an internal helper to Flow.
            """));

        var div = new Div();
        div.getStyle().setBackground(LumoProps.CONTRAST_5PCT.var());

        Button toggle = new Button("Start observing");
        toggle.addClickListener(e -> {
            if (registration == null) {
                registration = addResizeListener(event -> {
                    // This is what users would most often fetch, should probably be brought to event level
                    int width = event.getDimensions().width();

                    div.removeAll();
                    div.add(PrettyPrinter.toVaadin(event.getDimensions()));
                });
                toggle.setText("Stop observing");
            } else {
                registration.remove();
                registration = null;
                toggle.setText("Start observing");
            }
        });

        add(toggle, div);

    }
}
