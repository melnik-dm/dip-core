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

import ru.dip.core.model.interfaces.IDipParent;
import ru.dip.core.model.interfaces.IDipDocumentElement;
import ru.dip.core.utilities.tmp.ApplyAutoNumberingResult;
import ru.dip.ui.Messages;
import ru.dip.ui.table.ktable.KTableComposite;
import ru.dip.ui.table.ktable.actions.undo.CancelledDocumentAction;

public class ApplyAutoNumberingAction extends DocumentAction implements CancelledDocumentAction {
	
	private ApplyAutoNumberingResult fResult;

	public ApplyAutoNumberingAction(KTableComposite tableComposite) {
		super(tableComposite);
		setText(Messages.ApplyAutoNumberingAction_Name);
	}
	
	public ApplyAutoNumberingAction(ApplyAutoNumberingAction original) {
		super(original.fTableComposite);
		fResult = original.fResult;
	}
	
	public ApplyAutoNumberingAction copy() {
		return new ApplyAutoNumberingAction(this);
	}
	
	@Override
	public void run() {
		fResult = fTableComposite.doApplyAutoNumbering();
		if (fResult != null) {
			fTableComposite.actionStack().pushUndoAction(copy());
		}
	}

	@Override
	public void undo() throws Exception {
		fTableComposite.getAutoNumberingInteractor().undoRedoRename(fResult, true);
	}

	@Override
	public void redo() throws Exception {
		fTableComposite.getAutoNumberingInteractor().undoRedoRename(fResult, false);		
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
		
		if (readOnly) {
			setEnabled(false);
			return;
		}				
		setEnabled(selectedDipDocElement  instanceof IDipParent);									
	}

}
