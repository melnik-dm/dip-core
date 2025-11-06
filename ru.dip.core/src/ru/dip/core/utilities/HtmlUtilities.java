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
package ru.dip.core.utilities;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.swt.graphics.Point;

import ru.dip.core.utilities.xml.XmlStringUtilities;

public class HtmlUtilities {
		
	/**
	 * Устанвавливает ширину
	 * Если в первой строке тег table -> устанавливает в table
	 * В противном случае ищет тег body
	 */
	public static String setHtmlWidth(String oldText, int width) {
		String[] lines = oldText.split("\n");			
		if (lines.length > 0 && lines[0].trim().startsWith("<table")) {
			return setHtmlTableWidth(lines, width);
		} else {
			return setBodyWidth(oldText, width);
		}			
	}
	
	/**
	 * Устанавливает ширину таблицы
	 * тег table - должен быть в первой строке
	 */
	public static String setHtmlTableWidth(String oldText, int width) {
		String[] lines = oldText.split("\n");			
		if (lines.length > 0) {
			return setHtmlTableWidth(lines, width);
		}
		return null;
	}
	
	private static String setHtmlTableWidth(String[] lines, int width) {
		String firstLine = lines[0];
		int tagLength = getTableTagLength(firstLine);
		String tag = firstLine.substring(0, tagLength);
		if ("<table>".equals(firstLine.trim())) {
			tag = "<table>";
			tagLength = tag.length();
		}
		
		String newTag = deleteStyleWidth(tag);
		newTag = XmlStringUtilities.changeValueAttribut("width", String.valueOf(width), newTag);
		newTag = XmlStringUtilities. changeValueAttribut("border", "1", newTag);
		firstLine = newTag + firstLine.substring(tagLength);
		lines[0] = firstLine;
		
		StringBuilder builder = new StringBuilder();
		for (String str : lines) {
			builder.append(str);
			builder.append(TagStringUtilities.lineSeparator());
		}
		return builder.toString();
	}
	
	public static String setHtmlTableBorder(String content) {
		if (!content.trim().startsWith("<table")) {
			return content;
		}
		
		String[] lines = content.split("\n");					
		String firstLine = lines[0];
		int tagLength = getTableTagLength(firstLine);
		String tag = firstLine.substring(0, tagLength); // если нет
		if ("<table>".equals(firstLine.trim())) {
			firstLine = "<table border = \"1\"";
		} else {
			String newTag = deleteStyleWidth(tag);
			if (XmlStringUtilities.getValueAttribut("border", newTag) == null) {		
				newTag = XmlStringUtilities. changeValueAttribut("border", "1", newTag);
			} 
			firstLine = newTag + firstLine.substring(tagLength);			
		}
		
		lines[0] = firstLine;
		return String.join(TagStringUtilities.lineSeparator(), lines);
	}
		
	private static int getTableTagLength(String line) {
		String regex = "<table.+?>";
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(line);
		if (matcher.find()) {
			return matcher.end();
		}
		return 0;
	}
	
	private static String deleteStyleWidth(String tag) {
		String style = XmlStringUtilities.getValueAttribut("style", tag);
		if (style == null){
			return tag;
		}		
		String newStyle = style.replaceFirst("width: [0-9]*\\.?[0-9]*%;", "").trim();
		newStyle = newStyle.replaceFirst("width: [0-9]*\\.?[0-9]*px;", "").trim();
		return XmlStringUtilities.changeValueAttribut("style", newStyle, tag);
	}
	
	/**
	 * Устанавливает ширину в тег body
	 */
	public static String setBodyWidth(String oldText, int width) {
		Point point = getBodyTag(oldText);
		if (point == null) {
			return oldText;
		}
		String tagContent = oldText.substring(point.x, point.y);
		String styleValue = XmlStringUtilities.getValueAttribut("style", tagContent);	
		String newAtrValue = "width:" + width;
		String newTagContent = null;	
		if (styleValue == null) {
			newTagContent = XmlStringUtilities.changeValueAttribut("style", newAtrValue + ";", tagContent);
		} else {
			String newStyleValue = null;
			String regex = "width\\s*:[^;]*";		
			Pattern patern = Pattern.compile(regex);
			Matcher matcher = patern.matcher(styleValue);
			if (matcher.find()) {
				newStyleValue = matcher.replaceFirst(newAtrValue).toString();
			} else {
				newStyleValue = newAtrValue + ";" + styleValue; 
			}			
			newTagContent = XmlStringUtilities.changeValueAttribut("style", newStyleValue, tagContent);
		}
		String result = oldText.replace(tagContent, newTagContent);
		return result;
	}
	
	private static Point getBodyTag(String text) {
		String regex = "<body.*>";
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(text);
		if (matcher.find()) {
			return new Point(matcher.start(), matcher.end());
		} else {
			return null;
		}
	}

}
