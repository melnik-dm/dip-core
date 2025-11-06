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
package ru.dip.ui.table.ktable.actions;

import ru.dip.core.exception.DIPException;
import ru.dip.core.model.interfaces.IDipDocumentElement;
import ru.dip.core.utilities.tmp.EditDescriptionResult;
import ru.dip.ui.Messages;
import ru.dip.ui.table.ktable.KTableComposite;
import ru.dip.ui.table.ktable.actions.undo.CancelledDocumentAction;

public class EditDescriptionAction extends DocumentAction implements CancelledDocumentAction {
	
	private EditDescriptionResult fResult;

	public EditDescriptionAction(KTableComposite tableComposite) {
		super(tableComposite);
		setText(Messages.EditDescriptionAction_Name);
	}
	
	public EditDescriptionAction(KTableComposite tableComposite, EditDescriptionResult result) {
		super(tableComposite);
		fResult = result;
	}

	public EditDescriptionAction(EditDescriptionAction original) {
		super(original.fTableComposite);
		fResult = original.fResult;
	}
	
	public EditDescriptionAction copy() {
		return new EditDescriptionAction(this);
	}
	
	@Override
	public void run() {
		doAction();
		if (fResult != null) {			
			fTableComposite.actionStack().pushUndoAction(copy());
		} 
	}
	
	private void doAction() {
		fResult = fTableComposite.doEditDescriptionInDialog();
	}
	
	@Override
	public void undo() throws DIPException {
		fTableComposite.getEditDescriptionInteractor().undoEditDescription(fResult);
	}

	@Override
	public void redo() throws DIPException {
		fTableComposite.getEditDescriptionInteractor().redoEditDescription(fResult);
	}
	
	//=======================
	// when enabled

	@Override
	public void enableOneSelection(IDipDocumentElement selectedDipDocElement) {
		boolean readOnly = selectedDipDocElement.isReadOnly();
		setEnabled(!readOnly);
	}
}
