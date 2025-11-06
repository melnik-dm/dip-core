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
package ru.dip.ui.glossary;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
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

import de.kupzog.ktable.KTable;
import de.kupzog.ktable.KTableCellDoubleClickListener;
import de.kupzog.ktable.KTableCellSelectionListener;
import de.kupzog.ktable.SWTX;
import ru.dip.core.model.glossary.GlossaryField;
import ru.dip.core.model.glossary.GlossaryFolder;
import ru.dip.core.utilities.WorkbenchUtitlities;
import ru.dip.core.utilities.ui.CompositeBuilder;
import ru.dip.core.utilities.ui.GridDataFactory;
import ru.dip.core.utilities.ui.WSelectionListener;
import ru.dip.ui.Messages;
import ru.dip.ui.glossary.table.GlossaryEntry;
import ru.dip.ui.glossary.table.GlossaryTableModel;
import ru.dip.ui.utilities.image.ImageProvider;

public class GlossaryComposite {

	private static final String GLOSSARY_MSG_TITLE =  Messages.GlossaryComposite_TitleMsg;
	private static final String FIND_ABBREVIATIONS_TOOLTIP = Messages.GlossaryComposite_FindAbbrevToolTip;
	private static final String NOT_ABBREVIATIONS_MSG = Messages.GlossaryComposite_NotAbbrFoundMsg;
	
	// model
	private GlossaryFolder fGlossFolder;
	private List<GlossaryField> fUnusedFields;	// для поиска неиспользованных терминов
	private Collection<String> fTerms;   // для поиска аббревиатур по документу
	// controls
	private final Shell fShell;
	private ToolItem fEditButton;
	private ToolItem fDeleteButton;
	private Text fSearchText; 
	private KTable fTable;
	private GlossaryTableModel fGlossaryTableModel;
	
	public GlossaryComposite(Composite parent) {
		createContent(parent);
		fShell = parent.getShell();
	}
	
	private void createContent(Composite parent) {
		createToolBarComposite(parent);
		createTable(parent);
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
		createFindUnusedItem(toolbar);
		createFindAbbreviationsItem(toolbar);
	}
	
	private void createSearchText(Composite tbComposite) {
		fSearchText = new Text(tbComposite, SWT.SEARCH | SWT.ICON_CANCEL);
		GridData stGD = new GridData();
		stGD.widthHint = 160;
		fSearchText.setLayoutData(stGD);
		fSearchText.addModifyListener(new ModifyListener() {
			
			@Override
			public void modifyText(ModifyEvent e) {
				refresh();
			}
		});
	}
	
 	private void createAddItem(ToolBar toolbar) {
		ToolItem addItem = new ToolItem(toolbar, SWT.NONE);
		addItem.setImage(ImageProvider.ADD);
		addItem.setToolTipText(Messages.GlossaryComposite_NewToolTip);
		addItem.addSelectionListener(new WSelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				doNewField();				
			}
		});
	}
	
	private void createEditItem(ToolBar toolbar) {
		fEditButton = new ToolItem(toolbar, SWT.NONE);
		fEditButton.setImage(ImageProvider.EDIT);
		fEditButton.setToolTipText(Messages.GlossaryComposite_EditToolTip);
		fEditButton.setEnabled(false);
		fEditButton.addSelectionListener(new WSelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				doEditField();				
			}
		
		});		
	}
	
	private void createDeleteItem(ToolBar toolbar) {
		fDeleteButton = new ToolItem(toolbar, SWT.NONE);
		fDeleteButton.setImage(ImageProvider.DELETE);
		fDeleteButton.setToolTipText(Messages.GlossaryComposite_DeleteToolTip);
		fDeleteButton.setEnabled(false);
		fDeleteButton.addSelectionListener(new WSelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				doDeleteField();				
			}
		
		});
	}
	
	private void createFindUnusedItem(ToolBar toolbar) {
		ToolItem findUnusedTerms = new ToolItem(toolbar, SWT.NONE);
		findUnusedTerms.setImage(ImageProvider.FIND_UNSED);
		findUnusedTerms.setToolTipText(Messages.GlossaryComposite_ButtonToolTip);
		findUnusedTerms.addSelectionListener(new WSelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				doCheckFields();
			}
		});
	}
	
	private void createFindAbbreviationsItem(ToolBar toolbar) {
		ToolItem findAbbreviations = new ToolItem(toolbar, SWT.NONE);
		findAbbreviations.setImage(ImageProvider.ABBREVIATION);
		findAbbreviations.setToolTipText(FIND_ABBREVIATIONS_TOOLTIP);
		findAbbreviations.addSelectionListener(new WSelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				doFindAbbreviations();
			}
					
		});	
	}
		
	//==========================
	// table
	
	private void createTable(Composite parent) {
		Composite composite = CompositeBuilder.instance(parent).full().build();
		fTable = new KTable(composite,
				SWT.V_SCROLL | SWT.MULTI | SWT.FULL_SELECTION | SWTX.FILL_WITH_LASTCOL | SWTX.EDIT_ON_KEY);
		GridDataFactory.applyFillBoth(fTable);
	}
	
	private void setTableModel() {
		if (fGlossFolder != null) {
			fGlossaryTableModel = new GlossaryTableModel(fTable, fGlossFolder, this);
			fTable.setModel(fGlossaryTableModel);
			fTable.addControlListener(new ControlAdapter() {
					
				@Override
				public void controlResized(ControlEvent e) {
					fGlossaryTableModel.update();
				}

			});
			fTable.addCellSelectionListener(new KTableCellSelectionListener() {
				
				@Override
				public void fixedCellSelected(int col, int row, int statemask) {}
				
				@Override
				public void cellSelected(int col, int row, int statemask) {
					fGlossaryTableModel.select(col, row);
					fTable.redraw();					
					GlossaryField selection = fGlossaryTableModel.getSelectedField();				
					if (selection != null){
						fEditButton.setEnabled(true);
						fDeleteButton.setEnabled(true);
					} else {
						fEditButton.setEnabled(false);
						fDeleteButton.setEnabled(false);
					}
				}
									
			});
			
			fTable.addCellDoubleClickListener(new KTableCellDoubleClickListener() {
				
				@Override
				public void fixedCellDoubleClicked(int col, int row, int statemask) {}
				
				@Override
				public void cellDoubleClicked(int col, int row, int statemask) {
					if (row > 0) {
						GlossaryEntry entry = (GlossaryEntry) fGlossaryTableModel.getContentAt(0, row);
						doEditField(entry.getField());
					}
				}
			});		
		}
	}
	
	//=====================
	// update 
	
	public void setInput(GlossaryFolder folder) {
		if (isDispose()) {
			return;
		}		
		fUnusedFields = null;
		
		if (fGlossFolder == folder) {
			return;
		}
		fGlossFolder = folder;
		if (fGlossFolder == null) {
			setTableModel();
		} else {
			setTableModel();
		}
		fullUpdateTable();
	}
		
	private void fullUpdateTable() {		
		fGlossaryTableModel.fullUpdate();
		fTable.setModel(fGlossaryTableModel);
	}
	
	/**
	 * Проверка по строке поиска
	 */
	public boolean checkFieldByFilter(GlossaryField field) {
		if (fSearchText == null) {
			return true;
		}
		String searchText = fSearchText.getText().trim().toLowerCase();
		if (searchText.isEmpty()) {
			return true;
		}

		if (field.name().toLowerCase().contains(searchText)) {
			return true;
		}
		if (field.getValue().contains(searchText)) {
			return true;
		}

		return false;
	}
	
	public void refresh() {		
		if (!isDispose()) {
			fullUpdateTable();
		}
	}
	
	//=======================
	// actions
	
	private void doNewField(){
		NewGlossFieldDialog dialog = new NewGlossFieldDialog(fShell, fGlossFolder);
		dialog.open();
		fullUpdateTable();
	}
	
	private void doEditField(){
		GlossaryField field = getSelectedField();
		doEditField(field);
	}
	
	private void doEditField(GlossaryField field ){
		if (field != null){
			EditGlossFieldDialog dialog = new EditGlossFieldDialog(fShell, field);
			dialog.open();
			fullUpdateTable();
		}
	}
		
	private void doDeleteField(){
		GlossaryField field = getSelectedField();
		if (field != null){
			boolean confirmation =  MessageDialog.openQuestion(fShell, Messages.GlossaryComposite_DeleteQuestionTitle, Messages.GlossaryComposite_DeleteConfirmMessage);
			if (confirmation){
				try {
					fGlossFolder.deleteField(field);										
					fullUpdateTable();					
					WorkbenchUtitlities.updateProjectExplorer();
				} catch (IOException e) {
					MessageDialog.openError(fShell, Messages.GlossaryComposite_GlossError, Messages.GlossaryComposite_SaveGlossErrorMessage);
					e.printStackTrace();
				}
			}
		}
	}
	
	private void doCheckFields() {
		try {
			WorkbenchUtitlities.runWithCursorBusy(new IRunnableWithProgress() {				
				@Override
				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					fUnusedFields = fGlossFolder.findUnsedFields();
				}
			});
		
			if (fUnusedFields != null && !fUnusedFields.isEmpty()) {
				MessageDialog.openInformation(fShell, GLOSSARY_MSG_TITLE, Messages.GlossaryComposite_FindMessage + fUnusedFields.size());
				// надо добавить чтобы переходил к первому найденому 				
			} else {
				MessageDialog.openInformation(fShell, GLOSSARY_MSG_TITLE, Messages.GlossaryComposite_NotFoundMessage);
			}
		} catch (InvocationTargetException | InterruptedException e) {
			e.printStackTrace();
		}
	}
		
	private void doFindAbbreviations() {
		try {
			WorkbenchUtitlities.runWithCursorBusy(new IRunnableWithProgress() {				
				@Override
				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					fTerms = fGlossFolder.findAbbreviations(); 			}
			});
		} catch (InvocationTargetException | InterruptedException e) {
			e.printStackTrace();
		}
		
		if (fTerms == null || fTerms.isEmpty()) {
			MessageDialog.openInformation(fShell, GLOSSARY_MSG_TITLE, NOT_ABBREVIATIONS_MSG);

		} else {
			FindAbreviationDialog dialog = new FindAbreviationDialog(fShell,
				fGlossFolder, fTerms);
			dialog.open();
			fullUpdateTable();
		}
	}
		
	//========================
	//========================
	
	private GlossaryField getSelectedField(){
		return fGlossaryTableModel.getSelectedField();
	}
	
	public boolean isUnused(GlossaryField field) {
		return fUnusedFields != null && fUnusedFields.contains(field);
	}

	public boolean isDispose() {
		return fTable == null || fTable.isDisposed();
	}	
}
