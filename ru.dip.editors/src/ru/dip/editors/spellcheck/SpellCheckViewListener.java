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
package ru.dip.editors.spellcheck;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.PlatformUI;

import ru.dip.ui.table.editor.DipTableEditor;

public class SpellCheckViewListener implements IPartListener2 {

	@Override
	public void partActivated(IWorkbenchPartReference partRef) {
		IWorkbenchPart part = partRef.getPart(false);
		if (part instanceof SpellCheckView){
			SpellCheckView editView = (SpellCheckView) part;
			for (DipTableEditor tableEditor : getTableEditors(partRef)){
				tableEditor.addPropertyListener(editView);			
			}
		} else if (part instanceof DipTableEditor){
			DipTableEditor dipTableEditor = (DipTableEditor) part;
			SpellCheckView editView = getEditView(partRef);
			if (editView != null) {
				dipTableEditor.addPropertyListener(editView);
			}
		}
	}
	
	@Override
	public void partVisible(IWorkbenchPartReference partRef) {
        IWorkbenchPart part = partRef.getPart(false);
        if (part instanceof DipTableEditor) {
        	DipTableEditor tableEditor = (DipTableEditor) part;
        	tableEditor.fireUpdateTableComposite();
        } 
        if (part instanceof SpellCheckView) {
        	IEditorPart editPart = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
        	if (editPart instanceof DipTableEditor) {
        		((DipTableEditor) editPart).fireUpdateTableComposite();
        	}
        }
	}
	
	public List<DipTableEditor> getTableEditors(IWorkbenchPartReference partRef){
		List<DipTableEditor> result = new ArrayList<>();
		for (IEditorReference editorRef: partRef.getPage().getEditorReferences()){
			if (DipTableEditor.EDITOR_ID.equals(editorRef.getId())) {
				result.add((DipTableEditor) editorRef.getEditor(true));
			}
		}
		return result;
	}
	
	public SpellCheckView getEditView(IWorkbenchPartReference partRef){
		return (SpellCheckView) PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().findView(SpellCheckView.ID);
	}
	
}
