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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class TextSpellingPlugin extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "ru.dip.text.spelling"; //$NON-NLS-1$
	
	private static final String DICTIONARY_FOLDER_PATH_IN_PLUGIN = "dictionary";
	private static final String DICTIONARY_ZIP_NAME = "ru.zip";
	private static final String DICTIONARY_NAME = "ru.dictionary";
	
	public static final String PREF_FIRST_START = "_pref_first_start";	
	public static final String PREF_DICTIONARY = "_pref_dictionary";
	public static final String PREF_IGNORE_DIGITS = "_pref_ignore_digits";
	public static final String PREF_IGNORE_MIXED = "_pref_ignore_mixed";
	public static final String PREF_IGNORE_SENTENCE = "_pref_ignore_sentence";
	public static final String PREF_IGNORE_UPPER = "_pref_ignore_upper";	
	public static final String PREF_IGNORE_URLS= "_pref_ignore_urls";
	public static final String PREF_IGNORE_NONLETTERS = "_pref_ignore_nonletters";
	public static final String PREF_IGNORE_SINGLE = "_pref_ignore_single";
	
	// The shared instance
	private static TextSpellingPlugin plugin;
	
	/**
	 * The constructor
	 */
	public TextSpellingPlugin() {
	}

	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		if (getPreferenceStore().getBoolean(PREF_FIRST_START)) {
			getPreferenceStore().setValue(PREF_FIRST_START, false);
			String dictionary = copyScriptFiles();
			if (dictionary != null) {
				getPreferenceStore().setValue(PREF_DICTIONARY, dictionary);
			}
		}
	}
	
	@Override
	protected void initializeDefaultPreferences(IPreferenceStore store) {				
		store.setDefault(PREF_FIRST_START, true);
		store.setDefault(PREF_DICTIONARY, "");
		store.setDefault(PREF_IGNORE_DIGITS, true);	
		store.setDefault(PREF_IGNORE_MIXED, true);
		store.setDefault(PREF_IGNORE_SENTENCE, true);
		store.setDefault(PREF_IGNORE_UPPER, true);
		store.setDefault(PREF_IGNORE_URLS, true);
		store.setDefault(PREF_IGNORE_NONLETTERS, true);
		store.setDefault(PREF_IGNORE_SINGLE, true);	
	}
	
	private String copyScriptFiles() {		
		try {
			Path scriptPathFolder = Utils.getPathFromPlugin(this, DICTIONARY_FOLDER_PATH_IN_PLUGIN);
			Path targetFolder = Utils.metadataPluginPath(this, null); 					
			Utils.copyFolder(scriptPathFolder, targetFolder);
			Path zipPath = targetFolder.resolve(DICTIONARY_ZIP_NAME);
			Utils.unzip(zipPath, targetFolder);
			Files.delete(zipPath);
			return targetFolder.resolve(DICTIONARY_NAME).toString();
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
			
	public static String dictionary() {
		return plugin.getPreferenceStore().getString(PREF_DICTIONARY);
	}
	
	public static void setDictionary(String value) {
		plugin.getPreferenceStore().setValue(PREF_DICTIONARY, value);
	}
	

	@Override
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static TextSpellingPlugin getDefault() {
		return plugin;
	}

}
