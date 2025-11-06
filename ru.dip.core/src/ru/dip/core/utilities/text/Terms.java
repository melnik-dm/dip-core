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
package ru.dip.core.utilities.text;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.eclipse.swt.graphics.Point;

import ru.dip.core.link.LinkInteractor;
import ru.dip.core.utilities.PointUtils;
import ru.dip.core.utilities.TagStringUtilities;
import ru.dip.core.utilities.md.parser.latex.LatexModel;

public class Terms {
	
	// для поиска аббревиатур
	private static final String FIND_TERMS_REGEX = "\\b[А-ЯЁA-Z]{2,}\\b";
	private static final Pattern FIND_TERMS_PATTERN = Pattern.compile(FIND_TERMS_REGEX, Pattern.UNICODE_CHARACTER_CLASS);

	// для поиска переменных
	private static final String FIND_VARIABLE_REGEX = "((?<=_)\\$\\w+(?=_))|((?<!\\w)\\$\\w+(?!\\w))";
	private static final Pattern FIND_VARIABLES_PATTERN = Pattern.compile(FIND_VARIABLE_REGEX, Pattern.UNICODE_CHARACTER_CLASS);

	/**
	 * Создает регулярное выражение для поиска термина по тексту, с учетом окончаний
	 */
	public static String createRegexForGlossEntry(String word) {
		TermRegexBuilder builder = new TermRegexBuilder(word);
		builder.createRegex();
		return builder.getRegex();
	}
	
	public static String createRegexForWord(String word) {
		TermRegexBuilder builder = new TermRegexBuilder(word);
		builder.createWordRegex();
		return builder.getRegex();
	}

	/**
	 * Поиск аббревиатур
	 */
	public static void findTerms(String content, Set<String> terms) {
		Set<String> result = findWords(content, FIND_TERMS_PATTERN);
		terms.addAll(result);
	}
	
	public static String createVarRegex(String varName) {
		return "((?<=_)\\$"+ varName + "(?=_))|((?<!\\w)\\$"+ varName + "(?!\\w))";	
	}
	
	/**
	 * Список позиций переменных
	 */
	public static List<Point> findVars(String content){		
		List<Point> result = new ArrayList<>();
		SpecialPositions specialPositions = new SpecialPositions(content);	
		Matcher matcher = FIND_VARIABLES_PATTERN.matcher(content);
		int index = 0;
		while (matcher.find(index)) {		
			specialPositions.init();
			index = matcher.end();		
			if (specialPositions.containsPosition(matcher.start(), matcher.end())) {
				continue;
			}
			result.add(new Point(matcher.start(), matcher.end() - matcher.start()));
		}
		return result;
	}
	
	public static void findVarsNames(String content, Set<String> terms) {
		Set<String> result = findWords(content, FIND_VARIABLES_PATTERN)
				.parallelStream()
				.map(var ->  var.trim().substring(1)).collect(Collectors.toSet());
		terms.addAll(result);
	}
	
	private static Set<String> findWords(String content, Pattern pattern) {
		Set<String> result = new HashSet<>();
		SpecialPositions specialPositions = new SpecialPositions(content);

		Matcher matcher = pattern.matcher(content);
		int index = 0;
		while (matcher.find(index)) {
			specialPositions.init();
			index = matcher.end();	
			if (specialPositions.containsPosition(matcher.start(), matcher.end())) {
				continue;
			}	
			result.add(matcher.group());
		}
		return result;
	}
	
	/**
	 * Содержится ли данное слово внутри ссылки (внутри пути)
	 */
	private static boolean isWordWithinInLink(String content, int startWord, int endWord, List<Point> links) {
		for (Point p: links) {
			if (startWord >= p.x && endWord <= p.y) {
				String link = content.substring(p.x, p.y);
				int endTitle = link.indexOf(']');
				if (startWord > endTitle + p.x) {
					return true;
				} else {
					return false;
				}
			}
		}
		return false;	
	}

	
	/**
	 * Класс для проверки наличия позиции в специальных позициях (комментарии, ссылки, латех)
	 */
	private static class SpecialPositions {
		
		private final String fContent;
		private List<Point> fLinks = null;
		private List<Point> fComments = null;
		private List<Point> fLatexPoints = null;
		private boolean fInit = false;
		
		private SpecialPositions(String content) {
			fContent = content;
		}
		
		/**
		 * Инициализируем при необходимости
		 */
		private void init(){
			if (fInit == false) {
				fLinks = LinkInteractor.findAllLinks(fContent);
				fComments = TagStringUtilities.findAllComment(fContent);
				fLatexPoints = LatexModel.findLatexInMarkdown(fContent);
				fInit = true;
			}
		}
		
		private boolean containsPosition(int start, int end) {
			if (isWordWithinInLink(fContent, start, end, fLinks)) {
				return true;
			}
			if (PointUtils.containsPosition(fComments, start, end)) {
				return true;
			}
			if (PointUtils.containsPosition(fLatexPoints, start, end)) {
				return true;
			}
			return false;
		}
		
	}
	
}
