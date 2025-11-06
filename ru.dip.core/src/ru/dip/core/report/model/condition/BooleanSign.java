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

public enum BooleanSign implements ConditionPart {

	AND, OR, UNKNOWN;

	public static BooleanSign of(String value) {
		if ("&".equals(value) || "&&".equals(value) || "and".equals(value.toLowerCase())) {
			return AND;
		}
		if ("|".equals(value) || "||".equals(value) || "or".equals(value.toLowerCase())) {
			return OR;
		}
		return UNKNOWN;
	}

	public boolean isValidBoolean() {
		return this != UNKNOWN;
	}

}
