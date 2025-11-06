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
import ru.dip.core.utilities.DipTableUtilities;
import ru.dip.core.utilities.tmp.UpDownResult;
import ru.dip.ui.Messages;
import ru.dip.ui.table.ktable.KTableComposite;
import ru.dip.ui.table.ktable.actions.DocumentAction;
import ru.dip.ui.table.ktable.actions.undo.CancelledDocumentAction;

public class DownAction extends DocumentAction implements CancelledDocumentAction {

	private UpDownResult fResult;
	
	public DownAction(KTableComposite tableComposite) {
		super(tableComposite);
		setText(Messages.DownAction_Name);
	}
	
	public DownAction(DownAction original) {
		super(original.fTableComposite);
		fResult = original.fResult;
	}
	
	public DownAction copy() {
		return new DownAction(this);
	}
	
	@Override
	public String getText() {
		return Messages.DownAction_Name;
	}
	
	@Override
	public void run() {
		doAction();
		if (fResult != null) {
			fTableComposite.actionStack().pushUndoAction(copy());
		}		
	}
	
	private void doAction() {
		fResult = fTableComposite.doDown();
	}
	
	@Override
	public void undo() throws Exception {
		if (fResult != null) {
			fTableComposite.doUp(fResult);			
		}
	}

	@Override
	public void redo() throws Exception {
		if (fResult != null) {
			fTableComposite.doDown(fResult);			
		}
	}

	//==================================
	// when enabled

	@Override
	public void enableOneSelection(IDipDocumentElement selectedDipDocElement) {
		boolean readOnly = selectedDipDocElement.isReadOnly();												
		if (fTableComposite.model().isParentHeader(selectedDipDocElement)){
			setEnabled(false);
			return;
		}

		if (fTableComposite.isFilterMode()) {
			setEnabled(false);
			return;
		}
		
		if (selectedDipDocElement instanceof IncludeFolder && !selectedDipDocElement.parent().isReadOnly()) {
			setEnabled(DipTableUtilities.canDown(selectedDipDocElement));
			return;
		}
		
		if (!readOnly) {
			setEnabled(DipTableUtilities.canDown(selectedDipDocElement));
			return;
		}
		
		setEnabled(false);
	}

	@Override
	public void enableSeveralSelection() {
		// если выделен один из заголовков Section			
		IDipDocumentElement first = fTableComposite.selector().first();			
		if (first == null || fTableComposite.model().isTable(first) || fTableComposite.model().isParentHeader(first)) {
			setEnabled(false);
			return;
		}
		// if readonly
		if (fTableComposite.selector().hasReadOnlyObjects()) {
			setEnabled(false);
			return;
		}
		// down
		setEnabled (!fTableComposite.isFilterMode() 
				&& DipTableUtilities.canDown(fTableComposite.selector().getSelectedElements()));
	}

}
