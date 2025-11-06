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
package ru.dip.ui.table.ktable.celleditors;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import ru.dip.core.form.FormReader;
import ru.dip.core.form.control.IFieldButtonContainer;
import ru.dip.core.form.model.ItemsField;
import ru.dip.core.unit.form.FormField;
import ru.dip.core.utilities.ui.CompositeBuilder;
import ru.dip.core.utilities.ui.swt.ColorProvider;

public final class FieldButtonCellEditor extends AbstractFieldCellEditor {

	private final ItemsField fField;
	private IFieldButtonContainer fButtonContainer;
	
	public FieldButtonCellEditor(ItemsField checkField, FormField formField) {
		super(formField);
		fField = checkField;		
	}
	
	@Override
	protected Control createControl() {
		Composite composite = CompositeBuilder.instance(m_Table).border().notIndetns().full()
			.background(ColorProvider.WHITE).build();
		fButtonContainer = IFieldButtonContainer.create(composite, fField, getFont());
		fButtonContainer.createCheckButtons();
		return composite;
	}
		
	@Override
	protected  void setValue() {
		fButtonContainer.setValue();
	}

	@Override
	void setFieldValue(FormReader reader) {
		reader.getFieldByName(fField.getName()).setValue(fField.getValue());	
		fButtonContainer.updateFieldValue();
	}
	
}