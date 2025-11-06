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

import java.util.List;

public class CoreFormModel {
	
	private List<Field> fFields;
	private CorePositionModel fCorePositionModel;
	private String fHint;
	private String fName;
	private String fExtension;
	private String fFixedField; 	// если указано это поле, то только оно отображается в fixed-режиме 
		
	public void createModel(List<Tag> tags){
		clearModel();
		for (Field field: fFields){
			String fieldName = field.getName();
			for (Tag tag: tags){
				String tagName = tag.getName();
				if (fieldName.equals(tagName)){
					field.setTag(tag);
					break;
				}
			}
		}
	}
	
	public void clearModel(){
		for (Field field: fFields){
			field.setTag(null);
		}
	}
	
	public Field findField(String name) {
		for (Field field: fFields){
			if(field.getName().equals(name)) {
				return field;
			}
		}
		return null;
	}
	
	public CorePositionModel getPositionModel(){
		return fCorePositionModel;
	}
	
	//===========================
	// getters
		
	public void setFields(List<Field> fields){
		fFields = fields;
	}
	
	public List<Field> getFields(){
		return fFields;
	}
	
	public void setPositionModel(CorePositionModel positionModel){
		fCorePositionModel = positionModel;
	}
	
	public String getHint(){
		return fHint;
	}
	
	public void setHint(String hint){
		fHint = hint;
	}

	public void setName(String name) {
		fName = name;
	}
	
	public String getName(){
		return fName;
	}
	
	public void setExtension(String extension){
		fExtension = extension;
	}
	
	public String getExtension(){
		return fExtension;
	}

	public void setFixedField(String fixedField) {
		fFixedField = fixedField;
	}
	
	public String getFixedField() {
		return fFixedField;
	}

}
