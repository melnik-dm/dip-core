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
package ru.dip.ui.action.edit;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Stream;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.IShellProvider;
import org.eclipse.ui.actions.SelectionListenerAction;

import ru.dip.core.model.DipProject;
import ru.dip.core.model.DipSchemaElement;
import ru.dip.core.model.glossary.GlossaryField;
import ru.dip.core.model.glossary.GlossaryFolder;
import ru.dip.core.model.interfaces.IDipDocumentElement;
import ru.dip.core.model.interfaces.IDipElement;
import ru.dip.core.model.interfaces.IParent;
import ru.dip.core.model.vars.FolderVarContainer;
import ru.dip.core.model.vars.IVarContainer;
import ru.dip.core.model.vars.Variable;
import ru.dip.core.utilities.DipTableUtilities;
import ru.dip.core.utilities.DipUtilities;
import ru.dip.core.utilities.ResourcesUtilities;
import ru.dip.core.utilities.WorkbenchUtitlities;
import ru.dip.ui.Messages;
import ru.dip.ui.controller.DeleteController;
import ru.dip.ui.controller.DeleteController.DeleteControllerBuilder;
import ru.dip.ui.dialog.DeleteDialog;
import ru.dip.ui.table.editor.DipEditorRegister;
import ru.dip.ui.variable.dialog.VarMessagesDialogs;

public class DeleteAction extends SelectionListenerAction {

	private IShellProvider fShellProvider;
	private IDipElement[] fSelection;
	private HashSet<DipProject> fProjectsToUpdate = new HashSet<>(); 
	private boolean fGlossaryField = false;  // при удалении записей из глоссария
	private boolean fVariable = false;  // при удалении переменных
	private boolean fVarContainer = false;
	private boolean fSchema = false;
	
	public DeleteAction(IShellProvider provider) {
		super(Messages.DeleteAction_Action_name);
		fShellProvider = provider;
	}
	
	//====================================
	// update selection
	
	@Override
	protected boolean updateSelection(IStructuredSelection selection) {		
		fSelection = getSelection(selection);
		if (fSelection == null){
			setEnabled(false);
			return false;
		}
		setEnabled(true);
		return true;
	}
	
	private IDipElement[] getSelection(IStructuredSelection selection){
		fGlossaryField = false;
		fVariable = false;
		fVarContainer = false;
		fSchema = false;
		
		if (!selection.isEmpty()){
			Object[] objects = selection.toArray();
			List<IDipElement> selectionElements = new ArrayList<>();
			IParent previousElement = null;
			
			// если удаляются записи из глоссария
			IDipElement[] result = isGlossaryFields(objects);
			if (result != null){
				fGlossaryField = true;
				return result;
			}
			
			// если удаляются схемы
			result = isSchemas(objects);
			if (result != null){
				fSchema = true;
				return result;
			}
			
			// если удаляются переменные
			result = isVariables(objects);
			if (result != null) {
				// доступны ли для удаления
				for (IDipElement element: result) {
					if (!element.canDelete()){
						return null;
					}
				}
				fVariable = true;
				return result;
			}
			
			// если контейнер с переменными из папки
			if (objects.length == 1 && objects[0] instanceof FolderVarContainer) {
				fVarContainer = true;
				return new IDipElement[] {(IDipElement) objects[0]};				
			}
											
			for (int i = 0; i < objects.length; i++){
				if (objects[i] instanceof IDipElement){
					IDipElement element = (IDipElement) objects[i];
					if (!DipUtilities.canDeleteElement(element)){
						return null;
					}					
					if (previousElement == null){
						if (element instanceof IParent){
							previousElement = (IParent) element;
						}
						selectionElements.add(element);
					} else if (element.hasParent(previousElement)){
						continue;						
					} else {
						if (element instanceof IParent){
							previousElement = (IParent) element;
						}
						selectionElements.add(element);
					}
				} else {
					return null;
				}
			}
			result = new IDipElement[selectionElements.size()];
			selectionElements.toArray(result);
			return result;
		}
		return null;
	}
	
	private IDipElement[] isGlossaryFields(Object[] objects){
		for (Object obj: objects){
			if (!(obj instanceof GlossaryField)){
				return null;
			}
		}
		return Arrays.stream(objects).map(e -> (IDipElement)e).toArray(IDipElement[]::new);
	}
	
	private IDipElement[] isSchemas(Object[] objects){
		for (Object obj: objects){
			if (!(obj instanceof DipSchemaElement)){
				return null;
			}
		}
		return Arrays.stream(objects).map(e -> (IDipElement)e).toArray(IDipElement[]::new);
	}
	
	private IDipElement[] isVariables(Object[] objects){
		for (Object obj: objects){
			if (!(obj instanceof Variable)){
				return null;
			}
		}
		return Arrays.stream(objects).map(e -> (IDipElement)e).toArray(IDipElement[]::new);
	}
		
	//=========================
	// run action
		
	@Override
	public void run() {
		if (fGlossaryField){
			deleteGlossaryFields();
		} else if (fVariable) {
			deleteVariables();
		} else if (fVarContainer) { 
			deleteVarContainer();
		} else {
			getProjectToUpdate();
			deleteDipElements();
			updateProjectsNumeration();
			saveDnfo();
		}		
	}
	
	//==========================
	// delete glossary fields
	
	private void deleteGlossaryFields(){
		boolean confirmation = MessageDialog.openQuestion(fShellProvider.getShell(),
				Messages.DeleteAction_Confirm_title, Messages.DeleteAction_Confirm_gloss_message);
		if (confirmation){
			GlossaryFolder glossFolder = getGlossaryFolder();
			if (glossFolder != null){
				try {
					glossFolder.deleteFields(fSelection);
					WorkbenchUtitlities.updateProjectExplorer();
				} catch (IOException e) {
					MessageDialog.openError(fShellProvider.getShell(), Messages.DeleteAction_Glossary_error_title, Messages.DeleteAction_Glossary_error_message);
					e.printStackTrace();
				}
			}			
		}				
	}
	
	private GlossaryFolder getGlossaryFolder() {
		if (fSelection != null && fSelection.length > 0)
			if (fSelection[0] instanceof GlossaryField){
			return ((GlossaryField)fSelection[0]).parent();
		}
		return null;
	}
	
	//==========================
	// delete variables
	
	private void deleteVariables(){
		if (VarMessagesDialogs.confirmDeleteVariables(fSelection, fShellProvider.getShell())){
			IVarContainer varContainer = getVarContainer();
			if (varContainer != null){
				try {
					varContainer.deleteVariables(fSelection);
					WorkbenchUtitlities.updateProjectExplorer();
				} catch (IOException e) {
					VarMessagesDialogs.showSaveContainerError(varContainer, fShellProvider.getShell());
					e.printStackTrace();
				}
			}
		}
	}
	
	private void deleteVarContainer() {
		if (!VarMessagesDialogs.confirmDeleteVarContainer((IVarContainer) fSelection[0], fShellProvider.getShell())){
			return;
		}		
		if (fSelection != null && fSelection.length == 1 && fSelection[0] instanceof FolderVarContainer) {
			FolderVarContainer varContainer = (FolderVarContainer) fSelection[0];
			try {
				ResourcesUtilities.deleteResource(varContainer.resource(), fShellProvider.getShell());
				varContainer.getDipParent().deleteVarContainer();
				ResourcesUtilities.updateContainer(varContainer.getDipParent().resource());
				WorkbenchUtitlities.updateProjectExplorer(varContainer.getDipParent());
			} catch (CoreException e) {
				e.printStackTrace();
			}
		}
	}
	
	private IVarContainer getVarContainer() {
		if (fSelection != null && fSelection.length > 0)
			if (fSelection[0] instanceof Variable){
			return ((Variable)fSelection[0]).parent();
		}
		return null;
	}
	
	//============================
	// dip elements
	
	private void deleteDipElements(){
		DeleteDialog deleteDialog = new DeleteDialog(fShellProvider.getShell(), fSelection);
		if (deleteDialog.open() == Dialog.OK){
			DeleteController controller = new DeleteControllerBuilder()
					.elements(deleteDialog.getElements())
					.reserve(deleteDialog.isReserve())
					.extract(deleteDialog.isExtract())
					.deleteProjectContent(deleteDialog.isDeleteProjectContent())
					.build();
			controller.delete();
			controller.checkLinks();
			
			// update resources
			if (Stream.of(deleteDialog.getElements()).anyMatch(DipProject.class::isInstance)) {
				ResourcesUtilities.updateRoot();				
			} else {					
				Stream.of(deleteDialog.getElements()).map(IDipElement::parent)
					.map(IParent::resource)
					.filter(IContainer.class::isInstance)
					.map(IContainer.class::cast)
					.distinct()
					.forEach(ResourcesUtilities::updateContainer);
			}
			WorkbenchUtitlities.updateProjectExplorer();

			
			// обновление в редакторах
			DipEditorRegister.instance.findEditorsAndGroup(fSelection).entrySet()
				.forEach(e -> e.getKey().updater()
						.updateAfterDelete(e.getValue().stream().toArray(IDipDocumentElement[]::new)));
			
			if (fSchema) {
				for (DipProject dp: fProjectsToUpdate) {
					dp.getSchemaModel().update();
				}
			}
		}	
	}
	
	private void saveDnfo() {
		fProjectsToUpdate.forEach(DipTableUtilities::saveModel);
	}
	
	private void getProjectToUpdate(){
		fProjectsToUpdate = new HashSet<>();
		for (IDipElement element: fSelection) {
			if (!(element instanceof DipProject)) {
				fProjectsToUpdate.add(element.dipProject());
			}
		}		
	}
	
	private void updateProjectsNumeration() {
		fProjectsToUpdate.forEach(DipProject::updateNumeration);
	}

}
