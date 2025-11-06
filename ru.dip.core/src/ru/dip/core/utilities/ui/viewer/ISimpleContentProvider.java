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
package ru.dip.core.utilities.ui.viewer;

import org.eclipse.jface.viewers.ITreeContentProvider;

/**
 * ITreeContentProvider без дочерних элементов
 */
public interface ISimpleContentProvider extends ITreeContentProvider {

	@Override
	default boolean hasChildren(Object element) {
		return false;
	}

	@Override
	default Object[] getChildren(Object parentElement) {
		return null;
	}

	@Override
	default Object getParent(Object element) {
		return null;
	}

}
