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

import ru.dip.core.report.model.condition.BooleanSign;
import ru.dip.core.report.model.condition.BooleanValue;
import ru.dip.core.report.model.condition.CloseBracket;
import ru.dip.core.report.model.condition.ConditionPart;
import ru.dip.core.report.model.condition.OpenBracket;

public class BooleanCalculator {

	public static boolean checkBooleanConditions(List<ConditionPart> boolConditions) {
		if (boolConditions == null) {
			return false;
		}
		int size = boolConditions.size();

		while (size != 1) {
			boolConditions = evaluateBoolean(boolConditions);
			if (boolConditions.size() == size) {
				return false;
			} else {
				size = boolConditions.size();
			}
		}
		ConditionPart part = boolConditions.get(0);
		if (part instanceof BooleanValue) {
			BooleanValue bv = (BooleanValue) part;
			return bv.getValue();
		}
		return false;
	}

	private static List<ConditionPart> evaluateBoolean(List<ConditionPart> conditions) {
		List<ConditionPart> result = new ArrayList<>();
		for (int i = 0; i < conditions.size(); i++) {
			ConditionPart part = conditions.get(i);
			if (part instanceof OpenBracket && i + 2 < conditions.size()) {
				if (conditions.get(i + 1) instanceof BooleanValue && conditions.get(i + 2) instanceof CloseBracket) {
					result.add(conditions.get(i + 1));
					i = i + 2;
					continue;
				} else {
					result.add(part);
				}
			} else if (part instanceof BooleanValue && i + 2 < conditions.size()) {
				if (conditions.get(i + 1) instanceof BooleanSign && conditions.get(i + 2) instanceof BooleanValue) {
					BooleanValue value1 = (BooleanValue) part;
					BooleanSign sign = (BooleanSign) conditions.get(i + 1);
					BooleanValue value2 = (BooleanValue) conditions.get(i + 2);
					BooleanValue v = evaluateBoolean(value1, sign, value2);
					if (v == null) {
						return null;
					} else {
						result.add(v);
						i = i + 2;
						continue;
					}
				} else {
					result.add(part);
				}
			} else {
				result.add(part);
			}
		}
		return result;
	}

	private static BooleanValue evaluateBoolean(BooleanValue value1, BooleanSign sign, BooleanValue value2) {
		if (sign.isValidBoolean()) {
			if (sign == BooleanSign.AND) {
				boolean b = value1.getValue() && value2.getValue();
				return BooleanValue.of(b);
			} else {
				boolean b = value1.getValue() || value2.getValue();
				return BooleanValue.of(b);
			}
		} else {
			return null;
		}
	}
}
