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

import java.util.List;

import ru.dip.core.model.DipUnit;
import ru.dip.core.model.interfaces.IDipParent;

public interface ITableNode extends ITableElementContainer, ITablePaintable, IDipTableElement {

	ITableNode parent();
	
	int level();

	IDipParent dipDocElement();

	ITableNode addNewFolderToBegin(IDipParent parent);

	IDipTableElement addNewUnitToStart(DipUnit unit);

	boolean expand();

	void setExpand(boolean expand);

	List<IDipTableElement> visibleUnitElements();

	IDipTableElement addNewUnit(DipUnit newUnit, IDipTableElement selectedElement, boolean b);

	void down(IDipTableElement element);

	void down(List<IDipTableElement> elements);

	void up(List<IDipTableElement> elements);

	void up(IDipTableElement element);

	void delete(IDipTableElement tableElement);

	ITableNode addNewNeightborFolder(IDipParent dipParent, ITableNode tableNode, boolean before);

	IDipTableElement addNewUnitToEnd(DipUnit unit);
	
	IDipTableModel model();
}
