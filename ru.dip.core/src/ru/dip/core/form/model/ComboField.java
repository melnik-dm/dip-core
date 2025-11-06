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

import java.util.ArrayList;

import ru.dip.core.utilities.ArrayUtils;

public class ComboField extends ItemsField {

	public ComboField(CoreFormModel dipModel, String name, String title, Item[] items) {
		super(dipModel, name, title, items);
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
	
	public String[] getComboItemValues(String itemName) {
		String[] itemValues = getItemValues();
		if (itemName.isEmpty()) {
			return itemValues;
		}
		Item item = getItem(itemName);				
		if (item == null || item.getSeq() == null || item.getSeq().length == 0) {
			return itemValues;
		}
		if (item.isIllegalSeq()) {
			return new String[] {itemName};
		}
		ArrayList<String> result = new ArrayList<>();	
		result.add(itemName);
		for (int number : item.getSeq()) {
			if (number < itemValues.length && number >= 0) {
				result.add(itemValues[number]);
			}
		}
		
		return result.stream().toArray(String[]::new);
	}
	
	@Override
	public FormFieldType getType() {
		return FormFieldType.COMBO;
	}
}
