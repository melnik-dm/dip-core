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
package ru.dip.editors.md.model;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.TypedPosition;

import ru.dip.core.utilities.md.MarkdownParagraphParser;
import ru.dip.core.utilities.md.MarkdownParagraphParser.MdStyledPosition;
import ru.dip.core.utilities.md.MarkdownParagraphParser.Type;
import ru.dip.editors.md.partitions.PartitionStyles;

public class MDNode {
	
	private MdDocumentModel fModel;
	private MDNode fParent;
	private List<MdStyledPosition> fPositions = new ArrayList<>();
	private List<MDNode> fChildren = new ArrayList<>();
	private String fType;
	private int fOffset;
	private int fLength;
	
	public MDNode(MdDocumentModel model, TypedPosition typedPosition) {
		fModel = model;
		fType = typedPosition.getType();
		fOffset = typedPosition.getOffset();
		fLength = typedPosition.getLength();		
		if (PartitionStyles.PARAGRAPH.equals(fType)) {
			computeParagraphChildren();
			addGlossaryWordPosition(fOffset);
		} else if (PartitionStyles.isList(fType)) {
			computeListItemChildren();
			addGlossaryWordPosition(fOffset);
		}
		if (!fPositions.isEmpty()) {
			for (MdStyledPosition position: fPositions) {
				MDNode child = new MDNode(this, position, fOffset);
				fChildren.add(child);				
			}
		}
	}
	
	public MDNode(MDNode parent, MdStyledPosition styledPosition, int parentOffset) {
		fParent = parent;
		fModel = parent.model();
		fOffset = styledPosition.offset() + parentOffset;
		fLength = styledPosition.length();
		fType = styledPosition.type().toString();		
	}
	
	public void computeParagraphChildren() {
		try {
			String content = content();
			fPositions = computePositions(content);
		} catch (BadLocationException e) {
			fPositions = new ArrayList<>();
			e.printStackTrace();
		}
	}

	public void computeListItemChildren() {
		try {
			String content = content();
			fPositions = MarkdownParagraphParser.getListItemPositions(content);
		} catch (BadLocationException e) {
			fPositions = new ArrayList<>();
			e.printStackTrace();
		}
	}
	
	protected List<MdStyledPosition> computePositions(String content) {
		return MarkdownParagraphParser.getParagraphPositions(content);
	}
	
	
	private void addGlossaryWordPosition(int offset) {
		List<MdStyledPosition> positions = MarkdownParagraphParser.addGlossaryWordPosition(fModel.dipProject(),
				fModel.document().get(), fPositions, offset);
		if (positions != null) {
			fPositions = positions;
		}
	}

	/**
	 * Возвращает дочернию позицию по offset, либо себя если нет детей
	 * this.hasPosition(offset) должно быть true
	 */
	public MDNode findPosition(int offset) {
		if (fChildren.isEmpty()) {
			return this;
		}		
		for (MDNode child: fChildren) {
			if (child.hasPosition(offset)) {
				return child;
			}
		}	
		return null;
	}
	
	
	public boolean hasPosition(int offset) {
		return  fOffset <= offset 
				&& fOffset + fLength >= offset;
	}
	
	public boolean isComment() {
		if (fParent == null) {
			return PartitionStyles.COMMENT.equals(fType);
		} else {
			return Type.COMMENT.toString().equals(fType);
		}
	}
	

	public boolean isEmpty() {
		if (fParent == null) {
			return PartitionStyles.EMPTY_LINE.equals(fType);
		} else {
			return Type.EMPTY.toString().equals(fType);
		}
	}
	
	public boolean canComment() {
		if (fParent == null) {
			return PartitionStyles.COMMENT.equals(fType) || PartitionStyles.EMPTY_LINE.equals(fType);
		} 
		return true;
	}
	
	//====================
	// getters
		
	public String content() throws BadLocationException {
		return fModel.document().get(fOffset, fLength);	
	}
	
	public MdDocumentModel model() {
		return fModel;
	}
	
	public List<MdStyledPosition> positions(){
		return fPositions;
	}
	
	public List<MDNode> children(){
		return fChildren;
	}
	
	public MDNode parent() {
		return fParent;
	}
	
	public int offset() {
		return fOffset;
	}
	
	public int length() {
		return fLength;
	}
	
	public String type() {
		return fType;
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(fType);
		builder.append(" "); //$NON-NLS-1$
		builder.append(fOffset);
		builder.append("-"); //$NON-NLS-1$
		builder.append(fOffset + fLength);
		if (!fChildren.isEmpty()) {
			builder.append("\nChildren:  \n"); //$NON-NLS-1$
			for (MDNode node: fChildren) {
				builder.append("    "); //$NON-NLS-1$
				builder.append(node);
				builder.append("\n"); //$NON-NLS-1$
			}			
		}		
		return builder.toString();
	}

}
