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

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffEntry.ChangeType;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;

import ru.dip.core.utilities.GITUtilities;

public class GitCommitInfoUtils {
	
	//==========================================
	// измененные файлы в указанном коммите
	
	/**
	 * Список файлов которые были изменены в указанном коммите
	 * Берет из commitInfo, если там null, то получает из Git
	 */
	public static List<Path> getChangedCommitFiles(CommitInfo commitInfo, Repository repo) throws MissingObjectException, IncorrectObjectTypeException, IOException, GitAPIException {
		List<Path> changedFiles = commitInfo.getCommitChangedFiles();
		if (changedFiles == null) {
			changedFiles = computeChangedCommitFiles(commitInfo, repo);
			commitInfo.setCommitChangedFiles(changedFiles);
		}
		return changedFiles;
	}
	
	/**
	 * Список файлов которые были изменены в указанном коммите
	 * Получает из Git
	 */
	private static List<Path> computeChangedCommitFiles(CommitInfo commitInfo, Repository repo) throws MissingObjectException, IncorrectObjectTypeException, IOException, GitAPIException {
		try (Git git = new Git(repo)){
			RevCommit commit = GITUtilities.getRevCommitByHash(repo, commitInfo.getHash());			
			RevCommit prevCommit = GITUtilities.getPrevCommit(commit, repo);			
			List<DiffEntry> diffs = GITUtilities.computeChanged(repo, git, prevCommit, commit, null);
			return mapDiffEntryToPath(diffs, repo);			
		}
	}
	
	//==========================================
	// измененные файлы после указанного коммита (diff к HEAD)
	
	/**
	 * Вычисляет файлы, которые были изменены после указанного коммита (diff к HEAD)
	 * Берет из commitInfo, если там null, то получает из Git
	 */
	public static List<Path> getChangedToHeadFiles(CommitInfo commitInfo, Repository repo) throws MissingObjectException, IncorrectObjectTypeException, IOException, GitAPIException {
		List<Path> changedFiles = commitInfo.getChangedToHeadFiles();
		if (changedFiles == null) {
			changedFiles = computeChangedToHeadFiles(commitInfo, repo);
			commitInfo.setChangedToHeadFiles(changedFiles);
		}
		return changedFiles;
	}

	
	/**
	 * Вычисляет файлы, которые были изменены после указанного коммита (diff к HEAD)
	 * Получает из Git
	 */
	private static List<Path> computeChangedToHeadFiles(CommitInfo commitInfo, Repository repo) throws MissingObjectException, IncorrectObjectTypeException, IOException, GitAPIException {
		try (RevWalk revWalk = new RevWalk(repo); Git git = new Git(repo)){
			RevCommit commit = GITUtilities.getRevCommitByHash(repo, commitInfo.getHash());
			ObjectId head = repo.resolve(Constants.HEAD);
			RevCommit headCommit = revWalk.parseCommit(head);
			if (Objects.equals(commit.getName(), headCommit.getName())) {
				return Collections.emptyList();
			}			
			List<DiffEntry> diffs = GITUtilities.computeChanged(repo, git, commit, headCommit, null);
			return mapDiffEntryToPath(diffs, repo);			
		} 
	}
	
	//==========================================
	// измененные файлы до указанного коммита (diff к start commit)
	
	/**
	 * Вычисляет файлы, которые были изменены до указанного коммита (diff к StartCommit)
	 * Берет из commitInfo, если там null, то получает из Git
	 */
	public static List<Path> getChangedToStartFiles(CommitInfo commitInfo, Repository repo)
			throws MissingObjectException, IncorrectObjectTypeException, IOException, GitAPIException {
		List<Path> changedFiles = commitInfo.getChangedToStartFiles();
		if (changedFiles == null) {
			changedFiles = computeChangedToStartCommitFiles(commitInfo, repo);
			commitInfo.setChangedToStartFiles(changedFiles);
		}
		return changedFiles;
	}
	
	/**
	 * Вычисляет файлы, которые были изменены до указанного коммита (diff к StartCommit)
	 * Получает из Git
	 */
	private static List<Path> computeChangedToStartCommitFiles(CommitInfo commitInfo, Repository repo) throws MissingObjectException, IncorrectObjectTypeException, IOException, GitAPIException {
		try(RevWalk revWalk = new RevWalk(repo); Git git = new Git(repo)) {
			ObjectId commitId = repo.resolve(commitInfo.getHash());
			RevCommit commit = revWalk.parseCommit(commitId);
						
			RevCommit prevCommit = GITUtilities.getPrevCommit(commit, repo);
			if (prevCommit == null) {
				return Collections.emptyList();
			}			
			RevCommit startCommit = GITUtilities.getStartCommit(repo);
			
			// если указанный коммит является вторым
			if (Objects.equals(prevCommit.getName(), startCommit.getName())) {
				// вернуть список всех файлов из первого коммита
				return GITUtilities.getFilesForCommit(prevCommit, repo);
			}		
			List<DiffEntry> diffs = GITUtilities.computeChanged(repo, git, startCommit, prevCommit, null);
			return mapDiffEntryToPath(diffs, repo);
		}
	}
		
	//=================================
	// utils
	
	/**
	 * Преобразует DiffEntry в FullPath
	 */
	private static List<Path> mapDiffEntryToPath(List<DiffEntry> diffs, Repository repo){
		List<Path> result = new ArrayList<Path>();
		for (DiffEntry diff : diffs) {
			String path = null;
			if (diff.getChangeType() == ChangeType.DELETE) {
				path = diff.getOldPath();
			} else {
				path = diff.getNewPath();
			}
			Path fullPath = repo.getDirectory().getParentFile().toPath().resolve(path);
			result.add(fullPath);
		}
		return result;
	}
	
}
