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
package ru.dip.core.utilities;

import org.eclipse.core.resources.IFile;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;

import ru.dip.core.unit.UnitType;

public class EditorUtils {
	
	// editor events
	public static final int SAVE_EVENT = 88;
	public static final int VISIBLE_EVENT = 89;
	public static final int HIDE_EVENT = 90;
	
	public static IFile getFileFromEditor(IEditorPart editorPart) {
		IEditorInput editorInput = editorPart.getEditorInput();
		if (editorInput instanceof IFileEditorInput) {
			return ((IFileEditorInput)editorInput).getFile();
		}
		return null;
	}
	
	public static UnitType getEditorType(IEditorPart editorPart) {
		IFile file = getFileFromEditor(editorPart);
		if (file == null) {
			return UnitType.UNDEFINE;
		}
		return UnitType.defineUnitType(file.getName());
	}
}
