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
import ru.dip.core.model.Appendix;
import ru.dip.core.model.DipUnit;
import ru.dip.core.model.interfaces.IDipElement;
import ru.dip.core.model.interfaces.IDipParent;
import ru.dip.core.model.interfaces.IDipDocumentElement;
import ru.dip.core.utilities.DipUtilities;
import ru.dip.core.utilities.tmp.ActivateNumerationResult;
import ru.dip.ui.Messages;
import ru.dip.ui.table.ktable.KTableComposite;
import ru.dip.ui.table.ktable.actions.undo.CancelledDocumentAction;

public class ActivateReqNumerationAction extends DocumentAction implements CancelledDocumentAction {

	private ActivateNumerationResult fResult;
	
	public ActivateReqNumerationAction(KTableComposite tableComposite) {
		super(tableComposite);
		setText(Messages.ActivateReqNumerationAction_Name);
	}
	
	public ActivateReqNumerationAction(ActivateReqNumerationAction original) {
		super(original.fTableComposite);
		fResult = original.fResult;
	}
	
	public ActivateReqNumerationAction copy() {
		return new ActivateReqNumerationAction(this);
	}
	
	@Override
	public void run() {
		fResult = fTableComposite.doActivateNumeration();
		if (fResult != null) {
			fTableComposite.actionStack().pushUndoAction(copy());
		}
	}
	
	@Override
	public void undo() throws Exception {
		Optional<IDipElement> reqOpt = DipUtilities.findDipElementInProject(fResult.getParentId(), fTableComposite.dipProject());
		if (reqOpt.isEmpty() || !(reqOpt.get() instanceof IDipParent)) {
			throw new DIPException("Undo Set Numeration Error");
		}
		fTableComposite.doActivateNumeration((IDipParent) reqOpt.get());
	}

	@Override
	public void redo() throws Exception {
		Optional<IDipElement> reqOpt = DipUtilities.findDipElementInProject(fResult.getParentId(), fTableComposite.dipProject());
		if (reqOpt.isEmpty() || !(reqOpt.get() instanceof IDipParent)) {
			throw new DIPException("Redo Set Numeration Error");
		}
		fTableComposite.doActivateNumeration((IDipParent) reqOpt.get());		
	}

				
	public void setChecked(IDipParent parent){
		boolean checked = parent.isActiveNumeration();
		setChecked(checked);
	}

	@Override
	public void enableOneSelection(IDipDocumentElement selectedDipDocElement) {
		boolean readOnly = selectedDipDocElement.isReadOnly();												
		if (!readOnly && fTableComposite.model().isTable(selectedDipDocElement)){
			boolean isAppendix = ((IDipParent)selectedDipDocElement) instanceof Appendix;
			boolean isAppendixParition = Appendix.isAppendixPartition((IDipParent)selectedDipDocElement);
			setEnabled(!isAppendix && !isAppendixParition);
			setChecked(fTableComposite.model());
			return;
		}
		if (fTableComposite.model().isParentHeader(selectedDipDocElement)){
			return;
		}
		
		if (readOnly) {
			setEnabled(false);
			return;
		}
				
		if (selectedDipDocElement instanceof DipUnit) {
			setEnabled(false);									
		} else if (selectedDipDocElement instanceof IDipParent){					
			boolean isAppendix = ((IDipParent)selectedDipDocElement) instanceof Appendix;
			boolean isAppendixParition = Appendix.isAppendixPartition((IDipParent)selectedDipDocElement);
			setEnabled(!isAppendix && !isAppendixParition);
			setChecked((IDipParent) selectedDipDocElement);
		}
	}

}
