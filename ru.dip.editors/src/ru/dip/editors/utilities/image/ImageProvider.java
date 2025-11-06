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
package ru.dip.editors.utilities.image;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

public class ImageProvider {
	
	// DIP VIEW
	public static final Image ZOOM_PLUS = new Image(Display.getCurrent(),
			ImageProvider.class.getResourceAsStream("zoom_plus.gif"));
	public static final Image ZOOM_MINUS = new Image(Display.getCurrent(),
			ImageProvider.class.getResourceAsStream("zoom_minus.gif"));
	public static final Image DEFAULT_SIZE = new Image(Display.getCurrent(),
			ImageProvider.class.getResourceAsStream("default.png"));
	public static final Image VIEW_SIZE = new Image(Display.getCurrent(),
			ImageProvider.class.getResourceAsStream("view_size.gif"));

}
