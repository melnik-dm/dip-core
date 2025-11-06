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

public class BoldAction extends Action{

	public static final String ID = Messages.BoldAction_ID;
	
	private static BoldAction instance = new BoldAction();
	
	private MarkdownDocument fMdDocument;
	
	public static BoldAction instance() {
		return instance;
	}

	private BoldAction() {
		setText(Messages.BoldAction_Name + " (Ctrl + B)");
		setAccelerator(SWT.CTRL + 'b');
		setId(ID);
		setImageDescriptor(ImageProvider.BOLD_DESCRIPTOR);
		setDisabledImageDescriptor(ImageProvider.DISABLE_BOLD_DESCRIPTOR);
		setChecked(true);			
	}
	
	//=================================
	// selection
	
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
								
			boolean isBold = isBold(relativeOffset, partitionText, type);		
			setChecked(isBold);
		} catch (BadLocationException e) {
			e.printStackTrace();
			return;
		}
	}
	
	public void updateFullSelection(MarkdownDocument mdDocument, Point selection) {
		fMdDocument = mdDocument;		
		boolean selectOnePartition  = fMdDocument.isSelectOnePartition(selection);
		if (!selectOnePartition) {
			updateBoldActionSelection(false, null);
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
			updateBoldActionSelection(false, null);
			return;
		} 		
		// случается при выделении мышкой всего текста
		if (selection.y < selection.x) {
			return;
		}		
		StyleRange[] ranges = fMdDocument.textWidget().getStyleRanges(selection.x, selection.y - selection.x);
		updateBoldActionSelection(true, ranges);
	}

	private void updateBoldActionSelection(boolean enable, StyleRange[] ranges ) {
		setEnabled(enable);
		if (!enable) {
			return;
		}
		boolean boldSelect = true;
		for (StyleRange range: ranges) {
			if ((range.fontStyle & SWT.BOLD) != SWT.BOLD){
				boldSelect = false;
			}
		}
		setChecked(boldSelect);
	}
	
	//==================================
	//  run
	
	@Override
	public void run() {
		run(!isChecked());
	}
	
	public void run(boolean isChecked) {
		if (isChecked) {
			doUnbold();
		} else {
			doBold();
		}
	}
	
	public boolean isBold(int offset, String content, String type) {
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
		return parser.isBold(indexPosition, offset);		
	}
	
	
	//=============================
	// bold
	
	private void doBold() {
		if (fMdDocument.hasSelection()){
			doBoldForSelection();
		} else {
			doBoldForCursor();
		}
		fMdDocument.fireMdDocumentUdpated();
	}
		
	private void doBoldForCursor() {
		Point selection = fMdDocument.selectionWord(); // выделение (слово, либо текст)
		// курсор стоит рядом с пробелом
		if (selection == null) {
			return;
		}					
		int caret = fMdDocument.cursorPosition();		
		try {
			String text = newContent(selection);
			fMdDocument.document().replace(selection.x, selection.y, text);			
			fMdDocument.setCursorPosition(caret + 2);
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
	}
	
	private String newContent(Point selection) throws BadLocationException {		
		if (MdUtilities.isAsteriskEmphasis(fMdDocument.document(), selection.x,  selection.x + selection.y)){
			return "**" + fMdDocument.document().get(selection.x, selection.y) + "**"; //$NON-NLS-1$ //$NON-NLS-2$
		} else {
			return "__" + fMdDocument.document().get(selection.x, selection.y) + "__"; //$NON-NLS-1$ //$NON-NLS-2$
		}
	}
	
	private void doBoldForSelection() {
		StyleCommandMarkdownParser parser = StyleCommandMarkdownParser.parserBoldSelection(fMdDocument);
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
		
	//================================
	// unbold
	
	private void doUnbold() {
		if (fMdDocument.hasSelection()){
			doUnboldForSelection();
		} else {
			doUnboldForCursor();
		}		
		fMdDocument.fireMdDocumentUdpated();
	}
	
	private void doUnboldForCursor() {		
		StyleCommandMarkdownParser parser = StyleCommandMarkdownParser.parserBoldCursor(fMdDocument);
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
	
	private void doUnboldForSelection() {	
		StyleCommandMarkdownParser parser = StyleCommandMarkdownParser.parserBoldSelection(fMdDocument);
		if (!parser.isValid()) {
			return;
		}		
		// выделенные позиции
		parser.selectedPositions();		
		// проверяем первую позицию на boldstart определяем маркер при необходимости		
		parser.checkFirstPosition();
		parser.defineFirstMarker();
		// проверяем последнюю позицию на boldend	определяем маркер при необходимости		
		parser.checkLastPosition();
		parser.defineLastMarker();
		// внутренние bold-маркеры
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
	
	public void setMdDocument(MarkdownDocument mdDocument) {
		fMdDocument = mdDocument;
	}
	
}
