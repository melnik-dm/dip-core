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

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;

public class DiaConvertCommandException extends Exception {

	private static final long serialVersionUID = 1L;

	private String fMessage;
	private String fCommand;

	public DiaConvertCommandException(String command, String message) {
		fCommand = command;
		fMessage = message;
	}

	public void showErrorMessage(Shell shell) {
		String message = fCommand + " \n\n" + fMessage;
		MessageDialog.openError(shell, "D error", message);
	}

	public String getMessage() {
		return fMessage;
	}

	public String getCommand() {
		return fCommand;
	}

}
