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
package ru.dip.ui.table.ktable.celleditors;

import ru.dip.ui.table.ktable.model.IDipTableElement;

public final class CellInfo {
	
	private final IDipTableElement fTableElement;
	private final int fColumn;
	private final int fRow;
	
	public CellInfo(IDipTableElement element, int column, int row) {
		fTableElement = element;
		fColumn = column;
		fRow = row;
	}
	
	public IDipTableElement getTableElement() {
		return fTableElement;
	}
	
	public int getColumn() {
		return fColumn;
	}
	
	public int getRow() {
		return fRow;
	}

}
