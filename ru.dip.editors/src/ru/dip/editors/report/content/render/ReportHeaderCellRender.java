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

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;

import de.kupzog.ktable.KTableCellRenderer;
import de.kupzog.ktable.KTableModel;
import ru.dip.core.utilities.ui.gc.GCUtils;
import ru.dip.core.utilities.ui.swt.ColorProvider;
import ru.dip.core.utilities.ui.swt.FontDimension;
import ru.dip.core.utilities.ui.swt.FontManager;
import ru.dip.editors.report.content.ReportContentModel;
import ru.dip.ui.Messages;
import ru.dip.ui.table.table.TableSettings;

public class ReportHeaderCellRender  implements KTableCellRenderer   {

	private static final String ID_TITLE = Messages.HeaderCellRender_IdTitle;
	private static final String PRESENRATION_TITLE = Messages.HeaderCellRender_ContentsTitle;
	private static final String COMMENT_TITLE = Messages.HeaderCellRender_CommentsTitle;
	
	private Font fFont =  FontManager.boldFont;
	private FontDimension fDimenstion = FontDimension.createFontSize(fFont);
		
	private ReportContentModel fModel;
	
	public ReportHeaderCellRender(ReportContentModel tableModel) {
		fModel = tableModel;
	}
	
	public void drawCell(GC gc, Rectangle rect, int col, int row, Object content, boolean focus, boolean header,
			boolean clicked, KTableModel model) {
				
		gc.setForeground(ColorProvider.BLACK);
		
		String text = getTitle(col);	
		int length = (int) (text.length() * fDimenstion.getWidth());
		int x = rect.x + (rect.width -length) / 2;
		int y = rect.y + (rect.height - fDimenstion.charHeight())/2;			
		gc.setFont(FontManager.boldFont);
		if (fModel.tableComposite().isCtrlPressed()) {
			Color defaultBackground = gc.getForeground();
			gc.setForeground(ColorProvider.SELECT);
			gc.drawText(text, x, y);
			gc.setForeground(defaultBackground);
		} else {
			gc.drawText(text, x, y);
		}
		drawBorders(col,gc, rect);

	}
	
	private String getTitle(int col) {
		if (col == fModel.idColumnNumber()) {
			return ID_TITLE;
		} else if (col == fModel.presentationColumnNumber()) {
			return  PRESENRATION_TITLE;
		} else if (col == fModel.commentColumnNumber()) {
			return COMMENT_TITLE;
		}
		return ""; //$NON-NLS-1$
	}
	
	private void drawBorders(int col, GC gc, Rectangle rect) {
		Color currentForeground = gc.getForeground();
		gc.setForeground(TableSettings.lineColor());
		GCUtils.drawUpBorder(gc, rect);
		if (col == fModel.getColumnCount() - 1) {
			GCUtils.drawRightBorder(gc, rect);
		}

		if (TableSettings.isShowLine()) {
			GCUtils.drawLeftBorder(gc, rect);
		} else if (col == 0) {
			GCUtils.drawLeftBorder(gc, rect);
		}
		gc.setForeground(currentForeground);		
	}

	@Override
	public int getOptimalWidth(GC gc, int col, int row, Object content, boolean fixed, KTableModel model) {
		return 0;
	}
	
}
