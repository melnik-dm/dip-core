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
package ru.dip.ui.table.table;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.swt.graphics.Point;

import ru.dip.core.model.DipDescription;
import ru.dip.core.model.DipElementType;
import ru.dip.core.model.DipProject;
import ru.dip.core.model.DipTableContainer;
import ru.dip.core.model.interfaces.IParent;
import ru.dip.core.model.reports.IReportContainer;
import ru.dip.core.model.vars.IVarContainer;
import ru.dip.ui.table.ktable.model.ITableInputModel;
import ru.dip.core.model.interfaces.IDipComment;
import ru.dip.core.model.interfaces.IDipElement;
import ru.dip.core.model.interfaces.IDipParent;
import ru.dip.core.model.interfaces.IDipDocumentElement;

/**
 *  Модель для отображения в Document
 *  Включает список родительских элементов + сам контейнер
 */
public class TableModel implements IDipParent, ITableInputModel {
	
	private DipTableContainer fDipContainer;
	private List<IDipParent> fParents;
	
	public TableModel(DipTableContainer dipTableContainer) {
		fDipContainer = dipTableContainer;
		setParents(fDipContainer);
	}
	
	//===========================
	// update
	
	public void updateModel() {
		// NOP
	}
	
	/**
	 * Дополнительное обновление элемента, в моделях наследниках
	 */
	public void additionalUpdate(IDipDocumentElement endElement) {}
	
	public void additionalUpdate(Set<IResource> resources) {}
	
	//=================================
	
	public IDipParent getContainer(){
		return fDipContainer;
	}
	
	private void setParents(IDipParent parent){
		fParents = new ArrayList<>();
		fParents.add(parent);
		while (parent.type() != DipElementType.RPOJECT){
			parent = parent.parent();
			fParents.add(0, parent);			
		}
	}
	
	public List<IDipParent> getParentsList(){
		return fParents;
	}
	
	/**
	 * Для Section (является ли объект одним из заголовоков-родителей)
	 */
	public boolean isParentHeader(IDipElement element){
		return fParents.contains(element);
	}
	
	public boolean isTable(IDipElement element){
		return fDipContainer.equals(element);
	}
	
	public boolean isChild(IDipElement element){
		return element.hasParent(fDipContainer);
	}
	
	//============================
	// IDipParent
	

	@Override
	public String name() {
		return fDipContainer.name();
	}

	@Override
	public DipElementType type() {
		return fDipContainer.type();
	}

	@Override
	public IContainer resource() {
		return fDipContainer.resource();
	}
	
	@Override
	public String id() {
		return fDipContainer.id();
	}

	@Override
	public IDipParent parent() {
		return fDipContainer.parent();
	}

	@Override
	public boolean isRoot() {		
		return fDipContainer.isRoot();
	}
	
	@Override
	public void setParent(IParent parent) {
		
	}

	@Override
	public boolean hasParent(IParent parent) {
		return false;
	}

	@Override
	public String description() {
		return fDipContainer.description();
	}

	@Override
	public void setDescription(String description) {
		fDipContainer.setDescription(description);
	}

	@Override
	public <T> T getAdapter(Class<T> adapter) {
		return null;
	}

	@Override
	public List<IDipElement> getChildren() {
		return fDipContainer.getChildren();
	}

	@Override
	public boolean hasChildren() {
		return fDipContainer.hasChildren();
	}

	@Override
	public IDipElement getChild(String name) {
		return fDipContainer.getChild(name);
	}

	@Override
	public void removeChild(IDipElement child) {
		fDipContainer.removeChild(child);
	}

	@Override
	public IDipElement createUnit(IFile file) {
		return fDipContainer.createUnit(file);
	}

	@Override
	public IDipElement createFolder(IFolder folder) {
		return fDipContainer.createFolder(folder);
	}

	@Override
	public IDipElement createReservedFolder(IFolder folder) {
		return fDipContainer.createFolder(folder);
	}

	@Override
	public IDipElement createReservedUnit(IFile file) {
		return fDipContainer.createReservedUnit(file);
	}

	@Override
	public List<IDipDocumentElement> getDipDocChildrenList() {
		return fDipContainer.getDipDocChildrenList();
	}

	@Override
	public IDipDocumentElement[] getOneListChildren() {
		return fDipContainer.getOneListChildren();
	}
	
	@Override
	public IDipDocumentElement[] getDipChildren() {		
		return fDipContainer.getDipChildren();
	}

	@Override
	public DipProject dipProject() {
		return fDipContainer.dipProject();
	}

	@Override
	public IDipElement createNewUnit(IFile file) {
		return fDipContainer.createNewUnit(file);
	}

	@Override
	public IDipDocumentElement createNewUnit(IFile file, int reqIndex) {
		return fDipContainer.createNewUnit(file, reqIndex);
	}

	@Override
	public IDipParent createNewFolder(IFolder folder) {
		return fDipContainer.createNewFolder(folder);
	}

	@Override
	public IDipParent createNewFolder(IFolder folder, int dipIndex) {
		return fDipContainer.createNewFolder(folder, dipIndex);
	}

	@Override
	public void setResource(IResource resource) {
		fDipContainer.setResource(resource);
	}

	@Override
	public void addNewChild(IDipDocumentElement dipDocElement, int dipIndex) {
		fDipContainer.addNewChild(dipDocElement, dipIndex);		
	}

	@Override
	public String getLocalNumber() {
		return fDipContainer.getLocalNumber();
	}

	@Override
	public boolean isActiveNumeration() {
		return fDipContainer.isActiveNumeration();
	}

	@Override
	public void setActiveNumeration(boolean active) {
		fDipContainer.setActiveNumeration(active);		
	}

	@Override
	public String getParentNumber() {
		return fDipContainer.getParentNumber();
	}

	@Override
	public String number() {
		return fDipContainer.number();
	}

	@Override
	public void refresh() {
		fDipContainer.refresh();
	}

	@Override
	public IDipComment comment() {
		return fDipContainer.comment();
	}
	
	@Override
	public String getCommentContent() {
		return fDipContainer.getCommentContent();
	}

	@Override
	public void setDipComment(IDipComment comment) {
		fDipContainer.setDipComment(comment);
	}

	@Override
	public DipDescription dipDescription() {
		return fDipContainer.dipDescription();
	}

	@Override
	public void setDipDescription(DipDescription description) {
		fDipContainer.setDipDescription(description);
	}

	@Override
	public void removeDescription() {
		fDipContainer.removeDescription();
	}

	@Override
	public void updateDescription(String newDescriptionContent) {
		fDipContainer.updateDescription(newDescriptionContent);	
	}

	@Override
	public void updateDipComment(String newCommentContent) {
		fDipContainer.updateDipComment(newCommentContent);
	}

	@Override
	public void deleteDipComment() {
		fDipContainer.deleteDipComment();		
	}

	@Override
	public boolean isFileNumeration() {		
		return fDipContainer.isFileNumeration();
	}

	@Override
	public boolean isFolderNumeration() {
		return fDipContainer.isFolderNumeration();
	}

	@Override
	public void setFileStep(String step) {
		fDipContainer.setFileStep(step);		
	}

	@Override
	public String getFileStep() {
		return fDipContainer.getFileStep();
	}

	@Override
	public String getFolderStep() {
		return fDipContainer.getFolderStep();
	}

	@Override
	public void setFolderStep(String step) {
		fDipContainer.setFolderStep(step);
	}

	@Override
	public void updateWithProject() {
		fDipContainer.updateWithProject();
	}

	@Override
	public String getNumberDescrition(boolean showNumeration) {
		return fDipContainer.getNumberDescrition(showNumeration);
	}

	@Override
	public IDipParent includeFolder(IFolder folder, String name, String description, boolean readOnly) {
		return fDipContainer.includeFolder(folder, name, description, readOnly);
	}

	@Override
	public IDipParent includeFolder(IFolder folder, int dipIndex, String name, String description, boolean readOnly) {
		return fDipContainer.includeFolder(folder, dipIndex, name, description, readOnly);
	}

	@Override
	public boolean isReadOnly() {
		return fDipContainer.isReadOnly();
	}

	@Override
	public void setReadOnly(boolean value) {
		fDipContainer.setReadOnly(value);
	}

	@Override
	public boolean isIncluded() {
		return fDipContainer.isIncluded();
	}
	
	@Override
	public void setIncluded(boolean value) {
		fDipContainer.setIncluded(value);
	}

	@Override
	public boolean canDelete() {
		return fDipContainer.canDelete();
	}

	@Override
	public boolean canRename() {
		return fDipContainer.canRename();
	}

	@Override
	public String getPageBreak() {
		return fDipContainer.getPageBreak();
	}

	@Override
	public void setPageBreak(String value) {
		fDipContainer.setPageBreak(value);
	}

	@Override
	public boolean isDisabled() {
		return fDipContainer.isDisabled();
	}

	@Override
	public void setDisabled(boolean value) {
		fDipContainer.setDisabled(value);
	}

	@Override
	public boolean isDisabledInDocument() {
		return fDipContainer.isDisabledInDocument();
	}

	@Override
	public void sort() throws ParserConfigurationException, IOException {
		fDipContainer.sort();		
	}

	@Override
	public IDipElement createReservedMarker(IFile file) {
		return fDipContainer.createReservedMarker(file);
	}

	@Override
	public IDipDocumentElement strong() {
		return fDipContainer.strong();
	}

	@Override
	public boolean hasFindResult() {
		return fDipContainer.hasFindResult();
	}

	@Override
	public List<Point> getFindedPoints() {
		return fDipContainer.getFindedIdPoints();
	}

	@Override
	public void updateFindedPoints(String newContent) {
		fDipContainer.updateFindedPoints(newContent);
	}

	@Override
	public IVarContainer getVariablesContainer() {
		return fDipContainer.getVariablesContainer();
	}

	@Override
	public void deleteVarContainer() {
		fDipContainer.deleteVarContainer();
	}

	@Override
	public IReportContainer getReportContainer() {
		return fDipContainer.getReportContainer();
	}

	@Override
	public void dispose() {
		fDipContainer = null;
		fParents.clear();
	}

}
