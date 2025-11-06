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
package ru.dip.ui.utilities.ui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class EntryTextComposite extends Composite {

	private Text fText;

	public EntryTextComposite(Composite parent, String labelText) {
		this(parent, labelText, GridData.FILL_HORIZONTAL);
	}
	
	public EntryTextComposite(Composite parent, String labelText, int gridStyle) {
		super(parent, SWT.NULL);
		setLayout(new GridLayout(2, false));
		setLayoutData(new GridData(gridStyle));
		createContent(labelText);
	}

	private void createContent(String labelText){
		Label label = new Label(this, SWT.NONE);
		label.setText(labelText);
		fText = new Text(this, SWT.BORDER);
		fText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
	}
	
	public void addModifyListener(ModifyListener listener){
		fText.addModifyListener(listener);
	}
	
	@Override
	public boolean setFocus() {
		return fText.setFocus();
	}
	
	@Override
	public void setEnabled(boolean enabled) {		
		super.setEnabled(enabled);
		fText.setEnabled(enabled);
	}
	
	//========================
	// getters & setters
	
	public String getValue(){
		return fText.getText().trim();
	}
	
	public void setValue(String value){
		fText.setText(value);
	}
	
	public Text getTextControl(){
		return fText;
	}
	
}
