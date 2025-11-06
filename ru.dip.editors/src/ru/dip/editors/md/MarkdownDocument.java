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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Point;

import ru.dip.editors.md.model.MdDocumentModel;
import ru.dip.editors.md.partitions.PartitionStyles;

public class MarkdownDocument {
	
	public static interface MdDocumentListener {
		
		public void mdDocumentUpdated();
	}
	
	private List<MdDocumentListener> fListeners = new ArrayList<>();	
	private StyledText fText;
	private IDocument fDocument;
	private MdDocumentModel fMdModel;
	
	public MarkdownDocument(StyledText text, IDocument document, MdDocumentModel model) {
		fText = text;
		fDocument = document;	
		fMdModel = model;
	}
	
	/**
	 * Выделен ли текст
	 */
 	public boolean hasSelection() {
		Point p = fText.getSelection();
		return p != null &&  p.x != p.y;
	}
 	
	public Point getSelection() {
		return fText.getSelection();
	}
	
	public void setSelection(Point p) {
		fText.setSelection(p);
	}
 	
 	/**
	 * Возвращает выделение,
	 * Если нет выделения и курсор стоит в в середине слова, то возвращает слово.
	 * В остальных случаях возвращает null.
	 */
	public Point selectionWord() {
		Point p = fText.getSelection();
		if (p.x != p.y) {
			return p;
		}
		
		int offset = fText.getCaretOffset();
		
		if (offset == 0 && offset == fText.getText().length() -1) {
			return new Point(fText.getCaretOffset(), 0);			
		}
		try {
			char previous = fDocument.getChar(offset -1);		
			if (Character.isSpaceChar(previous) || '\n' == previous) {
				return null;
			}
			char next = fDocument.getChar(offset);
			if (Character.isSpaceChar(next) || '\n' == next) {
				return null;
			}
			
			int x = offset -1;
			for (int i = offset - 2; i >= 0; i--) {
				char ch = fDocument.getChar(i);
				if (Character.isSpaceChar(ch) || '\n' == ch) {
					break;
				}	
				x = i;
			}
			
			int y = offset;
			for (int i = offset + 1; i < fText.getText().length(); i++) {
				char ch = fDocument.getChar(i);
				if (Character.isSpaceChar(ch) || '\n' == ch) {
					break;
				}	
				y = i;
			}		
			return new Point(x, y - x + 1);			
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
		return new Point(fText.getCaretOffset(), 0);
	}
	
	public int cursorPosition() {
		return fText.getCaretOffset();
	}
	
	public void setCursorPosition(int offset) {
		fText.setCaretOffset(offset);
	}
	
	
	public String partitionType(int caret) {		 
		try {
			ITypedRegion region = fDocument.getPartition(caret);			
			return region.getType();
		} catch (BadLocationException e) {
			e.printStackTrace();
		}	
		return null;
	}
	
	public String partitionContent(int caret) {
		try {
			ITypedRegion region = fDocument.getPartition(caret);			
			return  fDocument.get(region.getOffset(), region.getLength());		
		} catch (BadLocationException e) {
			e.printStackTrace();
			return null;
		}
	}
	
 	
	public int partitionOffset(int caret) {		 
		try {
			ITypedRegion region = fDocument.getPartition(caret);			
			return region.getOffset();
		} catch (BadLocationException e) {
			e.printStackTrace();
		}	
		return -1;
	}
	
	public boolean isSelectOnePartition(Point selection) {
		try {
			ITypedRegion region = fDocument.getPartition(selection.x);
			// если выделено до конца документа
			int y = selection.y;
			if (selection.y == fDocument.getLength()) {
				y = y - 2;
			}
			ITypedRegion region2 = fDocument.getPartition(y);
			return region.equals(region2);
		} catch (BadLocationException e) {
			return false;		
		}
	}
	
	public Point isSelectOneNotEmptyPartition(Point selection) {
		List<ITypedRegion> regions = getPartitions(selection);
		ITypedRegion notEmpty = null;
		
		for (ITypedRegion region : regions) {
			if (PartitionStyles.EMPTY_LINE.equals(region.getType()) ||
					IDocument.DEFAULT_CONTENT_TYPE.equals(region.getType())) {
				continue;
			}
			if (notEmpty != null) {
				return null;
			}
			notEmpty = region;
		}
		if (notEmpty == null) {
			return null;
		}
		
		int offset = selection.x;
		int end = selection.y;
		
		int regionEnd = notEmpty.getOffset() + notEmpty.getLength();		
		if (notEmpty.getOffset() > offset) {
			offset = notEmpty.getOffset();
		}
		if (end > regionEnd) {
			end = regionEnd;
		} 	
		return new Point(offset, end);
	}
	
	public List<ITypedRegion> getPartitions(Point selection){
		List<ITypedRegion> result = new ArrayList<>();
		ITypedRegion partition = null;
		try {
			partition = fDocument.getPartition(selection.x);
			result.add(partition);
			int documentLength = fDocument.getLength();
			while (partition.getOffset() + partition.getLength() < selection.y 
					&& partition.getOffset() + partition.getLength() < documentLength) {
				int newOffset = partition.getOffset() + partition.getLength() + 1;
				partition = fDocument.getPartition(newOffset);
				result.add(partition);
			}		
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
		return result;
	}
	
	public List<String> getPartitionTypes(Point selection){
		// check selection
		if (selection.x + selection.y > fDocument.getLength()){
			selection.y = fDocument.getLength() - selection.x;
		}
		List<String> result = new ArrayList<>();
		try {
			ITypedRegion partition = fDocument.getPartition(selection.x);
			result.add(partition.getType());
			while (partition.getOffset() + partition.getLength() + 1 < selection.y) {
				int newOffset = partition.getOffset() + partition.getLength() + 1;
				partition = fDocument.getPartition(newOffset);
				result.add(partition.getType());
			}		
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
		return result;
	}
	
	
	public boolean isCurrentEmptyLine() {
		return getCurrentLine().isEmpty();
	}
	
	public String getCurrentLine() {
		int caretOffset = fText.getCaretOffset();
		int lineIndex = fText.getLineAtOffset(caretOffset);
		return fText.getLine(lineIndex);
	}
	
 	//========================
 	// listeners
 	
 	public void addListener(MdDocumentListener listener) {
 		fListeners.add(listener);
 	}
 	
 	public void removeListener(MdDocumentListener listener) {
 		fListeners.remove(listener);
 	}
 	
 	public void fireMdDocumentUdpated() {
 		fListeners.forEach(MdDocumentListener::mdDocumentUpdated);
 	}
 	
 	//=======================
 	// getters
 	
 	public IDocument document() {
 		return fDocument;
 	}
 	
 	public MdDocumentModel mdModel() {
 		return fMdModel;
 	}

 	public StyledText textWidget() {
 		return fText;
 	}
}
