package se.martenssonborg.views;

import javax.annotation.PostConstruct;

import org.atmosphere.config.service.Post;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.router.Route;

import se.martenssonborg.views.templates.BarForAppLayout;

@Route("applayout")
// Skriv alltid "extends <den layout du vill ha>
@Component // Denna är till för att säga att AppLayoutView är en Spring Boot komponent. Den kräver <artifactId>vaadin-spring-boot-starter</artifactId> i .pom xml filen
public class AppLayoutView extends AppLayout{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	/*
	 * Notera! Vanliga konstruktören måste köras FÖRST
	 * Notera! Autowired är Dependency Injection. Använd denna om objektet barForAppLayout ska vara globalt.
	 * Notera! Autowrired körs bara vid uppstart. Du kan aldrig köra autowired igen om du tar t.ex. new class osv
	 */
	@Autowired 
	BarForAppLayout barForAppLayout;
	
	@PostConstruct // PostConstruct är alltså en konstruktör som körs EFTER vanliga konstruktören
	public void init() {
		
		// Här kan vi göra massa saker
		
		Label minLabel = new Label("Hej!");
		Button Min_knapp = new Button("Min knapp");
		Min_knapp.addClickListener(e -> {
			// När du klickar på denna knapp så skall vi skriva
			String hej = "min text";
			//funktionenSomGarTillMainView(hej);
			
			// Denna
			//Notification myNotifcation = new Notification("Hej! Du klickade", 3000);
			//myNotifcation.open();
			
			// Är exakt samma som denna
			new Notification("Hej! Du klickade", 3000).open(); 
		});
		
		HorizontalLayout horisonten = new HorizontalLayout(minLabel);
		
		addToNavbar(barForAppLayout.getImg(), barForAppLayout.getDrawerToggle()); // Här brukar man alltid lägga någon logotyp osv.
		addToDrawer(barForAppLayout.getTabs()); // Här brukar man alltid lägga tabbarna
		setContent(horisonten); // Det vi vill se
		
	}
	
	/**
	 * Just detta är unikt sätt att kommentera. När man generera API docs av detta projekt så följer alla parametrar med.
	 * Parametern "@param hej" är alltså autogenererat igenom att jag  först skriver /** och sedan CTRL + Enter ovanför "funktionenSomGarTillMainView"
	 * @param hej
	 */
	private void funktionenSomGarTillMainView(String hej) {
		// TODO Auto-generated method stub
		UI.getCurrent().navigate(MainView.class); // Gå till MainView route
	}

	// Vanliga konstruktören - Denna gör inget
	public AppLayoutView() {
		System.out.println("Jag gör inget förutom att skriva detta");
	}

}
