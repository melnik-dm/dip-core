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
package ru.dip.ui.table.ktable.actions.md;

import ru.dip.ui.Messages;
import ru.dip.ui.table.ktable.KTableComposite;

public class MdSplitAction extends MdExtractAction {
	
	private static final String ACTION_NAME = Messages.MdSplitAction_ActionName;
		
	public MdSplitAction(KTableComposite tableComposite) {
		super(tableComposite);
		setText(ACTION_NAME);
	}
	
	public MdSplitAction(MdSplitAction original) {
		super(original);
	}
		
	@Override
	protected void doAction() {
		fExtractResult = fTableComposite.splitMarkdown();
	}


}
