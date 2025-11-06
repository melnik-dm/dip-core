/**********************************************************************
 *  
 * Copyright (c) 2025 Denis Melnik.
 * Copyright (c) 2025 Ruslan Sabirov.
 * Copyright (c) 2025 Andrei Motorin.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package ru.dip.ktable.model;

import de.kupzog.ktable.KTableModel;

public interface IKTableModel extends KTableModel  {
	
	//===========================
	// поддержка плавной прокрутки
	
	int getRowLocation(int row);
	
	int getFullRowHeight(int row);
	
	void clearFirstElement();
	
	int firstCell();
	
	void setFirstCell(int firstCell);
	
	int indent();
	
	void setIndent(int indent);
		
	//=========================
	// getters
	
	boolean isCtrlPressed();

}
