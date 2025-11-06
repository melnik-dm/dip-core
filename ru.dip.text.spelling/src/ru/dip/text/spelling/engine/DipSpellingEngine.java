/*******************************************************************************
 * SpellingEngine.java
 * 
 * Copyright (c) 2000, 2008 IBM Corporation and others.
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
 * This work is derived from the original source code of the SpellingEngine.java.
 * 
 *******************************************************************************/
package ru.dip.text.spelling.engine;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.ui.texteditor.spelling.ISpellingEngine;
import org.eclipse.ui.texteditor.spelling.ISpellingProblemCollector;
import org.eclipse.ui.texteditor.spelling.SpellingContext;

public class DipSpellingEngine  implements ISpellingEngine {

	/*
	 * @see org.eclipse.jdt.internal.ui.text.spelling.SpellingEngine#check(org.eclipse.jface.text.IDocument, org.eclipse.jface.text.IRegion[], org.eclipse.jdt.internal.ui.text.spelling.engine.ISpellChecker, org.eclipse.ui.texteditor.spelling.ISpellingProblemCollector, org.eclipse.core.runtime.IProgressMonitor)
	 */	
	protected void check(IDocument document, IRegion[] regions, ISpellChecker checker, ISpellingProblemCollector collector, IProgressMonitor monitor) {
		SpellEventListener listener= new SpellEventListener(collector, document);
		for (IRegion region : regions) {
			if (monitor != null && monitor.isCanceled())
				return;
			if (listener.isProblemsThresholdReached())
				return;
			checker.execute(listener, new SpellCheckIterator(document, region, checker.getLocale()));
		}
	}
		
	/**
	 * {@link ISpellEvent}listener that forwards events as
	 * {@link org.eclipse.ui.texteditor.spelling.SpellingProblem}.
	 */
	protected static class SpellEventListener implements ISpellEventListener {

		/** Spelling problem collector */
		private ISpellingProblemCollector fCollector;

		/**
		 * The document.
		 * @since 3.3
		 */
		private IDocument fDocument;

		private int fProblemsThreshold;
		private int fProblemCount;

		/**
		 * Initialize with the given spelling problem collector.
		 *
		 * @param collector the spelling problem collector
		 * @param document the document
		 */
		public SpellEventListener(ISpellingProblemCollector collector, IDocument document) {
			fCollector= collector;
			fDocument= document;
			fProblemsThreshold = 1000;
		}

		/*
		 * @see org.eclipse.jdt.internal.ui.text.spelling.engine.ISpellEventListener#handle(org.eclipse.jdt.internal.ui.text.spelling.engine.ISpellEvent)
		 */
		@Override
		public void handle(ISpellEvent event) {
			if (isProblemsThresholdReached())
				return;			
			fProblemCount++;
			fCollector.accept(new DipSpellingProblem(event, fDocument));
		}

		boolean isProblemsThresholdReached() {
			return fProblemCount >= fProblemsThreshold;
		}
	}

	/*
	 * @see org.eclipse.ui.texteditor.spelling.ISpellingEngine#check(org.eclipse.jface.text.IDocument, org.eclipse.jface.text.IRegion[], org.eclipse.ui.texteditor.spelling.SpellingContext, org.eclipse.ui.texteditor.spelling.ISpellingProblemCollector, org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public void check(IDocument document, IRegion[] regions, SpellingContext context, ISpellingProblemCollector collector, IProgressMonitor monitor) {		
		if (collector != null) {
			final SpellCheckEngine spellingEngine= SpellCheckEngine.getInstance();
			ISpellChecker checker= spellingEngine.getSpellChecker();
			if (checker != null) {
				check(document, regions, checker, collector, monitor);
			}
		}
	}

}
