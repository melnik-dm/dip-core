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

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;

public class TreeViewerUtils {

	@SuppressWarnings("unchecked")
	public static <T> T getSelectedObject(TreeViewer viewer, Class<T> objectClass) {
		IStructuredSelection selection = viewer.getStructuredSelection();
		if (selection != null && !selection.isEmpty()) {
			Object obj = selection.getFirstElement();			
			if (objectClass.isInstance(obj)) {
				return (T) obj;
			}
		}		
		return null;
	}

}
