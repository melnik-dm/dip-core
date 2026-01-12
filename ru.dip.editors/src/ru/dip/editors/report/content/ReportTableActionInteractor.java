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
package ru.dip.editors.report.content;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.swt.widgets.Menu;

import ru.dip.core.model.interfaces.IDipUnit;
import ru.dip.ui.Messages;
import ru.dip.ui.table.ktable.actions.manager.CopyIdIneractor;
import ru.dip.ui.table.ktable.actions.manager.CopyIdIneractor.CopyIdType;

public class ReportTableActionInteractor {
	
	private ReportContentComposite fReportContentComposite;
	private CopyIdIneractor fCopyIdInteractor;
	
	// actions 
	private OpenAction fOpenAction = new OpenAction();
	private CopyRelativeAction fCopyRelativeAction = new CopyRelativeAction();
	private CopyIdAction fCopyIdAction = new CopyIdAction();
	private CopyVersionId fCopyVersionId = new CopyVersionId();
	
	public ReportTableActionInteractor(ReportContentComposite table) {
		fReportContentComposite = table;
		fCopyIdInteractor = new CopyIdIneractor(fReportContentComposite);
	}

	public void addContextMenu() {
		MenuManager popupMenuManager = new MenuManager();
		popupMenuManager.setRemoveAllWhenShown(true);
		IMenuListener listener = new IMenuListener() {

			@Override
			public void menuAboutToShow(IMenuManager mng) {	
				createMenu(mng);
				IDipUnit unit = fReportContentComposite.getSelectedDipUnit();
				
				if (unit != null) {
					fOpenAction.setEnabled(true);
					fCopyRelativeAction.setEnabled(true);
					fCopyIdAction.setEnabled(true);
					fCopyVersionId.setEnabled(true);
				} else {
					fOpenAction.setEnabled(false);
					fCopyRelativeAction.setEnabled(false);
					fCopyIdAction.setEnabled(false);
					fCopyVersionId.setEnabled(false);
				}
			}		
	
			private IMenuManager createMenu(IMenuManager mng){				
				mng.add(fOpenAction);
				mng.add(fCopyRelativeAction);
				mng.add(fCopyIdAction);			
				mng.add(fCopyVersionId);	
				return mng;
			}
		};

		popupMenuManager.addMenuListener(listener);
		Menu menu = popupMenuManager.createContextMenu(fReportContentComposite.table());
		fReportContentComposite.table().setMenu(menu);
	}
				
	private class OpenAction extends Action {

		OpenAction() {
			setText(Messages.OpenAction_Name);
		}
				
		@Override
		public void run() {
			fReportContentComposite.openFile();
		}		
	}
	
	private class CopyRelativeAction extends Action {

		CopyRelativeAction() {
			setText(Messages.CopyIdHandler_RelativeCopyActionName);
		}
				
		@Override
		public void run() {
			IDipUnit unit = fReportContentComposite.getSelectedDipUnit();
			fCopyIdInteractor.doCopyID(CopyIdType.RELATIVE, unit);
		}		
	}
	
	private class CopyIdAction extends Action {

		CopyIdAction() {
			setText(Messages.CopyIdHandler_CopyIdActionName);
		}
				
		@Override
		public void run() {
			IDipUnit unit = fReportContentComposite.getSelectedDipUnit();
			fCopyIdInteractor.doCopyID(CopyIdType.FULL, unit);		
		}		
	}

	private class CopyVersionId extends Action {
		
		CopyVersionId() {
			setText(Messages.CopyIdHandler_CopyIdWithRevisionActionName);
		}
				
		@Override
		public void run() {
			IDipUnit unit = fReportContentComposite.getSelectedDipUnit();
			fCopyIdInteractor.doCopyID(CopyIdType.REVISION, unit);	
		}	
	}
	
}
