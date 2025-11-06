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

import java.io.IOException;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import ru.dip.core.model.glossary.GlossaryField;
import ru.dip.core.model.glossary.GlossaryFolder;
import ru.dip.core.utilities.WorkbenchUtitlities;
import ru.dip.ui.Messages;

public class EditGlossFieldDialog extends Dialog {

	private GlossaryFolder fGlossFolder;
	private GlossaryField fGlossaryField;
	private Text fNameText;
	private Text fValueText;

	
	public EditGlossFieldDialog(Shell parentShell, GlossaryField field) {
		super(parentShell);
		fGlossaryField = field;
		fGlossFolder = fGlossaryField.parent();
	}
	
	@Override
	protected void configureShell(Shell newShell) {		
		super.configureShell(newShell);
		newShell.setText(Messages.EditGlossFieldDialog_EditGlossaryShellTitle);
	}
	
	@Override
	protected Point getInitialSize() {
		Point p =  super.getInitialSize();
		p.x = 650;
		p.y = 300;
		return p;
	}
	
	@Override
	protected Control createContents(Composite parent) {
		Control control =  super.createContents(parent);
		getButton(OK).setEnabled(false);
		return control;
	}
	
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout(2 ,false));
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
				
		Label nameLabel = new Label(composite, SWT.NONE);
		nameLabel.setText(Messages.EditGlossFieldDialog_TermLabel);
		fNameText = new Text(composite, SWT.BORDER);
		fNameText.setText(fGlossaryField.name());
		fNameText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		fNameText.addModifyListener(this::validate);
		
		Label label = new Label(composite, SWT.NONE);
		label.setText(Messages.EditGlossFieldDialog_DefineLabel);
		label.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING));		
		fValueText = new Text(composite, SWT.WRAP | SWT.MULTI | SWT.BORDER);		
		fValueText.setText(fGlossaryField.getValue());
		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
		gd.heightHint = 500;
		fValueText.setLayoutData(gd);		
		fValueText.addModifyListener(this::validate);
		return super.createDialogArea(parent);
	}
	
	private void validate(ModifyEvent event){
		getButton(OK).setEnabled(isValidValues());
	}
	
	private boolean isValidValues(){
		if (fNameText.getText().trim().isEmpty()){
			return false;
		}
		if (fValueText.getText().trim().isEmpty()){
			return false;
		}
		return true;
	}
	
	@Override
	protected void okPressed() {
		String newName = fNameText.getText().trim();
		// already exists
		if (!newName.equalsIgnoreCase(fGlossaryField.name())  
				&& fGlossFolder.getChild(newName) != null){
			MessageDialog.openError(getShell(), Messages.EditGlossFieldDialog_EditGlossErrorTtile, Messages.EditGlossFieldDialog_AlreadyExistsErrorMessage);
			return;
		}  
		// not change
		String newValue = fValueText.getText().trim();
		if (newName.equals(fGlossaryField.name()) 
				&& newValue.equals(fGlossaryField.getValue())){
			super.okPressed();
			return;
		}
		// ask user
		boolean change = MessageDialog.openQuestion(getShell(), Messages.EditGlossFieldDialog_EditGlossaryShellTitle2, Messages.EditGlossFieldDialog_EditConfirmMessage);
		if (!change){
			return;
		}
		// change
		fGlossaryField.setNewValue(newName, newValue);
		try {
			fGlossFolder.saveGlossary();
		} catch (IOException e) {
			MessageDialog.openError(getShell(), Messages.EditGlossFieldDialog_GlossaryError2, Messages.EditGlossFieldDialog_GlossSaveErrorMessage);
			e.printStackTrace();
		}		
	    WorkbenchUtitlities.updateProjectExplorer();
		super.okPressed();
	}
}