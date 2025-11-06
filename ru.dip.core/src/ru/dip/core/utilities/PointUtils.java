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
package ru.dip.core.utilities;

import java.util.List;

import org.eclipse.swt.graphics.Point;

public class PointUtils {

	/**
	 * Содержит (пересекается) ли список позиций (начало, конец) указанную позицию
	 * (start, end)
	 */
	public static boolean containsPosition(List<Point> points, int start, int end) {
		return points.parallelStream().anyMatch(p -> containsOffset(p, start, end));
	}

	/**
	 * Содержит (пересекается) ли позиция (начало, конец) указанную позицию (start,
	 * end)
	 */
	private static boolean containsOffset(Point point, int start, int end) {
		return containsOffset(point, start) || containsOffset(point, end);
	}

	/**
	 * Содержит ли позиция (начало, конец) указанный offset
	 */
	private static boolean containsOffset(Point point, int offset) {
		return offset >= point.x && offset <= point.y;
	}

}
