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
package ru.dip.ui.navigator;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;

import ru.dip.core.model.DipElementType;
import ru.dip.core.model.DipProject;
import ru.dip.core.model.interfaces.IDipElement;
import ru.dip.core.model.interfaces.IDipParent;
import ru.dip.core.model.reports.IReportContainer;
import ru.dip.core.model.vars.IVarContainer;

@SuppressWarnings("deprecation")
public class NavigatorViewerSorter extends ViewerSorter {

	public NavigatorViewerSorter() {
	}
	
	@Override
	public int compare(Viewer viewer, Object e1, Object e2) {
		if (e1 instanceof DipProject && e2 instanceof DipProject) {
			return ((DipProject) e1).name().compareTo(((DipProject) e2).name());			
		} 	
		if (e1 instanceof IProject && e2 instanceof IProject) {
			return ((IProject) e1).getName().compareTo(((IProject) e2).getName());
		} 		
		if (e1 instanceof DipProject && e2 instanceof IProject) {
			return ((DipProject) e1).name().compareTo(((IProject) e2).getName());
		} 	
		if (e1 instanceof IProject && e2 instanceof DipProject) {
			return ((IProject) e1).getName().compareTo(((DipProject) e2).name());
		} 
		
		if (e1 instanceof IDipElement && e2 instanceof IDipElement){
			IDipElement element1 = (IDipElement) e1;
			IDipElement element2 = (IDipElement) e2;
			DipElementType type1 = element1.type();
			DipElementType type2 = element2.type();
			if (type1 == type2 
					|| (DipElementType.isFolderType(element1) 
							&& DipElementType.isFolderType(element2))){
				return compareOneType(element1, element2);
			}
			if (type1 == DipElementType.SCHEMA_FOLDER){
				return -1;
			}
			if (type2 == DipElementType.SCHEMA_FOLDER){
				return 1;
			}
			if (type1 == DipElementType.REPORT){
				return -1;
			}
			if (type2 == DipElementType.REPORT){
				return 1;
			}
			if (type1 == DipElementType.REPORT_FOLDER){
				return -1;
			}
			if (type2 == DipElementType.REPORT_FOLDER){
				return 1;
			}
			if (type1 == DipElementType.VARIABLE) {
				return -1;
			}
			if (type2 == DipElementType.VARIABLE) {
				return 1;
			}
			if (type1 == DipElementType.VARIABLES_CONTAINER) {
				return -1;
			}
			if (type2 == DipElementType.VARIABLES_CONTAINER) {
				return 1;
			}
			if (type1 == DipElementType.GLOSSARY_FOLDER){
				return - 1;
			}
			if (type2 == DipElementType.GLOSSARY_FOLDER){
				return 1;
			}
			
			if (type1 == DipElementType.UNIT){
				return -1;
			}
			if (type2 == DipElementType.UNIT){
				return 1;
			}
			if (type1 == DipElementType.FOLDER || type1 == DipElementType.INCLUDE_FOLDER){
				return -1;
			}
			if (type2 == DipElementType.FOLDER || type2 == DipElementType.INCLUDE_FOLDER){
				return 1;
			}
			if (type1 == DipElementType.RESERVED_MARKER){
				return -1;
			}
			if (type2 == DipElementType.RESERVED_MARKER){
				return 1;
			}
			if (type1 == DipElementType.RESERVED_UNIT){
				return -1;
			}
			if (type2 == DipElementType.RESERVED_UNIT){
				return 1;
			}
			if (type1 == DipElementType.RESERVED_FOLDER){
				return -1;
			}
			if (type2 == DipElementType.RESERVED_FOLDER){
				return 1;
			}
			if (type1 == DipElementType.TABLE || type1 == DipElementType.SCHEMA_LIST){
				return 1;
			}
			if (type2 == DipElementType.TABLE || type2 == DipElementType.SCHEMA_LIST){
				return -1;
			}
		} else if (e1 instanceof IDipElement && e2 instanceof IFolder) {
			return -1;
		} else if (e2 instanceof IDipElement && e1 instanceof IFolder) {
			return 1;
		}
		return super.compare(viewer, e1, e2);
	}
	
	
	private int compareOneType(IDipElement element1, IDipElement element2){		
		DipElementType type = element1.type();
		if (type == DipElementType.REPORT_FOLDER) {			
			String path1 = ((IReportContainer) element1).getRelativePath();
			String path2 = ((IReportContainer) element2).getRelativePath();
			return path1.compareTo(path2);
		}
		if (type == DipElementType.VARIABLES_CONTAINER) {
			String path1 = ((IVarContainer) element1).getRelativePath();
			String path2 = ((IVarContainer) element2).getRelativePath();
			return path1.compareTo(path2);
		}		
		if (type == DipElementType.UNIT || DipElementType.isFolderType(type)){
			IDipParent parent = (IDipParent) element1.parent();
			int index1 = parent.getDipDocChildrenList().indexOf(element1);
			int index2 = parent.getDipDocChildrenList().indexOf(element2);
			return index1 - index2;					
		}
		return element1.name().compareTo(element2.name()); 
	}

}
