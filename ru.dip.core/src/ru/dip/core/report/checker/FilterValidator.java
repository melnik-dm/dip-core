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

import java.util.Collections;
import java.util.List;

import org.eclipse.jgit.lib.Repository;

import ru.dip.core.Messages;
import ru.dip.core.model.DipProject;
import ru.dip.core.model.interfaces.ISchemaContainer;
import ru.dip.core.report.model.condition.Condition;
import ru.dip.core.report.model.condition.ErrorCondition;

public class FilterValidator {
	
	private static final String INVALID_FILTER = Messages.FilterValidator_InvalidConditionMessage;

	public static String validateFilter(Condition condition, DipProject dipProject) {
		List<ISchemaContainer> schemaContainers = List.of(dipProject);
		List<Repository> repositories = dipProject.getGitRepo() != null ?
				List.of(dipProject.getGitRepo()) : Collections.emptyList();
		return validateFilter(condition, schemaContainers, repositories);
	}
	
	/**
	 * Если фильтр применяется для одного проекта - то лист из одного ISchemaContainer и одного репозитория (при наличии)
	 * 
	 */
	public static String validateFilter(Condition condition, List<ISchemaContainer> schemaContainers, List<Repository> repositories) {
		String error = isInvalidCondition(condition);
		if (error != null) {
			return error;
		}	
		return condition.validate(schemaContainers, repositories);			
	}
	
	
	private static String isInvalidCondition(Condition condition) {
		if (condition instanceof ErrorCondition) {
			String message = ((ErrorCondition) condition).error();
			if (message == null) {
				return INVALID_FILTER;
			} else {
				return INVALID_FILTER + ": " + message; //$NON-NLS-1$
			}
		}
		if (condition == null) {
			return INVALID_FILTER;
		}
		return null;
	}
	
}
