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
package ru.dip.ui.wizard.paste;

import java.nio.file.Paths;

import org.eclipse.core.resources.IResource;

import ru.dip.core.model.interfaces.IDipParent;

public class PasteResourceElement extends PasteElement {
		
	private IResource fResource;
	
	public PasteResourceElement(IDipParent parent, IResource resource) {
		super(parent);
		fResource = resource;
		setNewName(resource.getName());
	}
		
	@Override
	public String getOldName() {
		return fResource.getName();
	}
	
	@Override
	public boolean isFolder() {
		return fResource.getType() == IResource.FOLDER;
	}
	
	@Override
	public java.nio.file.Path getPath() {
		return Paths.get(fResource.getLocationURI());
	}

	public IResource getResource() {
		return fResource;
	}

}
