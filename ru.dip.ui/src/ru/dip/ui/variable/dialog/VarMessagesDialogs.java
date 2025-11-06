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
package ru.dip.ui.variable.dialog;

import java.text.MessageFormat;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;

import ru.dip.core.model.vars.IVarContainer;
import ru.dip.core.model.vars.Variable;
import ru.dip.ui.Messages;

public class VarMessagesDialogs {

	
	/**
	 * Сообщение об ошибке при сохранении файла .vars
	 */
	public static void showSaveContainerError(IVarContainer varContainer, Shell shell) {
		String varFileLocation = varContainer.resource().getLocation().toOSString();
		MessageDialog.openError(shell, 
				Messages.VarMessagesDialogs_SaveErrorTitle,
				Messages.VarMessagesDialogs_SaveErrorMessage + varFileLocation);
	}

	/**
	 * Сообщение, что уже существует переменная с таким именем (при редактировании)
	 */
	public static void showAlreadyExistsErrorWhenEdit(Shell shell, String name) {
		MessageDialog.openError(shell, 
				Messages.VarMessagesDialogs_EditVarErrorTitle, 
				Messages.VarMessagesDialogs_VarAlreadyExistsError + name);
	}
	
	/**
	 * Сообщение, что уже существует переменная с таким именем (при создании)
	 */
	public static void showAlreadyExistsErrorWhenNew(Shell shell, String name) {
		MessageDialog.openError(shell, 
				Messages.VarMessagesDialogs_AddVarErrorTitle, 
				Messages.VarMessagesDialogs_VarAlreadyExistsError + name);
	}
	
	/**
	 * Сообщение, неопределенные переменные не найдены
	 */
	public static void showNotFindUndefinedVarsMsg(Shell shell) {
		MessageDialog.openInformation(shell, Messages.VarMessagesDialogs_NotFoundUndefVarsTitle,
				Messages.VarMessagesDialogs_NotFoundUndefVarsMsg);
	}
	
	/**
	 * Подтверждение на редактирование переменной
	 */
	public static boolean confirmEditVar(Shell shell) {
		return MessageDialog.openQuestion(shell, 
				Messages.VarMessagesDialogs_EditVarTitle, 
				Messages.VarMessagesDialogs_EditVarConfirm);
	}
	
	
	/**
	 * Подтверждение на удаление переменной (переменных)
	 */
	public static boolean confirmDeleteVariables(Object[] selection, Shell shell) {
		if (selection.length == 1) {
			Variable variable = (Variable) selection[0];
			return confirmDeleteVariable(variable, shell);

		} else {
			return MessageDialog.openQuestion(shell,
				Messages.VarMessagesDialogs_DeleteConfirmTitle, 
				Messages.VarMessagesDialogs_Confirm_variables_message);
		}
	}
	
	public static boolean confirmDeleteVariable(Variable variable, Shell shell) {
		return MessageDialog.openQuestion(shell,
				Messages.VarMessagesDialogs_DeleteConfirmTitle, 
				MessageFormat.format(Messages.VarMessagesDialogs_Confirm_variable_message, variable.name()));
	}
	
	public static boolean confirmDeleteVarContainer(IVarContainer container, Shell shell) {
		if (container.getVariables().isEmpty()) {
			return true;
		}
		return MessageDialog.openQuestion(shell,
				Messages.VarMessagesDialogs_DeleteConfirmTitle, 
				Messages.VarMessagesDialogs_DeleteVarContainer +container.getDipParent().name());		
	}
		
}
