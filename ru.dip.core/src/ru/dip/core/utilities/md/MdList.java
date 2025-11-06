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

import org.commonmark.node.BulletList;
import org.commonmark.node.Node;

public abstract class MdList {

	private Node fList;
	
	public MdList(Node list) {
		fList = list;
	}
		
	public boolean isParent(Node node){
		return fList.equals(node.getParent());
	}
	
	public Node getList(){
		return (BulletList) fList;
	}
	
	public abstract String evaluateListMarker();
	
	public abstract String currentMarker();
}
