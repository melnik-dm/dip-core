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

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import ru.dip.core.model.vars.IVarContainer;
import ru.dip.core.utilities.ui.CompositeBuilder;
import ru.dip.core.utilities.ui.swt.ColorProvider;
import ru.dip.ui.Messages;
import ru.dip.ui.variable.view.VariablesComposite;

public class VarContainerDialog extends Dialog {

	private final int MIN_WIDTH = 700;
	private final int MIN_HEIGHT = 700;

	// model
	private IVarContainer fVarContainer;

	public VarContainerDialog(Shell parentShell, IVarContainer folder) {
		super(parentShell);
		fVarContainer = folder;
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(Messages.VarContainerDialog_Title);
	}

	@Override
	protected Point getInitialSize() {
		Point p = super.getInitialSize();
		if (p.x < MIN_WIDTH) {
			p.x = MIN_WIDTH;
		}
		p.y = MIN_HEIGHT;
		return p;
	}

	@Override
	protected boolean isResizable() {
		return true;
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite body = CompositeBuilder.instance(parent).full().background(ColorProvider.WHITE).build();
		VariablesComposite variablesComposite = new VariablesComposite(body);
		variablesComposite.setInput(fVarContainer);
		return body;
	}

}
