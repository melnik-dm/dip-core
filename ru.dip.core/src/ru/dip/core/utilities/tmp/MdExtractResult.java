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

public class MdExtractResult {
	
	private final TmpElement fOriginalTmpElement;	
	private final List<String> fCreatedElementNames;
	
	// for redo
	private TmpElement fNewOriginalTmpElement;
	private List<TmpElement> fNewTmpElements;

	public MdExtractResult(TmpElement originalElement, List<String> newNames) {
		fOriginalTmpElement = originalElement;
		fCreatedElementNames = newNames;
	}
	
	public TmpElement getTmpElement() {
		return fOriginalTmpElement;		
	}
	
	public List<String> getCreatedElementNames(){
		return fCreatedElementNames;
	}
	
	public TmpElement getNewOriginalTmpElement(){
		return fNewOriginalTmpElement;
	}
	
	public void setNewOriginalTmpElement(TmpElement tmpElement) {
		fNewOriginalTmpElement = tmpElement;
	}
	
	public List<TmpElement> getNewTmpElements(){
		return fNewTmpElements;
	}
	
	public void setNewTmpElements(List<TmpElement> newTmpElements) {
		fNewTmpElements = newTmpElements;
	}

}
