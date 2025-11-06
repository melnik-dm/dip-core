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
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;

import ru.dip.core.model.DipFolder;
import ru.dip.core.model.interfaces.IDipParent;
import ru.dip.core.model.vars.IVarContainer;
import ru.dip.core.model.vars.VarContainer;
import ru.dip.core.utilities.ResourcesUtilities;
import ru.dip.core.utilities.WorkbenchUtitlities;

public class CreateVarContainerHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		Object obj = HandlerUtil.getCurrentStructuredSelection(event).getFirstElement();
		if (obj instanceof DipFolder) {
			DipFolder folder = (DipFolder) obj;			
			try {
				IFile file = folder.resource().getFile(VarContainer.VAR_FILE);
				ResourcesUtilities.createFile(file, HandlerUtil.getActiveShell(event));
				IVarContainer varContainer = folder.createNewVarContainer(file);
				WorkbenchUtitlities.updateProjectExplorer();
				WorkbenchUtitlities.selectAndReveal(varContainer);
			} catch (CoreException e) {
				e.printStackTrace();
			}
		}
		return null;
	}
	
	@Override
	public boolean isEnabled() {
		ISelection selection = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getSelectionService().getSelection();
		if (selection instanceof IStructuredSelection) {
			IStructuredSelection stucturedSelection = (IStructuredSelection) selection;
			if (!stucturedSelection.isEmpty()) {
				Object obj = stucturedSelection.getFirstElement();
				if (obj instanceof IDipParent) {
					IDipParent parent = (IDipParent) obj;
					return parent.getVariablesContainer() == null;
				}
			}
		}
		return false;
	}

}
