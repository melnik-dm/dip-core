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

import java.io.IOException;
import java.util.List;

import ru.dip.core.model.interfaces.IDipElement;
import ru.dip.core.model.interfaces.IDipParent;
import ru.dip.core.model.interfaces.IParent;

public interface IVarContainer extends IParent {

	List<Variable> getVariables();

	IDipParent getDipParent();

	String getRelativePath();

	void deleteVariables(IDipElement[] objects) throws IOException;

	IDipElement getChild(String name);

	void addField(String name, String value) throws IOException;

	void deleteField(Variable field) throws IOException;

	void saveVarContainer() throws IOException;
}
