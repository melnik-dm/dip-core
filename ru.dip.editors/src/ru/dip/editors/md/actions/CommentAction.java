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

import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.swt.graphics.Point;

import ru.dip.editors.Messages;
import ru.dip.editors.md.MarkdownDocument;
import ru.dip.editors.md.model.MDNode;
import ru.dip.ui.utilities.image.ImageProvider;

public class CommentAction extends Action {
	
	public static final String ID = Messages.CommentAction_ID;
	
	private static CommentAction instance = new CommentAction();
	
	public static CommentAction instance() {
		return instance;
	}
	
	private MarkdownDocument fMdDocument;
	
	private CommentAction() {
		setText(Messages.CommentAction_Name);
		setImageDescriptor(ImageProvider.COMMENT_EDIT_DESCRIPTOR);
		setId(ID);	
		setChecked(true);			
	}
	
	//=================================
	// selection
	
	public void updateEmptySelection(MarkdownDocument mdDocument, int caret) {	
		fMdDocument = mdDocument;
		MDNode node = fMdDocument.mdModel().findNode(caret);
		if (node == null) {
			setEnabled(false);
			return;
		} 
		setChecked(node.isComment());
		setEnabled(node.canComment());
	}
	
	public void updateFullSelection(MarkdownDocument mdDocument, Point p) {
		fMdDocument = mdDocument;
		List<MDNode> selectedNodes = fMdDocument.mdModel().getNotEmptySelectedNodes(p);
		if (selectedNodes.size() == 1) {
			MDNode node = selectedNodes.get(0);
			setChecked(node.isComment());
			setEnabled(node.canComment());
			return;
		}
		
		for (MDNode node: selectedNodes) {
			if (node.isComment()) {
				setEnabled(false);		
				return;
			}
		}
		
		setEnabled(true);
		setChecked(false);
	}
	
	//==================================
	//  run
	
	@Override
	public void run() {
		if (!isChecked()) {
			doUncommentSelection();
		} else {
			doCommentSelection();
		}
		fMdDocument.fireMdDocumentUdpated();
	}
	
	private void doCommentSelection() {
		Point selection = fMdDocument.getSelection();
		Point point = selection;
		if (point.x == point.y) {
			point = fMdDocument.selectionWord();
			if (point == null) {	
				doEmptyComment(selection.x);				
				return;
			}
			point.y = point.x + point.y;
		}			
		try {
			String selectionContent = fMdDocument.document().get(point.x, point.y - point.x);
			int cursor = fMdDocument.cursorPosition();		
			if (cursor >= point.x && cursor <= point.y) {
				cursor = cursor + 4;
			}			
			fMdDocument.document().replace(point.x, point.y - point.x, "<!--" + selectionContent + "-->"); //$NON-NLS-1$ //$NON-NLS-2$
			fMdDocument.setCursorPosition(cursor);
		} catch (BadLocationException e) {
			e.printStackTrace();
		}		
	}
	
	private void doEmptyComment(int offset) {
		try {					
			fMdDocument.document().replace(offset, 0, "<!--  -->"); //$NON-NLS-1$
			fMdDocument.setCursorPosition(offset + 5);
		} catch (BadLocationException e) {
			e.printStackTrace();
		}	
	}
	
	private void doUncommentSelection() {				
		try {
			Point selection = fMdDocument.getSelection();		
			Point p = fMdDocument.isSelectOneNotEmptyPartition(selection);		
			
			if (p == null) {
				return;
			}
			
			MDNode node = fMdDocument.mdModel().findNode(p);
			String commentContent = node.content();
			String newContent = uncommentContent(commentContent);

			int cursor = fMdDocument.cursorPosition();
			if (cursor >= node.offset() && cursor <= node.offset() + node.length()) {
				cursor = cursor - commentContent.indexOf(newContent);
			}
					
			fMdDocument.document().replace(node.offset(), node.length(), newContent);				
			if (cursor >= 0) {
				fMdDocument.setCursorPosition(cursor);
			}		
		} catch (BadLocationException e) {
			e.printStackTrace();
		}		
	}
	
	private String uncommentContent(String commentContent) {	
		String trimContent = commentContent.trim();		
		return trimContent.substring(4, trimContent.length() - 3);
	}
	
	public void setMdDocument(MarkdownDocument mdDocument) {
		fMdDocument = mdDocument;
	}
}
