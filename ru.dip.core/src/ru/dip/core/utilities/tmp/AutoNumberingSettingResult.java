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

public class AutoNumberingSettingResult {
	
	private String fParentId;
	private String fOldFileStep;
	private String fOldFolderStep;
	private String fNewFileStep;
	private String fNewFolderStep;
	
	
	public AutoNumberingSettingResult(String parentId, String oldFileStep, String oldFolderStep,
			String newFileStep, String newFolderStep) {
		fParentId = parentId;
		fOldFileStep = oldFileStep;
		fOldFolderStep = oldFolderStep;
		fNewFileStep = newFileStep;
		fNewFolderStep = newFolderStep;		
	}
	
	public String getParentId() {
		return fParentId;
	}
	
	public String getOldFileStep() {
		return fOldFileStep;
	}
	
	public String getOldFolderStep() {
		return fOldFolderStep;
	}
	
	public String getNewFileStep() {
		return fNewFileStep;
	}
	
	public String getNewFolderStep() {
		return fNewFolderStep;
	}

}
