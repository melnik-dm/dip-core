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

import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Control;

import ru.dip.core.form.FormReader;
import ru.dip.core.form.control.ComboContainer;
import ru.dip.core.form.model.ComboField;
import ru.dip.core.unit.form.FormField;

public final class FieldComboCellEditor extends AbstractFieldCellEditor {

	private ComboField fComboField;
	private ComboContainer fComboContainer;

	public FieldComboCellEditor(ComboField comboField, FormField formField) {
		super(formField);
		fComboField = comboField;
	}
	
	protected Control createControl() {
		fComboContainer = new ComboContainer(m_Table, fComboField);
		return fComboContainer.getControl();
	}
	
	@Override
	protected  void setValue() {
		fComboContainer.setValue();
	}

	@Override
	void setFieldValue(FormReader reader) {
		reader.getFieldByName(fComboField.getName()).setValue(fComboField.getValue());	
		fComboContainer.updateFieldValue();
	}
	
	@Override
	public void setBounds(Rectangle rect) {
		super.setBounds(new Rectangle(rect.x, rect.y + 1, rect.width, rect.height - 2));
	}

}
