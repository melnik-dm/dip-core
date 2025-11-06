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
package ru.dip.ui.variable.view;

import org.eclipse.jface.viewers.ITreeContentProvider;

import ru.dip.core.model.vars.IVarContainer;

public class VariablesContentProvider implements ITreeContentProvider {
	@Override
	public boolean hasChildren(Object element) {				
		if (element instanceof IVarContainer) {
			return  !((IVarContainer) element).getChildren().isEmpty();
		}
		return false;
	}
	
	@Override
	public Object getParent(Object element) {
		return null;
	}
	
	@Override
	public Object[] getElements(Object inputElement) {
		if (inputElement instanceof IVarContainer){
			IVarContainer glossFolder = (IVarContainer) inputElement;
			return glossFolder.getChildren().toArray();
		}
		return new Object[0];
	}
	
	@Override
	public Object[] getChildren(Object parentElement) {
		if (parentElement instanceof IVarContainer) {
			return  ((IVarContainer) parentElement).getChildren().toArray();
		}			
		return null;
	}
}