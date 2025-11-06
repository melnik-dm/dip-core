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
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

public class CompositeBuilder {
	
	public static CompositeBuilder instance(Composite parent) {
		return new CompositeBuilder(parent);
	}
	
	private Composite fParent;
	// span
	private int fColumns = 1;
	private boolean fEqualWidth = true;
	private int fStyle = SWT.NONE;
	// layout
	private int fMarginHeight = 5;
	private int fMarginWidth = 5;
	private int fHorizontalSpacing = 5;
	private int fVerticalSpacing = 5;
	// layout_data
	private int fDataStyle = SWT.NONE;
	private int fVerticalSpan = 1;
	private int fHorizontalSpan = 1;
	private boolean fHorizontalGrab = false;
	private boolean fVerticalGrab = false;	
	// background
	private Color fBackground;
	
	
	private CompositeBuilder(Composite parent) {
		fParent = parent;
	}
	
	public CompositeBuilder columns(int columns) {
		fColumns = columns;
		return this;
	}
	
	public CompositeBuilder columns(int columns, boolean equal) {
		fColumns = columns;
		fEqualWidth = equal;
		return this;
	}
	
	public CompositeBuilder border() {
		fStyle = SWT.BORDER;
		return this;
	}
	
	//===============
	// Indents
	
	public CompositeBuilder notIndetns() {
		fMarginHeight = 0;
		fMarginWidth = 0;
		fHorizontalSpacing = 0;
		fVerticalSpacing = 0;
		return this;
	}
	
	public CompositeBuilder horizontalSpacing(int spacing) {
		fHorizontalSpacing = spacing;
		return this;
	}
	
	public CompositeBuilder verticallSpacing(int spacing) {
		fVerticalSpacing = spacing;
		return this;
	}

	public CompositeBuilder marginHeights(int indent) {
		fMarginHeight = indent;
		return this;
	}
	
	//================
	// GRID DATA
	
	public CompositeBuilder full() {
		fDataStyle |= GridData.FILL_BOTH;
		return this;
	}
	
	public CompositeBuilder horizontal() {
		fDataStyle |= GridData.FILL_HORIZONTAL;
		return this;
	}
	
	public CompositeBuilder verticalBeginning() {		
		fDataStyle |= GridData.VERTICAL_ALIGN_BEGINNING;
		return this;
	}
	
	public CompositeBuilder rightAlign() {		
		fDataStyle |= GridData.HORIZONTAL_ALIGN_END;
		return this;
	}
	
	public CompositeBuilder vericalSpan(int span) {
		fVerticalSpan = span;
		return this;
	}
	
	public CompositeBuilder horizontalSpan(int span) {
		fHorizontalSpan = span;	
		return this;
	}
	
	public CompositeBuilder horizontalGrab() {
		fHorizontalGrab = true;
		return this;
	}
	
	public CompositeBuilder verticalGrab() {
		fVerticalGrab = true;
		return this;
	}
	
	public CompositeBuilder fullGrab() {
		fVerticalGrab = true;
		fHorizontalGrab = true;
		return this;
	}
	
	//==================
	// color
	
	public CompositeBuilder background(Color background) {
		fBackground = background;
		return this;
	}
	
	//==================
	// build

	public Composite build() {
		Composite composite = new Composite(fParent, fStyle);		
		buildLayout(composite);
		buildData(composite);
		if (fBackground != null) {
			composite.setBackground(fBackground);
		}		
		return composite;
	}
	
	private void buildLayout(Composite composite) {
		GridLayout layout = (fColumns == 1) ? new GridLayout() : new GridLayout(fColumns, fEqualWidth);		
		layout.marginHeight = fMarginHeight;
		layout.marginWidth = fMarginWidth;
		layout.horizontalSpacing = fHorizontalSpacing;
		layout.verticalSpacing = fVerticalSpacing;
		composite.setLayout(layout);
	}
	
	private void buildData(Composite composite) {
		GridData gd = new GridData(fDataStyle);
		gd.verticalSpan = fVerticalSpan;
		gd.horizontalSpan = fHorizontalSpan;
		if (fHorizontalGrab) {
			gd.grabExcessHorizontalSpace = fHorizontalGrab;
		}
		if (fVerticalGrab) {
			gd.grabExcessVerticalSpace = fVerticalGrab;
		}
		composite.setLayoutData(gd);
	}
}
