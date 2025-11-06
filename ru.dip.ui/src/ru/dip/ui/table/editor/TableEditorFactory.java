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
package ru.dip.ui.table.editor;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.Path;
import org.eclipse.ui.IElementFactory;
import org.eclipse.ui.IMemento;

import ru.dip.core.manager.DipNatureManager;
import ru.dip.core.model.DipProject;
import ru.dip.core.model.DipRoot;
import ru.dip.core.model.DnfoTable;
import ru.dip.core.model.DipTableContainer;
import ru.dip.core.model.interfaces.IDipElement;
import ru.dip.core.utilities.DipUtilities;
import ru.dip.core.utilities.WorkbenchUtitlities;

public class TableEditorFactory implements IElementFactory {

	public static final String ID_FACTORY =  "ru.dip.table.factory";
	private static final String PROJECT_NAME = "porject";
	private static final String FOLDER_PATH = "folder_path";
	
	@Override
	public IAdaptable createElement(IMemento memento) {
		IContainer container = getContainer(memento);
		if (container != null){
			if (DipNatureManager.hasNature(container)){
				DipProject project = DipRoot.getInstance().getDipProject(container.getProject());
				IDipElement element = DipUtilities.findDipElementInProject(container, project);
				if (element instanceof DipTableContainer) {								
					DnfoTable table = ((DipTableContainer) element).getTable();
					if (table == null) {
						// в режиме maximize - не успевает создаться модель
						WorkbenchUtitlities.updateRoot();
						table = ((DipTableContainer) element).getTable();
					}
					TableEditorInput editorInput = new TableEditorInput(table);
	    			return editorInput;
				} else if (element instanceof DnfoTable) {
					TableEditorInput editorInput = new TableEditorInput((DnfoTable) element);
	    			return editorInput;

				}			
			}
		}	
		return null;
	}

	private IContainer getContainer(IMemento memento) {
		String projectName = memento.getString(PROJECT_NAME);
		if (projectName != null) {
			IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
			if (project != null && project.exists()) {
            	String containerPath = memento.getString(FOLDER_PATH);
            	if (containerPath != null){
            		IFolder folder = project.getFolder(new Path(containerPath));
            		if (folder != null && folder.exists()){
            			return folder;
            		}            		            		
            	} else {
            		return project;
            	}
			}
		}
		return null;
	}	
	
    public static String getFactoryId() {
        return ID_FACTORY;
    }
 
    public static void saveState(IMemento memento, TableEditorInput input) {
        IContainer container = input.getContainer();
        String projectName = container.getProject().getName();
        memento.putString(PROJECT_NAME, projectName);
        if (container instanceof IFolder){
        	memento.putString(FOLDER_PATH, container.getProjectRelativePath().toOSString());
        }
    }

}
