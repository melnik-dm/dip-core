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

import org.eclipse.swt.events.FocusListener;
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
import ru.dip.editors.formeditor.FormsEditor;

public abstract class FieldControl {
	
	public static FieldControl createFiledControl(FormsEditor editor, Composite parent, Field field){
		if (field instanceof ComboField){	
			return new ComboFieldControl(parent, (ComboField) field);		
		} else if (field instanceof CheckField){
			return new ButtonFieldControl(parent, (CheckField) field);
		} else if (field instanceof RadioField){
			return new ButtonFieldControl(parent, (RadioField) field);
		} else {
			return new TextFieldControl(editor, parent, (TextField) field);
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
	
	public void setNullSelection(){		
	}
	
	public void addFocusListener(){};
	
	public void addFocusListener(FocusListener focusListener) {}
	
	public abstract void dispose();
	
}

