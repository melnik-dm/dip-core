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
package ru.dip.editors;

import org.eclipse.core.resources.IFile;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartReference;

import ru.dip.core.model.interfaces.IDipDocumentElement;
import ru.dip.core.model.interfaces.IDipElement;
import ru.dip.core.utilities.DipUtilities;
import ru.dip.core.utilities.WorkbenchUtitlities;
import ru.dip.ui.table.editor.DipTableEditor;

public class ReqWorkbenchListener implements IPartListener2 {
		
	private IFile fDeactivatedFile; // последний активный файл
		
	@Override
	public void partActivated(IWorkbenchPartReference partRef) {
		if (!ReqEditorsPlugin.isDocumentSynhronize()) {
			return;
		}
			
        IWorkbenchPart part = partRef.getPart(false);
		if (part instanceof DipTableEditor) {
        	DipTableEditor editor = (DipTableEditor) part;        	
        	if (fDeactivatedFile != null) {
        		IDipElement element = DipUtilities.findElement(fDeactivatedFile);		        				        		
        		if (element instanceof IDipDocumentElement) {
        			editor.select((IDipDocumentElement) element);
        			fDeactivatedFile = null;
        		}	      		        			
        	}
        }
	}
	
	@Override
	public void partDeactivated(IWorkbenchPartReference partRef) {
		// запоминаем файл, который закрыли
		IWorkbenchPart part = partRef.getPart(false);
		if (part instanceof IEditorPart && !(part instanceof DipTableEditor)) {
			fDeactivatedFile = WorkbenchUtitlities.getFileFromOpenedEditor(part);
		}	
	}
}
