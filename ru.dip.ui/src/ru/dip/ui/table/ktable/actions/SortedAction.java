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
import ru.dip.core.model.DipTableContainer;
import ru.dip.core.model.interfaces.IDipElement;
import ru.dip.core.model.interfaces.IDipDocumentElement;
import ru.dip.core.utilities.DipUtilities;
import ru.dip.core.utilities.tmp.SortedResult;
import ru.dip.ui.Messages;
import ru.dip.ui.table.ktable.KTableComposite;
import ru.dip.ui.table.ktable.actions.undo.CancelledDocumentAction;
import ru.dip.ui.table.ktable.model.TableNode;

public class SortedAction extends DocumentAction implements CancelledDocumentAction {
	
	private SortedResult fResult;

	public SortedAction(KTableComposite tableComposite) {
		super(tableComposite);
		setText(Messages.SortedAction_Name);
	}
	
	public SortedAction(SortedAction original) {
		super(original.fTableComposite);
		fResult = original.fResult;
	}
	
	private SortedAction copy() {
		return new SortedAction(this);
	}
	
	@Override
	public void run() {
		fResult = fTableComposite.doSort();
		if (fResult != null) {
			fTableComposite.actionStack().pushUndoAction(copy());
		}
	}
	
	@Override
	public void undo() throws Exception {
		String parentId = fResult.getParentId();
		Optional<IDipElement> elementOpt = DipUtilities.findDipElementInProject(parentId, fTableComposite.dipProject());
		if (elementOpt.isEmpty()) {
			throw new DIPException("Undo sort error. Element not found");
		}
		//fTableComposite.sor
		DipTableContainer parent = (DipTableContainer) elementOpt.get();		
		parent.sort(fResult.getOriginalOrder());
		// обновить node
		Optional<TableNode> nodeOpt = fTableComposite.tableModel().findNodeByName(parent);
		if (nodeOpt.isEmpty()) {
			throw new DIPException("Do sort Error");			
		}
		fTableComposite.editor().updater().updateParent(parent);
		fTableComposite.selector().setSelection(nodeOpt.get());
		
	}

	@Override
	public void redo() throws Exception {
		String parentId = fResult.getParentId();
		Optional<IDipElement> elementOpt = DipUtilities.findDipElementInProject(parentId, fTableComposite.dipProject());
		if (elementOpt.isEmpty()) {
			throw new DIPException("Undo sort error. Element not found");
		}
		DipTableContainer parent = (DipTableContainer) elementOpt.get();
		fTableComposite.doSort(parent);
		fTableComposite.selector().setSelection(parent);
	}

	@Override
	public void enableOneSelection(IDipDocumentElement selectedDipDocElement) {
		boolean readOnly = selectedDipDocElement.isReadOnly();												
		setEnabled(!readOnly);
	}



}
