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
package ru.dip.ui.wizard.rename;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;

import ru.dip.core.model.DipElement;
import ru.dip.core.model.DipElementType;
import ru.dip.core.model.interfaces.IDipElement;
import ru.dip.ui.Messages;
import ru.dip.ui.utilities.image.ImageProvider;

public class RenameWizard extends Wizard implements INewWizard  {

	private IRenamePage fPage;
	private IDipElement fDipElement;
	private String fNewName;
	private boolean fNeedReserve;

	public RenameWizard(IDipElement dipElement) {
		fDipElement = dipElement;
		setWindowTitle(Messages.RenameWizard_Title);
		initializeDefaultPageImageDescriptor();
	}
	
	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
	    setWindowTitle(Messages.RenameWizard_Title2);
	    setNeedsProgressMonitor(true);
	    initializeDefaultPageImageDescriptor();	   
	}
	
	protected void initializeDefaultPageImageDescriptor() {
		DipElementType type = fDipElement.type();
		if (type == DipElementType.UNIT || type == DipElementType.REPORT){
			ImageDescriptor desc = ImageProvider.NEW_FILE_WIZ;
			setDefaultPageImageDescriptor(desc);
		} else if (type == DipElementType.FOLDER){
			ImageDescriptor desc = ImageProvider.NEW_FOLDER_WIZ;
			setDefaultPageImageDescriptor(desc);
		} else if (type == DipElementType.RPOJECT){
			ImageDescriptor desc = ImageProvider.NEW_PROJECT_WIZ;
			setDefaultPageImageDescriptor(desc);
		}		
   }

	@Override
	public void addPages() {
		if (fDipElement.type() == DipElementType.REPORT){
			fPage = new RenameReportElementPage("File", (DipElement) fDipElement); //$NON-NLS-1$
		} else if (fDipElement.type() == DipElementType.EXPORT_CONFIG){ 
			fPage = new RenameNoRequiremntPage("File",  (DipElement) fDipElement); //$NON-NLS-1$
		} else {
			fPage = new RenamePage("File", fDipElement); //$NON-NLS-1$
		}
		fPage.setTitle(Messages.RenameWizard_PageTitle);
		fPage.setDescription(Messages.RenameWizard_PaggeDescription);
		addPage(fPage);
	}
	
	@Override
	public boolean performFinish() {
		//doRename();
		fNewName = fPage.getNewName();
		fNeedReserve = fPage.isNeedReserve();		
		return true;
	}
	
	public IDipElement getDipElement() {
		return fDipElement;
	}
	
	public String getName() {
		return fNewName;
	}
	
	public boolean isNeedReserve() {
		return fNeedReserve;
	}
	
}
