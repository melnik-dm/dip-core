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

import ru.dip.core.DipCorePlugin;
import ru.dip.core.model.interfaces.IDipParent;

public class SaveTableDIPException extends DIPException {

	private static final long serialVersionUID = 1L;

	private IDipParent fParent;

	public SaveTableDIPException(IDipParent parent, String message) {
		super(message);
		fParent = parent;
		DipCorePlugin.logError(this, fParent + " message");
	}

}
