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

public class CreateFolderResult {
	
	private String fParentId;
	private int fIndex = -1;    // индекс нового элемента
	private String fName;  // имя нового элемента
	private String fDescription;
	private String fFileStep;
	private String fFolderStep;
	private boolean fReserved;
	
	private CreateFolderResult() {}
	
	public String getParentId() {
		return fParentId;
	}
	
	public String getFolderName() {
		return fName;
	}
	
	public int getIndex() {
		return fIndex;
	}
	
	public String getDescription() {
		return fDescription;
	}
	
	public String getFileStep() {
		return fFileStep;
	}
	
	public String getFolderStep() {
		return fFolderStep;
	}
	
	public boolean isReserved() {
		return fReserved;
	}
	

	
	public static class CreateFolderResultBuilder {
		
		private String fParentId;
		private int fIndex = -1;
		private String fName; 
		private String fDescription;
		private String fFileStep;
		private String fFolderStep;
		private boolean fReserved;
		
		
		public CreateFolderResult build() {
			CreateFolderResult result = new CreateFolderResult();
			result.fParentId = fParentId;
			result.fIndex = fIndex;
			result.fName = fName; 
			result.fDescription = fDescription;
			result.fFileStep = fFileStep;
			result.fFolderStep = fFolderStep;
			result.fReserved = fReserved;			
			return result;
		}
		
		public CreateFolderResultBuilder buildParentId(String parentId) {
			fParentId = parentId;
			return this;
		}
		
		public CreateFolderResultBuilder buildIndex(int index) {
			fIndex = index;
			return this;
		}
		
		public CreateFolderResultBuilder buildName(String name) {
			fName = name;
			return this;
		}
		
		public CreateFolderResultBuilder buildDescritpion(String description) {
			fDescription = description;
			return this;
		}
		
		public CreateFolderResultBuilder buildFileStep(String fileStep) {
			fFileStep = fileStep;
			return this;
		}
		
		public CreateFolderResultBuilder buildFolderStep(String folderStep) {
			fFolderStep = folderStep;
			return this;
		}
		
		public CreateFolderResultBuilder buildReserved(boolean reserved) {
			fReserved = reserved;
			return this;
		}
			
	}
		
}
