package org.vaadin.firitin.appframework;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Paragraph;

// Same order as with MyFirstView.java, but title (derived from classname) after it alphabetically
@MenuItem(order = MenuItem.BEGINNING)
public class MySecondView extends MyAbstractView {
    public MySecondView() {
        add(new Paragraph("Second content"));

        add(new Button("Toggle fourth", e-> {
            NavigationItem item = findAncestor(MyMainLayout.class).getNavigationItems().get(3);
            item.setEnabled(!item.isEnabled());
        }));

    }
}
