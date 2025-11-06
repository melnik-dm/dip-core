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

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.formatter.ContentFormatter;

import ru.dip.editors.md.MDEditor;

public class MDContentFormatter extends ContentFormatter{
	
	private MDEditor fEditor;
	
	public MDContentFormatter(MDEditor editor) {
		fEditor = editor;
	}
	
	@Override
	public void format(IDocument document, IRegion region) {
		fEditor.fAutoFormatProcess = true;
		super.format(document, region);
		addLastEmptyLine(document, region);
		fEditor.fAutoFormatProcess = false;
	}
	
	private void addLastEmptyLine(IDocument document, IRegion region) {
		if (isAllDocument(document, region)) {
			try {
				if (!hasLastEmptyLine(document)) {
					document.replace(document.getLength(), 0, "\n"); //$NON-NLS-1$
				}
			} catch (BadLocationException e) {
				e.printStackTrace();
			}
		}
	}
	
	private boolean isAllDocument(IDocument document, IRegion region) {
		return region.getOffset() == 0 
				&& region.getLength() == document.getLength();
	}
	
	private boolean hasLastEmptyLine(IDocument document) throws BadLocationException {
		int numberLines = document.getNumberOfLines();
		if (numberLines > 0) {
			IRegion region = document.getLineInformation(numberLines - 1);
			String lastLine = document.get(region.getOffset(), region.getLength());
			return lastLine.trim().isEmpty();	
		}
		return true;
	}
}
