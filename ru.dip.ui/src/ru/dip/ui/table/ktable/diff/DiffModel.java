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
package ru.dip.ui.table.ktable.diff;

import static ru.dip.core.utilities.DnfoUtils.DNFO_FILENAME;
import static ru.dip.core.utilities.git.GitDiffUtils.getDiffPath;
import static ru.dip.core.utilities.git.GitDiffUtils.isNormalType;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.core.resources.IResource;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffEntry.ChangeType;
import org.eclipse.jgit.errors.NoWorkTreeException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;

import ru.dip.core.DipCorePlugin;
import ru.dip.core.model.DipTableContainer;
import ru.dip.core.model.interfaces.IDipElement;
import ru.dip.core.model.interfaces.IDipParent;
import ru.dip.core.model.interfaces.IDipDocumentElement;
import ru.dip.core.model.interfaces.IUnitExtension;
import ru.dip.core.utilities.GITUtilities;
import ru.dip.core.utilities.DipUtilities;
import ru.dip.ui.table.ktable.diff.model.ReqDiffStatus;
import ru.dip.ui.table.table.TableModel;

public class DiffModel extends TableModel {
	
	private IDipParent fOriginalContainer;
	private Repository fRepo;
	private String fCommitFromHistory;
	private Path fProjectFullPath;

	private Map<IDipElement, ReqDiffStatus> fDiffStatusByElement = new HashMap<>(); 

	public DiffModel(DipTableContainer dipTableContainer
			, Repository repo, String commitFromHistory) {
		super(dipTableContainer);
		fOriginalContainer = dipTableContainer;
		fRepo = repo;
		fCommitFromHistory = commitFromHistory;
		fProjectFullPath = Paths.get(fOriginalContainer.resource().getLocationURI());
	}
	
	public void computeDiffs() {
		fDiffStatusByElement.clear();
		Git git = new Git(fRepo);	
		// список staging
		Set<Path> paths = Set.of(Paths.get(getContainer().resource().getLocationURI()));
		List<DiffEntry> staging = stagingChanged(git, paths);
		// список изменений между HEAD  и указанным комитом	
		List<DiffEntry> diffs = computeChanged(git, paths);
		applyDiff(diffs);
		applyStagingDiff(staging);	
		git.close();
	}
	
	private List<DiffEntry> stagingChanged(Git git, Set<Path> paths){	
		try {			
			return GITUtilities.stagingChanged(git, fRepo, paths);
		} catch (NoWorkTreeException | GitAPIException e) {
			e.printStackTrace();
			return new ArrayList<>();
		}	
	}
	
	private List<DiffEntry> computeChanged(Git git,   Set<Path> paths) {
		try {
			ObjectId oldObject = git.getRepository().resolve(fCommitFromHistory);
			ObjectId newObject = git.getRepository().resolve("HEAD^{tree}");		 //$NON-NLS-1$
			return GITUtilities.computeChanged(git.getRepository(), git, oldObject, newObject, paths);		
		} catch (NoWorkTreeException | IOException | GitAPIException e) {
			e.printStackTrace();
			return new ArrayList<>();
		}
	}
	
	public void updateResource(IResource container) {
		Git git = new Git(fRepo);		
		Path resPath = Paths.get(container.getLocationURI());
		Set<Path> paths = Set.of(resPath);
		List<DiffEntry> stagings = stagingChanged(git, paths);
		List<DiffEntry> diffs = computeChanged(git, paths);
		clear(container);
		applyDiff(diffs);
		applyStagingDiff(stagings);				
		git.close();
	}
	
	public void updateResources(Set<IResource> containers) {
		Git git = new Git(fRepo);		
		Set<Path> paths = containers.stream().map(res -> Paths.get(res.getLocationURI())).collect(Collectors.toSet());
		List<DiffEntry> stagings = stagingChanged(git, paths);
		List<DiffEntry> diffs = computeChanged(git, paths);
		clear(containers);
		applyDiff(diffs);
		applyStagingDiff(stagings);				
		git.close();	
	}
	
	//=============================
	// apply
		
	private void applyDiff(List<DiffEntry> diffs) {	
		for (DiffEntry diff: diffs) {
			applyDiff(diff);
		}
	}
	
	private void applyDiff(DiffEntry diff) {	
		ChangeType type = diff.getChangeType();
		if (!isNormalType(type)) {
			DipCorePlugin.logError("AbstractDiffElement:setDiffStatus: " + "Новый тип ChangeType: " + type);
			return;
		}		
		String path = getDiffPath(diff);		
		// change type for description or comment
		if (path.endsWith(".d") || path.endsWith(".r")) { //$NON-NLS-1$ //$NON-NLS-2$
			type = ChangeType.MODIFY;
		}
		// apply type
		setDiffStatus(path, type);
	}
	
	private void setDiffStatus(String path, ChangeType type) {
		IDipElement element = findElement(path);
		if (element != null) {
			ReqDiffStatus status = ReqDiffStatus.fromChangeType(type, false);			
			fDiffStatusByElement.put(element, status);
		} 
	}
	
	
	private void applyStagingDiff(List<DiffEntry> stagingDiffs) {
		for (DiffEntry diff: stagingDiffs) {		
			applyStagingDiff(diff);
		}
	}
	
	private void applyStagingDiff(DiffEntry diff) {
		ChangeType type = diff.getChangeType();
		if (!isNormalType(type)) {
			return;
		}
		String path = getDiffPath(diff);
		if (path.endsWith(".d") || path.endsWith(".r")) { //$NON-NLS-1$ //$NON-NLS-2$
			type = ChangeType.MODIFY;
		}		
		setStagingStatus(path, type);
	}
	
	// сделать общий метод с setDiffStatus
	private void setStagingStatus(String path, ChangeType type) {
		IDipElement element = findElement(path);
		if (element != null) {
			ReqDiffStatus status = ReqDiffStatus.fromChangeType(type, true);			
			fDiffStatusByElement.put(element, status);
		} 
	}
	
	private IDipElement findElement(String diffPath){
		// для Description и комментариев
		if (diffPath.endsWith(".d") || diffPath.endsWith(".r")) { //$NON-NLS-1$ //$NON-NLS-2$
			diffPath = diffPath.substring(0, diffPath.length() - 2);
		}
		// для .dnfo - возвращаем папку
		if (diffPath.endsWith(DNFO_FILENAME)) { //$NON-NLS-1$
			diffPath = diffPath.substring(0, diffPath.length() - 5);

		}
		Path path = Paths.get(diffPath);	
		Path fullElementPath = fRepo.getDirectory().getParentFile().toPath().resolve(path);
		String relativePath = fProjectFullPath.relativize(fullElementPath).normalize().toString();
		IDipElement element = DipUtilities.findDiffElement(this, relativePath);
		return element;
	}
	
	//==============================
	// возвращает статус для отрисовки
	
	public ReqDiffStatus getDiffStatus(IDipElement element) {
		if (element instanceof IUnitExtension) {
			element = ((IUnitExtension) element).getDipUnit();
		}
		return fDiffStatusByElement.get(element);		
	}

	//================================
	// clear
	
	/**
	 * Очистить все элементы из этого узла
	 */
	public void clear(IResource container) {
		if (container.equals(fOriginalContainer.resource())) {
			fDiffStatusByElement.clear();
		} else {
			List<IDipElement> clearElements = fDiffStatusByElement.keySet().stream().filter((element) -> element
					.resource().getLocation().toOSString().startsWith(container.getLocation().toOSString()))
					.collect(Collectors.toList());
			fDiffStatusByElement.keySet().removeAll(clearElements);
		}
	}
	
	public void clear(Set<IResource> resources) {
		Set<String> resLocations = resources.stream().map(res -> res.getLocation().toOSString()).collect(Collectors.toSet());		
		List<IDipElement> clearElements =  fDiffStatusByElement.keySet().stream().filter(element -> {
			String elementLocation = element.resource().getLocation().toOSString();
			Optional<String> first = resLocations.stream().filter(str -> elementLocation.startsWith(str)).findFirst();						
			return first.isPresent();
		}).collect(Collectors.toList());
		fDiffStatusByElement.keySet().removeAll(clearElements);
	}
	
	//=============================
	// update
	
	@Override
	public void additionalUpdate(IDipDocumentElement dipDocElement) {	
		updateResource(dipDocElement.resource());			
	}
	
	@Override
	public void additionalUpdate(Set<IResource> resources) {
		updateResources(resources);
	}

}
