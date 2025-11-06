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

import ru.dip.ui.Messages;

public class QuestionDialog {

	public static boolean needReserveForMove(Shell shell, boolean one){
		String message;
		if (one){
			message = Messages.QuestionDialog_Reserve_obj_with_move;
		} else {
			message = Messages.QuestionDialog_Reserve_objs_with_move;
		}	
		boolean result = MessageDialog.openQuestion(shell, Messages.QuestionDialog_Move_resource_title, message);	
		return result;
	}
}

