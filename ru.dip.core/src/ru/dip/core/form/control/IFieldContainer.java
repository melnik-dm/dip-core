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

import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import ru.dip.core.form.model.ComboField;
import ru.dip.core.form.model.ItemsField;

public interface IFieldContainer {

	public static IFieldContainer create(Composite parent, ItemsField field) {
		return create(parent, field, null);
	}

	public static IFieldContainer create(Composite parent, ItemsField field, Font font) {
		switch (field.getType()) {
		case COMBO: {
			return new ComboContainer(parent, (ComboField) field);
		}
		case RADIO:
		case CHECK: {
			return IFieldButtonContainer.create(parent, field, font);
		}
		default:
			throw new IllegalArgumentException(
					"Недопустимый тип поля: поддерживается только Combo, Radio и Check: " + field.getType());
		}
	}

	void setValue();

	boolean updateFieldValue();

	Control getControl();

	String getFieldName();

	String getValue();

}
