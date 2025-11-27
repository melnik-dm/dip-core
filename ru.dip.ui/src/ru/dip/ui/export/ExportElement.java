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
package ru.dip.ui.export;

public class ExportElement implements IExportElement {
	
	private ExportElementType fType;
	private String fPath;
	private String fId;

	private ExportElement() {}
		
	public static class ExportElementBuilder {
		
		private ExportElementType fType;
		private String fPath;
		private String fId;

				
		public ExportElement build() {
			ExportElement element = new ExportElement();
			element.fType = fType;
			element.fPath = fPath;
			element.fId = fId;
			return element;
		}
		
		public ExportElementBuilder buildType(ExportElementType type){
			fType = type;
			return this;
		}
	
		public ExportElementBuilder buildPath(String path){
			fPath = path;
			return this;
		}
		
		public ExportElementBuilder buildId(String id) {
			fId = id;
			return this;
		}		
	}
	
	@Override
	public ExportElementType getType() {
		return fType;
	}
	
	@Override
	public String getPath() {
		return fPath;              
	}
	
	@Override
	public void setPath(String path) {
		fPath = path;
	}
	
	@Override
	public String getId() {
		return fId;
	}
}
