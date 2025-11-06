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
package ru.dip.ui.wizard.rename;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import ru.dip.core.DipCorePlugin;
import ru.dip.core.model.DipElementType;
import ru.dip.core.model.interfaces.IDipElement;
import ru.dip.core.utilities.DipUtilities;
import ru.dip.core.utilities.status.DipStatus;
import ru.dip.core.utilities.status.Errors;
import ru.dip.core.utilities.status.StatusUtils;
import ru.dip.ui.Messages;
import ru.dip.ui.utilities.ui.EntryTextComposite;

public class RenamePage extends WizardPage  implements IRenamePage {

	// controls
	private EntryTextComposite fNewFilePath;
	private Button fReserveButton;
	// model
	private IContainer fParentContainer;
	private IDipElement fDipElement;
	private DipElementType fReqType;
	
	protected RenamePage(String pageName, IDipElement dipElement) {
		super(pageName);
		setTitle(pageName);
		setDescription(Messages.RenamePage_Description);
		fParentContainer = dipElement.resource().getParent();
		fDipElement = dipElement;
		fReqType = fDipElement.type();
	
	}
	
	@Override
	public void setVisible(boolean visible) {
		super.setVisible(visible);
		if (visible) {
			fNewFilePath.setFocus();
		}
	}

	@Override
	public void createControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout(2, false));
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		new Label(composite, SWT.NONE);
		fNewFilePath = new EntryTextComposite(composite, Messages.RenamePage_NewNameTitle);
		fNewFilePath.addModifyListener(new ModifyListener() {

			@Override
			public void modifyText(ModifyEvent e) {
				boolean valid = validate();
				setPageComplete(valid);

			}
		});
		fNewFilePath.setValue(fDipElement.dipName());
		if (!DipCorePlugin.isDisableReservation() && canReserve(fReqType)){
			createReserveButton(composite);
		}
		
		composite.pack();
		setControl(composite);
		fNewFilePath.setFocus();
		fNewFilePath.getTextControl().setSelection(fDipElement.name().length());
		setPageComplete(false);
	}
	
	private boolean canReserve(DipElementType reqType) {
		return fReqType != DipElementType.RPOJECT 
				&& fReqType != DipElementType.INCLUDE_FOLDER;
	}
	
	private void createReserveButton(Composite parent){
		new Label(parent, SWT.NONE);
		new Label(parent, SWT.NONE);
		fReserveButton  = new Button(parent, SWT.CHECK);
		fReserveButton.setText(Messages.RenamePage_ReserveObjectButton);
		GridData gd = new GridData();
		gd.horizontalSpan = 2;
		fReserveButton.setLayoutData(gd);
	}
	
	private boolean validate(){
		IStatus status = getValidateStatus();
		if (status.isOK()){
			setErrorMessage(null);	
			return true;
		} else if (status.getSeverity() == IStatus.INFO){
			setMessage(status.getMessage());
			return false;
		} else if (status.getSeverity() == IStatus.ERROR || status.getSeverity() == IStatus.WARNING){			
			if (status instanceof DipStatus 
					&& ((DipStatus) status).getError() == Errors.FILE_ALREADY_EXISTS
					&&  fDipElement.name().equalsIgnoreCase(fNewFilePath.getValue())){
				setErrorMessage(null);	
				return true;
			}
			
			setErrorMessage(status.getMessage());
			return false;
		}
		return false;
	}
	
	private IStatus getValidateStatus(){
		String newName = fNewFilePath.getValue();
		IStatus status = DipUtilities.checkReqName(newName);
		if (!status.isOK()){
			return status;
		}		
		if (fDipElement.name().equals(newName)){
			return StatusUtils.NO_NAME;
		}
		if (fReqType == DipElementType.UNIT || fReqType == DipElementType.REPORT){
			return DipUtilities.canCreateFile(fParentContainer, newName);	
		} else if (fReqType == DipElementType.RPOJECT) {
			return DipUtilities.canCreateProject(newName);
		} else {
			return DipUtilities.canCreateFolder(fParentContainer, newName);

		}
	}

	public boolean isNeedReserve(){
		if (fReserveButton == null){
			return false;
		}
		return fReserveButton.getSelection();
	}
	
	@Override
	public String getNewName(){
		return fNewFilePath.getValue();
	}

}
