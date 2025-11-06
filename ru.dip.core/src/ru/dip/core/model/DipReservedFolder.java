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
package ru.dip.core.model;

import java.util.ArrayList;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;

import ru.dip.core.model.interfaces.IParent;
import ru.dip.core.model.interfaces.IDipElement;

public class DipReservedFolder extends DipContainer {
		
	public static DipReservedFolder instance(IFolder container, IParent parent) {
		IDipElement element = DipRoot.getInstance().getElement(container, parent, DipElementType.RESERVED_FOLDER);
		if (element == null) {
			DipReservedFolder reservedFolder = new DipReservedFolder(container, parent);
			DipRoot.getInstance().putElement(reservedFolder);
			return reservedFolder;
		} else {
			return (DipReservedFolder) element;
		}
	}
	
	private DipReservedFolder(IFolder folder, IParent parent) {
		super(folder, parent);
	}
	
	public void computeChildren(){
		fChildren = new ArrayList<>();
		try {
			for (IResource resource: resource().members()){
				DipElementType type = DipRoot.getType(resource);
				switch (type){
				case RESERVED_FOLDER:{
					createReservedFolder((IFolder) resource);
					break;
				}
				case RESERVED_MARKER:{
					createReservedMarker((IFile) resource);
					break;
				}
				case RESERVED_UNIT:{
					createReservedUnit((IFile) resource);
					break;
				}
				default:
					break;				
				}
			}
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public IFolder resource() {
		return (IFolder) super.resource();
	}
	
	@Override
	public DipElementType type() {
		return DipElementType.RESERVED_FOLDER;
	}
	
	@Override
	public IParent parent() {
		return super.parent();
	}

}
