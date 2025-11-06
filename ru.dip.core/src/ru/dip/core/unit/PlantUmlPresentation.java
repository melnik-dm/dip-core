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
import java.io.IOException;
import java.util.Collection;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

import ru.dip.core.model.finder.FindSettings;
import ru.dip.core.model.interfaces.IDipUnit;
import ru.dip.core.utilities.FileUtilities;
import ru.dip.core.utilities.TagStringUtilities;
import ru.dip.core.utilities.UmlUtilities;
import ru.dip.core.utilities.text.Terms;

public class PlantUmlPresentation extends ZoomImagePresentation {

	private Image fImage;
	private String fText;
	
	public PlantUmlPresentation(IDipUnit unit) {
		super(unit);
	}
	
	@Override
	protected void read() {
		fImage = null;
		fText = null;
		IFile file = getUnit().resource();
		String filename = file.getLocation().toOSString();
		File imageFile = UmlUtilities.getImageFileFromUml(filename, getUnit().id());
		if (imageFile != null){
			fImage = new Image(Display.getDefault(), imageFile.getAbsolutePath());
			disposeZoomImage();
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
	
	//===================================
	// find
	
	@Override
	public boolean contains(String text, FindSettings findSettings) {
		return find(text, findSettings.caseSensetive(), TEXT);
	}
	
	@Override
	public boolean findWord(String text, boolean caseSensitive) {
		return find(text, caseSensitive, WORD);
	}
	
	@Override
	public int findText(String text, FindSettings findSettings) {
		if (contains(text, findSettings)) {
			return 1;
		}
		return 0;
	}
	
	public boolean find(String text, boolean caseSensitive, boolean word) {
		try {
			String filetext = FileUtilities.readFile(getUnit().resource());
			if (!caseSensitive) {
				return TagStringUtilities.containsToIgnoreCase(filetext, text, word);
			}
			if (word) {
				return TagStringUtilities.containsWord(filetext, text, caseSensitive);
			} else {			
				return (filetext.contains(text));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}		
		return false;
	}
	
	@Override
	public boolean appendFind(String text, boolean caseSensitive) {
		return false;
	}
	
	@Override
	public boolean appendWord(String text, boolean caseSensitive) {
		return false;
	}
	
	@Override
	public void cleanFind() {		
	}
	
	//===================================
	// glossary

	@Override
	public void removeIfFind(Collection<String> terms) {
		try {
			String filetext = FileUtilities.readFile(getUnit().resource());
			TagStringUtilities.removeIfContains(filetext, terms);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void findTerms(Set<String> terms) {
		try {
			String filetext = FileUtilities.readFile(getUnit().resource());
			Terms.findTerms(filetext, terms);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	//===================================
	// variables
	
	@Override
	public void findVars(Set<String> vars) {
		try {
			String filetext = FileUtilities.readFile(getUnit().resource());
			Terms.findVarsNames(filetext, vars);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
