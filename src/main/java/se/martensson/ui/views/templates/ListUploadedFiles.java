package se.martensson.ui.views.templates;

import java.io.File;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class ListUploadedFiles {
	
	private String filePath;
	
	@SuppressWarnings("unused")
	private File file;
	
}
