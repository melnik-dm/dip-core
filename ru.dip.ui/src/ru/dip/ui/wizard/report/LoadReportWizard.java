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

import org.eclipse.jface.wizard.Wizard;

import ru.dip.core.model.DipProject;
import ru.dip.ui.Messages;

public class LoadReportWizard extends Wizard {

	private LoadReportPage fLoadReportPage;
	private SelectRulePage fSelectRulePage;
	private final LoadReportModel fModel;
	
	public LoadReportWizard(DipProject dipProject) {
		fModel = new LoadReportModel(dipProject);
		setWindowTitle(Messages.LoadReportWizard_DialogTitle);
	}
	
	@Override
	public void addPages() {
		fLoadReportPage = new LoadReportPage(fModel);
		fSelectRulePage = new SelectRulePage(fModel);
		addPage(fLoadReportPage);
		addPage(fSelectRulePage);	
	}
	
	@Override
	public boolean canFinish() {		
		return fLoadReportPage.canFinish() || super.canFinish();
	}

	@Override
	public boolean performFinish() {
		return true;
	}
	
	public LoadReportModel getModel() {
		return fModel;
	}

}
