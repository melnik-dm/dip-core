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

import org.eclipse.core.resources.IFile;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

import ru.dip.core.model.interfaces.IDipUnit;
import ru.dip.core.model.interfaces.INotTextPresentation;


public class ImagePresentation extends ZoomImagePresentation implements INotTextPresentation {

	private Image fImage;
	private String fText;
	
	public ImagePresentation(IDipUnit unit) {
		super(unit);
	}
	
	@Override
	public void read(){
		IFile file = getUnit().resource();
		try {
			fImage = new Image(Display.getDefault(), file.getLocation().toOSString());
			disposeZoomImage();
			fText = createText();
		} catch (Exception exception) {
			fText = exception.getMessage();
		}

	}
	
	private String createText(){
		int numberString = fImage.getBounds().height / 18;
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < numberString; i++){		
			builder.append("\n");
		}
		return builder.toString();
	}
	
	@Override
	public String getText() {
		return fText;
	}

	@Override
	public Image getImage() {
		if (fImage == null) {
			read();
		}
		return fImage;
	}
}
