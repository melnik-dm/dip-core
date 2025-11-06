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
package ru.dip.editors.editview;

import java.util.ArrayList;

import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.PlatformUI;

import ru.dip.ui.table.editor.DipTableEditor;

public class EditViewPartListener implements IPartListener2 {

	@Override
	public void partActivated(IWorkbenchPartReference partRef) {
		IWorkbenchPart part = partRef.getPart(false);
		if (part instanceof EditViewPart){
			EditViewPart editView = (EditViewPart) part;
			for (DipTableEditor tableEditor : getTableEditors(partRef)){
				tableEditor.addPropertyListener(editView);			
			}
		} else if (part instanceof DipTableEditor){
			DipTableEditor dipTableEditor = (DipTableEditor) part;
			EditViewPart editView = getEditView(partRef);
			if (editView != null) {
				dipTableEditor.addPropertyListener(editView);
			}
		} else {
			EditViewPart editView = getEditView(partRef);
			if (editView != null) {
				editView.checkSave();
				editView.checkVisible();
			}
		}
	}
	
	@Override
	public void partVisible(IWorkbenchPartReference partRef) {
        IWorkbenchPart part = partRef.getPart(false);
        if (part instanceof DipTableEditor) {
        	DipTableEditor tableEditor = (DipTableEditor) part;
        	tableEditor.visible();
        } 
        if (part instanceof EditViewPart) {
        	IEditorPart editPart = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
        	if (editPart instanceof DipTableEditor) {
        		((DipTableEditor) editPart).updateSelection();
        	}
        }
	}

	
	
	public ArrayList<DipTableEditor> getTableEditors(IWorkbenchPartReference partRef){
		ArrayList<DipTableEditor> result = new ArrayList<>();
		
		for (IEditorReference editorRef: partRef.getPage().getEditorReferences()){
			if (DipTableEditor.EDITOR_ID.equals(editorRef.getId())) {
				result.add((DipTableEditor) editorRef.getEditor(true));
			}
		}
		return result;
	}
	
	public EditViewPart getEditView(IWorkbenchPartReference partRef){
		return (EditViewPart) PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().findView(EditViewPart.ID);
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
