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

public class CreateResourceException extends DIPException {

	private static final long serialVersionUID = 8959201441008262108L;

	public CreateResourceException(String message) {
		super(message);
	}

	public CreateResourceException(String message, Exception cause) {
		super(message, cause);
	}

}
