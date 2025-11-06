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
package ru.dip.ui.export;

public class TocEntry {
	
	private final String fNumber;
	private final String fDescritpion;
	private final String fLinkId;
	private final boolean fAppendix;
	
	public TocEntry(String number, String description, String linkId, boolean appendix) {
		fNumber = number;
		fDescritpion = description;
		fLinkId = linkId;
		fAppendix = appendix;
	}
	
	public String getNumber() {
		return fNumber;
	}
	
	public String getDescription() {
		return fDescritpion;
	}
	
	public String getLinkId() {
		return fLinkId;
	}
	
	public boolean isAppendix() {
		return fAppendix;
	}
	
}
