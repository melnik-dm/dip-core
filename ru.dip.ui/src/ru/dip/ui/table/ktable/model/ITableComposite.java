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

import ru.dip.core.model.finder.IFinder;
import ru.dip.core.model.interfaces.IDipDocumentElement;
import ru.dip.core.unit.form.IFormSettings;
import ru.dip.ui.table.table.TableModel;

public interface ITableComposite {

	IFinder getFinder();

	ISelector selector();
	
	boolean isSelect(IDipTableElement tableElement);

	IFormSettings getFormSettings();

	int tableHeight();

	void updateCommentFromCellEditor(IDipTableElement element, IDipDocumentElement dipDocElement, String newComment);

	void updateDescriptionFromCellEditor(IDipTableElement element, IDipDocumentElement dipDocElement,
			String newContent);

	TableModel model();
	
	ITableCompositeSetting getTableSettings();
}
