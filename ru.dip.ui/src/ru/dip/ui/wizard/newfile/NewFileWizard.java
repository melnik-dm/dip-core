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
package ru.dip.ui.wizard.newfile;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;

import ru.dip.core.model.interfaces.IDipParent;
import ru.dip.core.utilities.DipTableUtilities;
import ru.dip.core.utilities.DipUtilities;
import ru.dip.core.utilities.ResourcesUtilities;
import ru.dip.core.utilities.WorkbenchUtitlities;
import ru.dip.ui.Messages;
import ru.dip.ui.ReqUIPlugin;
import ru.dip.ui.utilities.image.ImageProvider;
import ru.dip.ui.wizard.newtablefile.NewReportRefPage;

public class NewFileWizard extends Wizard implements INewWizard  {

	public static final String WIZARD_ID = "ru.dip.new.file"; //$NON-NLS-1$

	private NewFilePage fPage;
	private NewReportRefPage fReportRefPage;
	private IDipParent fParent;

	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
	    setWindowTitle(Messages.NewFileWizard_Title);
	    setNeedsProgressMonitor(true);
	    initializeDefaultPageImageDescriptor();	    
	}
	
	protected void initializeDefaultPageImageDescriptor() {
		ImageDescriptor desc = ImageProvider.NEW_FILE_WIZ;
		setDefaultPageImageDescriptor(desc);
   }

	@Override
	public void addPages() {
		fParent = WorkbenchUtitlities.getSelectedDipContainerFromProjectExplorer();
		fPage = new NewFilePage(fParent);
		if (fParent.isFileNumeration()) {
			String start = DipTableUtilities.getStartNumberInFolder(fParent);
			String end = DipTableUtilities.getEndNumberInFolder(fParent);
			fPage.setAutoNumeration(start, end);
		}		
		fPage.setTitle(Messages.NewFileWizard_FilePageTitle);
		fPage.setDescription(Messages.NewFileWizard_CreateFileLabel);
		addPage(fPage);
		
		fReportRefPage = new NewReportRefPage(fParent.dipProject());
		addPage(fReportRefPage);
	}
	
	@Override
	public boolean canFinish() {
		if (!fPage.isPageComplete()) {
			return false;
		}
		if (fPage.isReportRef()) {
			return fReportRefPage.isPageComplete();
		}
		return true;
	}
	
   @Override
	public boolean performFinish() {
       // create file
	   IFile file = createFile();	   
       if (file == null) {
			return false;
       }
       
       ifFormFile(file);
       ifDiaFile(file);
       ifReportRefFile(file);
       
       if (fPage.isStart()) {
			DipTableUtilities.addNewFileStart(fPage.getParent(), file);
       } else {
			DipTableUtilities.addNewFileEnd(fPage.getParent(), file);
       }
       
       updateAndSave(file);
       updateFileExtension(file);
       return true;
   }

   private IFile createFile(){
	   IFile file = fPage.getFile();
		try {
			return ResourcesUtilities.createFile(file, getShell());
		} catch (CoreException e) {
			e.printStackTrace();			
			MessageDialog.openError(getShell(), Messages.NewFileWizard_ErrorDialogTitle, Messages.NewFileWizard_CreateFileErrorMessage);
			return null;
		}
   }
   
   private void ifFormFile(IFile file) {
       if (DipUtilities.isFormFile(file)){
    	   DipUtilities.createFormFile(file);
       }
   }
   
   private void ifDiaFile(IFile file) {
	   if (DipUtilities.isDiaFile(file)) {
			DipUtilities.createDiaFile(file);
	   }
   }
   
   private void ifReportRefFile(IFile file) {
	   if (fPage.isReportRef()) {
			DipUtilities.createReportRefFile(file, fReportRefPage.getReport(), fReportRefPage.getRelativeReportRef());
	   }
   }
   
   private void updateFileExtension(IFile file) {
	   String fileExtension = file.getFileExtension();
	   if (fileExtension == null) {
		   fileExtension = ""; //$NON-NLS-1$
	   }
	   ReqUIPlugin.setLastExtension(fileExtension);	   
   }

	private void updateAndSave(IFile file) {
		DipTableUtilities.saveModel(fParent);
		ResourcesUtilities.updateProject(file);
		WorkbenchUtitlities.updateProjectExplorer();
		WorkbenchUtitlities.selectAndReveal(file);
		WorkbenchUtitlities.openFile(file);
		fPage.getParent().dipProject().updateNumeration();
	}

}
