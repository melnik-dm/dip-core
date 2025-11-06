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

public class FieldName implements ConditionPart {
	
	public static enum FieldType {

		EXT("ext"), 
		ENABLED("enabled"), 
		PATH("path"), 
		FORM_FIELD("form"), 
		TEXT("text"), 
		CASE_TEXT("case_text"),
		WORD("word"), 
		CASE_WORD("case_word"),
		VERSION("version");

		private String fVarName;

		private FieldType(String name) {
			fVarName = name;
		}

		public String getVarName() {
			return fVarName;
		}

		public boolean is(String var) {
			return fVarName.equals(var);
		}
	}
	
	private String fValue;
	
	public FieldName(String value) {
		fValue = value;
	}
	
	public String getValue(){
		return fValue;
	}
	
	public void setValue(String newName) {
		fValue = newName;
	}
	
	public FieldType type() {
		if (FieldType.EXT.is(fValue)) {
			return FieldType.EXT;
		}
		if (FieldType.PATH.is(fValue)) {
			return FieldType.PATH;
		}
		if (FieldType.ENABLED.is(fValue)) {
			return FieldType.ENABLED;
		}
		if (FieldType.TEXT.is(fValue)) {
			return FieldType.TEXT;
		}
		if (FieldType.CASE_TEXT.is(fValue)) {
			return FieldType.CASE_TEXT;
		}
		if (FieldType.WORD.is(fValue)) {
			return FieldType.WORD;
		}
		if (FieldType.CASE_WORD.is(fValue)) {
			return FieldType.CASE_WORD;
		}
		if (FieldType.VERSION.is(fValue)) {
			return FieldType.VERSION;
		}
		
		String[] fields = fValue.split("\\.");
		if (fields.length == 2) {
			return FieldType.FORM_FIELD;
		}		
		return null;
	}
	
	@Override
	public String toString() {
		return fValue;
	}

}
