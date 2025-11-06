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
package ru.dip.ui.wizard.importfile;

import java.io.File;
import java.nio.file.Paths;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import ru.dip.core.model.interfaces.IDipParent;
import ru.dip.core.model.interfaces.IDipDocumentElement;
import ru.dip.core.utilities.DipUtilities;
import ru.dip.core.utilities.status.StatusUtils;
import ru.dip.ui.Messages;
import ru.dip.ui.utilities.ui.EntryTextComposite;
import ru.dip.ui.utilities.ui.FileDialogs;
import ru.dip.ui.wizard.newfile.AbstractNewFilePage;

public class ImportFilePage extends AbstractNewFilePage {

	private EntryTextComposite fImportPath;
	private IDipDocumentElement fRelative;
	private String fImportFilePath;
	private String fImportFileName;

	protected ImportFilePage(IDipDocumentElement relative) {
		super(Messages.ImportFilePage_PageName);
		setTitle(Messages.ImportFilePage_PageTitle);
		setDescription(Messages.ImportFilePage_Description);
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
		createImportComposite(composite);
		createIDComposite(composite);

		new Label(composite, SWT.NONE);
		createPositionComposite(composite);
		new Label(composite, SWT.NONE);

		setControl(composite);
	}

	private void createImportComposite(Composite composite) {
		Composite importComposite = new Composite(composite, SWT.NONE);
		importComposite.setLayout(new GridLayout(2, false));
		importComposite.setLayoutData(new GridData(GridData.FILL_BOTH));

		fImportPath = new EntryTextComposite(importComposite, Messages.ImportFilePage_ImportFileLabel);
		fImportPath.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING | GridData.FILL_HORIZONTAL));
		fImportPath.addModifyListener(new ModifyListener() {

			@Override
			public void modifyText(ModifyEvent e) {
				fIdLabel.setText(getID());
				boolean valid = validate();
				importComposite.layout();
				setPageComplete(valid);
			}
		});

		Composite buttonComposite = new Composite(importComposite, SWT.NONE);
		buttonComposite.setLayout(new GridLayout());

		Button fileSystem = new Button(buttonComposite, SWT.PUSH);
		fileSystem.setText(Messages.ImportFilePage_File_system_label);
		fileSystem.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		fileSystem.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				String filepath = FileDialogs.getFileFromFileSystemDialog(getShell(),
						Messages.ImportFilePage_Select_file_dialog_title);
				if (filepath == null) {
					fImportPath.setValue(""); //$NON-NLS-1$
				} else {
					fImportPath.setValue(filepath);
				}
				fIdLabel.setText(getID());
				boolean valid = validate();
				importComposite.layout();
				setPageComplete(valid);
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {

			}
		});

		Button workspaceButton = new Button(buttonComposite, SWT.PUSH);
		workspaceButton.setText(Messages.ImportFilePage_Worskpace_button);
		workspaceButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		workspaceButton.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				IFile file = FileDialogs.getWorkspaceFileDialog(getShell());
				if (file == null) {
					fImportPath.setValue(""); //$NON-NLS-1$
				} else {
					java.nio.file.Path path = Paths.get(file.getLocationURI());
					fImportPath.setValue(path.toString());
				}
				fIdLabel.setText(getID());
				importComposite.layout();
				boolean valid = validate();
				setPageComplete(valid);
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {

			}
		});
	}

	@Override
	protected String getID() {
		StringBuilder builder = new StringBuilder();
		builder.append(fParentContainer.getProject().getName());
		builder.append(File.separator);
		if (!(fParentContainer instanceof IProject)) {
			builder.append(fParentContainer.getProjectRelativePath());
			builder.append(File.separator);
		}
		String fileName = getFileName();
		if (fileName != null) {
			builder.append(fileName);
		}
		return builder.toString();
	}

	private String getFileName() {
		String fileName = ""; //$NON-NLS-1$
		if (!fImportPath.getValue().isEmpty()) {
			File file = new File(fImportPath.getValue());
			return file.getName();
		}
		return fileName;
	}

	@Override
	protected String getPositionText() {
		return Messages.ImportFilePage_Paste_file_label;
	}

	@Override
	protected void setPositionButtonValues() {
		if (fRelative != null) {
			fStart.setText(Messages.ImportFilePage_Before_file_label + fRelative.name());
			fEnd.setText(Messages.ImportFilePage_After_file_label + fRelative.name());
			if (fBeforeMode) {
				fStart.setSelection(true);
			} else {
				fEnd.setSelection(true);
			}
		} else {
			fStart.setText(Messages.ImportFilePage_Begin_directory_label);
			fEnd.setText(Messages.ImportFilePage_End_directory_label);
			fEnd.setSelection(true);
		}
	}

	@Override
	protected boolean validate() {
		IStatus status = getValidateStatus();
		if (status.isOK()) {
			setErrorMessage(null);
			return true;
		} else if (status.getSeverity() == IStatus.INFO) {
			setMessage(status.getMessage());
			return false;
		} else if (status.getSeverity() == IStatus.ERROR) {
			setErrorMessage(status.getMessage());
			return false;
		}
		return false;
	}

	@Override
	protected IStatus getValidateStatus() {
		File file = new File(fImportPath.getValue());
		if (!file.exists() || !file.isFile()) {
			return StatusUtils.NOT_FILE_EXISTS;
		}
		fImportFileName = getFileName();
		IStatus status = DipUtilities.checkReqName(fImportFileName);
		if (!status.isOK()) {
			return status;
		}
		fImportFilePath = fImportPath.getValue();
		return DipUtilities.canCreateFile(fParentContainer, fImportFileName);
	}

	public String getImportFilePath() {
		return fImportFilePath;
	}

	public IContainer getParentContainer() {
		return fParentContainer;
	}

	public String getImportFileName() {
		return fImportFileName;
	}

}
