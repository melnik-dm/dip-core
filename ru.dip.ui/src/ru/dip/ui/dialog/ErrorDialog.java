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
package ru.dip.ui.dialog;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;

import ru.dip.core.utilities.status.Errors;
import ru.dip.ui.Messages;

public class ErrorDialog {
	
	public static void openMoveErorrMessage(Shell shell, String message){
		String title = Messages.ErrorDialog_Move_error_title;
		MessageDialog.openInformation(shell, title, message);
	}
	
	public static void openopyErorrMessage(Shell shell, int error){
		String title = Messages.ErrorDialog_Copy_error_title;
		String message = getErrorMessage(error);
		MessageDialog.openInformation(shell, title, message);
	}
	
	private static String getErrorMessage(int error){
		return Errors.messages.get(error);
	}

}
