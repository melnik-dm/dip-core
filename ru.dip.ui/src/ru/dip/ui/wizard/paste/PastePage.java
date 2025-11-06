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
package ru.dip.ui.wizard.paste;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.layout.TreeColumnLayout;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;

import ru.dip.core.utilities.DipUtilities;
import ru.dip.core.utilities.ui.CompositeBuilder;
import ru.dip.core.utilities.ui.CompositeFactory;
import ru.dip.core.utilities.ui.ControlFactory;
import ru.dip.core.utilities.ui.swt.ColorProvider;
import ru.dip.core.utilities.ui.swt.FontManager;
import ru.dip.core.utilities.ui.viewer.ISimpleContentProvider;
import ru.dip.ui.Messages;
import ru.dip.ui.utilities.image.ImageProvider;
import ru.dip.ui.utilities.ui.BrowseProjectFolderComposite;
import ru.dip.ui.utilities.ui.EntryTextComposite;
import ru.dip.ui.utilities.ui.IBrowseCompositeListener;

public class PastePage extends WizardPage {

	private static final String DESCRIPTION = Messages.PastePage_SelectObjects;
	private static final String IDENTIFIER = Messages.PastePage_Identifier;
	private static final String INPUT_NEW_NAME = Messages.PastePage_InputNewName;
	
	private PasteWizard fWizard;
	private CheckboxTreeViewer fViewer;

	protected PastePage(PasteWizard wizard) {
		super("Paste Resources"); //$NON-NLS-1$
		fWizard = wizard;
		setTitle("Paste Resources"); //$NON-NLS-1$
		setDescription(DESCRIPTION);
	}

	@Override
	public void createControl(Composite parent) {
		Composite composite = CompositeBuilder.instance(parent).full().build();
		
		BrowseProjectFolderComposite folderComposite = new BrowseProjectFolderComposite(composite, 
				"Target Directory", fWizard.getTargetParent()); //$NON-NLS-1$
		folderComposite.addBrowseListener(new IBrowseCompositeListener() {
			
			@Override
			public void browseChanged() {
				fWizard.setTargetParent(folderComposite.getDipParent());
				fViewer.refresh();
				setPageComplete(isPageComplete());
			}
		});
		
		folderComposite.setValue(DipUtilities.relativeProjectID(fWizard.getTargetParent()));
		createTree(composite);
		setControl(composite);
	}

	private void createTree(Composite parent) {

		Composite composite = CompositeFactory.full(parent);

		TreeColumnLayout fTreeColumnlayout = new TreeColumnLayout(true);
		composite.setLayout(fTreeColumnlayout);
		GridData gridData = new GridData(GridData.FILL_BOTH);
		gridData.grabExcessHorizontalSpace = true;
		composite.setLayoutData(gridData);

		Tree tree = new Tree(composite, SWT.BORDER | SWT.NO_SCROLL | SWT.V_SCROLL | SWT.CHECK);
		fViewer = new CheckboxTreeViewer(tree);
		tree.setHeaderVisible(true);
		tree.setLinesVisible(true);

		GridData treeLayoutData = new GridData(GridData.VERTICAL_ALIGN_BEGINNING | GridData.FILL_HORIZONTAL);
		treeLayoutData.grabExcessHorizontalSpace = true;
		tree.setLayoutData(treeLayoutData);

		TreeViewerColumn viewerColumn = new TreeViewerColumn(fViewer, SWT.CENTER);
		TreeColumn fColumn1 = viewerColumn.getColumn();
		fColumn1.setText("FileName"); //$NON-NLS-1$
		fColumn1.setAlignment(SWT.LEFT);
		fColumn1.setResizable(true);
		fTreeColumnlayout.setColumnData(fColumn1, new ColumnWeightData(20, 0, true));

		TreeViewerColumn viewerColumn2 = new TreeViewerColumn(fViewer, SWT.CENTER);
		TreeColumn fColumn2 = viewerColumn2.getColumn();
		fColumn2.setText("New Identificator"); //$NON-NLS-1$
		fColumn2.setAlignment(SWT.LEFT);
		fColumn2.setResizable(true);
		fTreeColumnlayout.setColumnData(fColumn2, new ColumnWeightData(40, 0, true));
		
		TreeViewerColumn viewerColumn3 = new TreeViewerColumn(fViewer, SWT.CENTER);
		TreeColumn fColumn3 = viewerColumn3.getColumn();
		fColumn3.setText("New Name"); //$NON-NLS-1$
		fColumn3.setAlignment(SWT.LEFT);
		fColumn3.setResizable(true);
		fTreeColumnlayout.setColumnData(fColumn3, new ColumnWeightData(20, 0, true));
		
		TreeViewerColumn viewerColumn4 = new TreeViewerColumn(fViewer, SWT.CENTER);
		TreeColumn fColumn4 = viewerColumn4.getColumn();
		fColumn4.setText("Status"); //$NON-NLS-1$
		fColumn4.setAlignment(SWT.LEFT);
		fColumn4.setResizable(true);
		fTreeColumnlayout.setColumnData(fColumn4, new ColumnWeightData(20, 0, true));

		fViewer.setContentProvider(new ISimpleContentProvider() {

			@Override
			public Object[] getElements(Object inputElement) {
				return fWizard.getPasteElements();
			}

		});
		
		fViewer.setLabelProvider(new CellLabelProvider() {
			
			@Override
			public void update(ViewerCell cell) {
				
				int index = cell.getColumnIndex();
				PasteElement element = (PasteElement) cell.getElement();
				
				if (index == 0) {
					cell.setText(element.getOldName());					
					if (element.isFolder()) {
						cell.setImage(ImageProvider.FOLDER);
					} else {
						cell.setImage(ImageProvider.FILE);
					}
					
				} else if (index == 1) {
					cell.setText(element.getId(fWizard.getTargetParent()));
				} else if (index == 2) {
					cell.setText(element.getNewName());
				} else if (index == 3) {
					cell.setText(element.getStatusMessage());
					if (element.isStatusOk()) {
						cell.setForeground(ColorProvider.GREEN_DARK);
					} else {
						cell.setForeground(ColorProvider.RED);
					}
				}
			}
		});
		
		
		fViewer.addDoubleClickListener(new IDoubleClickListener() {
			
			@Override
			public void doubleClick(DoubleClickEvent event) {
				PasteElement element = (PasteElement) fViewer.getStructuredSelection().getFirstElement();
				SetNewNameDialog dialog = new SetNewNameDialog(element);
				dialog.open();
			}
		});
		
		fViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				setSelectDescription();
				
			}
		});
		
		fViewer.setInput(fWizard);
		fViewer.setCheckedElements(fWizard.getPasteElements());
		fViewer.addCheckStateListener(new ICheckStateListener() {
			
			@Override
			public void checkStateChanged(CheckStateChangedEvent event) {
				PasteElement element = (PasteElement) event.getElement();
				element.setEnable(event.getChecked());				
				setPageComplete(isPageComplete());
			}
		});
		
		setPageComplete(isPageComplete());
	}
	
	private void setSelectDescription() {
		PasteElement element = (PasteElement) fViewer.getStructuredSelection().getFirstElement();
		if (element.isStatusOk()) {
			setErrorMessage(null);
		} else {
			setErrorMessage(element.getOriginalStatusMessage());
		}
	}
		
	@Override
	public boolean isPageComplete() {
		Object[] elements = fViewer.getCheckedElements();
		if (elements.length == 0) {
			return false;
		}
		for (Object element: elements) {
			PasteElement pasteElement = (PasteElement) element;
			if (!pasteElement.isStatusOk()) {
				return false;
			}
		}		
		return true;
	}
	
	
	private class SetNewNameDialog extends Dialog{
		
		private PasteElement fElement;
		
		private Label fErrorLabel;
		private Label fNewId;
		private EntryTextComposite fTextComposite;
		
		public SetNewNameDialog(PasteElement element) {
			super(PastePage.this.getShell());
			fElement = element;
		}
		
		@Override
		protected Point getInitialSize() {
			Point p =  super.getInitialSize();
			if (p.x < 600) {
				p.x = 600;
			}
			return p;
		}
		
		@Override
		protected void configureShell(Shell newShell) {
			super.configureShell(newShell);
			newShell.setText("Name Conflict"); //$NON-NLS-1$
		}
		
		@Override
		protected Control createDialogArea(Composite parent) {
			Composite composite = CompositeBuilder.instance(parent).full().build();
			ControlFactory.emptyLabel(composite);
			Composite idComposite = CompositeBuilder.instance(composite).columns(2, false).build();
			ControlFactory.label(idComposite, IDENTIFIER);
			fNewId = new Label(idComposite, SWT.NONE);
			fNewId.setText(fElement.getId(fWizard.getTargetParent()));
			fNewId.setFont(FontManager.boldFont);
			ControlFactory.emptyLabel(composite);
			fTextComposite = new EntryTextComposite(composite, INPUT_NEW_NAME);
			fTextComposite.setValue(fElement.getNewName());
			
			ControlFactory.emptyLabel(composite);
			fErrorLabel = new Label(composite, SWT.NONE);
			if (!fElement.isStatusOk()) {
				fErrorLabel.setText(fElement.getOriginalStatusMessage());
			} else {
				fErrorLabel.setText("\n"); //$NON-NLS-1$
			}
			
			fTextComposite.addModifyListener(new ModifyListener() {
				
				@Override
				public void modifyText(ModifyEvent e) {
					IStatus status = fElement.checkStatus(fWizard.getAllNames(), fTextComposite.getValue());
					setErrorLabel(status);
					fNewId.setText(fElement.getId(fWizard.getTargetParent(), fTextComposite.getValue()));
					composite.layout();
				}
			});
			return composite;
		}
		
		private void setErrorLabel(IStatus status) {
			if (!status.isOK()) {
				fErrorLabel.setText(status.getMessage());
			} else {
				fErrorLabel.setText("\n"); //$NON-NLS-1$
			}
		}
		
		@Override
		protected void okPressed() {
			if (!fTextComposite.getValue().isEmpty() && !fTextComposite.getValue().equals(fElement.getNewName())) {
				fElement.setNewName(fTextComposite.getValue());
				fElement.checkStatus(fWizard.getAllNames());
				setPageComplete(isPageComplete());
				fViewer.refresh();
				setSelectDescription();
			}
			super.okPressed();
		}	
	}
	
}
