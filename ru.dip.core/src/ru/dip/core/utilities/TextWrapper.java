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

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import org.eclipse.swt.graphics.Point;

import ru.dip.core.link.LinkInteractor;

public class TextWrapper {
	
	private static class Token {
		
		String fText;
		TokenType fType;
		
		Token(String text, TokenType type){
			fText = text;
			fType = type;
		}
		
		@Override
		public String toString() {
			return "Token: " + fType + " [" + fText + "]";
		}		
	}
	
	private static enum TokenType {
		TEXT, SPACE, LINK
	}
		
	public static String getWrapText(String text, int length){
		if (text == null || text.length() <= length){
			return text;
		}
		String[] lines = text.split(TagStringUtilities.LINE_SPLIT_REGEX, -1);
		List<String> result = new ArrayList<>();
		for (String line:lines){
			String wrapLine = getWrapLine(line, length);
			result.add(wrapLine);			
		}
		return String.join(TagStringUtilities.lineSeparator(), result);		
	}
	
	public static String getIndentWrapText(String text, int length){		
		if (text == null || text.length() <= length){
			return text;
		}
		String[] lines = text.split(TagStringUtilities.LINE_SPLIT_REGEX, -1);
		List<String> result = new ArrayList<>();
		for (String line:lines){
			String indent = TagStringUtilities.getIndent(line);
			String wrapLine = getWrapIndentLine(indent, line, length);
			result.add(wrapLine);			
		}
		return String.join(TagStringUtilities.lineSeparator(), result);		
	}
	
	private static  String getWrapLine(String line, int length){
		return getWrapIndentLine(null, line, length);
	}
		
	private static  String getWrapIndentLine(String indent, String line, int length){
		List<Token> tokens = parseIndentLine(indent, line);
		Deque<Token> dequeTokens = new ArrayDeque<>(tokens);
				
		int currentLineLength = 0;
		StringBuilder builder = new StringBuilder();
		
		// indent
		builder.append(indent);
		currentLineLength = indent.length();
		boolean afterIndent = true;
		
		while (!dequeTokens.isEmpty()) {
			Token token = dequeTokens.pollFirst();
			// after indent
			switch (token.fType) {
			case LINK:{
				if (afterIndent) {
					String link = token.fText;
					builder.append(link);
					currentLineLength += link.length();
				} else {
					String link = token.fText;
					if (currentLineLength + link.length() > length) {
						if (builder.charAt(builder.length() - 1) == ' ') {
							builder.setCharAt(builder.length() - 1, '\n');
						} else {						
							builder.append("\n");
						}
						
						if (indent != null) {
							builder.append(indent);
							currentLineLength = indent.length();
						}
					} 
					builder.append(link);
					currentLineLength += link.length();
				}
				afterIndent = false;
				break;
			}
			case TEXT:{
				if (afterIndent) {
					String link = token.fText;
					builder.append(link);
					currentLineLength += link.length();
				} else {
					String text = token.fText;
					if (currentLineLength + text.length() > length) {
						if (builder.charAt(builder.length() - 1) == ' ') {
							builder.setCharAt(builder.length() - 1, '\n');
						} else {						
							builder.append("\n");
						}		
						if (indent != null) {
							builder.append(indent);
							currentLineLength = indent.length();
						}
					} 
					builder.append(text);
					currentLineLength += text.length();
				}
				afterIndent = false;
				break;
			}
			case SPACE:{
				if (!afterIndent) {
					String space = token.fText;
					builder.append(space);
					currentLineLength += space.length();
				}
				break;
			}						
			}
		}
		return builder.toString();
	}

	//===========================
	// parse line
	
	private static List<Token> parseIndentLine(String indent, String line){
		if (indent.length() > 0) {
			line = line.substring(indent.length());
		}
		return parseLine(line);
	}

	private static List<Token> parseLine(String line){
		List<Token> tokens = new ArrayList<>();
		List<Point> links = LinkInteractor.findAllLinks(line);
		int currentIndex = 0;
		for (Point p: links) {
			if (p.x > currentIndex) {
				String simpleText = line.substring(currentIndex, p.x);
				tokens.addAll(parseTextWithoutLinks(simpleText));
			}
			tokens.add(new Token(line.substring(p.x, p.y), TokenType.LINK));
			currentIndex = p.y;
		}
		if (currentIndex < line.length()) {
			String simpleText = line.substring(currentIndex);
			tokens.addAll(parseTextWithoutLinks(simpleText));
		}
		return tokens;
	}
	
	private static List<Token> parseTextWithoutLinks(String text){
		List<Token> tokens = new ArrayList<>();
		int index = 0;
		while (true) {
			int spaceIndex = text.indexOf(" ", index);
			if (spaceIndex < 0) {
				if (index < text.length()) {
					tokens.add(new Token(text.substring(index), TokenType.TEXT));
				}
				break;
			} else {
				if (spaceIndex > index) {
					tokens.add(new Token(text.substring(index, spaceIndex), TokenType.TEXT));
				}				
				if (!isLastSpace(tokens)){
					tokens.add(new Token(" ", TokenType.SPACE));
				} 
				index = spaceIndex + 1;
			}
		}
		return tokens;		
	}
	
	private static boolean isLastSpace(List<Token> tokens) {
		return !tokens.isEmpty() 
				&& tokens.get(tokens.size() - 1).fType == TokenType.SPACE;
	}
	
}
