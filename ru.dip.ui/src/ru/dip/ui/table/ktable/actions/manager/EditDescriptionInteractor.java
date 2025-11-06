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
package ru.dip.ui.table.ktable.actions.manager;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.widgets.Shell;

import ru.dip.core.exception.DIPException;
import ru.dip.core.model.IncludeFolder;
import ru.dip.core.model.DipProject;
import ru.dip.core.model.interfaces.IDipElement;
import ru.dip.core.model.interfaces.IDipDocumentElement;
import ru.dip.core.utilities.DipUtilities;
import ru.dip.core.utilities.tmp.EditDescriptionResult;
import ru.dip.ui.Messages;
import ru.dip.ui.dialog.EditDescriptionDialog;
import ru.dip.ui.table.editor.DipTableEditor;
import ru.dip.ui.table.ktable.KDipTableSelector;
import ru.dip.ui.table.ktable.KTableComposite;
import ru.dip.ui.table.ktable.model.DipTableModel;
import ru.dip.ui.table.ktable.model.HideElements;
import ru.dip.ui.table.ktable.model.IDipTableElement;
import ru.dip.ui.table.ktable.model.TableElement;
import ru.dip.ui.table.table.TableSettings;

public class EditDescriptionInteractor {
	
	private KTableComposite fTableComposite;
	
	public EditDescriptionInteractor(KTableComposite tableComposite) {
		fTableComposite = tableComposite;
	}
	
	public EditDescriptionResult doRemoveDescription() {
		IDipDocumentElement selectedElement = selector().getSelectedOneDipDocElement();
		if (selectedElement.isReadOnly() && !(selectedElement instanceof IncludeFolder)) {
			return null;
		}
		String oldDescription = selectedElement.description();
		IDipTableElement element = selector().getLastSelectObject().endElement(HideElements.EXCLUDE);
		selectedElement.removeDescription();
		deleteIfExistsEmptyDescription(element);
		editor().updater().updateUnitElement(element, true);
		return new EditDescriptionResult(DipUtilities.relativeProjectID(selectedElement),
				oldDescription, null);
	}
	
	private boolean deleteIfExistsEmptyDescription(IDipTableElement element) {
		if (element.isEmptyDescription()) {
			tableModel().getElements().remove(element);
			return true;
		}
		return false;
	}
	

	public EditDescriptionResult doEditDescription(int column, int row) {
		Object obj = fTableComposite.tableModel().getContentAt(column, row);
		if (obj instanceof TableElement) {
			IDipDocumentElement selectedElement = ((TableElement) obj).dipDocElement();
			if (selectedElement.isReadOnly() && !(selectedElement instanceof IncludeFolder)) {
				return null;
			}
			if (TableSettings.isEditDescInTable()) {
				doEditDescriptionInTable(column, row);
				return null; // в стек добавится через callback
			} else {
				return doEditDescriptionInDialog(selectedElement);
			}
		}
		return null;
	}

	private void doEditDescriptionInTable(int column, int row) {
		fTableComposite.cellEditorManager().openCellEditor(column, row);
	}
	
	//===========================
	// in dialog

	public EditDescriptionResult doEditDescriptionInDialog() {
		IDipDocumentElement selectedElement = selector().getSelectedOneDipDocElement();
		if (selectedElement == null) {
			return null;
		}
		if (selectedElement.isReadOnly() && !(selectedElement instanceof IncludeFolder)) {
			return null;
		}
		return doEditDescriptionInDialog(selectedElement);
	}

	private EditDescriptionResult doEditDescriptionInDialog(IDipDocumentElement selectedElement) {
		String description = selectedElement.description();
		if (description == null) {
			description = ""; //$NON-NLS-1$
		}
		EditDescriptionDialog dialog = new EditDescriptionDialog(getShell(),
				Messages.KTableComposite_IndputDesctiptionLabel + selectedElement.id(), description);
		if (Dialog.OK == dialog.open()) {
			String oldDescription = selectedElement.description();
			String newDescription = dialog.getResult();
			IDipTableElement endElement = selector().getLastSelectObject().endElement(HideElements.EXCLUDE);
			updateDescription(endElement, selectedElement, newDescription);
			if(Objects.equals(newDescription, oldDescription)) {
				return null;
			}			
			return new EditDescriptionResult(DipUtilities.relativeProjectID(selectedElement),
					oldDescription, newDescription);
		}
		return null;
	}

	//========================
	// update description

	public void updateDescription(IDipTableElement endElement, IDipDocumentElement dipDocElement, String newDescription) {
		if (newDescription == null || newDescription.trim().isEmpty()) {
			dipDocElement.removeDescription();
			deleteIfExistsEmptyDescription(endElement);
		} else {
			dipDocElement.updateDescription(newDescription);
			addDescriptionToModel(endElement);
		}
		editor().updater().updateUnitElement(endElement, true);
	}

	private void addDescriptionToModel(IDipTableElement endElement) {
		if (!tableModel().getElements().contains(endElement)) {
			IDipTableElement startElement = endElement.startElement(HideElements.EXCLUDE);
			int indexStartElement = tableModel().getElements().indexOf(startElement);
			int index = indexStartElement + startElement.parent().linkedElements(startElement, HideElements.EXCLUDE).size() - 1;
			tableModel().getElements().add(index, endElement);
		}
	}
	
	//=========================
	// undo
	
	public void undoEditDescription(EditDescriptionResult result) throws DIPException {
		setDescription(result.getId(), result.getOldDescription());
	}
	
	public void redoEditDescription(EditDescriptionResult result) throws DIPException {
		setDescription(result.getId(), result.getNewDescription());
	}
	
	private void setDescription(String reqId, String description) throws DIPException {
		Optional<IDipElement> reqOpt = DipUtilities.findDipElementInProject(reqId, dipProject());
		if (reqOpt.isEmpty()) {
			throw new DIPException("Edit Description Error. Element not found.");
		}
		IDipDocumentElement dipDocElement = (IDipDocumentElement) reqOpt.get();
		
		if (description == null || description.isEmpty()) {
			dipDocElement.removeDescription();
		} else {
			dipDocElement.updateDescription(description);
		}
		Optional<IDipTableElement> elementOpt = tableModel().findElementByName(dipDocElement);
		if (elementOpt.isEmpty()) {
			throw new DIPException("Edit Description Error. Element not found.");
		}		
		IDipTableElement descriptionElement = elementOpt.get().endElement(HideElements.EXCLUDE);
		if (deleteIfExistsEmptyDescription(descriptionElement)) {
			fTableComposite.selector().setTableElementSelection(elementOpt.get());
		} else {			
			tableModel().updateElements(List.of(descriptionElement));
			addDescriptionToModel(descriptionElement);
			fTableComposite.selector().setTableElementSelection(descriptionElement);
		}
		
		tableModel().getTableModel().additionalUpdate(descriptionElement.dipDocElement());
		editor().updater().updateUnitElement(elementOpt.get(), true);
	}
		
	// =========================
	// utitlites & getters

	private KDipTableSelector selector() {
		return fTableComposite.selector();
	}

	private Shell getShell() {
		return fTableComposite.getShell();
	}

	private DipProject dipProject() {
		return fTableComposite.dipProject();
	}
	
	private DipTableModel tableModel() {
		return fTableComposite.tableModel();
	}
	
	private DipTableEditor editor() {
		return fTableComposite.editor();
	}
		
}
