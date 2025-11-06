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
package ru.dip.ui.navigator;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.navigator.ILinkHelper;
import org.eclipse.ui.part.FileEditorInput;

import ru.dip.core.manager.DipNatureManager;
import ru.dip.core.model.DipUnit;
import ru.dip.core.model.DnfoTable;
import ru.dip.core.model.interfaces.IDipElement;
import ru.dip.core.utilities.DipUtilities;
import ru.dip.ui.table.editor.TableEditorInput;

public class ReqLinkHelper implements ILinkHelper {

	public ReqLinkHelper() {
	}
	
	@Override
	public IStructuredSelection findSelection(IEditorInput anInput) {
		if (anInput instanceof FileEditorInput) {
			IFile file = ((FileEditorInput) anInput).getFile();
			if (DipNatureManager.hasNature(file)) {							
				IDipElement element = DipUtilities.findElement(file);
				if (element != null) {
					return new StructuredSelection(element);
				}				
			} 
		} else if (anInput instanceof TableEditorInput){
			TableEditorInput tableInput = (TableEditorInput) anInput;
			DnfoTable table = tableInput.table();
			if (table != null) {
				return new StructuredSelection(table);
			}		
		} 	
		return StructuredSelection.EMPTY;
	}

	@Override
	public void activateEditor(IWorkbenchPage aPage, IStructuredSelection aSelection) {		
		if (aSelection == null || aSelection.isEmpty())
			return;
		if (aSelection.getFirstElement() instanceof IDipElement) {
			IDipElement rElement = (IDipElement) aSelection.getFirstElement();
			if (rElement instanceof DipUnit) {
				IEditorInput fileInput = new FileEditorInput((IFile) rElement.resource());
				IEditorPart editor = null;
				if ((editor = aPage.findEditor(fileInput)) != null) {
					aPage.bringToTop(editor);
				}								
			} else if (rElement instanceof DnfoTable) {
				TableEditorInput tabEditorInput = new TableEditorInput((DnfoTable) rElement);
				IEditorPart editor = null;
				if ((editor = aPage.findEditor(tabEditorInput)) != null) {
					aPage.bringToTop(editor);
				}
			}
		}		
	}

}
