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
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.swt.graphics.Point;

import ru.dip.editors.Messages;
import ru.dip.editors.md.MarkdownDocument;
import ru.dip.editors.md.partitions.PartitionStyles;
import ru.dip.ui.utilities.image.ImageProvider;

public class CodeAction extends Action {
	
	public static final String ID = Messages.CodeAction_ID;
	
	private static CodeAction instance = new CodeAction();
	
	public static CodeAction instance() {
		return instance;
	}
	
	private MarkdownDocument fMdDocument;
	private int fCaret;
	private Point fSelection;
	
	private CodeAction() {
		setText(Messages.CodeAction_Name);
		setImageDescriptor(ImageProvider.CODE_DESCRIPTOR);
		setId(ID);
		setChecked(true);
	}
	
	//=================================
	// selection
	
	public void updateEmptySelection(MarkdownDocument mdDocument, int caret) {
		fCaret = caret;
		fMdDocument = mdDocument;
		String partitionType = fMdDocument.partitionType(caret);
		setChecked(partitionType);
	}
	
	private void setChecked(String partitionType) {
		if (PartitionStyles.CODE.equals(partitionType)) {
			setChecked(true);
			setEnabled(true);
		} else if (isCanCodePartition(partitionType)) {
			setChecked(false);
			setEnabled(true);
		} else {
			setChecked(false);
			setEnabled(false);
		}
	}
	
	public void updateFullSelection(MarkdownDocument mdDocument, Point selection) {
		fMdDocument = mdDocument;
		fSelection = selection;
		boolean onePartition = fMdDocument.isSelectOnePartition(selection);
		if (onePartition) {
			String partitionType = fMdDocument.partitionType(selection.x);
			setChecked(partitionType);			
		} else {
			List<String> partitions = fMdDocument.getPartitionTypes(selection);
			boolean canCode = true;
			for (String type: partitions) {
				if (!isCanCodePartition(type)) {
					canCode = false;
					break;
				}
			}
			if (canCode) {
				setChecked(false);
				setEnabled(true);
			} else {
				setChecked(false);
				setEnabled(false);
			}
		}	
	}
	
	private boolean isCanCodePartition(String type) {
		return PartitionStyles.EMPTY_LINE.equals(type)
				|| PartitionStyles.isList(type)
				|| PartitionStyles.PARAGRAPH.equals(type);
	}
	
	//=================================
	// run
		
	@Override
	public void run() {
		if (fMdDocument.hasSelection()) {
			if (isChecked()) {
				doCodeSelection();
			} else {
				doUncodeSelection();
			}			
		} else {
			if (isChecked()) {
				doCode();
			} else {
				doUncode();
			}		
		}
		fMdDocument.fireMdDocumentUdpated();
	}
	
	private void doCode() {
		String newContent = "```\n\n```\n"; //$NON-NLS-1$
		if (!fMdDocument.isCurrentEmptyLine()) {
			newContent = "\n" + newContent; //$NON-NLS-1$
		}
		fMdDocument.textWidget().replaceTextRange(fCaret, 0, newContent);
		fMdDocument.setCursorPosition(fCaret + newContent.length() - 5);
	}
	
	private void doUncode() {
		String content = fMdDocument.partitionContent(fCaret);
		if (content.startsWith("```") && content.endsWith("```")) { //$NON-NLS-1$ //$NON-NLS-2$
			String newContent = content.substring(4, content.length() - 4);
			int offset = fMdDocument.partitionOffset(fCaret);	
			fMdDocument.textWidget().replaceTextRange(offset, content.length(), newContent);
			fMdDocument.setCursorPosition(fCaret - 4); 
		}
	}
	
	private void doCodeSelection(){
		try {
			IRegion line = fMdDocument.document().getLineInformationOfOffset(fSelection.x);
			String selectionContent = fMdDocument.textWidget().getText(fSelection.x, fSelection.y -1);
			StringBuilder builder = new StringBuilder();
			if (line.getOffset() != fSelection.x) {
				builder.append("\n"); //$NON-NLS-1$
			}
			builder.append("```\n"); //$NON-NLS-1$
			builder.append(selectionContent);
			if (!selectionContent.endsWith("\n")) { //$NON-NLS-1$
				builder.append("\n"); //$NON-NLS-1$
			}
			builder.append("```\n"); //$NON-NLS-1$
			fMdDocument.textWidget().replaceTextRange(fSelection.x, selectionContent.length(), builder.toString());
			fMdDocument.setSelection(new Point(fSelection.x, fSelection.x + builder.length()));
		
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
	}
	
	private void doUncodeSelection(){
		if (fMdDocument.isSelectOnePartition(fSelection)) {
			try {
				ITypedRegion region = fMdDocument.document().getPartition(fSelection.x);
				String content = fMdDocument.document().get(region.getOffset(), region.getLength());
				if (content.startsWith("```") && content.endsWith("```")) { //$NON-NLS-1$ //$NON-NLS-2$
					String newContent = content.substring(3, content.length() - 3);
					fMdDocument.document().replace(region.getOffset(), region.getLength(), newContent);
				}
			} catch (BadLocationException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void setMdDocument(MarkdownDocument mdDocument) {
		fMdDocument = mdDocument;
	}

}
