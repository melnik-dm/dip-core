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
package ru.dip.ui.glossary.table;

import ru.dip.core.model.glossary.GlossaryField;

public class GlossaryEntry {

	private final GlossaryField fField;
	private int fNameHeight;
	private int fValueHeight;

	private String fNameWrapText;
	private String fValueWrapText;
	
	
	public GlossaryEntry(GlossaryField field) {
		fField = field;
	}
	
	public GlossaryField getField() {
		return fField;
	}

	public String getName() {
		return fField.name();
	}
		
	public String getValue() {
		return fField.getValue();
	}
	
	public String getWrapName() {
		return fNameWrapText;
	}
	
	public void setWrapName(String name) {
		fNameWrapText = name;
	}
	
	public String getWrapValue() {
		return fValueWrapText;
	}
	
	public void setWrapValue(String text) {
		fValueWrapText = text;
	}
	
	public int getHeight() {
		return fNameHeight > fValueHeight ? fNameHeight : fValueHeight;
	}
	
	public void setNameHeight(int height) {
		fNameHeight = height;
	}
	
	public void setValueHeight(int height) {
		fValueHeight = height;
	}
	
}
