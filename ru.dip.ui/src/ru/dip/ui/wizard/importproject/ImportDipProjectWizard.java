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
package ru.dip.ui.wizard.importproject;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IImportWizard;
import org.eclipse.ui.IWorkbench;

import ru.dip.core.model.DipProject;
import ru.dip.core.utilities.DipUtilities;
import ru.dip.core.utilities.WorkbenchUtitlities;
import ru.dip.ui.Messages;
import ru.dip.ui.utilities.image.ImageProvider;

public class ImportDipProjectWizard extends Wizard implements IImportWizard {

	private ImportDipProjectPage fPage;
	private String fProjectPath;
	
	public ImportDipProjectWizard() {
	}
	
	/**
	 * При импорте перетаскиванеим
	 */
	public ImportDipProjectWizard(String projectPath) {
		fProjectPath = projectPath;
	}
	
	@Override
	public String getWindowTitle() {
		return "Import DIP Project";
	}
	
	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
	    setWindowTitle(Messages.ImportDipProjectWizard_Title);
	    initializeDefaultPageImageDescriptor();	
	}
	
	protected void initializeDefaultPageImageDescriptor() {
		ImageDescriptor desc = ImageProvider.NEW_PROJECT_WIZ;
		setDefaultPageImageDescriptor(desc);
   }
	
	@Override
	public void addPages() {
		fPage = new ImportDipProjectPage(fProjectPath);
		addPage(fPage);
	}

	@Override
	public boolean performFinish() {
		String projectPath = fPage.getProjectPath();
		String projectName = fPage.getProjectName();
		boolean copy = fPage.isCopy();
		DipProject dipProject = DipUtilities.importDipProject(projectName, projectPath, copy, getShell());
		if (dipProject != null) {
			dipProject.refresh();
			WorkbenchUtitlities.selectAndReveal(dipProject);
		}
		return true;
	}
	
}
