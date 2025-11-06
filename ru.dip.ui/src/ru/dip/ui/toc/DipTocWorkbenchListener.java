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
package ru.dip.ui.toc;

import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartReference;

import ru.dip.ui.table.editor.DipTableEditor;

public class DipTocWorkbenchListener implements IPartListener2 {

	@Override
	public void partActivated(IWorkbenchPartReference partRef) {
	}
	
	@Override
	public void partVisible(IWorkbenchPartReference partRef) {
		IWorkbenchPart part = partRef.getPart(false);
		if (part instanceof DipTocView){
			DipTocView tdView = (DipTocView) part;
			tdView.update();
		} else if (part instanceof DipTableEditor){
			DipTableEditor tableEditor = (DipTableEditor) part;
			DipTocView dipTocView = getDipTocView(partRef);
			if (dipTocView != null) {
				dipTocView.update(tableEditor);
			}
		} else if (part instanceof IEditorPart){
			DipTocView dipTocView = getDipTocView(partRef);
			if (dipTocView != null) {
				dipTocView.update();
			}
		}
	}
	
	public DipTocView getDipTocView(IWorkbenchPartReference partRef){
		return (DipTocView) partRef
        .getPage().findView(DipTocView.ID);
	}

	@Override
	public void partOpened(IWorkbenchPartReference partRef) {
		/*IWorkbenchPart part = partRef.getPart(false);
		if (part instanceof DipTocView){
			DipTocView tdView = (DipTocView) part;
			tdView.update();
		} else if (part instanceof DipTableEditor){
			DipTableEditor tableEditor = (DipTableEditor) part;

			DipTocView dipTocView = getDipTocView(partRef);
			if (dipTocView != null) {
				dipTocView.update(tableEditor);
			}
		} else if (part instanceof IEditorPart){
			DipTocView dipTocView = getDipTocView(partRef);
			if (dipTocView != null) {
				dipTocView.update();
			}
		}*/
	}

	@Override
	public void partBroughtToTop(IWorkbenchPartReference partRef) {
		
	}

	@Override
	public void partClosed(IWorkbenchPartReference partRef) {
		
	}

	@Override
	public void partDeactivated(IWorkbenchPartReference partRef) {
		
	}
	
	@Override
	public void partHidden(IWorkbenchPartReference partRef) {
		
	}

	@Override
	public void partInputChanged(IWorkbenchPartReference partRef) {
		
	}

}
