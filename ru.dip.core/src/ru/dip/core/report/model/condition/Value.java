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

public class Value implements ConditionPart {
	
	private final String fValue;
	private Object fData;  // вспомогательные данные (коммит, еще что-то, чтобы вычислить один раз)
	
	public Value(String value) {
		fValue = value;
	}
	
	public String getValue(){
		if (fValue != null && fValue.length() > 1 && fValue.startsWith("\"") && fValue.endsWith("\"")){
			return fValue.substring(1, fValue.length() - 1);
		}		
		return fValue;
	}
	
	public boolean isBooleanValue() {
		return "true".equalsIgnoreCase(fValue) ||
				"false".equalsIgnoreCase(fValue);
	}
	
	public Integer getInteger(){			
		if (fValue.isEmpty() || fValue.equals("\"\"")){
			return -1;
		} 
		try {
			return Integer.parseInt(fValue);
		} catch (NumberFormatException e){
			return null;
		}
	}
	
	public Object getData() {
		return fData;
	}
	
	public void setData(Object data) {
		fData = data;
	}
	
	@Override
	public String toString() {
		return fValue;
	}

}
