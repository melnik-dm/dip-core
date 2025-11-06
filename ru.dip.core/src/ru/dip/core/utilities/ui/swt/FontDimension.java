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

import java.io.File;
import java.util.ArrayList;
import java.util.stream.Stream;

import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.TextLayout;
import org.eclipse.swt.widgets.Display;

import ru.dip.core.utilities.TagStringUtilities;

public class FontDimension {
	
	public static final int MIN_FONT_SIZE = 5;
	public static final int MAX_FONT_SIZE = 27;
	
	private static String testText = "МШrks Двтыд mdhtj Wwryf 1234 Gjytr";

	private double fCharWidth;
	private int fCharHeight;
	
	public static FontDimension getDefaultCharWidth(){
		TextLayout layout = new TextLayout(Display.getDefault());

		layout.setText(testText);
		layout.setFont(FontManager.mono_font);
		Rectangle rectangle = layout.getBounds();
		double charWidth = (double)rectangle.width / testText.length();
		layout.dispose();
		return new FontDimension(charWidth, rectangle.height/*heights[0]/*, heights*/);
	}
	
	public static FontDimension createFontSize(Font font){
		TextLayout layout = new TextLayout(Display.getDefault());

		layout.setText(testText);
		layout.setFont(font);
		Rectangle rectangle = layout.getBounds();
		double charWidth = (double)rectangle.width / testText.length();
		layout.dispose();
		return new FontDimension(charWidth, rectangle.height/*/heights[0]/*, heights*/);
	}
	
	public FontDimension(double charWidth, int charHeight/*, int[] heights*/){
		fCharWidth = charWidth;
		fCharHeight = charHeight;
	}
	
	public double getWidth(){
		return fCharWidth;
	}
	
	public int charHeight(){
		return fCharHeight;
	}
	
	public int getLines(String line, int strLength){
		int lineLength = line.length();
		double k = (double)lineLength / strLength;
		int lines = (int) Math.ceil(k);
		return lines;
	}
	
	public int getStringLength(int width){		
		double d = (double)width/fCharWidth;
		return (int) d;
	}
	
	/**
	 * Вписать текст
	 */
	public static String getWrapText(String text, int length){
		if (text == null || text.length() <= length || length <= 0){
			return text;
		}
		String[] lines = text.split(TagStringUtilities.LINE_SPLIT_REGEX, -1);
		ArrayList<String> result = new ArrayList<>();
		for (String line:lines){
			String wrapLine = getWrapLine(line, length);
			result.add(wrapLine);
		}
		return String.join(TagStringUtilities.lineSeparator(), result);		
	}
	
	public static  String getWrapLine(String line, int length){
		if (line.length() <= length){
			return line;
		}
		int curentPosition = length;
		int startLine = 0;
		StringBuilder builder = new StringBuilder(line);
		int lineLength = line.length();
		while (curentPosition < lineLength){
			char ch = builder.charAt(curentPosition);
			if (Character.isSpaceChar(ch)){
				builder.setCharAt(curentPosition, '\n');
				startLine =  curentPosition + 1;
				curentPosition += length;
			} else {
				curentPosition--;	
				if (curentPosition == startLine){
					builder.insert(curentPosition + length, '\n');
					startLine = curentPosition + length + 1;
					curentPosition = startLine + length;
					lineLength++;
				}
			}		
		}	
		return builder.toString();
	}
	
	public int getTextWidth(String text) {
		int maxLength = getMaxLineLength(text);
		return (int) (maxLength * fCharWidth);
	}
	
	private int getMaxLineLength(String text) {
		return Stream.of(text.split(TagStringUtilities.LINE_SPLIT_REGEX, -1))
				.mapToInt(String::length).max()
				.orElse(0);
	}
	
	public int getTextHeight(String text) {
		int stringCounter = text.split(TagStringUtilities.LINE_SPLIT_REGEX).length;
		return charHeight() * stringCounter;
	}
	
	//===================
	// перенос id

	public static String getIDWrapText(String text, int length){
		if (text.length() <= length){
			return text;
		}
		if (length <= 0){
			return text;
		}
		return getWrapIDLine(text, length - 1);		
	}
	
	private static String getWrapIDLine(String line, int length){
		if (line.length() <= length){
			return line;
		}
		int curentPosition = length;
		int startLine = 0;
		int lineLength = line.length();

		StringBuilder builder = new StringBuilder(line);
		while (curentPosition < lineLength){		
			if (curentPosition < 0){
				break;
			}	
			char ch = builder.charAt(curentPosition);			
			if (File.separatorChar == (ch)){
				curentPosition++;
				builder.insert(curentPosition, '\n');
				lineLength++;
				startLine =  curentPosition + 1;
				curentPosition += length;
			} else {
				curentPosition--;	
				if (curentPosition == startLine){
					builder.insert(curentPosition + length, '\n');
					startLine = curentPosition + length + 1;
					curentPosition = startLine + length;
					lineLength++;
				}
			}		
		}	
		return builder.toString();
	}

	public int getHeight(int lines){
		return fCharHeight * lines;
	}

}
