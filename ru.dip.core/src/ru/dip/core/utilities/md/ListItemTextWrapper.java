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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.eclipse.swt.graphics.Point;

import ru.dip.core.utilities.TagStringUtilities;

/**
 * Расставляет переносы с учетом отступа.
 */
public class ListItemTextWrapper {

	private String fNewContent;
	private List<Point> fIndentPoints; // куда вставляются дополнительные пробелы
	private int fLineIndent = 0;  // отступ строки, для вычисления fPoints
	
	public ListItemTextWrapper(String content, int length, int additionalIndent) {
		fIndentPoints = new ArrayList<>();
		if (length <= 30) {
			fNewContent = content;
		} else {
			fNewContent = getWrapTextWithIndent(content, length, additionalIndent);
		}
	}
	
	private String getWrapTextWithIndent(String text, int length, int additionalIndent){			
		if (text.length() <= length){
			return text;
		}		
		int indent = TagStringUtilities.startSpaces(text.toCharArray()) + additionalIndent;
		String[] lines = text.split(TagStringUtilities.LINE_SPLIT_REGEX, -1);
		List<String> result = new ArrayList<>();
		
		for (String line:lines){
			String wrapLine = getWrapIndentLine(line, length, indent);
			fLineIndent = wrapLine.length() + 1;
			result.add(wrapLine);			
		}
		return String.join(TagStringUtilities.lineSeparator(), result);		
	}
	
	private  String getWrapIndentLine(String line, int length, int indent){
		if (line.length() <= length){
			return line;
		}
		int currentPosition = length;
		int startLine = indent;
		StringBuilder builder = new StringBuilder(line);
		while (currentPosition < builder.length()){
			char ch = builder.charAt(currentPosition);		
			// идём с конца строки до пробела
			if (Character.isSpaceChar(ch)){
				builder.setCharAt(currentPosition, '\n');				
				for (int i = 0; i < indent; i++) {
					builder.insert(currentPosition + 1, ' ');
				}
				fIndentPoints.add(new Point(fLineIndent + currentPosition, indent));			
				startLine =  currentPosition + 1 + indent;
				currentPosition += length;
			} else {				
				currentPosition--;
				// если до начала страка не было пробелов
				if (currentPosition == startLine){
					builder.insert(currentPosition + length - indent, '\n');
					for (int i = 0; i < indent; i++) {
						builder.insert(currentPosition + length - indent + 1, ' ');
					}
					fIndentPoints.add(new Point(fLineIndent + currentPosition + length - indent, indent + 1));				
					startLine = currentPosition + length + 1;
					currentPosition = startLine + length - indent;
				}
			}		
		}	
		return builder.toString();
	}
	
	/**
	 * Сдвигаем точки с учетом добавленных отступов при переносе строк
	 */
	public void updatePoints(Stream<Point> points, int startListItem) {
		points.forEach(p -> {
			for (Point intentPoint: fIndentPoints) {
				int intentPointX = intentPoint.x + startListItem;
				
				// точка после отступа
				if (intentPointX < p.x) {
					p.x = p.x + intentPoint.y;
				}
			
				// отступ внутри точки
				if (intentPointX >= p.x && intentPointX <= p.x + p.y) {					
					if (intentPointX + intentPoint.y <= p.x + p.y) {
						p.y += intentPoint.y;
					} else {
						p.y += p.x + p.y - (intentPointX + intentPoint.y); 						
					}
				}
			}
			
		});
		
	}
	
	//=========================
	// getters
	
	public List<Point> IntentPoints(){
		return fIndentPoints;
	}
	
	public String newContent() {
		return fNewContent;
	}
	
}
