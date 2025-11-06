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
package ru.dip.core.model.glossary;

import org.eclipse.core.resources.IResource;

import ru.dip.core.model.DipElementType;
import ru.dip.core.model.DipRoot;
import ru.dip.core.model.DipUnit;
import ru.dip.core.model.interfaces.IParent;
import ru.dip.core.model.interfaces.IDipElement;

public class GlossRef extends DipUnit {

	public static GlossRef instance(IResource resource, IParent parent) {
		IDipElement element = DipRoot.getInstance().getElement(resource, parent, DipElementType.GLOSS_REF);
		if (element == null) {
			GlossRef glossRef = new GlossRef(resource, parent);
			DipRoot.getInstance().putElement(glossRef);
			return glossRef;
		} else {
			return (GlossRef) element;
		}
	}
	
	private GlossRef(IResource resource, IParent parent) {
		super(resource, parent);
	}

	@Override
	public <T> T getAdapter(Class<T> adapter) {
		return null;		
	}
}
