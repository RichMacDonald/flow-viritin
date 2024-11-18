package org.vaadin.firitin.resizeobserver.issue75marc;

import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.RouterLink;

public class MainLayout extends AppLayout {
    public MainLayout() {
        VerticalLayout nav = new VerticalLayout();
        nav.add(new RouterLink("ResizeObserver1", ResizeObserverView.ResizeObserver1View.class));
        nav.add(new RouterLink("ResizeObserver2", ResizeObserverView.ResizeObserver2View.class));
        nav.add(new RouterLink("ResizeObserver3", ResizeObserverView.ResizeObserver3View.class));

        addToDrawer(nav);
    }
}
