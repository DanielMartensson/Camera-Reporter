package se.martensson.component;

import java.io.File;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

@Component
public class InitialStartUp implements ApplicationListener<ContextRefreshedEvent> {

	@Override
	public void onApplicationEvent(ContextRefreshedEvent event) {
		createDarknetFolders();
	}

	private void createDarknetFolders() {
		String[] folderPaths = new String[] {"Darknet/cfg", "Darknet/data", "Darknet/weights", "Darknet/data/labels"};
		for(String path : folderPaths) {
			new File(path).mkdirs(); // If not exist
		}
	}
}