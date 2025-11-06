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
package ru.dip.ui.action.duplicate;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import ru.dip.core.utilities.DipUtilities;
import ru.dip.core.utilities.status.StatusUtils;
import ru.dip.ui.Messages;
import ru.dip.ui.utilities.ui.EntryTextComposite;

public class RenameResourceDialog extends Dialog {

	private String fNewName;
	private EntryTextComposite fNewFilePath;
	private Label fErrorLabel;
	private IResource fResource;
	
	protected RenameResourceDialog(Shell parentShell, IResource resource) {
		super(parentShell);
		fResource = resource;
	}
	
	@Override
	protected Point getInitialSize() {
		Point p = super.getInitialSize();
		if (p.x < 600) {
			p.x = 600;
		}
		return p;
	}
	
	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(Messages.RenameResourceDialog_Shell_title + fResource.getProjectRelativePath());
	}
	
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout(2, false));
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		new Label(composite, SWT.NONE);
		new Label(composite, SWT.NONE);
		
		fErrorLabel = new Label(composite, SWT.WRAP);
		fErrorLabel.setText(Messages.RenameResourceDialog_Input_name_label);
		fErrorLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		new Label(composite, SWT.NONE);
		fNewFilePath = new EntryTextComposite(composite, Messages.RenameResourceDialog_new_name_label);
		fNewFilePath.setValue(fResource.getName());	

		fNewFilePath.addModifyListener(new ModifyListener() {

			@Override
			public void modifyText(ModifyEvent e) {
				validate();
			}
		});
		composite.pack();
		fNewFilePath.setFocus();
		fNewFilePath.getTextControl().setSelection(fResource.getName().length());
		return composite;
	}
	
	@Override
	protected Control createButtonBar(Composite parent) {
		Control control =  super.createButtonBar(parent);
		getButton(OK).setEnabled(false);
		return control;
	}

	private void validate() {
		IStatus status = getValidateStatus();
		if (status.isOK()) {
			fErrorLabel.setText(""); //$NON-NLS-1$
			getButton(OK).setEnabled(true);
		} else {
			getButton(OK).setEnabled(false);
			fErrorLabel.setText(status.getMessage());
		}
	}
	
	private IStatus getValidateStatus(){
		String newName = fNewFilePath.getValue();
		IStatus status = DipUtilities.checkReqName(newName);
		if (!status.isOK()){
			return status;
		}		
		if (fResource.getName().equals(newName)){
			return StatusUtils.NO_NAME;
		}
		if (fResource instanceof IFile){
			return DipUtilities.canCreateFile(fResource.getParent(), newName);	
		} else if (fResource instanceof IFolder) {
			return DipUtilities.canCreateFolder(fResource.getParent(), newName);

		}
		return Status.OK_STATUS;
	}
	
	@Override
	protected void okPressed() {
		fNewName = fNewFilePath.getValue();
		super.okPressed();
	}

	public String getNewName() {
		return fNewName;
	}
	
}
