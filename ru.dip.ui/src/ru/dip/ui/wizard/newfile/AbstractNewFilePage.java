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
package ru.dip.ui.wizard.newfile;

import java.io.File;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import ru.dip.core.model.DipProject;
import ru.dip.core.unit.ReportRefPresentation;
import ru.dip.core.unit.UnitType;
import ru.dip.core.utilities.ui.swt.ColorProvider;
import ru.dip.ui.Messages;
import ru.dip.ui.ReqUIPlugin;

public abstract class AbstractNewFilePage extends WizardPage {

	// controls
	protected Text fNewFileName;
	protected Combo fExtensionCombo;
	protected Label fIdLabel;
	protected Button fStart;
	protected Button fEnd;
	// model
	protected String fStartBeforeName;
	protected String fStartAfterName;
	protected boolean fAutoNumerationEnabled;
	protected boolean fIsChanged;  // флаг, когда пользователь изменил имя	
	protected boolean fBeforeMode = false;	// файл создается в начале директории либо перед указанным объектом
	protected DipProject fDipProject;
	protected IContainer fParentContainer;
	protected IFile fNewFile;
	protected boolean fReportFile;

	protected AbstractNewFilePage(String pageName) {
		super(pageName);
	}
	
	protected AbstractNewFilePage() {
		super(Messages.AbstractNewFilePage_PageName);
		setTitle(Messages.AbstractNewFilePage_Title);
		setDescription(Messages.AbstractNewFilePage_Description);
	}

	protected void createNameComposite(Composite parent) {
		Composite nameComposite = new Composite(parent, SWT.NONE);
		nameComposite.setLayout(new GridLayout(3, false));
		nameComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		Label label = new Label(nameComposite,SWT.NONE);
		label.setText(Messages.AbstractNewFilePage_FileNameLabel);
			
		fNewFileName = new Text(nameComposite, SWT.BORDER);		
		if (fAutoNumerationEnabled){
			if (fBeforeMode) {
				fNewFileName.setText(fStartBeforeName);
				fNewFileName.setSelection(fStartBeforeName.length());
			} else {
				fNewFileName.setText(fStartAfterName);
				fNewFileName.setSelection(fStartAfterName.length());
			}
		} else {
			fNewFileName.setText(Messages.AbstractNewFilePage_NewNameLabel);
			fNewFileName.selectAll();
		}
		
		fNewFileName.addModifyListener(new ModifyListener() {
			
			@Override
			public void modifyText(ModifyEvent e) {
				boolean valid = validate();
				setPageComplete(valid);
				fIdLabel.setText(getID());
				fIsChanged = true;
				parent.layout();
			}
		});
		fNewFileName.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));		
		fExtensionCombo = new Combo(nameComposite, SWT.READ_ONLY);
		String[] possibleExtensions = fDipProject.getPossibleExtensions();
		fExtensionCombo.setItems(possibleExtensions);
		fExtensionCombo.select(possibleExtensions.length - 1);
		setStartFileExtension(possibleExtensions);
		fExtensionCombo.addModifyListener(new ModifyListener() {
			
			@Override
			public void modifyText(ModifyEvent e) {
				if (UnitType.isNotNameExtension(fExtensionCombo.getText())) {
					fNewFileName.setText("");					 //$NON-NLS-1$
				} 
				boolean valid = validate();
				setPageComplete(valid);
				fIdLabel.setText(getID());
				fIsChanged = true;
				parent.layout();				
			}
		});
		GridData gd = new GridData();
		gd.minimumWidth = 150;
		gd.widthHint = 150;
		fExtensionCombo.setLayoutData(gd);
	}
	
	private void setStartFileExtension(String[] possibleExtensions) {
		String ext = ReqUIPlugin.getLastExtension();
		if (ext != null && !ext.isEmpty()){
			for (String posExt: possibleExtensions) {
				if (posExt.endsWith(ext) && posExt.length() == ext.length() + 1) {
					fExtensionCombo.setText(posExt);
				}
			}
		}
	}
	
	protected void createIDComposite(Composite parent) {
		Composite idComposite = new Composite(parent, SWT.NONE);
		idComposite.setLayout(new GridLayout(2, false));
		idComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));		
		Label label = new Label(idComposite, SWT.NONE);
		label.setText(Messages.AbstractNewFilePage_IdLabel);
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
		builder.append(fNewFileName.getText());
		builder.append(fExtensionCombo.getText());
		return builder.toString();
	}
	
	protected void createPositionComposite(Composite parent){
		Label position = new Label(parent, SWT.NONE);
		position.setText(getPositionText());
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
	
	protected String getPositionText() {
		return Messages.AbstractNewFilePage_AddFileLabel;
	}
	
	protected abstract void setPositionButtonValues();

	protected void updateName() {
		if (fAutoNumerationEnabled && !fIsChanged) {			
			if (fStart.getSelection()) {
				fNewFileName.setText(fStartBeforeName);
			} else {
				fNewFileName.setText(fStartAfterName);
			}					
			fIsChanged = false;
		}		
	}
	
	protected boolean validate(){		
		IStatus status = getValidateStatus();
		if (status.isOK()){
			setErrorMessage(null);
			fReportFile = ReportRefPresentation.EXTENSION_WITHOUT_DOT.equals(getExtension());
			return true;
		} else if (status.getSeverity() == IStatus.INFO){
			setMessage(status.getMessage());
			return false;
		} else if (status.getSeverity() == IStatus.ERROR){
			setErrorMessage(status.getMessage());
			return false;
		}
		return false;
	}
	
	
	protected abstract IStatus getValidateStatus();
	
	//================
	// getters & setters
	
	public String getExtension() {
		String extension = fExtensionCombo.getText();
		if (extension.startsWith(".")) {
			extension = extension.substring(1);
		}
		return extension;
	}
	
	public void setAutoNumeration(String start, String end) {
		fAutoNumerationEnabled = true;
		fStartBeforeName = start;
		fStartAfterName = end;
	}
	
	public IFile getFile(){
		return fNewFile;
	}

	public void setBeforeMode() {
		fBeforeMode = true;
	}
	
	public boolean isStart() {
		return fStart.getSelection();
	}

	public boolean isReportRef() {
		return fReportFile;
	}	
}
