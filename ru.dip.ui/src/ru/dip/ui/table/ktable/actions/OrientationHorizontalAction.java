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
import ru.dip.core.model.DipUnit;
import ru.dip.core.model.interfaces.IDipElement;
import ru.dip.core.model.interfaces.IDipDocumentElement;
import ru.dip.core.utilities.DipUtilities;
import ru.dip.core.utilities.tmp.OrientationResult;
import ru.dip.ui.Messages;
import ru.dip.ui.table.ktable.KTableComposite;
import ru.dip.ui.table.ktable.actions.undo.CancelledDocumentAction;

public class OrientationHorizontalAction extends DocumentAction implements CancelledDocumentAction {

	private OrientationResult fResult;
	
	public OrientationHorizontalAction(KTableComposite tableComposite) {
		super(tableComposite);
		setText(Messages.HorizontalOrientationAction_Name);
	}
	
	public OrientationHorizontalAction(OrientationHorizontalAction original) {
		super(original.fTableComposite);
		fResult = original.fResult;

	}
	
	public OrientationHorizontalAction copy() {
		return new OrientationHorizontalAction(this);
	}
		
	@Override
	public int getStyle() {
		return AS_RADIO_BUTTON;
	}
	
	public void setChecked(DipUnit unit) {
		boolean checked = unit.isHorizontalOrientation();
		setChecked(checked);
	}
	
	@Override
	public void run() {
		fResult = fTableComposite.doSetHorizontalOrientation(true);
		if (fResult != null) {		
			fTableComposite.actionStack().pushUndoAction(copy());
		}
	}
	
	
	@Override
	public void undo() throws Exception {
		Optional<IDipElement> reqOpt = DipUtilities.findDipElementInProject(fResult.getDipDocElementId(), fTableComposite.dipProject());
		if (reqOpt.isEmpty()) {
			throw new DIPException("Undo Set Orientation Error");
		}	
		fTableComposite.doSetHorizontalOrientation((DipUnit) reqOpt.get(), false);		
	}

	@Override
	public void redo() throws Exception {
		Optional<IDipElement> reqOpt = DipUtilities.findDipElementInProject(fResult.getDipDocElementId(), fTableComposite.dipProject());
		if (reqOpt.isEmpty()) {
			throw new DIPException("Redo Set Orientation Error");
		}	
		fTableComposite.doSetHorizontalOrientation((DipUnit) reqOpt.get(), true);		
	}

	@Override
	public void enableOneSelection(IDipDocumentElement selectedDipDocElement) {
		boolean readOnly = selectedDipDocElement.isReadOnly();													
		if (!readOnly && selectedDipDocElement instanceof DipUnit) {
			setChecked((DipUnit) selectedDipDocElement);
			setEnabled(true);
		} else {
			setEnabled(false);
		}
	}

}
