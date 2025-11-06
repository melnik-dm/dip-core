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
package ru.dip.core.utilities.md.parser.latex;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.swt.graphics.Point;

import ru.dip.core.utilities.md.parser.IMdText;
import ru.dip.core.utilities.md.parser.MdText;

/**
 * Содержит карту - ключ в виде $$LATEX_NLATEX_END$$ и сам latex
 * В markdown вставляются ключи вместо latex, потом при экспорте извлекается latex и карты
 *
 */
public class LatexModel {
	
	private static final String LATEXT_PREFIX = "$$LATEX_";
	private static final String LATEXT_SUFIFIX = "LATEX_END$$";

	private static final String REGEX_FOR_LATEX_KEYS = "\\$\\$LATEX_\\d+LATEX_END\\$\\$";
	private static final Pattern PATTERN_FOR_LATEX_KEYS = Pattern.compile(REGEX_FOR_LATEX_KEYS);
	
	private Map<String, String> fLatexContent = new HashMap<>();
	private int latexCount = 0;

	/**
	 * Ищет latex в исходном документе, заменяет на ключи
	 */
	public String parseOriginalMardown(String content) {
		List<Point> latexPoints = findLatexInMarkdown(content);
		if (latexPoints.isEmpty()) {
			return content;
		}

		StringBuilder newContent = new StringBuilder();
		latexCount = 0;
		int searchStart = 0;
		int end;
		for (Point p : latexPoints) {
			int start = p.x;
			end = p.y;
			if (start > searchStart) {
				String simpleText = content.substring(searchStart, start);
				newContent.append(simpleText);
			}
			String latexText = content.substring(start, end);
			latexText = latexText.replaceAll("&lt;", "<");
			latexText = latexText.replaceAll("&gt;", ">");
			latexText = latexText.replace("&#91;", "[");
			latexText = latexText.replace("&#93;", "]");
			
			String latexPrefix = LATEXT_PREFIX + (latexCount++);
			newContent.append(latexPrefix + LATEXT_SUFIFIX);
			fLatexContent.put(latexPrefix, latexText);
			searchStart = end;
		}

		if (searchStart <= content.length() - 1) {
			String simpleText = content.substring(searchStart, content.length());
			newContent.append(simpleText);
		}
		return newContent.toString();
	}
	
	/**
	 * Возвращает список элементов: простой текст и латех
	 */
	public List<IMdText> parseContentWithLatexKeys(String text) {
		List<IMdText> result = new ArrayList<>();
		Matcher matcher = PATTERN_FOR_LATEX_KEYS.matcher(text);
		int searchStart = 0;
		while (matcher.find(searchStart)) {
			 int start = matcher.start();
			 int end = matcher.end();
			 String simpleText = text.substring(searchStart, start);
			 result.add(new MdText(simpleText));
			 
			 String latexKey = matcher.group().substring(0,  matcher.group().length() - LATEXT_SUFIFIX.length());		 
			 String latexText = fLatexContent.get(latexKey);
			 
			 if (latexText == null) {
				 throw new RuntimeException();
			 }
			 
			 result.add(new LatexText(latexText));			 
			 searchStart = end;
		}
		
		if (searchStart == 0) {
			result.add(new MdText(text));			
		} else if (searchStart <= text.length() - 1) {
			 String simpleText = text.substring(searchStart, text.length());
				result.add(new MdText(simpleText));			
		}	
		return result;
	}
	
	/**
	 * Поиск Latex в Markdown
	 * Возвращает список позиций (start, end)
	 */
	public static List<Point> findLatexInMarkdown(String content) {
		List<Point> result = new ArrayList<>();
		char[] chars = content.toCharArray();
		int startIndex = 0;
		int start = 0;
		while ((start = content.indexOf("~f[", startIndex)) >= 0) {
			
			int balance = 0;
			int end = start + 3;
			for (int i = start + 3; i <= content.length() - 1; i++) {
				char ch = chars[i];
				if (ch == '[') {
					balance++;
				}
				if (ch == ']') {
					if (balance > 0) {
						balance--;
					} else {
						end = i;
						result.add(new Point(start, end + 1));
						break;
					}
				}
			}
			startIndex = end + 1;
			if (startIndex > content.length() - 1) {
				break;
			}
		}
		return result;
	}

}
