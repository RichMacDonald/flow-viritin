package org.vaadin.firitin.resizeobserver;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.router.Route;
import org.vaadin.firitin.components.orderedlayout.VVerticalLayout;
import org.vaadin.firitin.util.ResizeObserver;

@Route
public class ResizeObserverIssue75View extends VVerticalLayout {

    public ResizeObserverIssue75View() {

        var size = new Paragraph("UI size unknown (not attached yet)");
        add(size);

        var paragraph = new Paragraph("Lazy attached paragraph");
        paragraph.setWidthFull();

        ResizeObserver.get()
                .observe(paragraph, d -> size.setText(d.toString()));

        add(new Button("Attach paragraph", e -> {
            add(paragraph);
        }));

    }

}
