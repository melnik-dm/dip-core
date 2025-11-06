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
package ru.dip.ui.wizard.demo;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

import ru.dip.core.utilities.DipUtilities;
import ru.dip.ui.Messages;
import ru.dip.ui.utilities.ui.EntryTextComposite;

public class NewDemoProjectPage extends WizardPage {

	private EntryTextComposite fNameEntry;
	private String fDefaultName;
		
	public NewDemoProjectPage(String pageName, String title, String defaultName) {
		super(pageName);
		setTitle(title);
		setDescription(Messages.NewDemoProjectPage_Description);
		fDefaultName = defaultName;
	}
	

	@Override
	public void createControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout());
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		fNameEntry = new EntryTextComposite(composite, Messages.NewDemoProjectPage_ProjectNameLabel);
		fNameEntry.setValue(getStartProjectName());
		fNameEntry.addModifyListener(new ModifyListener() {

			@Override
			public void modifyText(ModifyEvent e) {
				validate();
			}			
		});
		
		setControl(composite);
	}
	
	private String getStartProjectName() {
		String result = fDefaultName;
		if (DipUtilities.checkProjectName(result).isOK()) {
			return result;
		}
		int i = 1;		
		while(true) {
			StringBuilder builder = new StringBuilder(fDefaultName + "_"); //$NON-NLS-1$
			builder.append(i);
			if (DipUtilities.checkProjectName(builder.toString()).isOK()) {
				return builder.toString();
			}
			i++;
		}
	}

	private void validate() {
		String projectName = fNameEntry.getValue();
		if (projectName.isEmpty()) {
			setErrorMessage(Messages.NewDemoProjectPage_InputProjectNameLabel);
			setPageComplete(false);
		}
		
		IStatus status = DipUtilities.checkProjectName(projectName);
		if (status.isOK()){
			setErrorMessage(null);
			setPageComplete(true);
			return;
		} else {
			setErrorMessage(status.getMessage());
			setPageComplete(true);
		}
	}
	
	public String getProjectName() {
		return fNameEntry.getValue();
	}
}
