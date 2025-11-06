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

import org.eclipse.swt.graphics.Image;

import ru.dip.core.model.interfaces.IDipUnit;
import ru.dip.core.model.interfaces.INotTextPresentation;

public class TocRefPresentation extends TablePresentation implements INotTextPresentation {

	public static final String FILE_NAME = ".tocref";
	private static final String TOC = "Содержание";
	
	public TocRefPresentation(IDipUnit unit) {
		super(unit);
	}

	@Override
	public boolean checkUpdate(){		
		return false;
	}	
	
	@Override
	protected void read() {

	}

	@Override
	public String getText() {		
		return TOC;
	}

	@Override
	public Image getImage() {
		return null;
	}

}
