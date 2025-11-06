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
package ru.dip.ui.wizard.newfolder;

import java.io.File;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.wizard.WizardPage;
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

import ru.dip.core.utilities.DipUtilities;
import ru.dip.core.utilities.ui.swt.ColorProvider;
import ru.dip.ui.Messages;
import ru.dip.ui.utilities.ui.EntryTextComposite;

public abstract class AbstractNewFolderPage extends WizardPage {

	// controls
	protected EntryTextComposite fNewFolderName;
	protected EntryTextComposite fDescription;	
	protected Label fIdLabel;
	protected Button fStart;
	protected Button fEnd;
	// model
	protected String fStartBeforeName;
	protected String fStartAfterName;
	protected boolean fAutoNumerationEnabled;
	protected boolean fIsChanged;  // флаг, когда пользователь изменил имя	
	protected boolean fBeforeMode = false;	
	protected IContainer fParentContainer;
	// result
	protected boolean fReservedFolder = false;
	protected IFolder fNewFolder;
	
	protected AbstractNewFolderPage() {
		super(Messages.AbstractNewFolderPage_PageName);
		setTitle(Messages.AbstractNewFolderPage_Title);
		setDescription(Messages.AbstractNewFolderPage_Description);
	}

	protected void createNameComposite(Composite parent){
		fNewFolderName = new EntryTextComposite(parent, Messages.AbstractNewFolderPage_FolderNameLabel);
		
		if (fAutoNumerationEnabled){
			if (fBeforeMode) {
				fNewFolderName.setValue(fStartBeforeName);
				fNewFolderName.getTextControl().setSelection(fStartBeforeName.length());
			} else {
				fNewFolderName.setValue(fStartAfterName);
				fNewFolderName.getTextControl().setSelection(fStartAfterName.length());
			}
		} else {
			fNewFolderName.setValue(Messages.AbstractNewFolderPage_NewFolderTitle);
			fNewFolderName.getTextControl().selectAll();
		}

		
		fNewFolderName.addModifyListener(new ModifyListener() {

			@Override
			public void modifyText(ModifyEvent e) {
				boolean valid = validate();
				setPageComplete(valid);
				fIdLabel.setText(getID());
				if (DipUtilities.isServedFolder(fNewFolderName.getValue())) {
					fDescription.setEnabled(false);
					fStart.setEnabled(false);
					fEnd.setEnabled(false);
				} else {
					fDescription.setEnabled(true);
					fStart.setEnabled(true);
					fEnd.setEnabled(true);
				}								
				parent.layout();
			}
		});
	}
	
	protected void createDescriptionComposite(Composite parent){
		fDescription = new EntryTextComposite(parent, Messages.AbstractNewFolderPage_DescriptionLabel);
	}
	
	protected void createPositionComposite(Composite parent){
		Label position = new Label(parent, SWT.NONE);
		position.setText(Messages.AbstractNewFolderPage_AddFolderLabel);
		GridData gd = new GridData();
		gd.horizontalIndent = 5;
		position.setLayoutData(gd);
		Composite positionComposite = new Composite(parent, SWT.NONE);
		positionComposite.setLayout(new GridLayout(1, false));		
		fStart = new Button(positionComposite, SWT.RADIO);
		fEnd = new Button(positionComposite, SWT.RADIO);
		setPositionButtonValues();
		fStart.addSelectionListener(new SelectionListener() {		
			@Override
			public void widgetSelected(SelectionEvent e) {
				updateName();					
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				
			}
		});
	}
	
	protected abstract void setPositionButtonValues();

	protected void updateName() {
		if (fAutoNumerationEnabled && !fIsChanged) {			
			if (fStart.getSelection()) {
				fNewFolderName.setValue(fStartBeforeName);
			} else {
				fNewFolderName.setValue(fStartAfterName);
			}					
			fIsChanged = false;
		}		
	}
	
	protected void createIDComposite(Composite parent) {
		Composite idComposite = new Composite(parent, SWT.NONE);
		idComposite.setLayout(new GridLayout(2, false));
		idComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));		
		Label label = new Label(idComposite, SWT.NONE);
		label.setText(Messages.AbstractNewFolderPage_IdLabel);
		fIdLabel = new Label(idComposite, SWT.NONE);	
		fIdLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		fIdLabel.setForeground(ColorProvider.MAGENTA);	
		fIdLabel.setText(getID());
	}
	
	protected String getID() {
		StringBuilder builder = new StringBuilder();		
		builder.append(fParentContainer.getProject().getName());
		builder.append(File.separator);
		if (!(fParentContainer instanceof IProject)) {
			builder.append(fParentContainer.getProjectRelativePath());
			builder.append(File.separator);
		}
		builder.append(fNewFolderName.getValue());
		return builder.toString();
	}
	
	
	protected abstract boolean validate();
	
	//======================
	// getters & setters
	
	public void setAutoNumeration(String start, String end) {
		fAutoNumerationEnabled = true;
		fStartBeforeName = start;
		fStartAfterName = end;
	}
	
	public void setBeforeMode() {
		fBeforeMode = true;
	}
	
	public IFolder getFolder(){
		return fNewFolder;
	}
	
	public boolean isReservedFolder(){
		return fReservedFolder;
	}
	
	public String getFolderName() {
		return fNewFolderName.getValue();
	}
	
	public boolean isStart() {
		return fStart.getSelection();
	}
	
	public String getFolderDescription() {
		if (!fDescription.getValue().isEmpty()) {						
			return fDescription.getValue();
		} else {
			return null;
		}
	}
}
