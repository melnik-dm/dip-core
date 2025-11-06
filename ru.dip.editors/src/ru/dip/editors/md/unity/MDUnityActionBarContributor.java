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
package ru.dip.editors.md.unity;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.part.EditorActionBarContributor;

import ru.dip.editors.md.MDEditorContributor.SyntaxHighlighting;
import ru.dip.editors.md.actions.AutoCorrectAction;
import ru.dip.editors.md.actions.AutoTextTransferAction;
import ru.dip.editors.md.actions.BoldAction;
import ru.dip.editors.md.actions.CodeAction;
import ru.dip.editors.md.actions.CommentAction;
import ru.dip.editors.md.actions.ItalicAction;
import ru.dip.editors.md.actions.LinkAction;
import ru.dip.editors.md.actions.MarkerListAction;
import ru.dip.editors.md.actions.NumberListAction;
import ru.dip.editors.md.actions.ParagraphAction;
import ru.dip.editors.md.unity.action.AddUnityCommentAction;
import ru.dip.ui.utilities.image.ImageProvider;

public class MDUnityActionBarContributor extends EditorActionBarContributor {

	private UnityMdEditor fUnityMdEditor;

	private ShowCommentsAction fShowComment = new ShowCommentsAction();
	private AddUnityCommentAction fAddCommentAction = AddUnityCommentAction.instance();

	@Override
	public void contributeToToolBar(IToolBarManager toolBarManager) {
		super.contributeToToolBar(toolBarManager);
		toolBarManager.add(new SyntaxHighlighting());
		toolBarManager.add(new AutoTextTransferAction());
		toolBarManager.add(new AutoCorrectAction());
		toolBarManager.add(fShowComment);

		toolBarManager.add(BoldAction.instance());
		toolBarManager.add(ItalicAction.instance());
		toolBarManager.add(CommentAction.instance());
		toolBarManager.add(NumberListAction.instance());
		toolBarManager.add(MarkerListAction.instance());
		toolBarManager.add(LinkAction.instance());
		toolBarManager.add(CodeAction.instance());
		toolBarManager.add(ParagraphAction.instance());
		toolBarManager.add(fAddCommentAction);
	}

	@Override
	public void contributeToMenu(IMenuManager menuManager) {
		super.contributeToMenu(menuManager);
	}

	@Override
	public void setActiveEditor(IEditorPart targetEditor) {
		IActionBars actionBars = getActionBars();
		if (actionBars != null && targetEditor instanceof UnityMdEditor) {
			fUnityMdEditor = (UnityMdEditor) targetEditor;

			actionBars.setGlobalActionHandler(ActionFactory.UNDO.getId(), fUnityMdEditor.getUndoAction());
			actionBars.setGlobalActionHandler(ActionFactory.REDO.getId(), fUnityMdEditor.getRedoAction());

			BoldAction.instance().setEnabled(false);
			ItalicAction.instance().setEnabled(false);
			CommentAction.instance().setEnabled(false);
			CodeAction.instance().setEnabled(false);
			NumberListAction.instance().setEnabled(false);
			MarkerListAction.instance().setEnabled(false);
			LinkAction.instance().setEnabled(false);
			ParagraphAction.instance().setEnabled(false);
		}
	}

	private class ShowCommentsAction extends Action {

		private final static String ID = "ru.dip.action.show.comments.actions2";

		private ShowCommentsAction() {
			setImageDescriptor(ImageProvider.COMMENT_DESCRIPTOR);
			setId(ID);
			setToolTipText("Show Comments");
			setChecked(false);
		}

		@Override
		public boolean isChecked() {
			if (fUnityMdEditor == null) {
				return false;
			}
			return !fUnityMdEditor.isShowComment();
		}

		@Override
		public void run() {
			if (fUnityMdEditor != null) {
				fUnityMdEditor.showComment();
			}
		}

	}

}
