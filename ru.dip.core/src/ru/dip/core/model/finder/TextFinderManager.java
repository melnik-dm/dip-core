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
package ru.dip.core.model.finder;

import java.util.List;

import org.eclipse.swt.graphics.Point;

import ru.dip.core.model.interfaces.IFindable;
import ru.dip.core.utilities.TagStringUtilities;

/**
 * Управление поиск и фильтром для текстовых презентаций
 */
public class TextFinderManager implements IFindable, IFindPoints {
	
	private final IContent fContent;
	protected IFindResult fFindResult;
	private Filter fFilter;
	
	public TextFinderManager(IContent content) {
		fContent = content;
	}
	
	/**
	 * Простой поиск, не содержит точек
	 */
	@Override
	public boolean contains(String text, FindSettings findSettigns) {
		boolean result = doContains(text, findSettigns.caseSensetive());
		if (result) {
			fFindResult = FindResult.simple(text, findSettigns.caseSensetive());
		}		
		return result;
	}
	
	private boolean doContains(String text, boolean caseSensitive) {
		String content = fContent.getContent();
		if (!caseSensitive) {
			return TagStringUtilities.containsToIgnoreCase(content, text); 
		} else {
			return content.contains(text);	
		}
	}
	
	@Override
	public boolean findWord(String text, boolean caseSensitive) {
		FindResult result = findText(text, caseSensitive, WORD);
		return result != null;
	}
	
	@Override
	public int findText(String text, FindSettings findSettigns) {
		fFindResult = findText(text, findSettigns.caseSensetive(), TEXT);
		return fFindResult == null ? 0 : fFindResult.size();
	}
	
	private FindResult findText(String text, boolean caseSensitive, boolean wholeWord) {
		String content = fContent.getContent();
		return FindResult.find(content, text, caseSensitive, wholeWord);
	}
		
	@Override
	public void updateFindedPoints(String content) {
		if (fFindResult != null) {
			fFindResult.updateFindPoints(content);
		}
		if (fFilter != null) {
			fFindResult = fFilter.findPoints(content);
		}
	}
	
	@Override
	public boolean appendFind(String text, boolean caseSensitive) {
		if (fFilter == null) {
			fFilter = new Filter();
		}
		fFilter.appendFind(text, caseSensitive);
		return false;
	}
	
	@Override
	public boolean appendWord(String text, boolean caseSensitive) {	
		if (fFilter == null) {
			fFilter = new Filter();
		}
		fFilter.appendWord(text, caseSensitive);
		return false;	
	}
	

	@Override
 	public void cleanFind() {
		fFindResult = null;
		fFilter = null;
	}
	
	@Override
	public boolean hasFindResult() {
		return fFilter != null || fFindResult != null;
	}	

	@Override
	public List<Point> getFindedPoints() {				
		return fFindResult == null ? null : fFindResult.getFindPoints();
	}
	
	public IFindResult getFindedResult() {
		return fFindResult;
	}

	public int size() {
		List<Point> points = getFindedPoints();
		return points == null ? 0 : points.size();
	}

}
