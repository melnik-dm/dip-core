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
package ru.dip.ui.glossary;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import ru.dip.core.model.glossary.GlossaryFolder;
import ru.dip.core.utilities.ui.swt.ColorProvider;
import ru.dip.ui.Messages;

public class GlossaryDialog extends Dialog {

	// model
	private GlossaryFolder fGlossFolder;

	public GlossaryDialog(Shell parentShell, GlossaryFolder folder) {
		super(parentShell);
		fGlossFolder = folder;
	}
	
	@Override
	protected void configureShell(Shell newShell) {		
		super.configureShell(newShell);
		newShell.setText(Messages.GlossaryDialog_Title);
	}
	
	@Override
	protected Point getInitialSize() {
		Point p =  super.getInitialSize();
		if (p.x < 700){
			p.x = 700;
		}
		p.y = 700;
		return p;
	}
	
	@Override
	protected boolean isResizable() {
		return true;
	}
	
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL,
				true);
	}
	
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite body = new Composite(parent, SWT.NONE);
		body.setLayout(new GridLayout());
		body.setLayoutData(new GridData(GridData.FILL_BOTH));		
		body.setBackground(ColorProvider.WHITE);
		
		GlossaryComposite glossaryComposite = new GlossaryComposite(body);
		glossaryComposite.setInput(fGlossFolder);
		return body;
	}
	
}
