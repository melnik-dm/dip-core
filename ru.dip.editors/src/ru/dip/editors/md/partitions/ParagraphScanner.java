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
package ru.dip.editors.md.partitions;

import static ru.dip.editors.md.partitions.PartitionStyles.getToken;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.ITokenScanner;
import org.eclipse.jface.text.rules.Token;

import ru.dip.core.model.DipProject;
import ru.dip.core.utilities.md.MarkdownParagraphParser;
import ru.dip.core.utilities.md.MarkdownParagraphParser.MdStyledPosition;

public class ParagraphScanner implements ITokenScanner {

	private List<MdStyledPosition> fPositions = new ArrayList<>();
	private int fIndex = 0;
	private MdStyledPosition fCurrentPosition;
	
	private int fOffset;
	private int fLength;	

	private DipProject fDipProject;
	private IDocument fDocument;
	
	public ParagraphScanner(DipProject dipProject, IDocument document){
		fDipProject = dipProject;
		fDocument = document;
	}
	
	@Override
	public void setRange(IDocument document, int offset, int length) {
		fDocument = document;
		fPositions.clear();
		fOffset = offset;
		fIndex = 0;
		try {
			String content = document.get(offset, length);
			fPositions = computePositions(content);
			addGlossaryWordPosition(offset);
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
	}
	
	private void addGlossaryWordPosition(int offset) {
		List<MdStyledPosition> positions = MarkdownParagraphParser.addGlossaryWordPosition(fDipProject, fDocument.get(), fPositions, offset);
		if (positions != null) {
			fPositions = positions;
		}
	}
	
	protected List<MdStyledPosition> computePositions(String content) {		
		return MarkdownParagraphParser.getParagraphPositions(content);
	}

	@Override
	public IToken nextToken() {
		if (fIndex < fPositions.size()) {
			fCurrentPosition = fPositions.get(fIndex);
			fIndex++;		
			return getToken(fCurrentPosition);
		} else {
			fCurrentPosition = null;
			return Token.EOF;
		}
	}

	@Override
	public int getTokenOffset() {
		if (fCurrentPosition == null) {
			return fOffset + fLength;
		} else {
			return fOffset + fCurrentPosition.offset();
		}
	}

	@Override
	public int getTokenLength() {
		if (fCurrentPosition == null) {
			return 0;
		} else {
			return fCurrentPosition.length();
		}
	}
	
}
