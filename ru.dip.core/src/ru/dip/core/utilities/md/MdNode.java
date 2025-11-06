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

import org.commonmark.node.Node;

public class MdNode {

	private int fStart;
	private int fEnd;
	private Node fNode;
	
	private List<MdNode> fChildren = new ArrayList<>();
	
	public MdNode(Node node) {
		fNode = node;
	}
	
	public void addChild(MdNode child){
		fChildren.add(child);
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(fNode);
		builder.append(" ");
		builder.append(fStart);
		builder.append("-");
		builder.append(fEnd);
		builder.append("  CHILDREN: ");
		builder.append(fChildren);
		return builder.toString();
	}
	
	//======================
	// getters & setters
	
	public int start() {
		return fStart;
	}
	
	public void setStart(int newValue) {
		fStart = newValue;
	}
	
	public int end() {
		return fEnd;
	}
	
	public void setEnd(int newValue) {
		fEnd = newValue;
	}
	
}
