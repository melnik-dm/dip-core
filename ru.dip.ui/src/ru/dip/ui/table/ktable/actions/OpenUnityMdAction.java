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

import ru.dip.core.model.interfaces.IDipParent;
import ru.dip.core.model.interfaces.IDipDocumentElement;
import ru.dip.core.utilities.WorkbenchUtitlities;
import ru.dip.ui.Messages;
import ru.dip.ui.table.ktable.KTableComposite;

public class OpenUnityMdAction extends DocumentAction {

	public OpenUnityMdAction(KTableComposite tableComposite) {
		super(tableComposite);
		setText(Messages.OpenUnityMdAction_ActionName);
	}
	
	@Override
	public void run() {		
		IDipParent parent = fTableComposite.selector().getSelectionParent();
		WorkbenchUtitlities.openUnityMdEditor(parent.resource());	
	}
	
	@Override
	public void enableOneSelection(IDipDocumentElement selectedDipDocElement) {
		if (selectedDipDocElement instanceof IDipParent && !selectedDipDocElement.isReadOnly()) {
			setEnabled(true);
		} else {
			setEnabled(false);
		}
	}

}
