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

import org.eclipse.core.resources.IFolder;

import ru.dip.core.model.interfaces.IDipParent;
import ru.dip.core.model.interfaces.IParent;

public class Appendix extends DipFolder {

	public static boolean isAppendix(IParent parent, String name) {
		return parent instanceof DipProject && "appendix".equalsIgnoreCase(name);
	}
	
	public static boolean isAppendixPartition(IDipParent container) {		
		return container.parent() != null && container.parent() instanceof Appendix;
	}
	
	protected Appendix(IFolder container, IParent parent) {
		super(container, parent);
		setActiveNumeration(false);
	}
	
	@Override
	public boolean isActiveNumeration() {
		return false;
	}

}
