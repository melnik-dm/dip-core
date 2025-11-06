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

import java.util.Optional;

import org.eclipse.swt.SWT;

import ru.dip.core.exception.RenameDIPException;
import ru.dip.core.model.IncludeFolder;
import ru.dip.core.model.interfaces.IDipElement;
import ru.dip.core.model.interfaces.IDipParent;
import ru.dip.core.model.interfaces.IDipDocumentElement;
import ru.dip.core.utilities.DipUtilities;
import ru.dip.core.utilities.tmp.RenameResult;
import ru.dip.ui.Messages;
import ru.dip.ui.table.ktable.KTableComposite;
import ru.dip.ui.table.ktable.actions.DocumentAction;
import ru.dip.ui.table.ktable.actions.undo.CancelledDocumentAction;

public class RenameAction extends DocumentAction implements CancelledDocumentAction {

	private RenameResult fResult;
	
	public RenameAction(KTableComposite tableComposite) {
		super(tableComposite);
		setText(Messages.RenameAction_Name);
		setAccelerator(SWT.F2);
	}
	
	public RenameAction(RenameAction original) {
		super(original.fTableComposite);
		fResult = original.fResult;
	}
	
	public RenameAction copy() {
		return new RenameAction(this);
	}
	
	@Override
	public void run() {
		doAction();
		if (fResult != null) {
			fTableComposite.actionStack().pushUndoAction(copy());
		} 
	}
	
	private void doAction() {
		fResult = fTableComposite.doRename();
	}
	
	@Override
	public void undo() throws Exception {
		Optional<IDipElement> dipParentOpt = DipUtilities.findDipElementInProject(fResult.getParentId(), fTableComposite.dipProject());
		if (dipParentOpt.isEmpty() || !(dipParentOpt.get() instanceof IDipParent)) {
			throw new RenameDIPException("Undo Rename Error");
		}
		IDipDocumentElement dipDocumentElement = (IDipDocumentElement) ((IDipParent)dipParentOpt.get()).getChild(fResult.getNewName());
		if (dipDocumentElement == null) {
			throw new RenameDIPException("Undo Rename Error");
		}
		
		fTableComposite.doRenameWithoutResult(dipDocumentElement, fResult.getOldName(), false);		
	}

	@Override
	public void redo() throws Exception {
		Optional<IDipElement> dipParentOpt = DipUtilities.findDipElementInProject(fResult.getParentId(), fTableComposite.dipProject());
		if (dipParentOpt.isEmpty() || !(dipParentOpt.get() instanceof IDipParent)) {
			throw new RenameDIPException("Redo Rename Error");
		}
		IDipDocumentElement dipDocElement = (IDipDocumentElement) ((IDipParent)dipParentOpt.get()).getChild(fResult.getOldName());
		if (dipDocElement == null) {
			throw new RenameDIPException("Redo Rename Error");
		}		
		fTableComposite.doRenameWithoutResult(dipDocElement, fResult.getNewName(), false);
	}

	//==================================
	//  when enabled
	
	@Override
	public void enableOneSelection(IDipDocumentElement selectedDipDocElement) {
		boolean readOnly = selectedDipDocElement.isReadOnly();												
		if (selectedDipDocElement instanceof IncludeFolder && !selectedDipDocElement.parent().isReadOnly()) {
			setEnabled(true);
			return;
		}
		setEnabled(!readOnly);
	}

}
