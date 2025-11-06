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

import ru.dip.core.model.interfaces.IDipElement;
import ru.dip.core.model.interfaces.IParent;
import ru.dip.core.report.model.condition.BooleanValue;
import ru.dip.core.report.model.condition.Condition;
import ru.dip.core.report.model.condition.ConditionPart;
import ru.dip.core.report.model.condition.FieldName;
import ru.dip.core.report.model.condition.FieldName.FieldType;
import ru.dip.core.report.model.condition.Sign;
import ru.dip.core.report.model.condition.SimpleCondition;
import ru.dip.core.report.model.condition.Value;

/**
 * Проверка для элеметов, которые не входят в документ
 * Только фильтр path и расширение
 */
public class NoneDocElementConditionChecker {

	public static boolean checkDipDocElement(IDipElement dipElement, Condition condition) {
		List<ConditionPart> parts = condition.getParts();
		if (parts.isEmpty()) {
			return true;
		}
		List<ConditionPart> boolConditions = evaluateSimpleConditions(parts, dipElement);
		return BooleanCalculator.checkBooleanConditions(boolConditions);
	}

	private static List<ConditionPart> evaluateSimpleConditions(List<ConditionPart> parts,
			IDipElement dipElement) {
		List<ConditionPart> result = new ArrayList<>();
		for (ConditionPart part : parts) {
			if (part instanceof SimpleCondition) {
				SimpleCondition simpleCondition = (SimpleCondition) part;
				boolean evaluate = evaluate(simpleCondition, dipElement);
				result.add(BooleanValue.of(evaluate));
			} else {
				result.add(part);
			}
		}
		return result;
	}

	private static boolean evaluate(SimpleCondition simpleCondition, IDipElement dipElement) {
		FieldName field = simpleCondition.fieldName();
		Sign sign = simpleCondition.sign();
		Value value = simpleCondition.value();
		if (dipElement instanceof IParent) {
			return evaluateParent((IParent) dipElement, field, sign, value);
		} else {
			return evaluateDipElement(dipElement, field, sign, value);
		}
	}

	private static boolean evaluateDipElement(IDipElement dipElement, FieldName fieldname, Sign sign, Value value) {
		FieldType type = fieldname.type();
		switch (type) {
		case EXT: {
			return SimpleConditionChecker.checkFileExtension(dipElement, sign, value);
		}
		case PATH: {
			return SimpleConditionChecker.checkPath(dipElement, value);
		}	
		default:
			return false;
		}
	}

	private static boolean evaluateParent(IParent parent, FieldName fieldname, Sign sign, Value value) {
		if (fieldname.type() == FieldType.PATH) {
			return SimpleConditionChecker.checkPath(parent, value);
		}
		return true;
	}

}
