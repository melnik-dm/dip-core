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

import java.util.Collection;
import java.util.List;

import ru.dip.core.model.DipUnit;
import ru.dip.core.model.interfaces.IDipParent;

public interface ITableNode extends IDipTableElement {

	int level();

	List<IDipTableElement> linkedElements(IDipTableElement startElement, HideElements exclude);

	IDipParent dipDocElement();

	IDipTableElement find(String movedElementName);

	ITableNode addNewFolderToBegin(IDipParent parent);

	IDipTableElement addNewUnitToStart(DipUnit unit);

	boolean expand();

	void setExpand(boolean expand);

	Collection<IDipTableElement> children();

	List<IDipTableElement> visibleUnitElements();

	IDipTableElement addNewUnit(DipUnit newUnit, IDipTableElement selectedElement, boolean b);

	void down(IDipTableElement element);

	void down(List<IDipTableElement> elements);

	void up(List<IDipTableElement> elements);

	void up(IDipTableElement element);

	void delete(IDipTableElement tableElement);

	ITableNode addNewNeightborFolder(IDipParent dipParent, ITableNode tableNode, boolean before);

	IDipTableElement startElement(IDipTableElement tableElement, HideElements hideElements);

	IDipTableElement endElement(IDipTableElement tableElement, HideElements hideElements);

	IDipTableElement addNewUnitToEnd(DipUnit unit);
	
	DipTableModel model();
}
