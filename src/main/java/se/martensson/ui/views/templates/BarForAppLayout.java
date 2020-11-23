package se.martensson.ui.views.templates;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;

import lombok.Data;
import se.martensson.security.SecurityConfig;
import se.martensson.ui.views.FileUploaderView;
import se.martensson.ui.views.MailConfigurationView;
import se.martensson.ui.views.YoloView;

@Data
public class BarForAppLayout {
	
	private Image img;
	private Tabs tabs;
	private DrawerToggle drawerToggle;

	public BarForAppLayout() {
		// Bar image
		img = new Image("images/logo.png", "Camera Reporter Logo");
        img.setHeight("44px");
        
        // Drawer
        drawerToggle = new DrawerToggle();
        drawerToggle.setAutofocus(true); 
        
        // Tabs
        Tab runTab = new Tab("Run");
        runTab.getElement().addEventListener("click", e -> {
        	UI.getCurrent().navigate(YoloView.class);
        });
        Tab fileUploaderTab = new Tab("File uploader");
        fileUploaderTab.getElement().addEventListener("click", e -> {
        	UI.getCurrent().navigate(FileUploaderView.class);
        });
        Tab createOrLoadTab = new Tab("Mail configuration");
        createOrLoadTab.getElement().addEventListener("click", e -> {
        	UI.getCurrent().navigate(MailConfigurationView.class);
        });
        Tab logoutTab = new Tab("Logout");
        logoutTab.getElement().addEventListener("click", e -> {
        	UI.getCurrent().getPage().setLocation(SecurityConfig.LOGOUT);
        });
        tabs = new Tabs(runTab, fileUploaderTab, createOrLoadTab, logoutTab);
        tabs.setOrientation(Tabs.Orientation.VERTICAL);
	}

}
