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
package ru.dip.core.form.control;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

import ru.dip.core.form.model.ItemsField;

public class RadioButtonContainer extends CheckButtonContainer {

	RadioButtonContainer(Composite parent, ItemsField checkField, Font font) {
		super(parent, checkField, font);
	}

	// ================================
	// create controls

	@Override
	protected Button createButton(int number) {
		return createButton(number, SWT.RADIO);
	}

	@Override
	public String computeFieldValue() {
		for (Button button : fButtons) {
			if (button.getSelection()) {
				return button.getText();
			}
		}
		return "";		
	}

}
