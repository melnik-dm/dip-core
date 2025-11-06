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
import ru.dip.ui.table.ktable.model.IContentContainer;
import ru.dip.ui.table.ktable.model.IDipTableElement;
import ru.dip.ui.table.ktable.model.TableNode;
import ru.dip.ui.table.table.TableSettings;

public class IDCellRender extends DipRender {

	public IDCellRender(DipTableModel tableModel) {
		super(tableModel);
	}
	
	@Override
	public void drawCell(GC gc, Rectangle rect, int col, int row, Object object, boolean arg5, boolean arg6,
			boolean arg7, KTableModel model) {
		gc.setForeground(ColorProvider.BLACK);
		if (object instanceof IDipTableElement) {
			drawIdTableElement(gc, rect, (IDipTableElement) object, col, row);
		}
	}
	
	private void drawIdTableElement(GC gc, Rectangle rect, IDipTableElement tableElement, int col, int row) {
		GCUtils.fillBackground(gc, rect, tableElement.idBackground());		
		Rectangle indentRect = new Rectangle(rect.x, rect.y, rect.width, rect.height);				
		if (!fModel.tableComposite().isOneListMode() && col == 0) {
			int indent = fModel.getIndent(tableElement) * 16;
			// отрисовка иконки для фолдинга
			if (hasFolding(tableElement)) {
				drawExpandNodeImage(gc, rect, (TableNode) tableElement, indent);
			}
			indentRect.x += indent + 16;
		} else {
			indentRect.x += TableSettings.marginLeft();
		}			
		// смещение при скролинге если ячейку наверху
		if (row == fModel.firstCell()) {
			indentRect.y -= fModel.indent();
		}
		
		int width = fModel.idWidth();
		
		fModel.idPainter().paint(width, gc, indentRect, (IContentContainer) tableElement);
		IDipDocumentElement req = tableElement.dipDocElement();
		drawBorders(col, row, fModel, gc, rect, req, tableElement);
	}
	
	private boolean hasFolding(IDipTableElement tableElement) {
		return tableElement instanceof TableNode
			&& !fModel.isParent(tableElement.dipDocElement());
	}
	
}
