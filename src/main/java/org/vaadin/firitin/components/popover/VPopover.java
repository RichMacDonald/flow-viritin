package org.vaadin.firitin.components.popover;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.popover.Popover;
import com.vaadin.flow.component.popover.PopoverVariant;
import org.vaadin.firitin.fluency.ui.FluentHasSize;
import org.vaadin.firitin.fluency.ui.FluentHasStyle;

public class VPopover extends Popover implements FluentHasStyle<VPopover>, FluentHasSize<VPopover> {

    public VPopover() {
        addThemeVariants(PopoverVariant.ARROW);
    }

    public VPopover(ContentProvider contentProvider) {
        this();
        addOpenedChangeListener(event -> {
            if(event.isOpened()) {
                add(contentProvider.get());
            } else {
                removeAll();
            }
        });
    }

    /**
     *
     * @param components
     *            the components to add
     * @deprecated Warning, using this method directly easily creates excessive memory usage,
     * consider passing in {@link ContentProvider} in the constructor, which postpones the
     * content creation to a moment when actually needed.
     */
    @Deprecated
    @Override
    public void add(Component... components) {
        super.add(components);
    }

}
