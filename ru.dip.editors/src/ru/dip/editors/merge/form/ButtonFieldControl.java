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
package ru.dip.editors.merge.form;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;

import ru.dip.core.form.control.IFieldButtonContainer;
import ru.dip.core.form.model.Field;
import ru.dip.core.form.model.ItemsField;
import ru.dip.core.utilities.ui.WSelectionListener;
import ru.dip.core.utilities.ui.swt.ColorProvider;

public class ButtonFieldControl extends FieldControl {

	// model
	private ItemsField fField;
	private boolean fReadonly = false;
	// control
	private IFieldButtonContainer fButtonContainer;
	private Composite fParentComposite;
	private Composite fMainComposite;
	private Group fGroup;
	private Font fFont;
	
	public ButtonFieldControl(Composite parent, ItemsField field, boolean readOnly) {
		fParentComposite = parent;
		fField = field;
		fReadonly = readOnly;
		fFont = getFont();
		createContent();
	}
	
	//================================
	// content
	
	private void createContent(){
		createGroup();
		createCheckButtons();
	}
	
	private void createGroup(){
		fGroup = new Group(fParentComposite, SWT.NONE);
		int column = 2;
		if (fField.getLength() != null){
			column = fField.getLength();
			if (column > 3) {
				column /= 2;
			}
		}
		
		fGroup.setLayout(new GridLayout());
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		fGroup.setLayoutData(gd);
		fGroup.setText(fField.getTitle());
		
		setGroupToolTip();
		fGroup.setForeground(ColorProvider.SELECT);
		
		fMainComposite = new Composite(fGroup, SWT.NONE);
		fMainComposite.setLayout(new GridLayout(column, true));
		fMainComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
	}
	
	@Override
	void updateStatus() {
		fMainComposite.setBackground(getDiffColor());
	}
	
	private void setGroupToolTip(){
		String hint = fField.getHint();
		if (hint != null){
			fGroup.setToolTipText(hint);
		}
	}
	
	private void createCheckButtons() {
		fButtonContainer = IFieldButtonContainer.create(fGroup, fField, fFont);
		fButtonContainer.createCheckButtons();
		fButtonContainer.addSelectionListener(e -> updateTagContent());
		fButtonContainer.applyToButtons(b -> b.setEnabled(fReadonly));
	}

	@Override
	public void addModifyListener(ModifyListener modifyListener) {
		WSelectionListener listener = e -> modifyListener.modifyText(null);		
		fButtonContainer.addSelectionListener(listener);
	}
	
	//================================
	// get & set value
	
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
