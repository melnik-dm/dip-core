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
package ru.dip.editors.md.actions;

import org.eclipse.jface.text.IDocument;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Point;

import ru.dip.editors.md.IMdEditor;
import ru.dip.editors.md.MarkdownDocument;
import ru.dip.editors.md.comment.AddCommentAction;

public class MdActionUpdater {
	
	private final IMdEditor fMdEditor;
	
	public MdActionUpdater(IMdEditor editor) {
		fMdEditor = editor;
	}
		
	public void updateActionStatus() {
		StyledText text = getStyledText(); 			
		Point p = text.getSelection();
		if (p.x == p.y) {
			int caret = text.getCaretOffset();
			updateEmptySelection(caret);
		} else {
			updateSelection(p);
		}
	}
		
	private void updateEmptySelection(int caret) {
		if (caret > getDocument().getLength()) {
			caret = getDocument().getLength();
		}	
		BoldAction.instance().updateEmptySelection(getMdDocument(), caret);
		ItalicAction.instance().updateEmptySelection(getMdDocument(), caret);		
		CommentAction.instance().updateEmptySelection(getMdDocument(), caret);
		CodeAction.instance().updateEmptySelection(getMdDocument(), caret);		
		NumberListAction.instance().updateEmptySelection(getMdDocument(), caret);
		MarkerListAction.instance().updateEmptySelection(getMdDocument(), caret);
		LinkAction.instance().updateEmptySelection(getMdDocument(), caret);
		ParagraphAction.instance().updateEmptySelection(getMdDocument(), caret);
		AddCommentAction.instance().updateEmptySelection();
	}
	
	private void updateSelection(Point selection) {
		if (selection.y > getDocument().getLength()) {
			selection.y = getDocument().getLength();
		}
		CodeAction.instance().updateFullSelection(getMdDocument(), selection);
		NumberListAction.instance().updateFullSelection(getMdDocument(), selection);
		MarkerListAction.instance().updateFullSelection(getMdDocument(), selection);		
		LinkAction.instance().updateFullSelection(getMdDocument(), selection);
		ParagraphAction.instance().updateFullSelection(getMdDocument(), selection);			
		CommentAction.instance().updateFullSelection(getMdDocument(), selection);
		BoldAction.instance().updateFullSelection(getMdDocument(), selection);
		ItalicAction.instance().updateFullSelection(getMdDocument(), selection);
		AddCommentAction.instance().updateFullSelection();
	}
	
	protected StyledText getStyledText() {
		return fMdEditor.styledText();
	}
	
	protected IDocument getDocument() {
		return fMdEditor.document();
	}
	
	protected MarkdownDocument getMdDocument() {
		return fMdEditor.mdDocument();
	}
	
}
