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

import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import ru.dip.core.model.DipSchemaElement;
import ru.dip.core.utilities.TagStringUtilities;
import ru.dip.core.utilities.ui.CompositeBuilder;
import ru.dip.core.utilities.ui.ControlFactory;
import ru.dip.core.utilities.ui.GridDataFactory;
import ru.dip.ui.Messages;

public class RenameSchemaDialog extends Dialog {

	private static final String MESSAGE = Messages.RenameSchemaDialog_RenameMessge;

	private Text fText;
	private String fExtension;
	private final String fOldExtension;
	private final List<String> fExistExtensions; // расширения для которых уже есть схема

	public RenameSchemaDialog(Shell parentShell, DipSchemaElement schema) {
		super(parentShell);
		fOldExtension = schema.getFormExtension();
		fExistExtensions = schema.parent().getAllExtensions();
	}
	
	@Override
	protected Point getInitialSize() {		
		Point p =  super.getInitialSize();
		if (p.x < 500) {
			p.x = 500;
		} 			
		if (p.y < 200) {
			p.y = 200;
		}
		return p;
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Rename Schema"); //$NON-NLS-1$
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite composite = CompositeBuilder.instance(parent).columns(2, false).full().build();
		Label label = new Label(composite, SWT.WRAP);
		label.setText("\n" + MESSAGE); //$NON-NLS-1$
		GridDataFactory.applyHorizontalSpan(label, 2);
		fText = new Text(composite, SWT.BORDER);
		fText.setText(fOldExtension);
		fText.setSelection(fOldExtension.length());
		GridDataFactory.applyFillHorizontal(fText);
		fText.addModifyListener(new ModifyListener() {

			@Override
			public void modifyText(ModifyEvent e) {
				getButton(OK).setEnabled(isValid(fText.getText().trim()));
			}
		});
		
		ControlFactory.label(composite, ".xml"); //$NON-NLS-1$
		
		return composite;
	}

	@Override
	protected Control createButtonBar(Composite parent) {
		Control control = super.createButtonBar(parent);
		getButton(OK).setEnabled(false);
		return control;
	}

	private boolean isValid(String extension) {
		if (fExistExtensions.contains(extension)) {
			return false;
		}
		return TagStringUtilities.isExtension(extension);
	}

	@Override
	protected void okPressed() {
		fExtension = fText.getText().trim();
		super.okPressed();
	}

	public String getExtension() {
		return fExtension;
	}

}
