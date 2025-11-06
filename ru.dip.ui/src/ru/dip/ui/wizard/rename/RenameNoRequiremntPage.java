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
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import ru.dip.core.model.DipElement;
import ru.dip.core.utilities.DipUtilities;
import ru.dip.core.utilities.status.Errors;
import ru.dip.core.utilities.status.DipStatus;
import ru.dip.core.utilities.status.StatusUtils;
import ru.dip.ui.Messages;
import ru.dip.ui.utilities.ui.EntryTextComposite;

public class RenameNoRequiremntPage extends WizardPage implements IRenamePage {
	
	// controls
	private EntryTextComposite fNewFilePath;
	// model
	private IContainer fParentContainer;
	private DipElement fDipElement;
	private String fOldName;
	
	protected RenameNoRequiremntPage(String pageName, DipElement dipElement) {
		super(pageName);
		setTitle(pageName);
		setDescription(Messages.RenameNoRequiremntPage_Description);
		fParentContainer = dipElement.resource().getParent();
		fDipElement = dipElement;	
		fOldName = getOldName();
	}
	
	private String getOldName(){
		String name = fDipElement.name();
		return name;
	}

	@Override
	public void createControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout(2, false));
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		new Label(composite, SWT.NONE);
		fNewFilePath = new EntryTextComposite(composite, Messages.RenameNoRequiremntPage_NewNameLabel);
		fNewFilePath.addModifyListener(new ModifyListener() {

			@Override
			public void modifyText(ModifyEvent e) {
				boolean valid = validate();
				setPageComplete(valid);

			}
		});
		fNewFilePath.setValue(fOldName);		
		composite.pack();
		setControl(composite);
		fNewFilePath.setFocus();
		fNewFilePath.getTextControl().setSelection(fOldName.length());
		setPageComplete(false);
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
		String newName = getNewName();
		IStatus status = DipUtilities.checkReqName(newName);
		if (!status.isOK()){
			return status;
		}		
		if (fDipElement.name().equals(newName)){
			return StatusUtils.NO_NAME;
		}
		return DipUtilities.canCreateFile(fParentContainer, newName);	
		
	}

	@Override
	public String getNewName(){
		return fNewFilePath.getValue();
	}

	@Override
	public boolean isNeedReserve() {
		return false;
	}

}
