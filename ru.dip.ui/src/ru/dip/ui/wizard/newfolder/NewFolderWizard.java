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
package ru.dip.ui.wizard.newfolder;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;

import ru.dip.core.model.DipReservedFolder;
import ru.dip.core.model.interfaces.IDipElement;
import ru.dip.core.model.interfaces.IDipParent;
import ru.dip.core.table.TableWriter;
import ru.dip.core.utilities.DipTableUtilities;
import ru.dip.core.utilities.DipUtilities;
import ru.dip.core.utilities.ReservedUtilities;
import ru.dip.core.utilities.ResourcesUtilities;
import ru.dip.core.utilities.WorkbenchUtitlities;
import ru.dip.ui.Messages;
import ru.dip.ui.table.editor.DipEditorUpdater;
import ru.dip.ui.utilities.image.ImageProvider;

public class NewFolderWizard extends Wizard implements INewWizard  {

	public static final String WIZARD_ID = "ru.dip.new.folder"; //$NON-NLS-1$

	private NewFolderPage fPage;
	private IDipParent fParent;
	private NewFolderNumberingPage fNumberingPage;


	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
	    setWindowTitle(Messages.NewFolderWizard_Title);
	    setNeedsProgressMonitor(true);
	    initializeDefaultPageImageDescriptor();	    
	}
	
	protected void initializeDefaultPageImageDescriptor() {
		ImageDescriptor desc = ImageProvider.NEW_FOLDER_WIZ;
		setDefaultPageImageDescriptor(desc);
   }

	@Override
	public void addPages() {
		fParent = WorkbenchUtitlities.getSelectedDipContainerFromProjectExplorer();
		fPage = new NewFolderPage(fParent);
		if (fParent.isFolderNumeration()) {
			String start = DipTableUtilities.getStartFolderNumber(fParent);
			String end = DipTableUtilities.getEndFolderNumber(fParent);
			fPage.setAutoNumeration(start, end);
		}				
		fPage.setTitle(Messages.NewFolderWizard_PageTitle);
		fPage.setDescription(Messages.NewFolderWizard_PageDescription);		
		addPage(fPage);
		fNumberingPage = new NewFolderNumberingPage(Messages.NewFolderWizard_NumberingPageName, fParent);
		addPage(fNumberingPage);
	}
	
	@Override
	public boolean performFinish() {
		boolean isStart = fPage.isStart();
		String description = fPage.getFolderDescription();
		boolean result;
		if (fPage.isReservedFolder()) {
			unreserveFolder();
		} else {
			createFolder();
		}
		IFolder folder = fPage.getFolder();
		if (!DipUtilities.isServedFolder(folder)) {
			result = createDipFolder(folder, isStart, description);
			if (!result) {
				return false;
			}
		}
		ResourcesUtilities.updateProject(folder);
		WorkbenchUtitlities.updateProjectExplorer();
		WorkbenchUtitlities.selectAndReveal(folder);
		DipEditorUpdater.updateNewRes(folder);
		return true;
	}

	private boolean unreserveFolder() {
		IFolder folder = fPage.getFolder();
		IDipElement element = fPage.getParent().getChild(folder.getName());
		if (element instanceof DipReservedFolder && folder.exists()) {
			DipReservedFolder reservedFolder = (DipReservedFolder) element;
			ReservedUtilities.deleteUnreserveMarker(reservedFolder, getShell());
			return true;
		}
		return false;
	}

	private boolean createDipFolder(IFolder folder, boolean isStart, String description) {
		if (folder == null) {
			return false;
		}
		IDipParent dipParent;
		if (isStart) {
			dipParent = DipTableUtilities.addNewFolderStart(fPage.getParent(), folder);
		} else {
			dipParent = DipTableUtilities.addNewFolderEnd(fPage.getParent(), folder);
		}	
		// description
		if (description != null && !description.isEmpty()) {
			dipParent.updateDescription(description);
		}	
		// set autonumbering
		if (dipParent != null){
			if (isFileAutoNumbering()){
				dipParent.setFileStep(getFileStep());				
			}
			if (isFolderAutoNumbering()){
				dipParent.setFolderStep(getFolderStep());		
			}			
		}	
		try {
			TableWriter.saveModel(dipParent.parent());
			ResourcesUtilities.updateProject(dipParent.parent().resource());
		} catch (ParserConfigurationException | IOException e) {
			e.printStackTrace();
		}
		return true;
	}

	private IFolder createFolder() {
		IFolder folder = fPage.getFolder();
		try {
			return ResourcesUtilities.createFolder(folder, getShell());
		} catch (CoreException e) {
			e.printStackTrace();
			MessageDialog.openError(getShell(), Messages.NewFolderWizard_ErrorTitle, Messages.NewFolderWizard_CreateFolderError);
			return null;
		}
	}
	
	//======================
	// getters
	
	public String getNewFolderName(){
		return fPage.getFolderName();
	}
	
	public boolean isFileAutoNumbering() {
		return fNumberingPage.isFileAutoNumbering();
	}

	public boolean isFolderAutoNumbering() {
		return fNumberingPage.isFolderAutoNumbering();
	}

	public String getFileStep() {
		return fNumberingPage.getFileStep();
	}
	
	public String getFolderStep(){
		return fNumberingPage.getFolderStep();
	}

}
