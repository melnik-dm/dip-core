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

import ru.dip.core.model.DipUnit;
import ru.dip.core.model.interfaces.IDipDocumentElement;
import ru.dip.core.utilities.tmp.ImportResult;
import ru.dip.ui.Messages;
import ru.dip.ui.table.ktable.KTableComposite;
import ru.dip.ui.table.ktable.actions.DocumentAction;
import ru.dip.ui.table.ktable.actions.undo.CancelledDocumentAction;

public class ImportFileBeforeAction extends DocumentAction  implements CancelledDocumentAction  {

	private ImportResult fResult;
	
	public ImportFileBeforeAction(KTableComposite tableComposite) {
		super(tableComposite);
		setText(Messages.ImportFileBeforeAction_Name);
	}
	
	public ImportFileBeforeAction(ImportFileBeforeAction original) {
		super(original.fTableComposite);
		fResult = original.fResult;
	}
	
	public ImportFileBeforeAction copy() {
		return new ImportFileBeforeAction(this);
	}
	
	@Override
	public void run() {
		doAction();
		if (fResult != null) {			
			fTableComposite.actionStack().pushUndoAction(copy());
		} 
	}
	
	private void doAction() {
		fResult = fTableComposite.doImportBeforeFile();
	}
	
	@Override
	public void undo() throws Exception {
		fTableComposite.getImportActionInteractor().undoImport(fResult);
	}

	@Override
	public void redo() throws Exception {
		fTableComposite.getImportActionInteractor().redoImportFromResult(fResult);
	}
	
	//==============================
	//  when enabled
	
	@Override
	public void enableNullSelection() {
		setEnabled(false);
	}

	@Override
	public void enableOneSelection(IDipDocumentElement selectedDipDocElement) {
		boolean readOnly = selectedDipDocElement.isReadOnly();													
		if (!readOnly && selectedDipDocElement instanceof DipUnit) {
			setEnabled(true);
		} else {
			setEnabled(false);
		}
	}

	@Override
	public void enableSeveralSelection() {
		setEnabled(false);
	}

}
