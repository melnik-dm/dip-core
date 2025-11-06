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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.ui.model.WorkbenchContentProvider;

import ru.dip.core.manager.DipNatureManager;
import ru.dip.core.model.DipProject;
import ru.dip.core.model.DipRoot;
import ru.dip.core.model.DipSchemaFolder;
import ru.dip.core.model.interfaces.IDipDocumentElement;
import ru.dip.core.model.interfaces.IDipElement;
import ru.dip.core.model.interfaces.IDipParent;
import ru.dip.core.model.interfaces.IParent;
import ru.dip.core.model.reports.ProjectReportFolder;
import ru.dip.core.utilities.DipUtilities;

public class NavigatorContentProvider implements ITreeContentProvider {
		
	public static final WorkbenchContentProvider WorkBenchContentProvider = new WorkbenchContentProvider();
	
	@Override
	public Object[] getElements(Object inputElement) {
		if (inputElement instanceof IWorkspaceRoot){
			IWorkspaceRoot root = (IWorkspaceRoot) inputElement;
			IProject[] projects =  root.getProjects();
			Object[] result = new Object[projects.length];
			for (int i = 0; i < result.length; i++){
				if (DipNatureManager.hasNature(projects[i])){
					result[i] = DipRoot.getInstance().getDipProject(projects[i]);
				} else {
					result[i] = projects[i];
				}
			}
			return result;
		} else if (inputElement instanceof DipProject){
			return getChildren(inputElement);
		}
		return null;
	}

	@Override
	public Object[] getChildren(Object parentElement) {
		if (parentElement instanceof IParent){	
			IParent parent = (IParent) parentElement;
			List<?> children = parent.getChildren();
			List<Object> result =  new ArrayList<>();
			result.addAll(children);
			if (parent instanceof IDipParent) {
				// добавление сервисных директорий (без .dnfo и которые начинаются с __ )
				IContainer container = (IContainer) parent.resource();
				if (container.exists()) {
					try {
						for (IResource res : container.members()) {							
							if (res instanceof IFolder) {
								IFolder folder = (IFolder) res;
								if (DipUtilities.isServedFolder(folder.getName())){
									result.add(res);
								} else if (isBrokenFolder(folder, (IDipParent) parent)) {
									result.add(res);
								}
							}
						}
					} catch (CoreException e) {
						e.printStackTrace();
					}			
				}
			}
			return result.toArray();					
		}
		if (parentElement instanceof IResource) {
			if (DipNatureManager.hasNature((IResource) parentElement)){
				return WorkBenchContentProvider.getChildren(parentElement);
			}
		}		
		return null;
	}
	
	private boolean isBrokenFolder(IFolder folder, IDipParent parent) {
		String folderName = folder.getName();
		if (parent instanceof DipProject) {			
			String name = folderName;			
			if (DipSchemaFolder.SCHEMA_FOLDER_NAME.equals(name)){
				return false;
			} else if (ProjectReportFolder.REPORT_FOLDER_NAME.equals(name)){
				return false;
			} 						
		}	
		// если нет в ReqChildren
		for (IDipDocumentElement dipDocElement: parent.getDipDocChildrenList()) {
			if (Objects.equals(dipDocElement.name(), folderName)) {
				return false;
			}
		}
		return true;
	}
	
	@Override
	public Object getParent(Object element) {
		if (element instanceof DipProject){
			return ResourcesPlugin.getWorkspace().getRoot();
		}
		if (element instanceof IDipElement){
			return ((IDipElement) element).parent();
		}
		return null;
	}

	@Override
	public boolean hasChildren(Object element) {
		if (element instanceof IParent){
			return ((IParent) element).hasChildren();
		}				
		if (element instanceof IResource) {
			if (DipNatureManager.hasNature((IResource) element)){
				return WorkBenchContentProvider.hasChildren(element);
			}
		}
		return false;
	}

}
