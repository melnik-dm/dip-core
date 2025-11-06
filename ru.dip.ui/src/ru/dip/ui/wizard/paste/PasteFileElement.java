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
package ru.dip.ui.wizard.paste;

import java.nio.file.Files;
import java.nio.file.Paths;

import ru.dip.core.model.interfaces.IDipParent;

public class PasteFileElement extends PasteElement {

	private java.nio.file.Path fPath;
		
	public PasteFileElement(IDipParent targetParent, String filePath) {
		super(targetParent);
		fPath = Paths.get(filePath);
		setNewName(fPath.getFileName().toString());
	}

	@Override
	public boolean isFolder() {
		return Files.isDirectory(fPath);
	}
	
	@Override
	public java.nio.file.Path getPath() {
		return fPath;
	}

	@Override
	public String getOldName() {
		return fPath.getFileName().toString();
	}
}
