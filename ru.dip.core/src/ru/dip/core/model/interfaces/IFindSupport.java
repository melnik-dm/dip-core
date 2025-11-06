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

import java.util.List;

import ru.dip.core.model.finder.IFinder;

public interface IFindSupport {

	/**
	 *  Найти
	 */
	IFinder find(String searchText, boolean caseSensitive, boolean findInId);

	
	/**
	 * Очистить результаты поиска
	 */
	void cleanFind();

	/**
	 * Выделить элемент
	 */
	void selectNext(IDipDocumentElement dipDocumentElement);

	
	/**
	 * Обновить элементы
	 */
	void updateFindedElements(List<IDipDocumentElement> findedElements);
	
	
	/**
	 *  Установить режим поиска
	 */
	void setFindMode(boolean value);


	/**
	 * Последний искомый текст
	 */
	String getLastSearchedText();
	
	void setLastSearchedText(String searchingText);
	
}
