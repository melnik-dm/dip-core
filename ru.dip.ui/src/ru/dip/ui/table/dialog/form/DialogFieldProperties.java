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
package ru.dip.ui.table.dialog.form;

import ru.dip.core.schema.FieldShowProperties;

public class DialogFieldProperties {
	
	private FieldShowProperties fFieldProperties;
	private boolean fEnable;
	private boolean fShowTitle;

	public DialogFieldProperties(FieldShowProperties properties) {
		fFieldProperties = properties;
		fEnable = fFieldProperties.isEnable();
		fShowTitle = fFieldProperties.isShowTitle();
	}

	public void save() {
		fFieldProperties.setEnable(fEnable);
		fFieldProperties.setShowTitle(fShowTitle);
	}

	public String title() {
		return fFieldProperties.field().getTitle();
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

}
