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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import ru.dip.core.form.FormReader;
import ru.dip.core.form.control.IFieldContainer;
import ru.dip.core.form.model.ItemsField;
import ru.dip.core.unit.form.FieldUnity;
import ru.dip.core.unit.form.FormField;
import ru.dip.core.utilities.ui.CompositeFactory;

public final class FieldUnityCellEditor extends AbstractFieldCellEditor  {
	
	private final List<FormField> fFormFields;
	private final List<IFieldContainer> fFieldContainers = new ArrayList<>();
	
	public FieldUnityCellEditor(FieldUnity unity) {
		super(unity);
		fFormFields = unity.getFormFields();
	}
	
	@Override
	protected Control createControl() {
		Composite composite = CompositeFactory.fullBorder(m_Table);		
		for (FormField formField: fFormFields) {
			fFieldContainers.add(IFieldContainer.create(composite, (ItemsField) formField.getField()));						
		}
		return composite;
	}
	
	@Override
	public void setValue() {
		fFieldContainers.forEach(IFieldContainer::setValue);
	}
				
	@Override
	void setFieldValue(FormReader reader) {
		for (IFieldContainer container: fFieldContainers) {
			reader.getFieldByName(container.getFieldName()).setValue(container.getValue());	
		}
		fFieldContainers.forEach(IFieldContainer::updateFieldValue);
	}

}
