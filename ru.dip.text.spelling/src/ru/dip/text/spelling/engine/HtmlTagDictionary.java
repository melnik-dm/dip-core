/*******************************************************************************
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
 *******************************************************************************/

package ru.dip.text.spelling.engine;

import java.net.URL;

/**
 * Dictionary for html tags.
 *
 * @since 3.0
 */
public class HtmlTagDictionary extends AbstractSpellDictionary {

	/*
	 * @see org.eclipse.jdt.internal.ui.text.spelling.engine.AbstractSpellDictionary#getName()
	 */
	@Override
	protected final URL getURL() {
		return null;
	}

	/*
	 * @see org.eclipse.jdt.internal.ui.text.spelling.engine.ISpellDictionary#isCorrect(java.lang.String)
	 */
	@Override
	public boolean isCorrect(final String word) {

		if (word.charAt(0) == HtmlTagConstants.HTML_TAG_PREFIX)
			return super.isCorrect(word);

		return false;
	}

	/*
	 * @see org.eclipse.jdt.ui.text.spelling.engine.AbstractSpellDictionary#load(java.net.URL)
	 */
	@Override
	protected synchronized boolean load(final URL url) {

		unload();

		for (String tag : HtmlTagConstants.HTML_GENERAL_TAGS) {
			hashWord(HtmlTagConstants.HTML_TAG_PREFIX + tag + HtmlTagConstants.HTML_TAG_POSTFIX);
			hashWord(HtmlTagConstants.HTML_CLOSE_PREFIX + tag + HtmlTagConstants.HTML_TAG_POSTFIX);
		}
		return true;
	}

	/*
	 * @see org.eclipse.jdt.internal.ui.text.spelling.engine.AbstractSpellDictionary#stripNonLetters(java.lang.String)
	 * @since 3.3
	 */
	@Override
	protected String stripNonLetters(String word) {
		return word;
	}

}
