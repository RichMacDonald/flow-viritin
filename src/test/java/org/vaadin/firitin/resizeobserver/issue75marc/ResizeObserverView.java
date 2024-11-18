package org.vaadin.firitin.resizeobserver.issue75marc;

import org.vaadin.firitin.util.ResizeObserver;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.HasComponents;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;

public abstract class ResizeObserverView<C extends Component & HasComponents> extends Composite<C> {

    private ResizeObserver resizeObserver;

    protected ResizeObserverView() {
        super();
        getResizeObserver().observe(getContent(), dimensions -> {
            System.out.println("Height: " + dimensions.height());
            System.out.println("Width: " + dimensions.width());
        });
    }

    @Override
    protected C initContent() {
        var content = super.initContent();
        try {
            Thread.sleep(500); // Simulate database query or longer reflective operation
            content.add(new Text("foo"));
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
        return content;
    }

    public ResizeObserver getResizeObserver() {
        if (resizeObserver == null) {
            resizeObserver = ResizeObserver.get();
        }
        return resizeObserver;
    }

    @Route(value = "resize1", layout = MainLayout.class)
    public static class ResizeObserver1View extends ResizeObserverView<VerticalLayout> {

    }

    @Route(value = "resize2", layout = MainLayout.class)
    public static class ResizeObserver2View extends ResizeObserverView<VerticalLayout> {

    }

    @Route(value = "resize3", layout = MainLayout.class)
    public static class ResizeObserver3View extends ResizeObserverView<VerticalLayout> {

    }

}