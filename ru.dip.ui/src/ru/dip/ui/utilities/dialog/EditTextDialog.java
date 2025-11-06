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
package ru.dip.ui.utilities.dialog;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class EditTextDialog extends Dialog	{
	
	private String fTitle;
	private String fLabel;
	private String fStartValue;
	private String fResult;
	private Text fText;
	
	public EditTextDialog(Shell parentShell, String title, String label, String startValue) {
		super(parentShell);
		fLabel = label;
		fStartValue = startValue;
		fTitle = title;
	}
	
	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(fTitle);
	}
	
	@Override
	protected boolean isResizable() {		
		return true;
	}
	
	@Override
	protected Point getInitialSize() {
		return new Point(650, 250);
	}
	
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite composite = new Composite(parent,  SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.marginTop = 10;
		layout.verticalSpacing = 10;
		composite.setLayout(layout);
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		Label label = new Label(composite, SWT.NONE);
		label.setText(fLabel);
		fText = new Text(composite, SWT.WRAP | SWT.BORDER | SWT.V_SCROLL);
		if (fStartValue != null){
			fText.setText(fStartValue);
		}
		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
		gd.heightHint = 160;
		fText.setLayoutData(gd);	
		return composite;
	}
	
	@Override
	protected void okPressed() {
		fResult = fText.getText();
		super.okPressed();
	}
	
	public String getResult(){
		return fResult;
	}
}
