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
import ru.dip.core.utilities.tmp.PageBreakResult;
import ru.dip.ui.Messages;
import ru.dip.ui.table.ktable.KTableComposite;
import ru.dip.ui.table.ktable.actions.undo.CancelledDocumentAction;

public class PageBreakAction extends DocumentAction implements CancelledDocumentAction {
	
	public static enum PageBreakMode {		
		EACH_FOLDER, LAST_FOLDER, AUTO;
		
		public String getMenuLabel() {
			switch (this) {
			case EACH_FOLDER:{
				return Messages.PageBreakAction_EachFolderActionName;
			}
			case LAST_FOLDER:{
				return Messages.PageBreakAction_LastFolderActionName;
			}
			case AUTO:{
				return Messages.PageBreakAction_NoBreakActionName;
			}			
			}			
			return super.toString();
		}	
		
		public String getStringValue() {
			if (this != PageBreakMode.AUTO) {
				return toString();
			}	
			return null;
		}
		
	}
	
	private PageBreakMode fMode;
	private PageBreakResult fResult;

	public PageBreakAction(KTableComposite tableComposite, PageBreakMode mode) {
		super(tableComposite);		
		fMode = mode;
		setText(fMode.getMenuLabel());		
	}
	
	public PageBreakAction(PageBreakAction original) {
		super(original.fTableComposite);		
		fMode = original.fMode;
		fResult = original.fResult;
	}
	
	public PageBreakAction copy() {
		return new PageBreakAction(this);
	}
	
	@Override
	public int getStyle() {
		return AS_RADIO_BUTTON;
	}
	
	private void setChecked(IDipParent folder) {
		PageBreakMode mode = getFolderMode(folder);
		boolean checked = (mode == fMode); 
		setChecked(checked);
	}
	
	private PageBreakMode getFolderMode(IDipParent folder) {
		String value = folder.getPageBreak();
		if (value == null) {
			return PageBreakMode.AUTO;
		}
		try {
			return PageBreakMode.valueOf(value);
		} catch (IllegalArgumentException e) {
			return PageBreakMode.AUTO;
		}		
	}
	
	@Override
	public void run() {
		if (!isChecked()) {
			return;
		}
		fResult = setMode(fMode);
		if (fResult != null) {
			fTableComposite.actionStack().pushUndoAction(copy());
		}
	}
	
	private PageBreakResult setMode(PageBreakMode mode) {	
		return fTableComposite.doSetPageBreak(fMode.toString());
	}
	

	@Override
	public void undo() throws Exception {
		Optional<IDipElement> reqOpt = DipUtilities.findDipElementInProject(fResult.getDipParentId(), fTableComposite.dipProject());
		if (reqOpt.isEmpty()) {
			throw new DIPException(Messages.PageBreakAction_UndoPageBreadError);
		}			
		fTableComposite.doSetPageBreak(fResult.getOldValue(), (IDipParent) reqOpt.get());
	}	

	@Override
	public void redo() throws Exception {
		Optional<IDipElement> reqOpt = DipUtilities.findDipElementInProject(fResult.getDipParentId(), fTableComposite.dipProject());
		if (reqOpt.isEmpty()) {
			throw new DIPException(Messages.PageBreakAction_RedoPageBreakError);
		}			
		fTableComposite.doSetPageBreak(fResult.getNewValue(), (IDipParent) reqOpt.get());
	}


	@Override
	public void enableOneSelection(IDipDocumentElement selectedDipDocElement) {
		boolean readOnly = selectedDipDocElement.isReadOnly();
		if (fTableComposite.model().isTable(selectedDipDocElement)) {
			IDipParent folder = (IDipParent) selectedDipDocElement;			
			setChecked(folder);
			setEnabled(!readOnly);
			return;
		}
				
		if (fTableComposite.model().isParentHeader(selectedDipDocElement)){
			setEnabled(false);
			return;
		}		
		if (selectedDipDocElement instanceof IDipParent) {
			IDipParent folder = (IDipParent) selectedDipDocElement;			
			setChecked(folder);
			setEnabled(!readOnly);
		}		
	}

}
