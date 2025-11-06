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
package ru.dip.editors.navigator;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;

import ru.dip.core.model.interfaces.IDipElement;
import ru.dip.core.model.interfaces.IDipDocumentElement;
import ru.dip.core.utilities.DipUtilities;
import ru.dip.core.utilities.WorkbenchUtitlities;
import ru.dip.ui.action.hyperlink.ReqLink;

public class OpenInDocumentHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {		
		IFile file = WorkbenchUtitlities.getFileFromOpenedEditor();
		if (file == null) {
			return null;
		}
		
		IDipElement element=  DipUtilities.findElement(file);
		if (element instanceof IDipDocumentElement) {
			ReqLink.openElementInTable((IDipDocumentElement) element);
		}
		return null;
	}

}
