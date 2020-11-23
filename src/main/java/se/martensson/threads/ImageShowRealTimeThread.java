package se.martensson.threads;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
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
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.imageio.ImageIO;

import com.github.sarxos.webcam.Webcam;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.server.StreamResource;

import lombok.Setter;
import se.martensson.component.SendMail;
import se.martensson.entity.YoloObjectEntity;
import se.martensson.service.YoloObjectService;
import se.martensson.ui.views.templates.ListUploadedFiles;

public class ImageShowRealTimeThread extends Thread {

	private UI ui;
	private AtomicBoolean startStopThread;
	@Setter
	private Webcam selectedWebcam;
	@Setter
	private Image realTimeCameraImage;
	private Select<ListUploadedFiles> darknet;
	private Select<ListUploadedFiles> configuration;
	private Select<ListUploadedFiles> weights;
	private Select<String> thresholds;
	private YoloObjectService yoloObjectService;
	private SendMail sendMail;

	public ImageShowRealTimeThread(AtomicBoolean startStopThread, UI ui, Select<ListUploadedFiles> darknet, Select<ListUploadedFiles> configuration, Select<ListUploadedFiles> weights, Select<String> thresholds, YoloObjectService yoloObjectService, SendMail sendMail) {
		this.startStopThread = startStopThread;
		this.ui = ui;
		this.darknet = darknet;
		this.configuration = configuration;
		this.weights = weights;
		this.thresholds = thresholds;
		this.yoloObjectService = yoloObjectService;
		this.sendMail = sendMail;
	}

	@Override
	public void run() {
		while (true) {
			while (startStopThread.get()) {
				sleepThread();

				// Try to take a snap short and convert it to bytes and then to a stream
				// resource
				try {
					BufferedImage cameraImage = selectedWebcam.getImage();
					ByteArrayOutputStream byteImage = new ByteArrayOutputStream();
					ImageIO.write(cameraImage, "png", byteImage);
					byte[] streamBytes = byteImage.toByteArray();
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
					ui.access(() -> realTimeCameraImage.setSrc(resource));
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			sleepThread();
		}
	}

	private void yoloDetection(byte[] streamBytes) {
		saveStreamToImgFolder(streamBytes);
		callDarkNetToDoItsPrediction();

	}

	private void callDarkNetToDoItsPrediction() {
		try {
			// Arguments
			String darkPath = darknet.getValue().getFilePath().replace("Darknet/", "./");
			String configurationFlag = configuration.getValue().getFilePath().replace("Darknet/", "");
			String weightsFlag = weights.getValue().getFilePath().replace("Darknet/", "");
			String imageFlag = "data/camera.png";
			String thresValue =  thresholds.getValue();

			// Process builder
			ProcessBuilder processBuilder = new ProcessBuilder();
			processBuilder.directory(new File("Darknet")); // Important
			processBuilder.command(darkPath, "detect", configurationFlag, weightsFlag, imageFlag, "-thresh", thresValue);
			processBuilder.redirectErrorStream(true); // Important
			Process process = processBuilder.start();
			BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
			String line;
			boolean predictionsComesNow = false;
			ArrayList<String> predictedObjects = new ArrayList<String>();
			while ((line = reader.readLine()) != null) {
				// Collect objects if we have seen the "Predicted in" text
				if(predictionsComesNow) {
					predictedObjects.add(line.split(":")[0]); // Always split on ":"
				}
				
				// Read if we have seen this "Predicted in" text
				if(line.contains("Predicted in")) {
					predictionsComesNow = true;
				}
			}
			process.waitFor();  
			
			// Read the objects and compare
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

	/**
	 * It's important to have a sleep time in thread, else the thread can freeze
	 */
	private void sleepThread() {
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
