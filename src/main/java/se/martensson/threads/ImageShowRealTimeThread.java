package se.martensson.threads;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.imageio.ImageIO;

import com.github.sarxos.webcam.Webcam;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.server.StreamResource;

import lombok.Setter;


public class ImageShowRealTimeThread extends Thread{
	
	private UI ui;
	private AtomicBoolean startStopThread;
	@Setter
	private Webcam selectedWebcam;
	@Setter
	private Image realTimeCameraImage;

	public ImageShowRealTimeThread(AtomicBoolean startStopThread, UI ui) {
		this.startStopThread = startStopThread;
		this.ui = ui;
	}

	@Override
	public void run() {
		while(true) {
			while(startStopThread.get()) {
				sleepThread();
	
				// Try to take a snap short and convert it to bytes and then to a stream resource  
				try {
					BufferedImage cameraImage = selectedWebcam.getImage();
					ByteArrayOutputStream byteImage = new ByteArrayOutputStream();
					ImageIO.write(cameraImage, "png", byteImage);
					StreamResource resource = new StreamResource("camera.png", () -> new ByteArrayInputStream(byteImage.toByteArray()));
					ui.access(() -> realTimeCameraImage.setSrc(resource));
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			sleepThread();
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
