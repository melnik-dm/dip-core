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

import java.io.File;

import org.eclipse.core.resources.IFile;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

import ru.dip.core.exception.DiaConvertCommandException;
import ru.dip.core.model.interfaces.IDipUnit;
import ru.dip.core.model.interfaces.INotTextPresentation;
import ru.dip.core.utilities.DiaUtilities;

public class DiaPresentation extends ZoomImagePresentation implements INotTextPresentation  {
	
	public static final String LINE_SPLIT_REGEX = "\r\n|\n";

	public static final String DIA_NOT_INSTALLED = "Dia не установлен, "
			+ "либо не указан путь до исполняемого файла. \n "
			+ "Путь можно указать в настройках: Window - Preferences - DIP";

	private Image fImage;
	private String fText;
	private boolean fDiaReadError;

	public DiaPresentation(IDipUnit unit) {
		super(unit);
	}
	
	@Override
	protected void read() {
		fImage = null;
		fText = null;
		IFile file = getUnit().resource();
		String filename = file.getLocation().toOSString();
		File imageFile = null;;
		try {
			imageFile = DiaUtilities.toPng(filename, getUnit().id());
			fDiaReadError = false;
		} catch (DiaConvertCommandException e) {
			e.printStackTrace();
			fDiaReadError = true;
		}
		if (imageFile != null){
			fImage = new Image(Display.getDefault(), imageFile.getAbsolutePath());
			fText = createText();	
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
		return fImage;
	}
	
	public boolean isDiaReadError() {
		return fDiaReadError;
	}

	
}
