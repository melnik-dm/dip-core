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
package ru.dip.ui.variable.view;

import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartReference;

import ru.dip.ui.table.editor.DipTableEditor;

public class VariableWorkbenchListener implements IPartListener2 {

	@Override
	public void partVisible(IWorkbenchPartReference partRef) {
		IWorkbenchPart part = partRef.getPart(false);
		if (part instanceof VariablesView) {
			VariablesView tdView = (VariablesView) part;
			tdView.update();
		} else if (part instanceof DipTableEditor) {
			DipTableEditor tableEditor = (DipTableEditor) part;
			VariablesView variablesView = getDipVariablesView(partRef);
			if (variablesView != null) {
				variablesView.update(tableEditor);
			}
		} else if (part instanceof IEditorPart) {
			VariablesView variablesView = getDipVariablesView(partRef);
			if (variablesView != null) {
				variablesView.update();
			}
		}
	}

	public VariablesView getDipVariablesView(IWorkbenchPartReference partRef) {
		return (VariablesView) partRef.getPage().findView(VariablesView.ID);
	}

	@Override
	public void partActivated(IWorkbenchPartReference partRef) {
	}

	@Override
	public void partOpened(IWorkbenchPartReference partRef) {
	}

	@Override
	public void partBroughtToTop(IWorkbenchPartReference partRef) {
	}

	@Override
	public void partClosed(IWorkbenchPartReference partRef) {
	}

	@Override
	public void partDeactivated(IWorkbenchPartReference partRef) {
	}

	@Override
	public void partHidden(IWorkbenchPartReference partRef) {
	}

	@Override
	public void partInputChanged(IWorkbenchPartReference partRef) {
	}

}