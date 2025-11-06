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

import org.eclipse.core.resources.IFolder;
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
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;

import ru.dip.core.model.interfaces.IDipParent;
import ru.dip.core.utilities.ui.swt.ColorProvider;
import ru.dip.ui.Messages;
import ru.dip.ui.utilities.ui.EntryTextComposite;

public class NewFolderNumberingPage extends WizardPage {
	// controls
	private Button fFilesNumerationButton;
	private Button fFoldersNumerationButton;
	private EntryTextComposite fFileStepEntry;
	private EntryTextComposite fFolderStepEntry;
	// model
	private IFolder fNewFolder;
	private IDipParent fDipParent;
	private boolean fReservedFolder = false;
	private boolean fFilesNumeration = false;
	private boolean fFoldersNumeration = false;
	private String fFileStep;
	private String fFolderStep;
		
	public NewFolderNumberingPage(String pageName, IDipParent dipParent) {
		super(pageName);
		setTitle(pageName);
		setDescription(Messages.NewFolderNumberingPage_Description);
		fDipParent = dipParent;
	}

	@Override
	public void createControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout());
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		new Label(composite, SWT.NONE);
		createNumerationComposite(composite);
		new Label(composite, SWT.NONE);
		setControl(composite);
		boolean valid = validate();
		setPageComplete(valid);	}
	
	private void createNumerationComposite(Composite parent){
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout(1, true));
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));	
		createFileNumberingGroup(composite);
		createFolderNumberingGroup(composite);
	}
	
	private void createFileNumberingGroup(Composite parent){
		Group filesGroup = new Group(parent, SWT.NONE);
		filesGroup.setText(Messages.NewFolderNumberingPage_AutonumerationFilesLabel);
		filesGroup.setLayout(new GridLayout(2 , false));
		filesGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		fFilesNumerationButton = new Button(filesGroup, SWT.CHECK);
		fFilesNumerationButton.setText(Messages.NewFolderNumberingPage_EnableButton);
		fFileStepEntry = new EntryTextComposite(filesGroup, Messages.NewFolderNumberingPage_StepLabel);			
		fFileStepEntry.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		fFileStepEntry.setEnabled(false);
		
		Label exampleLabel = new Label(filesGroup, SWT.NONE);
		exampleLabel.setText(Messages.NewFolderNumberingPage_ExampleLabel);
		exampleLabel.setEnabled(false);
		
		Label exampleID = new Label(filesGroup, SWT.NONE);
		exampleID.setText("  "); //$NON-NLS-1$
		exampleID.setForeground(ColorProvider.BLUE);
		exampleID.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		fFileStepEntry.addModifyListener(new ModifyListener() {
			
			@Override
			public void modifyText(ModifyEvent e) {
				String exampleIdValue = getExampleID(fFileStepEntry.getValue());
				exampleID.setText(exampleIdValue);
				boolean valid = validate();
				setPageComplete(valid);			
			}
		});
		
		fFilesNumerationButton.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				boolean selection = fFilesNumerationButton.getSelection();
				fFileStepEntry.setEnabled(selection);
				exampleLabel.setEnabled(selection);
				exampleID.setEnabled(selection);
			
				if (selection && fFileStepEntry.getValue() == null || fFileStepEntry.getValue().isEmpty()){
					fFileStepEntry.setValue("010"); //$NON-NLS-1$
				}
				
				if (selection){
					String exampleIdValue = getExampleID(fFileStepEntry.getValue());
					exampleID.setText(exampleIdValue);
				} else {
					exampleID.setText(""); //$NON-NLS-1$
				}
				
				boolean valid = validate();
				setPageComplete(valid);				
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				
			}
		});
	}
	
	private void createFolderNumberingGroup(Composite parent){
		Group foldersGroup = new Group(parent, SWT.NONE);
		foldersGroup.setText(Messages.NewFolderNumberingPage_AutonumerationFoldersLabel);
		foldersGroup.setLayout(new GridLayout(2, false));
		foldersGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		fFoldersNumerationButton = new Button(foldersGroup, SWT.CHECK);
		fFoldersNumerationButton.setText(Messages.NewFolderNumberingPage_Enable2);
		fFolderStepEntry = new EntryTextComposite(foldersGroup, Messages.NewFolderNumberingPage_Step2);			
		fFolderStepEntry.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		fFolderStepEntry.setEnabled(false);
		
		Label exampleLabel = new Label(foldersGroup, SWT.NONE);
		exampleLabel.setText(Messages.NewFolderNumberingPage_ExampleFolderNameLabel);
		exampleLabel.setEnabled(false);
		
		Label exampleID = new Label(foldersGroup, SWT.NONE);
		exampleID.setText("  "); //$NON-NLS-1$
		exampleID.setForeground(ColorProvider.BLUE);
		exampleID.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		fFolderStepEntry.addModifyListener(new ModifyListener() {
			
			@Override
			public void modifyText(ModifyEvent e) {
				String exampleIdValue = getExampleID(fFolderStepEntry.getValue());
				exampleID.setText(exampleIdValue);
				boolean valid = validate();
				setPageComplete(valid);			
			}
		});
		
		fFoldersNumerationButton.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				boolean selection = fFoldersNumerationButton.getSelection();
				fFolderStepEntry.setEnabled(selection);
				exampleLabel.setEnabled(selection);
				exampleID.setEnabled(selection);
			
				if (selection && fFolderStepEntry.getValue() == null || fFolderStepEntry.getValue().isEmpty()){
					fFolderStepEntry.setValue("010"); //$NON-NLS-1$
				}
				
				if (selection){
					String exampleIdValue = getExampleID(fFolderStepEntry.getValue());
					exampleID.setText(exampleIdValue);
				} else {
					exampleID.setText(""); //$NON-NLS-1$
				}
				
				boolean valid = validate();
				setPageComplete(valid);				
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				
			}
		});
	}
	
	private String getExampleID(String name){
		String exampleIdValue = fDipParent.id();
		StringBuilder builder = new StringBuilder();
		builder.append(exampleIdValue);
		if (!exampleIdValue.endsWith("/")){ //$NON-NLS-1$
			builder.append("/"); //$NON-NLS-1$
		}
		NewFolderWizard wizard = (NewFolderWizard) getWizard();
		String newFolder = wizard.getNewFolderName();
		builder.append(newFolder);
		builder.append("/"); //$NON-NLS-1$
		builder.append(name);
		return builder.toString();
	}
	
	//======================
	// validate
	
	private boolean validate(){		
		fReservedFolder = false;
		if (checkStepNumeration()){
			setErrorMessage(null);	
			return true;
		} else {
			setErrorMessage(Messages.NewFolderNumberingPage_InvalidStepError);
			return false;				
		}				
	}
		

	
	private boolean checkStepNumeration(){
		fFilesNumeration = fFilesNumerationButton.getSelection();
		fFoldersNumeration = fFoldersNumerationButton.getSelection();
		fFileStep = fFileStepEntry.getValue();	
		fFolderStep = fFolderStepEntry.getValue();
		if (fFilesNumeration){ 
			String value = fFileStepEntry.getValue().trim();
			try {
				int n = Integer.parseInt(value);
				return (n > 0);			
			} catch (NumberFormatException e){
				return false;
			}
		}
		if (fFoldersNumeration){ 
			String value = fFolderStepEntry.getValue().trim();
			try {
				int n = Integer.parseInt(value);
				return (n > 0);			
			} catch (NumberFormatException e){
				return false;
			}
		}		
		return true;
	}
	
	//======================
	// getters & setters
	
	public IFolder getFolder(){
		return fNewFolder;
	}
	
	public boolean isReservedFolder(){
		return fReservedFolder;
	}
	
	public boolean isFileAutoNumbering(){
		return fFilesNumeration;
	}
	
	public boolean isFolderAutoNumbering(){
		return fFoldersNumeration;
	}

	public String getFileStep(){
		return fFileStep;
	}
	
	public String getFolderStep(){
		return fFolderStep;
	}
	
}
