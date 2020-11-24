package se.martensson.ui.views;

import java.io.File;
import java.util.ArrayList;
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

import se.martensson.component.SendMail;
import se.martensson.service.YoloObjectService;
import se.martensson.threads.ImageShowRealTimeThread;
import se.martensson.ui.views.templates.BarForAppLayout;
import se.martensson.ui.views.templates.ListUploadedFiles;

@Route("")
@PWA(name = "Project Base for Vaadin", shortName = "Project Base", enableInstallPrompt = false)
@JsModule("./styles/shared-styles.js")
@CssImport("./styles/views/main/main-view.css")
@Push
public class YoloView extends AppLayout {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public static final String START = "START";
	public static final String STOP = "STOP";
	
	
	// For the thread
	private static AtomicBoolean startStopThread = null;
	private static ImageShowRealTimeThread imageShowRealTimeThread = null;
	private static Webcam selectedWebcam = null;
	
	// Selected values - Save them
	private static String selectedCamera = null;
	private static ListUploadedFiles selectedDarknet = null;
	private static ListUploadedFiles selectedConfiguration = null;
	private static ListUploadedFiles selectedData = null;
	private static ListUploadedFiles selectedWeights = null;
	private static String selectedThreshold = null;
	private static String selectedPictureSize = null;

	// This need to be a non-static field
	private Button startStopYOLO = null;


	public YoloView(YoloObjectService yoloObjectService, SendMail sendMail) {
		// Banner and tabs
		BarForAppLayout barForApplayput = new BarForAppLayout();
		addToNavbar(barForApplayput.getDrawerToggle(), barForApplayput.getImg());
		addToDrawer(barForApplayput.getTabs());

		// Create image for the real time
		Image realTimeCameraImage = new Image();
		Select<String> pictureSize = new Select<String>(new String[] {"608x608", "512x512", "416x416", "320x320", "Camera Size"});
		pictureSize.setLabel("Picture size");
		setPictureSize(pictureSize, realTimeCameraImage);
		realTimeCameraImage.setTitle("Real Time Camera");

		// Create selectors for Darknet
		Select<ListUploadedFiles> darknet = new Select<ListUploadedFiles>();
		darknet.setLabel("Darknet");
		Select<ListUploadedFiles> configuration = new Select<ListUploadedFiles>();
		configuration.setLabel("Configuration");
		Select<ListUploadedFiles> data = new Select<ListUploadedFiles>();
		data.setLabel("Data");
		Select<ListUploadedFiles> weights = new Select<ListUploadedFiles>();
		weights.setLabel("Weights");
		scanFiles(darknet, selectedDarknet, configuration, selectedConfiguration, data, selectedData, weights, selectedWeights);
		Select<String> thresholds = new Select<String>(new String[] { "0.1", "0.2", "0.3", "0.4", "0.5", "0.6", "0.7", "0.8", "0.9", "1.0"});
		thresholds.setLabel("Thresholds");
		setPastThresholdValue(thresholds);
		
		// Start and stop button for YOLO
		startStopYOLO = new Button(START);
		startStopYoloConfiguration();

		// Create the thread
		if(imageShowRealTimeThread == null) {
			startStopThread = new AtomicBoolean(false);
			imageShowRealTimeThread = new ImageShowRealTimeThread();
		}
		
		// Create the drop down button for the camera
		Select<String> cameras = new Select<String>();
		cameras.setLabel("Camera");
		darknet.addValueChangeListener(e -> enableCameraDropDownButton(cameras, darknet, configuration, data, weights, thresholds));
		data.addValueChangeListener(e -> enableCameraDropDownButton(cameras, darknet, configuration, data, weights, thresholds));
		weights.addValueChangeListener(e -> enableCameraDropDownButton(cameras, darknet, configuration, data, weights, thresholds));
		thresholds.addValueChangeListener(e -> enableCameraDropDownButton(cameras, darknet, configuration, data, weights, thresholds));
		createCameraSelectorButton(cameras, imageShowRealTimeThread, realTimeCameraImage, darknet, configuration, data, weights, thresholds);
		enableCameraDropDownButton(cameras, darknet, configuration, data, weights, thresholds);
		
		// Set the components to the thread
		imageShowRealTimeThread.setComponentsToThread(startStopYOLO, startStopThread, UI.getCurrent(), darknet, configuration, data, weights, thresholds, yoloObjectService, sendMail, cameras, selectedWebcam, realTimeCameraImage);
		if(!imageShowRealTimeThread.isAlive())
			imageShowRealTimeThread.start();

		
		// Content
		VerticalLayout layout = new VerticalLayout();
		layout.add(new FormLayout(startStopYOLO, cameras, darknet, configuration, data, weights, thresholds, pictureSize));
		layout.add(realTimeCameraImage);
		layout.setAlignItems(Alignment.CENTER);
		setContent(layout);
		
	}

	private void setPictureSize(Select<String> pictureSize, Image realTimeCameraImage) {
		pictureSize.setValue("Camera Size"); // Default value
		pictureSize.addValueChangeListener(e -> {
			String size = e.getValue();
			String[] height_width = size.split("x");
			switch(size) {
			case "Camera Size":
				realTimeCameraImage.setSizeUndefined();
				break;
			default:
				realTimeCameraImage.setWidth(height_width[1] + "px");
				realTimeCameraImage.setHeight(height_width[0] + "px");
				break;
			}
			selectedPictureSize = size;
			
		});
		if(selectedPictureSize != null) {
			pictureSize.setValue(selectedPictureSize);
		}
			
	}

	private void startStopYoloConfiguration() {
		startStopYOLO.setEnabled(false);
		if(selectedWebcam != null) {
			if(selectedWebcam.isOpen()) {
				startStopYOLO.setEnabled(true);
			}
		}
		startStopYOLO.addClickListener(e -> {
			if(startStopThread.get()) {
				startStopThread.set(false);
			}else {
				startStopThread.set(true);
			}
		});
	}

	private void setPastThresholdValue(Select<String> thresholds) {
		thresholds.addValueChangeListener(e -> {
			selectedThreshold = e.getValue();
		});
		if(selectedThreshold != null)
			thresholds.setValue(selectedThreshold);
	}

	private void enableCameraDropDownButton(Select<String> cameras, Select<ListUploadedFiles> darknet, Select<ListUploadedFiles> configuration, Select<ListUploadedFiles> data, Select<ListUploadedFiles> weights, Select<String> thresholds) {
		if (darknet.getValue() != null && configuration.getValue() != null && data.getValue() != null && weights.getValue() != null && thresholds.getValue() != null) {
			cameras.setEnabled(true);
		} else {
			cameras.setEnabled(false);
		}
		selectedDarknet = darknet.getValue();
		selectedConfiguration = configuration.getValue();
		selectedData = data.getValue();
		selectedWeights = weights.getValue();
	}

	private void scanFiles(Select<ListUploadedFiles> darknet, ListUploadedFiles selectedDarknet, Select<ListUploadedFiles> configuration, ListUploadedFiles selectedConfiguration, Select<ListUploadedFiles> data, ListUploadedFiles selectedData, Select<ListUploadedFiles> weights, ListUploadedFiles selectedWeights) {
		// Get all the current files
		File[] configurationFiles = new File("Darknet/" + FileUploaderView.CFG).listFiles((File pathname) -> pathname.getName().endsWith(".cfg"));
		File[] dataFiles = new File("Darknet/" + FileUploaderView.CFG).listFiles((File pathname) -> pathname.getName().endsWith(".data"));
		File[] darknetFiles = new File("Darknet/").listFiles((File pathname) -> !pathname.getName().contains(".") && pathname.isFile());
		File[] weightsFiles = new File("Darknet/" + FileUploaderView.WEIGHTS).listFiles((File pathname) -> pathname.getName().endsWith(".weights"));

		// Fill them
		fillSelecter(darknet, darknetFiles, selectedDarknet);
		fillSelecter(configuration, configurationFiles, selectedConfiguration);
		fillSelecter(data, dataFiles, selectedData);
		fillSelecter(weights, weightsFiles, selectedWeights);
	}

	private void fillSelecter(Select<ListUploadedFiles> selector, File[] files, ListUploadedFiles selectedFile) {
		// Fill the selector
		selector.clear();
		ArrayList<ListUploadedFiles> list = new ArrayList<>();
		for (File file : files)
			list.add(new ListUploadedFiles(file.getPath(), file));
		selector.setItems(list);
		
		// Set to the current value
		if(selectedFile != null) {
			for(ListUploadedFiles l : list) {
				if(selectedFile.getFilePath().equals(l.getFilePath())) {
					selector.setValue(l);
				}
			}
		}
		selector.setTextRenderer(ListUploadedFiles::getFilePath);
	}


	/**
	 * This creates the camera selector drop down button and also gives it a
	 * listener for enable the camera
	 * 
	 * @param cameras
	 * @param imageShowRealTimeThread
	 * @param realTimeCameraImage
	 * @param weights
	 * @param data
	 * @param darknet
	 * @param thresholds
	 */
	private void createCameraSelectorButton(Select<String> cameras, ImageShowRealTimeThread imageShowRealTimeThread, Image realTimeCameraImage, Select<ListUploadedFiles> darknet, Select<ListUploadedFiles> configuration, Select<ListUploadedFiles> data, Select<ListUploadedFiles> weights, Select<String> thresholds) {
		// Fill with camera names
		List<Webcam> webcamsList = Webcam.getWebcams();
		String[] webcamNames = new String[webcamsList.size()];
		int i = 0;
		for (Webcam webcam : webcamsList) {
			String cameraName = webcam.getName();
			boolean contains = Arrays.stream(webcamNames).anyMatch(cameraName::equals); // Check if cameraName contains in webcamArray
			if (!contains) {
				webcamNames[i] = cameraName;
				i++;
			}
		}
		cameras.setItems(webcamNames);

		// Add a listener for enabling the camera
		cameras.addValueChangeListener(e -> {
			if (selectedWebcam == null) {
				selectNewCamera(cameras, imageShowRealTimeThread, realTimeCameraImage, darknet, configuration, data, weights, thresholds);
			} else {
				selectedWebcam.close();
				selectNewCamera(cameras, imageShowRealTimeThread, realTimeCameraImage, darknet, configuration, data, weights, thresholds);
			}
		});
		if(selectedCamera != null) {
			cameras.setValue(selectedCamera);
		}
	}

	/**
	 * This will select the camera from webcamsList for us and open it
	 * 
	 * @param cameras
	 * @param imageShowRealTimeThread
	 * @param realTimeCameraImage
	 * @param weights
	 * @param data
	 * @param darknet
	 * @param thresholds
	 */
	private void selectNewCamera(Select<String> cameras, ImageShowRealTimeThread imageShowRealTimeThread, Image realTimeCameraImage, Select<ListUploadedFiles> darknet, Select<ListUploadedFiles> configuration, Select<ListUploadedFiles> data, Select<ListUploadedFiles> weights, Select<String> thresholds) {
		List<Webcam> webcamsList = Webcam.getWebcams();
		String selectedCameraName = cameras.getValue();
		selectedCamera = cameras.getValue();
		selectedWebcam = webcamsList.stream().filter(x -> selectedCameraName.equals(x.getName())).findFirst().get(); // This generates a new object of the web cam
		try {
			if (darknet.getValue() != null && configuration.getValue() != null && data.getValue() != null && weights.getValue() != null && thresholds.getValue() != null) {
				selectedWebcam.open();
				startStopYOLO.setEnabled(true);
				imageShowRealTimeThread.setSelectedWebcam(selectedWebcam);
			} else {
				startStopYOLO.setEnabled(false);
				Notification notification = new Notification("You need to select the YOLO files!", 2000);
				notification.open();
			}

		} catch (Exception e) {
			startStopYOLO.setEnabled(false);
			Notification notification = new Notification("You cannot select this camera!", 2000);
			notification.open();
		}
	}
}