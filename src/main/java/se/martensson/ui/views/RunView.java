package se.martensson.ui.views;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import com.github.sarxos.webcam.Webcam;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.PWA;

import se.martensson.threads.ImageShowRealTimeThread;
import se.martensson.ui.views.templates.BarForAppLayout;


@Route("")
@PWA(name = "Project Base for Vaadin", shortName = "Project Base", enableInstallPrompt = false)
@JsModule("./styles/shared-styles.js")
@CssImport("./styles/views/main/main-view.css")
@Push
public class RunView extends AppLayout {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final String START = "START";
	private static final String STOP = "STOP";
	private Webcam selectedWebcam = null;
	private AtomicBoolean startStopThread = new AtomicBoolean();
	private Button startStopYOLO;

	public RunView() {
		// Banner and tabs
		BarForAppLayout barForApplayput = new BarForAppLayout();
		addToNavbar(barForApplayput.getDrawerToggle(), barForApplayput.getImg());
		addToDrawer(barForApplayput.getTabs());
		
		// Create image for the real time
		Image realTimeCameraImage = new Image();
		realTimeCameraImage.setClassName("img-horizontal-mirror");
		realTimeCameraImage.setWidth("1280px");
		realTimeCameraImage.setHeight("720px");
		realTimeCameraImage.setTitle("Real Time Camera");
		
		// Start the thread
		ImageShowRealTimeThread imageShowRealTimeThread = new ImageShowRealTimeThread(startStopThread, UI.getCurrent());
		imageShowRealTimeThread.start();
		
		// Create the drop down button for the camera
		Select<String> cameras = new Select<String>();
		createCameraSelectorButton(cameras, imageShowRealTimeThread, realTimeCameraImage);
		
		// Start and stop button for YOLO
		startStopYOLO = new Button(START);
		startStopYOLO.setEnabled(false); // We need to first select the camera
		createYOLOStartButton(realTimeCameraImage, cameras);
		
		// Content
		VerticalLayout layout = new VerticalLayout();
		layout.add(new FormLayout(startStopYOLO, cameras));
		layout.add(realTimeCameraImage);
		layout.setAlignItems(Alignment.CENTER);
		setContent(layout);
				
	}
	
	
	/**
	 * This creates the start and stop listener for YOLO
	 * @param startStopYOLO
	 * @param realTimeCameraImage 
	 * @param cameras 
	 */
	private void createYOLOStartButton(Image realTimeCameraImage, Select<String> cameras) {
		startStopYOLO.addClickListener(e -> {
			if (startStopThread.get() == false) {
				startStopYOLO.setText(STOP);
				startStopThread.set(true); // Start YOLO program here
				cameras.setEnabled(false);
			} else {
				startStopYOLO.setText(START);
				startStopThread.set(false); // Stop YOLO program here
				cameras.setEnabled(true);
			}
		});
	}


	/**
	 * This creates the camera selector drop down button and also gives it a listener for enable the camera
	 * @param cameras
	 * @param imageShowRealTimeThread 
	 * @param realTimeCameraImage 
	 */
	private void createCameraSelectorButton(Select<String> cameras, ImageShowRealTimeThread imageShowRealTimeThread, Image realTimeCameraImage) {
		// Fill with camera names
		List<Webcam> webcamsList = Webcam.getWebcams();
		String[] webcamNames = new String[webcamsList.size()];
		int i = 0;
		for(Webcam webcam : webcamsList) {
			String cameraName = webcam.getName();
			boolean contains = Arrays.stream(webcamNames).anyMatch(cameraName::equals); // Check if cameraName contains in webcamArray
			if(!contains) {
				webcamNames[i] = cameraName;
				i++;
			}
		}
		cameras.setItems(webcamNames);
		
		// Add a listener for enabling the camera
		cameras.addValueChangeListener(e -> {
			if(selectedWebcam == null) {
				selectNewCamera(cameras, imageShowRealTimeThread, realTimeCameraImage);
			}else {
				selectedWebcam.close();
				selectNewCamera(cameras, imageShowRealTimeThread, realTimeCameraImage);
			}
		});
	}

	/**
	 * This will select the camera from webcamsList for us and open it
	 * @param cameras
	 * @param imageShowRealTimeThread 
	 * @param realTimeCameraImage 
	 */
	private void selectNewCamera(Select<String> cameras, ImageShowRealTimeThread imageShowRealTimeThread, Image realTimeCameraImage) {
		List<Webcam> webcamsList = Webcam.getWebcams();
		String selectedCameraName = cameras.getValue();
		selectedWebcam = webcamsList.stream().filter(x -> selectedCameraName.equals(x.getName())).findFirst().get(); // This generates a new object of the web cam
		try {
			selectedWebcam.open();	
			startStopYOLO.setEnabled(true);
			imageShowRealTimeThread.setSelectedWebcam(selectedWebcam);
			imageShowRealTimeThread.setRealTimeCameraImage(realTimeCameraImage);
		}catch(Exception e) {
			startStopYOLO.setEnabled(false);
			Notification notification = new Notification("You cannot select this camera!", 2000);
			notification.open();
		}
	}
}
