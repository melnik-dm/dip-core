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

public class FormSettings implements IFormSettings {
	
	private final boolean fFixedContent;
	private final IFormSettings fFormSettings;
	
	public FormSettings(IFormSettings formSettings, boolean fixedContent) {
		fFormSettings = formSettings;
		fFixedContent = fixedContent;
	}

	//=======================
	// getters
	
	@Override
	public boolean isShowNumeration() {
		return fFormSettings.isShowNumeration();
	}

	@Override
	public boolean isNewLineForTextbox() {
		return fFormSettings.isNewLineForTextbox();
	}

	@Override
	public boolean isBetweenLine() {
		return fFormSettings.isBetweenLine();
	}

	@Override
	public boolean isFormPrefEnable() {
		return fFormSettings.isFormPrefEnable();
	}

	@Override
	public boolean isWrapFields() {
		return fFormSettings.isWrapFields();
	}

	@Override
	public boolean isNotShowEmptyFields() {
		return fFormSettings.isNotShowEmptyFields();
	}
	
	/**
	 * В режиме FixedContent отображается дефолтное поле указанное в схеме при условии,
	 *  что настройки отображения полей (фильтры для  полей) отключены (formPrefEnable == false)
	 */
	public boolean isDefaultFixedContent() {
		return fFixedContent && !isFormPrefEnable();
	}

}
