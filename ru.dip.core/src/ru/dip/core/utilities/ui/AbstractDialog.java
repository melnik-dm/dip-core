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

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Shell;

public abstract class AbstractDialog extends Dialog {

	private String fTitle;
	private Point fSize;
	
	
	public AbstractDialog(Shell parentShell, String title, Point size) {
		super(parentShell);
		fTitle = title;
		fSize = size;
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(fTitle);
	}
	
	@Override
	protected boolean isResizable() {
		return true;
	}
	
	@Override
	protected Point getInitialSize() {
		Point p =  super.getInitialSize();
		if (p.x < fSize.x) {
			p.x= fSize.x; 
		}
		if (p.y < fSize.y) {
			p.y= fSize.y; 
		}
		return p;
	}

}
