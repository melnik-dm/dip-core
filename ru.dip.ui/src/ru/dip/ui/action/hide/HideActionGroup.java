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
package ru.dip.ui.action.hide;

import org.eclipse.ui.IActionBars;
import org.eclipse.ui.actions.ActionGroup;

public class HideActionGroup  extends ActionGroup {

	private ShowReservedAction fShowReservedAction;

	@Override
	public void fillActionBars(IActionBars actionBars) {
		if (fShowReservedAction == null){
			fShowReservedAction = new ShowReservedAction();
			actionBars.getToolBarManager().add(fShowReservedAction);			
		}
		super.fillActionBars(actionBars);
	}
	
}
