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
package ru.dip.ui.table.editor;

import org.eclipse.jface.action.Action;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.part.EditorActionBarContributor;

import ru.dip.ui.table.ktable.actions.edit.CopyAction;
import ru.dip.ui.table.ktable.actions.edit.PasteAction;
import ru.dip.ui.table.ktable.actions.undo.RedoAction;
import ru.dip.ui.table.ktable.actions.undo.UndoAction;

public class DipTableActionContributor extends EditorActionBarContributor {

	private FindAction fFindAction;
	private UndoAction fUndoAction;
	private RedoAction fRedoAction;
	private IActionBars fActionBars;
	private CopyAction fCopyAction;
	private PasteAction fPasteAction;
	
	@Override
	public void init(IActionBars bars) {
		fActionBars = bars;
		super.init(bars);		
		bars.setGlobalActionHandler(ActionFactory.FIND.getId(), fFindAction = new FindAction());
	}
	
	@Override
	public void setActiveEditor(IEditorPart targetEditor) {
		if (fFindAction != null) {
			fFindAction.setNewEditor(targetEditor);
		}
		
		// привязка команд undo/redo
		if (targetEditor instanceof DipTableEditor) {
			DipTableEditor dipTableEditor = (DipTableEditor) targetEditor;
			fRedoAction = new RedoAction(dipTableEditor);
			fUndoAction = new UndoAction(dipTableEditor);
			fCopyAction = new CopyAction(dipTableEditor.kTable());
			fPasteAction = new PasteAction(dipTableEditor.kTable());
			fActionBars.setGlobalActionHandler(ActionFactory.UNDO.getId(), fUndoAction);
			fActionBars.setGlobalActionHandler(ActionFactory.REDO.getId(), fRedoAction);
			fActionBars.setGlobalActionHandler(ActionFactory.COPY.getId(), fCopyAction);
			fActionBars.setGlobalActionHandler(ActionFactory.PASTE.getId(), fPasteAction);
			dipTableEditor.kTable().actionStack().setUndoAction(fUndoAction);
			dipTableEditor.kTable().actionStack().setRedoAction(fRedoAction);
		}
	}
	
	class FindAction extends Action {
	
		private FindDialog fDialog;
		private DipTableEditor fEditor;

		@Override
		public void run() {		
			if (fEditor == null) {
				return;
			}
			fDialog = new FindDialog(fEditor, fEditor.getSite().getShell());
			fDialog.open();
		}
		
		void setNewEditor(IEditorPart targetEditor) {
			fEditor = targetEditor instanceof DipTableEditor ? (DipTableEditor) targetEditor : null;		
			if (fDialog != null) {
				fDialog.setNewEditor(targetEditor);
			}
		}
		
	}
	
}
