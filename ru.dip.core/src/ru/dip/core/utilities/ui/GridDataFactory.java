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
package ru.dip.core.utilities.ui;

import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Control;

public class GridDataFactory {
	
	public static void applyHorizontalSpan(Control control, int span) {
		GridData gd = new GridData();
		gd.horizontalSpan = span;
		control.setLayoutData(gd);		
	}
	
	public static void applyFillBoth(Control control) {
		control.setLayoutData(new GridData(GridData.FILL_BOTH));		
	}
	
	public static void applyFillHorizontal(Control control) {
		control.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
	}
	
	public static void applyVerticalBegining(Control control) {
		control.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING));
	}
	
	public static GridData spanGridData(int horizontalSpan) {
		GridData gd = new GridData();
		gd.horizontalSpan = horizontalSpan;		
		return gd;
	}
	
	public static GridData widthGridData(int width) {
		GridData gd = new GridData();
		gd.widthHint = width;		
		return gd;
	}

}
