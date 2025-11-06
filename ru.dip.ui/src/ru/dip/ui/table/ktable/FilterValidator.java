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
package ru.dip.ui.table.ktable;

import java.util.Collections;
import java.util.List;

import org.eclipse.jgit.lib.Repository;

import ru.dip.core.model.DipProject;
import ru.dip.core.model.interfaces.ISchemaContainer;
import ru.dip.core.report.model.condition.Condition;
import ru.dip.core.utilities.WorkbenchUtitlities;
import ru.dip.ui.Messages;

public class FilterValidator {
	
	private static final String FILTER_ERROR = Messages.KTableComposite_FilterError;
		
	public static boolean isValidFilter(Condition condition, DipProject dipProject) {
		List<ISchemaContainer> schemaContainers = List.of(dipProject);
		List<Repository> repositories = dipProject.getGitRepo() != null ?
				List.of(dipProject.getGitRepo()) : Collections.emptyList();
		return isValidFilter(condition, schemaContainers, repositories);
	}
	
	public static boolean isValidFilter(Condition condition, List<ISchemaContainer> schemaContainers, List<Repository> repositories) {	
		String error = ru.dip.core.report.checker.FilterValidator.validateFilter(condition, schemaContainers, repositories);
		if (error == null) {
			return true;
		} else {
			WorkbenchUtitlities.openError(FILTER_ERROR, error);
			return false;
		}
	}
	
}
