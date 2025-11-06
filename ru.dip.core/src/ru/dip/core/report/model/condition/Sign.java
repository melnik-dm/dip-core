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

public enum Sign implements ConditionPart {

	EQUAL, NOT_EQUAL, MORE, MORE_EQUAL, LESS, LESS_EQUAL, UNKNOWN;

	public static Sign of(String textSign) {
		switch (textSign) {
		case "==":
			return EQUAL;
		case "!=":
			return NOT_EQUAL;
		case ">":
			return MORE;
		case ">=":
			return MORE_EQUAL;
		case "<":
			return LESS;
		case "<=":
			return LESS_EQUAL;
		default:
			return UNKNOWN;
		}
	}

	public boolean mayEquals() {
		return this == EQUAL || this == NOT_EQUAL;
	}

	public boolean mayCompare() {
		return this == EQUAL 
				|| this == NOT_EQUAL 
				|| this == MORE 
				|| this == MORE_EQUAL 
				|| this == LESS
				|| this == LESS_EQUAL;
	}

	
	@Override
	public String toString() {
		switch (this) {
		case EQUAL:
			return "==";
		case NOT_EQUAL:
			return "!=";
		case MORE:
			return ">";
		case MORE_EQUAL:
			return ">=";
		case LESS:
			return "<";
		case LESS_EQUAL:
			return "<=";
		default:
			return "UNKNOWN";
		}
	}
}
