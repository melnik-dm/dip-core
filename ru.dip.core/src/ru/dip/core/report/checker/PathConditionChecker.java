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
package ru.dip.core.report.checker;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import ru.dip.core.model.finder.FindResult;
import ru.dip.core.report.model.condition.BooleanValue;
import ru.dip.core.report.model.condition.Condition;
import ru.dip.core.report.model.condition.ConditionPart;
import ru.dip.core.report.model.condition.FieldName;
import ru.dip.core.report.model.condition.Sign;
import ru.dip.core.report.model.condition.SimpleCondition;
import ru.dip.core.report.model.condition.Value;
import ru.dip.core.report.model.condition.FieldName.FieldType;
import ru.dip.core.utilities.FileUtilities;

public class PathConditionChecker {

	/**
	 * Проверка объекта (без проекта) fullPath - полный путь до объекта repoPath -
	 * полный путь до репозитория или проекта (относительного этого пути будут
	 * определяться фильтры PATH)
	 */
	public static boolean checkByPath(Path fullPath, Path repoPath, Condition condition) {
		List<ConditionPart> parts = condition.getParts();
		if (parts.isEmpty()) {
			return true;
		}
		List<ConditionPart> boolConditions = evaluateSimpleConditions(parts, fullPath, repoPath);
		return BooleanCalculator.checkBooleanConditions(boolConditions);
	}

	private static List<ConditionPart> evaluateSimpleConditions(List<ConditionPart> parts, Path fullPath,
			Path repoPath) {
		List<ConditionPart> result = new ArrayList<>();
		for (ConditionPart part : parts) {
			if (part instanceof SimpleCondition) {
				SimpleCondition simpleCondition = (SimpleCondition) part;
				boolean evaluate = evaluate(simpleCondition, fullPath, repoPath);
				result.add(BooleanValue.of(evaluate));
			} else {
				result.add(part);
			}
		}
		return result;
	}

	private static boolean evaluate(SimpleCondition condition, Path path, Path repoPath) {
		FieldName field = condition.fieldName();
		Sign sign = condition.sign();
		Value value = condition.value();
		if (Files.isDirectory(path)) {
			return evaluateDirectory(path, repoPath, field, sign, value);
		} else {
			return evaluateFile(path, repoPath, field, sign, value);
		}
	}

	private static boolean evaluateDirectory(Path path, Path repoPath, FieldName fieldname, Sign sign, Value value) {
		if (fieldname.type() == FieldType.PATH) {
			return checkFolderPath(path, repoPath, value);
		}
		return true;
	}

	private static boolean evaluateFile(Path path, Path repoPath, FieldName fieldname, Sign sign, Value value) {
		FieldType type = fieldname.type();
		switch (type) {
		case EXT: {
			String extension = FileUtilities.getFileExtension(path);
			return SimpleConditionChecker.checkFileExtension(extension, sign, value);
		}
		case PATH: {
			return checkFilePath(path, repoPath, value);
		}
		case ENABLED: {
			return true;
		}
		case FORM_FIELD: {
			return false;
		}
		case TEXT: {
			try {
				String content = Files.readString(path);
				FindResult result = FindResult.find(content, value.getValue(), false, false);
				boolean find = result.size() > 0;
				return ((sign == Sign.EQUAL) == find);
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			}
		}
		case CASE_TEXT: {
			try {
				String content = Files.readString(path);
				FindResult result = FindResult.find(content, value.getValue(), true, false);
				boolean find = result.size() > 0;
				return ((sign == Sign.EQUAL) == find);
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			}
		}
		case WORD: {
			try {
				String content = Files.readString(path);
				FindResult result = FindResult.find(content, value.getValue(), false, true);
				boolean find = result.size() > 0;
				return ((sign == Sign.EQUAL) == find);
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			}
		}
		case CASE_WORD: {
			try {
				String content = Files.readString(path);
				FindResult result = FindResult.find(content, value.getValue(), true, true);
				boolean find = result.size() > 0;
				return ((sign == Sign.EQUAL) == find);
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			}
		}
		default:
			return false;
		}
	}

	private static boolean checkFilePath(Path objPath, Path repoPath, Value value) {
		return checkFolderPath(objPath.getParent(), repoPath, value);
	}

	private static boolean checkFolderPath(Path objPath, Path repoPath, Value value) {
		String path = value.getValue();
		if (path == null || path.isEmpty()) {
			return true;
		}
		boolean recursive = false;
		if (path.endsWith("/*")) {
			recursive = true;
			path = path.substring(0, path.length() - 2);
		}

		Path relativePath = repoPath.relativize(objPath);
		Path filterPath = Path.of(path);

		if (relativePath.equals(filterPath)) {
			return true;
		}

		if (recursive) {
			return relativePath.startsWith(filterPath);
		}
		return false;
	}

}
