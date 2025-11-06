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
package ru.dip.ui.table.ktable.dialog;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

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

import ru.dip.core.utilities.DipUtilities;
import ru.dip.core.utilities.ui.AbstractDialog;
import ru.dip.core.utilities.ui.CompositeFactory;
import ru.dip.ui.Messages;
import ru.dip.ui.utilities.ui.EntryTextComposite;
import ru.dip.ui.utilities.ui.FileDialogs;

public class ImportFolderDialog extends AbstractDialog {

	private EntryTextComposite fBrowseComposite;
	private String fFolderPath;
	
	public ImportFolderDialog(Shell parentShell) {
		super(parentShell, Messages.ImportFolderDialog_Title, new Point(570,0));
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite composte = CompositeFactory.full(parent, 2, false);
		
		fBrowseComposite = new EntryTextComposite(composte, Messages.ImportFolderDialog_ImportFolderLabel);
		fBrowseComposite.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING | GridData.FILL_HORIZONTAL));
		fBrowseComposite.addModifyListener(new ModifyListener() {
			
			@Override
			public void modifyText(ModifyEvent e) {
				boolean valid = validate();
				getButton(OK).setEnabled(valid);
			}
		});
		
		Composite buttonComposite = new Composite(composte, SWT.NONE);
		buttonComposite.setLayout(new GridLayout());
				
		Button fileSystem = new Button(buttonComposite, SWT.PUSH);
		fileSystem.setText(Messages.ImportFolderDialog_FileSystemButton);
		fileSystem.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		fileSystem.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				String filepath = FileDialogs.getFolderFromFileSystem(getShell(), Messages.ImportFolderDialog_SelectFolderDialogTitle);
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
		return composte;
	}
	
	@Override
	protected Control createButtonBar(Composite parent) {
		Control control =  super.createButtonBar(parent);
		getButton(OK).setEnabled(false);
		return control;
	}
		
	protected boolean validate() {
		String pathStr = fBrowseComposite.getValue();
		if (pathStr == null || pathStr.isEmpty()) {
			return false;
		}
		Path path = Paths.get(pathStr);
		if (!Files.exists(path)) {
			return false;
		}
		if (!Files.isDirectory(path)) {
			return false;
		}
		String name = path.getFileName().toString();
		if (!name.isEmpty() && !DipUtilities.isDipName(name)) {
			return false;
		}	
		return true;
	}

	@Override
	protected void okPressed() {
		fFolderPath = fBrowseComposite.getValue();
		super.okPressed();
	}
	
	public String getFolderPath() {
		return fFolderPath;
	}
}
