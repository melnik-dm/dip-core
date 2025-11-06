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
package ru.dip.core.report.checker;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffEntry.ChangeType;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;

import ru.dip.core.model.DipUnit;
import ru.dip.core.model.finder.FindSettings;
import ru.dip.core.model.interfaces.IDipDocumentElement;
import ru.dip.core.model.interfaces.IDipElement;
import ru.dip.core.model.interfaces.IParent;
import ru.dip.core.report.model.condition.Sign;
import ru.dip.core.report.model.condition.Value;
import ru.dip.core.unit.TablePresentation;
import ru.dip.core.utilities.DipUtilities;
import ru.dip.core.utilities.GITUtilities;
import ru.dip.core.utilities.git.CommitInfo;
import ru.dip.core.utilities.git.GitCommitInfoUtils;

public class SimpleConditionChecker {

	// =================
	// file extension

	public static boolean checkFileExtension(IDipElement element, Sign sign, Value value) {
		if (element.resource() == null) {
			return false;
		}
		String extension = element.resource().getFileExtension();
		return checkFileExtension(extension, sign, value);
	}

	public static boolean checkFileExtension(String extension, Sign sign, Value value) {
		if (sign == Sign.EQUAL) {
			return extension.equals(value.getValue());
		} else {
			return !extension.equals(value.getValue());
		}
	}

	// ===================
	// path

	public static boolean checkPath(IDipElement element, Value value) {
		return checkPath(element.parent(), value);
	}

	private static boolean checkPath(IParent parent, Value value) {
		String path = value.getValue();
		if (path == null || path.isEmpty()) {
			return true;
		}
		boolean recursive = false;
		if (path.endsWith("/*")) {
			recursive = true;
			path = path.substring(0, path.length() - 2);
		}
		IDipElement element = DipUtilities.findElement(parent.dipProject(), path);
		if (!(element instanceof IParent)) {
			return false;
		}
		if (parent == element) {
			return true;
		}
		if (((IParent) element).hasParent(parent)) {
			return true;
		}
		if (recursive) {
			return parent.hasParent((IParent) element);
		}
		return false;
	}

	// ======================
	// version

	/**
	 * Проверяет были ли изменения объекта в указанном коммите (больше, меньше, неравно)
	 */
	public static boolean checkVersion(DipUnit unit, Sign sign, Value value) {
		CommitInfo commitInfo = null;
		if (value.getData() instanceof CommitInfo) {
			commitInfo = (CommitInfo) value.getData();
		} else {
			return false;
		}

		Repository repo = unit.dipProject().getGitRepo();
		if (repo == null) {
			return false;
		}

		try {
			if (sign == Sign.EQUAL) {
				if (value.getData() instanceof CommitInfo) {
					List<Path> changedFiles = GitCommitInfoUtils.getChangedCommitFiles(commitInfo, repo);
					Path unitPath = Path.of(unit.resource().getLocationURI());
					return changedFiles.contains(unitPath);
				} else {
					return false;
				}
			} else if (sign == Sign.NOT_EQUAL) {
				List<Path> changedFiles = GitCommitInfoUtils.getChangedCommitFiles(commitInfo, repo);
				Path unitPath = Path.of(unit.resource().getLocationURI());
				return !changedFiles.contains(unitPath);
			} else if (sign == Sign.MORE) {
				List<Path> toHeadChangedFiles = GitCommitInfoUtils.getChangedToHeadFiles(commitInfo, repo);
				Path unitPath = Path.of(unit.resource().getLocationURI());
				return toHeadChangedFiles.contains(unitPath);
			} else if (sign == Sign.LESS) {
				List<Path> toStartChangedFiles = GitCommitInfoUtils.getChangedToStartFiles(commitInfo, repo);
				Path unitPath = Path.of(unit.resource().getLocationURI());
				if (toStartChangedFiles != null) {
					return toStartChangedFiles.contains(unitPath);
				}
			} else if (sign == Sign.MORE_EQUAL) {
				List<Path> toHeadChangedFiles = GitCommitInfoUtils.getChangedToHeadFiles(commitInfo, repo);
				List<Path> changedFiles = GitCommitInfoUtils.getChangedCommitFiles(commitInfo, repo);
				Path unitPath = Path.of(unit.resource().getLocationURI());
				return toHeadChangedFiles.contains(unitPath) || changedFiles.contains(unitPath);
			} else if (sign == Sign.LESS_EQUAL) {
				List<Path> toStartChangedFiles = GitCommitInfoUtils.getChangedToStartFiles(commitInfo, repo);
				List<Path> changedFiles = GitCommitInfoUtils.getChangedCommitFiles(commitInfo, repo);
				Path unitPath = Path.of(unit.resource().getLocationURI());
				return toStartChangedFiles.contains(unitPath) || changedFiles.contains(unitPath);
			}
		} catch (IOException | GitAPIException e) {
			e.printStackTrace();
			return false;
		}
		return false;
	}
	
	/**
	 * Проверка по последнему изменению файла
	 * За версию объекта принмается тот коммит, в котором было последне изменение
	 * В данный момент не используется на будущее
	 */
	@SuppressWarnings("unused")
	private static boolean checkLastVersion(DipUnit unit, Sign sign, Value value) {
		CommitInfo commitInfo = null;
		if (value.getData() instanceof CommitInfo) {
			commitInfo = (CommitInfo) value.getData();
		} else {
			return false;
		}
		
		Repository repo = unit.dipProject().getGitRepo();
		if (repo == null) {
			return false;
		}
		
		try {
			RevCommit changedUnitCommit = GITUtilities.getChangedCommit(repo, unit);
			if (changedUnitCommit == null) {
				return false;
			}

			if (!commitInfo.getRepoPath().equals(repo.getDirectory().getParentFile().getAbsolutePath())) {
					return false;
				}
	
			int time1 = changedUnitCommit.getCommitTime();
			int time2 = commitInfo.getTime();

			switch (sign) {
			case EQUAL: {
				return Objects.equals(changedUnitCommit.getName(), commitInfo.getHash());
			}
			case NOT_EQUAL: {
				return !Objects.equals(changedUnitCommit.getName(), commitInfo.getHash());
			}
			case MORE: {
				return time1 > time2;
			}
			case MORE_EQUAL: {
				return time1 >= time2;
			}
			case LESS: {
				return time1 < time2;
			}
			case LESS_EQUAL: {
				return time1 <= time2;
			}
			default:
				return false;
			}
		} catch (IOException | GitAPIException e) {
			e.printStackTrace();
		}
		return false;
	}

	public static List<Path> mapDiffEntryToPath(List<DiffEntry> diffs, Repository repo){
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
	
	// =======================
	// enabled

	public static boolean checkEnabled(IDipDocumentElement dipDocumentElement, Sign sign, Value value) {
		boolean bValue = Boolean.parseBoolean(value.getValue());
		boolean enabled = !dipDocumentElement.isDisabled();
		if (Sign.EQUAL == sign) {
			return enabled == bValue;
		} else {
			return enabled != bValue;
		}
	}

	// ========================
	// text

	public static boolean checkText(DipUnit unit, Sign sign, Value value, boolean caseSensetive) {
		TablePresentation presentation = unit.getUnitPresentation().getPresentation();
		boolean find = presentation.contains(value.getValue(), FindSettings.onlyCaseSensetive(caseSensetive));
		return ((sign == Sign.EQUAL) == find);
	}

	public static boolean checkWord(DipUnit unit, Sign sign, Value value, boolean caseSensetive) {
		TablePresentation presentation = unit.getUnitPresentation().getPresentation();
		boolean find = presentation.findWord(value.getValue(), caseSensetive);
		return ((sign == Sign.EQUAL) == find);
	}

}
