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

import java.io.IOException;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.handlers.HandlerUtil;

import ru.dip.core.model.DipSchemaElement;
import ru.dip.core.model.DipSchemaFolder;
import ru.dip.core.utilities.ResourcesUtilities;
import ru.dip.core.utilities.SchemaUtilities;
import ru.dip.core.utilities.WorkbenchUtitlities;
import ru.dip.ui.dialog.NewSchemaDialog;

public class NewSchemaHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		Shell shell = HandlerUtil.getActiveShell(event);
		Object firstElement = HandlerUtil.getCurrentStructuredSelection(event).getFirstElement();
		if (firstElement instanceof DipSchemaFolder) {
			DipSchemaFolder dipSchemaFolder = (DipSchemaFolder) firstElement;
			NewSchemaDialog dialog = new NewSchemaDialog(shell, dipSchemaFolder);
			if (dialog.open() == Window.OK) {
				String newExtension = dialog.getExtension();
				createSchema(dipSchemaFolder, newExtension, shell);
			}
		}
		return null;
	}

	private void createSchema(DipSchemaFolder dipSchemaFolder, String newExtension, Shell shell) {
		try {
			// create file
			IFile file = ResourcesUtilities.createFile(dipSchemaFolder.resource(), newExtension + ".xml", shell);
			SchemaUtilities.writeNewScema(file);
			// add to model
			DipSchemaElement formSchema = dipSchemaFolder.createSchema(file);
			// update-select-open
			WorkbenchUtitlities.updateProjectExplorer(dipSchemaFolder);
			WorkbenchUtitlities.selectAndReveal(formSchema);
			WorkbenchUtitlities.openFile(file);
		} catch (CoreException | IOException e) {
			e.printStackTrace();
			WorkbenchUtitlities.openError("Create Schema Error", e.getMessage());
		}
	}
}
