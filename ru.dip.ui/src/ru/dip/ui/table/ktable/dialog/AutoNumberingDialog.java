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
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import ru.dip.core.model.interfaces.IDipParent;
import ru.dip.core.utilities.DipUtilities;
import ru.dip.core.utilities.tmp.AutoNumberingSettingResult;
import ru.dip.core.utilities.ui.swt.ColorProvider;
import ru.dip.ui.utilities.ui.EntryTextComposite;

public class AutoNumberingDialog extends Dialog {

	private AutoNumberingSettingResult fResult;
	// control
	private Button fFilesNumerationButton;
	private Button fFoldersNumerationButton;
	private EntryTextComposite fFileStepEntry;
	private EntryTextComposite fFolderStepEntry;
	// model
	private IDipParent fParent;
	
	public AutoNumberingDialog(Shell parentShell, IDipParent parent) {
		super(parentShell);
		fParent = parent;
	}
	
	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Автонумерация");
	}
	
	@Override
	protected Point getInitialSize() {
		Point p = super.getInitialSize();
		if (p.x < 550){
			p.x = 550;
		}
		return p;
	}
		
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(/*2*/1, false);
		layout.marginTop = 15;
		composite.setLayout(layout);
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		createFileNumberingGroup(composite);
		createFolderNumberingGroup(composite);
		setValues();
		return super.createDialogArea(parent);
	}

	private void createFileNumberingGroup(Composite parent){
		Group filesGroup = new Group(parent, SWT.NONE);
		filesGroup.setText("Автонумерация вложенных файлов");
		filesGroup.setLayout(new GridLayout(2 , false));
		filesGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		fFilesNumerationButton = new Button(filesGroup, SWT.CHECK);
		fFilesNumerationButton.setText("Включить");
		fFileStepEntry = new EntryTextComposite(filesGroup, "Шаг: ");			
		fFileStepEntry.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		fFileStepEntry.setEnabled(false);
		
		Label exampleLabel = new Label(filesGroup, SWT.NONE);
		exampleLabel.setText("Пример имени файла:    ");
		exampleLabel.setEnabled(false);
		
		Label exampleID = new Label(filesGroup, SWT.NONE);
		exampleID.setText("  ");
		exampleID.setForeground(ColorProvider.BLUE);
		exampleID.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		fFileStepEntry.addModifyListener(new ModifyListener() {
			
			@Override
			public void modifyText(ModifyEvent e) {
				String exampleIdValue = getExampleID(fFileStepEntry.getValue());
				exampleID.setText(exampleIdValue);
				validate();			
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
					fFileStepEntry.setValue("010");
				}
				
				if (selection){
					String exampleIdValue = getExampleID(fFileStepEntry.getValue());
					exampleID.setText(exampleIdValue);
				} else {
					exampleID.setText("");
				}				
				validate();				
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				
			}
		});
	}
	
	private void createFolderNumberingGroup(Composite parent){
		Group foldersGroup = new Group(parent, SWT.NONE);
		foldersGroup.setText("Автонумерация вложенных каталогов");
		foldersGroup.setLayout(new GridLayout(2, false));
		foldersGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		fFoldersNumerationButton = new Button(foldersGroup, SWT.CHECK);
		fFoldersNumerationButton.setText("Включить");
		fFolderStepEntry = new EntryTextComposite(foldersGroup, "Шаг: ");			
		fFolderStepEntry.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		fFolderStepEntry.setEnabled(false);
		
		Label exampleLabel = new Label(foldersGroup, SWT.NONE);
		exampleLabel.setText("Пример имени каталога:  ");
		exampleLabel.setEnabled(false);
		
		Label exampleID = new Label(foldersGroup, SWT.NONE);
		exampleID.setText("  ");
		exampleID.setForeground(ColorProvider.BLUE);
		exampleID.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		fFolderStepEntry.addModifyListener(new ModifyListener() {
			
			@Override
			public void modifyText(ModifyEvent e) {
				String exampleIdValue = getExampleID(fFolderStepEntry.getValue());
				exampleID.setText(exampleIdValue);
				validate();		
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
					fFolderStepEntry.setValue("010");
				}
				
				if (selection){
					String exampleIdValue = getExampleID(fFolderStepEntry.getValue());
					exampleID.setText(exampleIdValue);
				} else {
					exampleID.setText("");
				}
				validate();			
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				
			}
		});
	}
	
	private String getExampleID(String name){
		String exampleIdValue = fParent.id();
		StringBuilder builder = new StringBuilder();
		builder.append(exampleIdValue);
		if (!exampleIdValue.endsWith("/")){
			builder.append("/");
		}
		builder.append(name);
		return builder.toString();
	}

	private void setValues() {
		if (fParent.isFileNumeration()) {
			fFilesNumerationButton.setSelection(true);
			fFileStepEntry.setValue(fParent.getFileStep());
			fFileStepEntry.setEnabled(true);
		}
		if (fParent.isFolderNumeration()) {
			fFoldersNumerationButton.setSelection(true);
			fFolderStepEntry.setValue(fParent.getFileStep());
			fFolderStepEntry.setEnabled(true);
		}
	}
	
	private void validate(){
		Button button = getButton(OK);
		if (button != null) {
			boolean valid = checkStepNumeration();
			button.setEnabled(valid);
		}
	}
	
	private boolean checkStepNumeration() {
		if (fFilesNumerationButton.getSelection()) {
			String value = fFileStepEntry.getValue().trim();
			try {
				int n = Integer.parseInt(value);
				return (n > 0);
			} catch (NumberFormatException e) {
				return false;
			}
		}
		if (fFoldersNumerationButton.getSelection()) {
			String value = fFolderStepEntry.getValue().trim();
			try {
				int n = Integer.parseInt(value);
				return (n > 0);
			} catch (NumberFormatException e) {
				return false;
			}
		}
		return true;
	}
	
	@Override
	protected void okPressed() {
		fResult = new AutoNumberingSettingResult(DipUtilities.relativeProjectID(fParent), 
				fParent.getFileStep(), fParent.getFolderStep(), fFileStepEntry.getValue(), fFolderStepEntry.getValue());
		fParent.setFileStep(fFilesNumerationButton.getSelection() ?
				fFileStepEntry.getValue() : null);
		fParent.setFolderStep(fFoldersNumerationButton.getSelection() ?
				fFolderStepEntry.getValue() : null);
		super.okPressed();
	}
	
	public AutoNumberingSettingResult getResult() {
		return fResult;
	}
	
}
