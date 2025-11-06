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
package ru.dip.editors.formeditor.fieldpage;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;

import ru.dip.core.form.control.IFieldButtonContainer;
import ru.dip.core.form.model.Field;
import ru.dip.core.form.model.ItemsField;
import ru.dip.core.utilities.ui.swt.ColorProvider;

public class ButtonFieldControl extends FieldControl {

	// model
	private final ItemsField fField;
	private IFieldButtonContainer fButtonContainer;	
	// control	
	private final Composite fParentComposite;
	private final Font fFont;
	private Group fGroup;
	
	public ButtonFieldControl(Composite parent, ItemsField checkField) {
		fParentComposite = parent;
		fField = checkField;
		fFont = getFont();
		createContent();
	}

	//================================
	// content
	
	private void createContent(){
		createGroup();
		createButtons();
	}
	
	private void createGroup(){
		fGroup = new Group(fParentComposite, SWT.NONE);
		fGroup.setLayout(new GridLayout());
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = FieldsPage.COLUMNS;
		fGroup.setLayoutData(gd);
		fGroup.setText(fField.getTitle());
		setGroupToolTip();
		fGroup.setForeground(ColorProvider.SELECT);
		fGroup.setBackground(fParentComposite.getBackground());
	}
	
	private void setGroupToolTip(){
		String hint = fField.getHint();
		if (hint != null){
			fGroup.setToolTipText(hint);
		}
	}
	
	private void createButtons() {
		fButtonContainer = IFieldButtonContainer.create(fGroup, fField, fFont); 
		fButtonContainer.createCheckButtons();
		fButtonContainer.addSelectionListener(e -> this.updateTagContent());	
	}

	//================================
	// get & set value
	
	@Override
	public boolean updateTagValue() {
		return fButtonContainer.updateFieldValue();
	}
	
	private void updateTagContent(){
		String tagValue = fButtonContainer.computeFieldValue();		
		fField.setValue(tagValue);
	}

	@Override
	public void setValue() {
		fButtonContainer.setValue();
	}

	@Override
	public void dispose() {
		fFont.dispose();
	}

	@Override
	public Field getField() {
		return fField;
	}

	@Override
	public void selectText(String text) {
		// NOP
	}

	@Override
	public boolean isFocus() {
		return fButtonContainer.isFocus();
	}

}
