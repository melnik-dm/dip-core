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
package ru.dip.core.report.model.condition;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.jgit.lib.Repository;

import ru.dip.core.model.interfaces.ISchemaContainer;
import ru.dip.core.report.model.condition.FieldName.FieldType;

public class Condition {

	private List<ConditionPart> fParts = new ArrayList<>();
		
	public void addPart(ConditionPart part){
		fParts.add(part);
	}
	
	//============================
	// упростить выражение
	
	public void simplify() {
		fParts = computeSimpleConditions();
	}
	
	/**
	 * FieldName, Sign и Value - преабразуем в SimpleCondition 
	 */
	private ArrayList<ConditionPart> computeSimpleConditions(){
		ArrayList<ConditionPart> result = new ArrayList<>();
		for (int i = 0; i < fParts.size(); i++){
			ConditionPart part = fParts.get(i);
			if (part instanceof OpenBracket || part instanceof CloseBracket){
				result.add(part);
			} else if (part instanceof FieldName){
				if (isNextSignValue(i)){
					Sign sign = (Sign) fParts.get(i + 1);
					Value value = (Value) fParts.get(i + 2);
					SimpleCondition simpleCondition = new SimpleCondition((FieldName) part, sign, value);
					result.add(simpleCondition);
					i = i + 2;
				} else {
					return null;
				}
			} else {
				result.add(part);
			}			
		}		
		return result;
	}
	
	/*
	 * Проверяет, что далее в списке идут: знак и значение
	 */
	private boolean isNextSignValue(int index) {
		return index + 2 < fParts.size() 
		&& fParts.get(index + 1) instanceof Sign 
		&& fParts.get(index + 2) instanceof Value;
	}
	
	//=====================================
	// validate condition
	
	/**
	 * Возвращает ошибку, либо null
	 */
	public String validate(List<ISchemaContainer> schemaContainers, List<Repository> repositories) {
		if (fParts.isEmpty()) {
			return "Условие не задано";
		}
		for (ConditionPart part: fParts) {
			if (part instanceof SimpleCondition) {
				SimpleCondition condition = (SimpleCondition) part;
				String validate = condition.validate(schemaContainers, repositories);
				if (validate != null) {
					return validate;
				}
			}
		}						
		return null;
	}
	
	//============================
	// текст для поиска
	
	public Set<String> getNotCaseSensetiveTexts(){
		return getTextValues(FieldType.TEXT);
	}
	
	public Set<String> getNotCaseSensetiveWords(){
		return getTextValues(FieldType.WORD);
	}
	
	public Set<String> getCaseSensetiveTexts(){
		return getTextValues(FieldType.CASE_TEXT);
	}
	
	public Set<String> getCaseSensetiveWords(){
		return getTextValues(FieldType.CASE_WORD);
	}
	
	private Set<String> getTextValues(FieldType type){
		return fParts.stream()
		.filter(SimpleCondition.class::isInstance)
		.map(SimpleCondition.class::cast)
		.filter(cond -> type.is(cond.fieldName().getValue()))
		.filter(cond -> cond.sign() == Sign.EQUAL)
		.map(cond -> cond.value().getValue())
		.collect(Collectors.toSet());
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Condition: ");
		for (ConditionPart part: fParts){
			builder.append(part);
			builder.append(" ");
		}
		return builder.toString();
	}
	
	public List<ConditionPart> getParts(){
		return fParts;
	}

}