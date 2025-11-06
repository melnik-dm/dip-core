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

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.actions.ActionContext;
import org.eclipse.ui.navigator.CommonActionProvider;
import org.eclipse.ui.navigator.ICommonActionExtensionSite;

import ru.dip.ui.action.rename.RefactorActionGroup;

public class RefactoringActionProvider extends CommonActionProvider {

	private RefactorActionGroup fRefactorGroup;

	@Override
	public void init(ICommonActionExtensionSite anActionSite) {
		fRefactorGroup = new RefactorActionGroup(anActionSite.getViewSite().getShell(), (Tree)anActionSite.getStructuredViewer().getControl());
	}

	@Override
	public void dispose() {
		fRefactorGroup.dispose();
	}

	@Override
	public void fillActionBars(IActionBars actionBars) {
		fRefactorGroup.fillActionBars(actionBars);
	}

	@Override
	public void fillContextMenu(IMenuManager menu) {
		fRefactorGroup.fillContextMenu(menu);
	}

	@Override
	public void setContext(ActionContext context) {
		fRefactorGroup.setContext(context);
	}

	@Override
	public void updateActionBars() {
		fRefactorGroup.updateActionBars();
	}
}
