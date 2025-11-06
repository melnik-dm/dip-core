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

import java.util.ArrayList;
import java.util.List;

import ru.dip.core.model.DipUnit;
import ru.dip.core.model.interfaces.IDipDocumentElement;
import ru.dip.core.model.interfaces.IDipParent;
import ru.dip.core.report.model.condition.BooleanValue;
import ru.dip.core.report.model.condition.Condition;
import ru.dip.core.report.model.condition.ConditionPart;
import ru.dip.core.report.model.condition.FieldName;
import ru.dip.core.report.model.condition.FieldName.FieldType;
import ru.dip.core.report.model.condition.Sign;
import ru.dip.core.report.model.condition.SimpleCondition;
import ru.dip.core.report.model.condition.Value;

public class DipDocElementConditionChecker {

	public static boolean checkDipDocElement(IDipDocumentElement dipDocumentElement, Condition condition) {
		List<ConditionPart> parts = condition.getParts();
		if (parts.isEmpty()) {
			return true;
		}
		List<ConditionPart> boolConditions = evaluateSimpleConditions(parts, dipDocumentElement);
		return BooleanCalculator.checkBooleanConditions(boolConditions);
	}

	private static List<ConditionPart> evaluateSimpleConditions(List<ConditionPart> parts,
			IDipDocumentElement dipDocumentElement) {
		List<ConditionPart> result = new ArrayList<>();
		for (ConditionPart part : parts) {
			if (part instanceof SimpleCondition) {
				SimpleCondition simpleCondition = (SimpleCondition) part;
				boolean evaluate = evaluate(simpleCondition, dipDocumentElement);
				result.add(BooleanValue.of(evaluate));
			} else {
				result.add(part);
			}
		}
		return result;
	}

	private static boolean evaluate(SimpleCondition simpleCondition, IDipDocumentElement dipDocumentElement) {
		FieldName field = simpleCondition.fieldName();
		Sign sign = simpleCondition.sign();
		Value value = simpleCondition.value();
		if (dipDocumentElement instanceof DipUnit) {
			return evaluateDipUnit((DipUnit) dipDocumentElement, field, sign, value);
		} else if (dipDocumentElement instanceof IDipParent) {
			return evaluateDipParent((IDipParent) dipDocumentElement, field, sign, value);
		}
		return false;
	}

	private static boolean evaluateDipUnit(DipUnit unit, FieldName fieldname, Sign sign, Value value) {
		FieldType type = fieldname.type();
		switch (type) {
		case EXT: {
			return SimpleConditionChecker.checkFileExtension(unit, sign, value);
		}
		case PATH: {
			return SimpleConditionChecker.checkPath(unit, value);
		}
		case ENABLED: {
			return SimpleConditionChecker.checkEnabled(unit, sign, value);
		}
		case FORM_FIELD: {
			return FormConditionChecker.checkFormField(unit, fieldname, sign, value);
		}
		case TEXT: {
			return SimpleConditionChecker.checkText(unit, sign, value, false);
		}
		case CASE_TEXT: {
			return SimpleConditionChecker.checkText(unit, sign, value, true);
		}
		case WORD: {
			return SimpleConditionChecker.checkWord(unit, sign, value, false);
		}
		case CASE_WORD: {
			return SimpleConditionChecker.checkWord(unit, sign, value, true);
		}
		case VERSION: {
			return SimpleConditionChecker.checkVersion(unit, sign, value);
		}		
		default:
			return false;
		}
	}

	private static boolean evaluateDipParent(IDipParent parent, FieldName fieldname, Sign sign, Value value) {
		if (fieldname.type() == FieldType.PATH) {
			return SimpleConditionChecker.checkPath(parent, value);
		}
		return true;
	}
	
}
