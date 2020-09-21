package se.martenssonborg.ui.views;

import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.router.Route;

/**
 * The main view contains a button and a click listener.
 */
@Route("create")
@JsModule("./styles/shared-styles.js")
@CssImport("./styles/views/main/main-view.css")
public class CreateView extends AppLayout {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public CreateView() {
	
	}
}
