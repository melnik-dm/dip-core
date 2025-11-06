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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartReference;

import ru.dip.core.model.interfaces.IDipDocumentElement;
import ru.dip.core.model.interfaces.IDipElement;
import ru.dip.core.utilities.DipUtilities;
import ru.dip.core.utilities.WorkbenchUtitlities;
import ru.dip.editors.dipview.DipView;
import ru.dip.editors.md.MDEditor;
import ru.dip.table.editor.MultiPageTableEditor;
import ru.dip.ui.table.editor.DipTableEditor;

public class ReqWorkbenchListener implements IPartListener2 {
		
	private IFile fDeactivatedFile; // последний активный файл
	
	@Override
	public void partOpened(IWorkbenchPartReference partRef) {
		IWorkbenchPart part = partRef.getPart(false);
		if (part instanceof DipView) {
			DipView imageView = (DipView) part;
			for (MDEditor mdEditor: getMDEditors(partRef)){
				mdEditor.addPropertyListener(imageView);
			}
			for (MultiPageTableEditor tableEditor: getTableEditors(partRef)){
				tableEditor.addPropertyListener(imageView);
			}			
		} else if (part instanceof MDEditor) {
			MDEditor mdEditor = (MDEditor) part;
			DipView imageView = getImageView(partRef);
			if (imageView != null) {
				mdEditor.addPropertyListener(imageView);
			}
		} else if (part instanceof MultiPageTableEditor) {
			MultiPageTableEditor tbEditor = (MultiPageTableEditor) part;
			DipView imageView = getImageView(partRef);
			if (imageView != null) {
				tbEditor.addPropertyListener(imageView);
			}
		}
	}

	@Override
	public void partHidden(IWorkbenchPartReference partRef) {
        IWorkbenchPart part = partRef.getPart(false);
        if (part instanceof MDEditor){
        	MDEditor mdEditor = (MDEditor) part;
        	mdEditor.hide();
        } else if (part instanceof MultiPageTableEditor){
        	MultiPageTableEditor tableEditor = (MultiPageTableEditor) part;
        	tableEditor.hide();
        }
	}

	@Override
	public void partVisible(IWorkbenchPartReference partRef) {
        IWorkbenchPart part = partRef.getPart(false);
        if (part instanceof MDEditor){
        	MDEditor mdEditor = (MDEditor) part;
        	mdEditor.visible();
        } else if (part instanceof MultiPageTableEditor){
        	MultiPageTableEditor tableEditor = (MultiPageTableEditor) part;
        	tableEditor.visible();
        }
	}
	
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
	
	private List<MDEditor> getMDEditors(IWorkbenchPartReference partRef){
		List<MDEditor> result = new ArrayList<>();
		for (IEditorReference editorRef: partRef.getPage().getEditorReferences()){
			if (MDEditor.ID.equals(editorRef.getId())) {
				IEditorPart editor = editorRef.getEditor(true);
				if (editor instanceof MDEditor) {
					result.add((MDEditor) editor);
				}
			}
		}
		return result;	
	}
	
	private List<MultiPageTableEditor> getTableEditors(IWorkbenchPartReference partRef){
		List<MultiPageTableEditor> result = new ArrayList<>();
		for (IEditorReference editorRef: partRef.getPage().getEditorReferences()){
			if (MultiPageTableEditor.ID.equals(editorRef.getId())) {
				result.add((MultiPageTableEditor) editorRef.getEditor(true));
			}
		}
		return result;	
	}
	
	
	private DipView getImageView(IWorkbenchPartReference partRef){
		return (DipView) partRef
        .getPage().findView(DipView.ID);  
	}

	@Override
	public void partInputChanged(IWorkbenchPartReference partRef) {	
	}
	
	@Override
	public void partBroughtToTop(IWorkbenchPartReference partRef) {		
	}

	@Override
	public void partClosed(IWorkbenchPartReference partRef) {
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
