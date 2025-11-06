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

public class ResultStatus extends Status {

	private Object fData;
	
	public ResultStatus(Object data) {
		super(OK, DipCorePlugin.PLUGIN_ID, "ok");
	}
	
	public Object getData() {
		return fData;
	}

}
