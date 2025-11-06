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
package ru.dip.ui.wizard.tablenewfolder;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import ru.dip.core.model.interfaces.IDipParent;
import ru.dip.core.utilities.DipUtilities;
import ru.dip.ui.Messages;
import ru.dip.ui.wizard.newfolder.AbstractNewFolderPage;

public class TableNewFolderPage extends AbstractNewFolderPage {

	private IDipParent fRelative;
	
	protected TableNewFolderPage(IDipParent dipParent) {
		fParentContainer = dipParent.resource();
	}

	@Override
	public void createControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout());
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		createNameComposite(composite);
		createDescriptionComposite(composite);
		createIDComposite(composite);
		new Label(composite, SWT.NONE);
		createPositionComposite(composite);
		new Label(composite, SWT.NONE);
		setControl(composite);
		fNewFolderName.setFocus();
		boolean valid = validate();
		setPageComplete(valid);	
	}
	
	protected void setPositionButtonValues() {
		if (fRelative != null) {
			fStart.setText(Messages.TableNewFolderPage_BeforeDirectory + fRelative.name());
			fEnd.setText(Messages.TableNewFolderPage_AfterDirectory + fRelative.name());
			if (fBeforeMode) {
				fStart.setSelection(true);
			} else {
				fEnd.setSelection(true);
			}
		} else {
			fStart.setText(Messages.TableNewFolderPage_StartDirectory);
			fEnd.setText(Messages.TableNewFolderPage_EndDirectory);
			fEnd.setSelection(true);			
		}
	}

	//======================
	// validate
	
	protected boolean validate(){		
		fReservedFolder = false;
		IStatus status = getValidateStatus();
		if (status.isOK()){
			setErrorMessage(null);
			return true;
		} else if (status.getSeverity() == IStatus.INFO){
			setMessage(status.getMessage());
			return false;
		} else if (status.getSeverity() == IStatus.WARNING) {
			setMessage(status.getMessage());
			fReservedFolder = true;
			return true;
		} else if (status.getSeverity() == IStatus.ERROR){	
			setErrorMessage(status.getMessage());
			return false;
		}		
		return false;
	}
		
	private IStatus getValidateStatus(){
		String folderName = fNewFolderName.getValue();
		IStatus status = DipUtilities.checkReqName(folderName);
		if (!status.isOK()){
			return status;
		}
		return DipUtilities.canCreateFolder(fParentContainer, folderName);
	}
	
	//======================
	// getters & setters
	
	public void setRelativeParent(IDipParent relativeParent) {
		fRelative = relativeParent;
	}

}
