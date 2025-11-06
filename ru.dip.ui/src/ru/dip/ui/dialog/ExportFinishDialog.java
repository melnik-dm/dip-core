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
package ru.dip.ui.dialog;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import ru.dip.core.utilities.TagStringUtilities;
import ru.dip.core.utilities.ui.CompositeBuilder;
import ru.dip.ui.Messages;
import ru.dip.ui.export.error.IExportError;

public class ExportFinishDialog extends Dialog {

	private static final int SHOW_IN = 21;
	private String fPath;
	private String fLabel;
	private List<IExportError> fErrors;
	
	public ExportFinishDialog(Shell parentShell, String path, String label, List<IExportError> errors) {
		super(parentShell);
		fPath = path;
		fLabel = label;
		fErrors = errors;
	}
	
	@Override
	protected void configureShell(Shell newShell) {	
		super.configureShell(newShell);
		newShell.setText(Messages.ExportFinishDialog_Shell_title);
	}
	
	@Override
	protected Point getInitialSize() {
		Point p = super.getInitialSize();
		
		
		if (hasErrors()) {
			p.x = 820;
			if (p.y > 500) {
				p.y = 500;
			}
		} else {
			if (p.x < 600) {
				p.x = 600;
			}
		}
		return p;
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite composite = new Composite(parent, SWT.WRAP);
		GridLayout layout = new GridLayout(2 ,false);
		layout.marginTop = 10;
		layout.marginLeft = 12;
		composite.setLayout(layout);		
		Label imageLabel = new Label(composite, SWT.NONE);
		Image image = Display.getDefault().getSystemImage(SWT.ICON_INFORMATION);
		imageLabel.setImage(image);
		GridData gd = new GridData();
		gd.verticalSpan = 2;
		imageLabel.setLayoutData(gd);
		
		Label label = new Label(composite, SWT.NONE);
		label.setText(fLabel);
		label.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_CENTER));
		Label pathLabel = new Label(composite, SWT.NONE);
		pathLabel.setText(TagStringUtilities.wrapPath(fPath, 60));
		pathLabel.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_CENTER));
		
		

		if (hasErrors()) {
			Composite errorComposite = CompositeBuilder.instance(composite).horizontal().horizontalSpan(2).build();
			Label errorLabel = new Label(errorComposite, SWT.NONE);
			errorLabel.setText("Errors: ");
			
			StyledText text = new StyledText(errorComposite, SWT.BORDER |
					SWT.V_SCROLL | SWT.WRAP | SWT.READ_ONLY);
			StringBuilder builder = new StringBuilder();
			for (IExportError error: fErrors) {
				builder.append(error.getMessage());
				builder.append(" - ");
				builder.append(error.getExseptionMessage());
				builder.append("\n");
			}
			for (IExportError error: fErrors) {
				builder.append(error.getMessage());
				builder.append(" - ");
				builder.append(error.getExseptionMessage());
				builder.append("\n");
			}
			for (IExportError error: fErrors) {
				builder.append(error.getMessage());
				builder.append(" - ");
				builder.append(error.getExseptionMessage());
				builder.append("\n");
			}
			text.setText(builder.toString());
			GridData gd1 = new GridData(GridData.FILL,GridData.FILL, true, true);
			gd1.widthHint = 770;						
			gd1.heightHint = 300;
			//gd1.grabExcessHorizontalSpace = true;
			//gd2.g
			text.setLayoutData(gd1);
		}
		
		return super.createDialogArea(parent);
	}
	
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, SHOW_IN, Messages.ExportFinishDialog_Open_folder_button, true);
		createButton(parent, IDialogConstants.OPEN_ID, Messages.ExportFinishDialog_Open_button, true);
		//createButton(parent, SHOW_IN, "Показать в папке", true);
		createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL,
				true);	
	}
	
	@Override
	protected void buttonPressed(int buttonId) {
		if (IDialogConstants.OPEN_ID == buttonId){
			okPressed();
			Desktop desktop = null;
			if (Desktop.isDesktopSupported()) {
			    desktop = Desktop.getDesktop();
			}
			try {
			    desktop.open(new File(fPath));
			} catch (IOException ioe) {
			    ioe.printStackTrace();
			}
			
		} else if (SHOW_IN == buttonId) {
			okPressed();
			Desktop desktop = null;
			if (Desktop.isDesktopSupported()) {
			    desktop = Desktop.getDesktop();
			}
			try {
			    desktop.open((new File(fPath)).getParentFile());			    
			} catch (IOException ioe) {
			    ioe.printStackTrace();
			}
		}	
		super.buttonPressed(buttonId);
	}
	
	private boolean hasErrors() {
		return fErrors != null && !fErrors.isEmpty();
	}
}
