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
package ru.dip.ui.variable.view;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.jface.layout.TreeColumnLayout;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;

import ru.dip.core.model.vars.IVarContainer;
import ru.dip.core.model.vars.ProjectVarContainer;
import ru.dip.core.model.vars.Variable;
import ru.dip.core.utilities.WorkbenchUtitlities;
import ru.dip.core.utilities.ui.CompositeBuilder;
import ru.dip.core.utilities.ui.WSelectionListener;
import ru.dip.core.utilities.ui.viewer.ListContentProvider;
import ru.dip.ui.Messages;
import ru.dip.ui.utilities.image.ImageProvider;
import ru.dip.ui.variable.VarManager;
import ru.dip.ui.variable.dialog.UndefineVariablesDialog;
import ru.dip.ui.variable.dialog.VarMessagesDialogs;

public class VariablesComposite {
	
	private static final String FIND_UNDEF_VARS = Messages.VariablesComposite_FindUndefinedVarsActionName;
	private static final String SHOW_DUPLICATES = Messages.VariablesComposite_ShowDuplicatesActionName;
	
	// model
	private IVarContainer fVarContainer;
	private boolean fShowDuplicatesMode = false;
	// controls
	private final Shell fShell;
	private TreeViewer fViewer;
	private TreeColumn fNameColumn;
	private TreeColumn fValueColumn;	
	private ToolItem fAddButton;
	private ToolItem fEditButton;
	private ToolItem fDeleteButton;
	private ToolItem fShowDuplicatesButton;
	private ToolItem fFindVarsInProject;
	private Text fSearchText; 
	
	private ITreeContentProvider fContentProvider = new VariablesContentProvider();
	
	
	public VariablesComposite(Composite parent) {
		createContent(parent);
		fShell = parent.getShell();
	}
	
	private void createContent(Composite parent) {
		createToolBarComposite(parent);
		createTreeViewer(parent);		
	}
	
	//============================
	// toolbar
	
	private void createToolBarComposite(Composite parent) {		
		Composite tbComposite = CompositeBuilder.instance(parent).horizontal().columns(3, false).build();			

		createSearchText(tbComposite);
		Label emptyLabel = new Label(tbComposite, SWT.NONE);
		emptyLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		ToolBar toolbar = new ToolBar(tbComposite, SWT.NONE);
		createAddItem(toolbar);
		createEditItem(toolbar);
		createDeleteItem(toolbar);
		createShowDuplicatesItem(toolbar);
		createFindVarsItem(toolbar);
	}
	
	private void createSearchText(Composite tbComposite) {
		fSearchText = new Text(tbComposite, SWT.SEARCH | SWT.ICON_CANCEL);
		GridData stGD = new GridData();
		stGD.widthHint = 160;
		fSearchText.setLayoutData(stGD);
		fSearchText.addModifyListener(new ModifyListener() {
			
			@Override
			public void modifyText(ModifyEvent e) {
				fViewer.refresh();
			}
		});
	}
	
 	private void createAddItem(ToolBar toolbar) {
		fAddButton = new ToolItem(toolbar, SWT.NONE);
		fAddButton.setImage(ImageProvider.ADD);
		fAddButton.setToolTipText(Messages.VariablesComposite_NewButton);
		fAddButton.addSelectionListener(new WSelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				doNewVariable();				
			}
		});
	}
	
	private void createEditItem(ToolBar toolbar) {
		fEditButton = new ToolItem(toolbar, SWT.NONE);
		fEditButton.setImage(ImageProvider.EDIT);
		fEditButton.setToolTipText(Messages.VariablesComposite_EditButton);
		fEditButton.setEnabled(false);
		fEditButton.addSelectionListener(new WSelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				doEditVariable();				
			}
		
		});		
	}
	
	private void createDeleteItem(ToolBar toolbar) {
		fDeleteButton = new ToolItem(toolbar, SWT.NONE);
		fDeleteButton.setImage(ImageProvider.DELETE);
		fDeleteButton.setToolTipText(Messages.VariablesComposite_DeleteButton);
		fDeleteButton.setEnabled(false);
		fDeleteButton.addSelectionListener(new WSelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				doDeleteField();				
			}
		});
	}

	private void createShowDuplicatesItem(ToolBar toolbar) {
		fShowDuplicatesButton = new ToolItem(toolbar, SWT.CHECK);
		fShowDuplicatesButton.setImage(ImageProvider.DUPLICATE);
		fShowDuplicatesButton.setToolTipText(SHOW_DUPLICATES);
		fShowDuplicatesButton.addSelectionListener(new WSelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				doShowDuplicates(fShowDuplicatesButton.getSelection());				
			}
		});
	}
	
	private void createFindVarsItem(ToolBar toolbar) {
		fFindVarsInProject = new ToolItem(toolbar, SWT.NONE);
		fFindVarsInProject.setImage(ImageProvider.ABBREVIATION);
		fFindVarsInProject.setToolTipText(FIND_UNDEF_VARS);
		fFindVarsInProject.addSelectionListener(new WSelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				doFindVarsInProject();				
			}
		});
	}
	
	//==========================
	// Viewer
	
	private void createTreeViewer(Composite parent){
		Composite composite = new Composite(parent, SWT.BORDER);
		TreeColumnLayout treeColumnlayout = new TreeColumnLayout(true);
		composite.setLayout(treeColumnlayout);
		GridData gridData = new GridData(GridData.FILL_BOTH);
		gridData.grabExcessHorizontalSpace = true;
		composite.setLayoutData(gridData);
		
		createTree(composite);		
		createNameColumn(treeColumnlayout);
		createValueColumn(treeColumnlayout);
								
		fViewer.setContentProvider(fContentProvider);	
		fViewer.setLabelProvider(new VariablesLabelProvider(this));

		addSelectionListener();
		addDoubleClickListener();
		addFilter();
	}
	
	private void createTree(Composite parent) {
		Tree tree= new Tree(parent, SWT.BORDER | SWT.V_SCROLL);
		fViewer = new TreeViewer(tree);		
		tree.setHeaderVisible(true);
		
		GridData treeLayoutData = new GridData(GridData.VERTICAL_ALIGN_BEGINNING | GridData.FILL_HORIZONTAL);
		treeLayoutData.grabExcessHorizontalSpace = true;
		tree.setLayoutData(treeLayoutData);
	}
	
	private void createNameColumn(TreeColumnLayout treeColumnlayout) {
        TreeViewerColumn viewerColumn = new TreeViewerColumn(fViewer, SWT.NONE);		
		fNameColumn = viewerColumn.getColumn();
		fNameColumn.setText(Messages.VariablesComposite_VarNameColumn);
		fNameColumn.setAlignment(SWT.LEFT);	
		fNameColumn.setResizable(true);
		fNameColumn.addControlListener(new ControlAdapter() {

			@Override
			public void controlResized(ControlEvent e) {
				if (fVarContainer != null) {
					fViewer.refresh();
				}
			}
		});		
		treeColumnlayout.setColumnData(fNameColumn, new ColumnWeightData(20, 0, true));
	}
	
	private void createValueColumn(TreeColumnLayout treeColumnlayout) {
        TreeViewerColumn viewerColumn2 = new TreeViewerColumn(fViewer, SWT.NONE);		
        fValueColumn = viewerColumn2.getColumn();
        fValueColumn.setText(Messages.VariablesComposite_VarValueColumn);
        fValueColumn.setAlignment(SWT.LEFT);	
        fValueColumn.setResizable(true);	
        fValueColumn.addControlListener(new ControlAdapter() {	
			
			@Override
			public void controlResized(ControlEvent e) {
				if (fVarContainer != null) {
					fViewer.refresh();
				}
			}
		});
        
		treeColumnlayout.setColumnData(fValueColumn, new ColumnWeightData(60, 0, true));
	}
	
	private void addSelectionListener() {
		fViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			
			@Override
			public void selectionChanged(SelectionChangedEvent event) {				
				if (fVarContainer.isReadOnly()) {
					return;
				}				
				Variable variable = getSelectedVar();
				if (variable != null && !variable.isReadOnly()){					
					fEditButton.setEnabled(true);
					fDeleteButton.setEnabled(true);
				} else {
					fEditButton.setEnabled(false);
					fDeleteButton.setEnabled(false);
				}
			}
		});
	}
	
	private Variable getSelectedVar(){
		IStructuredSelection selection = fViewer.getStructuredSelection();
		if (selection != null){
			Object obj = selection.getFirstElement();
			if (obj instanceof Variable){
				return (Variable) obj;
			}
		}		
		return null;
	}
	
	private void addDoubleClickListener() {
		fViewer.addDoubleClickListener(new IDoubleClickListener() {
			
			@Override
			public void doubleClick(DoubleClickEvent event) {
				doEditVariable();				
			}
		});
	}
	
	private void addFilter() {
		fViewer.addFilter(new ViewerFilter() {
			
			@Override
			public boolean select(Viewer viewer, Object parentElement, Object element) {			
				if (fSearchText == null) {
					return true;
				}
				String searchText = fSearchText.getText().trim().toLowerCase();
				if (searchText.isEmpty()) {
					return true;
				}

				if (element instanceof Variable) {
					return checkVariable((Variable) element, searchText);
				} else if (element instanceof IVarContainer) {
					IVarContainer container = (IVarContainer) element;
					return container.getVariables().stream().anyMatch(v -> checkVariable(v, searchText));
					
				}
				return true;
			}
			
			private boolean checkVariable(Variable variable, String searchText) {
				if (variable.name().toLowerCase().contains(searchText)) {
					return true;
				}
				if (variable.getValue().contains(searchText)) {
					return true;
				}
				return false;
			}
			
		});
	}
	
	//====================
	// action
	
	private void doNewVariable(){
		VarManager.openNewVariableDialog(fShell, fVarContainer);
		fViewer.refresh();
	}
	
	private void doEditVariable(){
		Variable field = getSelectedVar();
		if (field != null){
			VarManager.openEditVariableDialog(fShell, field);
			fViewer.refresh();
		}
	}
	
	private void doDeleteField(){
		Variable field = getSelectedVar();
		if (field != null){
			if (VarMessagesDialogs.confirmDeleteVariable(field, fShell)){
				try {
					fVarContainer.deleteField(field);										
					fViewer.refresh();
					WorkbenchUtitlities.updateProjectExplorer();
				} catch (IOException e) {
					VarMessagesDialogs.showSaveContainerError(fVarContainer, fShell);
					e.printStackTrace();
				}
			}
		}
	}
	
	private void doShowDuplicates(boolean check){;
		if (check) {
			fShowDuplicatesMode = true;
			showDuplicates();
		} else {
			fShowDuplicatesMode = false;
			fViewer.setContentProvider(fContentProvider);
			fViewer.setInput(fVarContainer);
		}
	}
	
	private void showDuplicates() {		
		Map<String, List<Variable>> variablesByName = ((ProjectVarContainer) fVarContainer).getAllVariables()
			.stream().collect(Collectors.groupingBy(Variable::name));
			
		List<Object> resultInput = new ArrayList<>();
		variablesByName.values().stream()
			.filter(list -> list.size() > 1)
			.forEach(list ->  {
				resultInput.addAll(list);
				resultInput.add(new Object());
			});

		fViewer.setContentProvider(new ListContentProvider(resultInput));
		fViewer.setInput(resultInput);
	}
	
	private void doFindVarsInProject() {
		Set<String> vars = fVarContainer.dipProject().getVariablesContainer().getUndefinedVars();
		if (vars.isEmpty()) {	
			VarMessagesDialogs.showNotFindUndefinedVarsMsg(fShell);
		} else {
			UndefineVariablesDialog dialog = new UndefineVariablesDialog(fShell, fVarContainer.dipProject().getVariablesContainer(), vars);
			dialog.open();
		}
	}
		
	//====================
	// set input & refresh
	
	public void setInput(IVarContainer folder) {
		if (fViewer == null && fViewer.getTree().isDisposed()) {
			return;
		}
		if (fVarContainer == folder) {
			return;
		}
		fVarContainer = folder;
		if (fVarContainer == null) {
			fViewer.setInput(""); //$NON-NLS-1$
		} else {
			fViewer.setInput(fVarContainer);
			checkReadOnly();
		}
		fShowDuplicatesButton.setEnabled(fVarContainer instanceof ProjectVarContainer);
		fViewer.refresh();	
	}
	
	public void setFocus() {
		if (fViewer == null && fViewer.getTree().isDisposed()) {
			fViewer.getTree().setFocus();
		}		
	}
	
	/**
	 * Если контейнер нельзя изменять - сделать кнопки неактивными
	 */
	private void checkReadOnly() {
		if (fVarContainer.isReadOnly()) {
			fAddButton.setEnabled(false);
			fEditButton.setEnabled(false);
			fDeleteButton.setEnabled(false);
		}
	}
	
	public void refresh() {		
		if (fViewer != null && !fViewer.getTree().isDisposed()) {
			fViewer.refresh();
		}
	}
	
	//====================
	// dispose
	
	public boolean isDispose() {
		return fViewer == null 
				|| fViewer.getTree().isDisposed();
	}

	//=====================
	// getters
	
	public int getNameColumnWidth() { 
		return fNameColumn.getWidth();
	}
	
	public int getValueColumnWidth() {
		return fValueColumn.getWidth();
	}
	
	public boolean isDuplicateMode() {
		return fShowDuplicatesMode;
	}
	
}
