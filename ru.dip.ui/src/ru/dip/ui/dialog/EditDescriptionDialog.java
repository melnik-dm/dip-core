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

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import ru.dip.ui.Messages;

public class EditDescriptionDialog extends Dialog	{

	private static Point size;
	
	private String fLabel;
	private String fStartValue;
	private String fResult;
	private Text fText;
	
	public EditDescriptionDialog(Shell parentShell, String label, String startValue) {
		super(parentShell);
		fLabel = label;
		fStartValue = startValue;
	}
	
	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(Messages.EditDescriptionDialog_shell_title);
	}
	
	@Override
	protected boolean isResizable() {		
		return true;
	}
	
	@Override
	protected Point getInitialSize() {
		if (size == null) {
			size = new Point(650, 250);
		}
		return size;
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
		
		
		getShell().addControlListener(new ControlListener() {
			
			@Override
			public void controlResized(ControlEvent e) {
				size = getShell().getSize();
			}
			
			@Override
			public void controlMoved(ControlEvent e) {
				
			}
		});
		return composite;
	}
	
	@Override
	protected void okPressed() {
		fResult = fText.getText().trim();
		super.okPressed();
	}
	
	public String getResult(){
		return fResult;
	}

}
