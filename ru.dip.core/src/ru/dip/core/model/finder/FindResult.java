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

import ru.dip.core.utilities.TagStringUtilities;

public class FindResult implements IFindResult {

	public static FindResult simple(String findText, boolean caseSensitive) {
		return new FindResult(null, findText, caseSensitive);
	}
	
	public static FindResult find(String content, 
			String text, boolean caseSensitive, boolean wholeWord) {
		if (wholeWord) {
			return findWord(content, text, caseSensitive);
		} else {
			return findText(content, text, caseSensitive);
		}
	}
	
	public static FindResult findText(String content, 
			String text, boolean caseSensitive) {
		List<Point> points = TagStringUtilities.findText(content, text, caseSensitive);
		return points == null ? null 
				: new FindResult(points, text, caseSensitive);
	}
	
	public static FindResult findWord(String content, 
			String text, boolean caseSensitive) {
		List<Point> points = TagStringUtilities.findWords(content, text, caseSensitive);
		return points == null ? null 
				: new FindResult(points, text, caseSensitive);
	}
	
	// параметры поиска
	private final String fFindText;
	private final boolean fCaseSensitive;
	// результаты поиска
	private List<Point> fFindedPoints = null;	
	// SimpleFind = true, значит не учтены особенности форматирования, добавление переносов и т.п.

	private FindResult(List<Point> findedPoints, String findText, boolean caseSensitive) {
		fFindedPoints = findedPoints;
		fFindText = findText;
		fCaseSensitive = caseSensitive;
	}
	
	/**
	 * Обновление результата (если изменился текст, 
	 * например при форматировании, добавление отступов т.п.)
	 * 
	 * Только для поиска текста 
	 * (для поиска слов целиком нужно сделать отдельный метод при необходимости)
	 */
	public void updateFindPoints(String newContent) {
		fFindedPoints =  TagStringUtilities.findText(newContent, fFindText, fCaseSensitive);
	}
	
	@Override
	public List<Point> getFindPoints() {
		return fFindedPoints;
	}
	
	@Override
	public int size() {
		return fFindedPoints.size();
	}

}
