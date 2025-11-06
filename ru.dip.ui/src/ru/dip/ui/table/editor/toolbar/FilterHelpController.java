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
package ru.dip.ui.table.editor.toolbar;

import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import ru.dip.ui.table.ktable.dialog.HelpFilterDialog;

public class FilterHelpController {

	private HelpFilterDialog fFilterHelpShell;
	private boolean fIsShowedHelpFilter = false;
	
	public void forceOpenFilterHelp(Text filter, Shell shell) {		
		if (isFilterHelpVisible()) {
			return;
		}
				
		Point location = filter.toDisplay(filter.getLocation());
		location.y += filter.getBounds().height + 3;
		
		fFilterHelpShell = new HelpFilterDialog(shell);
		fFilterHelpShell.setLocation(location);
		fFilterHelpShell.createShell();
		filter.addModifyListener(new ModifyListener() {
			
			@Override
			public void modifyText(ModifyEvent e) {
				closeIfOpen();
				filter.removeModifyListener(this);
			}
		});
		filter.addFocusListener(new FocusListener() {
			
			@Override
			public void focusLost(FocusEvent e) {
				closeIfOpen();
				filter.removeFocusListener(this);
			}
			
			@Override
			public void focusGained(FocusEvent e) {}			
		});
		filter.setFocus();	
	}
	
	
	public void openFilterHelp(Text filter, Shell shell) {
		if (fIsShowedHelpFilter) {
			return;
		}
		fIsShowedHelpFilter = true;
		forceOpenFilterHelp(filter, shell);
	}
	
	
	public boolean isFilterHelpVisible() {
		return fFilterHelpShell != null && fFilterHelpShell.isOpen();
	}
	
	public void closeIfOpen() {
		if (isFilterHelpVisible()) {
			fFilterHelpShell.close();
			fFilterHelpShell = null;
		}
	}
}
