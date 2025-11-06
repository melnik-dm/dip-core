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
package ru.dip.editors.incorrectview;

import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.PlatformUI;

import ru.dip.core.link.LinkInteractor;

public class IncorrectViewPartListener implements IPartListener2 {

	@Override
	public void partActivated(IWorkbenchPartReference partRef) {

	}
	
	@Override
	public void partVisible(IWorkbenchPartReference partRef) {	
        IWorkbenchPart part = partRef.getPart(false);
        if (part instanceof IEditorPart) {    	
    		IncorrectLinksView incorrectView = getLinksView();
    		if (incorrectView != null  && incorrectView.isVisible()) {
    			incorrectView.checkLinks();
    		}
    		if (incorrectView != null) {
    			incorrectView.addListenerToEditor((IEditorPart) part);
    		}
        } else if (part instanceof IncorrectLinksView) {
        	IncorrectLinksView incorrectView = (IncorrectLinksView) part;
        	incorrectView.checkLinks();
        	incorrectView.addListenerToEditor();
        }
	}
	
	public IncorrectLinksView getLinksView(){
		return (IncorrectLinksView) PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().findView(LinkInteractor.LINS_VIEW_ID);
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
	public void partOpened(IWorkbenchPartReference partRef) {

	}

	@Override
	public void partHidden(IWorkbenchPartReference partRef) {
	}

	@Override
	public void partInputChanged(IWorkbenchPartReference partRef) {
		
	}

}
