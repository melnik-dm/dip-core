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
package ru.dip.ui.wizard.report;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.wizard.Wizard;

import ru.dip.core.DipCorePlugin;
import ru.dip.core.model.DipProject;
import ru.dip.core.model.interfaces.IParent;
import ru.dip.core.model.reports.IReportContainer;
import ru.dip.core.utilities.ReportUtils;
import ru.dip.core.utilities.WorkbenchUtitlities;
import ru.dip.ui.Messages;

public class SaveToReportWizard extends Wizard {

	private final DipProject fDipProject;
	private final String fFilter;
	
	private SaveReportPage fPage;
	
	public SaveToReportWizard(DipProject dipProject, String filterText) {
		fDipProject = dipProject;
		fFilter = filterText;
		setWindowTitle(Messages.SaveToReportWizard_WindowTitle);
	}
	
	@Override
	public void addPages() {
		fPage = new SaveReportPage(fDipProject);
		addPage(fPage);
	}

	@Override
	public boolean performFinish() {
		IParent parent = fPage.getDipParent();
		String fileName = fPage.getFileName();
		String reportName = fPage.getReportName();
		String ruleName = fPage.getRuleName();	
		String content = ReportUtils.createReportContentFromFilter(reportName, ruleName, fFilter);

		try {
			IReportContainer reportContainer = ReportUtils.getReportFolder(parent);		
			if (reportContainer != null) {
				ReportUtils.createNewReport(fileName, reportContainer, content, getShell());
			} else {
				DipCorePlugin.logError("Get report container error. " + parent); //$NON-NLS-1$
				throw new RuntimeException();
			}		
		} catch (CoreException e) {
			e.printStackTrace();
			WorkbenchUtitlities.openError("Сохранение в отчет", "Ошибка при создании файла"); //$NON-NLS-1$ //$NON-NLS-2$
		}
		
		return true;
	}

}
