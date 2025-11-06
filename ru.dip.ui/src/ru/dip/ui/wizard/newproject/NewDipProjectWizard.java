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
package ru.dip.ui.wizard.newproject;

import java.lang.reflect.InvocationTargetException;
import java.net.URI;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.dialogs.WizardNewProjectCreationPage;
import org.eclipse.ui.wizards.newresource.BasicNewProjectResourceWizard;

import ru.dip.core.manager.DipNatureManager;
import ru.dip.core.manager.DipProjectResourceCreator;
import ru.dip.core.model.DipProject;
import ru.dip.core.model.DipRoot;
import ru.dip.core.utilities.DipUtilities;
import ru.dip.core.utilities.ResourcesUtilities;
import ru.dip.core.utilities.WorkbenchUtitlities;

public class NewDipProjectWizard extends BasicNewProjectResourceWizard implements INewWizard {

	private WizardNewProjectCreationPage fPage;
	private boolean fPerformFinish = false;

	@Override
	public void addPages() {
		addStandardNewProjectPage();
	}

	private void addStandardNewProjectPage() {
		fPage = new WizardNewProjectCreationPage("basicNewProjectPage") { //$NON-NLS-1$
			@Override
			public void createControl(Composite parent) {
				super.createControl(parent);
				createWorkingSetGroup((Composite) getControl(), getSelection(),
						new String[] { "org.eclipse.ui.resourceWorkingSetPage" }); //$NON-NLS-1$
				Dialog.applyDialogFont(getControl());
			}
		};
		fPage.setTitle("Project");
		fPage.setDescription("Create a new project resource");
		addPage(fPage);
	}

	@Override
	public boolean canFinish() {
		String projectName = page().getProjectName();
		IStatus status = DipUtilities.checkReqName(projectName);
		if (!status.isOK()) {
			fPage.setErrorMessage(status.getMessage());
			return false;
		}
		fPage.setErrorMessage(null);
		return super.canFinish();
	}

	private WizardNewProjectCreationPage page() {
		if (fPage == null) {
			fPage = (WizardNewProjectCreationPage) getPage("basicNewProjectPage");
		}
		return fPage;
	}

	@Override
	public boolean performFinish() {
		try {
			WorkbenchUtitlities.runWithCursorBusy(getFinishFunction());
		} catch (InvocationTargetException | InterruptedException ignore) {
			// NOP
		}
		return fPerformFinish;
	}

	private IRunnableWithProgress getFinishFunction() {
		return new IRunnableWithProgress() {

			@Override
			public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
				Display.getDefault().asyncExec(() -> {
					IProject project = createResourceProject();
					if (project != null && project.exists()) {
						ResourcesUtilities.updateProject(project);
						DipProject dipProject = createDipProject(project);
						WorkbenchUtitlities.selectAndReveal(dipProject);
						fPerformFinish = true;
					}
				});
			}
		};
	}

	private IProject createResourceProject() {
		WizardNewProjectCreationPage page = page();
		String name = page.getProjectName();
		try {
			if (page.useDefaults()) {
				return ResourcesUtilities.createProject(name, getShell(), DipNatureManager.NATURE_ID);
			} else {
				URI location = page.getLocationURI();
				return ResourcesUtilities.createProject(name, location, getShell(), DipNatureManager.NATURE_ID);
			}
		} catch (CoreException e) {
			e.printStackTrace();
			return null;
		}
	}

	private DipProject createDipProject(IProject project) {
		DipProject dipProject = DipRoot.getInstance().getDipProject(project);
		DipProjectResourceCreator.createDipProject(dipProject);
		return dipProject;
	}

}
