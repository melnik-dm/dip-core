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
package ru.dip.ui.table.ktable.actions.undo;

import java.util.ArrayDeque;
import java.util.Deque;

import ru.dip.core.utilities.WorkbenchUtitlities;

public class ActionStack {
	
	private UndoAction fUndo;
	private RedoAction fRedo;	
	private Deque<CancelledDocumentAction> fUndoActions = new ArrayDeque<>(10);
	private Deque<CancelledDocumentAction> fRedoActions = new ArrayDeque<>(10);

	/**
	 * Если документ изменился, на другой вкладке, через гит и т.п. обнулить стак
	 */
	public void check() {
		if (documentChanged()) {
			fUndoActions.clear();
			fRedoActions.clear();
		}
	}
	
	private boolean documentChanged() {
		return false;
	}
	
	public void pushUndoAction(CancelledDocumentAction action) {
		if (fUndoActions.size() == 10) {
			fUndoActions.removeFirst();
		}
		fUndoActions.addLast(action);
		
		if (!fUndo.isEnabled()) {
			fUndo.setEnabled(true);
		}
	}
	
	public CancelledDocumentAction popUndoAction() {
		return fUndoActions.pollLast();
	}
	
	public String getLastUndoActionName() {
		if (fUndoActions.isEmpty()) {
			return "Undo";
		} else {
			String actionText = fUndoActions.getLast().getText();
			if (actionText != null && !actionText.isEmpty()) {
				return "Undo " + actionText;
			}
			return "Undo";
		}
	}
	
	public void pushRedoAction(CancelledDocumentAction action) {
		if (fRedoActions.size() == 10) {
			fRedoActions.removeFirst();
		}
		fRedoActions.addLast(action);
		
		if (!fRedo.isEnabled()) {
			fRedo.setEnabled(true);
		}
	}
	
	public CancelledDocumentAction popRedoAction() {
		return fRedoActions.pollLast();
	}
	
	public void undo() {
		CancelledDocumentAction action = popUndoAction();		
		if (action != null) {
			try {
				action.undo();
				pushRedoAction(action);				
				fUndo.setText(getLastUndoActionName());				
			} catch (Exception e) {
				handleError(e);
				return;
			}
		}
	}
	
	public void redo() {
		CancelledDocumentAction action = popRedoAction();
		if (action != null) {
			try {
				action.redo();
				pushUndoAction(action);
			} catch (Exception e) {
				handleError(e);
				return;
			}
		}
	}
	
	private void handleError(Exception e) {
		WorkbenchUtitlities.openError("Redo error", e.getMessage());
		e.printStackTrace();
		fUndoActions.clear();
		fRedoActions.clear();
	}
	
	public boolean hasUndo() {
		return !fUndoActions.isEmpty();
	}
	
	public boolean hasRedo() {
		return !fRedoActions.isEmpty();
	}

	public void setUndoAction(UndoAction undoAction) {
		fUndo = undoAction;
	}
	
	public void setRedoAction(RedoAction redoAction) {
		fRedo = redoAction;
	}

}
