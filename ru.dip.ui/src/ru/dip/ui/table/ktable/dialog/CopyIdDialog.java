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

public class CopyIdDialog extends PopupDialog { 
	
	private static final int shellStyle =  SWT.NO_FOCUS | SWT.ON_TOP;			
	private static final boolean takeFocusOnOpen = false;
	private static final boolean persistSize = false; 
	private static final boolean persistLocation = false;
	private static final boolean showDialogMenu = false;
	private static final boolean showPersistActions = false;
	
	private final Shell fParentShell;
	private Shell fDialogShell;
	
	private final String fText;
	
	public CopyIdDialog(Shell parent, String actionName, String text) {	
		super(parent, 
				shellStyle, 
				takeFocusOnOpen, 
				persistSize, 
				persistLocation, 
				showDialogMenu, 
				showPersistActions, 
				actionName,
				null);
		fParentShell = parent;
		fText = text;
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
		int x = rect.x + (rect.width / 2) - (getInitialSize().x / 2);
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
		label.setText(fText);		
		return super.createDialogArea(parent);
	}

}


