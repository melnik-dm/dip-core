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
package ru.dip.ui.export.diff.model;

public class DiffExportEntry {

	private UnitRevisionEntry fVersion1;
	private UnitRevisionEntry fVersion2;
	
	public void setVersion1(UnitRevisionEntry entry) {
		fVersion1 = entry;
	}
	
	public void setVersion2(UnitRevisionEntry entry) {
		fVersion2 = entry;
	}
	
	public UnitRevisionEntry getVersion1() {
		return fVersion1;
	}
	
	public UnitRevisionEntry getVersion2() {
		return fVersion2;
	}
	
	public String getId() {
		if (fVersion1 != null) {
			return fVersion1.getId();
		}
		if (fVersion2 != null) {
			return fVersion2.getId();
		}
		return null;
	}
	
}
