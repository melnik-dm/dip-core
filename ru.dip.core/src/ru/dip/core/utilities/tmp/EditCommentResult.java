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

public class EditCommentResult {
	
	private final String fDipId;
	private final String fOldComment;
	private final String fNewComment;
	
	public EditCommentResult(String dipId, String oldDescription, String newDescription) {
		fDipId = dipId;
		fOldComment = oldDescription;
		fNewComment = newDescription;
	}
	
	public String getId() {
		return fDipId;
	}
	
	public String getOldComment() {
		return fOldComment;
	}
	
	public String getNewComment() {
		return fNewComment;
	}

}
