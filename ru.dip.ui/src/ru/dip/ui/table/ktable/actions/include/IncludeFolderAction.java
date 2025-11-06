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
package ru.dip.ui.table.ktable.actions.include;

import ru.dip.core.exception.DIPException;
import ru.dip.core.model.interfaces.IDipDocumentElement;
import ru.dip.core.utilities.tmp.IncludeFolderResult;
import ru.dip.ui.Messages;
import ru.dip.ui.table.ktable.KTableComposite;
import ru.dip.ui.table.ktable.actions.DocumentAction;
import ru.dip.ui.table.ktable.actions.undo.CancelledDocumentAction;

public class IncludeFolderAction extends DocumentAction implements CancelledDocumentAction {
	
	private IncludeFolderResult fResult;

	public IncludeFolderAction(KTableComposite tableComposite) {
		super(tableComposite);
		setText(Messages.IncludeFolderAction_Name);
	}
	
	public IncludeFolderAction(IncludeFolderAction original) {
		super(original.fTableComposite);
		fResult = original.fResult;
	}
	
	private IncludeFolderAction copy() {
		return new IncludeFolderAction(this);
	}
	
	@Override
	public void run() {
		doAction();
		if (fResult != null) {
			fTableComposite.actionStack().pushUndoAction(copy());
		}
	}
	
	public void doAction() {
		fResult = fTableComposite.doInclude();	
	}
	
	@Override
	public void undo() throws DIPException {
		fTableComposite.getIncludeInteractor().undoInclude(fResult);
	}
		
	@Override
	public void redo() throws DIPException {
		fTableComposite.getIncludeInteractor().redoIncludeFolderFromResult(fResult);
	}

	//=====================
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
