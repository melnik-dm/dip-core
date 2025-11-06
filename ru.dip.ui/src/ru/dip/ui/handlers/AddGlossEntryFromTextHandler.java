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
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import ru.dip.core.manager.DipNatureManager;
import ru.dip.core.model.DipProject;
import ru.dip.core.model.DipRoot;
import ru.dip.core.model.glossary.GlossaryField;
import ru.dip.core.model.glossary.GlossaryFolder;
import ru.dip.core.utilities.WorkbenchUtitlities;
import ru.dip.ui.glossary.NewGlossFieldDialog;

public class AddGlossEntryFromTextHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		ISelection selection = window.getSelectionService()
				.getSelection();
		if (selection instanceof TextSelection) {
			String text = ((TextSelection) selection).getText();
			if (text == null || text.isEmpty() || text.length() > GlossaryField.NAME_MAX_LENGTH){
				return null;
			}						
			IWorkbenchPart part = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActivePart();
			IFile file = WorkbenchUtitlities.getFileFromOpenedEditor(part);
			if (file == null) {
				return null;
			}	
			if (!DipNatureManager.hasNature(file)) {
				return null;
			}
			DipProject dipProject = DipRoot.getInstance().getDipProject(file.getProject());
			GlossaryFolder glossFolder = dipProject.getGlossaryFolder();
			NewGlossFieldDialog dialog = new NewGlossFieldDialog(window.getShell(), glossFolder, text);
			dialog.open();			
		}
		return null;
	}

}
