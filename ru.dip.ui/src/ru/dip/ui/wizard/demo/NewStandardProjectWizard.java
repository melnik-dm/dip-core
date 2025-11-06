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
package ru.dip.ui.wizard.demo;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;

import ru.dip.core.model.DipProject;
import ru.dip.core.utilities.DipUtilities;
import ru.dip.core.utilities.WorkbenchUtitlities;
import ru.dip.ui.Messages;

public class NewStandardProjectWizard extends Wizard implements INewWizard {

	private NewDemoProjectPage fPage;
	private boolean fPerformFinish = false;
	
	public NewStandardProjectWizard() {
	    setWindowTitle(Messages.NewStandardProjectWizard_Title);
	}
	
	@Override
	public void addPages() {
		fPage = new NewDemoProjectPage(Messages.NewStandardProjectWizard_DemoProjectPageTitle, Messages.NewStandardProjectWizard_DemoProjectPageTitleDescription, Messages.NewStandardProjectWizard_dip_project);
		addPage(fPage);
	}

	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
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
					String name = fPage.getProjectName();	
					DipProject dipProject = DipUtilities.createStandartProject(name, getShell());		
					if (dipProject != null) {
						dipProject.refresh();
						WorkbenchUtitlities.selectAndReveal(dipProject);
						fPerformFinish = true;
					}
				});
			}
		};
	}

}
