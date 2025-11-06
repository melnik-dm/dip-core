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
package ru.dip.ui.navigator;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.ui.navigator.CommonDropAdapter;
import org.eclipse.ui.navigator.CommonDropAdapterAssistant;

import ru.dip.ui.wizard.importproject.ImportDipProjectWizard;

public class NavigatorDropAdapterAssistant extends CommonDropAdapterAssistant {

	@Override
	public boolean isSupportedType(TransferData aTransferType) {
		return FileTransfer.getInstance().isSupportedType(aTransferType);
	}
	
	@Override
	public IStatus validatePluginTransferDrop(IStructuredSelection aDragSelection, Object aDropTarget) {
		return super.validatePluginTransferDrop(aDragSelection, aDropTarget);
	}
	

	@Override
	public IStatus validateDrop(Object target, int operation, TransferData transferType) {
		return Status.OK_STATUS;
	}
	
	@Override
	public IStatus handleDrop(CommonDropAdapter aDropAdapter, DropTargetEvent aDropTargetEvent, Object aTarget) {		
		if (!(aDropTargetEvent.data instanceof String[])) {
			return null;
		}
		
		String[] names = (String[]) aDropTargetEvent.data;
		if (names != null && names.length > 0) {
			ImportDipProjectWizard wizard = new ImportDipProjectWizard(names[0]);
			WizardDialog wd = new WizardDialog(getShell(), wizard);			
			wd.setTitle("Import Project");
			wd.setTitle(wizard.getWindowTitle());
			if (wd.open() == WizardDialog.OK) {
				return Status.OK_STATUS;
			} 		
		}			
		return null;		
	}

}
