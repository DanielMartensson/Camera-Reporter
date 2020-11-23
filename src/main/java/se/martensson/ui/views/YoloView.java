package se.martensson.ui.views;

import java.io.File;
import java.io.FilenameFilter;
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
	private static final String START = "START";
	private static final String STOP = "STOP";
	private Webcam selectedWebcam = null;
	private AtomicBoolean startStopThread = new AtomicBoolean();
	private Button startStopYOLO;

	public YoloView(YoloObjectService yoloObjectService, SendMail sendMail) {
		// Banner and tabs
		BarForAppLayout barForApplayput = new BarForAppLayout();
		addToNavbar(barForApplayput.getDrawerToggle(), barForApplayput.getImg());
		addToDrawer(barForApplayput.getTabs());

		// Create image for the real time
		Image realTimeCameraImage = new Image();
		realTimeCameraImage.setWidth("1280px");
		realTimeCameraImage.setHeight("720px");
		realTimeCameraImage.setTitle("Real Time Camera");

		// Create selectors for Darknet
		Select<ListUploadedFiles> darknet = new Select<ListUploadedFiles>();
		darknet.setLabel("Darknet");
		Select<ListUploadedFiles> configuration = new Select<ListUploadedFiles>();
		configuration.setLabel("Configuration");
		Select<ListUploadedFiles> weights = new Select<ListUploadedFiles>();
		weights.setLabel("Weights");
		Select<String> thresholds = new Select<String>(new String[] { "0.1", "0.2", "0.3", "0.4", "0.5", "0.6", "0.7", "0.8", "0.9", "1.0"});
		thresholds.setLabel("Thresholds");
		scanFiles(darknet, configuration, weights);

		// Start the thread
		ImageShowRealTimeThread imageShowRealTimeThread = new ImageShowRealTimeThread(startStopThread, UI.getCurrent(), darknet, configuration, weights, thresholds, yoloObjectService, sendMail);
		imageShowRealTimeThread.start();

		// Create the drop down button for the camera
		Select<String> cameras = new Select<String>();
		cameras.setLabel("Camera");
		cameras.setEnabled(false);
		darknet.addValueChangeListener(e -> enableCameraDropDownButton(cameras, darknet, configuration, weights, thresholds));
		configuration.addValueChangeListener(e -> enableCameraDropDownButton(cameras, darknet, configuration, weights, thresholds));
		weights.addValueChangeListener(e -> enableCameraDropDownButton(cameras, darknet, configuration, weights, thresholds));
		thresholds.addValueChangeListener(e -> enableCameraDropDownButton(cameras, darknet, configuration, weights, thresholds));
		createCameraSelectorButton(cameras, imageShowRealTimeThread, realTimeCameraImage, darknet, configuration, weights, thresholds);

		// Start and stop button for YOLO
		startStopYOLO = new Button(START);
		startStopYOLO.setEnabled(false); // We need to first select the camera
		createYOLOStartButton(realTimeCameraImage, cameras, darknet, configuration, weights, thresholds);

		// Content
		VerticalLayout layout = new VerticalLayout();
		layout.add(new FormLayout(startStopYOLO, cameras, darknet, configuration, weights, thresholds));
		layout.add(realTimeCameraImage);
		layout.setAlignItems(Alignment.CENTER);
		setContent(layout);

	}

	private void enableCameraDropDownButton(Select<String> cameras, Select<ListUploadedFiles> darknet, Select<ListUploadedFiles> configuration, Select<ListUploadedFiles> weights, Select<String> thresholds) {
		if (darknet.getValue() != null && configuration.getValue() != null && weights.getValue() != null && thresholds.getValue() != null) {
			cameras.setEnabled(true);
		} else {
			cameras.setEnabled(false);
		}
	}

	private void scanFiles(Select<ListUploadedFiles> darknet, Select<ListUploadedFiles> configuration, Select<ListUploadedFiles> weights) {
		// Get all the current files
		File[] configurationFiles = new File("Darknet/" + FileUploaderView.CFG).listFiles((File pathname) -> pathname.getName().endsWith(".cfg"));
		File[] yoloFiles = new File("Darknet/").listFiles((File pathname) -> !pathname.getName().contains(".") && pathname.isFile());
		File[] weightsFiles = new File("Darknet/" + FileUploaderView.WEIGHTS).listFiles((File pathname) -> pathname.getName().endsWith(".weights"));

		// Fill them
		fillSelecter(darknet, yoloFiles);
		fillSelecter(configuration, configurationFiles);
		fillSelecter(weights, weightsFiles);
	}

	private void fillSelecter(Select<ListUploadedFiles> selector, File[] files) {
		selector.clear();
		ArrayList<ListUploadedFiles> list = new ArrayList<>();
		for (File file : files)
			list.add(new ListUploadedFiles(file.getPath(), file));
		selector.setItems(list);
		selector.setTextRenderer(ListUploadedFiles::getFilePath);
	}

	/**
	 * This creates the start and stop listener for YOLO
	 * 
	 * @param startStopYOLO
	 * @param realTimeCameraImage
	 * @param cameras
	 * @param weights
	 * @param configuration
	 * @param darknet
	 * @param thresholds
	 */
	private void createYOLOStartButton(Image realTimeCameraImage, Select<String> cameras, Select<ListUploadedFiles> darknet, Select<ListUploadedFiles> configuration, Select<ListUploadedFiles> weights, Select<String> thresholds) {
		startStopYOLO.addClickListener(e -> {
			if (startStopThread.get() == false) {
				startStopYOLO.setText(STOP);
				startStopThread.set(true); // Start YOLO program here
				cameras.setEnabled(false);
				darknet.setEnabled(false);
				configuration.setEnabled(false);
				weights.setEnabled(false);
				thresholds.setEnabled(false);
			} else {
				startStopYOLO.setText(START);
				startStopThread.set(false); // Stop YOLO program here
				cameras.setEnabled(true);
				darknet.setEnabled(true);
				configuration.setEnabled(true);
				weights.setEnabled(true);
				thresholds.setEnabled(true);
			}
		});
	}

	/**
	 * This creates the camera selector drop down button and also gives it a
	 * listener for enable the camera
	 * 
	 * @param cameras
	 * @param imageShowRealTimeThread
	 * @param realTimeCameraImage
	 * @param weights
	 * @param configuration
	 * @param darknet
	 * @param thresholds
	 */
	private void createCameraSelectorButton(Select<String> cameras, ImageShowRealTimeThread imageShowRealTimeThread, Image realTimeCameraImage, Select<ListUploadedFiles> darknet, Select<ListUploadedFiles> configuration, Select<ListUploadedFiles> weights, Select<String> thresholds) {
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
				selectNewCamera(cameras, imageShowRealTimeThread, realTimeCameraImage, darknet, configuration, weights, thresholds);
			} else {
				selectedWebcam.close();
				selectNewCamera(cameras, imageShowRealTimeThread, realTimeCameraImage, darknet, configuration, weights, thresholds);
			}
		});
	}

	/**
	 * This will select the camera from webcamsList for us and open it
	 * 
	 * @param cameras
	 * @param imageShowRealTimeThread
	 * @param realTimeCameraImage
	 * @param weights
	 * @param configuration
	 * @param darknet
	 * @param thresholds
	 */
	private void selectNewCamera(Select<String> cameras, ImageShowRealTimeThread imageShowRealTimeThread, Image realTimeCameraImage, Select<ListUploadedFiles> darknet, Select<ListUploadedFiles> configuration, Select<ListUploadedFiles> weights, Select<String> thresholds) {
		List<Webcam> webcamsList = Webcam.getWebcams();
		String selectedCameraName = cameras.getValue();
		selectedWebcam = webcamsList.stream().filter(x -> selectedCameraName.equals(x.getName())).findFirst().get(); // This generates a new object of the web cam
		try {
			if (darknet.getValue() != null && configuration.getValue() != null && weights.getValue() != null && thresholds.getValue() != null) {
				selectedWebcam.open();
				startStopYOLO.setEnabled(true);
				imageShowRealTimeThread.setSelectedWebcam(selectedWebcam);
				imageShowRealTimeThread.setRealTimeCameraImage(realTimeCameraImage);
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
