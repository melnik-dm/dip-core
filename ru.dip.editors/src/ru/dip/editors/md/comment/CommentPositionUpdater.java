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
package ru.dip.editors.md.comment;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentEvent;

public class CommentPositionUpdater {
	
	public void formatPartStart(CommentAnnotation annotation, String annotationText, DocumentEvent event) throws BadLocationException {
		int delta = event.fText.length() - event.getLength();  // ести стираем пробелы, то отрицательная
		final int eventStart = event.getOffset();		
		final int oldAnnotationTextStart = annotation.getOffset() - eventStart;
		final char[] annotationChars = annotationText.toCharArray();
		final char[] eventChars = event.getText().toCharArray();		
		// start
		final int firstIndex = findFirstIndex(eventChars, oldAnnotationTextStart + delta -1, annotationChars);
		int offsetDelta = firstIndex - oldAnnotationTextStart;
		annotation.setOffsetDelta(offsetDelta);	
	}
	
	public void formatPartEnd(CommentAnnotation annotation, String annotationText, DocumentEvent event) throws BadLocationException {	
		final int eventStart = event.getOffset();		
		final int oldAnnotationTextEnd = annotation.getEndOffset() - eventStart;
		final int oldLength = annotation.getLength();
		final char[] annotationChars = annotationText.toCharArray();
		final char[] eventChars = event.getText().toCharArray();		
		int delta = event.fText.length() - event.getLength();
		int findEndIndex = oldAnnotationTextEnd + delta + 1; // либо начинать с конца документа				
		if (findEndIndex >= eventChars.length) {
			findEndIndex = eventChars.length - 1;
		}
		final int newEndIndex = findLastIndex(eventChars, findEndIndex, annotationChars);

		annotation.setLengthDelta((newEndIndex + eventStart - annotation.getOffset()) - oldLength);		
	}
	
	public void formatCoverAnnotation(CommentAnnotation annotation, String annotationText, DocumentEvent event) throws BadLocationException {
		int delta = event.fText.length() - event.getLength();  // ести стираем пробелы, то отрицательная
		if (delta > 0) {
			delta = 0;
		}
		final int eventStart = event.getOffset();		
		final int oldAnnotationTextStart = annotation.getOffset() - eventStart;
		final int oldAnnotationTextEnd = annotation.getEndOffset() - eventStart;
		final int oldLength = annotation.getLength();
		final char[] annotationChars = annotationText.toCharArray();
		final char[] eventChars = event.getText().toCharArray();		
		// start
		final int firstIndex = findFirstIndex(eventChars, oldAnnotationTextStart  + delta -1, annotationChars);
		int offsetDelta = firstIndex - oldAnnotationTextStart;
		annotation.setOffsetDelta(offsetDelta);	
		// end
		int findEndIndex = oldAnnotationTextEnd + offsetDelta + 10; // либо начинать с конца документа
		if (findEndIndex >= eventChars.length) {
			findEndIndex = eventChars.length - 1;
		}
		final int newEndIndex = findLastIndex(eventChars, findEndIndex, annotationChars);
		annotation.setLengthDelta((newEndIndex - firstIndex) - oldLength);
	}
	
	/**
	 * Ищет индекс вхождения required (пробелы игнорируются)
	 */
	private int findFirstIndex(char[] text, int startSearchIndex,  char[] required) throws BadLocationException {
		int resultStartPosition = startSearchIndex;		
		// проверяем по первым 3-м символам		
		int annIndex = 0;
		int textIndex = startSearchIndex;
		int nextTextIndex;	
		int requiredLength = countNonSpaceChars(required);
			
		while (true) {
			// skip spaces
			annIndex = skipSpaces(required, annIndex);
			textIndex = skipSpaces(text, textIndex);			
			// если 1-й символ не совпал, начинаем поиск со следующего
			if (text[textIndex] != required[annIndex]) {
				textIndex++;
				continue;
			}			
			resultStartPosition = textIndex;	
			// если в искомом тексте только один символ			
			if (requiredLength == 1) {
				return resultStartPosition; 
			}			
			textIndex++;
			annIndex++;						
			// новая позиция для поиска (если не совпадёт)
			nextTextIndex = textIndex;		
			// skip spaces
			annIndex = skipSpaces(required, annIndex);
			textIndex = skipSpaces(text, textIndex);				
			// если 2-й символ не совпал, начинаем поиск заново с nextTextIndex
			if (text[textIndex] != required[annIndex]) {
				textIndex = nextTextIndex;
				annIndex = 0;
				continue;
			}
			// если в искомом тексте только 2 символа			
			if (requiredLength == 2) {
				return resultStartPosition; 
			}	
			textIndex++;
			annIndex++;			
			// skip spaces
			annIndex = skipSpaces(required, annIndex);
			textIndex = skipSpaces(text, textIndex);				
			// если 3-й символ не совпал
			if (text[textIndex] != required[annIndex]) {
				textIndex = nextTextIndex;
				annIndex = 0;
				continue;
			}	
			break;
		} 
		return resultStartPosition;
	}
	
	private int findLastIndex(char[] text, int startSearchIndex,  char[] required) throws BadLocationException {		
		int resultEndPosition = startSearchIndex;		
		// проверяем по первым 3-м символам		
		int annIndex = required.length - 1;
		int textIndex = startSearchIndex;
		int nextTextIndex;	
		int requiredLength = countNonSpaceChars(required);
		
		while (true) {
			// skip spaces
			annIndex = skipSpacesToBack(required, annIndex);
			textIndex = skipSpacesToBack(text, textIndex);			
			// если 1-й символ не совпал, начинаем поиск со следующего
			if (text[textIndex] != required[annIndex]) {
				textIndex--;
				continue;
			}
			resultEndPosition = textIndex;	
			// если в искомом тексте только один символ			
			if (requiredLength == 1) {
				return resultEndPosition; 
			}			
			textIndex--;
			annIndex--;						
			// новая позиция для поиска (если не совпадёт)
			nextTextIndex = textIndex;		
			// skip spaces
			annIndex = skipSpacesToBack(required, annIndex);
			textIndex = skipSpacesToBack(text, textIndex);				
			// если 2-й символ не совпал, начинаем поиск заново с nextTextIndex
			if (text[textIndex] != required[annIndex]) {
				textIndex = nextTextIndex;
				annIndex = required.length - 1;
				continue;
			}
			// если в искомом тексте только 2 символа			
			if (requiredLength == 2) {
				return resultEndPosition; 
			}	
			textIndex--;
			annIndex--;			
			// skip spaces
			annIndex = skipSpacesToBack(required, annIndex);
			textIndex = skipSpacesToBack(text, textIndex);				
			// если 3-й символ не совпал
			if (text[textIndex] != required[annIndex]) {
				textIndex = nextTextIndex;
				annIndex = required.length - 1;
				continue;
			}	
			break;
		}		
		return resultEndPosition;
	}
	
	
	/*
	 * Количество непробельных символов
	 * Если больше 2-х, то возвращает 3, т.к. больше не требуется
	 */
	private int countNonSpaceChars(char[] chars) {
		int count = 0;
		for (int index = 0; index < chars.length; index++) {
			if (Character.isSpaceChar(chars[index])){
				continue;
			}
			count++;
			if (count > 2) {
				return count;
			}
		}
		return count;
	}
	
	/*
	 * Пропускает пробелы.
	 * Возвращает индекс следующего непробельного символа 
	 */
	private int skipSpaces(char[] chars, int currentPosition) throws BadLocationException {
		int index = currentPosition;
		while (Character.isSpaceChar(chars[index]) || chars[index] == '\n') {
			index++;
			if (index == chars.length) {
				throw new BadLocationException();
			}
		}
		return index;		
	}
	
	private int skipSpacesToBack(char[] chars, int currentPosition) throws BadLocationException {
		int index = currentPosition;
		while (Character.isSpaceChar(chars[index]) || chars[index] == '\n') {			
			index--;
			if (index < 0) {
				throw new BadLocationException();
			}
		}
		return index;		
	}
	
}
