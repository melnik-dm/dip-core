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
package ru.dip.ui.action.rename;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.IShellProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.actions.WorkspaceAction;

import ru.dip.core.model.ExportConfig;
import ru.dip.core.exception.RenameDIPException;
import ru.dip.core.exception.SaveTableDIPException;
import ru.dip.core.model.DipFolder;
import ru.dip.core.model.DipProject;
import ru.dip.core.model.DipSchemaElement;
import ru.dip.core.model.DipUnit;
import ru.dip.core.model.interfaces.IDipDocumentElement;
import ru.dip.core.model.interfaces.IDipElement;
import ru.dip.core.model.reports.Report;
import ru.dip.core.utilities.DipUtilities;
import ru.dip.core.utilities.WorkbenchUtitlities;
import ru.dip.ui.Messages;
import ru.dip.ui.controller.RenameController;
import ru.dip.ui.dialog.RenameSchemaDialog;
import ru.dip.ui.table.editor.DipEditorRegister;
import ru.dip.ui.wizard.rename.RenameWizard;

public class RenameAction extends WorkspaceAction {

	private IDipElement fSelectionDipElement;
	private IShellProvider fShellProvider;
	
	public RenameAction(IShellProvider provider, Tree tree) {
		super(provider,Messages.RenameAction_Action_name);
		fShellProvider = provider;
	}

	@Override
	public void run() {
		if (fSelectionDipElement instanceof DipSchemaElement) {
			renameDipSchema();
		} else {
			renameDipDocElement();
		}
	}
	
	private void renameDipDocElement(){
		RenameWizard  wizard = new RenameWizard(fSelectionDipElement);
		WizardDialog wd = new WizardDialog(fShellProvider.getShell(), wizard);
		wd.setTitle(wizard.getWindowTitle());
		if (wd.open() == WizardDialog.OK){									
			RenameController controller = new RenameController(wizard.getDipElement(), wizard.getName(), 
					wizard.isNeedReserve(), fShellProvider.getShell());
			controller.doRename();
		
			// обвновить в редакторах
			if (fSelectionDipElement instanceof IDipDocumentElement) {
				IDipDocumentElement dde = (IDipDocumentElement) fSelectionDipElement;
				DipEditorRegister.instance.findEditors(dde)
					.forEach(editor -> editor.updater().updateAfterRename(dde.parent(), wizard.getName(), false));								
			}
			
			// обновить ресурсы
			try {
				fSelectionDipElement.resource().refreshLocal(IResource.DEPTH_INFINITE, null);												
			} catch (CoreException e) {
				// NOP
			}
			
			// обновить project exploter
			WorkbenchUtitlities.updateNavigatorServiceExplorer();			
		}
	}
	
	private void renameDipSchema() {
		DipSchemaElement schema = (DipSchemaElement) fSelectionDipElement;		
		RenameSchemaDialog dialog = new RenameSchemaDialog(fShellProvider.getShell(), schema);
		if (dialog.open() == Window.OK) {
			String newExtension = dialog.getExtension();
			try {
				DipUtilities.renameElement(schema, newExtension + ".xml", false, fShellProvider.getShell());
				schema.parent().refresh();
				schema.dipProject().getSchemaModel().update();
				
				// обновить ресурсы
				try {
					fSelectionDipElement.resource().refreshLocal(IResource.DEPTH_INFINITE, null);												
				} catch (CoreException e) {
					// NOP
				}
				
				// обновить project exploter
				WorkbenchUtitlities.updateNavigatorServiceExplorer();
				
			} catch (RenameDIPException | SaveTableDIPException e) {
				e.printStackTrace();
			}	
		}
	}
	
	@Override
	protected boolean updateSelection(IStructuredSelection selection) {
		fSelectionDipElement = null;
		if (selection == null || selection.isEmpty() || selection.size() != 1){
			return false;
		}
		Object obj = selection.getFirstElement();
		if (obj instanceof DipUnit || obj instanceof DipFolder 
				|| obj instanceof DipProject 
				|| obj instanceof Report 
				|| obj instanceof DipSchemaElement
				|| obj instanceof ExportConfig){
			fSelectionDipElement = (IDipElement) obj;
			if (fSelectionDipElement.isReadOnly()) {
				return false;
			}			
			return true;
		}
		return false;
	}

	@Override
	protected String getOperationMessage() {
		return null;
	}

}
