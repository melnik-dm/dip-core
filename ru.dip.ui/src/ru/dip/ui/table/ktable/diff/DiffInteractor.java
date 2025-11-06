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
package ru.dip.ui.table.ktable.diff;

import org.eclipse.jgit.lib.Repository;

import ru.dip.core.DipCorePlugin;
import ru.dip.core.model.DipTableContainer;
import ru.dip.core.model.interfaces.IDipParent;
import ru.dip.core.utilities.GITUtilities;
import ru.dip.ui.table.editor.DipTableEditor;
import ru.dip.ui.table.ktable.KTableComposite;

public class DiffInteractor {
	
	private KTableComposite fTableComposite;
		
	public DiffInteractor(KTableComposite kTableComposite) {
		fTableComposite = kTableComposite;
	}

	public void diffDocument() {
		// нужно найти репозиторий для проекта
		// текущий репозиторий
		IDipParent container = fTableComposite.editor().model().getContainer();
		Repository repo = GITUtilities.findRepo(container.resource());		
		if (repo == null) {
			DipCorePlugin.logError("DiffModeHandler: Not found repo");
			return;
		}		
		createDiffTableModel(fTableComposite.editor(), fTableComposite.model().getContainer(), 
				repo, "FETCH_HEAD^{tree}");		
	}
	
	private void createDiffTableModel(DipTableEditor editor, IDipParent container, Repository repo, String commitName ) {
		DiffModel model = new DiffModel((DipTableContainer) container, repo, commitName);		
		model.computeDiffs();
		editor.kTable().setDiffMode(true);
		editor.setNewModel(model);
		editor.kTable().updateBackgrouColor();
		editor.kTable().applyListMode();
	}

}
