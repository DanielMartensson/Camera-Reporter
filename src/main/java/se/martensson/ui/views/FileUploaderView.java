package se.martensson.ui.views;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

import org.apache.commons.io.IOUtils;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasComponents;
import com.vaadin.flow.component.HtmlComponent;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.component.upload.receivers.MultiFileMemoryBuffer;
import com.vaadin.flow.router.Route;

import se.martensson.ui.views.templates.BarForAppLayout;
import se.martensson.ui.views.templates.ListUploadedFiles;

/**
 * The main view contains a button and a click listener.
 */
@Route("fileuploader")
@JsModule("./styles/shared-styles.js")
@CssImport("./styles/views/main/main-view.css")
public class FileUploaderView extends AppLayout {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public static final String CFG = "cfg";

	public static final String WEIGHTS = "weights";
	
	public static final String DATA = "data";
	
	private static TextArea terminal = new TextArea("Terminal"); // We want to remember the text

	public FileUploaderView() {
		// Banner and tabs
		BarForAppLayout barForApplayput = new BarForAppLayout();
		addToNavbar(barForApplayput.getDrawerToggle(), barForApplayput.getImg());
		addToDrawer(barForApplayput.getTabs());
		
		// Status terminal
		terminal.setWidthFull();
		terminal.setHeightFull();
		
		// Here we can select if we want to delete a file or not
		Button deleteFileButton = new Button("Delete file");
		Select<ListUploadedFiles> uploadedFile = new Select<>();
		uploadedFile.setTextRenderer(ListUploadedFiles::getFilePath);
		Dialog dialog = new Dialog();
		deleteFileButton.addClickListener(e -> {
			scanFolderFiles(uploadedFile);
			dialog.open();
		});
		Button confirmButton = new Button("Yes", event -> {
			if(uploadedFile.getValue() != null) {
				uploadedFile.getValue().getFile().delete();
				writeToTerminal("File deletet from: " + uploadedFile.getValue().getFilePath());
			}
			dialog.close();
		});
		Button cancelButton = new Button("No", event -> {
			dialog.close();
		});
		Text deleteFileText = new Text("Delete file: ");
		VerticalLayout deleteFileTextLayout = new VerticalLayout(deleteFileText);
		deleteFileTextLayout.setAlignItems(Alignment.CENTER);
		dialog.add(new FormLayout(deleteFileTextLayout, uploadedFile, confirmButton, cancelButton));
		
		// Uploader for Darknet file
		MemoryBuffer loadDarknetFileBuffer = new MemoryBuffer();
		Upload loadDarknetFileButton = new Upload(loadDarknetFileBuffer);
		loadDarknetFileButton.setMaxFiles(1);
		loadDarknetFileButton.setDropLabel(new Label("Load Darknet file"));
		loadDarknetFileButton.addFileRejectedListener(event -> {
		    writeToTerminal(event.getErrorMessage());
		});
		loadDarknetFileButton.addSucceededListener(event -> {
		    saveSingleFile(event.getMIMEType(), event.getFileName(), loadDarknetFileBuffer);
		});
		
		// Uploader for Weights file
		MultiFileMemoryBuffer loadWeightsBuffer = new MultiFileMemoryBuffer();
		Upload loadWeightsButton = new Upload(loadWeightsBuffer);
		loadWeightsButton.setDropLabel(new Label("Load files to weights folder"));
		loadWeightsButton.addFileRejectedListener(event -> {
			writeToTerminal(event.getErrorMessage());
		});
		loadWeightsButton.addSucceededListener(event -> {
			saveMultiFile(event.getMIMEType(), event.getFileName(), loadWeightsBuffer, WEIGHTS);
		});
		
		// Uploader for Configuration file
		MultiFileMemoryBuffer configurationBuffer = new MultiFileMemoryBuffer();
		Upload loadConfigurationButton = new Upload(configurationBuffer);
		loadConfigurationButton.setDropLabel(new Label("Load files to configuration folder"));
		loadConfigurationButton.addFileRejectedListener(event -> {
			writeToTerminal(event.getErrorMessage());
		});
		loadConfigurationButton.addSucceededListener(event -> {
			saveMultiFile(event.getMIMEType(), event.getFileName(), configurationBuffer, CFG);
		});
		
		// Uploader for Data file
		MultiFileMemoryBuffer dataBuffer = new MultiFileMemoryBuffer();
		Upload loadDataButton = new Upload(dataBuffer);
		loadDataButton.setDropLabel(new Label("Load files to data folder"));
		loadDataButton.addFileRejectedListener(event -> {
			writeToTerminal(event.getErrorMessage());
		});
		loadDataButton.addSucceededListener(event -> {
			saveMultiFile(event.getMIMEType(), event.getFileName(), dataBuffer, DATA);
		});
		
		// Layout
		VerticalLayout layout = new VerticalLayout();
		FormLayout buttonForm = new FormLayout(loadDarknetFileButton, loadWeightsButton, loadConfigurationButton, loadDataButton, deleteFileButton);
		layout.add(buttonForm);
		layout.add(terminal);
		setContent(layout);
		
	}
	
	private void scanFolderFiles(Select<ListUploadedFiles> uploadedFile) {
		// Get all the current files
		File[] configurationFiles = new File("Darknet/" + CFG).listFiles((File pathname) -> pathname.isFile());
		File[] yoloFiles = new File("Darknet/").listFiles((File pathname) -> pathname.isFile());
		File[] weightsFiles = new File("Darknet/" + WEIGHTS).listFiles((File pathname) -> pathname.isFile());
		File[] dataFiles = new File("Darknet/" + DATA).listFiles((File pathname) -> pathname.isFile());
		
		// Insert them
		uploadedFile.clear();
		ArrayList<ListUploadedFiles> listOfUploadedFiles = new ArrayList<>();
		for(File configurationFile : configurationFiles)
			listOfUploadedFiles.add(new ListUploadedFiles(configurationFile.getPath(), configurationFile));
		for(File yoloFile : yoloFiles)
			listOfUploadedFiles.add(new ListUploadedFiles(yoloFile.getPath(), yoloFile));
		for(File weightsFile : weightsFiles)
			listOfUploadedFiles.add(new ListUploadedFiles(weightsFile.getPath(), weightsFile));
		for(File dataFile : dataFiles)
			listOfUploadedFiles.add(new ListUploadedFiles(dataFile.getPath(), dataFile));
		
		uploadedFile.setItems(listOfUploadedFiles);
	}

	private void saveMultiFile(String mimeType, String fileName, MultiFileMemoryBuffer loadWeightsBuffer, String fileType) {
		try {
			// Save
			String filePath = "Darknet/" + fileType + "/" + fileName;
			Path path = Paths.get(filePath); // If not exist
			Files.createDirectories(path.getParent());
			FileOutputStream fos = new FileOutputStream(filePath);
		    fos.write(loadWeightsBuffer.getInputStream(fileName).readAllBytes());
		    fos.close();
		    
		    // Make it runnable
		    makeAllRunable();
		    writeToTerminal("Uploaded new file at: " + filePath);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void makeAllRunable() {
		try {
			ProcessBuilder processBuilder = new ProcessBuilder();
		    processBuilder.directory(new File("Darknet"));
		    processBuilder.command("bash", "-c", "chmod 777 -R *");
		    processBuilder.redirectErrorStream(true);
		    Process process = processBuilder.start();
		    process.waitFor();
		} catch (IOException | InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void saveSingleFile(String mimeType, String fileName, MemoryBuffer loadDarknetFileBuffer) {
		try {
			String filePath = "Darknet/" + fileName;
			Path path = Paths.get(filePath); // If not exist
			Files.createDirectories(path.getParent());
			FileOutputStream fos = new FileOutputStream(filePath);
		    fos.write(loadDarknetFileBuffer.getInputStream().readAllBytes());
		    fos.close();
		    makeAllRunable();
		    writeToTerminal("Uploaded new file at: " + filePath);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void writeToTerminal(String newLine) {
		String currentText = terminal.getValue();
		String[] currentLines = currentText.split("\n");
		if(currentLines.length > 30) {
			// Remove the first line and add the last line
			String newText = "";
			for(int i = 1; i < currentLines.length; i++) 
				newText += currentLines[i];
			newText += newLine;
			terminal.setValue(newText);
		}else {
			// Just add
			String newText = currentText + "\n" + newLine;
			terminal.setValue(newText);
		}	
	}
}