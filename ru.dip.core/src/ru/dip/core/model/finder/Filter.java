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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.swt.graphics.Point;

import ru.dip.core.utilities.TagStringUtilities;

public class Filter {
	
	private final Set<String> fFilterText = new HashSet<>();
	private final Set<String> fFilterCaseText = new HashSet<>();
	private final Set<String> fFilterWord = new HashSet<>();
	private final Set<String> fFilterCaseWord = new HashSet<>();

	/**
	 * Добавляет текст в фильтр
	 */
	public void appendFind(String text, boolean caseSensitive) {
		if (caseSensitive) {
			fFilterCaseText.add(text);
		} else {
			fFilterText.add(text);
		}
	}
	
	/**
	 * Добавляет слово в фильтр
	 */
	public void appendWord(String text, boolean caseSensitive) {	
		if (caseSensitive) {
			fFilterCaseWord.add(text);
		} else {
			fFilterWord.add(text);
		}
	}

	/**
	 * Ищет в тексте совпадения по данному фильтру
	 */
	public IFindResult findPoints(String content) {
		List<Point> points = new ArrayList<>();
		for (String str : fFilterText) {
			List<Point> findedPoints = TagStringUtilities.findText(content, str, false);
			if (findedPoints != null) {
				points.addAll(findedPoints);
			}
		}
		for (String str : fFilterCaseText) {
			List<Point> findedPoints = TagStringUtilities.findText(content, str, true);
			if (findedPoints != null) {
				points.addAll(findedPoints);
			}		}
		for (String str : fFilterWord) {
			List<Point> findedPoints = TagStringUtilities.findWords(content, str, false);
			if (findedPoints != null) {
				points.addAll(findedPoints);
			}		}
		for (String str : fFilterCaseWord) {
			List<Point> findedPoints = TagStringUtilities.findWords(content, str, true);
			if (findedPoints != null) {
				points.addAll(findedPoints);
			}
		}
		
		return points.isEmpty() ? null : new FilterResult(points);
	}
	
}
