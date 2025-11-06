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

import java.util.function.Consumer;

import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

import ru.dip.core.form.model.FormFieldType;
import ru.dip.core.form.model.ItemsField;
import ru.dip.core.utilities.ui.WSelectionListener;

public interface IFieldButtonContainer extends IFieldContainer {
	
	public static IFieldButtonContainer create(Composite parent, 
			ItemsField field) {
		return create(parent, field, null);
	}
	
	public static IFieldButtonContainer create(Composite parent, 
			ItemsField field, Font font) {
		if (field.getType() == FormFieldType.RADIO) {
			return new RadioButtonContainer(parent, field, font);
		} else if (field.getType() == FormFieldType.CHECK) {
			return new CheckButtonContainer(parent, field, font);
		}
		throw new IllegalArgumentException("Недопустимый тип поля: поддерживается только Radio и Check: " + field.getType());
	}
	
	void createCheckButtons();

	String computeFieldValue();

	void addSelectionListener(WSelectionListener listener);

	void applyToButtons(Consumer<Button> consumer);
	
	boolean isFocus();
	
}
