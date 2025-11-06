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
package ru.dip.ui.table.ktable.dialog;

import org.eclipse.jface.dialogs.PopupDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

public class HelpCtrlDialog extends PopupDialog { 
	
	private static final int shellStyle =  SWT.NO_FOCUS | SWT.ON_TOP;			
	private static final boolean takeFocusOnOpen = false;
	private static final boolean persistSize = false; 
	private static final boolean persistLocation = false;
	private static final boolean showDialogMenu = false;
	private static final boolean showPersistActions = false;
	private static final  String titleText = "Ctrl help";
	private static final  String infoText = "При нажатой клавише Ctrl";
	
	private Shell fParentShell;
	private Shell fDialogShell;
	
	public HelpCtrlDialog(Shell parent) {	
		super(parent, 
				shellStyle, 
				takeFocusOnOpen, 
				persistSize, 
				persistLocation, 
				showDialogMenu, 
				showPersistActions, 
				titleText,
				infoText);
		fParentShell = parent;
	}
	
	@Override
	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		fDialogShell = shell;
	}
	
	public boolean isVisible() {
		return !fDialogShell.isDisposed() && fDialogShell.isVisible();
	}
	
	public void setVisible(boolean visible) {
		fDialogShell.setVisible(visible);
	}
	
	@Override
	protected Point getInitialLocation(Point initialSize) {
		Rectangle rect = fParentShell.getBounds();
		int x = rect.x + rect.width - getInitialSize().x - 10;
		int y = rect.y + rect.height - getInitialSize().y - 10;

		return new Point(x, y);
	}
	
	@Override
	protected Color getBackground() {
		return new Color(Display.getDefault(), new RGB(238, 232, 170));
	}	

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout());
		composite.setLayoutData(new GridData());
		Label label = new Label(composite, SWT.NONE);
		label.setText("Ctrl - Доступно изменение ширины столбцов");
		
		Label label2 = new Label(composite, SWT.NONE);
		label2.setText("Ctrl + Scroll - Настройка размера шрифта \nдля всех столбцов таблицы");
		
		Label label3 = new Label(composite, SWT.NONE);
		label3.setText("Ctrl + Shift + Scroll - Настройка размера \nшрифта для столбца \"Содержимое\"");
		
		Label label4 = new Label(composite, SWT.NONE);
		label4.setText("Ctrl + двойной клик по файлу - Выделение всех файлов \nодного уровня вложенности");
		
		Label label5 = new Label(composite, SWT.NONE);
		label5.setText("Ctrl + двойной клик по директории - Выделение всех \nобъектов ниже/выше в иерархии");
		
		return super.createDialogArea(parent);
	}

}


