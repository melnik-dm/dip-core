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

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;

import ru.dip.core.model.interfaces.IParent;
import ru.dip.core.model.interfaces.IDipElement;

public class DipReservedMarker extends DipElement {

	public final static String RESERVED_MARKER_NAME = ".rsvd";
	
	public static DipReservedMarker instance(IResource resource, IParent parent) {
		IDipElement element = DipRoot.getInstance().getElement(resource, parent, DipElementType.RESERVED_MARKER);
		if (element == null) {
			DipReservedMarker dipReserveMarker = new DipReservedMarker(resource, parent);
			DipRoot.getInstance().putElement(dipReserveMarker);
			return dipReserveMarker;
		} else {
			return (DipReservedMarker) element;
		}
	}
	
	private DipReservedMarker(IResource resource, IParent parent) {
		super(resource, parent);
	}

	@Override
	public DipElementType type() {
		return DipElementType.RESERVED_MARKER;
	}
	
	@Override
	public IFile resource() {
		return (IFile) super.resource();
	}

}
