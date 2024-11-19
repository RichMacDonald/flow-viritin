package org.vaadin.firitin.resizeobserver.issue75marc;

import com.vaadin.flow.component.html.Div;
import org.vaadin.firitin.rad.PrettyPrinter;
import org.vaadin.firitin.util.ResizeObserver;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.HasComponents;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import org.vaadin.firitin.util.style.LumoProps;

public abstract class ResizeObserverView<C extends Component & HasComponents> extends Composite<C> {

    private ResizeObserver resizeObserver;

    protected ResizeObserverView() {
        super();
        getResizeObserver().observe(getContent(), dimensions -> {
            boolean attached = isAttached();
            if(!attached) {
                // This should not happen !?
                System.out.println(getClass().getSimpleName() +" is not attached");
            }
            System.out.println(getClass().getSimpleName() + "(" + hashCode() + ")" +" Height: " + dimensions.height());
            System.out.println(getClass().getSimpleName() +" Width: " + dimensions.width());
            getSizeDiv().add(PrettyPrinter.toVaadin(dimensions));
        });
    }

    Div sizeDiv;

    Div getSizeDiv() {
        if(sizeDiv == null) {
            sizeDiv = new Div();
            sizeDiv.setHeight("320px");
            sizeDiv.getStyle().setBackgroundColor(LumoProps.CONTRAST_5PCT.var());
            sizeDiv.getStyle().setPadding("1em");
        }
        sizeDiv.removeAll();
        sizeDiv.add("View dimensions:");
        return sizeDiv;
    }

    @Override
    protected C initContent() {
        var content = super.initContent();
        try {
            Thread.sleep(500); // Simulate database query or longer reflective operation
            content.add(new Text(getClass().getSimpleName()));
            content.add(getSizeDiv());
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

    @Route(layout = MainLayout.class)
    public static class ResizeObserver1View extends ResizeObserverView<VerticalLayout> {

    }

    @Route(layout = MainLayout.class)
    public static class ResizeObserver2View extends ResizeObserverView<VerticalLayout> {

    }

    @Route(layout = MainLayout.class)
    public static class ResizeObserver3View extends ResizeObserverView<VerticalLayout> {

    }

}