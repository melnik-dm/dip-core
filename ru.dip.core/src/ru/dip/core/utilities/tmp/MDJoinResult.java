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

import ru.dip.core.model.interfaces.IDipDocumentElement;
import ru.dip.core.utilities.DipUtilities;

public class MDJoinResult {
	
	private final String fParentId;
	private final String fNewName;
	private final List<TmpElement> fOldTmpElements;
	// fo redo
	private TmpElement fNewTmpElement;
	
	public MDJoinResult(IDipDocumentElement newReq, List<TmpElement> oldElements) {
		fParentId = DipUtilities.relativeProjectID(newReq.parent());
		fNewName = newReq.name();
		fOldTmpElements = oldElements;
	}
	
	public String getNewName() {
		return fNewName;
	}
	
	public String getParentId() {
		return fParentId;
	}
	
	public List<TmpElement> getOldTmpElements(){
		return fOldTmpElements;
	}
	
	public TmpElement getNewTmpElement() {
		return fNewTmpElement;
	}
	
	public void setNewTmpElement(TmpElement newTmpElement) {
		fNewTmpElement = newTmpElement;
	}

}
