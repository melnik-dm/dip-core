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

import org.eclipse.swt.graphics.Color;

import ru.dip.core.utilities.ui.swt.ColorProvider;

public abstract class Field {
	
	private String fBackgroundColor;
	private String fForegroundColor;
	private String fName;
	private String fTitle;
	private String fDefaultValue;
	private String fHint;
	protected Tag fTag;
	protected CoreFormModel fFormModel;
	
	public Field(CoreFormModel formModel, String name, String title){
		fFormModel = formModel;
		fName = name;
		fTitle = title;
	}
	
	//========================
	// get & set value
	
	public abstract FormFieldType getType();
	
	public String getValue(){
		if (getTag() == null){
			return null;
		}
		String tagValue = getTag().getValue();
		return tagValue;
	}
	
	public void setValue(String newValue){
		String value = newValue;
		if (getTag() != null){
			getTag().setValue(value);
		} else {
			createTag(value);
		}
	}
	
	public void applayDefaultValue(){
		String newValue = computeDefaultValue();
		if (newValue != null && !newValue.isEmpty()){
			setValue(newValue);
		}
	}
	
	protected abstract String computeDefaultValue();
	
	//===========================
	// create tag

	private void createTag(String newValue){
		Tag previousTag = getPreviousTag();
		fFormModel.getPositionModel().createTagAfterTag(previousTag, getName(), newValue);
	}
	
	private Tag getPreviousTag(){
		int index = getFieldNumber();
		for (int i = index - 1; i >= 0; i--){
			Field field = fFormModel.getFields().get(i);
			if (field.getTag() != null){
				return field.getTag();
			}				
		}
		return null;
	}
	
	private int getFieldNumber(){
		return fFormModel.getFields().indexOf(this);
	}
	
	//===========================
	// getters & setters
	
	public String getName(){
		return fName;
	}
	
	public void setTag(Tag tag){
		fTag = tag;
	}
	
	public Tag getTag(){
		return fTag;
	}
	
	public String getDefaultValue(){
		return fDefaultValue;
	}
	
	public void setDefaultValue(String defaultValue){
		fDefaultValue = defaultValue;
	}
	
	public CoreFormModel getFormModel(){
		return fFormModel;
	}
	
	public String getTitle(){
		return fTitle;
	}
	
	public void setBackgroundColor(String color){
		fBackgroundColor = color;
	}
	
	public void setForegraund(String color){
		fForegroundColor  = color;
	}
	
	public Color getBackgroundColor(){
		if (fBackgroundColor != null){
			return ColorProvider.getColor(fBackgroundColor);
		}		
		return null;		
	}
	
	public Color getForegraundColor(){
		if (fForegroundColor != null){
			return ColorProvider.getColor(fForegroundColor);
		}		
		return null;				
	}
	
	public String getHint(){
		return fHint;
	}
	
	public void setHint(String hint){
		fHint = hint;
	}
}
