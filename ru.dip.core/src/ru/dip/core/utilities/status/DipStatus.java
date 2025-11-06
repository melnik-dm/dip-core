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
package ru.dip.core.utilities.status;

import org.eclipse.core.runtime.Status;

import ru.dip.core.DipCorePlugin;

public class DipStatus extends Status {
	
	public static final DipStatus OK_STATUS = new DipStatus();
	
	private final int fError;

	DipStatus(int severity, int error) {
		super(severity, DipCorePlugin.PLUGIN_ID, Errors.messages.get(error));
		fError = error;
	}
	
	DipStatus() {
		super(OK, DipCorePlugin.PLUGIN_ID, "ok");
		fError = 0;
	}
	
	public int getError(){
		return fError;
	}
	
	@Override
	public void setMessage(String message) {
		super.setMessage(message);
	}
	
}
