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
package ru.dip.ui.controller;

import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;

import ru.dip.core.exception.RenameDIPException;
import ru.dip.core.exception.SaveTableDIPException;
import ru.dip.core.model.interfaces.IDipElement;
import ru.dip.core.utilities.DipUtilities;
import ru.dip.core.utilities.WorkbenchUtitlities;
import ru.dip.ui.Messages;

public class RenameController {
	
	private IDipElement fDipElement;
	private String fNewName;
	private boolean fNeedReserve = false;
	private Shell fShell;
	
	public RenameController(IDipElement element, String newName) {
		this(element, newName, false, null);
	}
	
	public RenameController(IDipElement element, String newName, Shell shell) {
		this(element, newName, false, shell);
	}
	
	public RenameController(IDipElement element, String newName, boolean reserve, Shell shell) {
		fDipElement = element;
		fNewName = newName;
		fNeedReserve = reserve;		
		fShell = shell;
	}
	
	public IStatus doRename() {
		WorkspaceJob job = new WorkspaceJob("rename") { //$NON-NLS-1$
			
			@Override
			public IStatus runInWorkspace(IProgressMonitor monitor) throws CoreException {
				rename();
				return Status.OK_STATUS;
			}
		};
		
		try {
			return job.runInWorkspace(null);
		} catch (CoreException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	private void rename() {
		if(fShell == null) {
			fShell = WorkbenchUtitlities.getShell();
		}
		
		try {
			DipUtilities.renameElement(fDipElement, fNewName, fNeedReserve, fShell);
		} catch (RenameDIPException e) {
			e.printStackTrace();
			MessageDialog.openError(fShell, Messages.RenameWizard_RenameErrorTitle, e.getMessage());
		} catch (SaveTableDIPException e) {
			e.printStackTrace();
			MessageDialog.openError(fShell, Messages.RenameWizard_SaveTableError, e.getMessage());
		}
	}

	

}
