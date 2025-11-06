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
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import ru.dip.core.model.DipProject;
import ru.dip.core.model.interfaces.IDipParent;
import ru.dip.core.utilities.DipUtilities;
import ru.dip.core.utilities.ui.WSelectionListener;
import ru.dip.ui.Messages;

public class BrowseProjectFolderComposite extends Composite {

	private Text fText;
	private Button fBrowseButton;
	private IDipParent fStartFolder;
	private DipProject fProject;
	
	private IBrowseCompositeListener fListener;
	
	public BrowseProjectFolderComposite(Composite parent, String label, IDipParent startFolder) {
		super(parent, SWT.NULL);
		fStartFolder = startFolder;
		fProject = fStartFolder.dipProject();
		
		GridLayout layout = new GridLayout(3, false);
		layout.marginLeft = 0;
		layout.marginWidth = 0;
		setLayout(layout);
		setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		Label l = new Label(this, SWT.NONE);
		l.setText(label);
		createContent();
	}
	
	protected void createContent(){
		fText = new Text(this, SWT.BORDER | SWT.READ_ONLY);
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
	
	protected void perfomButton(){		
		selectFolder();
	}
	
	protected void selectFolder() {
		IDipParent parent = FileDialogs.getFolderFromDipProject(getShell(), fProject, fStartFolder);
		if (parent != null && parent != fStartFolder) {
			fStartFolder = parent;
			String newText = DipUtilities.relativeProjectID(parent);
			fText.setText(newText);	
			if (fListener != null) {
				fListener.browseChanged();
			}
		}
	}
	
	protected void applyNewValue(String newText) {
		if (newText != null) {
			fText.setText(newText);
		}
	}
	
	public void addBrowseListener(IBrowseCompositeListener listener) {
		fListener = listener;
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
		
	public IDipParent getDipParent() {
		return fStartFolder;
	}

}
