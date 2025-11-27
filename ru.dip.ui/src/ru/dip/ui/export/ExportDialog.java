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
package ru.dip.ui.export;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import ru.dip.core.model.DipProject;
import ru.dip.core.utilities.ArrayUtils;
import ru.dip.core.utilities.ui.CompositeBuilder;
import ru.dip.core.utilities.ui.swt.ColorProvider;
import ru.dip.core.utilities.ui.swt.FontManager;
import ru.dip.ui.Messages;
import ru.dip.ui.utilities.ui.BrowseFolderComposite;

public class ExportDialog extends Dialog {
	
	// model
	private DipProject fDipProject;	
	// result
	private String fTagetPath;
	private String fConfigName;
	
	// controls
	private BrowseFolderComposite fOutDirBrowse;
	private Combo fConfigCombo;
		
	public ExportDialog(Shell shell, DipProject project) {
		super(shell);
		fDipProject = project;
		
	}
	
	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(Messages.ExportDialog_Title);
	}
	
	@Override
	protected Point getInitialSize() {
		Point p = super.getInitialSize();
		if (p.x < 640) {
			p.x = 640;
		}
		if (p.y < 260) {
			p.y = 260;
		}
		return p;
	}
	
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.marginTop = 5;
		composite.setLayout(layout);
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));		
		createConfigComposite(composite);
		createOutComposite(composite);
		createExtensionComposite(composite);
		setValues();
		addValidateListeners();
		return composite;
	}
	
	private void createConfigComposite(Composite parent){
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout());			
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		Label configLabel = new Label(composite, SWT.NONE);		
		configLabel.setText(Messages.ExportDialog_ExportConfig);
		configLabel.setFont(FontManager.boldFont);
		fConfigCombo = new Combo(composite, SWT.READ_ONLY);
		fConfigCombo.setItems(fDipProject.getExportConfigs());
		fConfigCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
	}
	
	private void createOutComposite(Composite parent){
		Composite composite = CompositeBuilder.instance(parent).horizontal().build();	
		Label outLabel = new Label(composite, SWT.NONE);
		outLabel.setText(Messages.ExportDialog_TargetFolder);
		outLabel.setFont(FontManager.boldFont);
		fOutDirBrowse = new BrowseFolderComposite(composite, ""); //$NON-NLS-1$
	}
	
	protected void createExtensionComposite(Composite parent) {}
		
	private void setValues(){
		setOutFolder();
		setExportConfig();
	}
		
	private void setOutFolder(){
		String outPath = fDipProject.getProjectProperties().getExportOut();
		if (outPath != null && !outPath.isEmpty()){
			fOutDirBrowse.setValue(outPath);
		}
	}
	
	private void setExportConfig(){
		String config = fDipProject.getProjectProperties().getExportConfig();
		if (config != null && !config.isEmpty()){
			if (ArrayUtils.arrayContainsElement(fDipProject.getExportConfigs(), config)){
				fConfigCombo.setText(config);
			}			
		}
	}
	
	private void addValidateListeners() {
		fConfigCombo.addModifyListener(e -> validate());
		fOutDirBrowse.getTextControl().addModifyListener(new ModifyListener() {
			
			@Override
			public void modifyText(ModifyEvent e) {
				if (isValidOutDir()) {
					fOutDirBrowse.getTextControl().setForeground(null);
				} else {
					fOutDirBrowse.getTextControl().setForeground(ColorProvider.RED);
				}
				validate();
			}
		});
	}
	
	@Override
	protected Control createButtonBar(Composite parent) {
		Control control =  super.createButtonBar(parent);
		validate();		
		return control;
	}
	
	private void validate() {
		getButton(OK).setEnabled(isValid());		
	}
	
	private boolean isValid() {
		return !fConfigCombo.getText().isEmpty()
				&& isValidOutDir();				
	}
	
	private boolean isValidOutDir() {
		if (fOutDirBrowse.getValue().isEmpty()) {
			return false;
		}
		Path path = Paths.get(fOutDirBrowse.getValue());
		return Files.exists(path) && Files.isDirectory(path);
	}
	
	
	@Override
	protected void okPressed() {		
		doOkPressed();
		super.okPressed();
	}
	
	protected void doOkPressed() {
		fTagetPath = fOutDirBrowse.getValue();
		fConfigName = fConfigCombo.getText();
	}
	
	public String getTargetPath() {
		return fTagetPath;
	}
	
	public String getConfigName() {
		return fConfigName;
	}
}
