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

import org.eclipse.swt.graphics.Rectangle;

import de.kupzog.ktable.KTable;
import de.kupzog.ktable.KTableCellEditor;

abstract class AbstractCellEditor extends KTableCellEditor  {
		
	@Override
	public void setContent(Object content) {}

	@Override
	public void open(KTable table, int col, int row, Rectangle rect) {		
		super.open(table, col, row, rect);	
		setValue();
	}
	
	/**
	 *  Установить значение при открытии редактора
	 */
	abstract void setValue();
	
	
	@Override
	public void close(boolean save) {
		if (save) {
			save();
		}				
		super.close(save);
	}
	
	/**
	 * Сохранить значение после закрытия редактора
	 */
	abstract void save();
	
}
