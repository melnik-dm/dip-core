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

import ru.dip.core.model.interfaces.IDipUnit;
import ru.dip.core.unit.TextPresentation;

public class TextField extends Field {
	
	private Integer fHeight;
	private Integer fWidth;
	
	public TextField(CoreFormModel formModel, String name, String title) {
		super(formModel, name, title);
	}
	
	@Override
	public String getValue() {
		String value = super.getValue();
		if (value != null && value.trim().isEmpty()) {
			return value.trim();
		}		
		if (value != null && !value.isEmpty()) {		
			value = value.replaceAll("&lt;", "<")
				.replaceAll("&gt;", ">")
				.replaceAll("&amp;", "&");
				
		}		
		return value;
	}
	
	public String getPreparingValue(IDipUnit unit) {
		String value = getValue();	
		if (value == null) {
			return null;
		}
		return TextPresentation.prepareText(value, unit);
	}
	
	public void setHeight(int height){
		fHeight = height;
	}
	
	public Integer getHeight(){
		return fHeight;
	}

	public void setWidth(int width){
		fWidth = width;
	}
	
	
	public Integer getWidth() {
		return fWidth;
	}

	@Override
	protected String computeDefaultValue() {
		return getDefaultValue();
	}
	
	@Override
	public FormFieldType getType() {
		return FormFieldType.TEXT;
	}
	
}
