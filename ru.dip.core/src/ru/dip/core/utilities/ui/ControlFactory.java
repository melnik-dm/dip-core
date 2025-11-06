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
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

public class ControlFactory {
	
	public static Label label(Composite parent, String text) {
		Label label =  new Label(parent, SWT.NONE);
		label.setText(text);
		return label;
	}
	
	public static Label label(Composite parent, String text, Font font) {
		Label label = label(parent, text);  
		label.setFont(font);
		return label;
	}
	
	public static Label label(Composite parent, String text, Color fg) {
		Label label = label(parent, text);  
		label.setForeground(fg);
		return label;
	}
	
	public static Label emptyLabel(Composite parent) {
		return new Label(parent, SWT.NONE);
	}
	
	public static Label imageLabel(Composite parent, Image image) {
		Label label = new Label(parent, SWT.NONE);
		label.setImage(image);
		return label;
	}
	
	public static StyledText textLabel(Composite parent, String text) {
		StyledText label = new StyledText(parent, SWT.READ_ONLY | SWT.MULTI | SWT.WRAP | SWT.V_SCROLL);
		label.setText(text);
		return label;
	}
	
}
