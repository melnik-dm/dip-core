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

import ru.dip.core.model.interfaces.IDipParent;

public class IncludeWizard extends Wizard implements INewWizard  {

	private IncludeWizardPage fPage;
	private IDipParent fRelative;
	private IDipParent fParent;
	protected boolean fBeforeMode = false;	
	private boolean fReadOnly = false;
	
	private String fIncludePath;
	private String fIncludeName;
	private String fDescription;

	public IncludeWizard(IDipParent parent){
		fParent = parent;
	}
	
	public IncludeWizard(IDipParent neighbourElement, boolean before){
		fRelative = neighbourElement;
		fBeforeMode = before;
		fParent = neighbourElement.parent();
	}
	
	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		
	}
	
	@Override
	public void addPages() {
		fPage = new IncludeWizardPage(fRelative, fBeforeMode, fParent);
		addPage(fPage);
	}

	@Override
	public boolean performFinish() {
		fIncludePath = fPage.includePath();
		fIncludeName = fPage.includeName();
		fDescription = fPage.description();
		fBeforeMode = fPage.isStart();
		fReadOnly = fPage.isReadOnly();
		return true;
	}

	public String includePath() {
		return fIncludePath;
	}
	
	public String includeName() {
		return fIncludeName;
	}

	public String description() {
		return fDescription;
	}

	public boolean isStart() {
		return fBeforeMode;
	}
	
	public boolean isReadOnly() {
		return fReadOnly;
	}
	
}
