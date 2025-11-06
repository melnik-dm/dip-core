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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;

import ru.dip.core.DipCorePlugin;
import ru.dip.core.exception.CopyDIPException;
import ru.dip.core.exception.DeleteDIPException;
import ru.dip.core.link.LinkInteractor;
import ru.dip.core.model.DipUnit;
import ru.dip.core.model.interfaces.IDipParent;
import ru.dip.core.model.interfaces.IDipDocumentElement;
import ru.dip.core.utilities.DipTableUtilities;
import ru.dip.core.utilities.DipUtilities;
import ru.dip.core.utilities.tmp.MoveResult;
import ru.dip.ui.Messages;
import ru.dip.ui.table.ktable.KDipTableSelector;
import ru.dip.ui.table.ktable.KTableComposite;
import ru.dip.ui.table.ktable.dialog.SelectIntoFodlerDialog;
import ru.dip.ui.table.ktable.model.DipTableModel;
import ru.dip.ui.table.ktable.model.IDipTableElement;
import ru.dip.ui.table.ktable.model.ITableNode;
import ru.dip.ui.table.ktable.model.TableNode;
import ru.dip.ui.table.table.TableModel;

public class IntoFolderInteractor {
	
	private KTableComposite fTableComposite;
	
	public IntoFolderInteractor(KTableComposite tableComposite) {
		fTableComposite = tableComposite;		
	}
	
	public MoveResult doIntoFolder() {
		if (selector().isEmpty()) {
			return null;
		} else if (selector().isOneSelected()) {
			return doIntoFodlerOneElement();
		} else {
			return doIntoFolderManyElements();
		}
	}

	private MoveResult doIntoFodlerOneElement() {
		IDipTableElement sourceElement = selector().getLastSelectObject();
		IDipDocumentElement sourceReq = selector().getSelectedOneDipDocElement();
		if (sourceReq == null) {
			return null;
		}
		IDipParent targetDipParent = getIntoFolder();
		if (targetDipParent == null || targetDipParent.equals(sourceReq.parent())) {
			return null;
		}
		TableNode targetNode = tableModel().findNode(targetDipParent);
		if (targetNode == null) {
			return null;
		}
		return doIntoFodlerOneElement(sourceElement, sourceReq, targetNode, targetDipParent);
	}

	public MoveResult doIntoFodlerOneElement(IDipTableElement sourceElement, IDipDocumentElement sourceReq, TableNode targetNode,
			IDipParent targetDipParent) {
		MoveResult result = null;
		int index = DipTableUtilities.getIndex(sourceReq);

		try {
			IDipTableElement movedElement = moveIntoFolder(targetDipParent, sourceElement, sourceReq, false, targetNode);
			result = new MoveResult(movedElement.dipResourceElement().name(),
					DipUtilities.relativeProjectID(sourceReq.parent()), DipUtilities.relativeProjectID(targetDipParent),
					index);
		} catch (CopyDIPException | DeleteDIPException e) {
			e.printStackTrace();
			MessageDialog.openError(getShell(), Messages.KTableComposite_DropErrorTitle, e.getMessage());
		}		
		updateAfterIntoFolder(sourceElement.parent(), targetNode, sourceReq.name());
		return result;
	}

	public void updateAfterIntoFolder(ITableNode sourceNode, ITableNode targetNode, String movedElementName) {
		fTableComposite.editor().updater().updateTwoFolder(sourceNode, targetNode);
		// select
		IDipTableElement newTableElement = targetNode.find(movedElementName);
		if (newTableElement != null) {
			selector().setTableElementSelection(newTableElement);
			selector().setTopItemElement(newTableElement);
		}
	}
	
	/**
	 * Перенести в папку с определенным индексом (используется при отмене)
	 */
	public IDipTableElement moveIntoFolder(IDipParent targetDipParent, IDipTableElement sourceElement, IDipDocumentElement sourcedReq,
			boolean needReserved, ITableNode targetNode, int index) throws CopyDIPException, DeleteDIPException {
		if (index < 0) {
			DipUtilities.copyElement(targetDipParent, sourcedReq, getShell());
		} else {
			DipUtilities.copyElement(targetDipParent, sourcedReq, index, getShell());
		}
		DipUtilities.deleteElement(sourcedReq, needReserved, getShell());
		LinkInteractor.instance().updateLinks(sourcedReq, targetDipParent.getChild(sourcedReq.name()));
		// table move
		if (sourceElement instanceof TableNode) {
			IDipParent parent = (IDipParent) targetDipParent.getChild(sourcedReq.name());
			ITableNode newNode = targetNode.addNewFolderToBegin(parent);
			sourceElement.delete();
			return newNode;
		} else {
			DipUnit unit = (DipUnit) targetDipParent.getChild(sourcedReq.name());
			IDipTableElement newElement = targetNode.addNewUnitToStart(unit);
			sourceElement.delete();
			return newElement;
		}
	}
	
	public IDipTableElement moveIntoFolder(IDipParent targetDipParent, IDipTableElement sourceElement, IDipDocumentElement sourcedReq,
			boolean needReserved, ITableNode targetNode) throws CopyDIPException, DeleteDIPException {
		return moveIntoFolder(targetDipParent, sourceElement, sourcedReq, needReserved, targetNode, -1);
	}

	private MoveResult doIntoFolderManyElements() {
		IDipParent targetDipParent = getIntoFolder();
		if (targetDipParent == null) {
			return null;
		}
		TableNode targetNode = tableModel().findNode(targetDipParent);
		ITableNode sourceNode = selector().getLastSelectObject().parent();
		IDipParent sourceDipParent = sourceNode.dipDocElement();
		IDipDocumentElement[] sourceElements = selector().getArraySelectedElements();

		boolean needReserved = false;
		if (!DipCorePlugin.isDisableReservation()) {
			needReserved = MessageDialog.openQuestion(getShell(), Messages.KTableComposite_MoveElementsQuestionTitle,
					Messages.KTableComposite_MovedMessage);
		}

		return doIntoFolderManyElements(sourceElements, sourceNode, sourceDipParent, targetDipParent, targetNode,
				needReserved);
	}

	public MoveResult doIntoFolderManyElements(IDipDocumentElement[] sourceElements, ITableNode sourceNode,
			IDipParent sourceDipParent, IDipParent targetDipParent, ITableNode targetNode, boolean needReserved) {
		List<IDipTableElement> selectElements = new ArrayList<>();
		List<MoveResult> moveResults = new ArrayList<>();

		for (int i = sourceElements.length - 1; i >= 0; i--) {
			try {
				IDipDocumentElement sourceReq = sourceElements[i];
				int index = DipTableUtilities.getIndex(sourceReq);
				IDipTableElement sourceElement = sourceNode.find(sourceReq.name());
				if (sourceElement == null) {
					return null;
				}
				IDipTableElement newElement = moveIntoFolder(targetDipParent, sourceElement, sourceReq, needReserved,
						targetNode);
				selectElements.add(newElement);
				moveResults.add(new MoveResult(newElement.dipResourceElement().name(), null, null, index));

			} catch (CopyDIPException | DeleteDIPException e) {
				e.printStackTrace();
				MessageDialog.openError(getShell(), Messages.KTableComposite_DropErrorTitle, e.getMessage());
				break;
			}
		}
		updateAfterIntoFolder(sourceNode, targetNode, selectElements);
		return new MoveResult(moveResults, DipUtilities.relativeProjectID(sourceDipParent),
				DipUtilities.relativeProjectID(targetDipParent));

	}

	public void updateAfterIntoFolder(ITableNode sourceNode, ITableNode targetNode, List<IDipTableElement> selectElements) {
		fTableComposite.editor().updater().updateTwoFolder(sourceNode, targetNode);
		if (!targetNode.expand()) {
			fTableComposite.setNodeExpand(targetNode, !targetNode.expand());
		}
		if (!selectElements.isEmpty()) {
			selector().setManyTableElements(selectElements);
			selector().setTopItemElement(selectElements.get(0));
		}
	}

	private IDipParent getIntoFolder() {
		SelectIntoFodlerDialog dialog = new SelectIntoFodlerDialog(getShell(), model().getContainer(),
				selector().getSelectedElements());
		IDipParent startParent = selector().first().parent();
		if (dialog.open() == Window.OK) {
			IDipParent targetParent = dialog.getIntoFolder();
			if (targetParent == null || targetParent.equals(startParent)) {
				return null;
			}
			return targetParent;
		}
		return null;
	}

	
	// =========================
	// utitlites & getters

	private KDipTableSelector selector() {
		return fTableComposite.selector();
	}

	private Shell getShell() {
		return fTableComposite.getShell();
	}

	private DipTableModel tableModel() {
		return fTableComposite.tableModel();
	}

	private TableModel model() {
		return fTableComposite.model();
	}

}
