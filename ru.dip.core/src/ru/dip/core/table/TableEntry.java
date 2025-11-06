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
package ru.dip.core.table;

public class TableEntry {

	private String fName;
	private String fDesription;
	private boolean fNumeration = true;
	private String fLink;
	private boolean fReadOnly;
	private boolean fDisable;
	private boolean fHorizontalOrientation = false;

	public TableEntry(String name, String description, String numeration) {
		fName = name;
		fDesription = description;
		if (numeration != null && "false".equals(numeration)){
			fNumeration = false;
		} else {
			fNumeration = true;
		}
	}
	
	public String getName(){
		return fName;
	}
	
	public String getDescription(){
		return fDesription;
	}
	
	public boolean isShowNumeration(){
		return fNumeration;
	}
	
	public String getLink() {
		return fLink;
	}
	
	public void setLink(String link) {
		fLink = link;
	}
	
	public boolean isReadOnly() {
		return fReadOnly;
	}
	
	public void setReadOnly(String value) {
		if (value != null && "true".equals(value)){
			fReadOnly = true;
		} else {
			fReadOnly = false;
		}
	}

	public boolean isDisable() {
		return fDisable;
	}
	
	public void setDisable(String value) {
		if (value != null && "true".equals(value)){
			fDisable = true;
		} else {
			fDisable = false;
		}		
	}

	public void setOrientation(String orientation) {
		if (orientation != null && orientation.equals(TableReader.LANDSCAPE)) {
			fHorizontalOrientation = true;
		} else {
			fHorizontalOrientation = false;
		}
	}
	
	public boolean isHorizontal() {
		return fHorizontalOrientation;
	}
	
	@Override
	public String toString() {
		return fName;
	}
}
