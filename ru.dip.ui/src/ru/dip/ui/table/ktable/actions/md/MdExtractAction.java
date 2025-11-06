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
package ru.dip.ui.table.ktable.actions.md;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import ru.dip.core.exception.DIPException;
import ru.dip.core.model.DipUnit;
import ru.dip.core.model.interfaces.IDipElement;
import ru.dip.core.model.interfaces.IDipParent;
import ru.dip.core.model.interfaces.IDipDocumentElement;
import ru.dip.core.unit.TablePresentation;
import ru.dip.core.unit.UnitPresentation;
import ru.dip.core.unit.md.MarkDownPresentation;
import ru.dip.core.utilities.DipUtilities;
import ru.dip.core.utilities.tmp.MdExtractResult;
import ru.dip.core.utilities.tmp.TmpElement;
import ru.dip.ui.Messages;
import ru.dip.ui.table.ktable.KTableComposite;
import ru.dip.ui.table.ktable.actions.DocumentAction;
import ru.dip.ui.table.ktable.actions.undo.CancelledDocumentAction;
import ru.dip.ui.table.ktable.model.TableNode;

public class MdExtractAction extends DocumentAction implements CancelledDocumentAction {
	
	private static final String ACTION_NAME = Messages.MdExtractAction_ActionName;
	
	protected MdExtractResult fExtractResult;
	private boolean fHasNewTmp = false; // если true - значит уже есть копия для redo
	
	public MdExtractAction(KTableComposite tableComposite) {
		super(tableComposite);
		setText(ACTION_NAME);
	}
	
	public MdExtractAction(MdExtractAction original) {
		super(original.fTableComposite);
		fExtractResult = original.fExtractResult;		
	}
	
	public MdExtractAction copy() {
		return new MdExtractAction(this);
	}
	
	@Override
	public void run() {
		doAction();
		if (fExtractResult != null) {
			fTableComposite.actionStack().pushUndoAction(copy());
		}
	}
	
	protected void doAction(){
		fExtractResult = fTableComposite.extractMarkdown();
	}
	

	@Override
	public void undo() throws Exception {
		TmpElement tmpElment = fExtractResult.getTmpElement();		
		IDipElement parentElement = DipUtilities.findElement(tmpElment.getId());
		if (parentElement == null) {
			throw new DIPException(Messages.MdExtractAction_MdExtractUndoException);
		}
		IDipParent dipParent = (IDipParent) parentElement;		
		IDipElement oldOriginal = dipParent.getChild(tmpElment.getName());
			
		// удалить созданные элементы
		List<TmpElement> newElementTmps = new ArrayList<>();		
		for (String newElementName:  fExtractResult.getCreatedElementNames()) {
			IDipElement element = dipParent.getChild(newElementName);
			if (!fHasNewTmp) {
				TmpElement newElementTmp = DipUtilities.deleteElement(element, false, fTableComposite.getShell());
				newElementTmps.add(newElementTmp);	
			} else {
				DipUtilities.deleteElementsWithoutTmp(new IDipElement[] {element}, false, fTableComposite.getShell());
			}					
		}
		
		// удалить новый оригинальный элемент (если есть)
		TmpElement undoOriginal = null;
		if (oldOriginal != null) {
			if (!fHasNewTmp) {
				undoOriginal = DipUtilities.deleteElement(oldOriginal, false, fTableComposite.getShell());
				fExtractResult.setNewOriginalTmpElement(undoOriginal);
			} else {
				DipUtilities.deleteElementsWithoutTmp(new IDipElement[] {oldOriginal}, false, fTableComposite.getShell());
			}
		}
		
		if (!fHasNewTmp) {
			fExtractResult.setNewTmpElements(newElementTmps);
		}
		
		// воостановить исходиный элемент
		IDipDocumentElement newReq = fExtractResult.getTmpElement().recoveryDipDocElement();
		
		// обновить
		Optional<TableNode> nodeOpt =  fTableComposite.tableModel().findNodeByName(dipParent);
		if (nodeOpt.isEmpty()) {
			throw new DIPException(Messages.MdExtractAction_MdExtractUndoException); //$NON-NLS-1$
		}		
		fTableComposite.editor().updater().updateParent(dipParent);
		// выделить
		fTableComposite.selector().setSelection(newReq);
		
		fHasNewTmp = true;
	}

	@Override
	public void redo() throws Exception {
		TmpElement tmpElment = fExtractResult.getTmpElement();		
		IDipElement parentElement = DipUtilities.findElement(tmpElment.getId());
		if (parentElement == null) {
			throw new DIPException(Messages.MdExtractAction_MdExtractUndoException); //$NON-NLS-1$
		}
		IDipParent dipParent = (IDipParent) parentElement;			
		IDipElement oldOriginal = dipParent.getChild(tmpElment.getName());

		// удалить старый оригинальный элемент (если есть)
		if (oldOriginal != null) {
			DipUtilities.deleteElementsWithoutTmp(new IDipElement[] {oldOriginal}, false, fTableComposite.getShell());
		}
		
		List<IDipDocumentElement> forSelecting = new ArrayList<>();
		// восстановить оригинальный элемент (если есть)
		TmpElement newOriginal = fExtractResult.getNewOriginalTmpElement();
		if (newOriginal != null) {
			forSelecting.add(newOriginal.recoveryDipDocElement());
		}
		// восстановить новые элементы
		for (TmpElement tmpElement: fExtractResult.getNewTmpElements()) {			
			forSelecting.add(tmpElement.recoveryDipDocElement());			
		}
	
		// обновить
		Optional<TableNode> nodeOpt =  fTableComposite.tableModel().findNodeByName(dipParent);
		if (nodeOpt.isEmpty()) {
			throw new DIPException(Messages.MdExtractAction_MdExtractUndoException); //$NON-NLS-1$
		}		
		fTableComposite.editor().updater().updateParent(dipParent);
		// выделить
		fTableComposite.selector().selectManyDipDocElementss(forSelecting);
	}
	

	@Override
	public void enableOneSelection(IDipDocumentElement selectedDipDocElement) {
		if (selectedDipDocElement instanceof DipUnit) {
			TablePresentation  tablePresentation = ((DipUnit) selectedDipDocElement).getUnitPresentation().getPresentation();
			boolean isMd =  tablePresentation instanceof MarkDownPresentation;
			setEnabled(isMd);			
		} else if (selectedDipDocElement instanceof UnitPresentation){
			TablePresentation  tablePresentation = ((UnitPresentation) selectedDipDocElement).getPresentation();
			boolean isMd =  tablePresentation instanceof MarkDownPresentation;
			setEnabled(isMd);
		} else {
			setEnabled(false);
		}
	}

}
