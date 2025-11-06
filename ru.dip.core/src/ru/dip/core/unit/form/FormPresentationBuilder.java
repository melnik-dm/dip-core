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

import org.eclipse.swt.graphics.Point;

import ru.dip.core.form.model.Field;
import ru.dip.core.form.model.ItemsField;
import ru.dip.core.form.model.TextField;
import ru.dip.core.schema.FieldShowProperties;
import ru.dip.core.schema.FormShowProperties;
import ru.dip.core.unit.md.MarkdownSettings;
import ru.dip.core.unit.md.MdFormatPoints;
import ru.dip.core.utilities.TagStringUtilities;
import ru.dip.core.utilities.md.parser.MdPresentationParser;

public class FormPresentationBuilder {
	
	private StringBuilder fBuilder = new StringBuilder();
	
	private final IFormFields fFormFields;
	private final IFormSettings fFormSettings;
	private final FormShowProperties fFormProperties;
	private final MdFormatPoints fMdFormatPoints;
	private final int fLineLength;
	private final MarkdownSettings fMdSettings;
	
	public FormPresentationBuilder(IFormFields formFields,			
			MdFormatPoints mdFormatPoints, 
			IFormSettings formSettings,
			FormShowProperties formProperties,
			MarkdownSettings mdSettings, 
			int lineLength) {
		fFormFields = formFields;
		fMdFormatPoints = mdFormatPoints;
		fFormSettings = formSettings;
		fFormProperties = formProperties;
		fMdSettings = mdSettings;
		fLineLength = lineLength;
	}
	
	public void build() {
		build(true);
	}
	
	/**
	 * @param 
	 * showFormTitle - добавить ли к полю заголовок формы (используется если поле первое)
	 */
	public void build(boolean showFormTitle){
		if (showFormTitle && fFormSettings.isShowNumeration()) {
			addTitle();
		}
		addFields();
	}

	void addTitle() {
		String formTitle = fFormFields.getFormTitleString();	
		fBuilder.append(formTitle);
		fBuilder.append("\n");
		if (fFormSettings.isBetweenLine()) {
			fBuilder.append("\n");
		}			
		fFormFields.setFormTitlePoint(formTitle.length());
	}
	
	void addFields() {
		Field previous = null;		
		for (FormField formField : fFormFields.getFormFields()) {					
			boolean result = addField(formField, previous);
			if (result) {
				previous = formField.getField();
			}
		}
	}
	
	private boolean addField(FormField formField, Field previous) {
		Field field = formField.getField();
		if (field == null) {
			return false;
		}
		
		FieldShowProperties fieldSettings = null;
		if (fFormSettings.isNotShowEmptyFields()) {
			String fieldValue = getFieldValue(field);		
			if (fieldValue == null || fieldValue.isEmpty()) {
				return false;
			}
		}
		
		if (fFormSettings.isFormPrefEnable()) {
			fieldSettings = fFormProperties.findFieldProperties(field.getName());
			if (fieldSettings != null && !fieldSettings.isEnable()) {
				return false;
			}
			if (fieldSettings.isShowTitle()) {
				addSeparator(previous, field);
				addFieldTitle(field);
			} else {
				String fieldValue = getFieldValue(field);
				if (fieldValue == null || fieldValue.isEmpty()) {
					return false;
				}
				addSeparator(previous, field);
			}
		} else {
			addSeparator(previous, field);
			addFieldTitle(field);
		}
			
		appendFieldValue(field);		
		return true;
	}
	
	private void addSeparator(Field previous, Field field) {
		if (previous == null) {
			return;
		}
		if (fFormSettings.isWrapFields() && !(field instanceof TextField) && !(previous instanceof TextField)) {
			fBuilder.append(" ");
		} else {
			fBuilder.append(TagStringUtilities.lineSeparator());
			if (fFormSettings.isBetweenLine()) {
				fBuilder.append(TagStringUtilities.lineSeparator());
			}
		}
	}
	
	private void addFieldTitle(Field field) {
		int start = fBuilder.length();
		String title = field.getTitle();
		fBuilder.append(title);
		if (field instanceof ItemsField) {
			fBuilder.append(": ");  	
		}
		if (field instanceof TextField && fFormSettings.isNewLineForTextbox() 
				&& field.getValue() != null && !field.getValue().isEmpty()){
			fBuilder.append("\n");
		} 
		int end = fBuilder.length();
		fFormFields.getFieldTitlePoints().add(new Point(start, end));
	}

	private void appendFieldValue(Field field) {
		if (field instanceof TextField) {
			String fieldValue = ((TextField)field).getPreparingValue(fFormFields.getDipUnit());
			MdPresentationParser parser = new MdPresentationParser(fFormFields.getDipUnit());						
			fieldValue = parser.parse(fMdSettings, fLineLength, fieldValue);
			if (fieldValue != null) {										
				parser.addOffset(fBuilder.length());				
				fMdFormatPoints.boldPoints().addAll(parser.boldPoints());
				fMdFormatPoints.italicPoints().addAll(parser.italicPoints());
				fMdFormatPoints.boldItalicPoints().addAll(parser.boldItalicPoints());
				fMdFormatPoints.codePoints().addAll(parser.codePoints());
				fMdFormatPoints.fencedCodePoints().addAll(parser.fencedCodePoints());
				fMdFormatPoints.commentPoints().addAll(parser.commentPoints());		
				fBuilder.append(fieldValue);
			}
		} else {
			String fieldValue = getFieldValue(field);		
			if (fieldValue != null){
				fBuilder.append(fieldValue);
			}	
		}
	}
	
	private String getFieldValue(Field field) {
		String fieldValue = null;
		if (field instanceof TextField) {			
			fieldValue = ((TextField)field).getPreparingValue(fFormFields.getDipUnit());
			//fieldValue = VariableInteractor.changeVar(fieldValue, fFormFields.getDipUnit());	
		} else {
			fieldValue = field.getValue();
		}
		return fieldValue;
	}
	
	public void append(String text) {
		fBuilder.append(text);
	}
	
	public void build(Field field) {
		if (fFormSettings.isShowNumeration()) {
			addTitle();
		}
		addFieldTitle(field);
		String fieldValue = getFieldValue(field);		
		if (fieldValue != null){
			fBuilder.append(fieldValue);
		}
	}
				
	@Override
	public String toString() {
		return fBuilder.toString().trim();
	}
}

