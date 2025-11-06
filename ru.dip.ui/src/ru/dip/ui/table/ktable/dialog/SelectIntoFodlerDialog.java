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
package ru.dip.ui.table.ktable.dialog;

import java.util.TreeSet;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.model.WorkbenchLabelProvider;

import ru.dip.core.model.interfaces.IDipParent;
import ru.dip.core.model.interfaces.IDipDocumentElement;
import ru.dip.core.utilities.DipUtilities;

public class SelectIntoFodlerDialog extends Dialog {

	private static final String SELECT_FOLDER_MES = " Веберете директорию: \n\n";
	
	// controls
	private TreeViewer fViewer;
	private Label fErrorLabel;
	//  model
	private IDipParent fIntoFolder;
	private TreeSet<IDipDocumentElement> fMovedElements;
	private IDipParent fDipParent;
	
	public SelectIntoFodlerDialog(Shell parentShell, IDipParent fTableParent, TreeSet<IDipDocumentElement> movedElements) {
		super(parentShell);
		fMovedElements = movedElements;
		fDipParent = fTableParent;
	}
	
	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Into folder");
	}
	
	@Override
	protected Point getInitialSize() {
		return new Point(400, 600);
	}
	
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout());
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		createTreeViewer(composite);
		fErrorLabel = new Label(composite, SWT.NONE);
		fErrorLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		fErrorLabel.setText(SELECT_FOLDER_MES);		
		fViewer.setInput("");
		fViewer.expandToLevel(2);	
		return composite;
	}
	
	@Override
	protected Control createContents(Composite parent) {		
		Control control =  super.createContents(parent);
		getButton(OK).setEnabled(false);
		return control;
	}
	
	private void createTreeViewer(Composite parent){
		Composite treeComposite = new Composite(parent, SWT.NONE);
		treeComposite.setLayout(new GridLayout());
		treeComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
		fViewer = new TreeViewer(treeComposite);
		fViewer.getTree().setLayoutData(new GridData(GridData.FILL_BOTH));
		DipFoldersContentProvider contentProvider = new DipFoldersContentProvider();
		fViewer.setContentProvider(contentProvider);
		ILabelProvider labelProvider = 		WorkbenchLabelProvider.getDecoratingWorkbenchLabelProvider();
		fViewer.setLabelProvider(labelProvider);
		addViewerFilter();
		fViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				validate();
			}
		});
	}
	
	private class DipFoldersContentProvider implements ITreeContentProvider {

		@Override
		public Object[] getElements(Object inputElement) {	
			return new Object[]{fDipParent};
		}

		@Override
		public Object[] getChildren(Object parentElement) {
				
			if (parentElement instanceof IDipParent){
				return ((IDipParent) parentElement).getDipChildren();
			}			
			return null;
		}

		@Override
		public Object getParent(Object element) {
			if (element.equals(fDipParent)){
				return null;
			}
			
			
			if (element instanceof IDipDocumentElement){
				return ((IDipDocumentElement) element).parent();
			}		
			return null;
		}

		@Override
		public boolean hasChildren(Object element) {
			if (element instanceof IDipParent){
				IDipParent parent = (IDipParent) element;
				for (IDipDocumentElement child: parent.getDipDocChildrenList()){
					if (child instanceof IDipParent){
						return true;
					}				
				}		
				return false;
			}			
			return false;
		}		
	}

	private void addViewerFilter(){
		fViewer.addFilter(new ViewerFilter() {
			
			@Override
			public boolean select(Viewer viewer, Object parentElement, Object element) {
				return element instanceof IDipParent;
			}
		});
	}
	
	private void validate(){
		IStructuredSelection selection = fViewer.getStructuredSelection();
		if (selection == null){
			fErrorLabel.setText(SELECT_FOLDER_MES);
			getButton(OK).setEnabled(false);
			return;
		}
		IDipParent selectParent = (IDipParent) selection.getFirstElement();
		IStatus status = DipUtilities.canPaste(selectParent, fMovedElements);
		if (!status.isOK()) {
			fErrorLabel.setText(status.getMessage());
			getButton(OK).setEnabled(false);
		} else {
			fErrorLabel.setText("");
			getButton(OK).setEnabled(true);
		}	
	}
	
	@Override
	protected void okPressed() {
		IStructuredSelection selection = fViewer.getStructuredSelection();
		if (selection != null && selection.getFirstElement() instanceof IDipParent){
			fIntoFolder = (IDipParent) selection.getFirstElement();
		}		
		super.okPressed();
	}
	
	public IDipParent getIntoFolder(){
		return fIntoFolder;
	}
}