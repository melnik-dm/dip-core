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

public class IncludeFolderResult {
	
	private String fParentId;
	private int fIndex = -1;    // индекс нового элемента
	private String fName;  // имя нового элемента
	private String fDescription;
	private String fIncludePath;
	private boolean fReadOnly;

	private IncludeFolderResult() {}
	
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
	
	public String getIncludePath() {
		return fIncludePath;
	}
		
	public boolean isReadOnly() {
		return fReadOnly;
	}
	
	public static class IncludeResultBuilder {
		
		private String fParentId;
		private int fIndex = -1;
		private String fName; 
		private String fDescription;
		private String fIncludePath;
		private boolean fReadOnly;
		
		
		public IncludeFolderResult build() {
			IncludeFolderResult result = new IncludeFolderResult();
			result.fParentId = fParentId;
			result.fIndex = fIndex;
			result.fName = fName; 
			result.fDescription = fDescription;
			result.fIncludePath = fIncludePath;
			result.fReadOnly = fReadOnly;
			return result;
		}
		
		public IncludeResultBuilder buildParentId(String parentId) {
			fParentId = parentId;
			return this;
		}
		
		public IncludeResultBuilder buildIndex(int index) {
			fIndex = index;
			return this;
		}
		
		public IncludeResultBuilder buildName(String name) {
			fName = name;
			return this;
		}
		
		public IncludeResultBuilder buildDescritpion(String description) {
			fDescription = description;
			return this;
		}
		
		public IncludeResultBuilder buildIncludePath(String includePath) {
			fIncludePath = includePath;
			return this;
		}
		
		public IncludeResultBuilder buildReadOnly(boolean readOnly) {
			fReadOnly = readOnly;
			return this;
		}
			
	}
		
}
