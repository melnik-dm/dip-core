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
package ru.dip.ui.wizard.importfile;

import org.eclipse.core.resources.IContainer;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;

import ru.dip.core.model.interfaces.IDipDocumentElement;
import ru.dip.ui.Messages;
import ru.dip.ui.utilities.image.ImageProvider;

public class ImportFileWizard extends Wizard implements INewWizard  {

	public static final String WIZARD_ID = "ru.dip.new.file"; //$NON-NLS-1$

	private ImportFilePage fPage;
	private boolean fBeforeMode; // добавлять файл в начало / конец
	private IDipDocumentElement fRelative;
	private String fImportFilePath;
	private IContainer fParentContainer;
	private String fImportFileName;
	
	
	public ImportFileWizard(IDipDocumentElement relative,  boolean beforeMode) {
		setWindowTitle(Messages.ImportFileWizard_Title);
		initializeDefaultPageImageDescriptor();
		fRelative = relative;
		fBeforeMode = beforeMode;	}
	

	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
	    setWindowTitle(Messages.ImportFileWizard_Title2);
	    setNeedsProgressMonitor(true);
	    initializeDefaultPageImageDescriptor();	    
	}
	
	protected void initializeDefaultPageImageDescriptor() {
		ImageDescriptor desc = ImageProvider.NEW_FILE_WIZ;
		setDefaultPageImageDescriptor(desc);
   }

	@Override
	public void addPages() {
		fPage = new ImportFilePage(fRelative);
		if (fBeforeMode) {
			fPage.setBeforeMode();
		}
		fPage.setTitle(Messages.ImportFileWizard_Page_Title);
		fPage.setDescription(Messages.ImportFileWizard_Page_Description);
		addPage(fPage);
	}

   @Override
	public boolean performFinish() {
		fBeforeMode = fPage.isStart();
		fImportFilePath = fPage.getImportFilePath();
		fParentContainer = fPage.getParentContainer();
		fImportFileName = fPage.getImportFileName();
       return true;
   }
  
	public boolean isStart() {
		return fBeforeMode;
	}
	
	public String getImportPath() {
		return fImportFilePath;		
	}
	
	public IContainer getParentContainer() {
		return fParentContainer;
	}
	
	public String getImportFileName() {
		return fImportFileName;
	}

}
