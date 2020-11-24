package se.martensson.threads;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.imageio.ImageIO;

import com.github.sarxos.webcam.Webcam;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.server.StreamResource;

import lombok.Setter;
import se.martensson.component.SendMail;
import se.martensson.entity.YoloObjectEntity;
import se.martensson.service.YoloObjectService;
import se.martensson.ui.views.YoloView;
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

	public ImageShowRealTimeThread() {
		
	}

	@Override
	public void run() {
		while (true) {
			while (startStopThread.get()) {
				/*
				 * 1. Take a snap with camera
				 * 2. Classify it
				 * 3. Get the predicted objects
				 * 4. Check the database if we need to send a mail
				 */
				try {
					// Snap
					BufferedImage cameraImage = selectedWebcam.getImage();
					ByteArrayOutputStream byteImage = new ByteArrayOutputStream();
					ImageIO.write(cameraImage, "png", byteImage);
					byte[] streamBytes = byteImage.toByteArray();
					// Classify
					yoloDetection(streamBytes);
					StreamResource resource = new StreamResource("predictions.jpg", () -> {
						try {
							return new FileInputStream(new File("Darknet/predictions.jpg"));
						} catch (FileNotFoundException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						return null;
					});
					ui.access(() -> {
						realTimeCameraImage.setSrc(resource);
						enableDisableSelectAndButton();
					});
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	private void yoloDetection(byte[] streamBytes) {
		saveStreamToImgFolder(streamBytes);
		callDarkNetToDoItsPrediction();

	}

	private void callDarkNetToDoItsPrediction() {
		try {
			// Arguments
			String darkPath = darknet.getValue().getFilePath().replace("Darknet", "."); // E.g ./darknet
			String dataFlag = data.getValue().getFilePath().replace("Darknet", "."); // E.g ./cfg/coco.data
			String weightsFlag = weights.getValue().getFilePath().replace("Darknet", "."); // E.g ./weights/yolov4.weights
			String cfgFlag = configuration.getValue().getFilePath().replace("Darknet", "."); // E.g ./cfg/yolov4.cfg
			String imageFlag = "data/camera.png";
			String thresValue =  thresholds.getValue();

			// Process builder
			ProcessBuilder processBuilder = new ProcessBuilder();
			processBuilder.directory(new File("Darknet")); // Important
			processBuilder.command(darkPath, "detector", "test", dataFlag, cfgFlag, weightsFlag, imageFlag, "-thresh", thresValue); // E.g ./darknet detector test ./cfg/coco.data ./cfg/yolov4.cfg ./weights/yolov4.weights ./data/dog.jpg -thresh 0.4
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
			compareObjects(predictedObjects);
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private void compareObjects(ArrayList<String> predictedObjects) {
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

	private void saveStreamToImgFolder(byte[] streamBytes) {
		try {
			// Save image
			String picturePath = "Darknet/data/camera.png";
			Path path = Paths.get(picturePath); // If not exist
			Files.createDirectories(path.getParent());
			FileOutputStream fos = new FileOutputStream(picturePath);
			fos.write(streamBytes);
			fos.close();

			// Mirror it
			BufferedImage image = ImageIO.read(new File(picturePath));
			int width = image.getWidth();
			int height = image.getHeight();
			BufferedImage mirror = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
			for (int y = 0; y < height; y++) {
				for (int lx = 0, rx = width - 1; lx < width; lx++, rx--) {
					int p = image.getRGB(lx, y);
					mirror.setRGB(rx, y, p);
				}
			}
			ImageIO.write(mirror, "png", new File(picturePath)); 
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void setComponentsToThread(Button startStopYOLO, AtomicBoolean startStopThread, UI ui, Select<ListUploadedFiles> darknet, Select<ListUploadedFiles> configuration, Select<ListUploadedFiles> data, Select<ListUploadedFiles> weights, Select<String> thresholds, YoloObjectService yoloObjectService, SendMail sendMail, Select<String> cameras, Webcam selectedWebcam, Image realTimeCameraImage) {
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
	}
	
	private void enableDisableSelectAndButton() {
		if (startStopThread.get() == true) {
			startStopYOLO.setText(YoloView.STOP);
			startStopYOLO.setEnabled(true);
			startStopThread.set(true); // Start YOLO program here
			cameras.setEnabled(false);
			darknet.setEnabled(false);
			configuration.setEnabled(false);
			data.setEnabled(false);
			weights.setEnabled(false);
			thresholds.setEnabled(false);
		} else {
			startStopYOLO.setText(YoloView.START);
			startStopThread.set(false); // Stop YOLO program here
			cameras.setEnabled(true);
			darknet.setEnabled(true);
			configuration.setEnabled(true);
			data.setEnabled(true);
			weights.setEnabled(true);
			thresholds.setEnabled(true);
		}
	}

}
