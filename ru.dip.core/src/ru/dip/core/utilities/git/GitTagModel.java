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

import org.eclipse.jgit.revwalk.RevTag;

public class GitTagModel {
	
	private final String fHash;
	private final String fTagName;
	private final String fMessage;
	private final RevTag fRevTag;
	
	public GitTagModel(String hash, String name, RevTag revTag) {
		fHash = hash;
		fTagName = name;
		fRevTag = revTag;
		fMessage = revTag.getFullMessage();
	}
	
	public String getTagName() {
		return fTagName;
	}
	
	public String getMessage() {
		return fMessage;
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Hash: ");
		builder.append(fHash);
		builder.append(" TagName: ");
		builder.append(fTagName);
		builder.append(" FullMessage: ");
		builder.append(fMessage);
		builder.append(" Author: ");
		builder.append(fRevTag.getObject().getClass());
		return builder.toString();
	}

}
