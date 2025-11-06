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

import org.eclipse.jface.action.Action;

import ru.dip.ui.table.editor.DipTableEditor;

public class RedoAction extends Action {
	
	private DipTableEditor fEditor;

	public RedoAction(DipTableEditor editor) {
		fEditor = editor;
		setText("Redo");
		setEnabled(false);
	}
	
	@Override
	public void run() {
		fEditor.kTable().actionStack().redo();
		if (!fEditor.kTable().actionStack().hasRedo()) {
			setEnabled(false);
		}
	}

}
