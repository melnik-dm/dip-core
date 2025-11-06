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

import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;

import ru.dip.core.model.glossary.GlossaryField;
import ru.dip.core.utilities.WorkbenchUtitlities;
import ru.dip.ui.glossary.EditGlossFieldDialog;

public class GlossEditHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
		ISelection selection = window.getSelectionService().getSelection();
		if (selection instanceof IStructuredSelection) {
			IStructuredSelection structuredSelection = (IStructuredSelection) selection;
			Object firstElement = structuredSelection.getFirstElement();
			if (firstElement instanceof GlossaryField) {
				GlossaryField field = (GlossaryField) firstElement;
				EditGlossFieldDialog dialog = new EditGlossFieldDialog(window.getShell(), field);
				dialog.open();
			}
		} else if (selection instanceof TextSelection) {
			GlossaryField field = WorkbenchUtitlities.getField((TextSelection) selection);
			if (field != null){
				EditGlossFieldDialog dialog = new EditGlossFieldDialog(window.getShell(), field);
				dialog.open();
			}			
		}
		return null;
	}

}
