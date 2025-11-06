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

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import ru.dip.core.exception.CopyDIPException;
import ru.dip.core.exception.DIPException;
import ru.dip.core.exception.DeleteDIPException;
import ru.dip.core.exception.RecoveryException;
import ru.dip.core.exception.TmpCopyException;
import ru.dip.core.model.DipFolder;
import ru.dip.core.model.DipProject;
import ru.dip.core.model.interfaces.IParent;
import ru.dip.core.model.interfaces.IDipElement;
import ru.dip.core.model.interfaces.IDipParent;
import ru.dip.core.model.interfaces.IDipDocumentElement;
import ru.dip.core.utilities.DipUtilities;
import ru.dip.core.utilities.WorkbenchUtitlities;
import ru.dip.core.utilities.tmp.DeleteResultOperation;
import ru.dip.core.utilities.tmp.ExtractResult;
import ru.dip.core.utilities.tmp.MoveResult;
import ru.dip.core.utilities.tmp.ResultOperation;
import ru.dip.core.utilities.tmp.TmpElement;
import ru.dip.ui.dialog.DeleteDialog;
import ru.dip.ui.table.ktable.KDipTableSelector;
import ru.dip.ui.table.ktable.KTableComposite;
import ru.dip.ui.table.ktable.model.DipTableModel;
import ru.dip.ui.table.ktable.model.IDipTableElement;
import ru.dip.ui.table.ktable.model.TableNode;
import ru.dip.ui.table.table.TableModel;

public class DeleteFileInteractor {

	private KTableComposite fTableComposite;

	public DeleteFileInteractor(KTableComposite tableComposite) {
		fTableComposite = tableComposite;
	}

	public boolean canDelete() {
		if (selector().isEmpty()) {
			return false;
		}
		for (IDipDocumentElement req : selector().getSelectedElements()) {
			if (!req.canDelete()) {
				return false;
			}
		}
		IDipDocumentElement req = selector().first();
		return model().isChild(req);
	}

	public ResultOperation doDeleteOPeration() throws DeleteDIPException, CopyDIPException {
		if (selector().isEmpty()) {
			return null;
		}
		Deleter deleter = new Deleter();
		deleter.getDipDocElementsFromSelection();
		if (deleter.openDialog()) {
			if (deleter.fExtract) {
				deleter.prepareExtract();
				deleter.doExtract();
				deleter.updateTableAfterExtract();
			} else {
				deleter.doDelete();
				fTableComposite.editor().updater().updateAfterDelete(deleter.fDipDocElements);
			}
		}
		return deleter.fResult;
	}

	public void deleteWithoutUiForDipDocElements(List<IDipDocumentElement> dipDocElements) throws DeleteDIPException {
		IDipDocumentElement[] reqs = dipDocElements.stream().toArray(IDipDocumentElement[]::new);
		try {
			Deleter deleter = new Deleter(reqs);
			deleter.doDeleteWithoutTmp();
			fTableComposite.editor().updater().updateAfterDelete(deleter.fDipDocElements);
		} catch (TmpCopyException | DeleteDIPException e) {
			e.printStackTrace();
			throw new DeleteDIPException("Redo delete error. " + e.getMessage());
		}
	}

	public void deleteElementWithoutUI(IDipDocumentElement dipDocElement) throws DIPException {
		try {
			Deleter deleter = new Deleter(new IDipDocumentElement[] { dipDocElement });
			deleter.doDeleteWithoutTmp();
			fTableComposite.editor().updater().updateAfterDelete(deleter.fDipDocElements);
		} catch (TmpCopyException | DeleteDIPException e) {
			e.printStackTrace();
			throw new DeleteDIPException("Redo delete error. " + e.getMessage());
		}
	}

	// ===============================
	// undo

	public void undo(ResultOperation result) throws IOException, InvocationTargetException, InterruptedException {
		WorkbenchUtitlities.runWithCursorBusy(new IRunnableWithProgress() {

			@Override
			public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
				Display.getDefault().asyncExec(() -> {
					try {
						doUndo(result);
					} catch (IOException e) {
						e.printStackTrace();
						throw new RuntimeException("Do Undo Exception");
					}
				});
			}
		});
	}

	private void doUndo(ResultOperation result) throws IOException {
		if (result instanceof DeleteResultOperation) {
			DeleteRecoverer recover = new DeleteRecoverer();
			recover.undoDelete((DeleteResultOperation) result);
		} else if (result instanceof ExtractResult) {
			ExtractRecoverer recover = new ExtractRecoverer();
			recover.recoveryExtractedNode((ExtractResult) result);
		} else {
			throw new DeleteDIPException("Undo Delete. Illegal ResultOperation type");
		}
	}

	// ================================
	// redo

	public void redoDelete(ResultOperation result) throws InvocationTargetException, InterruptedException {
		WorkbenchUtitlities.runWithCursorBusy(new IRunnableWithProgress() {

			@Override
			public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
				Display.getDefault().asyncExec(() -> {
					try {
						doRedo(result);
					} catch (DeleteDIPException | CopyDIPException e) {
						e.printStackTrace();
					}
				});
			}
		});
	}

	private void doRedo(ResultOperation result) throws DeleteDIPException, CopyDIPException {
		if (result instanceof DeleteResultOperation) {
			doRedoDelete((DeleteResultOperation) result);
		} else if (result instanceof ExtractResult) {
			doRedoExtract((ExtractResult) result);
		} else {
			throw new DeleteDIPException("Undo Delete. Illegal ResultOperation type");
		}
	}

	private void doRedoDelete(DeleteResultOperation result) throws DeleteDIPException {
		try {
			Deleter deleter = new Deleter(result);
			deleter.doDeleteWithoutTmp();
			fTableComposite.editor().updater().updateAfterDelete(deleter.fDipDocElements);
		} catch (TmpCopyException | DeleteDIPException e) {
			e.printStackTrace();
			throw new DeleteDIPException("Redo delete error. " + e.getMessage());
		}
	}

	private void doRedoExtract(ExtractResult result) throws DeleteDIPException, CopyDIPException {
		Optional<IDipElement> dipElementOpt = DipUtilities.findDipElementInProject(result.getDeltedFolderId(),
				dipProject());
		if (dipElementOpt.isEmpty() || !(dipElementOpt.get() instanceof IDipParent)) {
			throw new DeleteDIPException("Not find element from result.");
		}
		Deleter deleter = new Deleter(new IDipDocumentElement[] { (IDipDocumentElement) dipElementOpt.get() });
		deleter.prepareExtract();
		deleter.doExtractWithoutTmp();
		deleter.updateTableAfterExtract();
	}

	private class DeleteRecoverer {

		private List<IDipDocumentElement> fDDEForSelecting = new ArrayList<>();

		private void undoDelete(DeleteResultOperation result) throws IOException {
			List<TmpElement> tmpElements = result.getTmpElements();
			Collections.reverse(tmpElements);
			for (TmpElement tmpElement : tmpElements) {
				IDipDocumentElement recovery = tmpElement.recoveryDipDocElement();
				if (recovery == null) {
					throw new RecoveryException("Ошибка востановления элемента: " + tmpElement);
				}
				fDDEForSelecting.add(recovery);
			}

			fTableComposite.editor().updater().updateNewObjects(fDDEForSelecting);
			// select
			fTableComposite.selector().selectManyDipDocElementss(fDDEForSelecting);
		}
	}

	private class ExtractRecoverer {

		private void recoveryExtractedNode(ExtractResult result) throws IOException {
			// востановили папку
			IDipParent dipParent = (IDipParent) result.getTmpFolder().recoveryDipDocElement();
			// переносим файлы
			List<MoveResult> moveResults = result.getMovedResults();
			for (MoveResult moveResult : moveResults) {
				IDipElement movedElement = dipParent.parent().getChild(moveResult.getName());
				if (movedElement instanceof IDipDocumentElement) {
					DipUtilities.moveElement((IDipDocumentElement) movedElement, dipParent, false,
							((IDipDocumentElement) movedElement).getIndex(), WorkbenchUtitlities.getShell());
				} else {
					throw new RecoveryException("Ошибка востановления элемента: " + moveResult);
				}
			}
			// update
			fTableComposite.editor().updater().updateFolderWithParent(dipParent);		
			// select
			TableNode recoveredNode = tableModel().findNode(dipParent);
			fTableComposite.selector().setSelection(recoveredNode);
		}
	}

	private class Deleter {

		// input
		private IDipDocumentElement[] fDipDocElements;
		// from dialog
		private boolean fReserve;
		private boolean fExtract;
		private boolean fDeleteProjectContent; // удалить ли содержимое проектов (при удалении проектов)
		// for extract (чтобы потом выделить)
		private IDipParent fTargetParent;
		private List<IDipDocumentElement> fMovedDipDocElements;
		// result
		private ResultOperation fResult;

		private Deleter() {
		}

		private Deleter(IDipDocumentElement[] dipDocElements) {
			fDipDocElements = dipDocElements;
		}

		private Deleter(DeleteResultOperation result) throws DeleteDIPException {
			getDipDocElementsFromResult(result);
		}

		private void getDipDocElementsFromResult(DeleteResultOperation result) throws DeleteDIPException {
			List<IDipDocumentElement> dipDocElementsForDelete = new ArrayList<IDipDocumentElement>();
			for (TmpElement element : result.getTmpElements()) {
				IDipElement dipElement = DipUtilities.findElement(element.getId());
				if (dipElement == null || !(dipElement instanceof IDipParent)) {
					throw new DeleteDIPException("Not find element from result.");
				}
				IDipParent dipParent = (IDipParent) dipElement;
				IDipDocumentElement dipDocElement = (IDipDocumentElement) dipParent.getChild(element.getName());
				if (dipDocElement == null) {
					throw new DeleteDIPException("Not find element from result.");
				}
				dipDocElementsForDelete.add(dipDocElement);
			}
			fDipDocElements = dipDocElementsForDelete.stream().toArray(IDipDocumentElement[]::new);
		}

		/*
		 * Получаем выделенные элементы для удаления ( с учетом вложенности)
		 */
		private void getDipDocElementsFromSelection() {
			List<IDipDocumentElement> selectedElements = selector().getSelectedObjects().stream()
					.map(IDipTableElement::dipResourceElement).distinct().sorted(IDipDocumentElement.indexComparator)
					.collect(Collectors.toList());

			List<IDipDocumentElement> resultElements = new ArrayList<>();
			IParent previousElement = null;
			for (IDipDocumentElement requrement : selectedElements) {
				if (previousElement == null) {
					if (requrement instanceof IDipParent) {
						previousElement = (IDipParent) requrement;
					}
					resultElements.add(requrement);
				} else if (requrement.hasParent(previousElement)) {
					continue;
				} else {
					if (requrement instanceof IDipParent) {
						previousElement = (IDipParent) requrement;
					}
					resultElements.add(requrement);
				}
			}
			fDipDocElements = resultElements.stream().toArray(IDipDocumentElement[]::new);
		}

		private boolean openDialog() {
			DeleteDialog dialog = new DeleteDialog(getShell(), fDipDocElements);
			if (dialog.open() == DeleteDialog.OK) {
				fReserve = dialog.isReserve();
				fExtract = dialog.isExtract();
				fDeleteProjectContent = dialog.isDeleteProjectContent();
				return true;
			}
			return false;
		}

		private void doDelete() throws TmpCopyException, DeleteDIPException {
			fResult = DipUtilities.deleteElements(fDipDocElements, fReserve, fDeleteProjectContent, getShell());
		}

		private void doDeleteWithoutTmp() throws TmpCopyException, DeleteDIPException {
			DipUtilities.deleteElementsWithoutTmp(fDipDocElements, fReserve, getShell());
		}

		private void prepareExtract() {
			fTargetParent = fDipDocElements[0].parent();
			fMovedDipDocElements = new ArrayList<>(((IDipParent) fDipDocElements[0]).getDipDocChildrenList());
		}

		private void doExtract() throws DeleteDIPException, CopyDIPException {
			IStatus status = DipUtilities.canExtract((DipFolder) fDipDocElements[0]);
			if (!status.isOK()) {
				throw new DeleteDIPException("Extract error." + status.getMessage());
			}
			fResult = DipUtilities.moveUp((DipFolder) fDipDocElements[0], fReserve, getShell());
		}

		private void doExtractWithoutTmp() throws DeleteDIPException, CopyDIPException {
			DipUtilities.moveUp((DipFolder) fDipDocElements[0], fReserve, getShell());
		}

		private void updateTableAfterExtract() throws DeleteDIPException {
			fTableComposite.editor().updater().updateParent(fTargetParent);
			List<IDipTableElement> forSelecting = tableModel().findElementsByName(fMovedDipDocElements);
			fTableComposite.selector().setManyTableElements(forSelecting);
		}
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

	private TableModel model() {
		return fTableComposite.model();
	}

}
