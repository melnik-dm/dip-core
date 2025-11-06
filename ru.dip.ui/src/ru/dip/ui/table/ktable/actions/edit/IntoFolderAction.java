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
package ru.dip.ui.table.ktable.actions.edit;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Display;

import ru.dip.core.exception.DIPException;
import ru.dip.core.model.IncludeFolder;
import ru.dip.core.model.interfaces.IDipDocumentElement;
import ru.dip.core.model.interfaces.IDipElement;
import ru.dip.core.model.interfaces.IDipParent;
import ru.dip.core.utilities.DipTableUtilities;
import ru.dip.core.utilities.DipUtilities;
import ru.dip.core.utilities.WorkbenchUtitlities;
import ru.dip.core.utilities.tmp.MoveResult;
import ru.dip.ui.Messages;
import ru.dip.ui.table.ktable.KTableComposite;
import ru.dip.ui.table.ktable.actions.DocumentAction;
import ru.dip.ui.table.ktable.actions.undo.CancelledDocumentAction;
import ru.dip.ui.table.ktable.model.IDipTableElement;
import ru.dip.ui.table.ktable.model.TableNode;

public class IntoFolderAction extends DocumentAction implements CancelledDocumentAction {

	private MoveResult fResult;
	
	public IntoFolderAction(KTableComposite tableComposite) {
		super(tableComposite);
		setText(Messages.IntoFolderAction_Name);
	}
	
	public IntoFolderAction(IntoFolderAction original) {
		super(original.fTableComposite);
		fResult = original.fResult;
	}
	
	public IntoFolderAction copy() {
		return new IntoFolderAction(this);
	}
	
	@Override
	public void run() {
		doAction();
		if (fResult != null) {			
			fTableComposite.actionStack().pushUndoAction(copy());
		} 
	}
	
	private void doAction() {
		fResult = fTableComposite.doIntoFolder();
	}
		
	@Override
	public void undo() throws IOException, InvocationTargetException, InterruptedException {		
		WorkbenchUtitlities.runWithCursorBusy(new IRunnableWithProgress() {
			
			@Override
			public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
				Display.getDefault().asyncExec(() -> {
					
						try {
							doUndo();
						} catch (IOException e) {
							e.printStackTrace();
						}				
				});
			}
		});
	}
	
	//===========================
	// undo
	
	private void doUndo() throws IOException {
		if (fResult.isOneElement()) {
			undoOneElement();
		} else {
			undoManyElements();
		}
	}

	private void undoOneElement() throws DIPException {
		IDipDocumentElement movedReq = getMovedDipDocElement();
		IDipParent oldSourceDipParent = getOldDipParent();
		TableNode parentNode = getParentNode(movedReq, oldSourceDipParent);
		IDipTableElement movedElement = getMovedElement(movedReq);

		fTableComposite.getIntoFolderInteractor().moveIntoFolder(oldSourceDipParent, movedElement, movedReq, false, parentNode,
				fResult.getOldIndex());
		// update
		fTableComposite.getIntoFolderInteractor().updateAfterIntoFolder(movedElement.parent(), 
					parentNode, movedReq.name());		
	}
	
	private IDipDocumentElement getMovedDipDocElement() throws DIPException {
		Optional<IDipElement> newParentOpt = DipUtilities.findDipElementInProject(fResult.getNewParentId(),
				fTableComposite.dipProject());
		if (newParentOpt.isEmpty() || !(newParentOpt.get() instanceof IDipParent)) {
			throw new DIPException("Ошибка отмены переноса объекта " + fResult.getNewParentId());
		}
		IDipParent newDipParent = (IDipParent)newParentOpt.get();		
		return (IDipDocumentElement) newDipParent.getChild(fResult.getName());
	}
	
	private IDipParent getOldDipParent() throws DIPException {
		Optional<IDipElement> elementOpt = DipUtilities.findDipElementInProject(fResult.getOldParentId(),
				fTableComposite.dipProject());
		if (elementOpt.isEmpty() || !(elementOpt.get() instanceof IDipParent)) {
			throw new DIPException("Ошибка отмены переноса объекта " + fResult.getOldParentId());
		}
		return (IDipParent) elementOpt.get();
	}
	
	private TableNode getParentNode(IDipDocumentElement dipDocElement, IDipParent oldSourceDipParent) throws DIPException {
		Optional<TableNode> parentNodeOpt = fTableComposite.tableModel().findNodeByName(oldSourceDipParent);
		if (parentNodeOpt.isEmpty()) {
			throw new DIPException("Ошибка отмены переноса объекта " + dipDocElement);
		}
		return parentNodeOpt.get();
	}
	
	private IDipTableElement getMovedElement(IDipDocumentElement movedDipDocElement) throws DIPException {
		Optional<IDipTableElement> recoveredElement = fTableComposite.tableModel().findElementByName(movedDipDocElement);
		if (recoveredElement.isEmpty()) {
			throw new DIPException("Ошибка отмены переноса объекта " + movedDipDocElement);
		}
		return recoveredElement.get();
	}
	
	
	private void undoManyElements() throws DIPException {
		Optional<IDipElement> newParentOpt = DipUtilities.findDipElementInProject(fResult.getNewParentId(),
				fTableComposite.dipProject());
		if (newParentOpt.isEmpty() || !(newParentOpt.get() instanceof IDipParent)) {
			throw new DIPException("Ошибка отмены переноса объекта " + fResult.getNewParentId());
		}
		IDipParent newDipParent = (IDipParent)newParentOpt.get();		
		Optional<IDipElement> elementOpt = DipUtilities.findDipElementInProject(fResult.getOldParentId(),
				fTableComposite.dipProject());
		if (elementOpt.isEmpty() || !(elementOpt.get() instanceof IDipParent)) {
			throw new DIPException("Ошибка отмены переноса объекта " + fResult.getOldParentId());
		}
		IDipParent oldSourceDipParent = (IDipParent) elementOpt.get();
		Optional<TableNode> parentNodeOpt = fTableComposite.tableModel().findNodeByName(oldSourceDipParent);
		if (parentNodeOpt.isEmpty()) {
			throw new DIPException("Ошибка отмены переноса объектов");
		}
		List<IDipTableElement> elementsForSelection = new ArrayList<>();
		Optional<IDipTableElement> recoveredElement  = null;
		for (int i = fResult.getElementResults().size() - 1; i >= 0; i--) {
			MoveResult result = fResult.getElementResults().get(i);			
			IDipDocumentElement recoveredReq = (IDipDocumentElement)newDipParent.getChild(result.getName());
			recoveredElement = fTableComposite.tableModel().findElementByName(recoveredReq);
			if (recoveredElement.isEmpty()) {
				throw new DIPException("Ошибка отмены переноса объектов");
			}
			IDipTableElement movedElement = fTableComposite.getIntoFolderInteractor().moveIntoFolder(oldSourceDipParent, recoveredElement.get(),
					recoveredReq, false, parentNodeOpt.get(), result.getOldIndex());
			elementsForSelection.add(movedElement);
		}
		if (recoveredElement != null && parentNodeOpt.isPresent()) {
			fTableComposite.getIntoFolderInteractor().updateAfterIntoFolder(recoveredElement.get().parent(), parentNodeOpt.get(), elementsForSelection);
		}
	}
	
	//============================
	// redo

	@Override
	public void redo() throws Exception {
		if (fResult.isOneElement()) {
			redoOneElement();
		} else {
			redoManyElements();
		}		
	}
	
	private void redoOneElement() throws DIPException {		
		Optional<IDipElement> oldParentOpt = DipUtilities.findDipElementInProject(fResult.getOldParentId(), fTableComposite.dipProject());
		Optional<IDipElement> newParentOpt = DipUtilities.findDipElementInProject(fResult.getNewParentId(), fTableComposite.dipProject());		
		
		if (oldParentOpt.isEmpty() || newParentOpt.isEmpty()) {
			throw new  DIPException("IntoFolder Redo Error");
		}
		
		IDipParent oldParent = (IDipParent) oldParentOpt.get();
		IDipParent newParent = (IDipParent) newParentOpt.get();		
		Optional<TableNode> targetNode = fTableComposite.tableModel().findNodeByName(newParent);
		
		if (targetNode.isEmpty()) {
			throw new  DIPException("IntoFolder Redo Error");
		}
		
		String elementName = fResult.getName();
		IDipDocumentElement sourceReq = (IDipDocumentElement) oldParent.getChild(elementName);
		Optional<IDipTableElement> sourceElement = fTableComposite.tableModel().findElementByName(sourceReq);
		
		if (sourceElement.isEmpty()) {
			throw new  DIPException("IntoFolder Redo Error");
		}			
		fTableComposite.getIntoFolderInteractor().doIntoFodlerOneElement(sourceElement.get(), sourceReq, targetNode.get(), newParent);
	}

	private void redoManyElements() throws DIPException {
		Optional<IDipElement> oldParentOpt = DipUtilities.findDipElementInProject(fResult.getOldParentId(), fTableComposite.dipProject());
		Optional<IDipElement> newParentOpt = DipUtilities.findDipElementInProject(fResult.getNewParentId(), fTableComposite.dipProject());		
		
		if (oldParentOpt.isEmpty() || newParentOpt.isEmpty()) {
			throw new  DIPException("IntoFolder Redo Error");
		}
		
		IDipParent oldParent = (IDipParent) oldParentOpt.get();
		IDipParent newParent = (IDipParent) newParentOpt.get();		
			
		Optional<TableNode> targetNode = fTableComposite.tableModel().findNodeByName(newParent);
		Optional<TableNode> sourceNode = fTableComposite.tableModel().findNodeByName(oldParent);

		if (targetNode.isEmpty() || sourceNode.isEmpty()) {
			throw new  DIPException("IntoFolder Redo Error");
		}
		
		IDipDocumentElement[] sourceElements = fResult.getElementResults().stream()
				.map(MoveResult::getName)
				.map(oldParent::getChild)
				.toArray(IDipDocumentElement[]::new);

		fTableComposite.getIntoFolderInteractor().doIntoFolderManyElements(sourceElements, sourceNode.get(), oldParent,
				newParent, targetNode.get(), false);		
	}


	//==========================
	// when enabled
	
	@Override
	public void enableNullSelection() {
		setEnabled(false);
	}

	@Override
	public void enableOneSelection(IDipDocumentElement selectedDipDocElement) {
		boolean readOnly = selectedDipDocElement.isReadOnly();												
		if (fTableComposite.model().isParentHeader(selectedDipDocElement)){
			setEnabled(false);
			return;
		}
		if (selectedDipDocElement instanceof IncludeFolder && !selectedDipDocElement.parent().isReadOnly()) {
			setEnabled(true);
			return;
		}
		setEnabled(!readOnly);
	}

	@Override
	public void enableSeveralSelection() {
		// если выделен один из заголовков Section			
		IDipDocumentElement first = fTableComposite.selector().first();			
		if (first == null || fTableComposite.model().isTable(first) || fTableComposite.model().isParentHeader(first)) {
			setEnabled(false);
			return;
		}
		// if readonly
		if (fTableComposite.selector().hasReadOnlyObjects()) {
			setEnabled(false);
			return;
		}		
		setEnabled(DipTableUtilities.canIntoFolder(fTableComposite.selector().getSelectedElements()));
	}

}
