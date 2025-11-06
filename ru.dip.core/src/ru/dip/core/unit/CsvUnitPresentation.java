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

import java.io.IOException;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Set;

import org.eclipse.swt.graphics.Image;

import ru.dip.core.DipCorePlugin;
import ru.dip.core.csv.model.CsvModel;
import ru.dip.core.model.finder.FindSettings;
import ru.dip.core.model.interfaces.IDipUnit;
import ru.dip.core.utilities.FileUtilities;
import ru.dip.core.utilities.TagStringUtilities;
import ru.dip.core.utilities.text.Terms;
import ru.dip.core.utilities.ui.swt.ImageUtilities;

public class CsvUnitPresentation extends ZoomImagePresentation {

	private String fText;
	private Image fImage;
	private CsvModel fCsvModel; 
	private int fOldWidth = -1;
	private int fDefaultWidth;
	
	public CsvUnitPresentation(IDipUnit unit) {
		super(unit);
	}

	@Override
	protected void read() {
		fCsvModel = readCsvModel();
		if (fCsvModel != null){
			fText = fCsvModel.getHtmlText();
			fText = TextPresentation.prepareText(fText, getUnit());
			try {
				fImage = ImageUtilities.createImageFromHtml(fText, getResource());
				fDefaultWidth = fImage.getBounds().width;
				disposeZoomImage();
				fOldWidth = 0;
			} catch (Exception e) {
				if (e instanceof NoSuchFileException) {
					DipCorePlugin.logError(e, "Read CSV error");
					e.printStackTrace();
					getUnit().updateWithProject();
					fText =  "Read CVS error";
				}				
				DipCorePlugin.logError(e, "Create html image error " + getUnit());
				e.printStackTrace();
				fText = "CVS";
			}
		} else {
			fText = "CVS";
		}		
	}
	
	private CsvModel readCsvModel(){
		Path path = Paths.get(getUnit().resource().getLocationURI());
		CsvModel csvModel = new CsvModel();
		try {
			csvModel.readModel(path);
		} catch (IOException e) {
			DipCorePlugin.logError(e, "Read csv model ERROR " + getUnit());
			e.printStackTrace();
			return null;
		}
		return csvModel;
	}
	
	@Override
	public String getText() {
		return fText;
	}

	@Override
	public Image getImage() {		
		return fImage;
	}
		
	public void trimImage(int width, boolean wrapContent) {
		if (getZoomImage() != null && fOldWidth == width){
			return;
		}
		fOldWidth = width;
		
		if (wrapContent && width > fDefaultWidth) {
			width = fDefaultWidth;
		}
		if (fCsvModel == null){
			fCsvModel = readCsvModel();
		}
		if (fCsvModel != null){			
			fText = fCsvModel.getHtmlText(width, wrapContent);		
			fText = TextPresentation.prepareText(fText, getUnit());
			try {
				Image image =ImageUtilities.createImageFromHtml(fText, getResource());
				super.trimImage(width, image);
			} catch (Exception e) {
				DipCorePlugin.logError(e, "Create html image error " + getUnit());
				e.printStackTrace();
			}
		}
	}
	
	public Image getExternalImage(int width, boolean wrapContent) {
		String text = fCsvModel.getHtmlText(width, wrapContent);
		fText = TextPresentation.prepareText(fText, getUnit());
		if (text != null) {
			try {
				Image image =  ImageUtilities.createImageFromHtml(text, getResource());
				if (image.getBounds().width > width + 20) {
					return ImageUtilities.trimImageByWidth(width, image);
				}
				
				return image;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return null;
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
	
	
	// попробовать поменять на приват
	public boolean find(String text, boolean caseSensitive, boolean word) {
		try {
			String filetext = FileUtilities.readFile(getUnit().resource());
			if (word) {
				return TagStringUtilities.containsWord(filetext, text, caseSensitive);
			}			
			if (!caseSensitive) {
				return TagStringUtilities.containsToIgnoreCase(filetext, text, word);
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
	public void cleanFind() {}
	

	//======================
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
	
	//======================
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
	
	//=====================================
	// getters
	
	public CsvModel csvModel() {
		return fCsvModel;
	}

}
