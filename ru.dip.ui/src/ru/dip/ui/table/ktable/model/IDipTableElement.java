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

import org.eclipse.swt.graphics.Color;

import ru.dip.core.model.interfaces.IDipDocumentElementHolder;
import ru.dip.core.utilities.ui.IBackground;

public interface IDipTableElement extends IBackground, IContentContainer, IDipDocumentElementHolder {

	//===================
	// связанные элементы
	
	void setNumber(int i);

	void setLinkedElements(List<IDipTableElement> currentElements);
	
	boolean isFirst();
	
	IDipTableElement startElement(HideElements exclude);
	
	List<IDipTableElement> allLinkedElements();

	List<IDipTableElement> getLinkedElements();

	IDipTableElement endElement(HideElements exclude);
	
	int getNumber();

	boolean isLast();
	
	List<IDipTableElement> linkedWithibleElements();

	//==============
	
	ITableNode parent();

	boolean isDiff();	
	
	boolean isVisible();
	
	//===============
	// presentation
		
	void prepare(int i, IDipTableModel dipTableModel);
	
	boolean hasFontPresentation();

	int height(IDipTableModel dipTableModel);
	
	void setCommentMainHeight(int i);
	
	int getCommentMainHeight();

	Color idBackground();
	
	
	//==============
	// edit
	
	void up();

	void down();

	void delete();
}
