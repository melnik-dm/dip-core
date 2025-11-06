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
package ru.dip.core.model.interfaces;

import ru.dip.core.model.finder.FindSettings;

public interface IFindable {
	
	public static final boolean WORD = true;
	public static final boolean TEXT = false; 
	
	
	default boolean contains(String text, FindSettings findSettings) {
		return findText(text, findSettings) > 0;
	}
	
	boolean findWord(String text, boolean caseSensitive);
	
	/**
	 * Возвращает количество найденных позиций
	 * 
	 * Переименовать, мб в findResult
	 */
	int findText(String text, FindSettings findSettings);
	
	/**
	 * Не сбрасывает результаты предыдущего поиска. Используетя в цепочке фильтров
	 */
	boolean appendFind(String text, boolean caseSensitive);
	
	boolean appendWord(String text, boolean caseSensitive);

	void cleanFind();

}
