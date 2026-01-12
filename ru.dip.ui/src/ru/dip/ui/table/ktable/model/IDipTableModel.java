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

import ru.dip.ktable.DipTable;
import ru.dip.ktable.model.IKTableModel;
import ru.dip.ui.table.table.TableModel;

public interface IDipTableModel extends IKTableModel  {
	
	//===========================
	// поддержка плавной прокрутки


	int firstCell();
	
	int indent();
	
	
	//int getIndent(IDipTableElement element);
	
	//=========================
	// для CellEditor
	
	void setEditRow(int row);
	
	//??
	void setEditRowHeight(int cellEditorHeight);
	
	Object getEditedCellInfo();

	void setEditedCellInfo(Object cellInfo);
	
	void saveUnit(String startContent, String newContent, Object unit);
	
	
	//=========================
	// getters
	

	boolean isShowId();

	boolean isShowComment();


	//??
	DipTable getTable();

	ITableComposite tableComposite();

	TableModel getTableModel();
}
