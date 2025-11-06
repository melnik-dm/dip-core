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
package ru.dip.core.utilities.ui.swt;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;

public enum KeyMode {
	
	CTRL, SHIFT, CTRL_SHIFT, NONE;
	
	public static KeyMode of(MouseEvent event) {
		if (isShift(event) && isCtrl(event)){
			return CTRL_SHIFT;
		}
		if (isShift(event)) {
			return SHIFT;
		}
		if (isCtrl(event)) {
			return CTRL;
		}		
		return NONE;
	}
	
	private static boolean isShift(MouseEvent event) {
		return (event.stateMask & SWT.SHIFT) == SWT.SHIFT;
	}
	
	private static boolean isCtrl(MouseEvent event) {
		return (event.stateMask & SWT.CTRL) == SWT.CTRL;
	}

}
