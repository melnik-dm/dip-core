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
package ru.dip.ui.table.ktable.actions.imprt;

import ru.dip.core.model.Appendix;
import ru.dip.core.model.DipUnit;
import ru.dip.core.model.interfaces.IDipParent;
import ru.dip.core.model.interfaces.IDipDocumentElement;
import ru.dip.core.utilities.tmp.ImportResult;
import ru.dip.ui.Messages;
import ru.dip.ui.table.ktable.KTableComposite;
import ru.dip.ui.table.ktable.actions.DocumentAction;
import ru.dip.ui.table.ktable.actions.undo.CancelledDocumentAction;

public class ImportFileAction extends DocumentAction implements CancelledDocumentAction{

	private ImportResult fResult;
	
	public ImportFileAction(KTableComposite tableComposite) {
		super(tableComposite);
		setText(Messages.ImportFileAction_Name);
	}
	
	public ImportFileAction(ImportFileAction original) {
		super(original.fTableComposite);
		fResult = original.fResult;
	}
	
	public ImportFileAction copy() {
		return new ImportFileAction(this);
	}
	
	@Override
	public void run() {
		doAction();
		if (fResult != null) {			
			fTableComposite.actionStack().pushUndoAction(copy());
		} 
	}
	
	private void doAction() {
		fResult = fTableComposite.doImportFile();
	}
	
	@Override
	public void undo() throws Exception {
		fTableComposite.getImportActionInteractor().undoImport(fResult);
	}

	@Override
	public void redo() throws Exception {
		fTableComposite.getImportActionInteractor().redoImportFromResult(fResult);
	}
	
	//==============================
	//  when enabled

	@Override
	public void enableNullSelection() {
		setEnabled(!fTableComposite.model().isReadOnly());
	}

	@Override
	public void enableOneSelection(IDipDocumentElement selectedDipDocElement) {
		boolean readOnly = selectedDipDocElement.isReadOnly();												
		if (!readOnly && fTableComposite.model().isTable(selectedDipDocElement)){
			boolean isAppendix = ((IDipParent)selectedDipDocElement) instanceof Appendix;
			setEnabled(!isAppendix);
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
			setEnabled(true);									
		} else if (selectedDipDocElement instanceof IDipParent){					
			boolean isAppendix = ((IDipParent)selectedDipDocElement) instanceof Appendix;
			setEnabled(!isAppendix);
		}
	}

	@Override
	public void enableSeveralSelection() {
		setEnabled(false);
	}


}
