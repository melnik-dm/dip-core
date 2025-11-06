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

import java.io.IOException;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import ru.dip.core.model.vars.Variable;
import ru.dip.core.utilities.WorkbenchUtitlities;
import ru.dip.ui.Messages;

public class EditVariableDialog extends AbstractVariableDialog {

	private final Variable fVariable;

	public EditVariableDialog(Shell parentShell, Variable variable) {
		super(parentShell, variable.parent());
		fVariable = variable;
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(Messages.EditVariableDialog_Title); 
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Control control = super.createDialogArea(parent);
		fNameText.setText(fVariable.name());
		fValueText.setText(fVariable.getValue());
		return control;
	}

	@Override
	protected void okPressed() {
		String newName = fNameText.getText().trim();
		// already exists
		if (!newName.equals(fVariable.name()) && fVarContainer.getChild(newName) != null) {
			VarMessagesDialogs.showAlreadyExistsErrorWhenEdit(getShell(), newName);
			return;
		}
		// not change
		String newValue = fValueText.getText().trim();
		if (newName.equals(fVariable.name()) && newValue.equals(fVariable.getValue())) {
			super.okPressed();
			return;
		}
		// ask user
		if (!VarMessagesDialogs.confirmEditVar(getShell())) {
			return;
		}
		// change
		fVariable.setNewValue(newName, newValue);
		try {
			fVarContainer.saveVarContainer();
		} catch (IOException e) {
			VarMessagesDialogs.showSaveContainerError(fVarContainer, getShell());
			e.printStackTrace();
		}
		WorkbenchUtitlities.updateProjectExplorer();
		super.okPressed();
	}
}