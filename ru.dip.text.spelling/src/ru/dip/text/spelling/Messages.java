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
package ru.dip.text.spelling;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "ru.dip.text.spelling.messages"; //$NON-NLS-1$
	private static final String RU_BUNDLE_NAME = "ru.dip.text.spelling.ru_messages"; //$NON-NLS-1$

	public static String SpellingPreferences_BrowseButton;
	public static String SpellingPreferences_DictionaryLabel;
	public static String SpellingPreferences_IgnoraInternetAddressed;
	public static String SpellingPreferences_IgnoreMixedCase;
	public static String SpellingPreferences_IgnoreNonLetters;
	public static String SpellingPreferences_IgnoreSentenceCapitalization;
	public static String SpellingPreferences_IgnoreSingleLetters;
	public static String SpellingPreferences_IgnoreUpperCaseWords;
	public static String SpellingPreferences_IgnoreWithDigits;
	
	static {
		// initialize resource bundle
		//if (DipCorePlugin.getLanguage() == 0) {
		if (true) {
			NLS.initializeMessages(RU_BUNDLE_NAME, Messages.class);
		} else {
			NLS.initializeMessages(BUNDLE_NAME, Messages.class);
		}
	}

	private Messages() {
	}
}
