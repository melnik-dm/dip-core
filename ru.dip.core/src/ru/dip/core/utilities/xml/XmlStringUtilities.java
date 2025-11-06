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
import org.eclipse.swt.widgets.Combo;

public class XmlStringUtilities {
	
	//==============================================
	// Работа с тегами
	
	/**
	 * метод возвращает полный тег с указанным отступом без аттрибутов
	 */
	public static String createFullTag(String name, String indent){
		StringBuilder builder = new StringBuilder();
		builder.append(indent);
		builder.append(createTagDeclaration(name));
		builder.append('\n');
		builder.append(indent);
		builder.append(createEndTagDeclaration(name));		
		return builder.toString();
	}	
	
	/**
	 * метод возвращает полный тег с указанным отступом c атрибутами
	 */
	public static String createFullTag(String name, String indent, String[] attributNames, String attributValues[]){
		StringBuilder builder = new StringBuilder();
		builder.append(indent);
		builder.append(createTagDeclaration(name, attributNames, attributValues));
		builder.append('\n');
		builder.append(indent);
		builder.append(createEndTagDeclaration(name));		
		return builder.toString();
	}	
	
	/**
	 * метод возвращает пустой тег с указанным отступом без атрибутов
	 */
	public static String createEmptyTag(String name, String indent){
		StringBuilder builder = new StringBuilder();
		builder.append(indent);
		builder.append(createEmptyTagDeclaration(name));	
		return builder.toString();
	}
	
	/**
	 * метод возвращает пустой тег с указанным отступом и атрибутами
	 */
	public static String createEmptyTag(String name, String indent, String[] attributNames, String attributValues[]){
		StringBuilder builder = new StringBuilder();
		builder.append(indent);
		builder.append(createEmptyTagDeclaration(name, attributNames, attributValues));	
		return builder.toString();
	}	
	
	/**
	 * мето возвращает объявление тега (начальный тег) без атрибутов
	 */
	public static String createTagDeclaration(String name){
		StringBuilder builder = new StringBuilder();
		builder.append('<');
		builder.append(name);
		builder.append('>');
		return builder.toString();
	}
		
	/** 
	 * метод возвращает объявление тега (начальный тег с одним атрибутом)
	 */
	public static String createTagDeclaration(String name, String atrName, String atrValue){
		StringBuilder builder = new StringBuilder();
		builder.append('<');
		builder.append(name);
		builder.append(' ');
		builder.append(atrName);
		builder.append('=');
		builder.append('\"');
		builder.append(atrValue);	
		builder.append('\"');
		builder.append('>');
		return builder.toString();
	}
	
	/**
	 *  метод возвращает объявление тега (начальный тег) с заданным именем и атрибутами
	 *  массивы с именами и значениями атрибутов должны быть одного размера
	 */
	public static String createTagDeclaration(String name, String[] attributNames, String attributValues[]){
		StringBuilder builder = new StringBuilder();	
		builder.append('<');
		builder.append(name);
		for (int i = 0; i < attributValues.length; i++){
			if (attributValues[i] == null || attributValues[i].isEmpty()){
				continue;
			}
			builder.append(' ');
			builder.append(attributNames[i]);
			builder.append('=');
			builder.append('\"');
			builder.append(attributValues[i]);
			builder.append('\"');
		}
		builder.append('>');										
		return builder.toString();
	}
	
	/**
	 * возвращает содержимое пустого тега без атрибутов	
	 */
	public static String createEmptyTagDeclaration(String name) {
		StringBuilder builder = new StringBuilder();
		builder.append('<');
		builder.append(name);
		builder.append("/>");
		return builder.toString();
	}
	
	/**
	 *  метод возвращает содержимое пустого тега с заданным именем и одним атрибутом
	 */
	public static String createEmptyTagDeclaration(String name, String atrName, String atrValue) {
		StringBuilder builder = new StringBuilder();
		builder.append('<');
		builder.append(name);
		builder.append(' ');
		builder.append(atrName);
		builder.append('=');
		builder.append('\"');
		builder.append(atrValue);
		builder.append('\"');
		builder.append("/>");
		return builder.toString();
	}
	
	/**
	 *  метод возвращает содержимое пустого тега с заданным именем и атрибутами
	 *  массивы с именами и значениями атрибутов должны быть одного размера
	 */
	public static String createEmptyTagDeclaration(String name, String[] attributNames, String attributValues[]){
		StringBuilder builder = new StringBuilder();	
		builder.append('<');
		builder.append(name);
		for (int i = 0; i < attributNames.length; i++){
			if (attributValues[i] == null || attributValues[i].isEmpty()){
				continue;
			}
			builder.append(' ');
			builder.append(attributNames[i]);
			builder.append('=');
			builder.append('\"');
			builder.append(attributValues[i]);
			builder.append('\"');
		}
		builder.append("/>");										
		return builder.toString();
	}
	
	/**
	 *  метод возвращает объявление конечного тега
	 */
	public static String createEndTagDeclaration(String name){
		StringBuilder builder = new StringBuilder();	
		builder.append("</");
		builder.append(name);
		builder.append('>');										
		return builder.toString();
	}
	
	/**
	 *  метод возвращает имя тега (конечного тега), pattern - для конечного тега 
	 */
	public static String getName(String tagContent, boolean isEndTag){		
		Pattern pattern;		
		if (isEndTag){
			pattern = Pattern.compile("[<][/][\\S]+[>]");
		}
		else{
			pattern = Pattern.compile("[<][\\S]+[(\\s)|(>)]");
		}					
		Matcher matcher = pattern.matcher(tagContent);
		if (matcher.lookingAt() && !isEndTag)
			{
			String result = tagContent.substring(matcher.start()+1, matcher.end()-1);
			if (result.endsWith("/"))
					return result.substring(0, result.length()-1);
			return 	result;
			}
		if (matcher.lookingAt() && isEndTag)
			return tagContent.substring(matcher.start()+2, matcher.end()-1);
		return null;
	}
	
	/**
	 *  метод определяет тип тега: 1 - пустой, 2 - стартовый, 3 - конечный
	 */
	public static int getTypeCommentTag(String content){
		String regex = "^\\s*<\\w[^<]+/>\\s*$";
		Pattern pat = Pattern.compile(regex);
		Matcher match = pat.matcher(content);
		if (match.find())
			return 1;		
		regex = "^\\s*<\\w{1}[^<]+[^/]>\\s*$";
		pat = Pattern.compile(regex);
		match = pat.matcher(content);
		if (match.find()) 
			return 2;		
		regex = "^\\s*</\\w{1}[^<]+[^/]>\\s*$";
		pat = Pattern.compile(regex);
		match = pat.matcher(content);
		if (match.find())
			return 3;			
		return 0;
	}
	
	//============================================
	// работа с атрибутами

	/**
	 * Возвращает значение атрибута
	 */
	public static String getValueAttribut(String nameAttr, String content) {
		List<Attribut> attributs = getAttributs(content);
		for (Attribut attribut: attributs) {
			if (attribut.getName().equals(nameAttr)) {
				return attribut.getValue();
			}
		}
		return null;
	}
	
	/**
	 * втавляет значение тега добавляет аттрибут в указанное место
	 */
	public static String changeValueAttribut(String nameAttr, String valueAttr, String content, int number) {
		String result = "";
		String regexForPatternAttributContent = "(\\s(" + nameAttr + ")\\s*=\\s*\"[^\"]*\")|(\\s(" + nameAttr
				+ ")\\s*=\\s*\'[^\']*\')";

		if (valueAttr.length() == 0){   // если значение аттрибута равно нулю
			result = content.replaceAll(regexForPatternAttributContent, "");
		}
		else {
			String newAttr = " " + nameAttr + "=" + "\"" + valueAttr + "\"";
			Pattern pattern = Pattern.compile(regexForPatternAttributContent);
			Matcher matcher = pattern.matcher(content);
			if (matcher.find()) {  // если есть такой атрибут
				result = content.replaceAll(regexForPatternAttributContent, newAttr);
			} else {
				String name = getName(content, false);

				if (number == 0) {   // если аттрибут первый номер
					name = getName(content, false);
					regexForPatternAttributContent = "<" + name;
					result = content.replaceAll(regexForPatternAttributContent,
							regexForPatternAttributContent + newAttr);
				} else {
					if ((getAttributs(content).size()) < number)
						return changeValueAttribut(nameAttr, valueAttr, content);
					else {
						regexForPatternAttributContent = "(\\s(\\S+)\\s*=\\s*\"[^\"]*\")|(\\s(\\S+)\\s*=\\s*\'[^\']*\')";
						pattern = Pattern.compile(regexForPatternAttributContent);
						matcher = pattern.matcher(content);
						for (int i = 0; i < number; i++){
							if (!matcher.find())
								return changeValueAttribut(nameAttr, valueAttr, content);			
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
			String forPatternAttributName = "(\\s(\\S+)\\s*=\\s*\")";
			Pattern patternNameAttribut = Pattern.compile(forPatternAttributName);
			Matcher matcherNameAttribut = patternNameAttribut.matcher(attributContent);
			if (matcherNameAttribut.find()) {
				name = matcherNameAttribut.group().substring(0, matcherNameAttribut.group().length() - 1).trim();
				name = name.trim();
				name = name.substring(0, name.length()-1).trim();
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
	 *  метод меняет аттрибут в заданном теге
	 */
	public static String changeValueAttribut(String nameAttr, String valueAttr, String content) {
		String result = "";
		String regexForPatternAttributContent = "(\\s(" + nameAttr + ")\\s*=\\s*\"[^\"]*\")|(\\s(" + nameAttr
				+ ")\\s*=\\s*\'[^\']*\')";

		if (valueAttr.length() == 0)
			result = content.replaceAll(regexForPatternAttributContent, "");
		else {
			String newAttr = " " + nameAttr + "=" + "\"" + valueAttr + "\"";
			Pattern pattern = Pattern.compile(regexForPatternAttributContent);
			Matcher matcher = pattern.matcher(content);
			if (matcher.find()) {
				result = content.replaceAll(regexForPatternAttributContent, newAttr);
			} else {
				if (content.trim().endsWith("/>")) {
					result = content.trim().substring(0, content.trim().length() - 2).trim() + newAttr + "/>";
				} else if (content.trim().endsWith(">")) {
					result = content.trim().substring(0, content.trim().length() - 1).trim() + newAttr + ">";
				}
			}
		}
		return result;
	}	
	

	//==========================================
	// отступы
	
	/**
	 *  метод возвращает отступ от указнного тега
	 */
	public static String getIndent(Tag tag){
		int offset = tag.getPosition().getOffset();
		IDocument document = tag.getDocument();
		IRegion info;
		try {
			info = document.getLineInformationOfOffset(offset);
			int start= info.getOffset();
			return document.get(start, offset-start);			
		} catch (BadLocationException e) {
			
		}							
		return "";
	}
	
	/**
	 *  возвращает отступ от строки
	 */
	public static String getIndent(String str){
		String regex = str.trim();
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(str);
		if (matcher.find()){
			 return str.substring(0, matcher.start());			
		}						
		return "";
	}
	
	/**
	 *  возвращает отступ в конце строки
	 */
	public static String getOutdent(String str){
		String regex = str.trim();
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(str);
		if (matcher.find()){
			 return str.substring(matcher.end());			
		}						
		return "";
	}
	
	//===================================
	// Коментирование
		
	/**
	 *  метод комментирует позицию
	 */
	public static void commentPosition(Position pos, IDocument document){
		try {
			String content = document.get(pos.getOffset(),pos.getLength());	
			String newContent = "<!--"+content+"-->";
			document.replace(pos.getOffset(), pos.getLength(), newContent);
			//pos.setLength(newContent.length());
		} catch (BadLocationException e) {
			e.printStackTrace();
			return;
		}
	}
	
	/**
	 *  метод раскомментирует позицию
	 */
	public static void uncommentPosition(Position pos, IDocument document){
		try {
			String content = document.get(pos.getOffset(),pos.getLength());	
			Position cleanPosition = getCleanPosition(pos, content);
			String newContent = document.get(cleanPosition.getOffset(),cleanPosition.getLength());
			document.replace(pos.getOffset(), pos.getLength(), newContent);
			//pos.setLength(newContent.length());
		} catch (BadLocationException e) {
			e.printStackTrace();
			return;
		}
	}

	/**
	 * метод получает позиции закоментированного тега возвращает позицию самого тега без учета символ коммента
	 */
	public static Position getCleanPosition(Position position, String content){
		String regex = "(<\\w[^<]+>)|(</\\w[^<]+>)";
		String content2 = content.substring(0, content.length()-3); 
		Pattern pat = Pattern.compile(regex);
		Matcher mat = pat.matcher(content2);		
		if (mat.find()){
			int length = mat.group().length();
			int offset = position.getOffset() + mat.start();
			return new Position(offset,length);
		}		
		return position;
	}
	
	//======================================
	// поиск по спискам
	
	/**
	 * проверяет естли строка в списке Combo
	 */
	public static boolean isComboType(String str, Combo combo){
		for (String iter : combo.getItems()) {
			if (iter.equals(str)) 
				return true;		
		}
		return false;
	}

	/**
	 *  true если такая строка есть в массиве
	 */
	public static boolean containsValueInArray(String[] array, String value){
		for (String iter: array)
			if (value.equals(iter))
				return true;
		
		return false;
	}
	
	//=======================================
	// Проверки на соответствие типу
	
	/**
	 *  является ли строка 10-тичным или 16-чным числом 
	 */
	public static boolean isDecOrHexValue (String str){
		String regex = "^[+-]{0,1}[0-9]+$|^[+-]{0,1}0x[0-9a-fA-F]+$";
		Pattern pat = Pattern.compile(regex);
		Matcher match = pat.matcher(str);
		return match.find();
	}
	
	/**
	 *  является ли строка 10-тичным или 16-чным положительным числом 
	 */
	public static boolean isIdentifierValue (String str){
		String regex = "^[+]{0,1}[0-9]+$|^[+]{0,1}0x[0-9a-fA-F]+$";
		Pattern pat = Pattern.compile(regex);
		Matcher match = pat.matcher(str);
		return match.find();
	}
	
	/**
	 *  является ли строка типом boolean
	 */
	public static boolean isBooleanValue (String str){
		return str.equals("0") || str.equals("1") || str.equals("true") || str.equals("false");
	}
	
	/**
	 * является ли строка числом float (по xsd схеме)
	 * не учитывает значения NaN, INF  и т.п.
	 */
	public static boolean isFloatXSDValue(String str){		
		String regex ="^[-+]?(?:\\b[0-9]+(?:\\.[0-9]*)?|\\.[0-9]+\\b)(?:[eE][-+]?[0-9]+\\b)?$";
		Pattern pat = Pattern.compile(regex);
		Matcher match = pat.matcher(str);
		return match.find();		
	}
	
	/**
	 * является ли строка положительным числом float (по xsd схеме)
	 * не учитывает значения NaN, INF  и т.п.
	 */
	public static boolean isPositiveFloatXSDValue(String str){		
		String regex ="^[+]?(?:\\b[0-9]+(?:\\.[0-9]*)?|\\.[0-9]+\\b)(?:[eE][+]?[0-9]+\\b)?$";
		Pattern pat = Pattern.compile(regex);
		Matcher match = pat.matcher(str);
		return match.find();		
	}
	
	/**
	 * является ли строка числом int (по xsd схеме)
	 * не учитывает значения NaN, INF  и т.п.
	 */
	public static boolean isIntegerXSDValue(String str){		
		String regex ="^[+-]?([1-9][0-9]*|0)$";
		Pattern pat = Pattern.compile(regex);
		Matcher match = pat.matcher(str);
		return match.find();		
	}
	
	/**
	 * является ли строка положительным числом int (по xsd схеме)
	 * не учитывает значения NaN, INF  и т.п.
	 */
	public static boolean isPositiveIntegerXSDValue(String str){		
		String regex ="^[+]?([1-9][0-9]*|0)$";
		Pattern pat = Pattern.compile(regex);
		Matcher match = pat.matcher(str);
		return match.find();		
	}
}
