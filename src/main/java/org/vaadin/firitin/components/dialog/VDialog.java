package org.vaadin.firitin.components.dialog;

import org.vaadin.firitin.fluency.ui.FluentComponent;
import org.vaadin.firitin.fluency.ui.FluentHasComponents;
import org.vaadin.firitin.fluency.ui.FluentHasSize;
import org.vaadin.firitin.util.ResizeObserver;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.shared.Registration;

public class VDialog extends Dialog implements FluentComponent<VDialog>,FluentHasSize<VDialog>,FluentHasComponents<VDialog> {

	public VDialog(Component... components) {
		super(components);
	}

	public VDialog withCloseOnOutsideClick(boolean closeOnOutsideClick) {
		setCloseOnOutsideClick(closeOnOutsideClick);
		return this;
	}

	public VDialog withOpened(boolean opened) {
		setOpened(opened);
		return this;
	}

	public VDialog withOpenedChangeListener(ComponentEventListener<OpenedChangeEvent> listener) {
		addOpenedChangeListener(listener);
		return this;
	}

	public VDialog withDialogCloseActionListener(ComponentEventListener<DialogCloseActionEvent> listener) {
		addDialogCloseActionListener(listener);
		return this;
	}

	@Override
	public VDialog withComponentAtIndex(int index, Component component) {
		addComponentAtIndex(index, component);
		return this;
	}

	public VDialog withCloseOnEsc(boolean closeOnEsc) {
		setCloseOnEsc(closeOnEsc);
		return this;
	}

	public VDialog withModal(boolean modal) {
		setModal(modal);
		return this;
	}

	public VDialog withDraggable(boolean draggable) {
		setDraggable(draggable);
		return this;
	}

	public VDialog withResizable(boolean resizable) {
		setResizable(resizable);
		return this;
	}

//Need solution for name erasure
//	public Registration addResizeListener(ComponentEventListener<ComponentEvent> listener) {
//		// had to override because compiler settings show inheriting two methods with different erased signatures
//		if (ResizeObserver.SizeChangeEvent.class.isInstance(listener)) {
//			return FluentHasSize.super.addResizeListener((ComponentEventListener<ResizeObserver.SizeChangeEvent>.SizeChangeEvent) listener);
//		}
//		if (DialogResizeEvent.class.isInstance(listener)) {
//			return super.addResizeListener( (ComponentEventListener<DialogResizeEvent>) listener);
//		}
//	}

}
