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
package ru.dip.ui.wizard.newtablefile;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import ru.dip.core.model.interfaces.IDipDocumentElement;
import ru.dip.core.model.interfaces.IDipParent;
import ru.dip.core.unit.ReportRefPresentation;
import ru.dip.core.utilities.DipUtilities;
import ru.dip.ui.Messages;
import ru.dip.ui.wizard.newfile.AbstractNewFilePage;

public class TableNewFilePage extends AbstractNewFilePage {

	private IDipDocumentElement fRelative; // для команд before, after

	protected TableNewFilePage(IDipDocumentElement relative) {
		fDipProject = relative.dipProject();
		if (relative instanceof IDipParent) {
			fParentContainer = (IContainer) relative.resource();
		} else if (relative instanceof IDipDocumentElement) {
			fRelative = (IDipDocumentElement) relative;
			fParentContainer = relative.parent().resource();
		}
	}

	@Override
	public void createControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout());
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		createNameComposite(composite);
		createIDComposite(composite);
		new Label(composite, SWT.NONE);
		createPositionComposite(composite);
		setControl(composite);
		fNewFileName.setFocus();
		boolean valid = validate();
		setPageComplete(valid);
	}

	@Override
	protected void setPositionButtonValues() {
		if (fRelative != null) {
			fStart.setText(Messages.TableNewFilePage_BeforeFile + fRelative.name());
			fEnd.setText(Messages.TableNewFilePage_AfterFile + fRelative.name());
			if (fBeforeMode) {
				fStart.setSelection(true);
			} else {
				fEnd.setSelection(true);
			}
		} else {
			fStart.setText(Messages.TableNewFilePage_StartDirectory);
			fEnd.setText(Messages.TableNewFilePage_EndDirectory);
			fEnd.setSelection(true);
		}
	}
	
	@Override
	public boolean canFlipToNextPage() {
		return ReportRefPresentation.EXTENSION_WITHOUT_DOT.equals(getExtension());
	}
	
	@Override
	public IStatus getValidateStatus() {
		String fileName = fNewFileName.getText() + fExtensionCombo.getText();
		IStatus status = DipUtilities.checkReqName(fileName);
		if (!status.isOK()) {
			return status;
		}
		return DipUtilities.canCreateFile(fParentContainer, fileName);
	}

	public String getFileName() {
		return fNewFileName.getText() + fExtensionCombo.getText();
	}

}
