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
package ru.dip.ui.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.handlers.HandlerUtil;

import ru.dip.core.manager.DipNatureManager;
import ru.dip.core.manager.DipProjectResourceCreator;
import ru.dip.core.model.DipProject;
import ru.dip.core.model.DipRoot;
import ru.dip.core.utilities.HandlerUtilities;
import ru.dip.core.utilities.ResourcesUtilities;
import ru.dip.core.utilities.SchemaUtilities;
import ru.dip.core.utilities.WorkbenchUtitlities;

public class ConvertToDipHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IProject project = HandlerUtilities.getProject(event);
		if (project == null) {
			return null;
		}		
		Shell shell = HandlerUtil.getActiveShell(event);
		DipProject dipProject = convertToDipProject(project, shell);
		WorkbenchUtitlities.selectInProjectExploter(dipProject);
		return null;
	}

	private DipProject convertToDipProject(IProject project, Shell shell){
		DipProject dipProject = DipRoot.getInstance().getDipProject(project);
		DipProjectResourceCreator.checkSchemaFolder(shell, dipProject);
		DipProjectResourceCreator.createDipFile(dipProject);
		dipProject.computeChildren();
		DipNatureManager.checkNature(dipProject);
		ResourcesUtilities.updateProject(project);
		SchemaUtilities.updateProperties(dipProject);
		return dipProject;
	}
	
}
