package org.vaadin.firitin.appframework;

import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;

@MenuItem(order = MenuItem.DEFAULT + 1, parent = GroupWithoutTarget.class)
@Route(layout = MyMainLayout.class)
public class GroupChild3 extends VerticalLayout {

    public GroupChild3() {
        add(new Paragraph("Child view " + getClass().getSimpleName()));
    }
}
