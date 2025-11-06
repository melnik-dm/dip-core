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
package ru.dip.core.utilities.git;

import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffEntry.ChangeType;

public class GitDiffUtils {
	
	public static boolean isNormalType(ChangeType type) {
		return type == ChangeType.MODIFY ||
				type == ChangeType.ADD ||
				type == ChangeType.DELETE;
	}
	
	/**
	 * Возвращает путь до объекта
	 *  oldPath - для удаленных
	 *  newPath - для остальных
	 */
	public static String getDiffPath(DiffEntry diff) {
		String diffPath;		
		if (diff.getChangeType() == ChangeType.DELETE) {
			diffPath =  diff.getOldPath();
		} else {
			diffPath = diff.getNewPath();
		}	
		return diffPath;
	}
	

}
