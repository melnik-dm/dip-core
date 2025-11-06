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

import ru.dip.core.unit.UnitType;

public enum ExportElementType {
	
	PAGE_BREAK, 
	HEADER1, HEADER2, HEADER3, HEADER4, HEADER5, HEADER6, HEADER7,
	TXT, MARKDOWN, 
	IMAGE, PLANTUML, DIA, HTML_IMAGE,
	TABLE, CSV,
	FORM, 
	REPORT,
	HTML, 
	CHANGELOG, GLOSSARY, TOC,
	UNKNOWN;
	
	public boolean isFolder() {
		return this == HEADER1 
				|| this == HEADER2
				|| this == HEADER3
				|| this == HEADER4
				|| this == HEADER5
				|| this == HEADER6
				|| this == HEADER7;
	}
	
	public static ExportElementType fromUnitType(UnitType type) {
		switch(type) {
		case CHANGELOG:{
			return CHANGELOG;
		}
		case CSV:{
			return CSV;
		}
		case DIA:{
			return DIA;
		}
		case DOT:{
			return PLANTUML;
		}
		case GLOS_REF:{
			return GLOSSARY;
		}
		case HTML:{
			return HTML;
		}
		case IMAGE:{
			return IMAGE;
		}
		case HTML_IMAGE:{
			return HTML_IMAGE;
		}
		case MARKDOWN:{
			return MARKDOWN;
		}
		case REPROT_REF:{
			return REPORT;
		}
		case FORM:{
			return FORM;
		}
		case TABLE:{
			return TABLE;
		}
		case TEXT:{
			return TXT;
		}
		case TOC_REF:{
			return TOC;
		}
		case UML:{
			return PLANTUML;
		}
		default:
			break;						
		}
		return UNKNOWN;
	}
	
	public static ExportElementType fromFolder(int level) {
		switch (level) {
		case 1: return HEADER1;
		case 2: return HEADER2;
		case 3: return HEADER3;
		case 4: return HEADER4;
		case 5: return HEADER5;
		case 6: return HEADER6;
		case 7: return HEADER7;
		}
		return HEADER7;
	}
	
}
