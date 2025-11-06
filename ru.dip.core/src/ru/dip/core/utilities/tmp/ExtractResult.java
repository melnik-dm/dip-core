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
package ru.dip.core.utilities.tmp;

import java.util.List;

public class ExtractResult implements ResultOperation {
	
	private List<MoveResult> fMovedResults;
	private TmpElement fTmpFolder;
	private String fDeletedFolderId;
	
	public ExtractResult(List<MoveResult> moved, TmpElement tmpFolder, String deletedFodlerId) {
		fTmpFolder = tmpFolder;
		fMovedResults = moved;
		fDeletedFolderId = deletedFodlerId;
	}
	
	public List<MoveResult> getMovedResults(){
		return fMovedResults;
	}
	
	public TmpElement getTmpFolder() {
		return fTmpFolder;
	}
	
	public String getDeltedFolderId() {
		return fDeletedFolderId;
	}
	
}
