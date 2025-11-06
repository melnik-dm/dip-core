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

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IExportWizard;
import org.eclipse.ui.IWorkbench;

import ru.dip.core.model.DipProject;
import ru.dip.core.utilities.WorkbenchUtitlities;
import ru.dip.ui.Messages;
import ru.dip.ui.export.ExportVersion;
import ru.dip.ui.export.Exporter;

public class DocxExportWizard extends Wizard implements IExportWizard {

	private static final String DOC_CONFIG = Messages.DocxExportWizard_0;
	
	private DipProjectExportPage fPage;
	private DipProject fProject;

	public DocxExportWizard() {
		setWindowTitle(Messages.DocxExportWizard_Title);
	}
	
	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		fProject = WorkbenchUtitlities.getSelectedDipProjectFromProjectExplorer();
	}
	
	@Override
	public void addPages() {
		fPage = new DipProjectExportPage(fProject, true);
		addPage(fPage);
	}
	
	@Override
	public boolean performFinish() {
		String targetPath = fPage.outDir();	
		if (!checkConfigFile()) {
			WorkbenchUtitlities.openError(Messages.DocxExportWizard_ExportErrorTitle, Messages.DocxExportWizard_ExportErrorMessage + configName());
			return false;
		}
		Exporter exporter = new Exporter(fPage.project(), getShell());
		// выбор версии Java
		exporter.doExport(targetPath, configName(), ExportVersion.JAVA);		
		return true;
	}
	
	private boolean checkConfigFile() {
		IFile file = fPage.project().getProject().getFile(configName());
		return file.exists();
	}
	
	protected String configName() {
		return DOC_CONFIG;
	}

}
