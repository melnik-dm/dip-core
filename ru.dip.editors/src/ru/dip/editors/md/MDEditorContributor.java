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
package ru.dip.editors.md;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.texteditor.BasicTextEditorActionContributor;

import ru.dip.core.utilities.WorkbenchUtitlities;
import ru.dip.editors.Messages;
import ru.dip.editors.dipview.DipView;
import ru.dip.editors.md.actions.AutoCorrectAction;
import ru.dip.editors.md.actions.AutoTextTransferAction;
import ru.dip.editors.md.actions.BoldAction;
import ru.dip.editors.md.actions.CodeAction;
import ru.dip.editors.md.actions.CommentAction;
import ru.dip.editors.md.actions.FormatBlockCodeAction;
import ru.dip.editors.md.actions.ItalicAction;
import ru.dip.editors.md.actions.LinkAction;
import ru.dip.editors.md.actions.MarkerListAction;
import ru.dip.editors.md.actions.NumberListAction;
import ru.dip.editors.md.actions.ParagraphAction;
import ru.dip.editors.md.comment.AddCommentAction;
import ru.dip.ui.preferences.MdPreferences;
import ru.dip.ui.utilities.image.ImageProvider;

public class MDEditorContributor extends BasicTextEditorActionContributor {

	private BoldAction fBoldAction = BoldAction.instance();
	private ItalicAction fItalicAction = ItalicAction.instance();
	private NumberListAction fNumberListAction = NumberListAction.instance();
	private MarkerListAction fMarkerListAction = MarkerListAction.instance();
	private LinkAction fLinkAction = LinkAction.instance();
	private CommentAction fCommentAction = CommentAction.instance();
	private CodeAction fCodeAction = CodeAction.instance();
	private ParagraphAction fParagraphAction = ParagraphAction.instance();
	private ShowCommentsAction fShowComment = new ShowCommentsAction();
	private AddCommentAction fAddCommentAction = AddCommentAction.instance();
	
	private MDEditor fMDEditor;
	
	@Override
	public void contributeToToolBar(IToolBarManager toolBarManager) {
		super.contributeToToolBar(toolBarManager);
		toolBarManager.add(new OpenViewAction());
		toolBarManager.add(new SyntaxHighlighting());
		toolBarManager.add(new AutoTextTransferAction());
		toolBarManager.add(new AutoCorrectAction());
		toolBarManager.add(new FormatBlockCodeAction());
		toolBarManager.add(fShowComment);
		toolBarManager.add(new Separator());
		toolBarManager.add(fBoldAction);
		toolBarManager.add(fItalicAction);
		toolBarManager.add(fCommentAction);
		toolBarManager.add(fNumberListAction);
		toolBarManager.add(fMarkerListAction);
		toolBarManager.add(fLinkAction);
		toolBarManager.add(fCodeAction);
		toolBarManager.add(fParagraphAction);
		toolBarManager.add(fAddCommentAction);
	}
	
	@Override
	public void setActiveEditor(IEditorPart part) {
		super.setActiveEditor(part);		
		fMDEditor = null;
		if (part instanceof MDEditor) {
			fMDEditor= (MDEditor) part;	
			MarkdownDocument mdDocument = fMDEditor.mdDocument();
			IAction formatAction = getAction(fMDEditor, Messages.MDEditorContributor_FormatActionName);
			IActionBars bars = this.getActionBars();
			bars.setGlobalActionHandler(Messages.MDEditorContributor_FormatActionID, formatAction);
			fBoldAction.setMdDocument(mdDocument);
			fItalicAction.setMdDocument(mdDocument);
			fNumberListAction.setMdDocument(mdDocument);
			fMarkerListAction.setMdDocument(mdDocument);
			fCommentAction.setMdDocument(mdDocument);
			fCodeAction.setMdDocument(mdDocument);
			fLinkAction.setMdDocument(mdDocument);
			fParagraphAction.setMdDocument(mdDocument);	
			fShowComment.setChecked(fMDEditor.getCommentManager().isShowComment());
		}			
	}
	
	//===============================
	// for edit view
	
	public void contributeEditView(IActionBars bars, IEditorPart editor) {
		if (bars == null) {
			return;
		}
		if (editor instanceof MDEditor) {
			fMDEditor = (MDEditor) editor;
			fShowComment.setChecked(fMDEditor.getCommentManager().isShowComment());
		}
		init(bars);	
		setActiveEditor(editor);
		IToolBarManager toolbar = bars.getToolBarManager();
		toolbar.remove(OpenViewAction.ID);
		toolbar.update(true);
	}
	
	/**
	 * Необходимо очищать тулбар при обновлении input в EditView
	 */
	public void disposeEditView(IActionBars bars) {
		if (bars != null) {		
			IToolBarManager toolbar = bars.getToolBarManager();
			toolbar.remove(BoldAction.ID);
			toolbar.remove(ItalicAction.ID);
			toolbar.remove(NumberListAction.ID);
			toolbar.remove(MarkerListAction.ID);
			toolbar.remove(LinkAction.ID);
			toolbar.remove(CommentAction.ID);
			toolbar.remove(CodeAction.ID);
			toolbar.remove(SyntaxHighlighting.ID);
			toolbar.remove(ParagraphAction.ID);
			toolbar.remove(AutoCorrectAction.ID);
			toolbar.remove(AutoTextTransferAction.ID);
			toolbar.remove(ShowCommentsAction.ID);
			toolbar.remove(AddCommentAction.ID);
			toolbar.remove(FormatBlockCodeAction.ID);
			toolbar.update(true);
		}
	}
	

	public static class OpenViewAction extends Action {
		
		public static final String ID = Messages.MDEditorContributor_OpenRenderViewActionID;
		
		public OpenViewAction() {
			setText(Messages.MDEditorContributor_OpenRenderActionName);
			setImageDescriptor(ImageProvider.RENDER);	
			setId(ID);
		}
		
		@Override
		public void run() {
			WorkbenchUtitlities.openView(DipView.ID);
			IEditorPart editorPart = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
			if (editorPart instanceof MDEditor){
				((MDEditor) editorPart).firePropertyChange(MDEditor.VISIBLE_EVENT);
			}
		}
	}
	
	public static class SyntaxHighlighting extends Action {
		
		public static final String ID = Messages.MDEditorContributor_HighligtEnableActionID;
		
		public SyntaxHighlighting() {
			setText(Messages.MDEditorContributor_SyntaxHighlightActionName);	
			setId(ID);
			setChecked(MdPreferences.highlghtEnable());
			setImageDescriptor(ImageProvider.SYNTAX_HIGHLIGHT_DESCRIPTOR);
		}
		
		@Override
		public void run() {
			boolean newValue = !MdPreferences.highlghtEnable();
			MdPreferences.instance().updateHighlightEnable(newValue);
			setChecked(newValue);
			MdPreferences.instance().fireListeners();
		}
		
	}
	
	private class ShowCommentsAction extends Action {
			
		private final static String ID = "ru.dip.action.show.comments.actions"; //$NON-NLS-1$
			
		private ShowCommentsAction() {
			setImageDescriptor(ImageProvider.COMMENT_DESCRIPTOR);
			setId(ID);
			setToolTipText(Messages.MDEditorContributor_ShowCommentsActionTitle);
			setChecked(false);
		}
		
		@Override
		public boolean isChecked() {
			if (fMDEditor == null) {
				return false;
			}
			return fMDEditor.getCommentManager().isShowComment();
		}
		
		@Override
		public void run() {
			if (fMDEditor != null) {
				fMDEditor.getCommentManager().showComment();
			}
			setChecked(!fMDEditor.getCommentManager().isShowComment());
		}
	}
	
}
