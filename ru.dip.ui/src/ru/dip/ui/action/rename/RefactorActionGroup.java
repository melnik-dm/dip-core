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
package ru.dip.ui.action.rename;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.IShellProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbenchCommandConstants;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.actions.ActionGroup;
import org.eclipse.ui.navigator.ICommonMenuConstants;

import ru.dip.core.model.IncludeFolder;
import ru.dip.core.model.DipFolder;
import ru.dip.core.model.DipProject;
import ru.dip.core.model.DipUnit;

public class RefactorActionGroup extends ActionGroup {
	
	private RenameAction fRenamAction;	
	private Shell fShell;
	private Tree fTree;

	public RefactorActionGroup(Shell aShell, Tree aTree) {
		fShell = aShell;
		fTree = aTree;
		makeActions();
	}

	@Override
	public void fillContextMenu(IMenuManager menu) {
		IStructuredSelection selection = (IStructuredSelection) getContext().getSelection();
		boolean canRename = isCanRename();
		if (canRename) {
			fRenamAction.selectionChanged(selection);
			menu.appendToGroup(ICommonMenuConstants.GROUP_REORGANIZE, fRenamAction);
		}
	}
	
	public boolean isCanRename(){
		IStructuredSelection selection = (IStructuredSelection) getContext().getSelection();
		if (selection == null || selection.isEmpty() || selection.size() != 1){
			return false;
		}
		Object obj = selection.getFirstElement();
		if (obj instanceof IncludeFolder) {
			return false;
		}	
		return (obj instanceof DipUnit || obj instanceof DipFolder || obj instanceof DipProject);
	}
	
	@Override
	public void fillActionBars(IActionBars actionBars) {
		updateActionBars();
		actionBars.setGlobalActionHandler(ActionFactory.RENAME.getId(), fRenamAction);
	}

	public void handleKeyPressed(KeyEvent event) {

		if (event.keyCode == SWT.F2 && event.stateMask == 0) {
			if (fRenamAction.isEnabled()) {
				fRenamAction.run();
			}
			// Swallow the event.
			event.doit = false;
		}
	}

	protected void makeActions() {
		IShellProvider sp = new IShellProvider() {
			@Override
			public Shell getShell() {
				return fShell;
			}
		};
		fRenamAction = new RenameAction(sp, fTree);
		fRenamAction.setActionDefinitionId(IWorkbenchCommandConstants.FILE_RENAME);
	}

	@Override
	public void updateActionBars() {
		IStructuredSelection selection = (IStructuredSelection) getContext().getSelection();
		fRenamAction.selectionChanged(selection);
	}

}
