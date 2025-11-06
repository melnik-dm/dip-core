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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;

import ru.dip.core.form.FormReader;
import ru.dip.core.form.model.Field;
import ru.dip.core.model.finder.FindSettings;
import ru.dip.core.model.finder.FormFinderManager;
import ru.dip.core.model.finder.IFindResult;
import ru.dip.core.model.interfaces.IFindable;
import ru.dip.core.model.interfaces.IGlossaryPoints;
import ru.dip.core.model.interfaces.IDipUnit;
import ru.dip.core.model.interfaces.ITextPresentation;
import ru.dip.core.schema.FieldShowProperties;
import ru.dip.core.schema.FormShowProperties;
import ru.dip.core.schema.Schema;
import ru.dip.core.unit.GlossaryPoints;
import ru.dip.core.unit.TablePresentation;
import ru.dip.core.unit.TextPresentation;
import ru.dip.core.unit.md.MarkdownSettings;
import ru.dip.core.unit.md.MdFormatPoints;

public class FormPresentation extends TablePresentation implements ITextPresentation, IFindable, IFormFields {

	private FormReader fReader;
	private List<FormField> fFormFields;
	private Point fTitleBoldPoint;   // для жирного шрифта заголовка
	private List<Point> fTitles = new ArrayList<>();
	private MdFormatPoints fMdFormatPoints = new MdFormatPoints();	
	private FormShowProperties fFormShowSettings;
	private final IGlossaryPoints fGlossaryPoints;
	private final FormFinderManager fFinderManager;

	
	public FormPresentation(IDipUnit unit) {
		super(unit);
		fFormShowSettings = getUnit().dipProject().getSchemaModel().findFormSettings(fReader.getShemaName());
		fGlossaryPoints = new GlossaryPoints(unit.dipProject());
		fFinderManager = new FormFinderManager(this);
	}

	@Override
	public void read() {
		fReader = new FormReader(getUnit().resource());
		fReader.read();
		List<Field> fields = fReader.getFields();
		fFormFields = fields.stream().map(f -> new FormField(getUnit(), f)).collect(Collectors.toList());
		if (fFormFields.isEmpty()) {
			fFormFields.add(new EmptyFormField(getUnit()));
		}
	}
	
	/**
	 * Обновляет данные для полей из файла (вызывается при обновлении файла)
	 */
	public void updateFieldsFromFile() {
		FormReader reader = new FormReader(getUnit().resource());
		reader.read();
		for (Field field: reader.getFields()) {
			Field oldField = fReader.getFieldByName(field.getName());
			oldField.setValue(field.getValue());
		}
	}
	
	public FormField getFormFieldByName(String name) {
		for (FormField formField: fFormFields) {
			if (formField.getField().getName().equals(name)) {
				return formField;
			}
		}
		return null;
	}
	
	
	public String tablePresentation(FormSettings formSettings,
			MarkdownSettings mdSettings, int lineLength) {
		return tablePresentation(formSettings, getShowProperties(), mdSettings, lineLength);
	}

	public String tablePresentation(FormSettings formSettings,
			FormShowProperties formProperties, MarkdownSettings mdSettings, int lineLength) {
		clearTitlePoints();
		FormPresentationBuilder builder = new FormPresentationBuilder(this , fMdFormatPoints, 
				formSettings, formProperties,  mdSettings, lineLength);
		
		if (formSettings.isDefaultFixedContent()) {
			String fixedField = fReader.fixedField();			
			if (fixedField != null && !fixedField.isEmpty()) {
				return fixedFieldPresentation(fixedField, builder);
			}
		}
		builder.build();
		return builder.toString();
	}
	
	private FormShowProperties getShowProperties() {
		return getUnit().dipProject().getSchemaModel().findFormSettings(schemaName());
	}
	
	private String fixedFieldPresentation(String fixedField, FormPresentationBuilder builder) {
		FormField formField = null;
		for (FormField f: fFormFields) {
			if (fixedField.equals(f.getField().getName())) {
				formField = f;
				break;
			}
		}
		if (formField != null) {					
			Field field = formField.getField();			
			builder.build(field);
		} 
		return builder.toString();
	}
	
	
	private void clearTitlePoints(){
		fTitles.clear();
		fMdFormatPoints.clear();
	}
	
	public Field getField(String name){
		for (Field field: fReader.getFields()){
			if (field.getName().equals(name)){
				return field;
			}
		}
		return null;
	}
	
	@Override
	public String getFormTitleString() {
		StringBuilder builder = new StringBuilder();
		builder.append(schemaName());
		builder.append(" ");
		builder.append(getUnit().getNumer());		
		return builder.toString();
	}
		
	/**
	 * Будет ли видимо поле с учетом данных настроек
	 */
	public boolean isVisible(IFormSettings formSetting, Field field) {
		FieldShowProperties fieldSettings = extractFieldShowProperties(formSetting, field);		
		return isVisible(formSetting, fieldSettings, field);
	}
	
	private FieldShowProperties extractFieldShowProperties(IFormSettings formSetting, Field field) {
		if (formSetting instanceof IFormExtendedSettings) {
			return ((IFormExtendedSettings)formSetting).getFieldProperties(field.getName());
		} else {
			return getFormShowProperties().findFieldProperties(field.getName());
		}
	}
	
	public boolean isVisible(IFormSettings formSetting, FieldShowProperties fieldSettings, Field field) {
		if (formSetting.isNotShowEmptyFields()) {
			String fieldValue = field.getValue();
			if (fieldValue == null || fieldValue.isEmpty()) {
				return false;
			}
		}
		if (formSetting.isFormPrefEnable()) {
			if (fieldSettings != null && !fieldSettings.isEnable()) {
				return false;
			}
			if (!fieldSettings.isShowTitle()) {
				String fieldValue = field.getValue();		
				if (fieldValue == null || fieldValue.isEmpty()) {
					return false;
				}
			}
		} 		
		return true;
	}
	
	/**
	 *  Есть ли хоть одно видимое поле
	 */
	public boolean hasVisibleField(IFormSettings formSetting) {
		return getFields().stream().anyMatch(field -> isVisible(formSetting, field));
	}
	
	/**
	 * Первое видимое поле
	 */
	public int firstVisibleField(IFormSettings formSetting) {
		
		for (int i = 0; i < getFields().size(); i++) {
			if (isVisible(formSetting, getFields().get(i))){
				return i;
			}
		}		
		return -1;
	}
	
	//========================
	// find
	
	@Override
	public String getContent() {
		return fFormFields.stream().map(FormField::getField).map(Field::getValue)
				.collect(Collectors.joining(" "));
	}
	
	public String getContent(IFormSettings formSettings) {				
		return fFormFields.stream()
				.map(FormField::getField)
				.filter(f ->  isVisible(formSettings, f))
				.map(Field::getValue)
				.map(s -> TextPresentation.prepareText(s, getDipUnit()))
				.collect(Collectors.joining(" "));
	}
	
	
	@Override
 	public boolean contains(String text, FindSettings findSettings) {
		return fFinderManager.contains(text, findSettings);
	}
	
	@Override
	public boolean findWord(String text, boolean caseSensitive) {
		return fFinderManager.findWord(text, caseSensitive);
	}

	@Override
	public int findText(String text, FindSettings findSettings) {
		return fFinderManager.findText(text, findSettings);
	}
	
	public int findText(String text, FindSettings findSettings, Object settings) {
		return fFinderManager.findText(text, findSettings, settings);
	}

	public void updateFindedPoints(String content) {
		// нужно отсечь точки из заголовков полей, 
		// поэтому сравниваем с первоначальным поиском, который был только по содержимому 
		int oldPointNumber = fFinderManager.size();
		fFinderManager.updateFindedPoints(content);		
		List<Point> points = fFinderManager.getFindedPoints();
		if (points != null && points.size() > oldPointNumber) {
			for (int i = 0; i < points.size() - oldPointNumber; i++) {
				points.remove(0);
			}					
		}
	}
	
	@Override
	public boolean appendFind(String text, boolean caseSensitive) {
		return fFinderManager.appendFind(text, caseSensitive);
	}

	@Override
	public boolean appendWord(String text, boolean caseSensitive) {
		return fFinderManager.appendWord(text, caseSensitive);
	}

	@Override
	public void cleanFind() {
		fFinderManager.cleanFind();
	}

	@Override
	public boolean hasFindResult() {
		return fFinderManager.hasFindResult();
	}

	@Override
	public List<Point> getFindedPoints() {
		return fFinderManager.getFindedPoints();
	}
	
	public IFindResult getFindResult() {
		return fFinderManager.getFindedResult();
	}

	//=================================
	// Glossary Support
	
	@Override
	public void removeIfFind(Collection<String> terms) {
		for (FormField field: fFormFields) {
			field.removeIfFind(terms);
		}	
	}
	
	@Override
	public void findTerms(Set<String> terms) {
		for (FormField field: fFormFields) {
			field.findTerms(terms);
		}			
	}
	
	//=================================
	// Variables Support
	
	@Override
	public void findVars(Set<String> vars) {
		for (FormField field: fFormFields) {
			field.findVars(vars);
		}			
	}
	
	//=================================
	// getters
	
	public List<Point> codePoints(){
		return fMdFormatPoints.codePoints();
	}
	
	public List<Point> fencedCodePoints(){
		return fMdFormatPoints.fencedCodePoints();
	}
	
	public List<Point> commentPoints(){
		return fMdFormatPoints.commentPoints();
	}
	
	public List<Point> boldPoints(){
		return fMdFormatPoints.boldPoints();
	}
	
	public List<Point> italicPoints(){
		return fMdFormatPoints.italicPoints();
	}
	
	public List<Point> boldItalicPoints(){
		return fMdFormatPoints.boldItalicPoints();
	}

	@Override
	public IGlossaryPoints getGlossaryPoints() {
		return fGlossaryPoints;
	}

	public FormReader getFormReader() {
		return fReader;
	}
	
	public FormShowProperties getFormShowProperties() {
		return fFormShowSettings;
	}
	
	@Override
	public List<FormField> getFormFields(){
		return fFormFields;
	}

	@Override
	public IDipUnit getDipUnit() {
		return getUnit();
	}

	@Override
	public FormShowProperties formShowProperties() {
		return fFormShowSettings;
	}
	
	@Override
	public String getText() {	
		return null;
	}
			
	@Override
	public Image getImage() {
		return null;
	}
	
	@Override
	public List<Point> getFieldTitlePoints(){
		return fTitles;
	}
	
	public List<Field> getFields(){
		return fReader.getFields();
	}
	
	public Schema getSchema() {
		return getUnit().dipProject().getSchemaModel().getSchemaByName(schemaName());
	}
	
	public String schemaName() {
		return fReader.getShemaName();
	}
	
	@Override
	public void setFormTitlePoint(int length) {
		fTitleBoldPoint = new Point(0, length);
	}
	
	public Point titleBoldPoint() {
		return fTitleBoldPoint;
	}

}
