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

public abstract class ItemsField extends Field {

	private final Item[] fItems;
	private final String[] fItemNames;
	private Integer fLength;
	
	public ItemsField(CoreFormModel dipModel, String name, String title, Item[] items) {
		super(dipModel, name, title);
		fItems = items;
		fItemNames = setItemValues();
	}
	
	
	private String[] setItemValues(){
		String[] result = new String[fItems.length];
		for (int i = 0; i < result.length; i++){
			result[i] = fItems[i].getName();
		}		
		return result;
	}
	
	public Item[] getItems(){
		return fItems;
	}
	
	public String[] getItemValues(){
		return fItemNames;
	}
	
	public Item getItem(String value){
		for (int i = 0; i < fItemNames.length; i++){
			if (value.equals(fItemNames[i])){
				return fItems[i];
			}
		}
		return null;
	}
	
	public int getItemNumber(String value){
		for (int i = 0; i < fItemNames.length; i++){
			if (value.equals(fItemNames[i])){
				return i;
			}
		}
		return -1;
	}
	
	public int getIntValue(){
		String value = getValue();
		for (int i = 0;  i < fItemNames.length; i++){
			if (fItemNames[i].equals(value)){
				return i;
			}
		}
		return -1;
	}
	
	@Override
	public String getValue() {
		String value = super.getValue();
		if (value == null) {
			return null;
		}
		return value.trim();
	}
	
	public void setLength(Integer length){
		fLength = length;
	}
	
	public Integer getLength(){
		return fLength;
	}
	
}
