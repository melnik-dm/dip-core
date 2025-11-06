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

import ru.dip.core.model.IncludeFolder;
import ru.dip.core.model.interfaces.IDipDocumentElement;
import ru.dip.core.utilities.tmp.DeleteResultOperation;
import ru.dip.core.utilities.tmp.PasteObjectsResult;
import ru.dip.ui.Messages;
import ru.dip.ui.table.ktable.KTableComposite;
import ru.dip.ui.table.ktable.actions.DocumentAction;
import ru.dip.ui.table.ktable.actions.manager.PasteInteractor;
import ru.dip.ui.table.ktable.actions.undo.CancelledDocumentAction;

public class PasteAction extends DocumentAction implements CancelledDocumentAction {

	private PasteObjectsResult fResultOperation;
	private DeleteResultOperation fDeleteResultOperation;
	
	public PasteAction(KTableComposite tableComposite) {
		super(tableComposite);
		setText(Messages.PasteAction_ActionName);
	}
	
	private PasteAction(PasteAction original) {
		super(original.fTableComposite);
		fResultOperation = original.fResultOperation;	
	}

	private PasteAction copy() {
		return new PasteAction(this);
	}
	
	@Override
	public void run() {
		fResultOperation = fTableComposite.doPaste();
		if (fResultOperation != null) {
			fTableComposite.actionStack().pushUndoAction(copy());
		}
	}
	
	@Override
	public void undo() throws Exception {
		PasteInteractor interactor = new PasteInteractor(fTableComposite);
		fDeleteResultOperation = interactor.undoCreate(fResultOperation);
	}

	@Override
	public void redo() throws Exception {
		fTableComposite.getDeleteInteractor().undo(fDeleteResultOperation);
	}

	@Override
	public void enableOneSelection(IDipDocumentElement selectedDipDocElement) {
		boolean readOnly = selectedDipDocElement.isReadOnly();												
		if (fTableComposite.model().isParentHeader(selectedDipDocElement)){
			setEnabled(false);
			return;
		}
		if (selectedDipDocElement instanceof IncludeFolder && !selectedDipDocElement.parent().isReadOnly()) {
			setEnabled(true);
			return;
		}
		setEnabled(!readOnly);
	}

}
