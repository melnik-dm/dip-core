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
package ru.dip.core.model.reports;

import java.util.ArrayList;
import java.util.List;

import ru.dip.core.model.interfaces.IDipElement;
import ru.dip.core.model.interfaces.IDipParent;

public class MainReportContainer extends ReportContainer implements IMainReportContainer {

	/**
	 * Главный ReportContainer в проекте без физической папки Reports 
	 * (В старых версиях используется ProjectReportFolder)
	 */
	public MainReportContainer(IDipParent parent) {
		super(parent);
	}

	private List<IReportContainer> fReportContainers = new ArrayList<>();

	@Override
	public List<IDipElement> getChildren() {
		if (getReports() == null) {
			super.computeChildren();
		}

		List<IDipElement> result = new ArrayList<>();
		result.addAll(getReports());
		if (fReportContainers != null) {
			result.addAll(fReportContainers);
		}
		return result;
	}
	
	@Override
	public boolean hasChildren() {
		return super.hasChildren() || !fReportContainers.isEmpty();
	}
	

	@Override
	public void addContainer(IReportContainer reportContainer) {
		fReportContainers.add(reportContainer);
	}

	@Override
	public void removeContainer(IReportContainer originalReportContainer) {
		fReportContainers.remove(originalReportContainer);
	}

}
