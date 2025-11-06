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

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.TextPresentation;
import org.eclipse.jface.text.presentation.IPresentationDamager;
import org.eclipse.jface.text.presentation.IPresentationRepairer;
import org.eclipse.swt.custom.StyleRange;


public class NonRuleBasedDamagerRepairer implements IPresentationDamager, IPresentationRepairer {

    protected IDocument fDocument;
    protected TextAttribute fDefaultTextAttribute;

    public NonRuleBasedDamagerRepairer(TextAttribute defaultTextAttribute) {
        this.fDefaultTextAttribute = defaultTextAttribute;
    }

    @Override
    public void setDocument(IDocument document) {
        this.fDocument = document;
    }

    @Override
    public IRegion getDamageRegion(ITypedRegion partition, DocumentEvent event, boolean documentPartitioningChanged) {
    	/*if (!documentPartitioningChanged)
			try {
				IRegion info = fDocument.getLineInformationOfOffset(event.getOffset());
				int start = Math.max(partition.getOffset(), info.getOffset());
				int end = event.getOffset() + (event.getText() != null ? event.getText().length() : event.getLength());
				if (info.getOffset() <= end && end <= info.getOffset() + info.getLength()) {
					end = info.getOffset() + info.getLength();
				} else {
					end = endOfLineOf(end);
				}
				end = Math.min(partition.getOffset() + partition.getLength(), end);
				return new Region(start, end - start);
			} catch (BadLocationException _ex) {
			}
		return partition;*/
    	return new Region(0, fDocument.getLength());
    }
    

    @SuppressWarnings("unused")
	private int endOfLineOf(int offset) throws BadLocationException {
		IRegion info = fDocument.getLineInformationOfOffset(offset);
		if (offset <= info.getOffset() + info.getLength()) {
			return info.getOffset() + info.getLength();
		}
		int line = fDocument.getLineOfOffset(offset);
		try {
			info = fDocument.getLineInformation(line + 1);
			return info.getOffset() + info.getLength();
		} catch (BadLocationException _ex) {
			return fDocument.getLength();
		}
    }
    
    
    @Override
    public void createPresentation(TextPresentation presentation, ITypedRegion region) {
		addRange(presentation, region.getOffset(), region.getLength(), fDefaultTextAttribute);
    }
    
    private void addRange(TextPresentation presentation, int offset, int length, TextAttribute attr) {
		if (attr != null) {
			presentation.addStyleRange(new StyleRange(offset, length, attr.getForeground(), attr.getBackground(), attr
					.getStyle()));
		}
    }
    
}
