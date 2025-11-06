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

import ru.dip.core.model.interfaces.IDipUnit;

public class EmptyFormField extends FormField {

	public EmptyFormField(IDipUnit unit) {
		super(unit, null);
	}
	
	@Override
	public String getText() {
		return "EMPTY";
	}
	
}
