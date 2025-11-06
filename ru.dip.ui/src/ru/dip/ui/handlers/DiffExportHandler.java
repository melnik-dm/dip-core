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
package ru.dip.ui.handlers;

import java.nio.file.Path;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IResource;
import org.eclipse.egit.ui.internal.history.HistoryPageInput;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.team.internal.ui.history.GenericHistoryView;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.handlers.HandlerUtil;

import ru.dip.core.model.DipProject;
import ru.dip.core.utilities.DipUtilities;
import ru.dip.core.utilities.WorkbenchUtitlities;
import ru.dip.ui.export.diff.DiffExporter;

public class DiffExportHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
	
		IStructuredSelection selection = HandlerUtil.getCurrentStructuredSelection(event);
		Object[] objs = selection.toArray();
		if (objs.length != 2) {
			return null;
		}

		RevCommit commit1 = (RevCommit) objs[0];
		RevCommit commit2 = (RevCommit) objs[1];

		HistoryPageInput hpi = getHistoryInput(event);
		Repository repo = hpi.getRepository();
		DipProject dipProject = getDipProject(hpi, repo);
		
		if (dipProject == null) {
			WorkbenchUtitlities.openError("Diff Export Error", "Не найден DIP-проект в репозитории: " + repo.getDirectory().getParentFile().toPath());
			return null;
		}
		
		Shell shell = HandlerUtil.getActiveShell(event);				
		
		DiffExporter diffExporter = new DiffExporter(repo, dipProject, commit1, commit2, shell);
		diffExporter.doExport();		
		return null;
	}
	
	private HistoryPageInput getHistoryInput(ExecutionEvent event) {
		IWorkbenchPart part = HandlerUtil.getActivePart(event);
		GenericHistoryView historyView = (GenericHistoryView) part;	
		Object input = historyView.getHistoryPage().getInput();
		return (HistoryPageInput) input;
	}
	
	private DipProject getDipProject(HistoryPageInput hpi, Repository repo) {
		Object obj = hpi.getSingleItem();
		if (obj instanceof IResource) {
			return DipUtilities.findDipProject((IResource) obj);
		}	
		// если корень репозитория не является DIP-проектом, берем первый найденный в данной папке
		// если в репозитории нескольлко проектов?
		Path repoPath = repo.getDirectory().getParentFile().toPath();		
		List<DipProject> projects = DipUtilities.findProjectsInFolder(repoPath);
		if (projects != null && !projects.isEmpty()) {
			return projects.get(0);
		}				
		return null;
	}
	
}
