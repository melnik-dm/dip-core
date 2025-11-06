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
package ru.dip.editors.md.unity.action;

import org.eclipse.jface.action.Action;

import ru.dip.editors.md.unity.MdTextField;

public class RedoAction extends Action {
	
	private MdTextField fTextField;
	
	@Override
	public void run() {
		if (fTextField != null) {
			fTextField.doRedo();
		}		
	}
	
	public void setMdField(MdTextField field) {
		fTextField = field;
		setEnabled(isEnabled());
	}
		
	@Override
	public boolean isEnabled() {
		if (fTextField == null) {
			return false;
		}		
		return fTextField.canRedo();
	}
}
