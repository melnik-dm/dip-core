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

import org.eclipse.swt.widgets.Display;

import ru.dip.core.model.interfaces.IDipDocumentElement;
import ru.dip.core.utilities.DipUtilities;
import ru.dip.ui.table.ktable.KDipTableSelector;
import ru.dip.ui.table.ktable.KTableComposite;
import ru.dip.ui.table.ktable.dialog.CopyIdDialog;

public class CopyIdIneractor {

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
	
	private final KTableComposite fTableComposite;
	private CopyIdDialog fCopyIdialog;

	private static final String COPY_REVISION_DIALOG_TITLE = " Copy ID (revision)";
	private static final String COPY_ID_DIALOG_TITLE = " Copy ID";
	private static final String COPY_RELATIVE_DIALOG_TITLE = " Copy relative path";
	
	public CopyIdIneractor(KTableComposite tableComposite) {
		fTableComposite = tableComposite;
	}
	
	public void doCopyID(int numberClickC) {
		if (numberClickC == 0) {
			doCopyRelID();
		} else if (numberClickC == 1) {
			doCopyFullID();
		} else {
			doCopyFullRevisionID();
		}
	}
	
	public void doCopyID(CopyIdType idType) {
		switch (idType) {
		case RELATIVE: {
			doCopyRelID();
			break;
		}
		case FULL: {
			doCopyFullID();
			break;
		}
		case REVISION: {
			doCopyFullRevisionID();
			break;
		}
		}
	}
	
	private void doCopyFullRevisionID() {
		IDipDocumentElement selectedElement = selector().getSelectedOneDipDocElement();
		String id = DipUtilities.copyFullIdRevisionClipboard(selectedElement, display());
		showMessage(COPY_REVISION_DIALOG_TITLE, id);	
	}
	
	private void doCopyFullID() {
		IDipDocumentElement selectedElement = selector().getSelectedOneDipDocElement();
		String id = DipUtilities.copyFullIdClipboard(selectedElement, display());
		showMessage(COPY_ID_DIALOG_TITLE, id);
	}
	

	private void doCopyRelID() {
		IDipDocumentElement selectedElement = selector().getSelectedOneDipDocElement();
		String id = DipUtilities.copyRelativeIdClipboard(selectedElement, display());
		showMessage(COPY_RELATIVE_DIALOG_TITLE, id);
	}
	
	private void showMessage(String actionName, String id) {			
		if (fCopyIdialog != null && fCopyIdialog.isVisible()) {
			fCopyIdialog.close();
		}
		
		CopyIdDialog dialog  = new CopyIdDialog(fTableComposite.getShell(), actionName , id);
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
	
	private KDipTableSelector selector() {
		return fTableComposite.selector();
	}

	private Display display() {
		return fTableComposite.getDisplay();
	}

}
