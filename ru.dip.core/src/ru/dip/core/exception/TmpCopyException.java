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

import org.eclipse.core.resources.IResource;

public class TmpCopyException extends CopyDIPException {

	private static final long serialVersionUID = 2936224683336679413L;

	public TmpCopyException(IResource resource, String message) {
		super(resource, message);
	}

}
