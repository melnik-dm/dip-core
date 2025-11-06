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

public class EndTag implements ITag {
	
	private FullTag fStartTag;
	private Tag fParent;
	private String fName;
	
	/**
	 * конструктор, заполняет поля + заполняет поле endTag у стартового тега 
	 */
	public EndTag(FullTag startTag, String name){
		fName = name;
		fStartTag = startTag;
		fParent = startTag.fParent;
		fStartTag.setEndTag(this);
	}

	@Override
	public String getName() {
		return fName;
	}

	@Override
	public Tag getParent() {
		return fParent;
	}		

}
