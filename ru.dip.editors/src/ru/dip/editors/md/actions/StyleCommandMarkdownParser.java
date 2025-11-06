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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.swt.graphics.Point;

import ru.dip.core.exception.MarkdownCommandException;
import ru.dip.core.utilities.md.MarkdownParagraphParser;
import ru.dip.core.utilities.md.MdUtilities;
import ru.dip.editors.md.MarkdownDocument;
import ru.dip.editors.md.partitions.PartitionStyles;

public class StyleCommandMarkdownParser extends MarkdownParagraphParser {
	
	private Type START_TYPE = Type.ITALIC_START;
	private Type END_TYPE = Type.ITALIC_END;
	//private String MARKER = "*";
	
	private MarkdownDocument fMdDocument;
	private int fCaret;
	private String fPartitionType;
	private Point fSelection;	
	private String fPartitionContent;
	private char[] fPartitionChars;
	private String fSelectionContent; 
	private char[] fSelectionChars;

	private int fPartitionOffset;
	private Point fPartitionSelection;
	// selected positions
	private int fCaretPositionIndex;
	private int fFirstPositionIndex;
	private int fLastPositionIndex;
	private List<MdStyledPosition> fSelectedPositions;
	private MdStyledPosition fFirstPosition;
	private MdStyledPosition fLastPosition;
	// has outside markers
	private boolean fHasMarkerEnd;
	private boolean fHasMarkerStart;
	// first position;
	private boolean fFirstPositionMarkerStart;
	private String fFirstMarker;
	// end position;
	private boolean fLastPositionMarkerEnd;
	private String fLastMarker;
	// inner
	private ArrayList<MdStyledPosition> fInnerMarkers;
	// for replace
	private int fEndOffset;
	private String fEndSpaceContent = ""; //$NON-NLS-1$
	private int fStartOffset;
	private String fStartSpaceContent = "";	 //$NON-NLS-1$
	private String newContent;
	
	public static StyleCommandMarkdownParser parserItalicSelection(MarkdownDocument mdDocument) {		
		StyleCommandMarkdownParser parser = new StyleCommandMarkdownParser(mdDocument);
		parser.setItalicType();
		parser.init();
		parser.defineSelection();
		parser.initParse();
		return parser;
	}
	
	public static StyleCommandMarkdownParser parserItalicCursor(MarkdownDocument mdDocument) {		
		StyleCommandMarkdownParser parser = new StyleCommandMarkdownParser(mdDocument);
		parser.setItalicType();
		parser.init();
		parser.fPartitionOffset = parser.fMdDocument.partitionOffset(parser.fCaret);
		parser.initParse();
		return parser;
	}
	
	public static StyleCommandMarkdownParser parserBoldSelection(MarkdownDocument mdDocument) {		
		StyleCommandMarkdownParser parser = new StyleCommandMarkdownParser(mdDocument);
		parser.setBoldType();
		parser.init();
		parser.defineSelection();
		parser.initParse();
		return parser;
	}
	
	public static StyleCommandMarkdownParser parserBoldCursor(MarkdownDocument mdDocument) {		
		StyleCommandMarkdownParser parser = new StyleCommandMarkdownParser(mdDocument);
		parser.setBoldType();
		parser.init();
		parser.fPartitionOffset = parser.fMdDocument.partitionOffset(parser.fCaret);
		parser.initParse();
		return parser;
	}
	
	private void setItalicType() {
		START_TYPE = Type.ITALIC_START;
		END_TYPE = Type.ITALIC_END;
	}
	
	private void setBoldType() {
		START_TYPE = Type.BOLD_START;
		END_TYPE = Type.BOLD_END;
	}
	
	private StyleCommandMarkdownParser(MarkdownDocument mdDocument) {
		fMdDocument = mdDocument;
	}
		
	private void init() {
		fCaret = fMdDocument.cursorPosition();
		if (fMdDocument.document().getLength() == fCaret) {
			fCaret--;
		}
		fPartitionType = fMdDocument.partitionType(fCaret);
		fPartitionContent = fMdDocument.partitionContent(fCaret);
		fPartitionChars = fPartitionContent.toCharArray();
	}
	
	private void defineSelection() {
		fSelection = fMdDocument.selectionWord();	
		fPartitionOffset = fMdDocument.partitionOffset(fSelection.x);
		fPartitionSelection = new Point(fSelection.x - fPartitionOffset, fSelection.y - fPartitionOffset);
	}
	
	private void initParse() {
		if (fPartitionType == null || fPartitionContent == null) {
			return;
		}	
		if (PartitionStyles.isList(fPartitionType)) {
			parseListItem(fPartitionContent);
		} else {
			parseParagraph(fPartitionContent);
		}
	}
	
	public boolean isValid() {
		return !getPositions().isEmpty();
	}
		
	public void selectedPositions() {
		fFirstPositionIndex = findPosition(fPartitionSelection.x);
		fLastPositionIndex = findLastPosition(fPartitionSelection.y);	
		int lastIndex = fLastPositionIndex;
		if (getPositions().size() > lastIndex) {
			lastIndex++;
		} 
		fSelectedPositions = new ArrayList<>(getPositions().subList(fFirstPositionIndex, lastIndex));;
		fFirstPosition = fSelectedPositions.get(0);
		fLastPosition = fSelectedPositions.get(fSelectedPositions.size() - 1);		
	}
	
	public void definePositionsForCursor() {
		fCaretPositionIndex = findPosition(fCaret - fPartitionOffset);
		fFirstPosition = previousStartPosition(fCaretPositionIndex);
		fLastPosition = nextEndPosition(fCaretPositionIndex);
	}
	
	public boolean hasStartEndPositions() {
		return fFirstPosition != null && fLastPosition != null;
	}
	
	private MdStyledPosition previousStartPosition(int currentPosition) {
		for (int i = currentPosition; i >= 0; i--){
			if (getPositions().get(i).type() == START_TYPE) {
				return getPositions().get(i);
			}
		}
		return null;
	}
	
	private MdStyledPosition nextEndPosition(int currentPosition) {
		for (int i = currentPosition; i < getPositions().size(); i++){
			if (getPositions().get(i).type() == END_TYPE) {
				return getPositions().get(i);
			}
		}	
		return null;
	}
	
	/**
	 * Проверяет является ли первая выделенная позиции - StartType
	 * Если нет, то проверяем предшествующую
	 */
	public void checkFirstPosition() {
		fFirstPositionMarkerStart = fFirstPosition.type() == START_TYPE;
		if (fFirstPositionMarkerStart) {
			if (fFirstPosition.offset() != fPartitionSelection.x) {
				fPartitionSelection.x = fFirstPosition.offset();
				fSelection.x = fPartitionSelection.x + fPartitionOffset;
			}
		} else {
			// проверяем предыдущую позицию
			if (fFirstPosition.offset() == fPartitionSelection.x 
					&& fFirstPositionIndex - 1 >= 0) {				
				MdStyledPosition previous = getPositions().get(fFirstPositionIndex -1);
				if (previous.type() == START_TYPE) {
					fFirstPositionMarkerStart = true;
					fFirstPositionIndex--;
					fFirstPosition = previous;
					fSelectedPositions.add(0, fFirstPosition);
					fPartitionSelection.x = fFirstPosition.offset();
					fSelection.x = fPartitionSelection.x + fPartitionOffset;					
				}
			}		
		}	
	}
	
	/**
	 * Определяет маркер, который нужно вставлять слева
	 */
	public void defineFirstMarker() {
		if (!fFirstPositionMarkerStart) {
			for (int i = fFirstPositionIndex - 1; i >= 0; i--) {
				MdStyledPosition position = getPositions().get(i);
				if (position.type() == START_TYPE) {
					try {
						fFirstMarker = fMdDocument.document().get(position.offset() + fPartitionOffset, position.length());
					} catch (BadLocationException e) {
						e.printStackTrace();
						break;
					}
				}
				if (position.type() == END_TYPE) {
					break;
				}
			}
		}
	}
	
	/**
	 * Проверяет является ли последняя выделенная позиция - EndType
	 * Если нет, то проверяем следующую
	 */
	public void checkLastPosition() {
		fLastPositionMarkerEnd = fLastPosition.type() == END_TYPE;
		if (fLastPositionMarkerEnd) {
			if (fLastPosition.offset() + fLastPosition.length() != fPartitionSelection.y) {
				fPartitionSelection.y = fLastPosition.offset() + fLastPosition.length();
				fSelection.y = fPartitionSelection.y + fPartitionOffset;
			} 
		} else {
			// проверяем предыдущую позицию
			if (fLastPosition.offset() + fLastPosition.length() == fPartitionSelection.y 
					&& fLastPositionIndex + 1 < getPositions().size()) {
				MdStyledPosition next = getPositions().get(fLastPositionIndex + 1);
				if (next.type() == END_TYPE) {
					fLastPositionMarkerEnd = true;
					fLastPositionIndex++;
					fLastPosition = next;
					fSelectedPositions.add(fLastPosition);
					fPartitionSelection.y = fLastPosition.offset() + fLastPosition.length();
					fSelection.y = fPartitionSelection.y + fPartitionOffset;					
				} 
			} 
		}	
	}
	
	/**
	 * Определяет маркер, который нужно вставлять справа
	 */
	public void defineLastMarker() {
		if (!fLastPositionMarkerEnd) {
			for (int i = fLastPositionIndex + 1; i < getPositions().size(); i++) {
				
				MdStyledPosition position = getPositions().get(i);
				if (position.type() == END_TYPE) {
					try {
						fLastMarker = fMdDocument.document().get(position.offset() + fPartitionOffset, position.length());
					} catch (BadLocationException e) {
						e.printStackTrace();
						break;
					}
				}
				if (position.type() == START_TYPE) {
					break;
				}
			}
		}
	}

	/**
	 * Есть ли слева маркер типа START
	 */
	public void defineOutsideStartMarker() {
		fHasMarkerEnd = false;
		for (int i = 0; i < fSelectedPositions.size(); i++) {
			MdStyledPosition position = fSelectedPositions.get(i);
			if (position.type() == START_TYPE) {
				break;
			} else if (position.type() == END_TYPE) {
				fHasMarkerEnd = true;
				break;
			}
		}
	}
	
	/**
	 * Есть ли справа маркер типа END
	 */
	public void defineOutsideEndMarker() {
		fHasMarkerStart = false;
		for (int i = fSelectedPositions.size() - 1; i >= 0; i--) {
			MdStyledPosition position = fSelectedPositions.get(i);
			if (position.type() == END_TYPE) {
				break;
			} else if (position.type() == START_TYPE) {
				fHasMarkerStart = true;
				break;
			}
		}
	}
		
	/**
	 * Определяем внутренние маркеры
	 */
	public void defineInnerMarkers() {
		fInnerMarkers = new ArrayList<>();
		for (MdStyledPosition position: fSelectedPositions) {
			if (position.type() == START_TYPE || position.type() == END_TYPE) {
				fInnerMarkers.add(position);
			}
		}
	}
	
	public void getSelectionContent() throws BadLocationException {
		fSelectionContent = fMdDocument.document().get(fSelection.x, fSelection.y - fSelection.x);
		fSelectionChars = fSelectionContent.toCharArray();
	}
	
	public void defineCommandEndOffset() throws MarkdownCommandException {
		fEndOffset = fSelection.y;
		int endSpaceOffset = -1;
		for (int i = 0; i < fSelectionChars.length; i++) {
			char ch = fSelectionChars[fSelectionChars.length - 1 - i];
			if (Character.isSpaceChar(ch) || '\n' == ch) {
				continue;
			}
			endSpaceOffset = i;
			break;
		}
		if (endSpaceOffset < 0) {
			throw new MarkdownCommandException("Markdown Command Error - одни пробелы в строке");  //$NON-NLS-1$
		}
		fEndOffset -= endSpaceOffset;
	}
	
	public void defineUnCommandCursorEndOffset() {
		fEndOffset = fPartitionOffset + fLastPosition.offset() + fLastPosition.length();
	}
	
	public void defineUnCommandEndOffset() {
		fEndOffset = fSelection.y;	
		if (!fLastPositionMarkerEnd) {
			int endSpaceOffset = fPartitionSelection.y;
			int spaces = 0;
			for (int i = endSpaceOffset; i < fPartitionChars.length; i++) {
				char ch = fPartitionChars[i];
				if (Character.isSpaceChar(ch) || '\n' == ch) {
					spaces++;
					continue;
				}
				break;
			}				
			fEndOffset = fEndOffset + spaces;
			if (spaces > 0) {
				try {
					fEndSpaceContent = fMdDocument.document().get(fSelection.y, spaces);
				} catch (BadLocationException e) {
					e.printStackTrace();
				}
			}	
		}
	}
	
	public void defineCommandStartOffset() throws MarkdownCommandException {
		fStartOffset = fSelection.x;
		int startSpaceOffset = -1;
		for (int i = 0; i < fSelectionChars.length; i++) {
			char ch = fSelectionChars[i];
			if (Character.isSpaceChar(ch) || '\n' == ch) {
				continue;
			}
			startSpaceOffset = i;
			break;
		}
		if (startSpaceOffset < 0) {
			throw new MarkdownCommandException("Markdown Command Error - одни пробелы в строке");  //$NON-NLS-1$
		}
		fStartOffset += startSpaceOffset;
	}
	
	public void defineUnCommandCursorStartOffset() {
		fStartOffset = fPartitionOffset + + fFirstPosition.offset();
	}
	
	public void defineUnCommandStartOffset() {
		fStartOffset = fSelection.x;		
		if (!fFirstPositionMarkerStart) {
			int spaces = 0;
			for (int i = fPartitionSelection.x -1; i>= 0; i--) {
				char ch = fPartitionChars[i];
				if (Character.isSpaceChar(ch) || '\n' == ch) {
					spaces++;
					continue;
				}
				break;
			}			
			if (spaces > 0) {
				try {
					fStartSpaceContent = fMdDocument.document().get(fPartitionSelection.x - spaces, spaces);
				} catch (BadLocationException e) {
					e.printStackTrace();
				}
				fStartOffset = fStartOffset - spaces;
			}								
		}
	}
	
	public void defineNewCommandContent() throws BadLocationException {
		StringBuilder builder = new StringBuilder();
		builder.append(fMdDocument.document().get(fStartOffset, fEndOffset - fStartOffset));
		 // смещение для удаляемых внутренних маркеров
		final int delta = fStartOffset - fPartitionOffset; 		
		// удаляем внутренние маркеры
		for (int i = fInnerMarkers.size() - 1; i >= 0; i--) {
			MdStyledPosition position = fInnerMarkers.get(i);
			int x = position.offset() - delta;
			int y = position.offset() + position.length() - delta;				
			builder.delete(x, y);
		}			
		// добавляем внешние маркеры
		boolean asterisk = MdUtilities.isAsteriskEmphasis(fMdDocument.document(), fStartOffset, fEndOffset);		
		String marker = "_"; //$NON-NLS-1$
		if (asterisk) {
			marker = "*"; //$NON-NLS-1$
		}
		if (START_TYPE == Type.BOLD_START) {
			marker = marker + marker;
		}
		
		if (!fHasMarkerStart) {
			builder.append(marker);
		}
		if (!fHasMarkerEnd) {
			builder.insert(0, marker);
		}
		newContent = builder.toString();
	}
	
	public void defineNewUnCursorCommandContent() throws BadLocationException {		
		int start = fStartOffset + fFirstPosition.length();
		int end = fEndOffset - fLastPosition.length();			
		newContent = fMdDocument.document().get(start, end - start);
	}
	
 	public void defineNewUnCommandContent() throws BadLocationException {		
		String content = fMdDocument.document().get(fSelection.x, fSelection.y - fSelection.x);	
		// текст для вставки
		StringBuilder builder = new StringBuilder();
		builder.append(content);
		// удаляем внутренние маркеры
		for (int i = fInnerMarkers.size() - 1; i >= 0; i--) {
			MdStyledPosition position = fInnerMarkers.get(i);
			int x = position.offset() - fPartitionSelection.x;
			int y = position.offset() - fPartitionSelection.x + position.length();
			builder.delete(x, y);
		}		
		if (!fFirstPositionMarkerStart) {
			builder.insert(0, fStartSpaceContent);
			builder.insert(0, fFirstMarker);
		}
		if (!fLastPositionMarkerEnd) {
			builder.append(fEndSpaceContent);
			builder.append(fLastMarker);
		}
		newContent = builder.toString();	
	}
	
	public void replace() throws BadLocationException {
		fMdDocument.document().replace(fStartOffset, fEndOffset - fStartOffset, newContent);		
		fMdDocument.setSelection(new Point(fStartOffset, fStartOffset + newContent.length()));	
	}
	
	public void replaceForCursor() throws BadLocationException {
		fMdDocument.document().replace(fStartOffset, fEndOffset - fStartOffset, newContent);	
		fMdDocument.setCursorPosition(fCaret - fFirstPosition.length());
	}
	
}
