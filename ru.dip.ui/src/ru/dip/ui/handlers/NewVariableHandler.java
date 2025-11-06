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
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.handlers.HandlerUtil;

import ru.dip.core.model.vars.IVarContainer;
import ru.dip.core.model.vars.Variable;
import ru.dip.ui.variable.VarManager;

public class NewVariableHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IStructuredSelection selection = HandlerUtil.getCurrentStructuredSelection(event);
		Object firstElement = selection.getFirstElement();
		IVarContainer varContainer = null;
		if (firstElement instanceof IVarContainer) {
			varContainer = (IVarContainer) firstElement;
		} else if (firstElement instanceof Variable) {
			varContainer = ((Variable) firstElement).parent();
		}
		if (varContainer != null && varContainer.canDelete()) {
			VarManager.openNewVariableDialog(HandlerUtil.getActiveShell(event), varContainer);
		}
		return null;
	}
}
