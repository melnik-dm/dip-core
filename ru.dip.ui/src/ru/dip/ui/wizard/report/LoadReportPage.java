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

import java.util.List;

import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

import ru.dip.core.model.reports.IReportContainer;
import ru.dip.core.model.reports.Report;
import ru.dip.core.report.model.report.ReportEntry;
import ru.dip.core.report.scanner.ReportReader;
import ru.dip.core.utilities.ui.CompositeBuilder;
import ru.dip.core.utilities.ui.viewer.TreeViewerUtils;
import ru.dip.ui.Messages;
import ru.dip.ui.navigator.NavigatorContentProvider;
import ru.dip.ui.navigator.NavigatorLabelProvider;

public class LoadReportPage extends WizardPage {
	
	private final LoadReportModel fModel;
	
	private TreeViewer fViewer;

	protected LoadReportPage(LoadReportModel model) {
		super("LoadReportPage"); //$NON-NLS-1$
		fModel = model;
		setPageComplete(false);
		setTitle(Messages.LoadReportPage_Title);
	}

	@Override
	public void createControl(Composite parent) {
		Composite composite = CompositeBuilder.instance(parent).full().build();		
		IReportContainer container = fModel.getDipProject().getReportFolder();
		if (container != null) {
			createTreeViewer(composite, container);
		}
		setControl(composite);
	}
	
	private void createTreeViewer(Composite parent, IReportContainer container){
		Composite treeComposite = new Composite(parent, SWT.NONE);
		treeComposite.setLayout(new GridLayout());
		GridData gd = new GridData(GridData.FILL_BOTH);
		gd.minimumHeight = 90;
		treeComposite.setLayoutData(gd);
		fViewer = new TreeViewer(treeComposite);
		fViewer.getTree().setLayoutData(new GridData(GridData.FILL_BOTH));
		fViewer.setContentProvider(new NavigatorContentProvider());
		fViewer.setLabelProvider(new NavigatorLabelProvider());	
		fViewer.addFilter(new ViewerFilter() {
			
			@Override
			public boolean select(Viewer viewer, Object parentElement, Object element) {
				return element instanceof IReportContainer || element instanceof Report;
			}
		});
		
		fViewer.expandAll();
		fViewer.addSelectionChangedListener(e -> selectionChanged());			
		fViewer.setInput(fModel.getDipProject());
	}
	
	private void selectionChanged() {
		fModel.clear();
		Report report = TreeViewerUtils.getSelectedObject(fViewer, Report.class);
		fModel.setReport(report);

		if (report != null) {
			ReportReader reader = new ReportReader(report.resource());
			reader.read();
			List<ReportEntry> entries = reader.getEntries();
			if (entries != null) {
				fModel.setEntries(entries);
				setPageComplete(true);
				return;
			}
		}
		setPageComplete(false);
	}

	public boolean canFinish() {
		return false;
	}

}
