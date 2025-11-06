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

import ru.dip.core.model.vars.IVarContainer;
import ru.dip.core.utilities.ResourcesUtilities;
import ru.dip.core.utilities.WorkbenchUtitlities;
import ru.dip.ui.Messages;

public class NewVariableDialog extends AbstractVariableDialog {

	private String fStartName;
	
	public NewVariableDialog(Shell parentShell, IVarContainer varContainer) {
		super(parentShell, varContainer);
	}
	
	public NewVariableDialog(Shell parentShell, IVarContainer varContainer, String startName) {
		super(parentShell, varContainer);
		fStartName = startName;
	}
	
	@Override
	protected void configureShell(Shell newShell) {		
		super.configureShell(newShell);
		newShell.setText(Messages.NewVariableDialog_Title);
	}
	
	
	@Override
	protected Control createDialogArea(Composite parent) {
		Control control = super.createDialogArea(parent);
		if (fStartName != null){
			fNameText.setText(fStartName);
		}
		return control;
	}
		
	@Override
	protected void okPressed() {
		final String name = fNameText.getText().trim();
		if (fVarContainer.getChild(name) != null){
			VarMessagesDialogs.showAlreadyExistsErrorWhenNew(getShell(), name);
			return;
		}		
		try {
			fVarContainer.addField(name, fValueText.getText().trim());
		} catch (IOException e) {
			VarMessagesDialogs.showSaveContainerError(fVarContainer, getShell());
			e.printStackTrace();
		}
		ResourcesUtilities.updateProject(fVarContainer.parent().resource());
	    WorkbenchUtitlities.updateProjectExplorer();
		super.okPressed();
	}

}
