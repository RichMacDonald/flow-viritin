package org.vaadin.firitin.appframework;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.RouteAlias;

@MenuItem(order = MenuItem.BEGINNING)
@PageTitle("First")
@RouteAlias(value="", layout = MyMainLayout.class)
public class MyFirstView extends MyAbstractView {
    public MyFirstView() {
        add(new Paragraph("First content"));

        add(new Button("Navigate to second", event -> {
            getUI().ifPresent(ui -> ui.navigate(MySecondView.class));
        }));
    }
}
