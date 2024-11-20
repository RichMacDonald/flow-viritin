package org.vaadin.firitin.components.popover;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.icon.VaadinIcon;
import org.vaadin.firitin.components.button.VButton;

/**
 * A button that shows a popover. Can be used as a basis for example for
 * custom menu's etc. The popover (and contents) is generated dynamically
 * with {@link ContentProvider}, so it doesn't consume resources unless actually
 * used.
 * <p>
 * Uses {@link VaadinIcon} ELLIPSIS_V by default, override with {@link #setIcon(Component)} and/or
 * {@link #setText(String)}-
 * </p>
 */
public class PopoverButton extends VButton {

    private final ContentProvider contentProvider;
    private VPopover popover;

    public PopoverButton(ContentProvider contentProvider) {
        this.contentProvider = contentProvider;
        addClickListener(e -> open());
        // TODO figure out if this is a good default or not 🤷
        setIcon(VaadinIcon.ELLIPSIS_V.create());
    }

    public VPopover getPopover() {
        if (popover == null) {
            popover = new VPopover(contentProvider);
            popover.setTarget(this);
        }
        return popover;
    }

    /**
     * Programmatically open the popup. E.g. if you want show the popover
     * by default.
     */
    public void open() {
        getPopover().open();
        getPopover().addOpenedChangeListener(e -> {
            if (!e.isOpened()) {
                // The popover might be left to the DOM forever if not removed.
                // We can safely do cleanup here, a new popower will be created if needed.
                popover.removeFromParent();
                popover = null;
            }
        });
    }

    /**
     * Programmatically close the popop.
     */
    public void close() {
        if (popover != null) {
            popover.close();
        }
    }
}
