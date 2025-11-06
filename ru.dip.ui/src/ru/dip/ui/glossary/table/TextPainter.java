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
package ru.dip.ui.glossary.table;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.TextLayout;

import de.kupzog.ktable.KTableCellRenderer;
import de.kupzog.ktable.KTableModel;
import ru.dip.core.utilities.ui.gc.GCUtils;
import ru.dip.core.utilities.ui.swt.ColorProvider;
import ru.dip.core.utilities.ui.swt.FontDimension;
import ru.dip.ui.table.table.TableSettings;

public class TextPainter  implements KTableCellRenderer {

	private final  GlossaryTableModel fModel;
	
	public TextPainter(GlossaryTableModel model) {
		fModel = model;
	}

	public void updateGlossaryPresentation(int nameWidth, int valueWidth, GlossaryEntry entry) {
		String name = entry.getName();
		String value = entry.getValue();
				
		String wrapName = FontDimension.getWrapText(name, TableSettings.presentationFontDimension().getStringLength(nameWidth));
		String wrapValue = FontDimension.getWrapText(value, TableSettings.presentationFontDimension().getStringLength(valueWidth));

		entry.setWrapName(wrapName);
		entry.setWrapValue(wrapValue);
		
		int nameHeight = TableSettings.presentationFontDimension().getTextHeight(wrapName)
				+ TableSettings.versionMarginTop()
				+ TableSettings.versionMarginBottom();
				
				
		int valueHeight = TableSettings.presentationFontDimension().getTextHeight(wrapValue)
				+ TableSettings.versionMarginTop()
				+ TableSettings.versionMarginBottom();
		
		entry.setNameHeight(nameHeight);
		entry.setValueHeight(valueHeight);		
	}

	@Override
	public void drawCell(GC gc, Rectangle rect, int col, int row, Object content, boolean focus, boolean header,
			boolean clicked, KTableModel model) {
		if (content instanceof GlossaryEntry) {
			if (row == fModel.getSelectRow()) {
				GCUtils.fillBackground(gc, rect, ColorProvider.SELECT);
			} 
			GlossaryEntry entry = (GlossaryEntry) content;
			
			// если в режиме отображения неиспользуемых терминов
			Color foreground = gc.getForeground();
			if (fModel.isUnused(entry)) {
				gc.setForeground(ColorProvider.RED);
			}
				
			String text = col == 0 ? entry.getName() : entry.getValue();						
			paintTextPresentation(rect.width, gc, rect, text);
			//drawBorders(col, row, gc, rect);
			gc.setForeground(foreground);
		}				
	}
	
	void drawBorders(int col, int row, GC gc, Rectangle rect) {	
		GCUtils.drawFullBorder(gc, rect);
	}
	
	public void paintTextPresentation(int width, GC gc, Rectangle rect, 
			String text) {	
	    final TextLayout layout = createTextLayout(text, width, gc);
	    drawTextAndDispose(layout, rect, gc);
	}
	
	protected void drawTextAndDispose(TextLayout layout, Rectangle rect, GC gc) {
	    layout.draw(gc, rect.x + TableSettings.versionMarginLeft(), 
	    		rect.y + TableSettings.versionMarginTop()); 
	    layout.dispose();
	}
	
	protected TextLayout createTextLayout(String text, int width, GC gc) {
		return GCUtils.createTextLayout(text, width, gc, TableSettings.presentationFont());
	}
	
	@Override
	public int getOptimalWidth(GC gc, int col, int row, Object content, boolean fixed, KTableModel model) {
		return 0;
	}	
	
}
