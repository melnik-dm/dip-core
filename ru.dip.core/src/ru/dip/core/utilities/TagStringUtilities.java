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

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.eclipse.swt.graphics.Point;

public class TagStringUtilities {

	public static final String EMPTY_STRING = "";
	public static final String LINESEPARATOR = "\n";
	public static final String PATH_SEPARATOR = "/";
	public static final String PATH_SEPARATOR_REGEX = "/|\\\\";

	public static String lineSeparator() {
		return LINESEPARATOR;
	}
	
	public static final String LINE_SPLIT_REGEX = "\r\n|\n";
	
	public static String getNameFromStartTag(String tagContent) {
		Pattern pattern = Pattern.compile("[<][\\S]+[(\\s)|(>)]");
		Matcher matcher = pattern.matcher(tagContent);
		if (matcher.lookingAt()) {
			String result = tagContent.substring(matcher.start() + 1, matcher.end() - 1);
			// для простго тега
			if (result.endsWith("/")) {
				return result.substring(0, result.length() - 1);
			}
			return result;
		}
		return null;
	}

	public static String getNameFromEndTag(String endTagContent) {
		Pattern pattern = Pattern.compile("[<][/][\\S]+[>]");
		Matcher matcher = pattern.matcher(endTagContent);
		if (matcher.lookingAt()) {
			return endTagContent.substring(matcher.start() + 2, matcher.end() - 1);
		}
		return null;
	}

	public static String getNameAttribute(String tagContent) {
		String regexForPatternAttributContent = "(\\s(\\S+)\\s*=\\s*\"[^\"]*\")|(\\s(\\S+)\\s*=\\s*\'[^\']*\')";
		Pattern pattern = Pattern.compile(regexForPatternAttributContent);
		Matcher matcher = pattern.matcher(tagContent);
		while (matcher.find()) { // находим пару аттрибут - значение
			String attributeName = null;
			String attributeValue = null;
			// получаем имя аттрибута
			String attributContent = matcher.group();
			String forPatternAttributName = "(\\s(\\S+)\\s*=\\s*\")";
			Pattern patternNameAttribut = Pattern.compile(forPatternAttributName);
			Matcher matcherNameAttribut = patternNameAttribut.matcher(attributContent);
			if (matcherNameAttribut.find()) {
				attributeName = matcherNameAttribut.group().substring(0, matcherNameAttribut.group().length() - 1).trim();
				attributeName = attributeName.trim();
				attributeName = attributeName.substring(0, attributeName.length()-1).trim();
			}
			if (!"name".equals(attributeName)){
				continue;
			}			
			// получаем значение атрибута
			String forPatternAttributValue = "(=\\s*\"[^\"]*\")|(=\\s*\'[^\']*\')";
			Pattern patternValueAttribut = Pattern.compile(forPatternAttributValue);
			Matcher matcherValueAttribut = patternValueAttribut.matcher(attributContent);
			if (matcherValueAttribut.find()) {
				attributeValue = (matcherValueAttribut.group().substring(1, matcherValueAttribut.group().length() - 1).trim())
						.substring(1);
			}
			return attributeValue;
		}
		return null;
	}
	
	public static List<Point> findText(String content, String text, boolean caseSensitive, boolean word){
		if (word) {
			return findWords(content, text, caseSensitive);
		} else {
			return findText(content, text, caseSensitive);
		}		
	}

	public static List<Point> findText(String content, String text, boolean caseSensitive){
		if (content == null || content.isEmpty()) {
			return null;
		}	
		if (!caseSensitive) {
			content = content.toLowerCase();
			text = text.toLowerCase();
		}
		
		List<Point> result = new ArrayList<>();			
		int index = 0;
		int x = -1;			
		while ((x = content.indexOf(text, index)) >= 0) {
			Point point = new Point(x, + x + text.length());
			result.add(point);
			index = x + text.length();
		}
		if (result.isEmpty()) {
			return null;
		}
		return result;
	}
	
	/**
	 * Поиск слова по тексту, если не нашел возвращает null
	 */
	public static List<Point> findWords(String content, String word, boolean caseSensitive){
		if (content == null || content.isEmpty()) {
			return null;
		}	
		if (!caseSensitive) {
			content = content.toLowerCase();
			word = word.toLowerCase();
		}
		
		String pattern = "\\b" + word + "\\b";
		List<Point> positions = new ArrayList<>();
		Pattern p = Pattern.compile(pattern);
		Matcher m = p.matcher(content);
		while (m.find()) {
		   positions.add(new Point(m.start(), m.start() + word.length()));
		}		
		if (positions.isEmpty()) {
			return null;
		}		
		return positions;
	}
	
	public static boolean containsWord(String content, String word, boolean caseSensitive) {
		if (content == null || content.isEmpty()) {
			return false;
		}	
		if (!caseSensitive) {
			content = content.toLowerCase();
			word = word.toLowerCase();
		}
		
		String pattern = "\\b" + word + "\\b";
		Pattern p = Pattern.compile(pattern); 
		Matcher m = p.matcher(content);
		return m.find();
	}
	
	/**
	 * Удаляет из коллекции texts элементы, если они содержатся в content
	 */
	public static void removeIfContains(String content, Collection<String> texts){
		if (content == null || content.isEmpty()) {
			return;
		}	
		texts.removeAll(contains(content, texts));
	}
	
	private static Collection<String> contains(String content, Collection<String> texts) {
		return texts.parallelStream()
		.filter(text -> contains(content, text))
		.collect(Collectors.toList());				
	}
	
	/**
	 * Содержит ли регулярное выражение
	 */
	public static boolean contains(String content, String regex) {
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(content);		
		return matcher.find();
	}
	

	
	public static boolean containsToIgnoreCase(String content, String text, boolean word) {
		if (word) {
			return containsWord(content, text, false);
		} else {
			return containsToIgnoreCase(content, text);
		}
	}
	
	public static boolean containsToIgnoreCase(String content, String text) {
		return content.toLowerCase().contains(text.toLowerCase());
	}

	public static List<String> removeEmptyStirng(String content) {
		String[] lines = content.split("\n");
		ArrayList<String> list = new ArrayList<>();
		for (String line: lines) {
			String l = line.trim();
			if (l.isEmpty()) {
				continue;
			}
			list.add(l);
		}
		return list;
	}
	
	public static int numberLine(String content, int index) {
		int counter = 0;
		char[] chars = content.toCharArray();
		for (int i = 0; i < index && i < content.length(); i++) {
			if (chars[i] == '\n') {
				counter++;
			}
		}
		return counter;
	}
	
	/**
	 * Возвращает отступ
	 */
	public static String getIndent(String str) {
		String mainStr = str.trim();
		int index = str.indexOf(mainStr);
		return str.substring(0,index);
	}
	
	/**
	 * Возвращает количество пробельных символов и переходов строк в начале
	 */
	public static int startSpacesChars(char[] chars) {
		int result = 0;
		for (int i = 0; i < chars.length; i++) {
			if (chars[i] == '\n' || Character.isSpaceChar(chars[i])) {
				result++;
			} else {
				break;
			}
		}
		return result;
	}
	
	/**
	 * Возвращает количество пробельных символов игнорируя переходы строк
	 */
	public static int startSpaces(char[] chars) {
		int result = 0;
		for (int i = 0; i < chars.length; i++) {
			if (Character.isSpaceChar(chars[i])) {
				result++;
			} else if (chars[i] == '\n'){
				;
			} else {
				break;
			}
		}
		return result;
	}
	
	/**
	 * Возвращает количество пробельных символов в конце
	 */
	public static int endSpaces(char[] chars) {
		int result = 0;
		for (int i = chars.length - 1; i >= 0; i--) {
			if (Character.isSpaceChar(chars[i]) || chars[i] == '\n') {
				result++;
			} else {
				break;
			}
		}
		return result;
	}
	
	/**
	 * Состоит ли текст только из русских букв
	 */
	public static boolean isRussianLetters(String text) {
		for (char ch: text.toLowerCase().toCharArray()) {
			if (ch < 'а' || ch > 'я') {
				return false;
			}
		}
		return true;		
	}
	
	
	/**
	 * Может ли быть расширением, только латинские буквы и цифры
	 * Макс длина 15 символов
	 */
	public static boolean isExtension(String extension) {
		if (extension.length() > 15) {
			return false;
		}
		
		String regex = "^[A-Za-z0-9]+$";
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(extension);
		return matcher.find();
	}
	
	/**
	 * Переносит полный путь до файла по частям
	 */
	public static String wrapPath(String pathStr, int maxLength) {
		Path path = Paths.get(pathStr);
		return wrapPath(path, maxLength);
	}
		
	public static String wrapPath(Path path, int maxLength) {
		Iterator<Path> iterator = path.iterator();
		StringBuilder builder = new StringBuilder();
		boolean needSeparator = path.isAbsolute();		
		
		String separator = File.separator;
		int currentLineLength = 0;
		
		while(iterator.hasNext()) {
			Path next = iterator.next();
			if (needSeparator) {
				builder.append(separator);
				currentLineLength += separator.length();
			} else {
				needSeparator = true;
			}
		
			String nextName = next.toString();		
			if (currentLineLength + nextName.length() >= maxLength) {
				builder.append("\n");
				currentLineLength = 0;
			} 
			builder.append(nextName);
			currentLineLength += nextName.length();
		}
		return builder.toString();
	}
	
	
	/**
	 * Преобразует путь в симтеме Windows в Unix
	 */
	public static String toUnixPath(String original) {
		return original.replaceAll("\\\\", "/");
	}
	
	
	/**
	 *  trim - с учетом неразрывных пробелов
	 */
	private static char unbreakable_space = 160;
	private static String UNBREAKBLE_SPACE_STR = String.valueOf(unbreakable_space);
	
	public static String trim(String str) {
		String result = str.trim();
		if (!result.isEmpty() && 
				(result.charAt(0) == unbreakable_space || result.charAt(result.length() - 1) == unbreakable_space)) {
			result = result.replaceAll(UNBREAKBLE_SPACE_STR, " ");
			result = result.trim();
		}
		return result;
	}

	/**
	 * Меняет все неразрывные пробелы на обычные
	 */
	public static String replaceUnbreakableSpaces(String input) {
		return input.replaceAll(String.valueOf(unbreakable_space), " ");
	}
	
	
	/**
	 * удаляет специальные-UNIX символы (заливка и т.п.)
	 */
	public static String deleteSpecialUnixChars(String input) {
		return input.replaceAll("\u001B\\[[;\\d]*m", "");
	}
	
	
	/**
	 * находит в тексте все комментарии типа  <!--  -->
	 */
	private static final String COMMENT_REGEX = "<!--.*?-->";	
	public static final Pattern COMMENT_PATTERN = Pattern.compile(COMMENT_REGEX);
	
	public static List<Point> findAllComment(String content){
		List<Point> comments = new ArrayList<>();	
		Matcher matcher = COMMENT_PATTERN.matcher(content);
		while (matcher.find()) {
			comments.add(new Point(matcher.start(), matcher.end()));
		}
		return comments;	
	}
		
}
