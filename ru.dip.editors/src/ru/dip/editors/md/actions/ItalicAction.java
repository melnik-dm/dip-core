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
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.graphics.Point;

import ru.dip.core.exception.MarkdownCommandException;
import ru.dip.core.utilities.md.MarkdownParagraphParser;
import ru.dip.core.utilities.md.MdUtilities;
import ru.dip.editors.Messages;
import ru.dip.editors.md.MarkdownDocument;
import ru.dip.editors.md.partitions.PartitionStyles;
import ru.dip.ui.utilities.image.ImageProvider;

public class ItalicAction extends Action{

	public static final String ID = Messages.ItalicAction_ID;
	
	private static ItalicAction instance = new ItalicAction();
	
	private MarkdownDocument fMdDocument;

	public static ItalicAction instance() {
		return instance;
	}
	
	private ItalicAction() {
		setText(Messages.ItalicAction_Name + " (Ctrl + I)");
		setAccelerator(SWT.CTRL + 'i');
		setId(ID);
		setImageDescriptor(ImageProvider.ITALIC_DESCRIPTOR);
		setDisabledImageDescriptor(ImageProvider.DISABLE_ITALIC_DESCRIPTOR);
		setChecked(true);			
	}
	
	public boolean isItalic(int offset, String content, String type) {
		MarkdownParagraphParser parser = new MarkdownParagraphParser();
		if (PartitionStyles.isList(type)) {
			parser.parseListItem(content);
		} else {
			parser.parseParagraph(content);
		}
		int indexPosition = parser.findPosition(offset);
		if (indexPosition < 0) {
			return false;
		}
		return parser.isItalic(indexPosition, offset);		
	}
	
	public void updateEmptySelection(MarkdownDocument mdDocument, int caret) {
		fMdDocument = mdDocument;
		try {
			ITypedRegion region = fMdDocument.document().getPartition(caret);			
			String type = region.getType();
						
			if (!PartitionStyles.hasStyles(type)) {
				setEnabled(false);
				return;
			} else {
				setEnabled(true);
			}
			
			int partitionOffset = region.getOffset();
			int relativeOffset = caret - partitionOffset;
			String partitionText = fMdDocument.document().get(region.getOffset(), region.getLength());
								
			boolean isItalic = isItalic(relativeOffset, partitionText, type);		
			setChecked(isItalic);		
		} catch (BadLocationException e) {
			e.printStackTrace();
			return;
		}
	}
	
	public void updateFullSelection(MarkdownDocument mdDocument, Point selection) {
		fMdDocument = mdDocument;
		boolean selectOnePartition  = fMdDocument.isSelectOnePartition(selection);
		if (!selectOnePartition) {
			updateItalicActionSelection(false, null);
			return;
		}
			
		String type = null;
		try {
			ITypedRegion region = fMdDocument.document().getPartition(selection.x);
			type = region.getType();
		} catch (BadLocationException e) {
			e.printStackTrace();
			return;
		}
		
		if (!PartitionStyles.hasStyles(type)) {
			updateItalicActionSelection(false, null);
			return;
		}
		
		// случается при выделении мышкой всего текста
		if (selection.y < selection.x) {
			return;
		}
		StyleRange[] ranges = fMdDocument.textWidget().getStyleRanges(selection.x, selection.y - selection.x);
		updateItalicActionSelection(true, ranges);
	}

	private void updateItalicActionSelection(boolean enable, StyleRange[] ranges ) {
		if (CommentAction.instance().isEnabled() && CommentAction.instance().isChecked()) {
			setEnabled(false);
			return;
		}
		
		setEnabled(enable);
		if (!enable) {			
			return;
		}
	
		boolean italicSelect = true;	
		for (StyleRange range: ranges) {
			if ((range.fontStyle & SWT.ITALIC) != SWT.ITALIC){
				italicSelect = false;
			}
		}
		setChecked(italicSelect);
	}
	
	//============================
	// run
	
	@Override
	public void run() {
		run(!isChecked());
	}
	
	public void run(boolean isChecked) {
		if (isChecked) {
			doUnItalic();
		} else {
			doItalic();
		}
	}
	
	//=============================
	// italic
	
	private void doItalic() {
		if (fMdDocument.hasSelection()){
			doItalicForSelection();
		} else {
			doItalicForCursor();
		}
		fMdDocument.fireMdDocumentUdpated();
	}
	
	private void doItalicForSelection() {		
		StyleCommandMarkdownParser parser = StyleCommandMarkdownParser.parserItalicSelection(fMdDocument);
		if (!parser.isValid()) {
			return;
		}		
		// выделенные позиции
		parser.selectedPositions();			
		parser.defineOutsideStartMarker();
		parser.defineOutsideEndMarker();
		parser.defineInnerMarkers();

		try {
			parser.getSelectionContent();
			parser.defineCommandEndOffset();
			parser.defineCommandStartOffset();
			parser.defineNewCommandContent();
			parser.replace();
		} catch (BadLocationException | MarkdownCommandException e) {
			e.printStackTrace();
		}			
	}
	
	private void doItalicForCursor() {
		Point selection = fMdDocument.selectionWord();
		if (selection == null) {
			return;
		}				
		int caret = fMdDocument.cursorPosition();		
		try {
			String text = newContent(selection);
			fMdDocument.document().replace(selection.x, selection.y, text);			
			fMdDocument.setCursorPosition(caret + 1);
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
	}
	
	private String newContent(Point selection) throws BadLocationException {		
		if (MdUtilities.isAsteriskEmphasis(fMdDocument.document(), selection.x,  selection.x + selection.y)){
			return "*" + fMdDocument.document().get(selection.x, selection.y) + "*"; //$NON-NLS-1$ //$NON-NLS-2$
		} else {
			return "_" + fMdDocument.document().get(selection.x, selection.y) + "_"; //$NON-NLS-1$ //$NON-NLS-2$
		}
	}
	
	//==============================
	// unItalic
	
	private void doUnItalic() {
		if (fMdDocument.hasSelection()){
			doUnItalicForSelection();
		} else {
			doUnItalicForCursor();
		}			
		fMdDocument.fireMdDocumentUdpated();
	}
	
	private void doUnItalicForSelection() {	
		StyleCommandMarkdownParser parser = StyleCommandMarkdownParser.parserItalicSelection(fMdDocument);
		if (!parser.isValid()) {
			return;
		}		
		// выделенные позиции
		parser.selectedPositions();		
		// проверяем первую позицию на italicstart определяем маркер при необходимости		
		parser.checkFirstPosition();
		parser.defineFirstMarker();
		// проверяем последнюю позицию на italicend	определяем маркер при необходимости		
		parser.checkLastPosition();
		parser.defineLastMarker();
		// внутренние italic-маркеры
		parser.defineInnerMarkers();		
		// end-start offset
		parser.defineUnCommandEndOffset();
		parser.defineUnCommandStartOffset();		
		try {			
			parser.defineNewUnCommandContent();
			parser.replace();
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
	}
	
	private void doUnItalicForCursor() {			
		StyleCommandMarkdownParser parser = StyleCommandMarkdownParser.parserItalicCursor(fMdDocument);
		parser.definePositionsForCursor();
		if (!parser.hasStartEndPositions()) {
			return;
		}
		parser.defineUnCommandCursorStartOffset();
		parser.defineUnCommandCursorEndOffset();
		
		try {
			parser.defineNewUnCursorCommandContent();
			parser.replaceForCursor();

		} catch (BadLocationException e) {
			e.printStackTrace();
		}				
	}
	
	public void setMdDocument(MarkdownDocument mdDocument) {
		fMdDocument = mdDocument;
	}

}
