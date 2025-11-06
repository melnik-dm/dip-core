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
package ru.dip.ui.variable;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VarValidator {
	
	/**
	 * Проверка имени переменной
	 * Допускаются: буквы, цифры, знак подчеркивания
	 */
	public static boolean isValidName(String name) {
		if (name.isEmpty()){
			return false;
		}
		
		String regex = "^\\w+$";
		Pattern pattern = Pattern.compile(regex, Pattern.UNICODE_CHARACTER_CLASS);
		Matcher matcher = pattern.matcher(name);
		if (!matcher.find()) {
			return false;
		}
		return true;
	}
	
	/**
	 * Не допускаются пустые строки
	 */
	public static boolean isValidValue(String value) {
		if (value.isEmpty()){
			return false;
		}
		
		if (value.contains("\n\n")) {
			return false;
		}
		return true;
	}

}
