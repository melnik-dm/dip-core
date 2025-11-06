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
package ru.dip.ui.table.ktable.actions.create;

import ru.dip.core.exception.DIPException;
import ru.dip.core.model.interfaces.IDipParent;
import ru.dip.core.model.interfaces.IDipDocumentElement;
import ru.dip.core.utilities.tmp.CreateFolderResult;
import ru.dip.ui.Messages;
import ru.dip.ui.table.ktable.KTableComposite;
import ru.dip.ui.table.ktable.actions.DocumentAction;
import ru.dip.ui.table.ktable.actions.undo.CancelledDocumentAction;

public class NewFolderBeforeAction extends DocumentAction implements CancelledDocumentAction {

	private CreateFolderResult fResult;
	
	public NewFolderBeforeAction(KTableComposite tableComposite) {
		super(tableComposite);
		setText(Messages.NewFolderBeforeAction_Name);
	}
	
	public NewFolderBeforeAction(NewFolderBeforeAction original) {
		super(original.fTableComposite);
		fResult = original.fResult;
	}
		
	private NewFolderBeforeAction copy() {
		return new NewFolderBeforeAction(this);
	}
	
	@Override
	public void run() {
		doAction();
		if (fResult != null) {
			fTableComposite.actionStack().pushUndoAction(copy());
		}
	}
	
	public void doAction() {
		fResult = fTableComposite.doAddNewFolderBefore();	
	}
	
	@Override
	public void undo() throws DIPException {
		fTableComposite.getCreateFileInteractor().undoCreate(fResult);
	}
		
	@Override
	public void redo() throws DIPException {
		fTableComposite.getCreateFileInteractor().redoCreateFolderFromResult(fResult);
	}

	//=========================
	// when enable

	@Override
	public void enableNullSelection() {
		setEnabled(false);
	}

	@Override
	public void enableOneSelection(IDipDocumentElement selectedDipDocElement) {	
		if (selectedDipDocElement.isReadOnly()){
			setEnabled(false);
			return;
		}
		if (fTableComposite.model().isParentHeader(selectedDipDocElement)){
			setEnabled(false);
			return;
		}		
		setEnabled(selectedDipDocElement instanceof IDipParent);
	}

	@Override
	public void enableSeveralSelection() {
		setEnabled(false);
	}

}
