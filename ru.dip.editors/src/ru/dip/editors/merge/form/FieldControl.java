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

import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;

import ru.dip.core.form.model.CheckField;
import ru.dip.core.form.model.ComboField;
import ru.dip.core.form.model.Field;
import ru.dip.core.form.model.RadioField;
import ru.dip.core.form.model.TextField;
import ru.dip.core.utilities.ui.swt.ColorProvider;

public abstract class FieldControl {
	
	private FieldStatus fStatus;
	
	public static FieldControl createFiledControl(Composite parent, Field field, boolean readOnly){
		if (field instanceof ComboField){	
			return new ComboFieldControl(parent, (ComboField) field, readOnly);		
		} else if (field instanceof CheckField){
			return new ButtonFieldControl(parent, (CheckField) field, readOnly);
		} else if (field instanceof RadioField){
			return new ButtonFieldControl(parent, (RadioField) field, readOnly);
		} else {
			return new TextFieldControl(parent, (TextField) field, readOnly);
		}
	}
	
	public  static  Font getFont(){
		String FONT_ID = "ru.dip.fontDefinition";
		FontData[] fd = PlatformUI.getWorkbench().getThemeManager()
		.getCurrentTheme().getFontRegistry().getFontData(FONT_ID);
		Font font = new Font(Display.getCurrent(), fd);		
		return font;
	}
	
	public boolean updateTagValue() {
		return false;
	}
		
	public abstract void setValue();
		
	public abstract Field getField();

	public abstract void selectText(String text);
	
	public abstract boolean isFocus();
	
	public void setNullSelection(){}

	public abstract void dispose();

	//=================================
	// listeners
	
	public void addFocusListener(){};
	
	public void addFocusListener(FocusListener focusListener) {}
	
	public abstract void addModifyListener(ModifyListener modifyListener);
	
	//==================================
	// diff
	
	public  void setStatus(FieldStatus status) {
		fStatus =status;
		updateStatus();
	}
	
	abstract void updateStatus();
	
	protected Color getDiffColor() {
		switch (fStatus) {
		case ADDED:{
			return ColorProvider.GREEN;
		}
		case EDIT:{
			return ColorProvider.YELLOW;
		}
		case EQUALS:{
			return ColorProvider.WHITE;
		}
		case REMOVE:{
			return ColorProvider.RED;
		}
		}
		return ColorProvider.WHITE;
	}

}

