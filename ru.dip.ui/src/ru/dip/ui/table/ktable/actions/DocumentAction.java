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

import org.eclipse.jface.action.Action;

import ru.dip.core.model.interfaces.IDipDocumentElement;
import ru.dip.ui.table.ktable.KTableComposite;

public abstract class DocumentAction extends Action implements SelectionUpdatable {
	
	protected KTableComposite fTableComposite;
	
	public DocumentAction(KTableComposite tableComposite) {
		fTableComposite = tableComposite;
	}
	
	@Override
	public void enableNullSelection() {
		setEnabled(false);
	}

	@Override
	public void enableOneSelection(IDipDocumentElement selectedDipDocElement) {	
		setEnabled(false);
	}
	
	@Override
	public void enableSeveralSelection() {
		setEnabled(false);
	}
	
}
