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
import java.util.List;

public class TermRegexBuilder {
	
	private static enum Type {
		WORD, ABBREVIATION, SPACE;
	}
	
	/**
	 * Совпадают ли слова с учетом склонений
	 * Не учитываются окончания типа ОГО - т.е. тестовый-тестового
	 */
	public static boolean isOneWordEquals(String name, String text) {	
		if (name.equalsIgnoreCase(text)){
			return true;
		}
		
		if (name.length() < 3) {
			return false;
		}
		
		char[] nameChars = name.toLowerCase().toCharArray();
		char[] textChars = text.toLowerCase().toCharArray();
		
		// находим номер символа с которого начинаются расхождения
		int notEqualsCharNumber = 0;
		for (int i = 0; i < textChars.length; i++) {
			if (i >= nameChars.length || nameChars[i] != textChars[i]) {
				notEqualsCharNumber = i;
				break;
			}
		}
		// должно остаться не более 2-х символов в термине и не более 3-х в тексте
		if (notEqualsCharNumber < nameChars.length - 3 
				|| notEqualsCharNumber < textChars.length - 4){ 
			return false;
		}
		// проверяем оставшиеся символы
		for (int i = notEqualsCharNumber; i < nameChars.length; i++) {
			if (!TermRegexBuilder.isVowelLetter(nameChars[i])) {
				return false;
			}
		}
		
		for (int i = notEqualsCharNumber; i < textChars.length; i++) {
			if (!TermRegexBuilder.isVowelLetter(textChars[i])) {
				return false;
			}
		}		
		return true;	
	}
	
	public static boolean isVowelLetter(char ch) {
		char lowCh = Character.toLowerCase(ch);
		return lowCh == 'а' || lowCh == 'е' || lowCh == 'ё'
				|| lowCh == 'и' || lowCh == 'о' || lowCh == 'у'
				|| lowCh == 'ы' || lowCh == 'э' || lowCh == 'ю' 
				|| lowCh == 'я' || lowCh == 'й' || lowCh == 'ь';
	}
	
	private String fOriginal;
	private List<String> fWords = new ArrayList<>();
	private List<Type> fTypes = new ArrayList<>();
	private String fRegex;
	
	public TermRegexBuilder(String original){
		fOriginal = original;
	}
	
	public void createRegex() {
		computeWords();
		fRegex = buildRegex();
	}
	
	public void createWordRegex() {
		computeWords();
		fRegex = buildWord();
	}
	
	private void computeWords() {
		char[] chars = fOriginal.toCharArray();
		boolean currentIsLetter = false;
		StringBuilder currentBuilder = new StringBuilder();
		for (int i = 0; i < chars.length; i++) {
			char  ch = chars[i];
			boolean isLetter = Character.isJavaIdentifierPart(ch) || ch == '-';
			if (i == 0) {
				currentBuilder.append(ch);
				currentIsLetter = isLetter;
			} else if (isLetter == currentIsLetter) {
				currentBuilder.append(ch);								
			} else {
				addWord(currentBuilder.toString(), currentIsLetter);
				currentBuilder = new StringBuilder();
				currentIsLetter = isLetter;
				currentBuilder.append(ch);
			}
		}
		if (currentBuilder.length() > 0) {
			addWord(currentBuilder.toString(), currentIsLetter);
		}	
	}
		
	private void addWord(String word, boolean isWord) {
		if (isWord) {
			addWord(word);
		} else {
			addSpaces(word);
		}
	}
			
	private void addWord(String word) {
		fWords.add(word);
		if (isAbbreviation(word)) {
			fTypes.add(Type.ABBREVIATION);
		} else {
			fTypes.add(Type.WORD);
		}
	}
	
	private boolean isAbbreviation(String word) {
		return word.equals(word.toUpperCase());
	}
	
	private void addSpaces(String word) {
		fWords.add(word);
		fTypes.add(Type.SPACE);
	}
	
	/*
	 * Полное регулярное выражение (самодостаточное для поиска)
	 */
	private String buildRegex() {
		StringBuilder builder = new StringBuilder();
		builder.append("\\b");
		for (int i = 0; i < fWords.size(); i++) {
			String word = fWords.get(i);
			Type type = fTypes.get(i);
			if (type == Type.WORD) {
				builder.append(getWordRegex(word, i == 0));
			} else {
				builder.append(word);
			}
		}
		builder.append("\\b");
		return builder.toString();
	}
	
	/*
	 * Регулярное выражение для одно термина, для вставки в общее регулярное выражение из множества терминов
	 */
	private String buildWord() {
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < fWords.size(); i++) {
			String word = fWords.get(i);
			Type type = fTypes.get(i);
			if (type == Type.WORD) {
				builder.append(getWordRegex(word, i == 0));
			} else {
				builder.append(word);
			}
		}
		return builder.toString();
	}
	
	private String getWordRegex(String word, boolean first) {
		String regex = getWordRegex(word);
		if (first) {
			return firstLetterRegex(regex.charAt(0)) + regex.substring(1);
		} else {
			return regex;
		}
	}
	
	private String getWordRegex(String word) {
		if (word.length() < 3) {
			return word;				
		}		
		char last = word.charAt(word.length() - 1);
		if (isVowelLetter(last)) {
			char preLast = word.charAt(word.length() - 2);
			if (isVowelLetter(preLast)) {
				return word.substring(0, word.length() - 2) + "\\S{0,3}";
			} else {
				return word.substring(0, word.length() - 1) + "\\S{0,3}";
			}																
		} else {
			return word + "\\S{0,2}";
		}
	}
		
	private String firstLetterRegex(char ch) {
		char one = ch;
		char two = ch;
		if (Character.isUpperCase(ch)) {
			two = Character.toLowerCase(ch);
		} else {
			two = Character.toUpperCase(ch);
		}
		return "[" + one + ',' + two + ']';
	}
	
	public String getRegex() {
		return fRegex;
	}
}
