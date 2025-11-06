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
package ru.dip.ui.wizard.newfile;

import java.io.File;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import ru.dip.core.model.Appendix;
import ru.dip.core.model.DipFolder;
import ru.dip.core.model.DipProject;
import ru.dip.core.model.interfaces.IDipElement;
import ru.dip.core.model.interfaces.IDipParent;
import ru.dip.core.unit.ReportRefPresentation;
import ru.dip.core.utilities.DipUtilities;
import ru.dip.core.utilities.status.StatusUtils;
import ru.dip.ui.Messages;
import ru.dip.ui.navigator.NavigatorContentProvider;
import ru.dip.ui.navigator.NavigatorLabelProvider;
import ru.dip.ui.utilities.ui.EntryTextComposite;

public class NewFilePage extends AbstractNewFilePage  {

	// controls
	private EntryTextComposite fParentPath;
	private TreeViewer fViewer;
	private Composite fParent;
	private IDipParent fSelectionContainer;
	
	protected NewFilePage(IDipParent selectedContainer) {
		fSelectionContainer = selectedContainer;
		fDipProject = fSelectionContainer.dipProject();
		fParentContainer = fSelectionContainer.resource();
	}
	
	//=========================
	// content

	@Override
	public void createControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout());
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		createParentPathField(composite);
		createTreeViewer(composite);
		createNameComposite(composite);		
		createIDComposite(composite);
		new Label(composite, SWT.NONE);
		createPositionComposite(composite);
		new Label(composite, SWT.NONE);
		setParentValue();		
		setControl(composite);
		fNewFileName.setFocus();
		boolean valid = validate();
		setPageComplete(valid);
	}
	
	private void createParentPathField(Composite parent){
		fParentPath = new EntryTextComposite(parent, Messages.NewFilePage_ParentFolderLabel);
		fParentPath.addModifyListener(new ModifyListener() {			
			@Override
			public void modifyText(ModifyEvent e) {
				boolean valid = validate();
				setPageComplete(valid);				
			}
		});
	}
	
	private void createTreeViewer(Composite parent){
		fParent = parent;
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
				//WorkbenchLabelProvider.getDecoratingWorkbenchLabelProvider();
		fViewer.setLabelProvider(labelProvider);	
		addViewerFilter();
		addViewerSelectionListener();
	}
	
	private void addViewerFilter(){
		fViewer.addFilter(new ViewerFilter() {
			
			@Override
			public boolean select(Viewer viewer, Object parentElement, Object element) {
				return element instanceof DipFolder || element instanceof DipProject;
			}
		});
	}
	
	private void addViewerSelectionListener(){
		fViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				IStructuredSelection selection = fViewer.getStructuredSelection();
				if (selection != null && !selection.isEmpty()){
					Object obj = selection.getFirstElement();
					if (obj instanceof IDipParent){
						fSelectionContainer = (IDipParent) obj;
						setParentPathValue(fSelectionContainer);
					}
				}
				fIdLabel.setText(getID());
				fParent.layout();				
				boolean valid = validate();
				setPageComplete(valid);
			}
		});
	}
	
	@Override
	protected void setPositionButtonValues() {
		fStart.setText(Messages.NewFilePage_Start_directory);
		fEnd.setText(Messages.NewFilePage_End_directory);
		fEnd.setSelection(true);					
	}
	
	//=========================
	// set values
	
	private void setParentValue(){
		setParentPathValue(fSelectionContainer);
		fViewer.setInput(ResourcesPlugin.getWorkspace().getRoot());
		fViewer.setSelection(new StructuredSelection(fSelectionContainer));
	}
	
	private void setParentPathValue(IDipParent parent){
		IContainer container = (IContainer) parent.resource();
		fParentPath.setValue(container.getProject().getName() + File.separator + container.getProjectRelativePath().toOSString());
	}
	
	//==========================
	// validate
	
	@Override
	public boolean canFlipToNextPage() {
		return ReportRefPresentation.EXTENSION_WITHOUT_DOT.equals(getExtension());
	}
	
	@Override
	protected IStatus getValidateStatus(){
		String fileName = fNewFileName.getText() + fExtensionCombo.getText();
		IStatus status = DipUtilities.checkReqName(fileName);
		if (!status.isOK()){
			return status;
		}
		IContainer parentContainer = getParentContainer(); 
		if (parentContainer == null){			
			return StatusUtils.INVALID_PARENT_FODLER;
		}
		if (fSelectionContainer instanceof Appendix) {
			return StatusUtils.CAN_NOT_CREATE_FILE_IN_APPENDIX;
		}
		fNewFile = parentContainer.getFile(new Path(fileName));
		return DipUtilities.canCreateFile(parentContainer, fileName);
	}
	
	private IContainer getParentContainer(){
		String parentPath = fParentPath.getValue();
		IDipElement element = DipUtilities.findElement(parentPath);
		if (element instanceof IDipParent){
			fSelectionContainer = (IDipParent) element;
			
			return (IContainer) element.resource();
		}
		return null;
	}
	
	public IDipParent getParent(){
		return fSelectionContainer;
	}
}
