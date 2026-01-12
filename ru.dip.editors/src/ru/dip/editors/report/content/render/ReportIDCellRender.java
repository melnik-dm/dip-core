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
package ru.dip.editors.report.content.render;

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;

import de.kupzog.ktable.KTableModel;
import ru.dip.core.model.interfaces.IDipDocumentElement;
import ru.dip.core.utilities.ui.gc.GCUtils;
import ru.dip.core.utilities.ui.swt.ColorProvider;
import ru.dip.editors.report.content.ReportContentModel;
import ru.dip.editors.report.content.model.RceEntryElement;
import ru.dip.ui.table.ktable.model.IContentContainer;
import ru.dip.ui.table.ktable.model.IDipTableElement;
import ru.dip.ui.table.table.TableSettings;

public class ReportIDCellRender extends ReportDipRender {

	public ReportIDCellRender(ReportContentModel tableModel) {
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
		if (tableElement instanceof RceEntryElement) {
			GCUtils.fillBackground(gc, rect, TableSettings.tableColor2());
		} else {
			GCUtils.fillBackground(gc, rect, TableSettings.tableColor1());
			Rectangle indentRect = new Rectangle(rect.x, rect.y, rect.width, rect.height);				
			indentRect.x += TableSettings.marginLeft();					
			// смещение при скролинге если ячейку наверху
			if (row == fModel.firstCell()) {
				indentRect.y -= fModel.indent();
			}
			
			int width = fModel.idWidth();			
			fModel.idPainter().paint(width, gc, indentRect, (IContentContainer) tableElement);	
		}
						
		IDipDocumentElement req = tableElement.dipDocElement();
		drawBorders(col, row, fModel, gc, rect, req, tableElement);
	}

	@Override
	public int getOptimalWidth(GC gc, int col, int row, Object content, boolean fixed, KTableModel model) {
		return 0;
	}
	
}
