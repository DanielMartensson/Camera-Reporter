package se.martenssonborg.views;

import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.PWA;

import se.martenssonborg.yolo.YoloFunctionality;

/**
 * The main view contains a button and a click listener.
 */
@Route("") // Route är http://localhost:8080/ . Om Route var "main" så skulle MainView vara
			// http://localhost:8080/main
@PWA(name = "Project Base for Vaadin", shortName = "Project Base", enableInstallPrompt = false) // Detta är till för att få mobilanpassat
@JsModule("./styles/shared-styles.js")
@CssImport("./styles/views/main/main-view.css")
public class MainView extends VerticalLayout {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L; // Denna är till för annars får jag en liten varning på "MainView" namnet ovan. Vet ej varför

	public MainView() {
		// Use TextField for standard text input
		TextField textField = new TextField("Your name");
		// textField.setEnabled(false); Med denna så kan vi avvaktivera. Tänk på att
		// många av Java objekt har .set... eller .get...

		Label text = new Label("Gå till http://localhost:8080/applayout");

		VerticalLayout VL = new VerticalLayout();
		VL.add(textField, text);
		add(VL);

		/*
		 * Eclipse tips: Source -> Clean up = Tar bort alla onödiga "import" som du ej
		 * använder Source -> Format = Snyggar till din kod. Du kan ställa till
		 * radbrytningarna i Window -> Preferences -> Java -> Code Style -> Formatter -> Edit -> Line Warping. Sätt maximum till högt tall t.ex. 300
		 */
		
		Button button = new Button("Kör YOLO");
		button.addClickListener(e -> {
			YoloFunctionality.testYoloWithSvhnData(); // Denna är statisk. Därför kan vi anropa den så här utan att skapa objekt! Funktionsprogrammering med andra ord.
		});
		add(button);
	}
}
