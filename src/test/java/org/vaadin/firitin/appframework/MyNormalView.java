package org.vaadin.firitin.appframework;

import com.vaadin.flow.component.AttachEvent;
import org.vaadin.firitin.components.RichText;

@MenuItem
public class MyNormalView extends MyAbstractView {

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        if (attachEvent.isInitialAttach()) {
            add(new RichText().withMarkDown("""
            # Just a view, testing if they are sorted properly
            
            """));
        }
   }
}
