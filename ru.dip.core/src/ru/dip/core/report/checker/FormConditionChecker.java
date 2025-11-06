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
package ru.dip.core.report.checker;

import ru.dip.core.form.model.CheckField;
import ru.dip.core.form.model.ComboField;
import ru.dip.core.form.model.Field;
import ru.dip.core.form.model.RadioField;
import ru.dip.core.form.model.TextField;
import ru.dip.core.model.DipUnit;
import ru.dip.core.report.model.condition.FieldName;
import ru.dip.core.report.model.condition.Sign;
import ru.dip.core.report.model.condition.Value;
import ru.dip.core.unit.TablePresentation;
import ru.dip.core.unit.UnitPresentation;
import ru.dip.core.unit.form.FormPresentation;

public class FormConditionChecker {

	public static boolean checkFormField(DipUnit unit, FieldName field, Sign sign, Value value) {
		String[] fields = field.getValue().split("\\.");
		if (fields.length != 2) {
			return false;
		}
		String ext = fields[0];
		boolean checkExt = SimpleConditionChecker.checkFileExtension(unit, Sign.EQUAL, new Value(ext));
		if (!checkExt) {
			return false;
		}
		UnitPresentation presentation = unit.getUnitPresentation();
		if (presentation == null) {
			return false;
		}
		TablePresentation tablePresentation = presentation.getPresentation();
		if (!(tablePresentation instanceof FormPresentation)) {
			return false;
		}
		FormPresentation formPresentation = (FormPresentation) tablePresentation;
		return FormConditionChecker.evaluate(formPresentation, new FieldName(fields[1]), sign, value);
	}

	public static boolean evaluate(FormPresentation formPresentation, FieldName fieldname, Sign sign, Value value) {
		Field field = formPresentation.getField(fieldname.getValue());
		if (field == null) {
			return false;
		}
		if (field instanceof TextField) {
			return evaluateTextField((TextField) field, sign, value);
		} else if (sign.mayCompare()) {
			if (field instanceof CheckField) {
				CheckField check = (CheckField) field;
				return evaluateCheckField(check, sign, value);
			} else if (field instanceof ComboField) {
				ComboField combo = (ComboField) field;
				return evaluateComboField(combo, sign, value);
			} else if (field instanceof RadioField) {
				RadioField radio = (RadioField) field;
				return evaluateRadioField(radio, sign, value);
			} else {
				return false;
			}
		}
		return false;
	}
	
	private static boolean evaluateTextField(TextField field, Sign sign, Value value){
		return evaluateValue(field.getValue(), sign, value.getValue());
	}
	
	private static boolean evaluateValue(String value1, Sign sign, String value2){
		if (sign.mayEquals()) {
			if (sign == Sign.EQUAL) {
				return (value2.equals(value1));
			} else {
				return !(value2.equals(value1));
			}
		} else {
			return false;
		}
	}
	
	private static boolean evaluateCheckField(CheckField check,Sign sign, Value value){
		if (sign == Sign.EQUAL) {
			Integer intValue = value.getInteger();
			if (intValue == null) {
				intValue = check.getItemNumber(value.getValue());
			}
			return check.checkedValue(intValue);
		} else if (sign == Sign.NOT_EQUAL) {
			Integer intValue = value.getInteger();
			if (intValue == null) {
				intValue = check.getItemNumber(value.getValue());
			}
			return !check.checkedValue(intValue);
		} else {
			Integer intValue = value.getInteger();
			if (intValue == null) {
				return false;
			}
			int checkedItemsCount = check.getValuesNumber().size();
			if (sign == Sign.MORE) {
				return checkedItemsCount > intValue;
			} else if (sign == Sign.MORE_EQUAL) {
				return checkedItemsCount >= intValue;
			} else if (sign == Sign.LESS) {
				return checkedItemsCount < intValue;
			} else if (sign == Sign.LESS_EQUAL) {
				return checkedItemsCount < intValue;
			} else {
				return false;
			}
		}
	}
	
	private static boolean evaluateComboField(ComboField combo, Sign sign, Value value){
		int comboValue = combo.getIntValue();
		value.getValue();
		Integer intValue = value.getInteger();
		if (intValue == null) {
			intValue = combo.getItemNumber(value.getValue());
		}
				
		if (comboValue < 0 && intValue < 0) {
			return evaluateValue(combo.getValue(), sign, value.getValue());
		}			
		return evaluateCombo(comboValue, sign, intValue);
	}

	private static boolean evaluateRadioField(RadioField radio, Sign sign, Value value){
		int comboValue = radio.getIntValue();
		Integer intValue = value.getInteger();
		if (intValue == null) {
			intValue = radio.getItemNumber(value.getValue());
		}
		if (intValue < 0) {
			return false;
		}
		return evaluateCombo(comboValue, sign, intValue);
	}
	
	private static boolean evaluateCombo(int value1, Sign sign, int value2){		
		switch (sign) {
		case EQUAL:{
			return value1 == value2;			
		}
		case NOT_EQUAL:{
			return value1 != value2;
		}
		case MORE:{
			return value1 > value2;
		}
		case MORE_EQUAL:{
			return value1 >= value2;
		}
		case LESS:{
			return value1 < value2;
		}
		case LESS_EQUAL:{
			return value1 <= value2;
		}
		default:
			return false;
		}
	}
	
}
