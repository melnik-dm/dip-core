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

public class LinkAction extends Action {
	
	public static final String ID = Messages.LinkAction_ID;
	
	private static LinkAction instance = new LinkAction();
	
	public static LinkAction instance() {
		return instance;
	}
	
	private MarkdownDocument fMdDocument;
	private int fCaret;
	private Point fSelection;
	
	private LinkAction() {
		setText(Messages.LinkAction_Name);
		setId(ID);	
		setImageDescriptor(ImageProvider.LINK_DESCRIPTOR);
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
			StringBuilder newContent = new StringBuilder();
			newContent.append("["); //$NON-NLS-1$
			newContent.append(content);
			newContent.append("]()"); //$NON-NLS-1$
			fMdDocument.textWidget().replaceTextRange(fSelection.x, 
					fSelection.y - fSelection.x, newContent.toString());
			fMdDocument.setCursorPosition(fSelection.x + newContent.length() - 1); 
		} else {
			fMdDocument.textWidget().replaceTextRange(fCaret, 0, "[]()"); //$NON-NLS-1$
			fMdDocument.setCursorPosition(fCaret + 1); 
		}		
	}
	
	public void setMdDocument(MarkdownDocument mdDocument) {
		fMdDocument = mdDocument;
	}
}
