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
package ru.dip.ui.wizard.newtablefile;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;

import ru.dip.core.model.interfaces.IDipDocumentElement;
import ru.dip.core.model.reports.Report;
import ru.dip.ui.Messages;
import ru.dip.ui.ReqUIPlugin;
import ru.dip.ui.utilities.image.ImageProvider;

public class TableNewFileWizard extends Wizard implements INewWizard {

	private TableNewFilePage fPage;
	private NewReportRefPage fReportRefPage;
	
	private boolean fBeforeMode; // добавлять файл в начало / конец (либо до/после указанного файла)
	private String fFileName;
	private String fStartBeforeName;
	private String fStartAfterName;
	private boolean fAutoNumerationEnabled = false;
	
	// файл до (либо после) которого создается новый файл, либо директория внутри которой создается
	private IDipDocumentElement fRelative;
	
	// при создании отчета
	private boolean fReportRef;
	private Report fReport;
	private boolean fRelativeReportRef;

	public TableNewFileWizard(IDipDocumentElement relative,  boolean beforeMode) {
		setWindowTitle(Messages.TableNewFileWizard_Title);
		initializeDefaultPageImageDescriptor();
		fRelative = relative;
		fBeforeMode = beforeMode;
	}

	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		setWindowTitle(Messages.TableNewFileWizard_Title2);
		setNeedsProgressMonitor(true);
		initializeDefaultPageImageDescriptor();
	}

	protected void initializeDefaultPageImageDescriptor() {
		ImageDescriptor desc = ImageProvider.NEW_FILE_WIZ;
		setDefaultPageImageDescriptor(desc);
	}

	@Override
	public void addPages() {
		fPage = new TableNewFilePage(fRelative);
		if (fBeforeMode) {
			fPage.setBeforeMode();
		}
		if (fAutoNumerationEnabled) {
			fPage.setAutoNumeration(fStartBeforeName, fStartAfterName);
		}
		
		fPage.setTitle(Messages.TableNewFileWizard_PageTitle);
		fPage.setDescription(Messages.TableNewFileWizard_PageDescription);
		addPage(fPage);
				
		fReportRefPage = new NewReportRefPage(fRelative.dipProject());
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
		fBeforeMode = fPage.isStart();
		fFileName = fPage.getFileName();
		if (fPage.isReportRef()) {
			fReportRef = true;
			fReport = fReportRefPage.getReport();
			fRelativeReportRef = fReportRefPage.getRelativeReportRef();
		}		
		updateLastFileExtension();
		return true;
	}
	
	private void updateLastFileExtension() {
		   String fileExtension = fPage.getExtension();
		   ReqUIPlugin.setLastExtension(fileExtension);	  
	}
	
	public void setAutoNumeration(String start, String end) {
		fStartBeforeName = start;
		fStartAfterName = end;
		fAutoNumerationEnabled = true;
	}
	
	public String getFileName() {
		return fFileName;
	}

	public boolean isStart() {
		return fBeforeMode;
	}
	
	public boolean isReportRef() {
		return fReportRef;
	}

	public Report getReport() {
		return fReport;
	}

	public boolean getRelativeReportRef() {
		return fRelativeReportRef;
	}
}
