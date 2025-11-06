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
package ru.dip.core.form.control;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;

import ru.dip.core.form.model.ComboField;
import ru.dip.core.utilities.ui.CompositeFactory;

public class ComboContainer implements IFieldContainer {

	// controls
	private Combo fCombo;
	private Composite fComposite;
	// model
	private final ComboField fComboField;
	private String[] fItems;

	public ComboContainer(Composite parent, ComboField comboField) {
		fComboField = comboField;
		fItems = fComboField.getItemValues();
		createControl(parent);
	}

	private Control createControl(Composite parent) {
		fComposite = CompositeFactory.full(parent);
		fCombo = new Combo(fComposite, SWT.READ_ONLY | SWT.BORDER);
		fCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		fCombo.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_LIST_BACKGROUND));
		if (fItems != null) {
			fCombo.setItems(fItems);
		}
		return fCombo;
	}

	@Override
	public void setValue() {
		String oldValue = fComboField.getValue();
		if (oldValue == null) {
			oldValue = "";
		}
		fCombo.setText(oldValue);
	}

	@Override
	public boolean updateFieldValue() {
		String newValue = fCombo.getText();
		if (!newValue.equals(fComboField.getValue())) {
			fComboField.setValue(newValue);
			return true;
		}
		return false;
	}

	@Override
	public Control getControl() {
		return fComposite;
	}

	@Override
	public String getFieldName() {
		return fComboField.getName();
	}

	@Override
	public String getValue() {
		return fComboField.getValue();
	}

}
