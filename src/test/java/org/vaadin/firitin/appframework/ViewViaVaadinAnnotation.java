package org.vaadin.firitin.appframework;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Menu;
import org.vaadin.firitin.components.RichText;

@Menu(order = MenuItem.END, title = "Menu annotation"/*, icon = "" TODO what to add here!?!  */)
public class ViewViaVaadinAnnotation extends MyAbstractView {

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        if (attachEvent.isInitialAttach()) {
            add(new RichText().withMarkDown("""
            # @Menu annotation support
            
            Vaadin 24.5 introduced @Menu annotation, pretty close to @MenuItem annotation in
            Viritin. Biggest difference is non-typed Icon and non-existing disabled flag and
            slightly odd double value used for ordering.
            """));
            add(new Paragraph("View added with Vaadin's @Menu annotation. " +  VaadinIcon.VAADIN_H.toString()));
            MyMainLayout mainLayout = (MyMainLayout) UI.getCurrent().getChildren().findFirst().get();

            VerticalLayout subview = new VerticalLayout(new Paragraph("Sub view content"), new Button("Close sub view & return to main view", e -> {
                mainLayout.closeSubView();
            }));

            add(new Button("Open sub view", e -> mainLayout.openSubView(subview, "Sub view title")));
        }

    }
}
