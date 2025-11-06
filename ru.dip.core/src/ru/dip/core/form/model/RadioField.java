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
package ru.dip.core.form.model;

import ru.dip.core.utilities.ArrayUtils;

public class RadioField extends ItemsField {
	
	public RadioField(CoreFormModel formModel, String name, String title, Item[] items) {
		super(formModel, name, title, items);
	}

	@Override
	protected String computeDefaultValue() {
		String defaultValue = getDefaultValue();
		if (ArrayUtils.arrayContainsElement(getItemValues(), defaultValue)){
			return defaultValue;
		}
		try {
			Integer number = Integer.parseInt(defaultValue);
			if (number >= 0 && number < getItemValues().length){
				return getItemValues()[number];
			}
		} catch (NumberFormatException e){
			// IGNORE
		}		
		return null;
	}
	
	@Override
	public FormFieldType getType() {
		return FormFieldType.RADIO;
	}
	
}
