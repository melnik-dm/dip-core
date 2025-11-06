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
import ru.dip.core.model.interfaces.IDipElement;

public class DeleteDIPException extends DIPException {

	private static final long serialVersionUID = 1L;

	private IDipElement fDeletedElement;

	public DeleteDIPException(IDipElement element, String message) {
		super(message);
		fDeletedElement = element;
		DipCorePlugin.logDeleteError(fDeletedElement, message);

	}

	public DeleteDIPException(String message) {
		super(message);
	}

}
