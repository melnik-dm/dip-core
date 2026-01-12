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
package ru.dip.ui.table.ktable;

import ru.dip.core.unit.form.IFormSettings;
import ru.dip.ui.table.ktable.model.ITableFormSettings;
import ru.dip.ui.table.table.TableSettings;

public class TableFormSettings implements IFormSettings {
	
	private final ITableFormSettings fTableSettings;
	
	public TableFormSettings(ITableFormSettings tableSettings) {
		fTableSettings = tableSettings;
	}
	
	@Override
	public boolean isShowNumeration() {
		return fTableSettings.isShowFormNumeration();
	}
	
	@Override
	public boolean isFormPrefEnable() {
		return fTableSettings.isFormShowPreferenciesEnable();
	}

	@Override
	public boolean isNewLineForTextbox() {
		return TableSettings.isNewStrForTextbox();
	}

	@Override
	public boolean isBetweenLine() {
		return TableSettings.isLineBetweenEntries();
	}

	@Override
	public boolean isWrapFields() {
		return TableSettings.isWrapFields();
	}

	@Override
	public boolean isNotShowEmptyFields() {
		return TableSettings.isNotShowEmptyFields();
	}

}
