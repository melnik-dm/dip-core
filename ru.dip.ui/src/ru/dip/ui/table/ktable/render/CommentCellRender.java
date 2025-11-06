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
package ru.dip.ui.table.ktable.render;

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;

import de.kupzog.ktable.KTableModel;
import ru.dip.core.model.interfaces.IDipDocumentElement;
import ru.dip.core.utilities.ui.gc.GCUtils;
import ru.dip.core.utilities.ui.swt.ColorProvider;
import ru.dip.ui.table.ktable.model.DipTableModel;
import ru.dip.ui.table.ktable.model.IDipTableElement;

public class CommentCellRender extends DipRender {

	public CommentCellRender(DipTableModel tableModel) {
		super(tableModel);
	}

	@Override
	public void drawCell(GC gc, Rectangle rect, int col, int row, Object object, boolean focus, boolean header,
			boolean clicked, KTableModel model) {
		gc.setForeground(ColorProvider.BLACK);
		if (object instanceof IDipTableElement) {
			IDipTableElement tableElement = (IDipTableElement) object;
			GCUtils.fillBackground(gc, rect, tableElement);	
			IDipDocumentElement req = tableElement.dipDocElement();		
			// смещение при скролинге
			Rectangle indentRect = new Rectangle(rect.x, rect.y, rect.width, rect.height);		
			if (row == fModel.firstCell()) {
				indentRect.y -= fModel.indent();
			}		
			int width = fModel.commentWidth();
			fModel.commentPainter().paint(width, gc, indentRect, tableElement);			
			drawBorders(col, row, fModel, gc, rect, req, tableElement);
		}
	}

}
