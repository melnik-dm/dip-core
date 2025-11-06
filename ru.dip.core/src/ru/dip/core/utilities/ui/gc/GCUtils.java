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
package ru.dip.core.utilities.ui.gc;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.TextLayout;
import org.eclipse.swt.graphics.TextStyle;

import ru.dip.core.model.finder.IFindResult;
import ru.dip.core.utilities.ui.IBackground;
import ru.dip.core.utilities.ui.swt.ColorProvider;

public class GCUtils {
	
	public static final TextStyle FIND_POINTS_STYLE = GCUtils.createStyle(IFindResult.CURRENT_POINT, null);
	public static final TextStyle FIND_CURRENT_POINT_STYLE = GCUtils.createStyle(ColorProvider.SELECT, null);;
	public static final TextStyle ERROR_STYLE = GCUtils.createErrorStyle();
	public static final TextStyle GRAY_TEXT_STYLE = GCUtils.createStyle(null, ColorProvider.GRAY);
	public static final TextStyle BLACK_TEXT_STYLE = GCUtils.createStyle(null, ColorProvider.BLACK);	
	public static final TextStyle FORM_HEADER_STYLE = GCUtils.createStyle(null, ColorProvider.SELECT);	
			
	//========================
	// Fill background
	
	public static void fillBackground(GC gc, Rectangle rect, IBackground tableElement) {
		fillBackground(gc, rect, tableElement.background());
	}
	
	public static void fillBackground(GC gc, Rectangle rect, Color background) {
		if (background != null) {
			fillBackgroundRectangle(gc, rect, background);
		} else {
			gc.fillRectangle(rect);
		}
	}
	
	public static void fillBackgroundRectangle(GC gc, Rectangle rect, Color background) {
		Color currentBackGround = gc.getBackground();
		gc.setBackground(background);
		gc.fillRectangle(rect);
		gc.setBackground(currentBackGround);
	}
	
	//=========================
	// draw borders
	
	public static void drawUpBorder(GC gc, Rectangle rect) {	
		gc.drawLine(rect.x, rect.y, rect.x + rect.width, rect.y); 
	}
	
	public static void drawLeftBorder(GC gc, Rectangle rect) {
		gc.drawLine(rect.x, rect.y, rect.x, rect.y + rect.height);  
	}
	
	public static void drawRightBorder(GC gc, Rectangle rect) {
		gc.drawLine(rect.x + rect.width, rect.y, rect.x + rect.width, rect.y + rect.height);  
	}
	
	public static void drawDownBorder(GC gc, Rectangle rect) {
		gc.drawLine(rect.x, rect.y + rect.height, rect.x + rect.width, rect.y + rect.height);
	}
	
	public static void drawFullBorder(GC gc, Rectangle rect) {
		gc.drawLine(rect.x, rect.y, rect.x + rect.width, rect.y);   // up
		gc.drawLine(rect.x, rect.y + rect.height, rect.x + rect.width, rect.y + rect.height); // down
		gc.drawLine(rect.x, rect.y, rect.x, rect.y + rect.height);  // left
		gc.drawLine(rect.x + rect.width, rect.y, rect.x + rect.width, rect.y + rect.height);  // rihgt
	}
	
	//=============================
	// TextLayout
	
	public static TextLayout createTextLayout(String text, int width, GC gc, Font font) {
	    TextLayout layout = new TextLayout(gc.getDevice());
	    layout.setText(text == null ? "" : text);
	    layout.setIndent(0);
	    layout.setFont(font);
	    layout.setWidth(width);
	    return layout;
	}
		
	public static void applyFontStyle(TextLayout layout, Iterable<Point> points, Font font) {
	    TextStyle style = new TextStyle();
	    style.font = font;	    
	    for (Point p: points){
		    layout.setStyle(style, p.x, p.y + p.x - 1);
	    }
	}
	
	public static void applyColorStyles(TextLayout layout, Iterable<Point> points, Color bg, Color fg) {
		applyColorStyles(layout, points, bg, fg, 0);
	}
	
	public static void applyColorStyles(TextLayout layout, Iterable<Point> points, Color bg, Color fg, int delta) {		
		TextStyle style = createStyle(bg, fg); 
	    for (Point p: points){
	    	if (layout.getText().length() == 0 || p.x >= layout.getText().length()) {
	    		return;
	    	}
	    	TextStyle currentStyle = layout.getStyle(p.x);
	    	if (currentStyle != null) {
			    TextStyle withCurrent = createStyle(bg, fg);
	    		Font font = currentStyle.font;
	    		withCurrent.font = font;    		
		    	layout.setStyle(withCurrent, p.x, p.y + p.x - delta);
	    	} else {
		    	layout.setStyle(style, p.x, p.y + p.x - delta);
	    	}
		}
	}
	
	public static void applyErrorStyle(TextLayout layout, Iterable<Point> points) {
	    for (Point p: points){
	    	TextStyle currentStyle = layout.getStyle(p.x);
	    	if (currentStyle != null) {
	    		// на каждую букву создается стиль, не очень хорошо, нужно подумать как оптимизировать
	    		// попробовать поменять на currentСтиль применять error
	    	    TextStyle withCurrent = GCUtils.createErrorStyle();
	    		withCurrent.font = currentStyle.font;
	    		withCurrent.foreground = currentStyle.foreground;
	    		withCurrent.background = currentStyle.background;
	    	    layout.setStyle(withCurrent, p.x, p.x + p.y - 1);
	    	} else {
	    	    layout.setStyle(ERROR_STYLE, p.x, p.x + p.y - 1);
	    	}
		}
	}
	
	public static TextStyle createStyle(Color bg, Color fg) {
		TextStyle style = new TextStyle();
	    if (bg != null) {
	    	style.background = bg;
	    }
	    if (fg != null) {
	    	style.foreground = fg;
	    }
	    return style;
	}
	
	public static TextStyle createErrorStyle() {
	    TextStyle style = new TextStyle();
	    style.underline = true;
	    style.underlineColor = ColorProvider.RED;
	    style.underlineStyle = SWT.UNDERLINE_ERROR;
	    return style;
	}
	
}
