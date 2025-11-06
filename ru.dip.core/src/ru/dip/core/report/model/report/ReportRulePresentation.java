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
package ru.dip.core.report.model.report;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.graphics.Point;

import ru.dip.core.form.model.Field;
import ru.dip.core.form.model.ItemsField;
import ru.dip.core.model.DipProject;
import ru.dip.core.report.model.condition.BooleanSign;
import ru.dip.core.report.model.condition.CloseBracket;
import ru.dip.core.report.model.condition.Condition;
import ru.dip.core.report.model.condition.ConditionPart;
import ru.dip.core.report.model.condition.FieldName;
import ru.dip.core.report.model.condition.OpenBracket;
import ru.dip.core.report.model.condition.Sign;
import ru.dip.core.report.model.condition.SimpleCondition;
import ru.dip.core.report.model.condition.Value;
import ru.dip.core.schema.Schema;

public class ReportRulePresentation {

	private Point fExtensionPoint;
	private List<Point> fBoundPoints = new ArrayList<>();
	private List<Point> fValues = new ArrayList<>();
	private List<Point> fFieldNames = new ArrayList<>();
	private List<Point> fSigns = new ArrayList<>();

	private ReportRule fRule;
	private DipProject fProject;
	private String fExtension;
	private Field fCurrentField;
	private StringBuilder fBuilder = new StringBuilder();
	
	public ReportRulePresentation(ReportRule rule, DipProject project) {
		fRule = rule;
		fExtension = rule.getExtension();
		fProject = project;
		createPresentation();
	}
	
	private void createPresentation() {
		addExtension();
		addBounds();
		addCondition();		
	}
	
	private void addExtension(){
		if (fExtension != null && !fExtension.isEmpty()){
			int start = fBuilder.length();
			fBuilder.append(fExtension);
			int end = fBuilder.length();
			fExtensionPoint = new Point(start, end);
			fBuilder.append("  ");
		}
	}
	
	private void addBounds(){	
		String bounds = fRule.getBounds();		
		if (bounds != null && !bounds.isEmpty()){
			fBuilder.append("[");
			String[] boundArray = bounds.split(",");
			for (int i = 0; i < boundArray.length; i++){
				String bound = boundArray[i];
				int start = fBuilder.length();
				fBuilder.append(bound);
				int end = fBuilder.length();
				fBoundPoints.add(new Point(start, end));
				if (i < boundArray.length - 1){
					fBuilder.append(",");
				}
			}
			fBuilder.append("]   ");						
		}
	}
	
	private void addCondition() {
		Condition condition = fRule.getCondition();
		for (ConditionPart part: condition.getParts()){
			if (part instanceof OpenBracket || part instanceof CloseBracket){
				addBracket(part);
			} else if (part instanceof Sign || part instanceof BooleanSign){
				addSign(part);
			} else if (part instanceof FieldName){
				addFieldName((FieldName) part);								
			} else if (part instanceof Value){
				addValue((Value) part);
			} else if (part instanceof SimpleCondition) {
				SimpleCondition simpleCondition = (SimpleCondition) part;
				addFieldName(simpleCondition.fieldName());
				addSign(simpleCondition.sign());
				addValue(simpleCondition.value());
			}
		}
	}
	
	private void addBracket(ConditionPart part){
		int start = fBuilder.length();
		fBuilder.append(part.toString());
		int end = fBuilder.length();
		fSigns.add(new Point(start, end));
	}
	
	private void addSign(ConditionPart part){
		fBuilder.append(" ");
		int start = fBuilder.length();
		fBuilder.append(part.toString());
		int end = fBuilder.length();				
		fSigns.add(new Point(start, end));
		fBuilder.append(" ");
	}
	
	private void addFieldName(FieldName fieldName){
		String title = getFieldTitle(fieldName);
		int start = fBuilder.length();
		fBuilder.append(title);
		int end = fBuilder.length();
		fFieldNames.add(new Point(start, end));		
	}
	
	
	private String getFieldTitle(FieldName name){
		String fieldName = ((FieldName) name).getValue();	
		if (fExtension == null || fExtension.isEmpty()){
			return fieldName;
		}		
		Schema schema = fProject.getSchemaModel().getSchema(fExtension);				
		if (schema == null){
			return fieldName;
		}
		schema.updateModel(fProject);
		fCurrentField = schema.getField(fieldName);
		if (fCurrentField == null){
			return fieldName;
		}
		return fCurrentField.getTitle();
	}
	
	private void addValue(Value value){
		String valueText = getValueText(value);
		int start = fBuilder.length();
		fBuilder.append("\"");
		fBuilder.append(valueText);
		fBuilder.append("\"");
		int end = fBuilder.length();
		fValues.add(new Point(start, end));				
	}
	
	private String getValueText(Value value){
		String valueText = value.getValue();
		if (fCurrentField == null){
			return valueText;
		}
		String text = getTextValue(value, fCurrentField);
		if (text == null){
			return valueText;
		}
		return text;
	}
	
	private String getTextValue(Value value, Field field){
		if (field == null){
			return null;
		}
		String text = value.getValue();
		Integer valueInt = value.getInteger();
		if (valueInt != null && valueInt >= 0){			
			if (field instanceof ItemsField){
				ItemsField itemsField = (ItemsField) field;
				if (itemsField.getItemValues().length > valueInt){
					return itemsField.getItemValues()[valueInt];
				}
			}						
		}		
		return text;
	}
	
	@Override
	public String toString() {	
		return fBuilder.toString();
	}
	
	public Point getExtensionPoint(){
		return fExtensionPoint;
	}

	public List<Point> getBoundPoints(){
		return fBoundPoints;
	}
	
	public List<Point> getSignPoints(){
		return fSigns;
	}
	
	public List<Point> getFieldNamePoints(){
		return fFieldNames;
	}
	
	public List<Point> getValuePoints(){
		return fValues;
	}
}


