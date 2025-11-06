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
package ru.dip.core.utilities.md;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ru.dip.core.utilities.TextWrapper;

public class MDWrapper {
	
	public static  String parse(String input, int length) {
		String[] lines = input.split("\n");
		String previous = null;
		StringBuilder builder = new StringBuilder();
		for (String line: lines) {
			if (line.isEmpty()) {
				if (previous == null || previous.isEmpty()) {					
					builder.append("\n");
				} else {
					builder.append("\n\n");
				}
			} else if (isNeedNewLine(line, previous)) { 
				if (previous != null && !previous.isEmpty()) {
					builder.append("\n");
				}
				builder.append(line);			
			} else {
				if (previous != null && !previous.isEmpty()) {
					builder.append(" ");
					builder.append(line);
				} else {
					builder.append(line);
				}
			}
			previous = line;
		}	
		if (input.endsWith("\n")) {
			builder.append("\n");
		}
		return TextWrapper.getIndentWrapText(builder.toString(), length);
	}
	
	
	public static  String parseListItem(String input, int length) {
		String[] lines = input.split("\n");
		String previous = null;
		StringBuilder builder = new StringBuilder();
		for (String line: lines) {
			if (line.trim().isEmpty()) {
				builder.append("\n");
			} else if (isNeedNewLine(line, previous)) { 
				if (previous != null && !previous.isEmpty()) {
					builder.append("\n");
				}
				builder.append(line);			
			} else {
				if (previous != null && !previous.isEmpty()) {
					builder.append(" ");
					builder.append(line.trim());
				} else {
					builder.append(line);
				}
			}
			previous = line;
		}	
		if (input.endsWith("\n")) {
			builder.append("\n");
		}
		return TextWrapper.getIndentWrapText(builder.toString(), length);
	}
	
	private static boolean isNeedNewLine(String line, String previous) {
		return isNumberListLine(line) || isMarkerListLine(line) || isCodeLine(line)
				|| (previous != null && isCodeLine(previous));
	}
	
	private static boolean isNumberListLine(String line) {
		String regex = "^\\d+[\\.\\)]{1}\\s.*";
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(line.trim()); 				
		return matcher.find();
	}
		
	private static boolean isMarkerListLine(String line) {
		String regex = "^[\\+,\\-,\\*]{1}\\s.*";
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(line.trim()); 				
		return matcher.find();
	}
	
	private static boolean isCodeLine(String line) {
		return line.trim().startsWith("```");
	}

}
