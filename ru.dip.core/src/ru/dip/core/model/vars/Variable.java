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

import org.eclipse.core.resources.IResource;

import ru.dip.core.model.DipElementType;
import ru.dip.core.model.DipProject;
import ru.dip.core.model.interfaces.IDipElement;
import ru.dip.core.model.interfaces.IParent;

public class Variable implements IDipElement {
	
	private String fName;
	private String fValue;
	private VarContainer fVarContainer;
	
	public Variable(String name, String value, VarContainer folder) {
		fName = name.trim();		
		fValue = value;
		fVarContainer = folder;
	}

	public String getValue(){
		return fValue;
	}
	
	public void setNewValue(String newName, String newValue) {
		fName = newName.trim();
		fValue = newValue;
	}
	
	@Override
	public <T> T getAdapter(Class<T> adapter) {
		return null;
	}

	@Override
	public String name() {
		return fName;
	}

	@Override
	public DipElementType type() {
		return DipElementType.VARIABLE;
	}

	@Override
	public IResource resource() {
		return null;
	}

	@Override
	public void setResource(IResource resource) {}
	
	@Override
	public String id() {
		return fName;
	}

	@Override
	public VarContainer parent() {
		return fVarContainer;
	}

	@Override
	public void setParent(IParent parent) {}

	@Override
	public boolean hasParent(IParent parent) {
		return true;
	}

	@Override
	public DipProject dipProject() {
		return fVarContainer.dipProject();
	}

	@Override
	public String toString() {
		return fName;
	}

	@Override
	public void updateWithProject() {}

	@Override
	public boolean isReadOnly() {
		return fVarContainer.isReadOnly();
	}

	@Override
	public void setReadOnly(boolean value) {}
	
	@Override
	public boolean isIncluded() {
		return fVarContainer.isIncluded();
	}
	
	@Override
	public void setIncluded(boolean value) {}
	
	@Override
	public boolean canDelete() {
		return fVarContainer.canDelete();
	}
	
	@Override
	public boolean canRename() {
		return fVarContainer.canRename();
	}

}
