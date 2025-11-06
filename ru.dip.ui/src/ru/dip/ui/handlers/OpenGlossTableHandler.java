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
package ru.dip.ui.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;

import ru.dip.core.manager.DipNatureManager;
import ru.dip.core.model.DipProject;
import ru.dip.core.model.DipRoot;
import ru.dip.core.model.glossary.GlossaryFolder;
import ru.dip.core.utilities.WorkbenchUtitlities;
import ru.dip.ui.glossary.GlossaryDialog;
import ru.dip.ui.table.editor.DipTableEditor;

public class OpenGlossTableHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {	
		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);	
        if (event.getTrigger() instanceof Event){
        	Event ev = (Event) event.getTrigger();
        	if (ev.type == SWT.KeyDown){
        		openFromHotKey(window);
        		return null;
        	}     	
        }
		IStructuredSelection selection = (IStructuredSelection) window.getSelectionService().getSelection();
        Object firstElement = selection.getFirstElement();
        if (firstElement instanceof GlossaryFolder){
        	GlossaryFolder glossFolder = (GlossaryFolder) firstElement;
        	GlossaryDialog dialog = new GlossaryDialog(window.getShell(), glossFolder);
        	dialog.open();
        }       
		return null;
	}
	
	private void openFromHotKey(IWorkbenchWindow window) throws ExecutionException{
		IEditorPart part = window.getActivePage().getActiveEditor();
		DipProject project = null;
		if (part instanceof DipTableEditor){
			DipTableEditor editor = (DipTableEditor) part;
			project = editor.getDipProject();
		} else {
			IFile file =  WorkbenchUtitlities.getFileFromOpenedEditor(part);
			if (file == null){
				return;
			}
			if (!DipNatureManager.hasNature(file.getProject())){
				return;
			}
			project = DipRoot.getInstance().getDipProject(file.getProject());
		}
		if (project == null){
			return;
		}		
		GlossaryFolder glossFolder = project.getGlossaryFolder();
		if (glossFolder == null){
			return;
		}
    	GlossaryDialog dialog = new GlossaryDialog(window.getShell(), glossFolder);
    	dialog.open();
	}
	
}
