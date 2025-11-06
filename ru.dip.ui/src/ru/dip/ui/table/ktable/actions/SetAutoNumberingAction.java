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

import java.util.Optional;

import ru.dip.core.exception.DIPException;
import ru.dip.core.model.interfaces.IDipElement;
import ru.dip.core.model.interfaces.IDipParent;
import ru.dip.core.model.interfaces.IDipDocumentElement;
import ru.dip.core.utilities.DipUtilities;
import ru.dip.core.utilities.tmp.AutoNumberingSettingResult;
import ru.dip.ui.Messages;
import ru.dip.ui.table.ktable.KTableComposite;
import ru.dip.ui.table.ktable.actions.undo.CancelledDocumentAction;

public class SetAutoNumberingAction extends DocumentAction implements CancelledDocumentAction {
	
	private AutoNumberingSettingResult fResult;

	public SetAutoNumberingAction(KTableComposite tableComposite) {
		super(tableComposite);
		setText(Messages.SetAutoNumberingAction_Name);
	}
	
	public SetAutoNumberingAction(SetAutoNumberingAction original) {
		super(original.fTableComposite);
		fResult = original.fResult;
	}
	
	public SetAutoNumberingAction copy() {
		return new SetAutoNumberingAction(this);
	}
	
	@Override
	public void run() {
		fResult = fTableComposite.doSetAutoNumbering();
		if (fResult != null) {
			fTableComposite.actionStack().pushUndoAction(copy());
		}		
	}
	

	@Override
	public void undo() throws Exception {
		Optional<IDipElement> reqOpt = DipUtilities.findDipElementInProject(fResult.getParentId(), fTableComposite.dipProject());
		if (reqOpt.isEmpty() || !(reqOpt.get() instanceof IDipParent)) {
			throw new DIPException("Undo Set AutoNumbering Error");
		}		
		IDipParent parent = (IDipParent) reqOpt.get();
		parent.setFileStep(fResult.getOldFileStep());
		parent.setFolderStep(fResult.getOldFolderStep());
		fTableComposite.editor().updater().updateAfterEnableAutoNumbering(parent);
	}

	@Override
	public void redo() throws Exception {
		Optional<IDipElement> reqOpt = DipUtilities.findDipElementInProject(fResult.getParentId(), fTableComposite.dipProject());
		if (reqOpt.isEmpty() || !(reqOpt.get() instanceof IDipParent)) {
			throw new DIPException("Redo Set AutoNumbering Error");
		}		
		IDipParent parent = (IDipParent) reqOpt.get();
		parent.setFileStep(fResult.getNewFileStep());
		parent.setFolderStep(fResult.getNewFolderStep());
		fTableComposite.editor().updater().updateAfterEnableAutoNumbering(parent);
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
