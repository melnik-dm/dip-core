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
package ru.dip.ui.table.ktable.actions.imprt;

import ru.dip.core.model.interfaces.IDipDocumentElement;
import ru.dip.core.utilities.tmp.ImportResult;
import ru.dip.ui.Messages;
import ru.dip.ui.table.ktable.KTableComposite;
import ru.dip.ui.table.ktable.actions.DocumentAction;
import ru.dip.ui.table.ktable.actions.undo.CancelledDocumentAction;

public class ImportFolderAction extends DocumentAction implements CancelledDocumentAction {

	private ImportResult fResult;
	
	public ImportFolderAction(KTableComposite tableComposite) {
		super(tableComposite);
		setText(Messages.ImportFolderAction_Name);
	}
	
	public ImportFolderAction(ImportFolderAction original) {
		super(original.fTableComposite);
		fResult = original.fResult;
	}
	
	public ImportFolderAction copy() {
		return new ImportFolderAction(this);
	}
	
	@Override
	public void run() {
		doAction();
		if (fResult != null) {			
			fTableComposite.actionStack().pushUndoAction(copy());
		} 
	}
	
	private void doAction() {
		fResult = fTableComposite.doImportFolder();
	}
	
	@Override
	public void undo() throws Exception {
		fTableComposite.getImportActionInteractor().undoImport(fResult);
	}

	@Override
	public void redo() throws Exception {
		fTableComposite.getImportActionInteractor().redoImportFolderFromResult(fResult);
	}

	//==============================
	// when enabled
	
	@Override
	public void enableNullSelection() {
		setEnabled(!fTableComposite.model().isReadOnly());
	}

	@Override
	public void enableOneSelection(IDipDocumentElement selectedDipDocElement) {
		boolean readOnly = selectedDipDocElement.isReadOnly();												
		if (!readOnly && fTableComposite.model().isTable(selectedDipDocElement)){
			setEnabled(true);
			return;
		}
		if (fTableComposite.model().isParentHeader(selectedDipDocElement)){
			return;
		}		
		setEnabled(!readOnly);
	}

	@Override
	public void enableSeveralSelection() {
		setEnabled(false);		
	}

}
