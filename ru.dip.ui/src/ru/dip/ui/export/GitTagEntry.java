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

public class GitTagEntry {
	
	private final String fTag;
	private final String fMessage;
	
	public GitTagEntry(String tag, String message) {
		fTag = tag;
		fMessage = message;
	}
	
	public String getTag() {
		return fTag;
	}
	
	public String getMessage() {
		return fMessage;
	}

}
