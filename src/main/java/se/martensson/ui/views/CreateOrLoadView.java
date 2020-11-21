package se.martensson.ui.views;

import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.router.Route;

import se.martensson.service.ObjectNameService;
import se.martensson.ui.views.templates.BarForAppLayout;

/**
 * The main view contains a button and a click listener.
 */
@Route("createload")
@JsModule("./styles/shared-styles.js")
@CssImport("./styles/views/main/main-view.css")
public class CreateOrLoadView extends AppLayout {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public CreateOrLoadView() {
		// Banner and tabs
		BarForAppLayout barForApplayput = new BarForAppLayout();
		addToNavbar(barForApplayput.getDrawerToggle(), barForApplayput.getImg());
		addToDrawer(barForApplayput.getTabs());
		
		// Status terminal
		TextArea terminal = new TextArea();
		terminal.setPlaceholder("Write status here ...");
		terminal.setWidthFull();
		terminal.setHeightFull();
		
		// Logic buttons
		Button loadModelButton = new Button("Load trained model");
		loadModelButton.addClickListener(e -> {
			loadModel();
		});
		Button loadPicturesButton = new Button("Load folder with sub picture folders");
		loadPicturesButton.addClickListener(e -> {
			loadPictures();
		});
		Button trainModelButton = new Button("Train model from sub picture folders");
		trainModelButton.addClickListener(e -> {
			trainModel();
		});
		Button downloadModelButton = new Button("Download trained model");
		downloadModelButton.addClickListener(e -> {
			downloadModel();
		});

		
		// Layout
		VerticalLayout layout = new VerticalLayout();
		FormLayout buttonForm = new FormLayout(loadModelButton, loadPicturesButton, trainModelButton, downloadModelButton);
		layout.add(buttonForm);
		layout.add(terminal);
		setContent(layout);
		
	}

	private void downloadModel() {

	}

	private void trainModel() {
		
	}

	private void loadPictures() {
		
	}

	private void loadModel() {
	}
}
