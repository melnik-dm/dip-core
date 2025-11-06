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
import java.util.TreeSet;
import java.util.stream.Collectors;

import ru.dip.core.model.interfaces.IDipDocumentElement;
import ru.dip.core.utilities.DipUtilities;

/**
 * Результат выполнение операции up илии down
 */
public class UpDownResult implements ResultOperation {
	
	@SuppressWarnings("unused")
	private boolean fUp;	// направление 
	private String fDipDocElementId;
	private List<String> fDipDocElementIds;
	
	public UpDownResult(boolean up, IDipDocumentElement dipDocElement) {
		fUp = up;
		fDipDocElementId = DipUtilities.relativeProjectID(dipDocElement);
	}
	
	public UpDownResult(boolean up, TreeSet<IDipDocumentElement> dipDocElements) {
		fUp = up;
		fDipDocElementIds = dipDocElements.stream()
				.map(DipUtilities::relativeProjectID)
				.collect(Collectors.toList());
	}

	public boolean isOne() {
		return fDipDocElementId != null;
	}
	
	public String getDipDocElementId() {
		return fDipDocElementId;
	}
	
	public List<String> getDipDocElementsIds(){
		return fDipDocElementIds;
	}
	

}
