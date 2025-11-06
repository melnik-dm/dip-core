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

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

public class CompositeFactory {
	
	public static Composite full(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout());
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		return composite;
	}
	
	public static Composite fullHorizontal(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout());
		composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		return composite;
	}
	
	public static Composite withGdStyle(Composite parent, int gdStyle) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout());
		composite.setLayoutData(new GridData(gdStyle));
		return composite;
	}
	
	public static Composite fullBorder(Composite parent) {
		Composite composite = new Composite(parent, SWT.BORDER);
		composite.setLayout(new GridLayout());
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		return composite;
	}
	
	public static Composite fullBorder(Composite parent, int gdStyle) {
		Composite composite = new Composite(parent, SWT.BORDER);
		composite.setLayout(new GridLayout());
		composite.setLayoutData(new GridData(gdStyle));
		return composite;
	}
	
	public static Composite full(Composite parent, int numColumns, boolean makeColumnsEqualWidth) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout(numColumns, makeColumnsEqualWidth));
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		return composite;
	}
	
	public static Composite fullBorder(Composite parent, int numColumns, boolean makeColumnsEqualWidth) {
		Composite composite = new Composite(parent, SWT.BORDER);
		composite.setLayout(new GridLayout(numColumns, makeColumnsEqualWidth));
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		return composite;
	}

	public static Composite noIndent(Composite parent,  int numColumns, boolean makeColumnsEqualWidth) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(numColumns, makeColumnsEqualWidth);
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		composite.setLayout(layout);
		return composite;
	}

}
