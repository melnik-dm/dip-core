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
package ru.dip.ui.table.dialog.form;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import ru.dip.core.utilities.ui.swt.ColorProvider;

public class FieldPropertiesControl {

	private DialogFieldProperties fFieldProperties;
	private Button fEnableButton;
	private Button fShowTitle;

	public FieldPropertiesControl(Composite parent, DialogFieldProperties settings) {
		fFieldProperties = settings;
		Composite composite = new Composite(parent, SWT.BORDER);
		composite.setLayout(new GridLayout());
		composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		Label label = new Label(composite, SWT.NONE);
		label.setText(settings.title());
		label.setForeground(ColorProvider.BLACK);
		
		composite = new Composite(parent, SWT.BORDER);
		composite.setLayout(new GridLayout(5, true));
		composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		new Label(composite, SWT.NONE);
		new Label(composite, SWT.NONE);
		fEnableButton = new Button(composite, SWT.CHECK);
		fEnableButton.setAlignment(SWT.CENTER);
		new Label(composite, SWT.NONE);
		new Label(composite, SWT.NONE);
		fEnableButton.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				boolean selection = fEnableButton.getSelection();
				fFieldProperties.setEnable(selection);
				fShowTitle.setEnabled(selection);
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {

			}
		});
		fEnableButton.setSelection(settings.isEnable());

		composite = new Composite(parent, SWT.BORDER);
		composite.setLayout(new GridLayout(7, true));
		composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		new Label(composite, SWT.NONE);
		new Label(composite, SWT.NONE);
		new Label(composite, SWT.NONE);
		fShowTitle = new Button(composite, SWT.CHECK);
		new Label(composite, SWT.NONE);
		new Label(composite, SWT.NONE);
		new Label(composite, SWT.NONE);
		fShowTitle.setForeground(ColorProvider.BLACK);
		fShowTitle.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
		fShowTitle.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				boolean selection = fShowTitle.getSelection();
				fFieldProperties.setShowTitle(selection);
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {

			}
		});
		fShowTitle.setSelection(fFieldProperties.isShowTitle());
		if (!settings.isEnable()) {
			fShowTitle.setEnabled(false);
		}
	}
}
