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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;

import ru.dip.core.model.glossary.GlossaryFolder;
import ru.dip.core.model.interfaces.IDipElement;
import ru.dip.core.model.interfaces.IParent;
import ru.dip.core.model.reports.ProjectReportFolder;
import ru.dip.core.model.reports.Report;
import ru.dip.core.model.vars.VarContainer;
import ru.dip.core.unit.ChangeLogPresentation;
import ru.dip.core.unit.GlossaryPresentation;
import ru.dip.core.unit.TocRefPresentation;
import ru.dip.core.unit.UnitType;
import ru.dip.core.utilities.DipUtilities;
import ru.dip.core.utilities.ResourcesUtilities;

public class DipRoot {
	
	private static DipRoot fInstance;
	
	public static boolean externalVersion = false;
	
	public static DipRoot getInstance(){
		if (fInstance == null){
			if (!externalVersion) {
				ResourcesUtilities.updateRoot();
			}
			fInstance = new DipRoot();
		}
		return fInstance;
	}
	
	private DipRoot(){}
	
	private Map<IProject, DipProject> fProjects = new HashMap<>();
	private Map<String, IDipElement> fElements = new HashMap<>();
		
	public DipProject getDipProject(IProject project){
		DipProject dipProject = fInstance.fProjects.get(project);
		if (dipProject == null){
			dipProject = createDipProject(project);
			fInstance.fProjects.put(project, dipProject);
		}		
		return dipProject;
	}
	
	public DipProject findDipProject(IProject project){
		return fProjects.get(project);
	}
	
	private DipProject createDipProject(IProject project){
		return DipProject.instance(project);
	}
	
	public void updateProjectInCache(DipProject dipProject){
		if (dipProject != null){
			IProject project = dipProject.getProject();
			DipRoot.getInstance().removeProject(dipProject);
			DipRoot.getInstance().getDipProject(project);
		}
	}
	
	public void removeProject(DipProject project){
		fProjects.remove(project.getProject());
	}
	
	public static DipElementType getType(IResource resource){
		if  (resource instanceof IFolder){
			return getType((IFolder) resource);
		} else {
			return getType((IFile) resource);
		}
	}
	
	public static DipElementType getType(IFolder folder){
		String name = folder.getName();
		if (folder.isLinked()) {
			return DipElementType.INCLUDE_FOLDER;
		} else if (DipSchemaFolder.SCHEMA_FOLDER_NAME.equals(name)){
			return DipElementType.SCHEMA_FOLDER;
		} else if (DipUtilities.isServedFolder(name)) {
			return DipElementType.SERV_FOLDER;
		} else if (isReservedFolder(folder)){
			return DipElementType.RESERVED_FOLDER;
		} else if (ProjectReportFolder.REPORT_FOLDER_NAME.equals(name)){
			if (folder.getParent() instanceof IProject || folder.getParent().isLinked()) {
				return DipElementType.REPORT_FOLDER;
			} else {
				return DipElementType.FOLDER;
			}
		} else if (DipUtilities.isNotDnfo(folder)) {
			return DipElementType.BROKEN_FOLDER;
		} else {
			return DipElementType.FOLDER;
		}
	}
	
	public static boolean isReservedFolder(IFolder folder) {
		IFile file = folder.getFile(".rsvd");
		return file.exists();
	}
	
	public static DipElementType getType(IFile file){
		String fileName = file.getName();
		if (DipReservedMarker.RESERVED_MARKER_NAME.equals(fileName)){
			return DipElementType.RESERVED_MARKER;
		}		
		if (DipFolderComment.FILE_NAME.equals(fileName)){
			return DipElementType.FOLDER_COMMENT;
		}		
		String extension = file.getFileExtension();
		if (DipReservedUnit.EXTENSION.equals(extension)){
			return DipElementType.RESERVED_UNIT;
		}
		if (DnfoTable.TABLE_FILE_NAME.equals(fileName)){
			return DipElementType.TABLE;
		}
		if (GlossaryFolder.GLOS_FILE.equals(fileName)){
			return DipElementType.GLOSSARY_FOLDER;
		}					
		if (GlossaryPresentation.FILE_NAME.equals(fileName)){
			return DipElementType.GLOSS_REF;
		}
		if (VarContainer.VAR_FILE.equals(fileName)) {
			return DipElementType.VARIABLES_CONTAINER;
		}
		if (TocRefPresentation.FILE_NAME.equals(fileName)) {
			return DipElementType.TOC_REF;
		}		
		if (UnitType.isChangeLog(fileName)) {
			return DipElementType.CHANGE_LOG;
		}
		if (Report.REPORT_EXTENSION.equals(extension)){
			return DipElementType.REPORT;
		}
		if (fileName.startsWith(".")){
			if (fileName.equals(GlossaryPresentation.FILE_NAME + ".r")
				|| fileName.equals(ChangeLogPresentation.CHANGE_LOG_NAME + ".r")
				|| fileName.equals(ChangeLogPresentation.CHANGE_LOG_REF_NAME + ".r")
				|| fileName.equals(TocRefPresentation.FILE_NAME + ".r")) {
				return DipElementType.COMMENT;
			}
			if (fileName.equals(GlossaryPresentation.FILE_NAME + ".d")
					|| fileName.equals(ChangeLogPresentation.CHANGE_LOG_NAME + ".d")
					|| fileName.equals(ChangeLogPresentation.CHANGE_LOG_REF_NAME + ".d")
					|| fileName.equals(TocRefPresentation.FILE_NAME + ".d")) {
					return DipElementType.DESCRIPTION;
				}
			return DipElementType.UNDEFINE;
		}
		if ("dip".equals(extension)) {
			return DipElementType.UNDEFINE;
		}		
		if (DipComment.EXTENSION.equals(extension)){
			return DipElementType.COMMENT;
		}		
		if (DipDescription.EXTENSION.equals(extension)){
			return DipElementType.DESCRIPTION;
		}	
		if (ExportConfig.EXTENSION.equals(extension)){
			return DipElementType.EXPORT_CONFIG;
		}		
		return DipElementType.UNIT;
	}
	
	public IDipElement getElement(IResource resource, IParent parent, DipElementType type) {
		String id = mapID(resource, parent);
		IDipElement element = getElement(id, type);
		
		// вот тут надо подумать
		if (element != null && element.parent() != parent) {
			return null;			
		}		
		
		return element;
	}
		
	private IDipElement getElement(String id, DipElementType type) {
		IDipElement element = fElements.get(id);				
		if (element != null) {
			if (element.type() == type) {
				return element;
			} else {
				return null;
			}
		}
		return null;		
	}
	
	public void putElement(IDipElement element) {
		String id = mapID(element);
		IDipElement oldElement = fElements.get(id);
		if (oldElement != null) {
			oldElement.dispose();
		}	
		fElements.put(id, element);
	}
	
	public void removeElement(IDipElement element) {
		String id = mapID(element);
		IDipElement oldElement = fElements.get(id);
		if (oldElement != null) {
			oldElement.dispose();
		}
		fElements.remove(id);
		
		// надо подумать, как это будет работать с инклюдами
		if (element instanceof IParent) {
			((IParent) element).getChildren().forEach(this::removeElement);
		}		
	}
	
	public String mapID(IResource resource, IParent parent) {
		if (resource instanceof IProject) {
			return resource.getName();
		}
		return resource.getName() + "/" + mapID(parent);
	}
	
	public String mapID(IDipElement element) {			
		StringBuilder builder = new StringBuilder();		
		while (element != null && !(element instanceof DipProject) && element.parent() != null) {
			builder.append(element.name());
			builder.append("/");
			element = element.parent();
		}
		builder.append(element.name());
		return builder.toString();
	}	
	
	public void updateID(IDipElement element, String oldID) {
		String newID = mapID(element);
		fElements.remove(oldID);
		fElements.put(newID, element);
	}
	
	public void clear() {
		fProjects.clear();
		fElements.values().forEach(IDipElement::dispose);
		fElements.clear();
	}
	
	public Collection<DipProject> getProjects() {
		return fProjects.values();
	}

}
