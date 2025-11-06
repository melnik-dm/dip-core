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
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Position;


public abstract class Tag implements ITag {

	protected Tag fParent; // родитель
	protected String fName; // имя 
	protected Position fPosition; // позиция в тескте
	protected IDocument fDocument; // документ
	protected List<Attribut> fAttributs = new ArrayList<Attribut>(); // атрибуты
	protected boolean fComment = false;
	private int fId;		

	/**
	 * Возвращает объявление тега (1 строка)
	 */
	public abstract String getDeclarationTagContent();
	
	/**
	 * Возвращает позицию объявления тега (1 строка) 
	 */
	public abstract Position getPositionDeclarationTag();
	

	/**
	 *  возвращает содержимое тега
	 */
	public String getContent() {
		int offset = fPosition.getOffset();
		int length = fPosition.getLength();
		try {
			return fDocument.get(offset, length);
		} catch (BadLocationException e) {

		}
		return "";
	}
	
	/**
	 *  меняет объявление тега (1 строка) на указанное
	 */
	public void setDeclarationTagContent(String newContent) {
		try {
			fDocument.replace(getPositionDeclarationTag().getOffset(), getPositionDeclarationTag().getLength(),
					newContent);
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
	}	

	/**
	 *  метод возвращает отступ от указнного тега
	 */
	public String getIndent(){
		int offset = getPosition().getOffset();
		IDocument document = getDocument();
		IRegion info;
		try {
			info = document.getLineInformationOfOffset(offset);
			int start= info.getOffset();
			return document.get(start, offset-start);			
		} catch (BadLocationException e) {
			
		}							
		return "";
	}

	//========================================
	// Атрибуты
	
	/**
	 *  проверяет есть ли такой атрибут
	 */
	public boolean isHaveAttribut(String atrName) {
		for (Attribut atr : fAttributs) {
			if (atr.getName().equals(atrName))
				return true;
		}
		return false;
	}

	/**
	 * возвращает указанный атрибут
	 */
	public Attribut getAttribut(String atrName){
		for (Attribut atr : fAttributs) {
			if (atr.getName().equals(atrName)){
				return atr;
			}
		}
		return null;
	}
	
	/**
	 * возвращает  значение указанного атрибута
	 */
	public String getAttributValue(String atrName){
		for (Attribut atr : fAttributs) {
			if (atr.getName().equals(atrName)){
				return atr.getValue();
			}
		}
		return null;
	}
	
	/**
	 * метод добавляет/меняет указанный атрибут в документе
	 */
	public void changeAttribut(String attributName, String attributValue){	
		String tagContent = getDeclarationTagContent();
		tagContent = XmlStringUtilities.changeValueAttribut(attributName, attributValue, tagContent);
		setDeclarationTagContent(tagContent);		
	}	

	//===========================================
	// getters & setters
	
	@Override
	public String getName() {
		return fName;
	}

	@Override
	public Tag getParent() {
		return fParent;
	}

	public Position getPosition() {
		return fPosition;
	}
	
	public void setPosition(Position pos) {
		fPosition = pos;
	}

	public IDocument getDocument() {
		return fDocument;
	}

	public void setDocument(IDocument document) {
		this.fDocument = document;
	}

	public List<Attribut> getAttributs() {
		return fAttributs;
	}

	public void setAttributs(List<Attribut> attributs) {
		this.fAttributs = attributs;
	}
	
	public int getId(){
		return fId;
	}
	
	public void setId(int id){
		fId = id;
	}
	
	public boolean isComment(){
		return fComment;
	}
	
	public void setComment(boolean comment){
		fComment = comment;
	}
	

}
