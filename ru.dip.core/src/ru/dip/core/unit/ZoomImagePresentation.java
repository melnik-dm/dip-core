/******************************************************************************* * 
 * Copyright (c) 2025 Denis Melnik.
 * Copyright (c) 2025 Ruslan Sabirov.
 * Copyright (c) 2025 Andrei Motorin.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 *******************************************************************************/
package ru.dip.core.unit;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

import ru.dip.core.model.interfaces.IDipUnit;
import ru.dip.core.utilities.ui.image.ImageUtilities;

public abstract class ZoomImagePresentation extends TablePresentation {

	private static final int MIN_ZOOM_WIDTH = 200;
	
	private Image fZoomImage;
	
	public ZoomImagePresentation(IDipUnit unit) {
		super(unit);
	}

	/**
	 * Уменьшаем размер изображения, в соответствии с указанной шириной
	 */
	public void trimImage(int width){		
		if (fZoomImage != null){
			if (fZoomImage.getBounds().width == width){
				return;
			}	
		}		
		Image image = getImage();
		if (image == null) {
			disposeZoomImage();	
			return;
		}
				
		int imageWidth = image.getBounds().width;
		if (imageWidth > width && width > MIN_ZOOM_WIDTH){
			double k = (double) width / imageWidth;
			int height = (int) (image.getBounds().height * k);
			fZoomImage = ImageUtilities.getResizedImage(image, width, height);
		} else {
			// если оригинальное изображение меньше
			if (fZoomImage != null){
				if (fZoomImage.getBounds().width == imageWidth){
					return;
				}	
			}	
			fZoomImage = new Image(Display.getCurrent(), image, SWT.IMAGE_COPY);		
		}
	}
	
	public Image getZoomImage(){
		if (fZoomImage != null){		
			return fZoomImage;
		}
		return getImage();
	}
	
	public Image getZoomImage(int width) {
		if (fZoomImage == null){
			trimImage(width);
		}		
		return fZoomImage;
	}
	
	
	public void trimImage(int width, Image newImage){
		if (fZoomImage != null){
			if (fZoomImage.getBounds().width == width){
				return;
			}	
		}		
		disposeZoomImage();	
		fZoomImage = newImage;
	}
	
	public void disposeZoomImage(){
		if (fZoomImage != null){
			fZoomImage.dispose();
			fZoomImage = null;
		}
	}
	
	@Override
	protected void dispose() {
		disposeZoomImage();
		super.dispose();
	}
	
}
