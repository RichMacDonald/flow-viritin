package org.vaadin.firitin.appframework;

import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;

@MenuItem(title = "Group with topview", order = MenuItem.DEFAULT + 1, openByDefault = true)
@Route(layout = MyMainLayout.class)
public class GroupView extends VerticalLayout {

    public GroupView() {
        add(new Paragraph("Group view content. This top level view has some views nested below it in navigation."));
    }
}
