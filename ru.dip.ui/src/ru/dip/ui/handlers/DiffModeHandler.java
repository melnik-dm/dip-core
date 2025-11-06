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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffEntry.ChangeType;
import org.eclipse.jgit.errors.CorruptObjectException;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.errors.RevisionSyntaxException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.PathFilter;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;

import ru.dip.core.DipCorePlugin;
import ru.dip.core.model.DipTableContainer;
import ru.dip.core.model.interfaces.IDipParent;
import ru.dip.core.utilities.GITUtilities;
import ru.dip.ui.table.editor.DipTableEditor;
import ru.dip.ui.table.ktable.diff.DiffModel;

public class DiffModeHandler extends AbstractHandler {
		
	private static final String SHOW_ONLY_PARAMETER = "ru.dip.diff.mode.paremeter.show_only_chaging";
	private static final String COMMIT_PARAMETER = "ru.dip.diff.mode.paremeter.commit";
	private static final String HEAD_VALUE = "head";
	private static final String ORIGIN_HEAD_VALUE = "origin_head";
	
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {			
		// текущий редактор
		DipTableEditor editor = activeTableEditor();
		if (editor == null) {
			DipCorePlugin.logError("DiffModeHandler: Not found editor");
			return null;
		}		
		// текущий репозиторий
		IDipParent container = editor.model().getContainer();
		Repository repo = GITUtilities.findRepo(container.resource());		
		if (repo == null) {
			DipCorePlugin.logError("DiffModeHandler: Not found repo");
			return null;
		}
		// read parameters
		boolean onlyShow = onlyShow(event);
		String commit = getCommitParameter(event);		
		// execute
		if (HEAD_VALUE.equals(commit)){
			executeForHead(editor, container, repo, onlyShow);
		} else if (ORIGIN_HEAD_VALUE.equals(commit)) {
			executeForOriginHead(editor, container, repo, onlyShow);
		} else {
			executeForSelectCommit(event, editor, container, repo, onlyShow);
		}
		return null;
	}
	
	private void executeForHead(DipTableEditor editor,
			IDipParent container, Repository repo, boolean onlyShow) {
		editor.kTable().setOnlyDiffMode(onlyShow);
		createDiffTableModel(editor, container, repo, "HEAD^{tree}");
	}
	
	private void executeForOriginHead(DipTableEditor editor,
			IDipParent container, Repository repo, boolean onlyShow) {
		editor.kTable().setOnlyDiffMode(onlyShow);
		createDiffTableModel(editor, container, repo, "FETCH_HEAD^{tree}");
	}
	
	private void executeForSelectCommit(ExecutionEvent event, DipTableEditor editor,
			IDipParent container, Repository repo, boolean onlyShow) {
		// комит из history
		RevCommit commitFromHistory = selectionRevCommit(event);
		if (commitFromHistory == null) {
			return;
		}		
		if (!isOneRepo(repo, commitFromHistory)) {
			return;
		}		
		editor.kTable().setOnlyDiffMode(onlyShow);
		createDiffTableModel(editor, container, repo, commitFromHistory.getTree().getName());
	}
		
	private void createDiffTableModel(DipTableEditor editor, IDipParent container, Repository repo, String commitName ) {
		DiffModel model = new DiffModel((DipTableContainer)container,repo, commitName);
		model.computeDiffs();
		editor.kTable().setDiffMode(true);
		editor.setNewModel(model);
		editor.kTable().updateBackgrouColor();
		editor.kTable().applyListMode();
	}
	
	private boolean onlyShow(ExecutionEvent event) {
		String value = event.getParameter(SHOW_ONLY_PARAMETER);				 //$NON-NLS-1$
		return "true".equals(value); //$NON-NLS-1$
	}
	
	private String getCommitParameter(ExecutionEvent event) {
		return event.getParameter(COMMIT_PARAMETER);				 //$NON-NLS-1$			
	}
	
	/**
	 * Получаем такой же коммит из репозитория открытого документа
	 * Если совпадают значит в hitsory и Document один репозиторий
	 */
	private boolean isOneRepo(Repository repo, RevCommit commitFromHistory){
		try {
			ObjectId id = repo.resolve(commitFromHistory.name());
			RevCommit commitFromRepo = repo.parseCommit(id);
			return(commitFromRepo.equals(commitFromHistory));
		} catch (RevisionSyntaxException | IOException e) {
			e.printStackTrace();
			return false;
		}			
	}
		
	/**
	 * Возвращает коммит из History
	 */
	private RevCommit selectionRevCommit(ExecutionEvent event) {
		IStructuredSelection selection = HandlerUtil.getCurrentStructuredSelection(event);
		if (selection == null || selection.isEmpty()) {
			return null;
		}
		if (selection.getFirstElement() instanceof RevCommit) {
			return (RevCommit) selection.getFirstElement();
		}	
		return null;
	}
	
	/*
	 * Метод, чтобы прочитать днфо
	 * Чтобы сравнить днфо, определить что именно изменилось (описание, порядок файлов и т.д.)
	 */
	@SuppressWarnings("unused")
	private void readDnfo(Repository repo, RevCommit oldCommit, List<DiffEntry> diffs2) throws MissingObjectException, IncorrectObjectTypeException, CorruptObjectException, IOException {
		for (DiffEntry diff : diffs2) {
			System.out.println("diff: " + diff.getNewPath() + "   " + diff.getOldPath()); //$NON-NLS-1$ //$NON-NLS-2$
			ChangeType type = diff.getChangeType();
			System.out.println(type.name());
			
			
			 RevTree tree = oldCommit.getTree();
			 TreeWalk treeWalk = new TreeWalk(repo);
			 treeWalk.addTree(tree);
			 treeWalk.setRecursive(true);
			 treeWalk.setFilter(PathFilter.create(diff.getOldPath()));
			 if (!treeWalk.next()) {
				 System.out.println("ERROR"); //$NON-NLS-1$
			 } else {
				 System.out.println("HAS NEXT"); //$NON-NLS-1$
				 System.out.println("================ dnfo ================"); //$NON-NLS-1$
				 ObjectId objectId = treeWalk.getObjectId(0);
				 ObjectLoader loader = repo.open(objectId);
				 InputStream in = loader.openStream();
				 BufferedReader reader2 = new BufferedReader(new InputStreamReader(in));
				 String line = reader2.readLine();
				 while(line != null) {
					 line = reader2.readLine();
				 }
				 reader2.close();
			 }
			 treeWalk.close();		 
		}
	}
	
	/**
	 * Возвращает активный DipTableEditor	
	 */
	private DipTableEditor activeTableEditor() {
		IEditorPart editPart = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
		if (editPart instanceof DipTableEditor) {
			return (DipTableEditor) editPart;
		}
		return null;
	}
}
