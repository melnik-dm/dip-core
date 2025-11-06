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

import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

import ru.dip.core.model.DipProject;
import ru.dip.core.model.interfaces.IDipDocumentElement;
import ru.dip.core.model.interfaces.IDipParent;
import ru.dip.core.model.reports.IReportContainer;
import ru.dip.core.model.reports.Report;
import ru.dip.core.utilities.ui.CompositeBuilder;
import ru.dip.core.utilities.ui.viewer.DipProjectFoldersContentProvider.ParentHolder;
import ru.dip.ui.Messages;
import ru.dip.ui.utilities.image.ImageProvider;

public class NewReportRefPage extends WizardPage {

	private final DipProject fProject;

	private TreeViewer fViewer;
	private Button fRelativePath;

	public NewReportRefPage(DipProject project) {
		super("Report_ref");
		fProject = project;
		setTitle(Messages.NewReportRefPage_Title);
		setDescription(Messages.NewReportRefPage_Description);
	}

	@Override
	public void createControl(Composite parent) {
		Composite composite = CompositeBuilder.instance(parent).full().build();
		fViewer = new TreeViewer(composite);
		fViewer.getTree().setLayoutData(new GridData(GridData.FILL_BOTH));
		fViewer.setContentProvider(new DipProjectReportsContentProvider());
		fViewer.setLabelProvider(new LabelProvider() {

			@Override
			public String getText(Object element) {
				if (element instanceof IReportContainer) {
					return ((IReportContainer) element).getRelativePath();
				}
				if (element instanceof Report) {
					return ((Report) element).name();
				}
				return super.getText(element);
			}

			@Override
			public Image getImage(Object element) {
				if (element instanceof IReportContainer) {
					return ImageProvider.REPORT_FOLDER;
				}
				if (element instanceof Report) {
					return ImageProvider.FILE;
				}
				return super.getImage(element);
			}
		});
		fViewer.setInput(new ParentHolder(fProject));
		fViewer.addSelectionChangedListener(new ISelectionChangedListener() {

			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				setPageComplete(isPageComplete());
			}
		});
		fRelativePath = new Button(composite, SWT.CHECK);
		fRelativePath.setText(Messages.NewReportRefPage_RelativePathButton);
		fRelativePath.setSelection(fProject.getProjectProperties().isReportRefRelativePath());
		fRelativePath.addSelectionListener(new SelectionAdapter() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				fProject.getProjectProperties().setReportRefRelativePath(fRelativePath.getSelection());
			}
		});
		setControl(composite);
	}
	
	class DipProjectReportsContentProvider implements ITreeContentProvider {

		@Override
		public Object[] getElements(Object inputElement) {
			if (inputElement instanceof DipProject) {
				DipProject project = (DipProject) inputElement;
				return project.getReportFolder().getChildren().toArray();
			} else if (inputElement instanceof ParentHolder) {
				IDipParent parent = ((ParentHolder) inputElement).getParent();
				if (parent instanceof DipProject) {
					DipProject project = (DipProject) parent;
					if (project.getReportFolder() != null) {
						return project.getReportFolder().getChildren().toArray();
					}
				}
			}
			return new Object[0];
		}

		@Override
		public Object[] getChildren(Object parentElement) {
			if (parentElement instanceof IReportContainer) {
				System.out.println("get children: " + parentElement + "  " + ((IReportContainer) parentElement).getReports());
				
				return ((IReportContainer) parentElement).getReports().toArray();
			}
			return null;
		}

		@Override
		public Object getParent(Object element) {
			if (element instanceof IDipDocumentElement) {
				return ((IDipDocumentElement) element).parent();
			}
			return null;
		}

		@Override
		public boolean hasChildren(Object element) {
			if (element instanceof IReportContainer) {
				return !((IReportContainer) element).getReports().isEmpty();
			}
			return false;
		}
	}

	@Override
	public boolean isPageComplete() {
		ITreeSelection selection = fViewer.getStructuredSelection();
		if (selection == null || selection.isEmpty()) {
			return false;
		}
		return selection.getFirstElement() instanceof Report;
	}

	public Report getReport() {
		return (Report) fViewer.getStructuredSelection().getFirstElement();
	}

	public boolean getRelativeReportRef() {
		return fRelativePath.getSelection();
	}

}
