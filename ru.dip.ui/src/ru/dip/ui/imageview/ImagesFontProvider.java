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

import org.eclipse.swt.graphics.Font;

import ru.dip.core.utilities.ui.swt.FontDimension;
import ru.dip.core.utilities.ui.swt.FontManager;

public class ImagesFontProvider {

	private Font fFont = FontManager.getMonoFont(10);
	private FontDimension fFontDimension = FontDimension.createFontSize(fFont);

	public Font getFont() {
		return fFont;
	}

	public String getWrapIDText(String original, int width) {
		int strLength = fFontDimension.getStringLength(width);
		return FontDimension.getIDWrapText(original, strLength);
	}

	public String getWrapText(String original, int width) {
		int strLength = fFontDimension.getStringLength(width);
		return FontDimension.getWrapText(original, strLength);
	}

	public void dispose() {
		fFont.dispose();
	}

}
