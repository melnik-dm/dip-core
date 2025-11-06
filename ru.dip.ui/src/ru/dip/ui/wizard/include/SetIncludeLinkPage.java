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

import static ru.dip.core.utilities.DnfoUtils.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.eclipse.core.resources.IContainer;
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

import ru.dip.core.table.TableReader;
import ru.dip.ui.Messages;
import ru.dip.ui.utilities.ui.EntryTextComposite;
import ru.dip.ui.utilities.ui.FileDialogs;
import ru.dip.ui.wizard.newfolder.AbstractNewFolderPage;

public class SetIncludeLinkPage extends AbstractNewFolderPage {

	private final static String DESCRIPTION =  Messages.IncludeWizardPage_Description;
	
	private EntryTextComposite fBrowseComposite;
	private Button fReadOnlyButton;
	private String fOldIncludePath;
	
	public SetIncludeLinkPage(String oldIncludePath) {		
		setTitle(Messages.IncludeWizardPage_Title);
		setDescription(DESCRIPTION);
		fOldIncludePath = oldIncludePath;
	}
	
	@Override
	public void createControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout());
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		createImportComposite(composite);
		new Label(composite, SWT.NONE);
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
		if (fOldIncludePath != null) {
			fBrowseComposite.setValue(fOldIncludePath);
		}
		
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

	@Override
	protected void setPositionButtonValues() {
		
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
		Path dnfoPath = path.resolve(DNFO_FILENAME);		 //$NON-NLS-1$
		if (!Files.exists(dnfoPath)){
			setErrorMessage(Messages.IncludeWizardPage_It_is_not_dip_project);
			return false;
		}
		if (!TableReader.isRoot(dnfoPath.toUri())) {
			setErrorMessage(Messages.IncludeWizardPage_It_is_not_dip_project);
			return false;
		}
		setMessage(null);
		setErrorMessage(null);
		return true;
	}
	
	public String includePath() {
		return fBrowseComposite.getValue();
	}
	
	public boolean isReadOnly() {
		return fReadOnlyButton.getSelection();
	}

}
