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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.variables.IStringVariableManager;
import org.eclipse.core.variables.VariablesPlugin;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.texteditor.spelling.SpellingService;

import ru.dip.text.spelling.TextSpellingPlugin;

public class SpellCheckEngine implements ISpellCheckEngine, IPropertyChangeListener {

	/** The dictionary location */
	public static final String DICTIONARY_LOCATION = "dictionaries/"; //$NON-NLS-1$

	/** The singleton engine instance */
	private static SpellCheckEngine fgEngine = null;

	/**
	 * Caches the locales of installed dictionaries.
	 *
	 * @since 3.3
	 */
	private static Set<Locale> fgLocalesWithInstalledDictionaries;

	/**
	 * Returns the locales for which this spell check engine has dictionaries.
	 *
	 * @return The available locales for this engine
	 */
	public static Set<Locale> getLocalesWithInstalledDictionaries() {
		if (fgLocalesWithInstalledDictionaries != null)
			return fgLocalesWithInstalledDictionaries;

		return fgLocalesWithInstalledDictionaries = Collections.emptySet();

	}

	/**
	 * Returns the default locale for this engine.
	 *
	 * @return The default locale
	 */
	public static Locale getDefaultLocale() {
		return Locale.getDefault();
	}

	/**
	 * Returns the dictionary closest to the given locale.
	 *
	 * @param locale the locale
	 * @return the dictionary or <code>null</code> if none is suitable
	 * @since 3.3
	 */
	public ISpellDictionary findDictionary(Locale locale) {
		ISpellDictionary dictionary = fLocaleDictionaries.get(locale);
		if (dictionary != null)
			return dictionary;

		// Try same language
		String language = locale.getLanguage();
		Iterator<Entry<Locale, ISpellDictionary>> iter = fLocaleDictionaries.entrySet().iterator();
		while (iter.hasNext()) {
			Entry<Locale, ISpellDictionary> entry = iter.next();
			Locale dictLocale = entry.getKey();
			if (dictLocale.getLanguage().equals(language))
				return entry.getValue();
		}

		return null;
	}

	/**
	 * Returns the singleton instance of the spell check engine.
	 *
	 * @return The singleton instance of the spell check engine
	 */
	public static synchronized final SpellCheckEngine getInstance() {

		if (fgEngine == null)
			fgEngine = new SpellCheckEngine();

		return fgEngine;
	}

	/**
	 * Shuts down the singleton instance of the spell check engine.
	 */
	public static synchronized final void shutdownInstance() {
		if (fgEngine != null) {
			fgEngine.shutdown();
			fgEngine = null;
		}
	}

	/** The registered locale insensitive dictionaries */
	private Set<ISpellDictionary> fGlobalDictionaries = new HashSet<>();

	/** The spell checker for fLocale */
	private DefaultSpellChecker fChecker = null;

	/** The registered locale sensitive dictionaries */
	private Map<Locale, ISpellDictionary> fLocaleDictionaries = new HashMap<>();

	/** The user dictionary */
	private ISpellDictionary fUserDictionary = null;

	/**
	 * Creates a new spell check manager.
	 */
	private SpellCheckEngine() {
		fGlobalDictionaries.add(new HtmlTagDictionary());
		TextSpellingPlugin.getDefault().getPreferenceStore().addPropertyChangeListener(this);
		EditorsUI.getPreferenceStore().addPropertyChangeListener(this);
	}

	/*
	 * @see org.eclipse.jdt.internal.ui.text.spelling.engine.ISpellCheckEngine#
	 * getSpellChecker()
	 */
	@Override
	public synchronized final ISpellChecker getSpellChecker() throws IllegalStateException {
		if (fGlobalDictionaries == null)
			throw new IllegalStateException("spell checker has been shut down"); //$NON-NLS-1$
		IPreferenceStore store = TextSpellingPlugin.getDefault().getPreferenceStore();
		Locale locale = getCurrentLocale(store);
		if (fUserDictionary == null && "".equals(locale.toString())) //$NON-NLS-1$
			return null;
		if (fChecker != null && fChecker.getLocale().equals(locale))
			return fChecker;
		resetSpellChecker();
		fChecker = new DefaultSpellChecker(store, locale);
		resetUserDictionary();
		return fChecker;
	}

	/**
	 * Returns the current locale of the spelling preferences.
	 *
	 * @param store the preference store
	 * @return The current locale of the spelling preferences
	 */
	private Locale getCurrentLocale(IPreferenceStore store) {
		// по хорошему spelling locale - Должна храниться в настройках плагина
		String storeLocale = "en_US";
		return convertToLocale(storeLocale);
	}

	public static Locale convertToLocale(String locale) {
		int length = locale.length();
		if (length >= 5)
			return new Locale(locale.substring(0, 2), locale.substring(3, 5));

		if (length == 2 && locale.indexOf('_') == -1)
			return new Locale(locale);

		if (length == 3 && locale.charAt(0) == '_')
			return new Locale("", locale.substring(1)); //$NON-NLS-1$

		return new Locale(""); //$NON-NLS-1$
	}

	/*
	 * @see org.eclipse.jdt.ui.text.spelling.engine.ISpellCheckEngine#getLocale()
	 */
	@Override
	public synchronized final Locale getLocale() {
		if (fChecker == null)
			return null;

		return fChecker.getLocale();
	}

	/*
	 * @see
	 * org.eclipse.jface.util.IPropertyChangeListener#propertyChange(org.eclipse.
	 * jface.util.PropertyChangeEvent)
	 */
	@Override
	public final void propertyChange(final PropertyChangeEvent event) {
		if (TextSpellingPlugin.PREF_DICTIONARY.equals(event.getProperty())) {
			resetSpellChecker();
			return;
		}

		if (SpellingService.PREFERENCE_SPELLING_ENABLED.equals(event.getProperty())
				&& !EditorsUI.getPreferenceStore().getBoolean(SpellingService.PREFERENCE_SPELLING_ENABLED)) {
			if (this == fgEngine)
				SpellCheckEngine.shutdownInstance();
			else
				shutdown();
		}
	}

	//
	// Здесь добавляем наш словарь
	/**
	 * Resets the current checker's user dictionary.
	 */
	private synchronized void resetUserDictionary() {
		if (fChecker == null)
			return;

		// Update user dictionary
		if (fUserDictionary != null) {
			fChecker.removeDictionary(fUserDictionary);
			fUserDictionary.unload();
			fUserDictionary = null;
		}

		String filePath = TextSpellingPlugin.dictionary();

		VariablesPlugin variablesPlugin = VariablesPlugin.getDefault();
		if (variablesPlugin == null)
			return;

		IStringVariableManager variableManager = variablesPlugin.getStringVariableManager();
		try {
			filePath = variableManager.performStringSubstitution(filePath);
		} catch (CoreException e) {
			return;
		}
		if (filePath.length() > 0) {
			try {
				File file = new File(filePath);
				if (!file.exists() && !file.createNewFile())
					return;

				final URL url = new URL("file", null, filePath); //$NON-NLS-1$
				InputStream stream = url.openStream();
				if (stream != null) {
					try {
						fUserDictionary = new PersistentSpellDictionary(url);
						fChecker.addDictionary(fUserDictionary);
					} finally {
						stream.close();
					}
				}
			} catch (IOException exception) {
			}
		}
	}

	/*
	 * @see org.eclipse.jdt.internal.ui.text.spelling.engine.ISpellCheckEngine#
	 * registerDictionary(org.eclipse.jdt.internal.ui.text.spelling.engine.
	 * ISpellDictionary)
	 */
	@Override
	public synchronized final void registerGlobalDictionary(final ISpellDictionary dictionary) {
		fGlobalDictionaries.add(dictionary);
		resetSpellChecker();
	}

	/*
	 * @see org.eclipse.jdt.internal.ui.text.spelling.engine.ISpellCheckEngine#
	 * registerDictionary(java.util.Locale,
	 * org.eclipse.jdt.internal.ui.text.spelling.engine.ISpellDictionary)
	 */
	@Override
	public synchronized final void registerDictionary(final Locale locale, final ISpellDictionary dictionary) {
		fLocaleDictionaries.put(locale, dictionary);
		resetSpellChecker();
	}

	/*
	 * @see
	 * org.eclipse.jdt.internal.ui.text.spelling.engine.ISpellCheckEngine#unload()
	 */
	@Override
	public synchronized final void shutdown() {
		TextSpellingPlugin.getDefault().getPreferenceStore().removePropertyChangeListener(this);
		EditorsUI.getPreferenceStore().removePropertyChangeListener(this);
		ISpellDictionary dictionary = null;
		for (final Iterator<ISpellDictionary> iterator = fGlobalDictionaries.iterator(); iterator.hasNext();) {
			dictionary = iterator.next();
			dictionary.unload();
		}
		fGlobalDictionaries = null;

		for (final Iterator<ISpellDictionary> iterator = fLocaleDictionaries.values().iterator(); iterator
				.hasNext();) {
			dictionary = iterator.next();
			dictionary.unload();
		}
		fLocaleDictionaries = null;

		fUserDictionary = null;
		fChecker = null;
	}

	private synchronized void resetSpellChecker() {
		if (fChecker != null) {
			ISpellDictionary dictionary = fLocaleDictionaries.get(fChecker.getLocale());
			if (dictionary != null)
				dictionary.unload();
		}
		fChecker = null;
	}

	/*
	 * @see org.eclipse.jdt.ui.text.spelling.engine.ISpellCheckEngine#
	 * unregisterDictionary(org.eclipse.jdt.ui.text.spelling.engine.
	 * ISpellDictionary)
	 */
	@Override
	public synchronized final void unregisterDictionary(final ISpellDictionary dictionary) {
		fGlobalDictionaries.remove(dictionary);
		fLocaleDictionaries.values().remove(dictionary);
		dictionary.unload();
		resetSpellChecker();
	}
}
