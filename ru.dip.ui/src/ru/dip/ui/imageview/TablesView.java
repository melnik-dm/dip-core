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
package ru.dip.ui.imageview;

import java.util.ArrayList;
import java.util.stream.Collectors;

import ru.dip.core.model.DipProject.ProjectTablesListener;
import ru.dip.core.model.interfaces.IDipElement;
import ru.dip.core.model.interfaces.IDipParent;
import ru.dip.core.model.interfaces.IParent;
import ru.dip.core.utilities.DipUtilities;
import ru.dip.ui.Messages;

public class TablesView extends ImagesView implements ProjectTablesListener {

	public static final String ID = Messages.TablesView_ID;

	@Override
	protected void getUnitsFromProject() {
		if (getPreferences().isFolderMode()) {
			if (fCurrentFile != null) {
				IDipElement element = DipUtilities.findElement(fCurrentFile);
				if (element != null) {
					IParent parent = element.parent();
					if (parent instanceof IDipParent) {
						fUnits = fProject.tables().stream()
								.filter(im -> im.parent().equals(parent))
								.collect(Collectors.toList());
						return;
					}
				}
			}
		}
		fUnits = new ArrayList<>(fProject.tables());
	}

	@Override
	public void tablesChanged() {
		if (isVisible()) {
			imagesChanged();
		}
	}

}