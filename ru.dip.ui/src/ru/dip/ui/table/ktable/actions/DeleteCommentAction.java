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
import ru.dip.core.model.interfaces.IDipComment;
import ru.dip.core.model.interfaces.IDipDocumentElement;
import ru.dip.core.utilities.tmp.EditCommentResult;
import ru.dip.ui.Messages;
import ru.dip.ui.table.ktable.KTableComposite;
import ru.dip.ui.table.ktable.actions.undo.CancelledDocumentAction;

public class DeleteCommentAction extends DocumentAction implements CancelledDocumentAction {

	private EditCommentResult fResult;
	
	public DeleteCommentAction(KTableComposite tableComposite) {
		super(tableComposite);
		setText(Messages.DeleteCommentAction_Name);
	}
	
	public DeleteCommentAction(DeleteCommentAction original) {
		super(original.fTableComposite);
		fResult = original.fResult;
	}
	
	public DeleteCommentAction copy() {
		return new DeleteCommentAction(this);
	}
	
	@Override
	public void run() {
		doAction();
		if (fResult != null) {			
			fTableComposite.actionStack().pushUndoAction(copy());
		} 
	}
	
	private void doAction() {
		fResult = fTableComposite.doDeleteComment();
	}
	
	@Override
	public void undo() throws DIPException {
		fTableComposite.getEditCommentInteractor().undoEditComment(fResult);
	}

	@Override
	public void redo() throws DIPException {
		fTableComposite.getEditCommentInteractor().redoEditComment(fResult);
	}
	
	//=======================
	// when enabled
	@Override
	public void enableOneSelection(IDipDocumentElement selectedDipDocElement) {
		boolean readOnly = selectedDipDocElement.isReadOnly();
		if (readOnly) {
			setEnabled(false);
			return;
		}
			
		IDipComment recComment = selectedDipDocElement.comment();
		if (recComment != null && recComment.getCommentContent() != null && !recComment.getCommentContent().isEmpty()){
			setEnabled(true);
		} else {
			setEnabled(false);
		}
	}

}
