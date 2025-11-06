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
package ru.dip.core.utilities.ui.swt;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;

public class ColorProvider {
	
	private static Display display = Display.getDefault();

	// цвет для выбора (рыжий)
	public static final String SELECT_NAME = "select";
	public static final Color SELECT = display.getSystemColor(SWT.COLOR_LIST_SELECTION);
	public static final Color DEFAULT_COLOR = display.getSystemColor(SWT.COLOR_LIST_BACKGROUND);
	public static final Color USER_SELECT = new Color(display, new RGB(255,238,166));	

	// таблица
	public static final String TABLE_1_COLOR_ID = "ru.dip.ui.color.table1";
	public static final String TABLE_2_COLOR_ID = "ru.dip.ui.color.table2";
	public static final String TABLE_SELECTION_COLOR_ID = "ru.dip.ui.color.table.selection";
		
	// стандартные цвета
	public static final String WHITE_NAME = "white";
	public static final String YELLOW_NAME = "yellow";
	public static final String RED_NAME = "red";
	public static final String GREEN_NAME = "green";
	public static final String BLUE_NAME = "blue";
	public static final String GRAY_NAME = "gray";
	public static final String BLACK_NAME = "black";

	public static final Color WHITE = display.getSystemColor(SWT.COLOR_WHITE);
	public static final Color YELLOW = display.getSystemColor(SWT.COLOR_YELLOW);
	public static final Color RED = display.getSystemColor(SWT.COLOR_RED);
	public static final Color GRAY = display.getSystemColor(SWT.COLOR_GRAY);
	public static final Color DARK_GRAY = display.getSystemColor(SWT.COLOR_DARK_GRAY);
	public static final Color GREEN = display.getSystemColor(SWT.COLOR_GREEN);
	public static final Color BLUE = display.getSystemColor(SWT.COLOR_BLUE);
	public static final Color BLACK = display.getSystemColor(SWT.COLOR_BLACK);
	public static final Color MAGENTA = display.getSystemColor(SWT.COLOR_DARK_MAGENTA);
	public static final Color GREEN_DARK = display.getSystemColor(SWT.COLOR_DARK_GREEN);
	
	
	public static final RGB LIGHT_GREEN_RGB = new RGB(233,254,230);
	public static final Color LIGHT_GREEN = new Color(display, LIGHT_GREEN_RGB);
	
	public static final RGB LIGHT_YELLOW_RGB = new RGB(238,232,170);
	public static final Color LIGHT_YELLOW = new Color(display, LIGHT_YELLOW_RGB);

	public static final RGB LIGHT_BLUE_RGB = new RGB(141,176,210);
	public static final Color LIGHT_BLUE = new Color(display, LIGHT_BLUE_RGB);
	
	public static final RGB WHITE_RGB = new RGB(255, 255, 255);
	
	public static final RGB DOUBLE_CLICK_CHILD_RGB = new RGB(0xC4,0xE8,0xFF);
	public static final Color TABLE_DOUBLE_CLICK_CHILD = new Color(display, DOUBLE_CLICK_CHILD_RGB);
 	
	public static final RGB IMAGE_VIEW_ID_GRAY_RGB = new RGB(136,138,133);
	public static final Color IMAGE_VIEW_ID_GRAY = new Color(display, IMAGE_VIEW_ID_GRAY_RGB);
	
	public static final RGB DIFF_ADDED_RGB = new RGB(149,231,123);
	public static final Color DIFF_ADDED = new Color(display, DIFF_ADDED_RGB);
	
	public static final RGB DIFF_MODIFY_RGB = new RGB(231,231,123);
	public static final Color DIFF_MODIFY = new Color(display, DIFF_MODIFY_RGB);
	
	public static final RGB DIFF_DELETE_RGB = new RGB(231,123,123);
	public static final Color DIFF_DELETE = new Color(display, DIFF_DELETE_RGB);		
	
	public static final Color DARK_YELLOW = display.getSystemColor(SWT.COLOR_DARK_YELLOW);
	
	//===============================
	// filter tools
	
	public static final RGB FILTER_APPLY_RGB = new RGB(149,231,123);
	public static final Color FILTER_APPLY = new Color(display, FILTER_APPLY_RGB);
	
	
	public static Map<String,Color> fColors = new HashMap<>();
	
	static {
		fColors.put(WHITE_NAME, WHITE);
		fColors.put(YELLOW_NAME, YELLOW);
		fColors.put(RED_NAME, RED);
		fColors.put(GRAY_NAME, GRAY);
		fColors.put(GREEN_NAME, GREEN);
		fColors.put(BLUE_NAME, BLUE);
		fColors.put(BLACK_NAME, BLACK);
		
		JFaceResources.getColorRegistry().put(WHITE_NAME, WHITE.getRGB());
		JFaceResources.getColorRegistry().put(YELLOW_NAME, YELLOW.getRGB());
		JFaceResources.getColorRegistry().put(RED_NAME, RED.getRGB());
		JFaceResources.getColorRegistry().put(GRAY_NAME, GRAY.getRGB());
		JFaceResources.getColorRegistry().put(GREEN_NAME, GREEN.getRGB());
		JFaceResources.getColorRegistry().put(BLUE_NAME, BLUE.getRGB());
		JFaceResources.getColorRegistry().put(BLACK_NAME, BLACK.getRGB());
		JFaceResources.getColorRegistry().put(SELECT_NAME, SELECT.getRGB());
	}
	
	public static Color getColor(String name){
		String colorName = name.toLowerCase();
		Color color = fColors.get(colorName);
		if (color == null){
			return getRGBColor(colorName);
		}
		return color;
	}
	
	public static Color getRGBColor(String rgbString){
		RGB rgb = getRGB(rgbString);
		if (rgb == null){
			return null;
		}
		Color color = new Color(display, rgb);
		return color;
		
	}
	
	public static Color getRGBColor(RGB rgb){
		if (rgb == null){
			return null;
		}
		Color color = new Color(display, rgb);
		return color;
		
	}
	
	public static RGB getRGB(String str){
		String[] rgbs = str.split(",");
		if (rgbs.length == 3){
			try {
				int red = Integer.parseInt(rgbs[0]);
				int green = Integer.parseInt(rgbs[1]);
				int blue = Integer.parseInt(rgbs[2]);
				return new RGB(red, green, blue);
			} catch (NumberFormatException e){
				return null;
			}
			
		}
		return null;
	}
	
	public static String RGBtoString(RGB rgb){
		StringBuilder builder = new StringBuilder();
		builder.append(rgb.red);
		builder.append(",");
		builder.append(rgb.green);
		builder.append(",");
		builder.append(rgb.blue);
		return builder.toString();
	}
	
	/**
	 * Возвращает цвет в строком виде #aabbff
	 */
	public static String colorToHtmlString(Color color) {
		return String.format("#%02x%02x%02x;", color.getRed(), color.getGreen(), color.getBlue());
	}
	
	
}
