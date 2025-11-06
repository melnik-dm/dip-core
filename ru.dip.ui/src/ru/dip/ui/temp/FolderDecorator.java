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
package ru.dip.ui.temp;

import org.eclipse.jface.viewers.ILabelDecorator;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.swt.graphics.Image;

import ru.dip.core.model.DipFolder;
import ru.dip.core.utilities.DnfoUtils;
import ru.dip.ui.preferences.OtherPreferences;

public class FolderDecorator implements ILabelDecorator {

	@Override
	public void addListener(ILabelProviderListener listener) {}

	@Override
	public void dispose() {}

	@Override
	public boolean isLabelProperty(Object element, String property) {
		return false;
	}

	@Override
	public void removeListener(ILabelProviderListener listener) {}

	@Override
	public Image decorateImage(Image image, Object element) {
		return null;
	}

	@Override
	public String decorateText(String text, Object element) {
		if (!OtherPreferences.isShowDescInProjectExplorer()) {
			return null;
		}
		if (element instanceof DipFolder) {			
			DipFolder folder = (DipFolder) element;
			String description = DnfoUtils.getDescription(folder.resource());
	        if (description != null && !description.isEmpty()) {			        			        
	        	return text + " [" + description + "]";
	        }
		}
		return text;
	}

}
