package org.vaadin.firitin.appframework;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.dom.Style;
import com.vaadin.flow.router.Menu;

/**
 * A component to represent a main view in the navigation menu
 */
public class BasicNavigationItem extends SideNavItem implements NavigationItem {
    private final Class<? extends Component> navigationTarget;
    private final String text;
    private String path;
    private boolean disabled = false;
    private NavigationItem parentItem;

    public BasicNavigationItem(Class<? extends Component> navigationTarget) {
        super(null, navigationTarget);
        getStyle().setDisplay(Style.Display.BLOCK); // TODO WTF?
        text = NavigationItem.getMenuTextFromClass(navigationTarget);
        setLabel(text);
        MenuItem me = navigationTarget.getAnnotation(MenuItem.class);
        if (me != null) {
            if(me.icon() != null) {
                setPrefixComponent(new Icon(me.icon()));
            }
            if(!me.enabled()) {
                setEnabled(false);
            }
            if(me.openByDefault()) {
                setExpanded(true);
            }
        } else if(navigationTarget.isAnnotationPresent(Menu.class)) {
            Menu menu = navigationTarget.getAnnotation(Menu.class);
            if(menu.icon() != null) {
                setPrefixComponent(new Icon(menu.icon()));
            }
        }
        this.navigationTarget = navigationTarget;
    }

    @Override
    public String getText() {
        return text;
    }

    @Override
    public Class<? extends Component> getNavigationTarget() {
        return navigationTarget;
    }

    @Override
    public void setPath(String path) {
        this.path = path;
        if(!disabled) {
            super.setPath(path);
        }
    }

    public void setEnabled(boolean enabled) {
        this.disabled = !enabled;
        if(disabled) {
            super.setPath((String) null);
        } else if (path != null) {
            super.setPath(path);
        }
        String color = enabled ? "" : "gray";
        getStyle().setColor(color);
    }

    @Override
    public boolean isEnabled() {
        return !disabled;
    }

    @Override
    public void setActive(boolean active) {
        if (active) {
            getElement().setAttribute("active", true);
        } else {
            getElement().removeAttribute("active");
        }
    }

    @Override
    public void addSubItem(NavigationItem item) {
        if(item instanceof BasicNavigationItem bi) {
            addItem(bi);
        } else {
            addSubMenu((SubMenu) item);
        }
    }

    private void addSubMenu(SubMenu item) {
        getElement().appendChild(item.getElement());
    }

    @Override
    public void setParentItem(NavigationItem parent) {
        this.parentItem = parent;
    }

    @Override
    public NavigationItem getParentItem() {
        return parentItem;
    }


}
