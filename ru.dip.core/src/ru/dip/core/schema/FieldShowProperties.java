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
package ru.dip.core.schema;

import ru.dip.core.form.model.Field;

public class FieldShowProperties {
	
	private final Field fField;
	private boolean fEnable;
	private boolean fShowTitle;
	
	public FieldShowProperties(Field field, boolean enable, boolean showTitle) {
		fField = field;
		fEnable = enable;
		fShowTitle = showTitle;
	}
	
	public Field field() {
		return fField;
	}

	public boolean isEnable() {
		return fEnable;
	}
	
	public void setEnable(boolean enable) {
		fEnable = enable;
	}
	
	public boolean isShowTitle() {
		return fShowTitle;
	}
	
	public void setShowTitle(boolean showTitle) {
		fShowTitle = showTitle;
	}
	
	@Override
	public String toString() {
		return fField.getTitle() + "   " + fEnable + "   "  + fShowTitle;
	}
	
}
