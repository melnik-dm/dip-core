/*******************************************************************************
 * SpellingProblem.java
 * 
 * Copyright (c) 2000, 2011 IBM Corporation and others.
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
 *     
 * Modifications:
 * 		2025 Denis Melnik.
 * 		2025 Ruslan Sabirov.
 * 		2025 Andrei Motorin.
 * 
 * This work is derived from the original source code of the SpellingProblem.java.
 *     
 *******************************************************************************/
package ru.dip.text.spelling.engine;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.quickassist.IQuickAssistInvocationContext;
import org.eclipse.ui.texteditor.spelling.SpellingProblem;

/**
 * A {@link SpellingProblem} that adapts a {@link ISpellEvent}.
 * <p>
 * TODO: remove {@link ISpellEvent} notification mechanism
 * </p>
 */
public class DipSpellingProblem extends SpellingProblem {

	/** Spell event */
	private ISpellEvent fSpellEvent;

	/**
	 * The associated document.
	 *
	 * @since 3.3
	 */
	private IDocument fDocument;

	/**
	 * Initialize with the given spell event.
	 *
	 * @param spellEvent the spell event
	 * @param document the document
	 */
	public DipSpellingProblem(ISpellEvent spellEvent, IDocument document) {
		Assert.isLegal(document != null);
		Assert.isLegal(spellEvent != null);
		fSpellEvent= spellEvent;
		fDocument= document;
	}

	/*
	 * @see org.eclipse.ui.texteditor.spelling.SpellingProblem#getOffset()
	 */
	@Override
	public int getOffset() {
		return fSpellEvent.getBegin();
	}

	/*
	 * @see org.eclipse.ui.texteditor.spelling.SpellingProblem#getLength()
	 */
	@Override
	public int getLength() {
		return fSpellEvent.getEnd() - fSpellEvent.getBegin() + 1;
	}

	/*
	 * @see org.eclipse.ui.texteditor.spelling.SpellingProblem#getMessage()
	 */
	@Override
	public String getMessage() {
		if (isSentenceStart() && isDictionaryMatch()) {
			String result = String.format("The word '%s' should have an initial upper case letter", fSpellEvent.getWord());
			return result;
		}
		String result =  String.format("The word '%s' is not correctly spelled", fSpellEvent.getWord());
		return result;
	}

	/*
	 * @see org.eclipse.ui.texteditor.spelling.SpellingProblem#getProposals()
	 */
	@Override
	public ICompletionProposal[] getProposals() {
		return getProposals(null);
	}

	/*
	 * @see org.eclipse.ui.texteditor.spelling.SpellingProblem#getProposals(org.eclipse.jface.text.quickassist.IQuickAssistInvocationContext)
	 * @since 3.4
	 */
	@Override
	public ICompletionProposal[] getProposals(IQuickAssistInvocationContext context) {
		String[] arguments= getArguments();
		if (arguments == null) {
			return new ICompletionProposal[0];
		}		
		ICompletionProposal[] result = new ICompletionProposal[1];
		result[0]= new AddWordProposal(arguments[0], context);	
		return result;
	}

	public String[] getArguments() {
		String prefix= ""; //$NON-NLS-1$
		String postfix= ""; //$NON-NLS-1$
		String word;
		try {
			word= fDocument.get(getOffset(), getLength());
		} catch (BadLocationException e) {
			return null;
		}
		try {

			IRegion line= fDocument.getLineInformationOfOffset(getOffset());
			prefix= fDocument.get(line.getOffset(), getOffset() - line.getOffset());
			int postfixStart= getOffset() + getLength();
			postfix= fDocument.get(postfixStart, line.getOffset() + line.getLength() - postfixStart);

		} catch (BadLocationException exception) {
			// Do nothing
		}
		return new String[] {
				word,
				prefix,
				postfix,
				isSentenceStart() ? Boolean.toString(true) : Boolean
						.toString(false),
				isDictionaryMatch() ? Boolean.toString(true) : Boolean
						.toString(false) };
	}

	/**
	 * Returns <code>true</code> iff the corresponding word was found in the dictionary.
	 * <p>
	 * NOTE: to be removed, see {@link #getProposals()}
	 * </p>
	 *
	 * @return <code>true</code> iff the corresponding word was found in the dictionary
	 */
	public boolean isDictionaryMatch() {
		return fSpellEvent.isMatch();
	}

	/**
	 * Returns <code>true</code> iff the corresponding word starts a sentence.
	 * <p>
	 * NOTE: to be removed, see {@link #getProposals()}
	 * </p>
	 *
	 * @return <code>true</code> iff the corresponding word starts a sentence
	 */
	public boolean isSentenceStart() {
		return fSpellEvent.isStart();
	}

}
