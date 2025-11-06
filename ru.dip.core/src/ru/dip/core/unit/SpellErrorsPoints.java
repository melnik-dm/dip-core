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
package ru.dip.core.unit;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.graphics.Point;

import ru.dip.core.model.interfaces.ISpellErrorPoints;
import ru.dip.core.utilities.spellcheck.Dictionary;

public class SpellErrorsPoints implements ISpellErrorPoints {

	private List<Point> fErrorPoints = new ArrayList<>();
	
	@Override
	public List<Point> errorsPoints() {
		return fErrorPoints;
	}

	@Override
	public void findErrorWords(String text) {
		fErrorPoints = Dictionary.instance().checkText(text);
	}

}
