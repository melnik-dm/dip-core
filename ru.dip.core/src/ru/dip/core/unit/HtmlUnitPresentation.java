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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;

import ru.dip.core.DipCorePlugin;
import ru.dip.core.model.finder.FindSettings;
import ru.dip.core.model.finder.IFindResult;
import ru.dip.core.model.interfaces.IDipUnit;
import ru.dip.core.utilities.FileUtilities;
import ru.dip.core.utilities.HtmlUtilities;
import ru.dip.core.utilities.TagStringUtilities;
import ru.dip.core.utilities.text.Terms;
import ru.dip.core.utilities.ui.image.ImageUtilities;
import ru.dip.core.utilities.ui.swt.ColorProvider;

public class HtmlUnitPresentation extends ZoomImagePresentation {

	private static final String BACKGROUND_PREFIX  = "<span style=\"background-color: ";
	private static final String BACKGROUND_POSTFIX  = "</span>";
	private static final int RGB_TEXT_LENGTH = 8;
	
	private String fText;
	private int fOldWidth = -1;
	private String fOldText;	
	
	private List<Point> fFindedPoints = new ArrayList<>();
	private int fCurrentFindedPoint = -1;

	public HtmlUnitPresentation(IDipUnit unit) {
		super(unit);
	}

	@Override
	protected void read() {
		try {
			if (fOldText == null) {
				fText = FileUtilities.readFile(getUnit().resource());			
			}
			//prepare text
			fText = TextPresentation.prepareText(fText, getUnit());
			disposeZoomImage();
			fOldWidth = 0;
		} catch (Exception e) {
			DipCorePlugin.logError(e, "Create .table image error " + getUnit());
			e.printStackTrace();
			fText = "Create .table image error " + e.getMessage();
		}
	}
	
	@Override
	public String getText() {
		return fText;
	}

	@Override
	public Image getImage() {
		return null;
	}
		
	@Override
	public void trimImage(int width) {
		fOldWidth = width;
		if (getZoomImage() != null && fOldWidth == width){
			return;
		}	
		try {
			if (fText == null) {
				fText = FileUtilities.readFile(getUnit().resource());
				fText = TextPresentation.prepareText(fText, getUnit());
			}			
			String newText = getHtmlWithWidth(fText, width);
			if (newText != null) {
				fText = newText;
				Image image = ImageUtilities.createImageFromHtml(fText, getUnit().resource());
				super.trimImage(width, image);
			}
		} catch (Exception e) {
			DipCorePlugin.logError(e, "Create html image error " + getUnit());
			e.printStackTrace();
		}
	}
	
	protected String getHtmlWithWidth(String text, int width) {
		return HtmlUtilities.setHtmlWidth(text, width);
	}
	
	public Image getExternalImage(int width) {
		String text = getHtmlWithWidth(getText(), width);
		if (text != null) {
			try {
				Image image = ImageUtilities.createImageFromHtml(text, getResource());
				if (image.getBounds().width > width) {
					return ImageUtilities.trimImageByWidth(width, image);
				}	
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
		if (fText == null) {
			return false;
		}
		fOldText = fText;
		fFindedPoints = find(text, findSettings.caseSensetive());
		
		boolean findResult = !fFindedPoints.isEmpty();
		if (findResult) {
			disposeZoomImage();
		}
		return findResult;
	}
	
	@Override
	public boolean findWord(String text, boolean caseSensitive) {
		if (fText == null) {
			return false;
		}
		fOldText = fText;
		boolean findResult = findNotCaseSensetiveWord(text, caseSensitive);	
		if (findResult) {
			disposeZoomImage();
		}		
		return findResult;	
	}
	
	@Override
	public int findText(String text, FindSettings findSettings) {
		if (contains(text,findSettings)) {
			return fFindedPoints.size();
		} else {
			return 0;
		}
	}
	
	@Override
	public boolean appendFind(String text, boolean caseSensitive) {
		if (fOldText == null) {
			fOldText = fText;
		}
		fFindedPoints = find(text, caseSensitive);		
		boolean findResult = !fFindedPoints.isEmpty();		
		if (findResult) {
			disposeZoomImage();
		}
		return findResult;
	}
	
	@Override
	public boolean appendWord(String text, boolean caseSensitive) {
		if (fOldText == null) {
			fOldText = fText;
		}		
		boolean findResult = findNotCaseSensetiveWord(text, caseSensitive);	
		if (findResult) {
			disposeZoomImage();
		}	
		return findResult;
	}
	
	/**
	 * Возвращает список точек для содержимого тегов ">[^<>]*<";
	 */
	private List<Point> getTagPoints(String original){
		String regex = ">[^<>]*<";
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(original);
		ArrayList<Point> points = new ArrayList<>();
		while(matcher.find()){
			points.add(new Point(matcher.start(), matcher.end()));
		}
		return points;
	}
		
	private String buildColorText(String original, Color color) {
		StringBuilder builder = new StringBuilder();
		builder.append(BACKGROUND_PREFIX);
		builder.append(ColorProvider.colorToHtmlString(color));
		builder.append("\">");
		builder.append(original);
		builder.append(BACKGROUND_POSTFIX);		
		return builder.toString();
	}
	
	
	public void setNewCurrentPointNumber(int currentPoint) {
		if (fFindedPoints.isEmpty()) {
			return;
		}
		
		if (fCurrentFindedPoint == currentPoint) {
			return;
		}
		StringBuilder builder = new StringBuilder(fText);	
		if (fCurrentFindedPoint >= 0) {
			Point p = fFindedPoints.get(fCurrentFindedPoint);
			builder.replace(p.x, p.x + RGB_TEXT_LENGTH, ColorProvider.colorToHtmlString(IFindResult.CURRENT_POINT));			
		}
		fCurrentFindedPoint = currentPoint;
		if (fCurrentFindedPoint >= 0) {
			Point p = fFindedPoints.get(fCurrentFindedPoint);
			builder.replace(p.x, p.x + RGB_TEXT_LENGTH, ColorProvider.colorToHtmlString(IFindResult.FIND_COLOR));			
		}
		fText = builder.toString();
		disposeZoomImage();		
	}

	
	private List<Point> find(String findText, boolean caseSensetive) {
		List<Point> result = new ArrayList<>();
		List<Point> points = getTagPoints(fText);
		StringBuilder builder = new StringBuilder(fText);
		for (int i = points.size() - 1; i >= 0; i--) {
			Point p = points.get(i);
			String line = fText.substring(p.x, p.y);
			String originalLine = line;
			if (!caseSensetive) {
				line = line.toLowerCase();
				findText = findText.toLowerCase();
			}
			int findTextLength = findText.length();
			int lastIndex = line.length() - 1;
			int findIndex = -1;
			while ((findIndex = line.lastIndexOf(findText, lastIndex)) > -1) {
				String originalText = originalLine.substring(findIndex, findIndex + findTextLength);
				String pasteText = buildColorText(originalText, IFindResult.CURRENT_POINT);
				builder.delete(p.x + findIndex, p.x + findIndex + findTextLength);
				builder.insert(p.x + findIndex, pasteText);
				
				//сдвигаем предыдущие точки, т.к. текст изменился
				result.forEach(point -> point.x += (pasteText.length() - findTextLength));
				
				result.add(0, new Point(p.x + findIndex + BACKGROUND_PREFIX.length(), 
						RGB_TEXT_LENGTH));
				lastIndex = findIndex - 1;
			}
		}
		if (!result.isEmpty()) {
			fText = builder.toString();
		}	
		return result;
	}
	
	/**
	 * For append 
	 */
	private boolean findNotCaseSensetiveWord(String findText, boolean caseSensetive) {
		boolean findResult = false;
		List<Point> points = getTagPoints(fText);
		StringBuilder builder = new StringBuilder(fText);
		for (int i = points.size() - 1; i >= 0; i--) {
			Point tagPoint = points.get(i);
			String line = fText.substring(tagPoint.x, tagPoint.y);
			List<Point> findedPoints = TagStringUtilities.findWords(line, findText, caseSensetive);
			if (findedPoints == null) {
				continue;
			}
			for (int pointIndex = findedPoints.size() - 1; pointIndex >= 0; pointIndex--) {
				Point wordPoint = findedPoints.get(pointIndex);
				String originalText = line.substring(wordPoint.x, wordPoint.y);
				String pasteText = buildColorText(originalText, IFindResult.CURRENT_POINT);
				builder.delete(tagPoint.x + wordPoint.x, tagPoint.x + wordPoint.y);
				builder.insert(tagPoint.x + wordPoint.x, pasteText);
				findResult = true;
			}
		}
		if (findResult) {
			fText = builder.toString();
		}	
		return findResult;
	}
	
	@Override
	public void cleanFind() {
		fText = fOldText;
		fOldText = null;
		disposeZoomImage();
	}
	
	//==========================
	// IGlossary
	
	@Override
	public void removeIfFind(Collection<String> terms) {
		if (fText != null) {
			TagStringUtilities.removeIfContains(fText, terms);
		}
	}
	
	@Override
	public void findTerms(Set<String> terms) {
		if (fText != null) {
			Terms.findTerms(fText, terms);
		}		
	}

	//==========================
	// Variables
	
	@Override
	public void findVars(Set<String> vars) {
		if (fText != null) {
			Terms.findVarsNames(fText, vars);
		}		
	}

}
