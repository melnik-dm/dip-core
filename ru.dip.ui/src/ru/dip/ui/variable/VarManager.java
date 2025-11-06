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
package ru.dip.ui.variable;

import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;

import ru.dip.core.model.vars.IVarContainer;
import ru.dip.core.model.vars.Variable;
import ru.dip.ui.variable.dialog.EditVariableDialog;
import ru.dip.ui.variable.dialog.NewVariableDialog;
import ru.dip.ui.variable.dialog.VarContainerDialog;

public class VarManager {

	public static void openVarContainerDialog(Shell shell, IVarContainer varContainer) {
		VarContainerDialog dialog = new VarContainerDialog(shell, varContainer);
		dialog.open();
	}

	public static void openEditVariableDialog(Shell shell, Variable variable) {
		EditVariableDialog dialog = new EditVariableDialog(shell, variable);
		dialog.open();
	}

	public static void openNewVariableDialog(Shell shell, IVarContainer varContainer) {
		NewVariableDialog dialog = new NewVariableDialog(shell, varContainer);
		dialog.open();
	}

	public static boolean openNewVariableDialog(Shell shell, IVarContainer varContainer, String varName) {
		NewVariableDialog dialog = new NewVariableDialog(shell, varContainer, varName);
		return dialog.open() == Window.OK;
	}

}
