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
package ru.dip.ui.table.ktable.diff.model;

import org.eclipse.jgit.diff.DiffEntry.ChangeType;
import org.eclipse.swt.graphics.Color;

import ru.dip.core.utilities.ui.swt.ColorProvider;

public enum ReqDiffStatus {

	ADD, DELETE, MODIFY, ADD_STAGING, DELETE_STAGING, MODIFY_STAGING;

	public static ReqDiffStatus fromChangeType(ChangeType type, boolean isStaging) {	
		if (isStaging) {
			switch (type) {
			case ADD: return ADD_STAGING;
			case DELETE: return DELETE_STAGING;
			case MODIFY: return MODIFY_STAGING;
			default:	throw new IllegalArgumentException("Неизвестный тип: " + type);	
			}
		} else {
			switch (type) {
			case ADD: return ADD;
			case DELETE: return DELETE;
			case MODIFY: return MODIFY;
			default:
				throw new IllegalArgumentException("Неизвестный тип: " + type);
			}
		}		
	}
	
	public boolean isStaging() {
		return this == ADD_STAGING 
				|| this == DELETE_STAGING
				|| this == MODIFY_STAGING;
	}
	
	public Color getColor() {
		if (this == MODIFY || this == MODIFY_STAGING) {
			return ColorProvider.DIFF_MODIFY;
		} else if (this == ADD || this == ADD_STAGING){
			return ColorProvider.DIFF_ADDED;
		}  else if (this == DELETE || this == DELETE_STAGING){
			return ColorProvider.DIFF_DELETE;
		} 
		return null;
	}

}
