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
package ru.dip.core.utilities.spellcheck;

import org.eclipse.swt.graphics.Point;

import ru.dip.core.model.interfaces.IDipElement;

public class SpellCheckError {
	
	private IDipElement fSource;
	private String fError;
	private Point fLocation;
	
	public SpellCheckError(IDipElement source, String error, Point location) {
		fSource = source;
		fError = error;
		fLocation = location;
	}

	public IDipElement source() {
		return fSource;
	}
	
	public String error() {
		return fError;
	}
	
	public Point location() {
		return fLocation;
	}
	
}
