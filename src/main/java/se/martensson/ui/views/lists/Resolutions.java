package se.martensson.ui.views.lists;

import java.awt.Dimension;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class Resolutions {
	
	private Double width;
	private Double height;
	private Dimension dimension;
	
	@Override
	public String toString() {
		return width + "x" + height;
	}

}
