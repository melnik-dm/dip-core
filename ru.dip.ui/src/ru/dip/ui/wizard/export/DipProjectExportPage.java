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
package ru.dip.ui.wizard.export;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import ru.dip.core.model.DipProject;
import ru.dip.core.utilities.ui.CompositeFactory;
import ru.dip.core.utilities.ui.ControlFactory;
import ru.dip.core.utilities.ui.swt.ColorProvider;
import ru.dip.core.utilities.ui.swt.FontManager;
import ru.dip.ui.Messages;
import ru.dip.ui.utilities.ui.BrowseComposite;
import ru.dip.ui.utilities.ui.BrowseFolderComposite;
import ru.dip.ui.utilities.ui.FileDialogs;

public class DipProjectExportPage extends WizardPage {

	private DipProject fProject;
	private BrowseFolderComposite fOutDirBrowse;
	
	protected DipProjectExportPage(DipProject project, boolean docx) {
		super(Messages.DipProjectExportPage_PageName);
		fProject = project;
		setDescription(Messages.DipProjectExportPage_ExportLabel);
	}
	
	@Override
	public void createControl(Composite parent) {
		Composite composite = CompositeFactory.full(parent);		
		ControlFactory.label(composite, "\n"); //$NON-NLS-1$
		Label projectLabel = new Label(composite, SWT.NONE);
		projectLabel.setText(Messages.DipProjectExportPage_ProjectLabel);
		projectLabel.setFont(FontManager.boldFont);	
		BrowseComposite selectProject = new BrowseComposite(composite, "") { //$NON-NLS-1$
			
			@Override
			protected void perfomButton() {
				DipProject project = FileDialogs.selectDipProject(getShell());
				if (project != null) {
					fProject = project;
					setValue(fProject.name());
				}
				setPageComplete(isPageComplete());
			}			
		};
		selectProject.getTextControl().setEditable(false);
		
		if (fProject != null) {
			selectProject.setValue(fProject.name());
		}
		
		ControlFactory.label(composite, "\n\n"); //$NON-NLS-1$
		Label outLabel = new Label(composite, SWT.NONE);
		outLabel.setText(Messages.DipProjectExportPage_OutputFolderLabel);
		outLabel.setFont(FontManager.boldFont);		
		fOutDirBrowse = new BrowseFolderComposite(composite, "");	 //$NON-NLS-1$
		fOutDirBrowse.getTextControl().addModifyListener(new ModifyListener() {
			
			@Override
			public void modifyText(ModifyEvent e) {
				Path path = Paths.get(fOutDirBrowse.getValue());
				if (!Files.exists(path) || !Files.isDirectory(path)){
					fOutDirBrowse.getTextControl().setForeground(ColorProvider.RED);
				} else {
					fOutDirBrowse.getTextControl().setForeground(null);
				}
			}
		});
		setOutFolder();
		setControl(composite);
	}
	
	private void setOutFolder(){
		if (fProject != null) {
			String outPath = fProject.getProjectProperties().getExportOut();
			if (outPath != null && !outPath.isEmpty()){
				fOutDirBrowse.setValue(outPath);
			}
		}
	}
	
	@Override
	public boolean isPageComplete() {
		return fProject != null;
	}
	
	public DipProject project() {
		return fProject;
	}
	
	public String outDir() {
		return fOutDirBrowse.getValue();
	}
	
}
