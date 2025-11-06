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

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

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

public class ImportProjectFromGit extends Wizard implements IImportWizard {

	private SelectRepoPage fRepoPage;
	private SelectProjectFromRepoPage fProjectsPage;
	
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
		fRepoPage = new SelectRepoPage();
		addPage(fRepoPage);
		fProjectsPage = new SelectProjectFromRepoPage();
		addPage(fProjectsPage);
	}

	@Override
	public boolean performFinish() {
		List<Path> projectsForImport = fProjectsPage.getSelectedProjects();
		List<DipProject> projects = new ArrayList<DipProject>();
		for (Path path: projectsForImport) {
			DipProject project = DipUtilities.importDipProject(path.getFileName().toString(), path.toString(), false, getShell());
			if (project != null) {
				project.refresh();
				projects.add(project);
			}
		}		
		WorkbenchUtitlities.selectInProjectExploter(projects.toArray());
		return true;
	}

}
