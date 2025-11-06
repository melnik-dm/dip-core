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
package ru.dip.core.unit.md;

import java.util.List;
import java.util.stream.Collectors;

import ru.dip.core.model.interfaces.IDipElement;
import ru.dip.core.model.interfaces.IDipParent;
import ru.dip.core.model.interfaces.IDipUnit;
import ru.dip.core.unit.UnitType;

public class SubMarkdownPresentation extends MarkDownPresentation {

	public SubMarkdownPresentation(IDipUnit unit) {
		super(unit);
	}
	
	public String getNumber() {
		IDipParent parent = getUnit().parent();
		if (parent.isActiveNumeration()) {
			List<IDipElement> submdUnits = parent.getChildren()
					.stream()
					.filter(e -> e instanceof IDipUnit && ((IDipUnit)e).getUnitType() == UnitType.SUBMARKDOWN)			
					.collect(Collectors.toList());
			int index = submdUnits.indexOf(getUnit().strong()) + 1;
			String number = parent.number() + "." + index + " ";			
			return number;
		}
		return null;
	}
			
}
