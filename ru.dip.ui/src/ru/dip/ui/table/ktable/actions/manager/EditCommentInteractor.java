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

import java.util.Optional;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.widgets.Shell;

import ru.dip.core.exception.DIPException;
import ru.dip.core.model.DipProject;
import ru.dip.core.model.IncludeFolder;
import ru.dip.core.model.interfaces.IDipComment;
import ru.dip.core.model.interfaces.IDipDocumentElement;
import ru.dip.core.model.interfaces.IDipElement;
import ru.dip.core.utilities.DipUtilities;
import ru.dip.core.utilities.tmp.EditCommentResult;
import ru.dip.ui.table.ktable.KDipTableSelector;
import ru.dip.ui.table.ktable.KTableComposite;
import ru.dip.ui.table.ktable.dialog.EditCommentDialog;
import ru.dip.ui.table.ktable.model.DipTableModel;
import ru.dip.ui.table.ktable.model.IDipTableElement;
import ru.dip.ui.table.ktable.model.TableElement;
import ru.dip.ui.table.table.TableSettings;

public class EditCommentInteractor {
	
	private KTableComposite fTableComposite;
	
	public EditCommentInteractor(KTableComposite tableComposite) {
		fTableComposite = tableComposite;
	}
	
	public EditCommentResult doRemoveComment() {
		IDipDocumentElement selectedElement = selector().getSelectedOneDipDocElement();
		if (selectedElement.isReadOnly() || selectedElement instanceof IncludeFolder) {
			return null;
		}
		String oldComment = selectedElement.getCommentContent();
		selectedElement.comment().deleteMainContent();
		if (selectedElement.comment().isEmpty()) {
			selectedElement.deleteDipComment();
		}
		fTableComposite.editor().updater().updateUnitElement(selector().getLastSelectObject(), true);
		return new EditCommentResult(DipUtilities.relativeProjectID(selectedElement),
				oldComment, null);
	}
	
	public EditCommentResult doEditComment(int column, int row) {
		Object obj = fTableComposite.tableModel().getContentAt(column, row);
		if (obj instanceof TableElement) {
			IDipDocumentElement selectedElement = ((TableElement) obj).dipDocElement();
			if (selectedElement.isReadOnly() && !(selectedElement instanceof IncludeFolder)) {
				return null;
			}
			if (TableSettings.isEditDescInTable()) {
				doEditCommentInTable(column, row);
				return null;  // в стек добавится через callback
			} else {
				return doEditCommentInDialog(selectedElement);
			}
		}
		return null;		
	}

	private void doEditCommentInTable(int column, int row) {
		fTableComposite.cellEditorManager().openCommentCellEditor(column, row);				
	}
	
	//===========================
	// in dialog

	public EditCommentResult doEditCommentInDialog() {
		IDipDocumentElement selectedElement = selector().getSelectedOneDipDocElement();
		if (selectedElement.isReadOnly() && !(selectedElement instanceof IncludeFolder)) {
			return null;
		}
		return doEditCommentInDialog(selectedElement);
	}

	private EditCommentResult doEditCommentInDialog(IDipDocumentElement selectedElement) {
		String commentText = null;
		IDipComment comment = selectedElement.comment();
		if (comment != null) {
			commentText = comment.getCommentContent();
		}
		EditCommentDialog dialog = new EditCommentDialog(getShell(), commentText);
		if (dialog.open() == Dialog.OK) {
			String newComment = dialog.getResult();
			selectedElement.updateDipComment(newComment);
			IDipTableElement element = selector().getLastSelectObject();
			fTableComposite.editor().updater().updateUnitElement(element, true);			
			return new EditCommentResult(DipUtilities.relativeProjectID(selectedElement),
					commentText, newComment);
		}
		return null;
	}

	//========================
	// update comment

	public void updateComment(IDipTableElement endElement, IDipDocumentElement dipDocElement, String newComment) {
		dipDocElement.updateDipComment(newComment);
		fTableComposite.editor().updater().updateUnitElement(endElement, true);
	}
	
	//=========================
	// undo
	
	public void undoEditComment(EditCommentResult result) throws DIPException {
		setComment(result.getId(), result.getOldComment());
	}
	
	public void redoEditComment(EditCommentResult result) throws DIPException {
		setComment(result.getId(), result.getNewComment());
	}
	
	private void setComment(String reqId, String comment) throws DIPException {
		Optional<IDipElement> reqOpt = DipUtilities.findDipElementInProject(reqId, dipProject());
		if (reqOpt.isEmpty()) {
			throw new DIPException("Edit Comment Error. Element not found.");
		}
		IDipDocumentElement dipDocElement = (IDipDocumentElement) reqOpt.get();
		dipDocElement.updateDipComment(comment);
		Optional<IDipTableElement> elementOpt = tableModel().findElementByName(dipDocElement);
		if (elementOpt.isEmpty()) {
			throw new DIPException("Edit Comment Error. Element not found.");
		}
		fTableComposite.editor().updater().updateUnitElement(elementOpt.get(), false);
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
	
}
