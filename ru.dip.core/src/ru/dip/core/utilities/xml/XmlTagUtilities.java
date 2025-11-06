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
package ru.dip.core.utilities.xml;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Position;


/**
 * Класс содержит методы для работы с тегами (работает с классами Tag, Argumnet, IDocument)
 */
public class XmlTagUtilities {
	
	/**
	 *  метод комментирует позицию в документе
	 */
	public static void commentPosition(Position pos, IDocument document){
		try {
			String content = document.get(pos.getOffset(),pos.getLength());					
			document.replace(pos.getOffset(), pos.getLength(), "<!--"+content+"-->");
		} catch (BadLocationException e) {
			e.printStackTrace();
			return;
		}
	}	

	/**
	 *  метод раскомментирует позицию в документе
	 */
	public static void uncommentPosition(Position pos, IDocument document){
		try {
			String content = document.get(pos.getOffset(),pos.getLength());	
			Position cleanPosition = XmlStringUtilities.getCleanPosition(pos, content);
			String newContent = document.get(cleanPosition.getOffset(),cleanPosition.getLength());
			document.replace(pos.getOffset(), pos.getLength(), newContent);
		} catch (BadLocationException e) {
			e.printStackTrace();
			return;
		}
	}	
	
	/**
	 * втавляет значение тега добавляет аттрибут в указанное место
	 */
	public static String changeValueAttribut(String nameAttr, String valueAttr, String content, int number) {
		String result = "";
		String regexForPatternAttributContent = "(\\s(" + nameAttr + ")\\s*=\\s*\"[^\"]*\")|(\\s(" + nameAttr
				+ ")\\s*=\\s*\'[^\']*\')";
		if (valueAttr.length() == 0)   // если значение аттрибута равно нулю
			result = content.replaceAll(regexForPatternAttributContent, "");
		else {
			String newAttr = " " + nameAttr + "=" + "\"" + valueAttr + "\"";
			Pattern pattern = Pattern.compile(regexForPatternAttributContent);
			Matcher matcher = pattern.matcher(content);
			if (matcher.find()) {  // если есть такой атрибут
				result = content.replaceAll(regexForPatternAttributContent, newAttr);
			} else {
				String name = XmlStringUtilities.getName(content, false);

				if (number == 0) {   // если аттрибут первый номер
					name = XmlStringUtilities.getName(content, false);
					regexForPatternAttributContent = "<" + name;
					result = content.replaceAll(regexForPatternAttributContent,
							regexForPatternAttributContent + newAttr);
				} else {
					if ((getAttributs(content).size()) < number)
						return XmlStringUtilities.changeValueAttribut(nameAttr, valueAttr, content);
					else {
						regexForPatternAttributContent = "(\\s(\\S+)\\s*=\\s*\"[^\"]*\")|(\\s(\\S+)\\s*=\\s*\'[^\']*\')";
						pattern = Pattern.compile(regexForPatternAttributContent);
						matcher = pattern.matcher(content);
						for (int i = 0; i < number; i++){
							if (!matcher.find())
								return XmlStringUtilities.changeValueAttribut(nameAttr, valueAttr, content);			
						}
						int endIndex = matcher.end();
						regexForPatternAttributContent = content.substring(0, endIndex);
						result = content.replaceAll(regexForPatternAttributContent,
								regexForPatternAttributContent + newAttr);
					}
				} 
			}
		}
		return result;
	}	
	
	/**
	 *  метод возвращает лист с атрибутами
	 */
	public static List<Attribut> getAttributs(String tagContent) {
		List<Attribut> result = new ArrayList<Attribut>();
		String regexForPatternAttributContent = "(\\s(\\S+)\\s*=\\s*\"[^\"]*\")|(\\s(\\S+)\\s*=\\s*\'[^\']*\')";				
		Pattern pattern = Pattern.compile(regexForPatternAttributContent);
		Matcher matcher = pattern.matcher(tagContent);
		while (matcher.find()) { // находим пару аттрибут - значение
			String name = null;
			String value = null;
			// получаем имя аттрибута
			String attributContent = matcher.group();
			String forPatternAttributName = "(\\s(\\S+)\\s*=\\s*\")|(\\s(\\S+)\\s*=\\s*\')";
			Pattern patternNameAttribut = Pattern.compile(forPatternAttributName);
			Matcher matcherNameAttribut = patternNameAttribut.matcher(attributContent);
			if (matcherNameAttribut.find()) {				
				String previousName = matcherNameAttribut.group().trim();
				int length  = previousName.length();
				length = previousName.substring(0, length-1).trim().length() - 1;
				name = previousName.substring(0, length).trim();
				//name = matcherNameAttribut.group().substring(0, matcherNameAttribut.group().length() - 1).trim();
			}
			// получаем значение
			String forPatternAttributValue = "(=\\s*\"[^\"]*\")|(=\\s*\'[^\']*\')";
			Pattern patternValueAttribut = Pattern.compile(forPatternAttributValue);
			Matcher matcherValueAttribut = patternValueAttribut.matcher(attributContent);
			if (matcherValueAttribut.find()) {
				value = (matcherValueAttribut.group().substring(1, matcherValueAttribut.group().length() - 1).trim())
						.substring(1);
			}
			result.add(new Attribut(name, value/* ,namePos,valuePos */));
		}

		return result;
	}
	
	/**
	 * Метод добавляет главный тег
	 */
	public static void addMainTag(String tagName, IDocument document){
		StringBuilder builder = new StringBuilder();
		builder.append(XmlStringUtilities.createTagDeclaration(tagName));
		builder.append('\n');
		builder.append(XmlStringUtilities.createEndTagDeclaration(tagName));
		try {
			document.replace(0, 0, builder.toString());
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * метод вставляет указанное сожержимое(Stirng, можно вставлять
	 * тег, обращать внимание на переходы строк) в документ
	 * после указанного объявления тега (Tag) 	 
	 */
	public static void addTag2(String content, Tag tag){		
		int offset = tag.getPosition().getOffset() + tag.getPosition().getLength();
		try {
			tag.getDocument().replace(offset, 0, content);
		} catch (BadLocationException e) {
			e.printStackTrace();
		}		
	}
	
	/**
	 * метод вставляет указанное сожержимое(Stirng, можно вставлять
	 * тег, обращать внимание на переходы строк) в документ
	 * после указанного полного тега (Tag) 	 
	 */
	public static void addTagAfterFullTag2(String content, FullTag tag){
		int offset = tag.getPosition().getOffset() + tag.getPosition().getLength();
		try {
			tag.getDocument().replace(offset, 0, content);
		} catch (BadLocationException e) {
			e.printStackTrace();
		}	
	}	
	
	/**
	 * Метод вставляет указанный контент после декларации тега
	 * Подходит для вставки после пустго тега
	 * Либо для вставки после начального полного тега (например добавить дочерний элемент) 
	 */
	public static void addContentAfterDeclarationTag(String content, Tag tag){
		int offset = tag.getPositionDeclarationTag().getOffset() + tag.getPositionDeclarationTag().getLength();	
		try {
			tag.getDocument().replace(offset, 0, content);
		} catch (BadLocationException e) {
			e.printStackTrace();
		}	
	}
	
	/**
	 * Метод вставляет указанный контент после тега
	 * Либо после пустого тега
	 * Либо после конечного полного тега
	 */
	public static void addContentAfterTag(String content, Tag tag){
		int offset = tag.getPosition().getOffset() + tag.getPosition().getLength();
		try {
			tag.getDocument().replace(offset, 0, content);
		} catch (BadLocationException e) {
			e.printStackTrace();
		}	
	}
	
	/**
	 * меняеет местами два тега
	 * первый вставляет после второго
	 */
	public static void changeTags(Tag firstTag, Tag secondTag){
		IDocument document = firstTag.getDocument();
		Position firstFullPosition = getFullPosition(firstTag.getPosition(), document, false);
		try {
			String firstContent = '\n'+document.get(firstFullPosition.getOffset(), firstFullPosition.getLength()) ;
			deleteTag(firstTag);
			addContentAfterTag(firstContent, secondTag);						
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * метод меняет объявление тега (начальнный тег) 
	 */
	public static void changeDeclarationTag(String content, Tag tag){
		IDocument document = tag.getDocument();
		int ofset = tag.getPositionDeclarationTag().offset;
		int length = tag.getPositionDeclarationTag().getLength();
		try {
			document.replace(ofset, length, content);
		} catch (BadLocationException e) {
			e.printStackTrace();
		}		
	}

	/**
	 * метод удаляет тег и пустые символы расположенные на этих строках
	 * возвращает офсет откуда удалили (первый символ)
	 */
	public static int deleteTag(Tag tag){
		Position pos = getFullPosition(tag.getPosition(), tag.getDocument(), true);
		try {
			tag.getDocument().replace(pos.getOffset(), pos.getLength(), "");
			return pos.getOffset();
		} catch (BadLocationException e1) {
			e1.printStackTrace();
		}
		return -1;
	}
	
	/**
	 * Возвращает полную позиции вместе с отступами до и после
	 * withDelimiters - true если вместе с переходом на другую строку
	 */
	public static Position getFullPosition(Position position, IDocument document, boolean withDelimiters){
		int start = position.getOffset();
		int end = position.getOffset() + position.getLength();
		try {
			IRegion line = document.getLineInformationOfOffset(start);
			int startLine = line.getOffset();
			String emtyText = document.get(startLine, start - startLine);
			if (isEmptyString(emtyText))
				start = startLine;
			line = document.getLineInformationOfOffset(end);
			int endLine = line.getOffset() + line.getLength();
			emtyText = document.get(end, endLine - end);
			if (isEmptyString(emtyText))
				end = endLine;	
			int delimiters = 0;  // чтобы удалить переход на другую строку
			if (withDelimiters) {
				if (document.get(end, 2).equals("\r\n"))
					delimiters = 2;
				else if (document.get(end, 1).equals("\n"))
					delimiters = 1;
			}
			return new Position(start, end - start + delimiters);
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
		return null;	
	}
	
	public static boolean isEmptyString(String str){
		String regex = "^[\\s]*$";
		Pattern pat = Pattern.compile(regex);
		Matcher match = pat.matcher(str);
		return match.find();
	}
	
	/**
	 * метод меняет пустой тег на такой же полный
	 * конечные тег не добавляется!!!!
	 */
	public static void changeToFullTag(EmptyTag tag){
		String oldContent = tag.getContent();
		String newContent = oldContent.substring(0, oldContent.length()-2) + ">";
		changeDeclarationTag(newContent, tag);		
	}
	
	/**
	 * метод меняет пустой тег на такой же полный
	 * добавляет пустой тег
	 */
	public static void changeToFullTagWithEnd(EmptyTag tag){				
		String indent = tag.getIndent();
		StringBuilder builder = new StringBuilder();
		builder.append('\n');
		builder.append(indent);
		builder.append(XmlStringUtilities.createEndTagDeclaration(tag.getName()));
		try {
			tag.getDocument().replace(tag.getPosition().getOffset() + tag.getPosition().getLength(), 0, builder.toString());
		} catch (BadLocationException e) {
			return;
		}
		
		String oldContent = tag.getContent();
		String newContent = oldContent.substring(0, oldContent.length()-2) + ">";
		changeDeclarationTag(newContent, tag);		
	}
	
	/**
	 * Метод возвращает дочерний тег по имени
	 * или null если нет
	 */
	public static Tag getChildTag(String name, FullTag tag){
		for (Tag iter: tag.getChildren())
			if (name.equals(iter.getName()))
				return iter;		
		return null;
	}
	
	/**
	 *  Добавляет в документ тег такого же уровня
	 *  Используется для вставки  тега (после пустого или полного)
	 */
	public static void addBrotherEmptyTag(String brotherContent, Tag tag){
		String indent = tag.getIndent();
		StringBuilder builder = new StringBuilder();
		builder.append('\n');
		builder.append(indent);
		builder.append(brotherContent);
		addContentAfterTag(builder.toString(), tag);
	}
		
	
	/**
	 * Добавляет полный тег такого же уровня
	 * с вложенным пустым тегом (исопльзуется для вставки полного тега)
	 */
	public static void addBrotherTag(String fullTagName, String emptyTagContent, Tag tag){		
		String indent = tag.getIndent();
		StringBuilder builder = new StringBuilder();
		builder.append('\n');
		builder.append(indent);
		builder.append(XmlStringUtilities.createTagDeclaration(fullTagName));
		builder.append('\n');
		builder.append(indent);
		builder.append('\t');
		builder.append(emptyTagContent);
		builder.append('\n');
		builder.append(indent);
		builder.append(XmlStringUtilities.createEndTagDeclaration(fullTagName));
		addContentAfterTag(builder.toString(), tag);
	}
	
	/**
	 *  Добавляет дочерний тег в документ
	 *  Используется для добавления  в полный тег пустой тег
	 */
	public static void addChildEmptyTag(String childContent, Tag tag){
		String indent = tag.getIndent();
		StringBuilder builder = new StringBuilder();
		builder.append('\n');
		builder.append(indent);
		builder.append('\t');
		builder.append(childContent);
		addContentAfterDeclarationTag(builder.toString(), tag);
	}
	
	/**
	 * Добавляет дочерний полный тег
	 * с вложенным пустым тегом
	 */
	public static void addChildTagWithEmptyTag(String fullTagName, String emptyTagContent, FullTag tag){
		String indent = tag.getIndent();
		StringBuilder builder = new StringBuilder();
		builder.append('\n');
		builder.append(indent);
		builder.append('\t');
		builder.append(XmlStringUtilities.createTagDeclaration(fullTagName));
		builder.append('\n');
		builder.append(indent);
		builder.append('\t');
		builder.append('\t');
		builder.append(emptyTagContent);
		builder.append('\n');
		builder.append(indent);
		builder.append('\t');
		builder.append(XmlStringUtilities.createEndTagDeclaration(fullTagName));
		addContentAfterDeclarationTag(builder.toString(), tag);
	}
	
	/**
	 * Меняет тег на полный и добавляет пустой тег
	 */
	public static void addChildEmptyTagIntoEmptyTag(String emptyTagContent, EmptyTag tag){
		String indent = tag.getIndent();
		StringBuilder builder = new StringBuilder();
		builder.append('\n');
		builder.append(indent);
		builder.append('\t');
		builder.append(emptyTagContent);
		builder.append('\n');
		builder.append(indent);		
		builder.append(XmlStringUtilities.createEndTagDeclaration(tag.getName()));
		addContentAfterTag(builder.toString(), tag);
		changeToFullTag(tag);
	}
	
	/**
	 * Меняет тег на полный и добавляет в него полный тег
	 * с вложенным пустым тегом
	 */
	public static void addChildTagIntoEmptyTag(String fullTagName, String emptyTagContent, EmptyTag tag){
		String indent = tag.getIndent();
		StringBuilder builder = new StringBuilder();
		builder.append('\n');
		builder.append(indent);
		builder.append('\t');
		builder.append(XmlStringUtilities.createTagDeclaration(fullTagName));
		builder.append('\n');
		builder.append(indent);
		builder.append('\t');
		builder.append('\t');
		builder.append(emptyTagContent);
		builder.append('\n');
		builder.append(indent);
		builder.append('\t');
		builder.append(XmlStringUtilities.createEndTagDeclaration(fullTagName));
		builder.append('\n');
		builder.append(indent);
		builder.append(XmlStringUtilities.createEndTagDeclaration(tag.getName()));
		addContentAfterTag(builder.toString(), tag);
		changeToFullTag(tag);
	}
	
}
