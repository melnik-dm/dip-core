package ru.dip.text.spelling.utils.images;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

public class ImageProvider {
	
	public static final Image ADD_CORRECTION = new Image(Display.getCurrent(),
			ImageProvider.class.getResourceAsStream("add_correction.png"));

}
