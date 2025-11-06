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
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.actions.ActionContext;
import org.eclipse.ui.navigator.CommonActionProvider;
import org.eclipse.ui.navigator.ICommonActionExtensionSite;

import ru.dip.ui.action.edit.EditActionGroup;

public class EditActionProvider extends CommonActionProvider {

	private EditActionGroup editGroup;

	private ICommonActionExtensionSite site;

	@Override
	public void init(ICommonActionExtensionSite anActionSite) {
		site = anActionSite;
		editGroup = new EditActionGroup(site.getViewSite().getShell());
	}

	@Override
	public void dispose() {
		editGroup.dispose();
	}

	@Override
	public void fillActionBars(IActionBars actionBars) {
		editGroup.fillActionBars(actionBars);
	}

	@Override
	public void fillContextMenu(IMenuManager menu) {
		editGroup.fillContextMenu(menu);
	}

	@Override
	public void setContext(ActionContext context) {
		editGroup.setContext(context);
	}

	@Override
	public void updateActionBars() {
		editGroup.updateActionBars();
	}
}
