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

public class MoveResult implements ResultOperation {
	
	private String fOldParentId;
	private String fNewParentId;
	// one objs
	private int fOldIndex;
	private String fName;
	// for many objs (into folder)
	private List<MoveResult> fElementResults;

	
	public MoveResult(String  name, String oldParentId, String newParentId, int oldIndex) {
		fName = name;
		fOldParentId = oldParentId;
		fNewParentId = newParentId;
		fOldIndex = oldIndex;
	}
	
	public MoveResult(List<MoveResult> elementResults, String oldParentId, String newParentId) {
		fElementResults = elementResults;
		fOldParentId = oldParentId;	
		fNewParentId = newParentId;
	}
	
	public String getName() {
		return fName;
	}
	
	public int getOldIndex() {
		return fOldIndex;
	}
	
	public List<MoveResult> getElementResults(){
		return fElementResults;
	}
	
	public boolean isOneElement() {
		return fName != null;
	}
	
	public String getOldParentId() {
		return fOldParentId;
	}
	
	public String getNewParentId() {
		return fNewParentId;
	}
	
}
