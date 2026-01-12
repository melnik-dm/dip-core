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
package ru.dip.editors.report.content.model;

import java.io.IOException;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.swt.graphics.Point;

import ru.dip.core.model.DipDescription;
import ru.dip.core.model.DipElementType;
import ru.dip.core.model.DipProject;
import ru.dip.core.model.interfaces.IDipComment;
import ru.dip.core.model.interfaces.IDipDocumentElement;
import ru.dip.core.model.interfaces.IDipElement;
import ru.dip.core.model.interfaces.IDipParent;
import ru.dip.core.model.interfaces.IParent;
import ru.dip.core.model.reports.IReportContainer;
import ru.dip.core.model.vars.IVarContainer;
import ru.dip.core.report.model.report.ReportEntry;

public class ReportEntryPresentation implements IDipParent {

	private ReportEntry fEntry;
	
	public ReportEntryPresentation(ReportEntry entry) {
		fEntry = entry;
	}
	
	public ReportEntry getEntry() {
		return fEntry;
	}
	
	//=====================
	// stub
	

	@Override
	public <T> T getAdapter(Class<T> adapter) {
		return null;
	}

	@Override
	public DipProject dipProject() {
		return null;
	}

	@Override
	public String name() {
		return null;
	}

	@Override
	public DipElementType type() {
		return null;
	}


	@Override
	public void setResource(IResource resource) {
		
	}

	@Override
	public String id() {
		return null;
	}

	@Override
	public void updateWithProject() {		
	}

	@Override
	public boolean canDelete() {
		return false;
	}

	@Override
	public boolean canRename() {
		return false;
	}

	@Override
	public void dispose() {
		
	}

	@Override
	public void setParent(IParent parent) {		
	}

	@Override
	public boolean hasParent(IParent parent) {
		return false;
	}

	@Override
	public boolean isReadOnly() {
		return false;
	}

	@Override
	public void setReadOnly(boolean value) {	
	}

	@Override
	public boolean isIncluded() {
		return false;
	}

	@Override
	public void setIncluded(boolean value) {
		
	}



	@Override
	public IDipDocumentElement strong() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IDipComment comment() {
		return null;
	}

	@Override
	public String getCommentContent() {
		return null;
	}

	@Override
	public void setDipComment(IDipComment comment) {
		
	}

	@Override
	public void updateDipComment(String newCommentContent) {
		
	}

	@Override
	public void deleteDipComment() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public DipDescription dipDescription() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setDipDescription(DipDescription description) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String description() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setDescription(String description) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void removeDescription() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void updateDescription(String newDescriptionContent) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isDisabled() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isDisabledInDocument() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setDisabled(boolean value) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public List<? extends IDipElement> getChildren() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean hasChildren() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public IDipElement getChild(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void removeChild(IDipElement child) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void refresh() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean hasFindResult() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public List<Point> getFindedPoints() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void updateFindedPoints(String newContent) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addNewChild(IDipDocumentElement dipDocumentElement, int dipIndex) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public IDipElement createNewUnit(IFile file) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IDipDocumentElement createNewUnit(IFile file, int dipIndex) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IDipParent createNewFolder(IFolder folder) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IDipParent createNewFolder(IFolder fodler, int dipIndex) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IDipParent includeFolder(IFolder folder, String name, String description, boolean readOnly) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IDipParent includeFolder(IFolder folder, int dipIndex, String name, String description, boolean readOnly) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IDipElement createUnit(IFile file) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IDipElement createFolder(IFolder folder) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IDipElement createReservedFolder(IFolder folder) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IDipElement createReservedUnit(IFile file) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<IDipDocumentElement> getDipDocChildrenList() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IDipDocumentElement[] getOneListChildren() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object[] getDipChildren() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getLocalNumber() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isActiveNumeration() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setActiveNumeration(boolean active) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getParentNumber() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String number() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IContainer resource() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getNumberDescrition(boolean showNumeration) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isFileNumeration() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isFolderNumeration() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setFileStep(String step) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getFileStep() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getFolderStep() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setFolderStep(String step) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getPageBreak() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setPageBreak(String value) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isRoot() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void sort() throws ParserConfigurationException, IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public IVarContainer getVariablesContainer() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void deleteVarContainer() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public IReportContainer getReportContainer() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IDipElement createReservedMarker(IFile file) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IDipParent parent() {
		// TODO Auto-generated method stub
		return null;
	}

}
