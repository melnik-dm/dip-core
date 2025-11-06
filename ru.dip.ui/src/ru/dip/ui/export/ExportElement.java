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

import java.util.List;

import ru.dip.core.link.Link;

public class ExportElement {
	
	private ExportElementType fType;
	private String fDescription;
	private String fPath;
	private String fId;
	private List<Link> fLinks;
	private String fNumber;
	private String fPageBreak;
	private boolean fLandscape;
	private boolean fAppendix;
	
	private ExportElement() {
		
	}
		
	public static class ExportElementBuilder {
		
		private ExportElementType fType;
		private String fDescription;
		private String fPath;
		private String fId;
		private List<Link> fLinks;
		private String fNumber;
		private String fPageBreak;
		private boolean fHorizontal = false;
		private boolean fAppendix = false;

				
		public ExportElement build() {
			ExportElement element = new ExportElement();
			element.fType = fType;
			element.fDescription = fDescription;
			element.fPath = fPath;
			element.fId = fId;
			element.fLinks = fLinks;
			element.fNumber = fNumber;
			element.fPageBreak = fPageBreak;
			element.fLandscape = fHorizontal;
			element.fAppendix = fAppendix;
			return element;
		}
		
		public ExportElementBuilder buildType(ExportElementType type){
			fType = type;
			return this;
		}
		
		public ExportElementBuilder buildDescription(String description) {
			fDescription = description;
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
		
		public ExportElementBuilder buildLinks(List<Link> links){
			fLinks = links;
			return this;
		}
		
		public ExportElementBuilder buildNumber(String numeration) {
			fNumber = numeration;
			return this;
		}
		
		public ExportElementBuilder buildPagebreak(String pageBreak) {
			fPageBreak = pageBreak;
			return this;
		}
		
		public ExportElementBuilder buildHorizontal(boolean horizontal) {
			fHorizontal = horizontal;
			return this;
		}
		
		public ExportElementBuilder buildAppendix(boolean appendix) {
			fAppendix = appendix;
			return this;
		}
		
	}
	
	public ExportElementType getType() {
		return fType;
	}

	public String getDescription() {
		return fDescription;
	}
	
	public String getPath() {
		return fPath;              
	}
	
	public void setPath(String path) {
		fPath = path;
	}
	
	public String getId() {
		return fId;
	}
	
	public List<Link> getLinks(){
		return fLinks;
	}
	
	public void setLinks(List<Link> links) {
		fLinks = links;
	}
	
	public String getPageBreak() {
		return fPageBreak;
	}
	
	public String getNumber() {
		return fNumber;
	}
	
	public boolean isLandscape() {
		return fLandscape;
	}
	
	public boolean isAppendix() {
		return fAppendix;
	}

	@Override
	public String toString() {
		return fType + " Description= " + fDescription + ", fPath=" + fPath + ", fId= " + fId +
				", fLinks=" + fLinks + "]";
	}

}
