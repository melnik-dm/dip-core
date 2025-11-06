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

import org.eclipse.ui.IActionBars;
import org.eclipse.ui.navigator.CommonActionProvider;
import org.eclipse.ui.navigator.ICommonActionExtensionSite;

import ru.dip.ui.action.hide.HideActionGroup;

public class HideActionProvider extends CommonActionProvider {

	private HideActionGroup fHideGroup;
	
	public HideActionProvider(){
		
	}
	
	@Override
	public void init(ICommonActionExtensionSite aSite) {
		fHideGroup = new HideActionGroup();
	}
	
	@Override
	public void fillActionBars(IActionBars actionBars) {
		fHideGroup.fillActionBars(actionBars);
	}

}
