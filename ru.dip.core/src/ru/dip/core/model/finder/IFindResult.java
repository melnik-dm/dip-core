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
package ru.dip.core.model.finder;

import java.util.List;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;

import ru.dip.core.utilities.ui.swt.ColorProvider;

public interface IFindResult {
	
	public static final Color FIND_COLOR = ColorProvider.SELECT;
	public static final Color CURRENT_POINT = ColorProvider.LIGHT_BLUE;
			
	List<Point> getFindPoints();

	default int size() {
		return getFindPoints().size();
	}

	default void updateFindPoints(String content) {};

}
