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
package ru.dip.ui.export.diff;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.errors.NoWorkTreeException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;

import ru.dip.core.model.DipProject;
import ru.dip.core.unit.UnitType;
import ru.dip.core.utilities.GITUtilities;
import ru.dip.ui.export.diff.model.DiffExportEntry;
import ru.dip.ui.export.diff.model.DipRevisionProject;
import ru.dip.ui.export.diff.model.UnitRevisionEntry;
import ru.dip.ui.table.ExporterHolder;

public class DiffExportPreprocessor {
	
	private final DipProject fDipProject;
	private final Repository fRepo;
	private final RevCommit fRevCommit1;
	private final RevCommit fRevCommit2;
	private final Path fTargetPath;
	private final Path fConfigPath;
	
	private List<DiffExportEntry> fDiffEntries;
	
	public DiffExportPreprocessor(DipProject project, 
			Repository repo, 
			Path targetPath, 
			Path configPath,
			RevCommit revCommit1,
			RevCommit revCommit2) {
		fDipProject = project;
		fRepo = repo;
		fRevCommit1 = revCommit1;
		fRevCommit2 = revCommit2;
		fTargetPath = targetPath;
		fConfigPath = configPath;
	}

	public void doDiffExport() throws Exception {
		Path repoPath = fRepo.getDirectory().getParentFile().toPath();
		Path dipProjectPath = Path.of(fDipProject.resource().getLocationURI());
		Path projectRelativePath = repoPath.relativize(dipProjectPath);

		Git git = new Git(fRepo);
		List<DiffEntry> result = computeChanged(fRepo, git, fRevCommit1, fRevCommit2);
		git.close();

		DipRevisionProject project1 = new DipRevisionProject(fRepo, fRevCommit1, projectRelativePath);
		DipRevisionProject project2 = new DipRevisionProject(fRepo, fRevCommit2, projectRelativePath);

		fDiffEntries = new ArrayList<>();
		for (DiffEntry diffEntry : result) {
			DiffExportEntry exportEntry = createDiffExportEntry(diffEntry, projectRelativePath, project1, project2);
			fDiffEntries.add(exportEntry);
		}
		
		ExporterHolder.instance().getExporter().diffExport(fDipProject, fTargetPath, fConfigPath, this);
	}
	
	private DiffExportEntry createDiffExportEntry(DiffEntry diffEntry, Path projectRelativePath,
			DipRevisionProject revisionProject1, DipRevisionProject revisionProject2) {
		DiffExportEntry exportEntry = new DiffExportEntry();

		// first version
		String oldPath = diffEntry.getOldPath();
		if (oldPath != null && !oldPath.equals("/dev/null")) {
			UnitRevisionEntry entry = createRevisionEntry(oldPath, projectRelativePath, revisionProject1);
			exportEntry.setVersion1(entry);
		}

		// second version
		String newPath = diffEntry.getNewPath();
		if (newPath != null && !newPath.equals("/dev/null")) {
			UnitRevisionEntry entry = createRevisionEntry(newPath, projectRelativePath, revisionProject2);
			exportEntry.setVersion2(entry);
		}
		return exportEntry;
	}
	
	private UnitRevisionEntry createRevisionEntry(String path, Path projectRelativePath, DipRevisionProject revisionProject) {
		Path relativeRepoPath = Path.of(path);					
		Path relativeDipPath = projectRelativePath.relativize(relativeRepoPath);					
		UnitRevisionEntry entry = new UnitRevisionEntry(revisionProject, relativeRepoPath, relativeDipPath);					
		UnitType type = revisionProject.getType(entry);
		entry.setType(type);
		return entry;
	}
		
	private List<DiffEntry> computeChanged(Repository repo, Git git, RevCommit revCommit1, RevCommit revCommit2) {
		try {
			Set<Path> paths = Set.of(Paths.get(fDipProject.resource().getLocationURI()));
			return GITUtilities.computeChanged(repo, git, revCommit1, revCommit2, paths);		
		} catch (NoWorkTreeException | GitAPIException | IOException e) {
			e.printStackTrace();
			return new ArrayList<>();
		}
	}

	public List<DiffExportEntry> getDiffs(){
		return fDiffEntries;
	}

	public String firstCommit() {		
		return fRevCommit1.getName();
	}
	
	public String secondCommit() {
		return fRevCommit2.getName();
	}
	
	public DipProject getDipProject() {
		return fDipProject;
	}
	
}
