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
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import ru.dip.ui.Messages;

public class BrowseComposite extends Composite {

	private static final String USER_HOME = "user.home";
	
	private Text fText;
	private Button fBrowseButton;	
	
	public BrowseComposite(Composite parent, String label) {
		super(parent, SWT.NULL);
		setLayout(new GridLayout(3, false));
		setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		Label l = new Label(this, SWT.NONE);
		l.setText(label);
		createContent(SWT.BORDER);
	}
	
	public BrowseComposite(Composite parent, String label , int style) {
		super(parent, SWT.NULL);
		setLayout(new GridLayout(3, false));
		setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		Label l = new Label(this, SWT.NONE);
		l.setText(label);
		createContent(SWT.BORDER | style);
	}
	
	protected void createContent(int style){
		fText = new Text(this, SWT.BORDER);
		fText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));		
		fBrowseButton = new Button(this, SWT.PUSH);
		fBrowseButton.setText(Messages.BrowseComposite_BrowseButton);
		fBrowseButton.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				perfomButton();
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});		
	}
	
	protected void perfomButton(){
		String newText = FileDialogs.getFileFromFileSystemDialog(getShell(), System.getProperty(USER_HOME));
		if (newText != null)
			fText.setText(newText);
	}	
	
	public String getValue(){
		return fText.getText().trim();
	}
	
	public void setValue(String text){
		fText.setText(text);
	}	
	
	public Text getTextControl(){
		return fText;
	}

}
