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
package ru.dip.core.model;

import ru.dip.core.model.interfaces.IDipElement;

public enum DipElementType {

	RPOJECT, 
	SCHEMA_FOLDER, 
	SCHEMA, 
	SCHEMA_LIST, 
	FOLDER, UNIT, 
	RESERVED_FOLDER, 
	RESERVED_UNIT,
	RESERVED_MARKER,
	TABLE,	
	COMMENT,
	DESCRIPTION,
	FOLDER_COMMENT,
	REPORT_FOLDER,
	REPORT, 
	GLOSSARY_FOLDER,
	GLOSSARY_FIELD,
	GLOSS_REF,
	EXPORT_CONFIG,
	TOC_REF,
	SERV_FOLDER,
	CHANGE_LOG,
	BROKEN_FOLDER,
	INCLUDE_FOLDER,
	VARIABLES_CONTAINER,
	VARIABLE,
	UNDEFINE;
	
	public static boolean isFolderType(IDipElement element) {
		return isFolderType(element.type());
	}
	
	public static boolean isFolderType(DipElementType elementType) {
		return elementType == DipElementType.FOLDER || elementType == DipElementType.INCLUDE_FOLDER;
	}
	
}
