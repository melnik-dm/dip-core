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

import java.nio.file.Path;
import java.util.List;

import org.eclipse.jgit.revwalk.RevCommit;

public class CommitInfo {
	
	private final String fHash;
	private final int fTime;
	private final String fRepoPath;
	
	private List<Path> fChangedFiles;   // файлы измененные в коммите
	private List<Path> fChangedToHeadFiles; // файлы измененные после коммита (diff к HEAD)
	private List<Path> fChangedToStartFiles; // файлы измененные до коммита (diff к start commit
	
	public CommitInfo(RevCommit commit, String repoPath) {
		fHash = commit.getName();
		fTime =  commit.getCommitTime();
		fRepoPath = repoPath;
		// RevCommit лучше не сохранять, не постоянен, лучше потом получить новый экземпляр по хэшу
	}
	
	public String getHash() {
		return fHash;
	}
	
	public int getTime() {
		return fTime;
	}
	
	public String getRepoPath() {
		return fRepoPath;
	}

	public List<Path> getCommitChangedFiles() {
		return fChangedFiles;
	}

	public void setCommitChangedFiles(List<Path> changedFiles) {
		fChangedFiles = changedFiles;
	}

	public List<Path> getChangedToHeadFiles() {
		return fChangedToHeadFiles;
	}
	
	public void setChangedToHeadFiles(List<Path> changedFiles) {
		fChangedToHeadFiles = changedFiles;
	}

	public List<Path> getChangedToStartFiles() {
		return fChangedToStartFiles;
	}
	
	public void setChangedToStartFiles(List<Path> changedFiles) {
		fChangedToStartFiles = changedFiles;
	}

}
