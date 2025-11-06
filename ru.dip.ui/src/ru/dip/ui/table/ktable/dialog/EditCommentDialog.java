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
package ru.dip.ui.table.ktable.dialog;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class EditCommentDialog extends Dialog {

	private static Point size;

	private Text fText;
	private String fCommentText;
	private String fResult;
	
	public EditCommentDialog(Shell parentShell, String commentText) {
		super(parentShell);
		fCommentText = commentText;
	}
	
	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Edit comment");
	}
	
	@Override
	protected boolean isResizable() {		
		return true;
	}
	
	@Override
	protected Point getInitialSize() {
		if (size == null) {
			size = new Point(550, 260);
		}
		return size;
	}
	
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout());
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		fText = new Text(composite, SWT.BORDER | SWT.WRAP  | SWT.V_SCROLL);
		fText.setLayoutData(new GridData(GridData.FILL_BOTH));
		if (fCommentText != null){
			fText.setText(fCommentText);
		}
		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
		//gd.heightHint = 160;
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
