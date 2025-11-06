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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import ru.dip.core.exception.DIPException;
import ru.dip.core.model.interfaces.IDipElement;
import ru.dip.core.model.interfaces.IDipDocumentElement;
import ru.dip.core.utilities.DipUtilities;
import ru.dip.ui.Messages;
import ru.dip.ui.table.ktable.KTableComposite;
import ru.dip.ui.table.ktable.actions.undo.CancelledDocumentAction;

public class DisableAction extends DocumentAction implements CancelledDocumentAction {

	private List<String> fObjIds;
	
	public DisableAction(KTableComposite tableComposite) {
		super(tableComposite);
		setText(Messages.DisableAction_NameDisable);
	}
	
	public DisableAction(DisableAction original) {
		super(original.fTableComposite);
		fObjIds = original.fObjIds;
	}
	
	public DisableAction copy() {
		return new DisableAction(this);
	}

	public void updateName(IDipDocumentElement selectedDipDocElement) {
		if (selectedDipDocElement.isDisabled()) {
			setText(Messages.DisableAction_NameEnable);
		} else {
			setText(Messages.DisableAction_NameDisable);
		}
	}
	
	@Override
	public void run() {
		fObjIds = fTableComposite.doDisableObject();
		if (fObjIds != null) {
			fTableComposite.actionStack().pushUndoAction(copy());
		}
	}
	
	@Override
	public void undo() throws Exception {
		List<IDipDocumentElement> reqs = new ArrayList<>();
		for (String id: fObjIds) {
			Optional<IDipElement> elementOpt = DipUtilities.findDipElementInProject(id, fTableComposite.dipProject());
			if (elementOpt.isEmpty()) {
				throw new DIPException("Undo Disable Error");
			}
			reqs.add((IDipDocumentElement) elementOpt.get());
		}
		fObjIds = fTableComposite.doDisable(reqs);
	}

	@Override
	public void redo() throws Exception {
		undo();
	}


	@Override
	public void enableOneSelection(IDipDocumentElement selectedDipDocElement) {
		boolean readOnly = selectedDipDocElement.isReadOnly();												
		if (!readOnly) {
			setEnabled(true);
			updateName(selectedDipDocElement);
		} else {
			setEnabled(false);
		}		
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
		
		// disable
		boolean enable = false;
		boolean disable = false;			
		for (IDipDocumentElement req: fTableComposite.selector().getSelectedElements()) {
			if (req.isDisabled()) {
				disable = true;
			} else {
				enable = true;
			}
		}
		if (enable && !disable) {
			setEnabled(true);
			setText(Messages.DisableAction_NameDisable);
		} else if (disable && !enable) {
			setEnabled(true);
			setText(Messages.DisableAction_NameEnable);
		}
	}

}
