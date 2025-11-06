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
package ru.dip.ui.action.hide;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;

import ru.dip.core.utilities.WorkbenchUtitlities;
import ru.dip.ui.Messages;
import ru.dip.ui.ReqUIPlugin;
import ru.dip.ui.utilities.image.ImageProvider;

public class ShowReservedAction extends Action {
	
	public ShowReservedAction() {
		setText(Messages.ShowReservedAction_Action_name);
		setImageDescriptor(ImageDescriptor.createFromImage(ImageProvider.SHOW_RESERVED_OBJS));
		setChecked(false);		
	}
	
	@Override
	public void run(){
		boolean check = isChecked();	
		setChecked(check);
		ReqUIPlugin.getDefault().setShowReservedObjects(check);	
		WorkbenchUtitlities.updateProjectExplorer();
	}

}
