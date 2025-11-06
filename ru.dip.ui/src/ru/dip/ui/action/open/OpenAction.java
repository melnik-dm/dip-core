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
package ru.dip.ui.action.open;

import java.util.Iterator;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchSite;
import org.eclipse.ui.PartInitException;

import ru.dip.core.model.DipProject;
import ru.dip.core.model.DnfoTable;
import ru.dip.core.model.TocRef;
import ru.dip.core.model.glossary.GlossaryField;
import ru.dip.core.model.interfaces.IDipDocumentElement;
import ru.dip.core.model.interfaces.IDipElement;
import ru.dip.core.model.vars.IVarContainer;
import ru.dip.core.model.vars.Variable;
import ru.dip.core.utilities.WorkbenchUtitlities;
import ru.dip.ui.Messages;
import ru.dip.ui.action.hyperlink.ReqLink;
import ru.dip.ui.glossary.EditGlossFieldDialog;
import ru.dip.ui.table.editor.DipTableEditor;
import ru.dip.ui.table.editor.TableEditorInput;
import ru.dip.ui.toc.DipTocView;
import ru.dip.ui.utilities.GlobalKeyListener;
import ru.dip.ui.variable.VarManager;

public class OpenAction extends SelectionDispatchAction {

	public OpenAction(IWorkbenchSite site) {
		super(site);
		setText(Messages.OpenAction_Action_name);
		setToolTipText(Messages.OpenAction_ToolTip);
		//setDescription("ActionMessages.OpenAction_description");
	}

	@Override
	public void selectionChanged(ITextSelection selection) {
	}

	@Override
	public void selectionChanged(IStructuredSelection selection) {
		setEnabled(checkEnabled(selection));
	}

	private boolean checkEnabled(IStructuredSelection selection) {
		if (selection == null){
			return false;
		}		
		if (selection.isEmpty()){
			return false;
		}
		if (isGlossaryField(selection)){
			return true;
		}
		if (isVariable(selection)){
			return true;
		}
		if (isVarContainer(selection)) {
			return true;
		}
		
		for (Iterator<?> iter = selection.iterator(); iter.hasNext();) {
			Object element = iter.next();
			if (element instanceof DnfoTable) {
				continue;
			}			
			if (element instanceof IDipElement){
				IDipElement dipElement  = (IDipElement) element;
				if (dipElement.isReadOnly()) {
					return false;
				}				
				return dipElement.resource() instanceof IFile;
			}			
			return false;
		}
		return true;
	}

	@Override
	public void run(IStructuredSelection selection) {		
		boolean ctrlPressed = GlobalKeyListener.isCtrl();
		if (ctrlPressed && GlobalKeyListener.navigatorSelection instanceof DnfoTable) {
			DnfoTable table = (DnfoTable) GlobalKeyListener.navigatorSelection;
			if (!(table.parent() instanceof DipProject)) {
				ReqLink.openElementInTable((IDipDocumentElement) table.parent());
				GlobalKeyListener.navigatorSelection = null;
				return;
			}
		} 
		if (!checkEnabled(selection)){
			return;
		}
		if (isGlossaryField(selection)){
			openEditGlossaryFieldDialog((GlossaryField) selection.getFirstElement());
			return;
		}
		if (isVariable(selection)) {
			VarManager.openEditVariableDialog(getShell(), (Variable) selection.getFirstElement());
			return;
		}
		if (isVarContainer(selection)) {
			VarManager.openVarContainerDialog(getShell(), (IVarContainer) selection.getFirstElement());
			return;
		}
		
		run(selection.toArray());		
	}
	
	private boolean isGlossaryField(IStructuredSelection selection){
		return selection.size() == 1 
				&& selection.getFirstElement() instanceof GlossaryField;
	}
	
	private boolean isVariable(IStructuredSelection selection){
		return selection.size() == 1 
				&& selection.getFirstElement() instanceof Variable;
	}
	
	private boolean isVarContainer(IStructuredSelection selection){
		return selection.size() == 1 
				&& selection.getFirstElement() instanceof IVarContainer;
	}
	
	private void openEditGlossaryFieldDialog(GlossaryField field){
		EditGlossFieldDialog dialog = new EditGlossFieldDialog(getShell(), field);
		dialog.open();
	}

	public void run(Object[] elements) {
		if (elements == null){
			return;
		}
		for (int i = 0; i < elements.length; i++) {
			Object element = elements[i];
			if (element instanceof DnfoTable) {
				DnfoTable table = (DnfoTable) element;
				IWorkbenchPage page = getSite().getPage();
				IEditorInput editorInput = new TableEditorInput(table);
				try {
					
					IEditorPart part = page.openEditor(editorInput, DipTableEditor.EDITOR_ID);
					part.setFocus();
				} catch (PartInitException e) {
					e.printStackTrace();
				}			
			} else if (element instanceof IDipElement){
				IDipElement dipElement = (IDipElement) element;
				
				if (dipElement instanceof TocRef) {
					WorkbenchUtitlities.openView(DipTocView.ID);
				} else if (dipElement.resource() instanceof IFile){
					openFile((IFile) dipElement.resource());
				}
			}
		}
	}

	void openFile(IFile file) {
	    WorkbenchUtitlities.openFile(file);
    }

}
