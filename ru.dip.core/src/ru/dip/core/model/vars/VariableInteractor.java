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
package ru.dip.core.model.vars;

import java.util.Collection;

import ru.dip.core.model.interfaces.IDipDocumentElement;
import ru.dip.core.utilities.text.Terms;

public class VariableInteractor {
	
	public static String changeVar(String content, IDipDocumentElement dde) {		
		ProjectVarContainer varContainer = dde.dipProject().getVariablesContainer();
		if (varContainer == null) {
			return content;
		}		
		Collection<Variable> vars = varContainer.getVariablesForUnit(dde);		
		for (Variable variable: vars) {		
			String regex = Terms.createVarRegex(variable.name());
			content = content.replaceAll(regex, variable.getValue());
		}
		return content;
	}
}
