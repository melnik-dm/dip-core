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
package ru.dip.ui.wizard.importproject;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

import ru.dip.core.utilities.DipUtilities;
import ru.dip.ui.Messages;
import ru.dip.ui.utilities.ui.BrowseFolderComposite;
import ru.dip.ui.utilities.ui.EntryTextComposite;

public class ImportDipProjectPage extends WizardPage  {

	private final String fStartPath;
	private BrowseFolderComposite fBrowseComposite;
	private EntryTextComposite fNameEntry;
	private Button fCopyButton;
	
	protected ImportDipProjectPage(String startPath) {
		super(Messages.ImportDipProjectWizard_ImprotPageName);
		setTitle(Messages.ImportDipProjectPage_Title);
		setDescription(Messages.ImportDipProjectPage_Description);
		fStartPath = startPath;
	}
	
	@Override
	public void createControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout());				
		fBrowseComposite = new BrowseFolderComposite(composite, Messages.ImportDipProjectPage_Project_directory_label);	
		fBrowseComposite.getTextControl().addModifyListener(new ModifyListener() {
			
			@Override
			public void modifyText(ModifyEvent e) {
				setProjectName();
				validate();
			}
		});
		
		fNameEntry = new EntryTextComposite(composite, Messages.ImportDipProjectPage_Project_name_label);
		fNameEntry.addModifyListener(new ModifyListener() {
			
			@Override
			public void modifyText(ModifyEvent e) {
				validate();
			}
		});
		createCopyButton(composite);
		setControl(composite);
		
		if (fStartPath != null) {
			fBrowseComposite.setValue(fStartPath);
		} else {
			setPageComplete(false);
		}
	}
	
	private void setProjectName() {
		String projectPath = fBrowseComposite.getValue();
		if (projectPath != null && !projectPath.isEmpty()) {
			Path path = Paths.get(projectPath);
			if (Files.exists(path) && Files.isDirectory(path) && path.getFileName() != null) {
				String name = path.getFileName().toString();
				fNameEntry.setValue(name);
			} else {
				fNameEntry.setValue(projectPath);
			}
		}
	}

	private void createCopyButton(Composite parent){
		fCopyButton = new Button(parent, SWT.CHECK);
		fCopyButton.setText(Messages.ImportDipProjectPage_Copy_into_workspace_button);
		fCopyButton.setSelection(false);		
		fCopyButton.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				if (fCopyButton.getSelection()) {
					setProjectName();
					fNameEntry.setEnabled(false);
				} else {
					fNameEntry.setEnabled(true);
				}				
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				
			}
			
		});
	}
	
	private void validate(){
		String path = fBrowseComposite.getValue();
		IStatus status = DipUtilities.canImportProject(path);
		if (status.isOK()) {
			status = DipUtilities.checkProjectName(fNameEntry.getValue());
		}
		if (status.isOK()){
			setErrorMessage(null);
			setPageComplete(true);
			return;
		} else if (status.getSeverity() == IStatus.ERROR || status.getSeverity() == IStatus.WARNING){
			setErrorMessage(status.getMessage());
		} else {
			setMessage(Messages.ImportDipProjectPage_Select_project_directory_message);
		}
		setPageComplete(false);
	}
	
	public String getProjectPath(){
		return fBrowseComposite.getValue();
	}
	
	public String getProjectName() {
		return fNameEntry.getValue();
	}

	public boolean isCopy(){
		return fCopyButton.getSelection();
	}
}
