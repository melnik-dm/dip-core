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
package ru.dip.editors.md.comment;

import ru.dip.core.model.interfaces.ITextComment;
import ru.dip.core.utilities.TagStringUtilities;
import ru.dip.core.utilities.ui.swt.FontDimension;

public class CommentAnnotation implements ITextComment, Comparable<CommentAnnotation>{
	
	private int fOffset;
	private int fLength;
	private String fComment;
	
	private String fWrapTtext;
	private int fLinesCount;
	private int fHeight;
	private int fViewerY;
	
	public CommentAnnotation(int offset, int length, String comment) {
		fOffset = offset;
		fLength = length;
		fComment = comment;
	}
	
	public CommentAnnotation(ITextComment textComment) {
		fOffset = textComment.getOffset();
		fLength = textComment.getLength();
		fComment = textComment.getContent();
	}
	
	@Override
	public String toString() {
		return "Annotation: " +  fOffset + " -  " + fLength + "   " + fComment.length();
	}
	
	@Override
	public int getOffset() {
		return fOffset;
	}
	
	public void setOffset(int offset) {
		fOffset = offset;
	}
	
	@Override
	public int getLength() {
		return fLength;
	}
	
	@Override
	public String getContent() {
		return fComment;
	}
	
	public void setComment(String newValue) {
		fComment = newValue;
	}
	
	@Override
	public int getEndOffset() {
		return fOffset + fLength;
	}

	
	public void setOffsetDelta(int delta) {
		fOffset += delta;		
	}
	
	public void setLengthDelta(int delta) {
		fLength += delta;
	}
	
	public void setLength(int length) {
		fLength = length;
	}
	
	public void computeHeight(FontDimension fontDimension, int width) {
		fWrapTtext = FontDimension.getWrapText(fComment, fontDimension.getStringLength(width));
		fLinesCount = fWrapTtext.split(TagStringUtilities.LINE_SPLIT_REGEX).length;		
		fHeight = fontDimension.charHeight() * fLinesCount;		
	}
	
	public int getHeight() {
		return fHeight;
	}
	
	public void setViewerY(int y) {
		fViewerY = y;
	}
	
	public int getViewerY() {
		return fViewerY;
	}

	public int getLineCount() {
		return fLinesCount;
	}

	@Override
	public int compareTo(CommentAnnotation o) {
		return fOffset - o.fOffset;
	}

}
