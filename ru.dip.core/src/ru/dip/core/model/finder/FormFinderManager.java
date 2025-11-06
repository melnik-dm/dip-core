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

import ru.dip.core.unit.form.FormPresentation;
import ru.dip.core.unit.form.IFormSettings;

/**
 * Управление поиском и фильтром для Форм 
 */
public class FormFinderManager extends TextFinderManager {
	
	private final FormPresentation fFormPresentation;
	
	public FormFinderManager(FormPresentation formPresentation) {
		super(formPresentation);
		fFormPresentation = formPresentation;
	}
	
	public int findText(String text, FindSettings findSettings, Object settings) {
		if (settings instanceof IFormSettings) {
			String content = fFormPresentation.getContent((IFormSettings) settings);			
			fFindResult =  FindResult.find(content, text, findSettings.caseSensetive(), TEXT);
			return fFindResult == null ? 0 : fFindResult.size();
		}
		return findText(text, findSettings);
	}
	
}
