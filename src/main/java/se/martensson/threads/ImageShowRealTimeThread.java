package se.martensson.threads;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.imageio.ImageIO;

import org.apache.commons.io.FileUtils;

import com.github.sarxos.webcam.Webcam;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.server.StreamResource;

import lombok.Setter;
import se.martensson.component.SendMail;
import se.martensson.entity.YoloObjectEntity;
import se.martensson.service.YoloObjectService;
import se.martensson.ui.views.YoloView;
import se.martensson.ui.views.lists.Resolutions;
import se.martensson.ui.views.templates.ListUploadedFiles;

public class ImageShowRealTimeThread extends Thread {

	private UI ui;
	private AtomicBoolean startStopThread;
	@Setter
	private Webcam selectedWebcam;
	private Image realTimeCameraImage;
	private Select<ListUploadedFiles> darknet;
	private Select<ListUploadedFiles> configuration;
	private Select<ListUploadedFiles> data;
	private Select<ListUploadedFiles> weights;
	private Select<String> thresholds;
	private YoloObjectService yoloObjectService;
	private SendMail sendMail;
	private Button startStopYOLO;
	private Select<String> cameras;
	private Select<Resolutions> pictureSize;
	private boolean hasDarknetFolderBeenDownloaded = false;
	
	public static final String ramDiskFolderPath = "/mnt/ramdisk/";

	public ImageShowRealTimeThread() {
		
	}

	@Override
	public void run() {
		while (true) {
			while (startStopThread.get()) {
				try {
					if(!selectedWebcam.isOpen()) {
						ui.access(() -> enableDisableSelectAndButton()); // This will start the camera
					}
					
					// Snap with camera
					BufferedImage cameraImage = selectedWebcam.getImage();
					if(cameraImage != null) {						
						// Save and then detect
						saveStreamToImgFolder(cameraImage);
						callDarkNetToDoItsPrediction();
						
						// Get the stream
						StreamResource predictedFileAsStreamSource = getStreamResourceFromPredictionsFile();
						if(predictedFileAsStreamSource != null) {
							ui.access(() -> {
								if(startStopThread.get()) // Prevents error if predictedFileAsStreamSource is non null and the predictions.jpg does not exist
									realTimeCameraImage.setSrc(predictedFileAsStreamSource);
								enableDisableSelectAndButton();
							});
						}else {
							stopThread();
						}
					}else {
						stopThread();
					}
				} catch (Exception e) {
					// Something bad happen
					e.printStackTrace();
					stopThread();
				}
			}
		}
	}

	private void stopThread() {
		ui.access(() -> {
			startStopThread.set(false); // Stop
			enableDisableSelectAndButton();
		});
	}

	private StreamResource getStreamResourceFromPredictionsFile() {
		return new StreamResource("predictions.jpg", () -> {
			try {
				return new FileInputStream(new File(ramDiskFolderPath + "Darknet/predictions.jpg"));
			} catch (FileNotFoundException e) {
				return null;
			}
		});
	}


	private void callDarkNetToDoItsPrediction() {
		try {
			// Arguments
			String darkPath = darknet.getValue().getFilePath().replace("Darknet", "."); // E.g ./darknet
			String dataFlag = data.getValue().getFilePath().replace("Darknet", "."); // E.g ./cfg/coco.data
			String weightsFlag = weights.getValue().getFilePath().replace("Darknet", "."); // E.g ./weights/yolov4.weights
			String cfgFlag = configuration.getValue().getFilePath().replace("Darknet", "."); // E.g ./cfg/yolov4.cfg
			String imageFlag = "./data/camera.png";
			String thresValue =  thresholds.getValue();

			// Process builder
			ProcessBuilder processBuilder = new ProcessBuilder();
			processBuilder.directory(new File(ramDiskFolderPath + "Darknet")); // Important
			processBuilder.command(darkPath, "detector", "test", dataFlag, cfgFlag, weightsFlag, imageFlag, "-thresh", thresValue, "-dont_show"); // E.g ./darknet detector test ./cfg/coco.data ./cfg/yolov4-tiny.cfg ./weights/yolov4-.weights ./data/camera.png -thresh 0.4 -dont_show
			processBuilder.redirectErrorStream(true); // Important
			Process process = processBuilder.start();
			
			// Collect the predicted objects
			BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
			String line;
			boolean predictionsComesNow = false;
			ArrayList<String> predictedObjects = new ArrayList<String>();
			while ((line = reader.readLine()) != null) {
				// Collect objects if we have seen the "Predicted in" text
				if(predictionsComesNow && line.contains(":")) {
					predictedObjects.add(line.split(":")[0]); // Always split on ":"
				}
				
				// Read if we have seen this "Predicted in" text
				if(line.contains("Predicted in")) {
					predictionsComesNow = true;
				}
			}
			process.waitFor();  
			
			// Check the predicted object if we need to send a mail
			compareDetectedObjectsWithDatabaseObjects(predictedObjects);
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private void compareDetectedObjectsWithDatabaseObjects(ArrayList<String> predictedObjects) {
		List<YoloObjectEntity> databaseObjects = yoloObjectService.findAll();
		for(YoloObjectEntity yoloObject : databaseObjects) {
			for(String predicted : predictedObjects) {
				String objectName = yoloObject.getObjectName();
				if(predicted.contains(objectName)) {
					// Send a message if the object is active
					if(yoloObject.getActive() && yoloObject.getMessageHasBeenSent() == false) {
						Boolean messageHasBeenSent = sendMail.sendMailTo(yoloObject.getEmail(), yoloObject.getMessage());
						yoloObject.setMessageHasBeenSent(messageHasBeenSent);
						yoloObjectService.save(yoloObject);
					}
				}
			}
		}
	}

	private void saveStreamToImgFolder(BufferedImage cameraImage) {
		try {
			// Mirror it before we save it
			int width = cameraImage.getWidth();
			int height = cameraImage.getHeight();
			BufferedImage mirror = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
			for (int y = 0; y < height; y++) {
				for (int lx = 0, rx = width - 1; lx < width; lx++, rx--) {
					int p = cameraImage.getRGB(lx, y);
					mirror.setRGB(rx, y, p);
				}
			}
			ImageIO.write(mirror, "png", new File(ramDiskFolderPath + "Darknet/data/camera.png")); 
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void setComponentsToThread(Button startStopYOLO, AtomicBoolean startStopThread, UI ui, Select<ListUploadedFiles> darknet, Select<ListUploadedFiles> configuration, Select<ListUploadedFiles> data, Select<ListUploadedFiles> weights, Select<String> thresholds, YoloObjectService yoloObjectService, SendMail sendMail, Select<String> cameras, Webcam selectedWebcam, Image realTimeCameraImage, Select<Resolutions> pictureSize) {
		this.startStopYOLO = startStopYOLO;
		this.startStopThread = startStopThread;
		this.ui = ui;
		this.darknet = darknet;
		this.configuration = configuration;
		this.data = data;
		this.weights = weights;
		this.thresholds = thresholds;
		this.yoloObjectService = yoloObjectService;
		this.sendMail = sendMail;
		this.cameras = cameras;
		this.selectedWebcam = selectedWebcam;
		this.realTimeCameraImage = realTimeCameraImage;
		this.pictureSize = pictureSize;
	}
	
	private void enableDisableSelectAndButton() {
		if (startStopThread.get() == true) {
			try {
				selectedWebcam.open(); 
				startStopYOLO.setText(YoloView.STOP);
				startStopYOLO.setEnabled(true);
				startStopThread.set(true); // Start YOLO program here
				cameras.setEnabled(false);
				darknet.setEnabled(false);
				configuration.setEnabled(false);
				data.setEnabled(false);
				weights.setEnabled(false);
				thresholds.setEnabled(false);
				pictureSize.setEnabled(false);
				if(!hasDarknetFolderBeenDownloaded)
					copyDarknetFolderToRamdiskFolder();
					
			}catch(Exception e) {
				new Notification("Select another camera", 3000).open();
			}
			
		} else {
			selectedWebcam.close(); 
			startStopYOLO.setText(YoloView.START);
			startStopThread.set(false); // Stop YOLO program here
			cameras.setEnabled(true);
			darknet.setEnabled(true);
			configuration.setEnabled(true);
			data.setEnabled(true);
			weights.setEnabled(true);
			thresholds.setEnabled(true);
			pictureSize.setEnabled(true);
			if(hasDarknetFolderBeenDownloaded)
				deleteDarknetFolderInRamdisk();
		}
	}
	
	private void deleteDarknetFolderInRamdisk() {
		try {
			FileUtils.deleteDirectory(new File(ramDiskFolderPath + "Darknet"));
			hasDarknetFolderBeenDownloaded = false;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void copyDarknetFolderToRamdiskFolder(){
		try {
			FileUtils.copyDirectory(new File("Darknet"), new File(ramDiskFolderPath + "Darknet"));
			hasDarknetFolderBeenDownloaded = true;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
