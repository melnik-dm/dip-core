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

import org.eclipse.jface.action.Action;
import org.eclipse.swt.graphics.Point;

import ru.dip.editors.Messages;
import ru.dip.editors.md.MarkdownDocument;
import ru.dip.ui.utilities.image.ImageProvider;

public class ParagraphAction extends Action {
	
	public static final String ID = Messages.ParagraphAction_ID;
	
	private static ParagraphAction instance = new ParagraphAction();
	
	public static ParagraphAction instance() {
		return instance;
	}
	
	private MarkdownDocument fMdDocument;
	private int fCaret;
	private Point fSelection;
	
	private ParagraphAction() {
		setText(Messages.ParagraphAction_Name);
		setId(ID);	
		setImageDescriptor(ImageProvider.PARAGRAPH_DESCRIPTOR);
	}
	
	//======================================
	// selection
		
	public void updateEmptySelection(MarkdownDocument mdDocument, int caret) {			
		fMdDocument = mdDocument;
		fCaret = caret;
		setEnabled(true);
	}
	
	public void updateFullSelection(MarkdownDocument mdDocument, Point selection) {
		fMdDocument = mdDocument;
		fSelection = selection;
		setEnabled(fMdDocument.isSelectOnePartition(selection));
	}
	
	//==================
	// run

	@Override
	public void run() {
		if (fMdDocument.hasSelection()) {
			String content = fMdDocument.textWidget().getText(fSelection.x, fSelection.y -1);
			StringBuilder builder = new StringBuilder();			
			addStartNewLine(builder, fSelection.x);
			int caretOffset = builder.length();
			builder.append(content);		
			addEndNewLine(builder, fSelection.y);		
			fMdDocument.textWidget().replaceTextRange(fSelection.x, 
					fSelection.y - fSelection.x, builder.toString());
			fMdDocument.setSelection(new Point(fSelection.x + caretOffset,
					fSelection.x + caretOffset + content.length()));
		} else {
			StringBuilder builder = new StringBuilder();
			addStartNewLine(builder, fCaret);
			int caretOffset = builder.length();
			addEndNewLine(builder, fCaret);
			fMdDocument.textWidget().replaceTextRange(fCaret, 0, builder.toString());
			fMdDocument.setCursorPosition(fCaret + caretOffset); 
		}		
	}

	private void addStartNewLine(StringBuilder builder, int offset) {
		if (offset == 0) {
			builder.append("\n"); //$NON-NLS-1$
		} else {
			String previous = fMdDocument.textWidget().getText(offset - 1, offset - 1);
			if ("\n".equals(previous)) {				 //$NON-NLS-1$
				int lineNumber = fMdDocument.textWidget().getLineAtOffset(offset - 1);
				String previousLine = fMdDocument.textWidget().getLine(lineNumber);
				if (previousLine != null && !previousLine.trim().isEmpty()) {
					builder.append("\n"); //$NON-NLS-1$
				}					
			} else {
				builder.append("\n\n"); //$NON-NLS-1$
			}				
		}
	}
	
	private void addEndNewLine(StringBuilder builder, int offset) {
		if (offset != fMdDocument.document().getLength()) {
			String next = fMdDocument.textWidget().getText(offset, offset);												
			if ("\n".equals(next)) { //$NON-NLS-1$
				int lineNumber = fMdDocument.textWidget().getLineAtOffset(offset + 1);
				String line = fMdDocument.textWidget().getLine(lineNumber);					
				if (line != null && !line.trim().isEmpty()) {		
				//if (!line.isBlank()) {
					builder.append("\n"); //$NON-NLS-1$
				}
			} else {
				builder.append("\n\n"); //$NON-NLS-1$
			}
		}
	}

	public void setMdDocument(MarkdownDocument mdDocument) {
		fMdDocument = mdDocument;
	}
}
