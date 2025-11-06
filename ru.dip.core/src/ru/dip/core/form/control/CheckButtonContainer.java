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

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import ru.dip.core.form.model.ItemsField;
import ru.dip.core.utilities.ArrayUtils;
import ru.dip.core.utilities.ResourcesUtilities;
import ru.dip.core.utilities.ui.CompositeBuilder;
import ru.dip.core.utilities.ui.WSelectionListener;
import ru.dip.core.utilities.ui.swt.ColorProvider;

public class CheckButtonContainer implements IFieldButtonContainer {

	private final int DEFAULT_COLUMNS = 4;
	
	// controls
	private Composite fMainComposite;
	protected final List<Button> fButtons = new ArrayList<>();
	// model
	private final ItemsField fCheckField;
	private final Color fBackgroundColor;
	private final Color fForegroundColor;
	private final Font fFont;
	
	// Убирает фокус с кнопки (иначе появляется рамка вокруг кнопки только для Linux)
	private final FocusListener fFocusListener = new FocusAdapter() {

		@Override
		public void focusGained(FocusEvent e) {
			fMainComposite.forceFocus();		
		}
	};
	
	CheckButtonContainer(Composite parent, 
			ItemsField checkField, Font font) {
		fCheckField = checkField;
		fBackgroundColor = fCheckField.getBackgroundColor();
		fForegroundColor = fCheckField.getForegraundColor();
		fFont = font;
		createComposite(parent);
	}
	
	//================================
	// create controls
	
	private Composite createComposite(Composite parent) {
		int columns = DEFAULT_COLUMNS;
		if (fCheckField.getLength() != null){
			columns = fCheckField.getLength();
		}		
		fMainComposite = CompositeBuilder.instance(parent)
				.horizontal()
				.columns(columns, true)
				.build();
		fMainComposite.setBackground(parent.getBackground());
		return fMainComposite;
	}
		
	@Override
	public void createCheckButtons(){
		for (int i = 0; i < fCheckField.getItems().length; i++){
			Button button = createButton(i);
			fButtons.add(button);
		}
	}
	
	protected Button createButton(int number) {
		return createButton(number, SWT.CHECK);
	}
	
	protected Button createButton(int number, int style) {
		Button button = new Button(fMainComposite, style);
		button.setForeground(ColorProvider.BLACK);
		button.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		String buttonName = fCheckField.getItemValues()[number];
		if (buttonName == null){
			buttonName = "null";
		}		
		setButtonToolTip(button, number);
		setColor(button, number);
		
		if (!ResourcesUtilities.isWindows) {
			button.addFocusListener(fFocusListener);
		}
		
		button.setText(buttonName);
		button.setFont(fFont);
		return button;
	}
		
	private void setColor(Button button, int index){
		Color foreground = fCheckField.getItems()[index].getForegroundColor();
		if (foreground != null){
			button.setForeground(foreground);
		} else if (fForegroundColor != null){
			button.setForeground(fForegroundColor);
		}
		Color background = fCheckField.getItems()[index].getBackgroundColor();
		if (background != null){
			button.setBackground(background);
		} else if (fBackgroundColor != null){
			button.setBackground(fBackgroundColor);
		}
	}
	
	private void setButtonToolTip(Button button, int index){
		String hint = fCheckField.getItems()[index].getHint();
		if (hint != null){
			button.setToolTipText(hint);
		}
	}
	
	@Override
	public boolean isFocus() {
		for (Button button: fButtons){
			if (button.isFocusControl()){
				return true;
			}
		}
		return fMainComposite.isFocusControl();
	}
	
	//========================
	// optional
	
	@Override
	public void addSelectionListener(WSelectionListener listener) {
		fButtons.forEach(b -> b.addSelectionListener(listener));
	}
	
	@Override
	public void applyToButtons(Consumer<Button> consumer) {
		fButtons.forEach(consumer);
	}	
	
	//=========================
	// setValues
	
	@Override
	public void setValue() {
		String value = fCheckField.getValue();
		setCheckValue(value);
	}

	private void setCheckValue(String value){
		if (value == null){
			setEmptyValues();
			return;
		}
		String[] checks = value.split(",");
		for (Button button: fButtons){
			String buttonName = button.getText();
			boolean selection  = ArrayUtils.arrayContainsElement(checks, buttonName);
			button.setSelection(selection);			
		}
	}
		
	private void setEmptyValues(){
		for (Button button: fButtons){
			button.setSelection(false);
		}
	}
	
	//=========================
	// getValue (update field)
	
	@Override
	public String getFieldName() {
		return fCheckField.getName();
	}
	
	@Override
	public String getValue() {
		return fCheckField.getValue();
	}
	
	@Override
	public boolean updateFieldValue() {
		String tagValue = computeFieldValue();
		if (!tagValue.equals(fCheckField.getValue())){
			fCheckField.setValue(tagValue);
			return true;
		}
		return false;
		
	}
	
	@Override
	public String computeFieldValue(){
		StringBuilder builder = new StringBuilder();	
		for (Button button:fButtons){
			if (button.getSelection()){
				builder.append(button.getText());
				builder.append(",");
			}
		}
		if (builder.length() > 0){
			builder.deleteCharAt(builder.length() - 1);
		}
		return builder.toString();		
	}	
	
	@Override
	public Control getControl() {
		return fMainComposite;
	}
}
