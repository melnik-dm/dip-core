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

import java.io.PrintWriter;
import java.io.StringWriter;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;

public class ExportCommandException extends Exception implements IExportException {

	private static final long serialVersionUID = 1L;

	private final String fMessage;
	private final String fCommand;
	private final Throwable fCause;

	public ExportCommandException(String command, String message, Throwable cause) {
		fCommand = command;
		fMessage = message;
		fCause = cause;
	}

	public void showErrorMessage(Shell shell) {
		String message = getFullMessage();
		MessageDialog.openError(shell, "Export error", message);
	}

	public String getMessage() {
		return fMessage;
	}

	public String getCommand() {
		return fCommand;
	}

	@Override
	public String getFullMessage() {
		StringBuilder builder = new StringBuilder();
		builder.append(fCommand);
		builder.append("\n\n");
		if (fCommand != null && !fCommand.isEmpty()) {
			builder.append(fCommand);
			builder.append("\n");
		}
		if (fMessage != null && !fMessage.isEmpty()) {
			builder.append(fMessage);
			builder.append("\n");
		}

		if (fCause != null) {
			StringWriter errors = new StringWriter();
			fCause.printStackTrace(new PrintWriter(errors));
			builder.append(errors.toString());
		}
		return builder.toString();
	}

}
