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
package ru.dip.ui.utilities.dip;

import org.eclipse.core.resources.IFile;
import org.eclipse.swt.widgets.Display;

import ru.dip.core.model.interfaces.IDipUnit;
import ru.dip.core.model.interfaces.IDipUnitProvider;
import ru.dip.core.utilities.DipUtilities;
import ru.dip.core.utilities.WorkbenchUtitlities;
import ru.dip.ui.action.hyperlink.ReqLink;

public class DipUnitManager {

	private final IDipUnitProvider fUnitProvider;

	public DipUnitManager(IDipUnitProvider unitProvider) {
		fUnitProvider = unitProvider;
	}

	public void copyID() {
		IDipUnit unit = fUnitProvider.getDipUnit();
		if (unit != null) {
			DipUtilities.copyRelativeIdClipboard(unit, Display.getDefault());
		}
	}

	public void fullCopyID() {
		IDipUnit unit = fUnitProvider.getDipUnit();
		if (unit != null) {
			DipUtilities.copyFullIdClipboard(unit, Display.getDefault());
		}
	}

	public void openInEditor() {
		IDipUnit unit = fUnitProvider.getDipUnit();
		if (unit != null) {
			IFile file = (IFile) unit.resource();
			WorkbenchUtitlities.openFile(file);
		}
	}

	public void openInDocument() {
		IDipUnit unit = fUnitProvider.getDipUnit();
		if (unit != null) {
			ReqLink.openElementInTable(unit);
		}
	}

}
