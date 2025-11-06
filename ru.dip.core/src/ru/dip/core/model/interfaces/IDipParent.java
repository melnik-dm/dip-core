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
package ru.dip.core.model.interfaces;

import java.io.IOException;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;

import ru.dip.core.model.finder.IFindPoints;
import ru.dip.core.model.reports.IReportContainer;
import ru.dip.core.model.vars.IVarContainer;

public interface IDipParent extends IDipDocumentElement,IParent, IFindPoints {

	// при перемещении
	void addNewChild(IDipDocumentElement dipDocumentElement, int dipIndex);
	
	//============================
	// создание новых элементов
	IDipElement createNewUnit(IFile file);

	IDipDocumentElement createNewUnit(IFile file, int dipIndex);
	
	IDipParent createNewFolder(IFolder folder);
	
	IDipParent createNewFolder(IFolder fodler, int dipIndex);
		
	IDipParent includeFolder(IFolder folder, String name, String description, boolean readOnly);
	
	IDipParent includeFolder(IFolder folder, int dipIndex, String name,  String description, boolean readOnly);
	
	//==========================
	// при создании модели
	
	IDipElement createUnit(IFile file);
	
	IDipElement createFolder(IFolder folder);
	
	IDipElement createReservedFolder(IFolder folder);
	
	IDipElement createReservedUnit(IFile file);
	
	//=========================
	// get children
	
	List<IDipDocumentElement> getDipDocChildrenList();
	
	IDipDocumentElement[] getOneListChildren();
		
	Object[] getDipChildren();
		
	//=========================
	// numeration
	
	String getLocalNumber();
	
	boolean isActiveNumeration();
	
	void setActiveNumeration(boolean active);
	
	String getParentNumber();
	
	String number();
	
	IContainer resource();
	
	String getNumberDescrition(boolean showNumeration);

	//==========================
	// children numeration
		
	boolean isFileNumeration();
		
	boolean isFolderNumeration();
	
	void setFileStep(String step);
		
	String getFileStep();
	
	String getFolderStep();
	
	void setFolderStep(String step);
	
	//=============================
	// pagebreak
	
	String getPageBreak();
	
	void setPageBreak(String value);

	boolean isRoot();

	void sort() throws ParserConfigurationException, IOException;

	
	//=========================
	// variables
	
	IVarContainer getVariablesContainer();

	void deleteVarContainer();
	
	//=========================
	// reports
	
	IReportContainer getReportContainer();
	
	//==========================
	// reserve
	
	IDipElement createReservedMarker(IFile file);
}
