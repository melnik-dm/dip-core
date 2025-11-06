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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;

import ru.dip.core.model.glossary.GlossaryFolder;
import ru.dip.core.model.glossary.ProjectGlossaryFolder;
import ru.dip.core.model.interfaces.IDipDocumentElement;
import ru.dip.core.model.interfaces.IDipElement;
import ru.dip.core.model.interfaces.IDipParent;
import ru.dip.core.model.interfaces.IParent;
import ru.dip.core.model.reports.FolderReportContainer;
import ru.dip.core.model.reports.IMainReportContainer;
import ru.dip.core.model.reports.IReportContainer;
import ru.dip.core.model.reports.ReportContainer;
import ru.dip.core.model.vars.FolderVarContainer;
import ru.dip.core.model.vars.IVarContainer;
import ru.dip.core.model.vars.ProjectVarContainer;
import ru.dip.core.model.vars.VarContainer;

public class DipFolder extends DipTableContainer implements IDipParent {
	
	public static DipFolder instance(IFolder container, IParent parent) {
		IDipElement element = DipRoot.getInstance().getElement(container, parent, DipElementType.FOLDER);
		if (element == null) {
			DipFolder folder = null;
			if (Appendix.isAppendix(parent, container.getName())) {
				folder = new Appendix(container, parent);
			} else {
				folder = new DipFolder(container, parent);
			}
			DipRoot.getInstance().putElement(folder);
			return folder;
		} else {
			return (DipFolder) element;
		}
	}
	
	private GlossaryFolder fGlossFolder;
	private FolderVarContainer fVarContainer;
	protected FolderReportContainer fReportContainer;
	
	protected DipFolder(IFolder container, IParent parent) {
		super(container, parent);
	}
	
	//======================
	// children
	
	@Override
	public IDipElement getChild(String name) {		
		return super.getChild(name);
	}
	
	@Override
	public void computeChildren(){
		fChildren = new ArrayList<>();
		if (resource() == null) {
			return;
		}
		clearChildren();
		createChildrenElements();
		createTable();
		setDipDocElementsChildren(computeDipChildren());
		updateChildren();
	}
	
	private void clearChildren() {
		// удалить контейнер с переменными из главного контейнера (чтобы потом не было дублирования)
		if (fVarContainer != null) {
			ProjectVarContainer main = dipProject().getVariablesContainer();
			main.removeContainer(fVarContainer.getOriginalVarContainer());
			fVarContainer = null;
		}
		if (fReportContainer != null) {
			IMainReportContainer mainReportFolder = dipProject().getOrCreateReportContainer();
			mainReportFolder.removeContainer(fReportContainer.getOriginalReportContainer());
			fReportContainer = null;
		}
		setDipDocElementsChildren(new ArrayList<>());
	}
	
	private void createChildrenElements() {
		try {
			for (IResource resource: resource().members()){
				DipElementType type = DipRoot.getType(resource);
				switch (type){
				case RESERVED_FOLDER:{
					createReservedFolder((IFolder) resource);
					break;
				}
				case INCLUDE_FOLDER:{
					createIncludeFolder((IFolder)resource);
					break;
				}				
				case FOLDER:{
					createFolder((IFolder) resource);
					break;
				}
				case SERV_FOLDER:{
					break;
				}
				case BROKEN_FOLDER:{
					break;
				}
				case EXPORT_CONFIG:{
					break;
				}
				case RESERVED_MARKER:{
					createReservedMarker((IFile) resource);
					break;
				}
				case RESERVED_UNIT:{
					createReservedUnit((IFile) resource);
					break;
				}				
				case UNIT:{
					createUnit((IFile) resource);
					break;
				}				
				case GLOSS_REF:{
					createGlossRef((IFile) resource);
					break;
				}
				case TOC_REF:{
					createTocRef((IFile) resource);
					break;
				}
				case CHANGE_LOG:{
					createChangeLog((IFile) resource);
					break;					
				}				
				case COMMENT:{
					createDipComment((IFile) resource);
					break;
				}		
				case DESCRIPTION:{		
					createDipDescription((IFile) resource);
					break;
				}
				case FOLDER_COMMENT:{
					createFolderDipComment((IFile) resource);
					break;
				}				
				case GLOSSARY_FOLDER:{
					createGlossaryFolder((IFile) resource);
					break;
				}
				case VARIABLES_CONTAINER:{
					createNewVarContainer((IFile) resource);
					break;
				}
				case REPORT_FOLDER:{
					createReportFolder((IFolder) resource);
					break;
				}								
				case REPORT:{
					IReportContainer container = getOrCreateReportContainer();
					container.addReportChild((IFile) resource);
					break;
				}					
				default:
					break;
				}
			}
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Папка с отчетами Reports (физическая) - для обратной совместимость, актуально только для IncludeFolder
	 */
	protected void createReportFolder(IFolder folder) {}
	
	@Override
	protected void checkUpdate(List<IDipDocumentElement> dipChildren) {
		if (fNeedUpdate) {
			updateFolderChildren(dipChildren);
			fNeedUpdate = false;
		}
	}
	
	private void updateFolderChildren(List<IDipDocumentElement> dipChildren) {
		for (IDipDocumentElement dipDocumentElement: dipChildren) {
			if (dipDocumentElement instanceof DipTableContainer) {							
				((DipTableContainer) dipDocumentElement).computeChildren();
			}			
		}
	}
	
	@Override
	protected List<IDipDocumentElement> computeDipChildren() {		
		List<IDipDocumentElement> result =  super.computeDipChildren();
		if (isIncluded()) {
			boolean readOnly = isReadOnly();
			result.forEach(it -> {
				it.setIncluded(true);
				it.setReadOnly(readOnly);
			});
		}		
		return result;
	}
	
	/**
	 *  Включая те директории, которых нет в .dnfo
	 */
	public  void computeAllChildren() {
		fChildren = new ArrayList<>();
		setDipDocElementsChildren(new ArrayList<>());
		createChildrenElements();
		createTable();
		setDipDocElementsChildren(computeAllDipChildren());
		updateAllChildren(getDipDocumentChildren());
	}
	
	/**
	 *  Включая те директории, которых нет в .dnfo
	 */
	protected List<IDipDocumentElement> computeAllDipChildren() {		
		List<IDipDocumentElement> result =  super.computeDipChildren(true);
		if (isIncluded()) {
			boolean readOnly = isReadOnly();
			result.forEach(it -> {
				it.setIncluded(true);
				it.setReadOnly(readOnly);
			});
		}		
		return result;
	}
	
	/**
	 *  Включая те директории, которых нет в .dnfo
	 */
	private void updateAllChildren(List<IDipDocumentElement> dipChildren) {
		for (IDipDocumentElement dipDocumentElement: dipChildren) {
			if (dipDocumentElement instanceof DipFolder) {							
				((DipFolder) dipDocumentElement).computeAllChildren();
			}
		}
	}
	
	@Override
	public List<IDipElement> getChildren() {
		if (fChildren == null){
			computeChildren();
		}		
		return fChildren;
	}
	
	@Override
	public boolean isRoot() {
		return false;
	}
	
	public void clearWhenDeleting() {
		removeGlossary();
		removeVarContainer();
		removeReportContainer();
	}
	
	//=============================
	// glossary
	
	private IDipElement createGlossaryFolder(IFile file) {	
		ProjectGlossaryFolder main = dipProject().getGlossaryFolder();
		fGlossFolder =  new GlossaryFolder(file, null);
		if (main != null) {
			main.addFolder(fGlossFolder);
		}
		return null;
	}
	
	/**
	 * Удаляет термины из глобального глоссария (рекурсивно) (при удалении папки) 
	 */
	private void removeGlossary() {
		// удаляем термины из глоссария
		if (fGlossFolder != null) {
			dipProject().getGlossaryFolder().removeFolder(fGlossFolder);
		} 
		getDipDocumentChildren()
		.stream()
		.filter(DipFolder.class::isInstance)
		.map(DipFolder.class::cast)
		.forEach(DipFolder::removeGlossary);
	}
	
	//===========================		
	// reports
	
	public IReportContainer getOrCreateReportContainer() {
		if (fReportContainer != null) {
			return fReportContainer;
		}
		
		ReportContainer reportContainer = createReportsContainer();
		fReportContainer = new FolderReportContainer(reportContainer);		
		int index = fVarContainer == null ? 0 : 1;		
		fChildren.add(index, fReportContainer);
		return fReportContainer;
	}
	
	private ReportContainer createReportsContainer() {	
		IMainReportContainer main = dipProject().getOrCreateReportContainer();
		ReportContainer reportContainer =  new ReportContainer(this);
		main.addContainer(reportContainer);
		return reportContainer;
	}
	
	@Override
	public IReportContainer getReportContainer() {
		return fReportContainer;
	}
	
	/**
	 * Удаляет переменные рекурсивно (при удалении папки) 
	 */
	private void removeReportContainer() {
		// удаляем термины из глоссария
		if (fReportContainer != null) {
			dipProject().getReportFolder().removeContainer(fReportContainer.getOriginalReportContainer());
			fReportContainer = null;
		} 
		getDipDocumentChildren()
		.stream()
		.filter(DipFolder.class::isInstance)
		.map(DipFolder.class::cast)
		.forEach(DipFolder::removeReportContainer);
	}
	
	//===========================
	// variables
	
	public IVarContainer createNewVarContainer(IFile file) {
		VarContainer varContainer = createVarContainer(file);
		fVarContainer = new FolderVarContainer(varContainer);
		fChildren.add(0, fVarContainer);
		return fVarContainer;
	}
	
	private VarContainer createVarContainer(IFile file) {	
		ProjectVarContainer main = dipProject().getVariablesContainer();
		VarContainer varContainer =  new VarContainer(file, this);
		if (main != null) {
			main.addContainer(varContainer);
		}
		return varContainer;
	}
	
	@Override
	public void deleteVarContainer() {
		fChildren.remove(fVarContainer);
		dipProject().getVariablesContainer().removeContainer(fVarContainer.getOriginalVarContainer());		
		fVarContainer = null;
	}
	
	@Override
	public IVarContainer getVariablesContainer() {
		return fVarContainer;
	}
		
	/**
	 * Удаляет переменные рекурсивно (при удалении папки) 
	 */
	private void removeVarContainer() {
		// удаляем термины из глоссария
		if (fVarContainer != null) {
			dipProject().getVariablesContainer().removeContainer(fVarContainer.getOriginalVarContainer());
			fVarContainer = null;
		} 
		getDipDocumentChildren()
		.stream()
		.filter(DipFolder.class::isInstance)
		.map(DipFolder.class::cast)
		.forEach(DipFolder::removeVarContainer);
	}
	
	//===========================
	// getters & setters
	
	@Override
	public IFolder resource() {
		return (IFolder) super.resource();
	}

	@Override
	public DipElementType type() {
		return DipElementType.FOLDER;
	}

	@Override
	public IDipDocumentElement strong() {
		return this;
	}
}
