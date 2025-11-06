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

import org.eclipse.jface.viewers.StyledCellLabelProvider;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.model.WorkbenchLabelProvider;

import ru.dip.core.model.DipFolder;
import ru.dip.core.utilities.ui.swt.ColorProvider;

public class DipProjectLabelProvider extends StyledCellLabelProvider {

	private static final WorkbenchLabelProvider workbenchLabelProvider = new WorkbenchLabelProvider();

	public static DipProjectLabelProvider instance() {
		return new DipProjectLabelProvider();
	}

	private DipProjectLabelProvider() {}
	
	@Override
	public void update(ViewerCell cell) {
		// не отображаются инклюды !!!
		String text = workbenchLabelProvider.getText(cell.getElement());
		cell.setImage(getImage(cell.getElement()));
		String decorateText = descorateText(cell.getElement(), text);
		cell.setText(decorateText);
		if (decorateText.length() > text.length()) {
			StyleRange range = new StyleRange(text.length(), decorateText.length() - text.length(), ColorProvider.LIGHT_BLUE,  null);
			cell.setStyleRanges(new StyleRange[] {range});			
		}
	}
	
	private Image getImage(Object element) {
		return workbenchLabelProvider.getImage(element);
	}

	private String descorateText(Object element, String text) {
		if (element instanceof DipFolder) {
			String description = ((DipFolder) element).description();
			if (description != null && !description.isEmpty()) {
				return text + " [" + description + "]";
			}
		}
		return text;
	}
}
