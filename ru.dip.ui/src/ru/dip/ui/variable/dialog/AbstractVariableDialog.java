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
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import ru.dip.core.model.vars.IVarContainer;
import ru.dip.core.utilities.ui.CompositeBuilder;
import ru.dip.ui.Messages;
import ru.dip.ui.variable.VarValidator;

public abstract class AbstractVariableDialog extends Dialog {

	private final int WIDTH = 650;
	private final int HEIGHT = 300;
	
	protected final IVarContainer fVarContainer;
	protected Text fNameText;
	protected Text fValueText;
	

	protected AbstractVariableDialog(Shell parentShell, IVarContainer varContainer) {
		super(parentShell);
		fVarContainer = varContainer;
	}
	
	@Override
	protected Point getInitialSize() {
		return new Point(WIDTH, HEIGHT);
	}
	
	@Override
	protected Control createContents(Composite parent) {
		Control control =  super.createContents(parent);
		getButton(OK).setEnabled(false);
		return control;
	}
	
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite composite = CompositeBuilder.instance(parent).columns(2, false).full().build();
		
		Label nameLabel = new Label(composite, SWT.NONE);
		nameLabel.setText(Messages.EditVariableDialog_VarLable);
		fNameText = new Text(composite, SWT.BORDER);
		fNameText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		fNameText.addModifyListener(this::validate);
		
		Label label = new Label(composite, SWT.NONE);
		label.setText(Messages.EditVariableDialog_VarValueLabel);
		label.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING));		
		fValueText = new Text(composite, SWT.WRAP | SWT.MULTI | SWT.BORDER);		
		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
		gd.heightHint = 500;
		fValueText.setLayoutData(gd);		
		fValueText.addModifyListener(this::validate);
		
		if (fVarContainer.isReadOnly()) {
			fNameText.setEnabled(false);
			fValueText.setEnabled(false);
		}		
		return super.createDialogArea(parent);
	}
	
	private void validate(ModifyEvent event){
		if (getButton(OK) != null) {
			getButton(OK).setEnabled(isValidValues());
		}
	}

	private boolean isValidValues(){
		if (!VarValidator.isValidName(fNameText.getText().trim())) {
			return false;
		}
		if (!VarValidator.isValidValue(fValueText.getText().trim())) {
			return false;
		}
		return true;
	}
	
}
