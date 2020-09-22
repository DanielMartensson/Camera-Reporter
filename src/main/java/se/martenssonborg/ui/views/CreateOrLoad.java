package se.martenssonborg.ui.views;

import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.router.Route;

import se.martenssonborg.service.ObjectNameService;
import se.martenssonborg.ui.views.templates.BarForAppLayout;

/**
 * The main view contains a button and a click listener.
 */
@Route("createload")
@JsModule("./styles/shared-styles.js")
@CssImport("./styles/views/main/main-view.css")
public class CreateOrLoad extends AppLayout {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public CreateOrLoad(ObjectNameService objectNameService) {
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
		Button loadModel = new Button("Load pre-trained model");
		loadModel.addClickListener(e -> {
			/*
			 * 1. Point to the pre-trained model that are located at the local computer
			 * 2. Load the model and write the status in the terminal
			 * 3. Clear the ObjectNameEntity and fill it with new object names
			 */
			loadPreTrainedModel(objectNameService);
		});
		Button trainModel = new Button("Train model with pictures from folder with sub folders");
		trainModel.addClickListener(e -> {
			/*
			 * 1. Point to the folder that contains sub folders that holds pictures
			 * 2. Train a model and save it. Write the status in the terminal
			 * 3. Call loadPreTrainedModel(objectNameService)
			 */
			trainModelWithPictures();
			loadPreTrainedModel(objectNameService);
		});

		
		// Layout
		VerticalLayout layout = new VerticalLayout();
		layout.add(new FormLayout(loadModel, trainModel));
		layout.add(terminal);
		setContent(layout);
		
	}

	private void trainModelWithPictures() {
		
	}

	private void loadPreTrainedModel(ObjectNameService objectNameService) {
		objectNameService.deleteAll();
		
	}
}
