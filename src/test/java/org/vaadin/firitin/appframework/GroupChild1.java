package org.vaadin.firitin.appframework;

import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;

@MenuItem(order = MenuItem.DEFAULT + 1, parent = GroupView.class)
@Route(layout = MyMainLayout.class)
public class GroupChild1 extends VerticalLayout {

    public GroupChild1() {
        add(new Paragraph("Child view."));
    }
}
