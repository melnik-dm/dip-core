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

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

import org.eclipse.jgit.revwalk.RevCommit;

public class GitRevisionModel {
	
	private final String fHash;
	private final String fMessage;
	private final String fAuthor;
	private final LocalDateTime fTime;
	private final RevCommit fCommit;
	
	public GitRevisionModel(RevCommit commit) {	
		fHash = commit.getId().abbreviate(7).name();		
		fMessage = commit.getFullMessage();
		fAuthor = commit.getAuthorIdent().getName();
				
		Date date = new Date(((long) commit.getCommitTime()) * 1000);
		fTime = LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());		
		fCommit = commit;
	}
	
	public String getHash() {
		return fHash;
	}
	
	public String getMessage() {
		return fMessage;
	}
	
	public String getAuthor() {
		return fAuthor;
	}
	
	public LocalDateTime getTime() {
		return fTime;
	}
	
	public RevCommit getRevCommit() {
		return fCommit;
	}
	
}
