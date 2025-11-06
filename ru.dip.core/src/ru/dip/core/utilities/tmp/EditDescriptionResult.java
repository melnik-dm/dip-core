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

public class EditDescriptionResult {
	
	private final String fDipId;
	private final String fOldDescription;
	private final String fNewDescription;
	
	public EditDescriptionResult(String dipId, String oldDescription, String newDescription) {
		fDipId = dipId;
		fOldDescription = oldDescription;
		fNewDescription = newDescription;
	}
	
	public String getId() {
		return fDipId;
	}
	
	public String getOldDescription() {
		return fOldDescription;
	}
	
	public String getNewDescription() {
		return fNewDescription;
	}

}
