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

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

import ru.dip.core.model.DipProject;
import ru.dip.core.model.DipTableContainer;
import ru.dip.core.model.interfaces.IParent;
import ru.dip.core.model.reports.IReportContainer;
import ru.dip.core.model.reports.Report;
import ru.dip.core.utilities.DipUtilities;
import ru.dip.core.utilities.ui.CompositeBuilder;
import ru.dip.core.utilities.ui.viewer.TreeViewerUtils;
import ru.dip.ui.Messages;
import ru.dip.ui.navigator.NavigatorContentProvider;
import ru.dip.ui.navigator.NavigatorLabelProvider;
import ru.dip.ui.utilities.ui.EntryTextComposite;

public class SaveReportPage extends WizardPage {

	private final DipProject fDipProject;
	
	private TreeViewer fViewer;
	private EntryTextComposite fFileNameEntry;
	private EntryTextComposite fParentFolder;
	private EntryTextComposite fReportName;
	private EntryTextComposite fRuleName;
	
	protected SaveReportPage(DipProject dipProject) {
		super("SaveReportPage"); //$NON-NLS-1$
		fDipProject = dipProject;
		setTitle(Messages.SaveReportPage_Title);
		setPageComplete(false);
	}

	@Override
	public void createControl(Composite parent) {
		Composite composite = CompositeBuilder.instance(parent).full().build();
		
		fParentFolder = new EntryTextComposite(composite, Messages.SaveReportPage_ReportPathLabel);
		fParentFolder.setEnabled(false);
		
		createTreeViewer(composite);
		
		fFileNameEntry = new EntryTextComposite(composite, Messages.SaveReportPage_FileNameLabel);
		fFileNameEntry.addModifyListener(e -> validate());
		
		fReportName = new EntryTextComposite(composite, Messages.SaveReportPage_ReportNameLabel);
		fReportName.addModifyListener(e -> validate());
		
		fRuleName = new EntryTextComposite(composite, Messages.SaveReportPage_RuleNameLabel);
		fRuleName.addModifyListener(e -> validate());
		
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
		NavigatorContentProvider contentProvider = new NavigatorContentProvider();
		fViewer.setContentProvider(contentProvider);
		ILabelProvider labelProvider = new NavigatorLabelProvider(); 	
		fViewer.setLabelProvider(labelProvider);		
		fViewer.addFilter(new ViewerFilter() {
			
			@Override
			public boolean select(Viewer viewer, Object parentElement, Object element) {
				return element instanceof IReportContainer || element instanceof DipTableContainer || element instanceof Report
						|| element.equals(fDipProject);
			}
		});
		
		fViewer.setSelection(new StructuredSelection(fDipProject));
		fViewer.addSelectionChangedListener(e -> selectionChanged());
		fViewer.setInput(fDipProject);
	}
	
	private void selectionChanged() {
		IContainer container = getSelectedContainer();
		if (container != null) {
			String path = container.getFullPath().toOSString();
			if (!path.endsWith("/")) { //$NON-NLS-1$
				path = path + "/"; //$NON-NLS-1$
			}
			if (fFileNameEntry.getValue() != null) {
				path = path + fFileNameEntry.getValue();
			}
			fParentFolder.setValue(path);
		} else {
			fParentFolder.setValue(""); //$NON-NLS-1$
		}
		validate();
	}
	
	public IContainer getSelectedContainer() {
		IParent parent = getDipParent();
		if (parent != null) {
			return (IContainer) parent.resource();
		}
		return null;
		
	}
	
	public IParent getDipParent() {
		return TreeViewerUtils.getSelectedObject(fViewer, IParent.class);
	}
	
	public String getFileName() {
		String name = fFileNameEntry.getValue();
		if (name != null) {
			if (name.endsWith("." + Report.REPORT_EXTENSION_WITH_DOT)) { //$NON-NLS-1$
				return name;
			} else {
				return name + Report.REPORT_EXTENSION_WITH_DOT;
			}			
		} else {
			return ""; //$NON-NLS-1$
		}
	}
	
	private void validate() {
		String validMessage = getValidMessage();
		setErrorMessage(validMessage);
		setPageComplete(validMessage == null);
	}
	
	private String getValidMessage() {
		IContainer container = getSelectedContainer();
		String fileName = getFileName();
		
		IStatus status = DipUtilities.canCreateFile(container, fileName);
		if (!status.isOK()) {
			return status.getMessage();
		}
		if (fReportName.getValue() == null || fReportName.getValue().trim().isEmpty()) {
			return Messages.SaveReportPage_NoneReportNameErrorMessage;
		}
		if (fRuleName.getValue() == null || fRuleName.getValue().trim().isEmpty()) {
			return Messages.SaveReportPage_NoneRuleNameErrorMessage;
		}
		return null;
	}
	
	public String getReportName() {
		return fReportName.getValue();
	}
	
	public String getRuleName() {
		return fRuleName.getValue();
	}

}
