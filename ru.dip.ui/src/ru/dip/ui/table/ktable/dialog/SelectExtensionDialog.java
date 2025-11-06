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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

public class SelectExtensionDialog extends Dialog {

	private List<String> fAllExtensions;
	private List<String> fCurrentExtensions;
	private List<Button> fButtons = new ArrayList<>();
	
	protected SelectExtensionDialog(Shell parentShell, List<String> allExtensions, List<String> currentExtensions) {
		super(parentShell);
		fAllExtensions = allExtensions;
		fCurrentExtensions = currentExtensions;
	}
	
	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("File extensions");
	}
	
	@Override
	protected Point getInitialSize() {
		Point p =  super.getInitialSize();
		if (p.x < 300){
			p.x = 300;
		}		
		return p;
	}
		
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout(2, true));
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		new Label(composite, SWT.NONE);
		new Label(composite, SWT.NONE);
		for (String string: fAllExtensions){
			Button button = new Button(composite, SWT.CHECK);
			button.setText("." + string);
			if (fCurrentExtensions.contains(string)){
				button.setSelection(true);
			}
			fButtons.add(button);
		}		
		return composite;
	}
	
	@Override
	protected void okPressed() {
		fCurrentExtensions = new ArrayList<>();
		for (int i = 0; i < fButtons.size(); i++){
			if (fButtons.get(i).getSelection()){
				fCurrentExtensions.add(fAllExtensions.get(i));
			}
		}
		super.okPressed();
	}
	
	public List<String> getExtensions(){
		return fCurrentExtensions;
	}

}
