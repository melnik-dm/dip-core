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

public class RenameResult implements ResultOperation {
	
	private String fOldName;
	private String fNewName;
	private String fParentId;
	private boolean fNeedReserve;
	
	public RenameResult(String parentId, String newName, String oldName) {
		this(parentId, newName, oldName, false);
	}
	
	public RenameResult(String parentId, String oldName, String newName,  boolean reserve) {
		fParentId = parentId;
		fOldName = oldName;
		fNewName = newName;
		fNeedReserve = reserve;		
	}
	
	public String getParentId() {
		return fParentId;
	}
	
	public String getOldName() {
		return fOldName;
	}
	
	public String getNewName() {
		return fNewName;
	}
	
	public boolean isNeedReserve() {
		return fNeedReserve;
	}
	
}
