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

import org.eclipse.core.resources.IResource;

import ru.dip.core.model.interfaces.IParent;
import ru.dip.core.model.interfaces.IDipElement;

public class TocRef extends DipUnit {
	
	public static TocRef instance(IResource resource, IParent parent) {
		IDipElement element = DipRoot.getInstance().getElement(resource, parent, DipElementType.TOC_REF);
		if (element == null) {
			TocRef unit = new TocRef(resource, parent);
			DipRoot.getInstance().putElement(unit);
			return unit;
		} else {
			return (TocRef) element;
		}
	}

	private TocRef(IResource resource, IParent parent) {
		super(resource, parent);
	}

	@Override
	public <T> T getAdapter(Class<T> adapter) {
		return null;		
	}
}
