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

import ru.dip.core.model.interfaces.IParent;
import ru.dip.core.model.interfaces.IDipElement;

public class DipReservedUnit extends DipElement {
	
	public static final String EXTENSION = "rsvd";
	
	public static DipReservedUnit instance(IFile resource, IParent parent) {
		IDipElement element = DipRoot.getInstance().getElement(resource, parent, DipElementType.RESERVED_UNIT);
		if (element == null) {
			DipReservedUnit dipReserveUnit = new DipReservedUnit(resource, parent);
			DipRoot.getInstance().putElement(dipReserveUnit);
			return dipReserveUnit;
		} else {
			return (DipReservedUnit) element;
		}
	}
	
	private DipReservedUnit(IFile file, IParent parent) {
		super(file, parent);
	}

	@Override
	public DipElementType type() {
		return DipElementType.RESERVED_UNIT;
	}
	
	@Override
	public IFile resource() {
		return (IFile) super.resource();
	}

}
