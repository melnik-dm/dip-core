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
package ru.dip.core.exception;

import java.io.IOException;

import ru.dip.core.DipCorePlugin;

public class DIPException extends IOException {

	private static final long serialVersionUID = 1L;

	public DIPException(String message) {
		super(message);
		DipCorePlugin.logError(message);
	}

	public DIPException(String message, Exception cause) {
		super(message, cause);
		DipCorePlugin.logError(message);
	}

}
