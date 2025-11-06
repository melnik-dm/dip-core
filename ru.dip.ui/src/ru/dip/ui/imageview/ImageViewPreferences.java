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

import org.eclipse.jface.preference.IPreferenceStore;

import ru.dip.ui.ReqUIPlugin;
import ru.dip.ui.imageview.interfaces.IImageViewPreferences;

public class ImageViewPreferences implements IImageViewPreferences {

	public static final String IMAGE_VIEW_ALLIGNMENT = "_image_view_allignment"; //$NON-NLS-1$
	public static final String IMAGE_VIEW_HORIZONTAL = "_image_view_horizontal"; //$NON-NLS-1$
	public static final String IMAGE_VIEW_FOLDER_MODE = "_image_view_folder_mode"; //$NON-NLS-1$

	public static void setDefaultValues(IPreferenceStore store) {
		store.setDefault(IMAGE_VIEW_ALLIGNMENT, 0);
		store.setDefault(IMAGE_VIEW_HORIZONTAL, false);
		store.setDefault(IMAGE_VIEW_FOLDER_MODE, false);
	}

	private ImageViewAllignment getAllignmentFromPreferences() {
		int value = ReqUIPlugin.getDefault().getPreferenceStore().getInt(IMAGE_VIEW_ALLIGNMENT);
		return ImageViewAllignment.values()[value];
	}

	private boolean getHorizontalFromPreferences() {
		return ReqUIPlugin.getDefault().getPreferenceStore().getBoolean(IMAGE_VIEW_HORIZONTAL);
	}

	private boolean getFolderModeFromPreferences() {
		return ReqUIPlugin.getDefault().getPreferenceStore().getBoolean(IMAGE_VIEW_FOLDER_MODE);
	}

	// mode
	private boolean fHorizontalMode; // поменять на енам
	private ImageViewAllignment fAlligment; // поменять на енам
	private boolean fFolderMode;

	public ImageViewPreferences() {
		fAlligment = getAllignmentFromPreferences();
		fHorizontalMode = getHorizontalFromPreferences();
		fFolderMode = getFolderModeFromPreferences();
	}

	@Override
	public boolean isFolderMode() {
		return fFolderMode;
	}

	@Override
	public boolean isHorizontalMode() {
		return fHorizontalMode;
	}

	@Override
	public ImageViewAllignment getAllignment() {
		return fAlligment;
	}

	@Override
	public void setFolderMode(boolean value) {
		fFolderMode = value;
		ReqUIPlugin.getDefault().getPreferenceStore().setValue(IMAGE_VIEW_FOLDER_MODE, value);
	}

	@Override
	public void setHorizontalMode(boolean horizontalMode) {
		fHorizontalMode = horizontalMode;
		ReqUIPlugin.getDefault().getPreferenceStore().setValue(IMAGE_VIEW_HORIZONTAL, fHorizontalMode);
	}

	@Override
	public void nextAllignment() {
		if (fAlligment == ImageViewAllignment.IMAGE_RIGHT) {
			fAlligment = ImageViewAllignment.VERTICAL_ORDER;
		} else if (fAlligment == ImageViewAllignment.VERTICAL_ORDER) {
			fAlligment = ImageViewAllignment.IMAGE_LEFT;
		} else {
			fAlligment = ImageViewAllignment.IMAGE_RIGHT;
		}
		ReqUIPlugin.getDefault().getPreferenceStore().setValue(IMAGE_VIEW_ALLIGNMENT, fAlligment.ordinal());
	}

}
