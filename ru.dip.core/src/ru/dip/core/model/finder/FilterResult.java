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

import org.eclipse.swt.graphics.Point;

public class FilterResult implements IFindResult {

	private final List<Point> fFindedPoints;

	public FilterResult(List<Point> points) {
		fFindedPoints = points;
	}

	public void append(List<Point> points) {
		fFindedPoints.addAll(points);
	}

	@Override
	public List<Point> getFindPoints() {
		return fFindedPoints;
	}

}
