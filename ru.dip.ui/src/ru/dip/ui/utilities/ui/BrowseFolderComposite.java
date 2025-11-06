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
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import ru.dip.core.utilities.ui.WSelectionListener;
import ru.dip.ui.Messages;

public class BrowseFolderComposite extends Composite {

	private String fTitle = "Select Folder";
	private Text fText;
	private Button fBrowseButton;
	private String fStartDirectory;

	public BrowseFolderComposite(Composite parent, String label) {
		super(parent, SWT.NULL);
		GridLayout layout = new GridLayout(3, false);
		layout.marginLeft = 0;
		layout.marginWidth = 0;
		setLayout(layout);
		setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		Label l = new Label(this, SWT.NONE);
		l.setText(label);
		createContent();
	}

	protected void createContent() {
		fText = new Text(this, SWT.BORDER);
		fText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		fBrowseButton = new Button(this, SWT.PUSH);
		fBrowseButton.setText(Messages.BrowseFolderComposite_BrowseButton);
		fBrowseButton.addSelectionListener(new WSelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				perfomButton();
			}

		});
	}

	protected void perfomButton() {
		String newText = selectFolder();
		applyNewValue(newText);
	}

	protected String selectFolder() {
		if (fStartDirectory == null) {
			fStartDirectory = System.getProperty(Messages.BrowseFolderComposite_UserHomeProperty);
		}
		return FileDialogs.getFolderFromFileSystem(getShell(), fTitle, fStartDirectory);
	}

	public void addModifyListener(ModifyListener listener) {
		fText.addModifyListener(listener);
	}

	protected void applyNewValue(String newText) {
		if (newText != null) {
			fText.setText(newText);
		}
	}

	public String getValue() {
		return fText.getText().trim();
	}

	public void setValue(String text) {
		fText.setText(text);
	}

	public Text getTextControl() {
		return fText;
	}

	public void setStartDirectory(String path) {
		fStartDirectory = path;
	}

	public void setTitle(String title) {
		fTitle = title;
	}

}
