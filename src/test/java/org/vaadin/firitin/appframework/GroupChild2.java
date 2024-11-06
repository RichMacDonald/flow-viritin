package org.vaadin.firitin.appframework;

import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;

@MenuItem(title = "GroupChild2,but first", order = MenuItem.BEGINNING, parent = GroupView.class)
@Route(layout = MyMainLayout.class)
public class GroupChild2 extends VerticalLayout {

    public GroupChild2() {
        add(new Paragraph("Child view, should be first in submenu"));
    }
}
