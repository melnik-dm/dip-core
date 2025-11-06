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
package ru.dip.ui.wizard.include;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.runtime.CoreException;
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
import ru.dip.core.utilities.DipUtilities;
import ru.dip.core.utilities.ResourcesUtilities;
import ru.dip.core.utilities.status.Errors;
import ru.dip.ui.Messages;
import ru.dip.ui.utilities.ui.EntryTextComposite;
import ru.dip.ui.utilities.ui.FileDialogs;
import ru.dip.ui.wizard.newfolder.AbstractNewFolderPage;

public class IncludeWizardPage extends AbstractNewFolderPage {

	private final static String DESCRIPTION =  Messages.IncludeWizardPage_Description;
	
	private EntryTextComposite fBrowseComposite;
	private EntryTextComposite fIncludeName;	
	private Button fReadOnlyButton;
	private IDipParent fRelative;
	private IDipParent fParent;

	public IncludeWizardPage() {
		setTitle(Messages.IncludeWizardPage_Title);
		setDescription(DESCRIPTION);
	}
	
	public IncludeWizardPage(IDipParent neighbour, boolean before, IDipParent parent) {
		this();
		fRelative = neighbour;
		fBeforeMode = before;
		fParent = parent;
	}
	
	@Override
	public void createControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout());
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		createImportComposite(composite);
		new Label(composite, SWT.NONE);
		createIncludeNameComposite(composite);
		createDescriptionComposite(composite);
		new Label(composite, SWT.NONE);
		createPositionComposite(composite);
		setControl(composite);
	}
	
	private void createImportComposite(Composite composite) {
		Composite importComposite = new Composite(composite, SWT.NONE);
		importComposite.setLayout(new GridLayout(2, false));
		importComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		fBrowseComposite = new EntryTextComposite(importComposite, Messages.IncludeWizardPage_Include_folder_label);
		fBrowseComposite.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING | GridData.FILL_HORIZONTAL));
		fBrowseComposite.addModifyListener(new ModifyListener() {
			
			@Override
			public void modifyText(ModifyEvent e) {
				boolean valid = validate();
				setPageComplete(valid);
			}
		});
		
		Composite buttonComposite = new Composite(importComposite, SWT.NONE);
		buttonComposite.setLayout(new GridLayout());
				
		Button fileSystem = new Button(buttonComposite, SWT.PUSH);
		fileSystem.setText(Messages.IncludeWizardPage_File_system_button);
		fileSystem.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		fileSystem.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				String filepath = FileDialogs.getFolderFromFileSystem(getShell(), Messages.IncludeWizardPage_File_dialog_title);
				if (filepath == null) {
					fBrowseComposite.setValue(""); //$NON-NLS-1$
				} else {				
					fBrowseComposite.setValue(filepath);
				}
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				
			}
		});
		
		Button workspaceButton = new Button(buttonComposite, SWT.PUSH);
		workspaceButton.setText(Messages.IncludeWizardPage_Workspace_button);
		workspaceButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		workspaceButton.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				IContainer folder = FileDialogs.getWorkspaceFolderDialog(getShell());
				if (folder == null) {
					fBrowseComposite.setValue(""); //$NON-NLS-1$
				} else {
					java.nio.file.Path path = Paths.get(folder.getLocationURI());
					fBrowseComposite.setValue(path.toString());
				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				
			}			
		});			
		fReadOnlyButton = new Button(fBrowseComposite, SWT.CHECK);
		fReadOnlyButton.setText(Messages.IncludeWizardPage_Read_only_button);
	}

	private void createIncludeNameComposite(Composite composite) {
		fIncludeName = new EntryTextComposite(composite, Messages.IncludeWizardPage_Name_label);
		fIncludeName.addModifyListener(new ModifyListener() {
			
			@Override
			public void modifyText(ModifyEvent e) {
				boolean valid = validate();
				setPageComplete(valid);				
			}
		});

	}
	
	@Override
	protected void setPositionButtonValues() {
		if (fRelative != null) {
			fStart.setText(Messages.IncludeWizardPage_Before_directory + fRelative.name());
			fEnd.setText(Messages.IncludeWizardPage_After_directory + fRelative.name());
			if (fBeforeMode) {
				fStart.setSelection(true);
			} else {
				fEnd.setSelection(true);
			}
		} else {
			fStart.setText(Messages.IncludeWizardPage_Begin_directory);
			fEnd.setText(Messages.IncludeWizardPage_End_directory);
			fEnd.setSelection(true);			
		}
	}

	@Override
	protected boolean validate() {
		String pathStr = fBrowseComposite.getValue();
		if (pathStr == null || pathStr.isEmpty()) {
			setMessage(Messages.IncludeWizardPage_Not_directory_path);
			return false;
		}
		Path path = Paths.get(pathStr);
		if (!Files.exists(path)) {
			setErrorMessage(Messages.IncludeWizardPage_Directory_not_exists);
			return false;
		}
		if (!Files.isDirectory(path)) {
			setErrorMessage(Messages.IncludeWizardPage_It_is_not_directory);
			return false;
		}
		if (!DipUtilities.isDipProject(path)) {
			setErrorMessage(Messages.IncludeWizardPage_It_is_not_dip_project);
			return false;
		}
		
		String name = fIncludeName.getValue();
		if (name.isEmpty()) {
			name = path.getFileName().toString();
		}
		try {
			if (ResourcesUtilities.contains(fParent.resource(), name)){
				setErrorMessage(Errors.messages.get(Errors.FILE_ALREADY_EXISTS));
				return false;
			}
		} catch (CoreException | IOException e) {
			setErrorMessage(e.getMessage());
			return false;
		}				
		if (!name.isEmpty() && !DipUtilities.isDipName(name)) {
			setErrorMessage(Errors.messages.get(Errors.INVALID_NAME));
			return false;
		}
		setMessage(null);
		setErrorMessage(null);
		return true;
	}

	public String includeName() {
		return fIncludeName.getValue();
	}
	
	public String includePath() {
		return fBrowseComposite.getValue();
	}
	
	public String description() {
		return fDescription.getValue();
	}
	
	public boolean isReadOnly() {
		return fReadOnlyButton.getSelection();
	}

}
