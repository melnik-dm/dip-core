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
package ru.dip.core.form.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Position;

public class Tag {
	
	private IDocument fDocument;
	private String fTagName;  // attribute name
	private String fValue;
	private List<Tag> fChildren = new ArrayList<>();
	private Position fStartTagPosition;
	private Position fEndTagPosition;
	
	public Tag(String name, Position startPosition, Position endPosition, IDocument document){
		fTagName = name;
		fStartTagPosition = startPosition;
		fEndTagPosition = endPosition;
		fDocument = document;
		fValue = getContentFromDocument();
	}
	
	private String getContentFromDocument(){
		int start = fStartTagPosition.offset + fStartTagPosition.length + 1;
		int end = fEndTagPosition.offset - 1;
		try {
			return fDocument.get(start, end - start);
		} catch (BadLocationException e) {
			return "";
		}
	}
	
	public void setValue(String newValue){
		if (Objects.equals(fValue, newValue)) {
			return;
		}
		if (newValue == null) {
			newValue = "";
		}
		fValue = newValue;
		String newContent = "\n" + fValue;
		int start = fStartTagPosition.offset + fStartTagPosition.length;
		int end = fEndTagPosition.offset - 1;
		try {
			fDocument.replace(start, end - start, newContent);
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public String toString() {
		return fTagName + " " + fValue;
	}
	
	//=================================
	// children
	
	public void addChild(Tag tag){
		fChildren.add(tag);
	}
	
	public void addChildren(Collection<Tag> children){
		fChildren.addAll(children);
	}
	
	public List<Tag> getChildren(){
		return fChildren;
	}
	
	//==================================
	// getters & setters
	
	public String getName(){
		return fTagName;
	}
	
	public String getValue(){
		return fValue;
	}
	
	public int getEndOffset(){
		return fEndTagPosition.getOffset() + fEndTagPosition.getLength();
	}
	
	public int getOffset(){
		return fStartTagPosition.offset;
	}
	
	public int getEndOfStartTag(){
		return fStartTagPosition.offset + fStartTagPosition.length;		
	}

}
