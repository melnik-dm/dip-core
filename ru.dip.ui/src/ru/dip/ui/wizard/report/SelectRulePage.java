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

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

import ru.dip.core.report.checker.ReportEntryChecker;
import ru.dip.core.report.model.report.ReportEntry;
import ru.dip.core.report.model.report.ReportRule;
import ru.dip.core.utilities.ui.CompositeBuilder;
import ru.dip.core.utilities.ui.viewer.ISimpleContentProvider;
import ru.dip.core.utilities.ui.viewer.TreeViewerUtils;
import ru.dip.ui.Messages;
import ru.dip.ui.utilities.image.ImageProvider;
import ru.dip.ui.wizard.report.LoadReportModel.ILoadReportModelListener;

public class SelectRulePage extends WizardPage implements ILoadReportModelListener {

	private TreeViewer fViewer;
	private final LoadReportModel fModel;
	
	protected SelectRulePage(LoadReportModel model) {
		super("SelectRulePage"); //$NON-NLS-1$
		setTitle(Messages.SelectRulePage_Title);
		fModel = model;
		fModel.addListener(this);
		setPageComplete(false);
	}

	@Override
	public void createControl(Composite parent) {
		Composite composite = CompositeBuilder.instance(parent).full().build();	
		createTreeViewer(composite);	
		setControl(composite);	
	}

	private void createTreeViewer(Composite parent){
		Composite treeComposite = new Composite(parent, SWT.NONE);
		treeComposite.setLayout(new GridLayout());
		GridData gd = new GridData(GridData.FILL_BOTH);
		gd.minimumHeight = 90;
		treeComposite.setLayoutData(gd);
		fViewer = new TreeViewer(treeComposite);
		fViewer.getTree().setLayoutData(new GridData(GridData.FILL_BOTH));	
		setContentProvider();
		setLabelProvider();
		fViewer.expandAll();	
		fViewer.addSelectionChangedListener(e -> selectionChanged());	
		fViewer.setInput(""); //$NON-NLS-1$
	}

	private void setContentProvider() {
		fViewer.setContentProvider(new ISimpleContentProvider() {
			
			@Override
			public Object[] getElements(Object inputElement) {
				if (fModel.getEntries() == null) {
					return new Object[0];
				}			
				return fModel.getEntries().toArray();
			}
		});
	}
	
	private void setLabelProvider() {
		fViewer.setLabelProvider(new LabelProvider() {
			
			@Override
			public String getText(Object element) {
				if (element instanceof ReportEntry) {
					return ((ReportEntry) element).getName();
				}				
				return super.getText(element);
			}
			
			@Override
			public Image getImage(Object element) {
				return ImageProvider.FILE;
			}
			
		});	
	}
	
	private void selectionChanged() {
		ReportEntry reportEntry = TreeViewerUtils.getSelectedObject(fViewer, ReportEntry.class);
		fModel.setReportEntry(reportEntry);
		if (reportEntry != null) {
			for (ReportRule rule : reportEntry.getRules()) {
				if (rule.getExtension() != null) {		
					ReportEntryChecker.ruleToFilter(rule);
				}				
			}
		}
		setPageComplete(reportEntry != null);	
	}
	
	@Override
	public void modelChaned() {
		fViewer.refresh();
	}
	
}
