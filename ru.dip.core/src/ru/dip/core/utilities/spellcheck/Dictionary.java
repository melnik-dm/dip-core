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
package ru.dip.core.utilities.spellcheck;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.Platform;
import org.eclipse.swt.graphics.Point;

import ru.dip.core.DipCorePlugin;
import ru.dip.core.model.DipUnit;
import ru.dip.core.model.interfaces.IDipParent;
import ru.dip.core.model.interfaces.IDipDocumentElement;
import ru.dip.core.unit.UnitType;
import ru.dip.core.utilities.FileUtilities;
import ru.dip.core.utilities.TagStringUtilities;

public class Dictionary {

	private static final String REGEX = "\\b\\w+\\b";
	private static final String pluginID = "ru.dip.text.spelling";
	private static final String PREF_DICTIONARY = "_pref_dictionary";

	private static Dictionary instance;

	public static Dictionary instance() {
		if (instance == null) {
			instance = new Dictionary();
		}
		return instance;
	}

	private String fDictionaryPath;
	private FileTime fLastModifiedTime;
	private Set<String> dictionary = new HashSet<>();

	private Dictionary() {
		fDictionaryPath = getDictionaryPathPref();
		if (!checkPath(fDictionaryPath)) {
			return;
		}
		readDictionary();
	}

	private String getDictionaryPathPref() {
		return Platform.getPreferencesService().getString(pluginID, PREF_DICTIONARY, "defaultValue", null);
	}

	private boolean checkPath(String dictPath) {
		if (dictPath == null) {
			return false;
		}
		Path path = Paths.get(dictPath);
		return Files.exists(path);
	}

	private void readDictionary() {
		Path path = Paths.get(fDictionaryPath);
		if (!Files.exists(path)) {
			return;
		}
		try {
			dictionary = Files.readAllLines(path, StandardCharsets.UTF_8)
					.parallelStream()
					.collect(Collectors.toSet());
			fLastModifiedTime = Files.getLastModifiedTime(path);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// ========================
	// modified dictionary

	public void checkDictionaryUpdate() {
		String dictionaryPath = getDictionaryPathPref();
		if (dictionaryPath == null || dictionaryPath.isEmpty()) {
			if (fDictionaryPath != null && !fDictionaryPath.isEmpty()) {
				dictionary = new HashSet<String>();
			}
			return;
		}

		if (dictionaryPath.equals(fDictionaryPath)) {
			if (!checkPath(fDictionaryPath)) {
				return;
			}
			if (isTimeModified()) {
				readDictionary();
			}
		} else {
			if (!checkPath(fDictionaryPath)) {
				dictionary = new HashSet<String>();
				return;
			} else {
				readDictionary();
			}
		}
	}

	private boolean isTimeModified() {
		Path path = Paths.get(fDictionaryPath);
		try {
			FileTime time = Files.getLastModifiedTime(path);
			if (time == null) {
				return false;
			}
			return !time.equals(fLastModifiedTime);
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}

	// ==============================
	// check spelling

	public List<Point> checkText(String text) {
		List<Point> result = new ArrayList<>();
		Pattern pattern = Pattern.compile(REGEX, Pattern.UNICODE_CHARACTER_CLASS);
		Matcher matcher = pattern.matcher(text);
		int start = 0;
		while (matcher.find(start)) {
			String group = matcher.group();
			if (!dictionary.contains(group.toLowerCase())) {
				if (!group.toUpperCase().equals(group) && TagStringUtilities.isRussianLetters(group)) {
					Point p = new Point(matcher.start(), matcher.end() - matcher.start());
					result.add(p);
				}
			}
			start = matcher.end();
		}
		return result;
	}

	public List<SpellCheckError> checkDipParent(IDipParent parent) {
		List<SpellCheckError> result = new ArrayList<>();
		for (IDipDocumentElement dipDocElement : parent.getDipDocChildrenList()) {
			if (dipDocElement instanceof IDipParent) {
				List<SpellCheckError> errors = checkDipParent((IDipParent) dipDocElement);
				if (errors != null) {
					result.addAll(errors);
				}
			} else if (dipDocElement instanceof DipUnit) {
				List<SpellCheckError> errors = checkDipUnit((DipUnit) dipDocElement);
				if (errors != null) {
					result.addAll(errors);
				}
			}
		}
		return result;
	}

	public List<SpellCheckError> checkDipUnit(DipUnit unit) {
		UnitType type = unit.getUnitType();
		if (type.isSpellCheckingSupport()) {
			try {
				List<SpellCheckError> result = spellCheckText(unit);
				return result;
			} catch (IOException e) {
				e.printStackTrace();
				DipCorePlugin.logError(e, "Ошибка проверки орфографии");
			}
		}
		return null;
	}

	public List<SpellCheckError> spellCheckText(DipUnit unit) throws IOException {
		String text = FileUtilities.readFile(unit.resource());
		List<SpellCheckError> result = new ArrayList<>();
		Pattern pattern = Pattern.compile(REGEX, Pattern.UNICODE_CHARACTER_CLASS);
		Matcher matcher = pattern.matcher(text);
		int start = 0;
		while (matcher.find(start)) {
			String group = matcher.group();
			if (!dictionary.contains(group.toLowerCase()) && TagStringUtilities.isRussianLetters(group)) {
				if (!group.toUpperCase().equals(group)) {
					Point p = new Point(matcher.start(), matcher.end() - matcher.start());
					SpellCheckError error = new SpellCheckError(unit, group, p);
					result.add(error);
				}
			}
			start = matcher.end();
		}
		return result;
	}

}
