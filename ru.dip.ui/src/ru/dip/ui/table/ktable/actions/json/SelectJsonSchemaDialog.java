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
package ru.dip.ui.table.ktable.actions.json;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import ru.dip.core.utilities.ui.CompositeFactory;
import ru.dip.ui.Messages;
import ru.dip.ui.utilities.ui.EntryTextComposite;
import ru.dip.ui.utilities.ui.FileDialogs;

public class SelectJsonSchemaDialog extends Dialog {

	private EntryTextComposite fBrowseEntry;
	private Path fResultPath;
	
	public SelectJsonSchemaDialog(Shell parentShell) {
		super(parentShell);
	}
	
	@Override
	protected Point getInitialSize() {
		Point p =  super.getInitialSize();
		if (p.x < 650){
			p.x = 650;
		}				
		return p;
	}
	
	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Validate By Json Schema");
	}
	
	
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite composite = CompositeFactory.full(parent, 2, false);

		fBrowseEntry = new EntryTextComposite(composite, "Json Schema: ");
		fBrowseEntry.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING | GridData.FILL_HORIZONTAL));
		fBrowseEntry.addModifyListener(new ModifyListener() {
			
			@Override
			public void modifyText(ModifyEvent e) {
				validate();
			}
		});
		
		Composite buttonComposite = new Composite(composite, SWT.NONE);
		buttonComposite.setLayout(new GridLayout());
				
		Button fileSystem = new Button(buttonComposite, SWT.PUSH);
		fileSystem.setText(Messages.IncludeWizardPage_File_system_button);
		fileSystem.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		fileSystem.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				String filepath = FileDialogs.getFileFromFileSystemDialog(getShell(), "Json Schema");
				if (filepath == null) {
					fBrowseEntry.setValue(""); //$NON-NLS-1$
				} else {				
					fBrowseEntry.setValue(filepath);
					okPressed();
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
				IFile file = FileDialogs.getWorkspaceFileDialog(getShell());
				if (file == null) {
					fBrowseEntry.setValue(""); //$NON-NLS-1$
				} else {
					java.nio.file.Path path = Paths.get(file.getLocationURI());
					fBrowseEntry.setValue(path.toString());
					okPressed();
					
				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				
			}			
		});			
		
		return composite;
	}
	
	@Override
	protected Control createButtonBar(Composite parent) {
		Control control =  super.createButtonBar(parent);
		getButton(OK).setEnabled(false);
		return control;
	}
	
	protected void validate() {
		boolean valid = isValid();
		getButton(OK).setEnabled(valid);
	}
	
	private boolean isValid() {
		String pathStr = fBrowseEntry.getValue();
		if (pathStr == null || pathStr.isEmpty()) {
			return false;
		}
		Path path = Paths.get(pathStr);
		if (!Files.exists(path)) {
			return false;
		}
		if (Files.isDirectory(path)) {
			return false;
		}
		return true;
	}
	
	@Override
	protected void okPressed() {
		fResultPath = Paths.get(fBrowseEntry.getValue());
		super.okPressed();
	}
	
	
	public Path getResult() {
		return fResultPath;
	}

}
