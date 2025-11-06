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

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Position;

public class FullTag extends Tag  {

	private ITag fEndTag; 	// конечный тег
	protected List<Tag> fChildren = new ArrayList<Tag>();	// вложенные теги	
	protected Position fPositionDeclarationTag;     // позиция объявления тега
	protected Position fPositionEndTag;			// позиция конечного тега
	
	public FullTag(String name, Tag parent){
		fName = name;
		fParent = parent;
	}
	
	/**
	 * Констурктор - преобразует пустой тег - в полный
	 */
	public FullTag(EmptyTag tag){
		tag.getIndent();
		tag.getPosition();
		tag.getDeclarationTagContent();
	}
	
	
	/**
	 * Возвращает содержимое объявления (1 строка)
	 */
	@Override
	public String getDeclarationTagContent() {
		String result = null;
		try {
			result = fDocument.get(fPositionDeclarationTag.getOffset(),fPositionDeclarationTag.getLength());
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
		return result;
	}
	
	/**
	 * Метод возвращает дочерний тег по имени
	 * или null если нет
	 */
	public Tag getChildTag(String name){
		for (Tag iter: fChildren)
			if (name.equals(iter.getName()))
				return iter;		
		return null;
	}
	
	/**
	 * Метод возвращает дочерний тег по номеру
	 */
	public int getChildTagNumber(Tag child){
		for (int i = 0; i < fChildren.size(); i++)
			if (child.equals(fChildren.get(i)))
				return i;
		return -1;
	}
	
	/**
	 * Возвращает полную позиции
	 */
	@Override
	public Position getPosition() {
		int offset = fPositionDeclarationTag.getOffset();
		int length = fPositionEndTag.getOffset() + fPositionEndTag.getLength() - offset;
		return new Position(offset, length);		
	}
	
	
	/**
	 * Полное содержимое с конечным тегом и со всеми внутренними	  
	 */
	@Override
	public String getContent() {
		int offset = getPosition().getOffset();
		int length = getPosition().getLength();
		try {
			return fDocument.get(offset, length);
		} catch (BadLocationException e) {

		}
		return "";
	}
	
	public String getTextContent(){
		int start = fPositionDeclarationTag.offset + fPositionDeclarationTag.length;
		int end = fPositionEndTag.offset;
		try {
			return fDocument.get(start, end - start).trim();
		} catch (BadLocationException e) {

		}
		return "";
		
	}

	//===========================================
	//getters & setters
	
	@Override
	public Position getPositionDeclarationTag() {
		return fPositionDeclarationTag;
	}
		
	public void setPositionDeclarationTag(Position pos){
		fPositionDeclarationTag = pos;
	}

	public Position getPositionEndTag() {
		return fPositionEndTag;
	}
	
	public void setPositionEndTag(Position position){
		fPositionEndTag = position;
	}
	
	
	public List<Tag> getChildren(){
		return fChildren;
	}
	
	public void setEndTag(ITag tag){
		fEndTag = tag;
	}
	
	public ITag getEndTag(){
		return fEndTag;
	}
	
}
