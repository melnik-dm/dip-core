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
package ru.dip.core.utilities.md;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.Path;
import org.eclipse.ui.IElementFactory;
import org.eclipse.ui.IMemento;

import ru.dip.core.manager.DipNatureManager;

public class UnityMdEditorFactory implements IElementFactory {

	public static final String ID_FACTORY =  "ru.dip.input.mdunity.factory";
	private static final String PROJECT_NAME = "porject";
	private static final String FOLDER_PATH = "folder_path";
	
	@Override
	public IAdaptable createElement(IMemento memento) {
		IContainer container = getContainer(memento);
		if (container != null){
			if (DipNatureManager.hasNature(container)){				
				UnityMdInput input = new UnityMdInput(container);
				return input;					
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
 
    public static void saveState(IMemento memento, UnityMdInput input) {
        IContainer container = input.getContainer();
        String projectName = container.getProject().getName();
        memento.putString(PROJECT_NAME, projectName);
        if (container instanceof IFolder){
        	memento.putString(FOLDER_PATH, container.getProjectRelativePath().toOSString());
        }
    }

}
