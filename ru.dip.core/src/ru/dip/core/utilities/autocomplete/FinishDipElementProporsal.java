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
package ru.dip.core.utilities.autocomplete;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;

import ru.dip.core.model.interfaces.IDipDocumentElement;

/**
 * Автодополнение идетификатора
 * Когда уже часть имени введена
 */
public class FinishDipElementProporsal extends DipElementProporsal {

	private String fStartText;
	
	public FinishDipElementProporsal(int offset, IDipDocumentElement element, 
			ITextViewer viewer, String startText) {
		super(offset, element, viewer);
		fStartText = startText;
	}
	
	@Override
	public void apply(IDocument document) {
		String replaceText = fElement.dipName();
		fViewer.getTextWidget().replaceTextRange(fOffset - fStartText.length(), fStartText.length(), replaceText);
		fViewer.getTextWidget().setCaretOffset(fOffset - fStartText.length() + replaceText.length());
	}

}