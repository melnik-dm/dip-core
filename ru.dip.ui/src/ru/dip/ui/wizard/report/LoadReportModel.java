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

import java.util.ArrayList;
import java.util.List;

import ru.dip.core.model.DipProject;
import ru.dip.core.model.reports.Report;
import ru.dip.core.report.model.report.ReportEntry;

public class LoadReportModel {

	public static interface ILoadReportModelListener {
		void modelChaned();
	}

	private final DipProject fDipProject;
	private Report fReport;
	private List<ReportEntry> fEntries;
	private ReportEntry fReportEntry;

	private List<ILoadReportModelListener> fListeners = new ArrayList<>();

	public LoadReportModel(DipProject dipProject) {
		fDipProject = dipProject;
	}

	public void clear() {
		fReport = null;
		fEntries = null;
		fReportEntry = null;
		fListeners.forEach(ILoadReportModelListener::modelChaned);
	}

	public void addListener(ILoadReportModelListener listener) {
		fListeners.add(listener);
	}

	// =====================
	// getters & setters

	public Report getReport() {
		return fReport;
	}

	public void setReport(Report report) {
		fReport = report;
	}

	public List<ReportEntry> getEntries() {
		return fEntries;
	}

	public void setEntries(List<ReportEntry> entries) {
		fEntries = entries;
		fListeners.forEach(ILoadReportModelListener::modelChaned);
	}

	public DipProject getDipProject() {
		return fDipProject;
	}

	public void setReportEntry(ReportEntry reportEntry) {
		fReportEntry = reportEntry;
	}

	public ReportEntry getReportEntry() {
		return fReportEntry;
	}

}
