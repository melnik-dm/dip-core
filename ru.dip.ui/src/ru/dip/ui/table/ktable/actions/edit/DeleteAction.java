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
package ru.dip.ui.table.ktable.actions.edit;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import org.eclipse.swt.SWT;

import ru.dip.core.model.IncludeFolder;
import ru.dip.core.model.interfaces.IDipDocumentElement;
import ru.dip.core.utilities.tmp.ResultOperation;
import ru.dip.ui.Messages;
import ru.dip.ui.table.ktable.KTableComposite;
import ru.dip.ui.table.ktable.actions.DocumentAction;
import ru.dip.ui.table.ktable.actions.undo.CancelledDocumentAction;

public class DeleteAction extends DocumentAction implements CancelledDocumentAction {

	private ResultOperation fResultOperation;
	
	public DeleteAction(KTableComposite tableComposite) {
		super(tableComposite);
		setText(Messages.DeleteAction_Name);
		setAccelerator(SWT.DEL);
	}

	private DeleteAction(DeleteAction original) {
		super(original.fTableComposite);
		fResultOperation = original.fResultOperation;	
	}

	private DeleteAction copy() {
		return new DeleteAction(this);
	}

	@Override
	public void run() {
		fResultOperation = fTableComposite.doDeleteOPeration();
		if (fResultOperation != null && !fResultOperation.isError()) {
			fTableComposite.actionStack().pushUndoAction(copy());
		}
	}
	
	@Override
	public void undo() throws InvocationTargetException, IOException, InterruptedException {
		fTableComposite.getDeleteInteractor().undo(fResultOperation);
	}

	@Override
	public void redo() throws InvocationTargetException, InterruptedException {	
		fTableComposite.getDeleteInteractor().redoDelete(fResultOperation);
	}
	
	//============================
	// when enabled
	
	@Override
	public void enableOneSelection(IDipDocumentElement selectedDipDocElement) {
		boolean readOnly = selectedDipDocElement.isReadOnly();
		if (fTableComposite.model().isParentHeader(selectedDipDocElement)) {
			setEnabled(false);
			return;
		}

		if (selectedDipDocElement instanceof IncludeFolder && !selectedDipDocElement.parent().isReadOnly()) {
			setEnabled(true);
			return;
		}
		setEnabled(!readOnly);
	}

	@Override
	public void enableSeveralSelection() {
		// если выделен один из заголовков Section
		IDipDocumentElement first = fTableComposite.selector().first();
		if (first == null || fTableComposite.model().isTable(first) || fTableComposite.model().isParentHeader(first)) {
			setEnabled(false);
			return;
		}
		if (fTableComposite.selector().hasReadOnlyObjects()) {
			setEnabled(false);
			return;
		}
		setEnabled(true);
	}

}
