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
package ru.dip.ui.wizard.tablenewfolder;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;

import ru.dip.core.model.interfaces.IDipParent;
import ru.dip.ui.Messages;
import ru.dip.ui.utilities.image.ImageProvider;
import ru.dip.ui.wizard.newfolder.NewFolderNumberingPage;
import ru.dip.ui.wizard.newfolder.NewFolderWizard;

public class TableNewFolderWizard extends NewFolderWizard implements INewWizard  {

	private TableNewFolderPage fPage;
	private NewFolderNumberingPage fNumberingPage;
	private IDipParent fDipParent;
	private IDipParent fRelative;
	
	private boolean fBeforeMode; // добавлять файл в начало / конец
	private String fStartBeforeName;
	private String fStartAfterName;
	private boolean fAutoNumerationEnabled = false;
	// result
	private String fDescription;
	private boolean fReservedFolder;
	private String fFolderName;

	public TableNewFolderWizard(IDipParent dipParent, IDipParent relative, boolean beforeMode) {
		setWindowTitle(Messages.TableNewFolderWizard_Title);
		fDipParent = dipParent;		
		fRelative = relative;
		fBeforeMode = beforeMode;
	}
	
	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
	    setWindowTitle(Messages.TableNewFolderWizard_Title);
	    setNeedsProgressMonitor(true);
	    initializeDefaultPageImageDescriptor();	    
	}
	
	protected void initializeDefaultPageImageDescriptor() {
		ImageDescriptor desc = ImageProvider.NEW_FOLDER_WIZ;
		setDefaultPageImageDescriptor(desc);
   }

	@Override
	public void addPages() {
		fPage = new TableNewFolderPage(fDipParent);
		fPage.setTitle(Messages.TableNewFolderWizard_PageTitle);
		fPage.setDescription(Messages.TableNewFolderWizard_PageDescription);
		if (fRelative != null) {
			fPage.setRelativeParent(fRelative);
		}
		if (fBeforeMode) {
			fPage.setBeforeMode();
		}
		if (fAutoNumerationEnabled) {
			fPage.setAutoNumeration(fStartBeforeName, fStartAfterName);
		}		
		addPage(fPage);
		fNumberingPage = new NewFolderNumberingPage(Messages.TableNewFolderWizard_PageTitle2, fDipParent);
		addPage(fNumberingPage);
	}

	@Override
	public boolean performFinish() {
		fBeforeMode = fPage.isStart();
		fDescription = fPage.getFolderDescription();
		fReservedFolder = fPage.isReservedFolder();
		fFolderName = fPage.getFolderName();
		return true;
	}

	//=========================
	// getters & setters

	public void setAutoNumeration(String start, String end) {
		fStartBeforeName = start;
		fStartAfterName = end;
		fAutoNumerationEnabled = true;
	}
	
	public String getNewFolderName(){
		return fPage.getFolderName();
	}
	
	/**
	 * Если папка была зарезервированна
	 */
	public boolean isReserved() {
		return fReservedFolder;
	}
	
	public boolean isStart() {
		return fBeforeMode;
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
	
	public String getFolderDescription() {
		return fDescription;
	}
	
	public String getFolderName() {
		return fFolderName;
	}
}
