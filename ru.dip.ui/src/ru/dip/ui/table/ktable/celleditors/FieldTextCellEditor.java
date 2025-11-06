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

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import ru.dip.core.form.FormReader;
import ru.dip.core.form.model.Field;
import ru.dip.core.form.model.TextField;
import ru.dip.core.unit.form.FormField;
import ru.dip.core.utilities.ui.LayoutManager;
import ru.dip.ui.table.table.TableSettings;

public final class FieldTextCellEditor extends AbstractFieldCellEditor implements ITextActionSupport {
	
	private StyledText fText; 
	private final Field fTextField;
	
	public FieldTextCellEditor(TextField textField, FormField formField) {
		super(formField);
		fTextField = textField;		
	}
	
	@Override
	protected Control createControl() {
		Composite composite = new Composite(m_Table, SWT.BORDER);	
		composite.setLayout(LayoutManager.notIndtentLayout());		
		fText = new StyledText(composite, SWT.MULTI | SWT.V_SCROLL | SWT.WRAP);
		fText.setLayoutData(new GridData(GridData.FILL_BOTH));
		setPadding();
		return composite;
	}

	@Override
	protected void setValue() {
		String content = getEditorContent();
		if (content == null) {
			content = ""; 
		}
		Font font = getFont();
		if (font != null) {
			fText.setFont(font);
		}		
		fText.setText(content);
		fText.selectAll();
		fText.setVisible(true);
		fText.setFocus();
	}
	
	private void setPadding() {
		fText.setMargins(TableSettings.marginLeft(), 
				TableSettings.marginTop(),
				TableSettings.marginRight(),
				TableSettings.marginBottom());
	}

	protected Font getFont() {							
		return TableSettings.presentationFont();
	}
	
	protected String getEditorContent() {
		String oldValue = fTextField.getValue();
		if (oldValue == null) {
			oldValue = "";
		}
		return oldValue;
	}

	@Override
	void setFieldValue(FormReader reader) {
		reader.getFieldByName(fTextField.getName()).setValue(fText.getText());
		fTextField.setValue(fText.getText());
	}

	@Override
	public void doPaste() {
		fText.paste();
	}

	@Override
	public void doCopy() {
		fText.copy();
	}
	
}