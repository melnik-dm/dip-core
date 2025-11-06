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
package ru.dip.core.utilities.tmp;

public class CreateFileResult {
	
	private String fParentId;
	private int fIndex = -1;    // индекс нового элемента
	private String fName;  // имя нового элемента

	public CreateFileResult(String parentId, String name, int index) {
		fParentId = parentId;
		fName = name;
		fIndex = index;
	}
	
	public String getParentId() {
		return fParentId;
	}
	
	public String getFileName() {
		return fName;
	}
	
	public int getIndex() {
		return fIndex;
	}
	
}
