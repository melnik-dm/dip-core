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
package ru.dip.ui.table.ktable.actions.manager;


import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import ru.dip.core.model.interfaces.IDipDocumentElement;
import ru.dip.core.utilities.DipUtilities;
import ru.dip.ui.table.ktable.dialog.CopyIdDialog;

public class CopyIdIneractor {
	
	private static final String COPY_REVISION_DIALOG_TITLE = " Copy ID (revision)";
	private static final String COPY_ID_DIALOG_TITLE = " Copy ID";
	private static final String COPY_RELATIVE_DIALOG_TITLE = " Copy relative path";

	public static enum CopyIdType {
		RELATIVE, FULL, REVISION;
		
		static CopyIdType from(int numberClickC) {
			if (numberClickC == 0) {
				return RELATIVE;
			} else if (numberClickC == 1) {
				return FULL;
			} else {
				return REVISION;
			}			
		}		
	}
		
	private CopyIdDialog fCopyIdialog;
	private final Control fControl;
	
	public CopyIdIneractor(Control tableComposite) {
		fControl = tableComposite;
	}
	
	public void doCopyID(int numberClickC, IDipDocumentElement dde) {
		if (numberClickC == 0) {
			doCopyRelID(dde);
		} else if (numberClickC == 1) {
			doCopyFullID(dde);
		} else {
			doCopyFullRevisionID(dde);
		}
	}
	
	public void doCopyID(CopyIdType idType, IDipDocumentElement dde) {
		switch (idType) {
		case RELATIVE: {
			doCopyRelID(dde);
			break;
		}
		case FULL: {
			doCopyFullID(dde);
			break;
		}
		case REVISION: {
			doCopyFullRevisionID(dde);
			break;
		}
		}
	}
	
	private void doCopyFullRevisionID(IDipDocumentElement dde) {		
		String id = DipUtilities.copyFullIdRevisionClipboard(dde, display());
		showMessage(shell(), COPY_REVISION_DIALOG_TITLE, id);	
	}
	
	private void doCopyFullID(IDipDocumentElement dde) {
		String id = DipUtilities.copyFullIdClipboard(dde, display());
		showMessage(shell(), COPY_ID_DIALOG_TITLE, id);
	}
	

	private void doCopyRelID(IDipDocumentElement dde) {
		String id = DipUtilities.copyRelativeIdClipboard(dde, display());
		showMessage(shell(), COPY_RELATIVE_DIALOG_TITLE, id);
	}
	
	private void showMessage(Shell shell, String actionName, String id) {			
		if (fCopyIdialog != null && fCopyIdialog.isVisible()) {
			fCopyIdialog.close();
		}
		
		CopyIdDialog dialog  = new CopyIdDialog(shell, actionName , id);
		fCopyIdialog = dialog;
		fCopyIdialog.open();
		
		Thread thread = new Thread() {		
			public void run() {
				try {
					Thread.sleep(2 * 1000);
					Display.getDefault().asyncExec(new Runnable() {
						
						@Override
						public void run() {
							dialog.close();
						}
					});
				} catch (InterruptedException e) {
					e.printStackTrace();
				}				
			};			
		};				
		thread.start();
	}

	
	//===============================
	// getters
	
	private Display display() {
		return fControl.getDisplay();
	}
	
	private Shell shell() {
		return fControl.getShell();
	}

}
