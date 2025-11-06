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
package ru.dip.ui.wizard.include;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;

import ru.dip.ui.Messages;

public class SetIncludeLinkWizard extends Wizard implements INewWizard  {

	private SetIncludeLinkPage fPage;
	
	private String fIncludePath;
	private boolean fReadOnly = false;
	private String fOldIncludePath;
	
	public SetIncludeLinkWizard(String linkRelativePath) {
		fOldIncludePath = linkRelativePath;
		setWindowTitle(Messages.IncludeWizardPage_Title);
	}
	
	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
	}
	
	@Override
	public void addPages() {
		fPage = new SetIncludeLinkPage(fOldIncludePath);
		addPage(fPage);
	}

	@Override
	public boolean performFinish() {
		fIncludePath = fPage.includePath();
		fReadOnly = fPage.isReadOnly();
		return true;
	}

	public String includePath() {
		return fIncludePath;
	}
	
	public boolean isReadOnly() {
		return fReadOnly;
	}

}
