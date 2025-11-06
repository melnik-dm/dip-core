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
package ru.dip.core.unit.form;

/**
 *  Настройки отображения формы (из таблицы + глобальны)
 */
public interface IFormSettings {
	
	/**
	 * Отображать нумерацию форм (в заголовке формы)
	 */
	boolean isShowNumeration();
		
	/**
	 * Отображать текстовые поля с новой строки
	 */
	boolean isNewLineForTextbox();
	
	/**
	 * Вставлять пустые строки между полями
	 */
	boolean isBetweenLine();
	
	/**
	 * Включены ли настройки отображения форма (отображение отдельных полей)
	 */
	boolean isFormPrefEnable();
	
	/**
	 * Не переносить поля на отедльную строку (для combo, radio, check)
	 */
	boolean isWrapFields();
	
	/**
	 * Не отображать пустые поля
	 */
	boolean isNotShowEmptyFields();
}
