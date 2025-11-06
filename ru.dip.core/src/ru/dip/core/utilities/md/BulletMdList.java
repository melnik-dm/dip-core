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

public class BulletMdList extends MdList {

	private static final String BULLET_LIST_MARKER = "\u2022 ";
	
	public BulletMdList(BulletList list) {
		super(list);
	}
		
	public boolean isParent(Node node){
		return getList().equals(node.getParent());
	}
	
	public BulletList getList(){
		return (BulletList) super.getList();
	}
	
	@Override
	public String evaluateListMarker() {
		return BULLET_LIST_MARKER;
	}
	
	@Override
	public String currentMarker() {
		return BULLET_LIST_MARKER;
	}
}
