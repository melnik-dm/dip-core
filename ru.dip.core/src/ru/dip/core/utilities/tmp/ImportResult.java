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

public class ImportResult {
	
	private String fParentId;
	private int fIndex = -1;    // индекс нового элемента
	private String fName;  // имя нового элемента
	private String fFilePath;

	public ImportResult(String parentId, String name, int index, String filePath) {
		fParentId = parentId;
		fName = name;
		fIndex = index;
		fFilePath = filePath;
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
	
	public String getFilePath() {
		return fFilePath;
	}

}
