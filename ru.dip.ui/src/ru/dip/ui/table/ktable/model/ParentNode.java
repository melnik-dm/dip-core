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
package ru.dip.ui.table.ktable.model;

import ru.dip.core.model.interfaces.IDipParent;
import ru.dip.core.utilities.DipUtilities;

public class ParentNode extends TableNode {

	public ParentNode(DipTableModel model, IDipParent dipParent, TableNode parent) {
		super(model, dipParent, parent);
	}
	
	@Override
	public int level() {	
		IDipParent folder = dipDocElement();
		int level = DipUtilities.getNestedLevel(folder.resource());	
		return level - 1;
	}

}
