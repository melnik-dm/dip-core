/*******************************************************************************
 * Copyright (c) 2000, 2012 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package ru.dip.text.spelling.engine;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.quickassist.IQuickAssistInvocationContext;
import org.eclipse.ui.texteditor.spelling.SpellingProblem;

import ru.dip.text.spelling.utils.images.ImageProvider;


/**
 * Proposal to add the unknown word to the dictionaries.
 *
 * @since 3.0
 */
public class AddWordProposal implements ICompletionProposal {


	/** The invocation context */
	private final IQuickAssistInvocationContext fContext;

	/** The word to add */
	private final String fWord;


	/**
	 * Creates a new add word proposal
	 *
	 * @param word
	 *                   The word to add
	 * @param context
	 *                   The invocation context
	 */
	public AddWordProposal(final String word, final IQuickAssistInvocationContext context) {
		fContext= context;
		fWord= word;
	}

	/*
	 * @see org.eclipse.jface.text.contentassist.ICompletionProposal#apply(org.eclipse.jface.text.IDocument)
	 */
	@Override
	public final void apply(final IDocument document) {

		final ISpellCheckEngine engine= SpellCheckEngine.getInstance();
		final ISpellChecker checker= engine.getSpellChecker();

		if (checker == null)
			return;


		if (checker.acceptsWords()) {
			checker.addWord(fWord);
			if (fContext != null && fContext.getSourceViewer() != null)
				SpellingProblem.removeAll(fContext.getSourceViewer(), fWord);
		}
	}
	
	/*
	 * @see org.eclipse.jface.text.contentassist.ICompletionProposal#getAdditionalProposalInfo()
	 */
	@Override
	public String getAdditionalProposalInfo() {
		return String.format("Adds the word  '%s' to the dictionary", fWord);
	}

	/*
	 * @see org.eclipse.jface.text.contentassist.ICompletionProposal#getContextInformation()
	 */
	@Override
	public final IContextInformation getContextInformation() {
		return null;
	}

	/*
	 * @see org.eclipse.jface.text.contentassist.ICompletionProposal#getDisplayString()
	 */
	@Override
	public String getDisplayString() {
		return String.format("Add  '%s' to dictionary", fWord);
	}

	/*
	 * @see org.eclipse.jface.text.contentassist.ICompletionProposal#getImage()
	 */
	@Override
	public Image getImage() {
		return ImageProvider.ADD_CORRECTION;
	}

	/*
	 * @see org.eclipse.jface.text.contentassist.ICompletionProposal#getSelection(org.eclipse.jface.text.IDocument)
	 */
	@Override
	public final Point getSelection(final IDocument document) {
		return new Point(fContext.getOffset(), fContext.getLength());
	}
}
