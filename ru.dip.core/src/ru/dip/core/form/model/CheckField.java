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
import java.util.List;

import ru.dip.core.utilities.ArrayUtils;

public class CheckField extends ItemsField {

	public CheckField(CoreFormModel formModel, String name, String title, Item[] items) {
		super(formModel, name, title, items);
	}
	
	public List<Integer> getValuesNumber(){
		List<Integer> result = new ArrayList<>();
		String value = getValue();
		if (value == null){
			return result;
		}
		
		String[] checks = value.split(",");
		for (int i = 0; i < getItemValues().length; i++){
			if (ArrayUtils.arrayContainsElement(checks, getItemValues()[i])){
				result.add(i);
			}			
		}
		return result;
	}
	
	@Override
	protected String computeDefaultValue() {		
		String defaultValue = getDefaultValue();
		String[] values = defaultValue.split(",");
		StringBuilder result = new StringBuilder();
		for (String value: values){
			String itemValue = checkItemValue(value);
			if (itemValue != null){
				result.append(itemValue);
				result.append(",");
			}
		}
		if (result.length() > 0){
			result.deleteCharAt(result.length() - 1);
			return result.toString();			
		}
		return null;
	}
	
	private String checkItemValue(String defaultItemValue){
		if (ArrayUtils.arrayContainsElement(getItemValues(), defaultItemValue)){
			return defaultItemValue;
		}
		try {
			Integer number = Integer.parseInt(defaultItemValue);
			if (number >= 0 && number < getItemValues().length){
				return getItemValues()[number];
			}
		} catch (NumberFormatException e){
			// IGNORE
		}		
		return null;
	}
	
	public boolean checkedValue(int number){
		return getValuesNumber().contains(number);
	}

	@Override
	public FormFieldType getType() {
		return FormFieldType.CHECK;
	}
	
}
