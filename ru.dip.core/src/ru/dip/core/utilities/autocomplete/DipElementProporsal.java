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
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.ui.model.WorkbenchLabelProvider;

import ru.dip.core.model.DipUnit;
import ru.dip.core.model.interfaces.IDipParent;
import ru.dip.core.model.interfaces.IDipDocumentElement;
import ru.dip.core.unit.UnitDescriptionPresentation;

public class DipElementProporsal implements ICompletionProposal {

	private static WorkbenchLabelProvider fWorkbenchLabelProvider = new WorkbenchLabelProvider();

	protected IDipDocumentElement fElement;
	protected int fOffset;
	protected ITextViewer fViewer;
	protected boolean fNeedDelimiter;
	
	public DipElementProporsal(int offset, IDipDocumentElement element, ITextViewer viewer) {
		fElement = element;
		fOffset = offset;
		fViewer = viewer;
	}
	
	public DipElementProporsal(int offset, IDipDocumentElement element, ITextViewer viewer, boolean needDelimiter) {
		this(offset, element, viewer);
		fNeedDelimiter = needDelimiter; 
	}
	
	@Override
	public void apply(IDocument document) {
		String replaceText = fElement.dipName();
		if (fNeedDelimiter) {
			replaceText = "/" + replaceText;
		}
		fViewer.getTextWidget().replaceTextRange(fOffset, 0, replaceText);
		fViewer.getTextWidget().setCaretOffset(fOffset + replaceText.length());
	}

	@Override
	public Point getSelection(IDocument document) {
		return null;
	}

	@Override
	public String getAdditionalProposalInfo() {
		return null;
	}

	@Override
	public String getDisplayString() {
		StringBuilder builder = new StringBuilder();
		builder.append(fElement.dipName());
		String descriptionText = getDescriptionText();
		if (descriptionText != null && !descriptionText.isEmpty()) {
			builder.append(" [");
			builder.append(descriptionText);
			builder.append("]");
		} 
		return builder.toString();
	}
	
	private String getDescriptionText() {
		if (fElement instanceof IDipParent) {
			return fElement.description();
		} 
		
		if (fElement instanceof DipUnit) {
			DipUnit unit = (DipUnit) fElement;
			UnitDescriptionPresentation descPresentation = unit.getUnitDescription();
			if (descPresentation == null) {
				return null;
			}
			return descPresentation.fullLinkTitle();
		}
		return null;
	}
	

	@Override
	public Image getImage() {
		if (fElement.resource() != null) {
			return fWorkbenchLabelProvider.getImage(fElement.resource());
		}
		return null;
	}

	@Override
	public IContextInformation getContextInformation() {		
		return null;
	}
	
}
