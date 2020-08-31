package se.martenssonborg.views.templates;

import org.springframework.stereotype.Component;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;

import lombok.Data;
import se.martenssonborg.views.AppLayoutView;
import se.martenssonborg.views.MainView;

@Component // Viktigt att detta är en Spring Boot klass
@Data // Denna är till för att istället för att skapa "Getters And Setters" för varje fält(img, tabs, drawerToggle). Denna kräver <artifactId>lombok</artifactId> i .pom xml filen
public class BarForAppLayout {
	
	// Vi kan även sätta @Getter eller @Setter framför dessa "private" orden om vi vill speicifikt ha olika åtkomst till dom
	private Image img;
	private Tabs tabs;
	private DrawerToggle drawerToggle;

	// Skapa lite saker
	public BarForAppLayout() {
		img = new Image("https://i.imgur.com/GPpnszs.png", "Vaadin Logo");
        img.setHeight("44px");
        
        drawerToggle = new DrawerToggle();
        drawerToggle.setAutofocus(true); // Om du klickar på enter så fort du är inne på http://localhost:8080/applayout så kommer du trycka på fyrkanten i vänstra hörnet
        
        // Skapa tabbar med lyssnare
        Tab home = new Tab("Home");
        home.getElement().addEventListener("click", e -> {
        	UI.getCurrent().navigate(MainView.class);
        });
        
        Tab enTillTab = new Tab("En till tab");
        enTillTab.getElement().addEventListener("click", e -> {
        	UI.getCurrent().navigate(AppLayoutView.class);
        });
        
        tabs = new Tabs(home, enTillTab);
      
        tabs.setOrientation(Tabs.Orientation.VERTICAL);
	}

	/* Behövs inte på grund utav @Data
	public Image getImg() {
		return img;
	}

	public void setImg(Image img) {
		this.img = img;
	}

	public Tabs getTabs() {
		return tabs;
	}

	public void setTabs(Tabs tabs) {
		this.tabs = tabs;
	}
	*/
	
	
}
