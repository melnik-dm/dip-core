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
package ru.dip.core.model.interfaces;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;

import ru.dip.core.model.DipElementType;

public interface IDipElement extends IAdaptable, IDipProjectHolder {

	//============================
	// general
	
	String name();
	
	default String dipName() {
		return name();
	}
	
	DipElementType type();
	
	IResource resource();
	
	void setResource(IResource resource);

	String id();
	
	void updateWithProject();
	
	boolean canDelete();
	
	boolean canRename();
	
	//=====================
	// child-parent

	IParent parent();
	
	void setParent(IParent parent);
	
	boolean hasParent(IParent parent);
	
	//=====================
	// read-only  / include
	
	boolean isReadOnly();
	
	void setReadOnly(boolean value);
	
	boolean isIncluded();
	
	void setIncluded(boolean value);
		
}
