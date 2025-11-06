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
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import ru.dip.core.exception.DIPException;
import ru.dip.core.model.DipUnit;
import ru.dip.core.model.interfaces.IDipElement;
import ru.dip.core.model.interfaces.IDipParent;
import ru.dip.core.model.interfaces.IDipDocumentElement;
import ru.dip.core.unit.TablePresentation;
import ru.dip.core.unit.md.MarkDownPresentation;
import ru.dip.core.utilities.DipUtilities;
import ru.dip.core.utilities.tmp.MDJoinResult;
import ru.dip.core.utilities.tmp.TmpElement;
import ru.dip.ui.Messages;
import ru.dip.ui.table.ktable.KTableComposite;
import ru.dip.ui.table.ktable.actions.DocumentAction;
import ru.dip.ui.table.ktable.actions.undo.CancelledDocumentAction;
import ru.dip.ui.table.ktable.model.TableNode;

public class MdJoinAction extends DocumentAction implements CancelledDocumentAction  {
	
	private static final String ACTION_NAME = Messages.MdJoinAction_ActionName;
	
	private MDJoinResult fResult;
	private boolean fUndo = false;
	
	public MdJoinAction(KTableComposite tableComposite) {
		super(tableComposite);
		setText(ACTION_NAME);
	}
	
	public MdJoinAction(MdJoinAction original) {
		super(original.fTableComposite);
		fResult = original.fResult;		
	}
	
	public MdJoinAction copy() {
		return new MdJoinAction(this);
	} 
	
	@Override
	public void run() {		
		fResult = fTableComposite.joinMarkdown();
		if (fResult != null) {
			fTableComposite.actionStack().pushUndoAction(copy());
		}
	}
	
	@Override
	public void undo() throws Exception {
		Optional<IDipElement> parentElementOpt = DipUtilities.findDipElementInProject(fResult.getParentId(), fTableComposite.dipProject());			
		if (parentElementOpt.isEmpty()) {
			throw new DIPException(Messages.MdJoinAction_MdJoinUndoException);
		}
		IDipParent dipParent = (IDipParent) parentElementOpt.get();						
		IDipDocumentElement newElement = (IDipDocumentElement) dipParent.getChild(fResult.getNewName());
		if (newElement == null) {
			throw new DIPException(Messages.MdJoinAction_MdJoinUndoException); //$NON-NLS-1$
		}
		
		// удаляем новый элемент
		if (!fUndo) {
			TmpElement newTmpElement = DipUtilities.deleteElement(newElement, false, fTableComposite.getShell());
			fResult.setNewTmpElement(newTmpElement);
		} else {
			DipUtilities.deleteElementsWithoutTmp(new IDipElement[] {newElement}, false, fTableComposite.getShell());
		}
			
		// восстанавливаем старые
		List<IDipDocumentElement> forSelecting = new ArrayList<>();		
		List<TmpElement> recoverTmpElements = fResult.getOldTmpElements();
		
		for (int i = recoverTmpElements.size() - 1; i>= 0; i--) {
			TmpElement tmpElement = recoverTmpElements.get(i);
			forSelecting.add(tmpElement.recoveryDipDocElement());			

		}
		// обновить
		Optional<TableNode> nodeOpt =  fTableComposite.tableModel().findNodeByName(dipParent);
		if (nodeOpt.isEmpty()) {
			throw new DIPException(Messages.MdJoinAction_MdExtractUndoException);
		}				
		fTableComposite.editor().updater().updateParent(nodeOpt.get().dipDocElement());
		// выделить
		fTableComposite.selector().selectManyDipDocElementss(forSelecting);		
	}

	@Override
	public void redo() throws Exception {
		Optional<IDipElement> parentElementOpt = DipUtilities.findDipElementInProject(fResult.getParentId(), fTableComposite.dipProject());			
		if (parentElementOpt.isEmpty()) {
			throw new DIPException(Messages.MdJoinAction_MdJoinRedoException);
		}
		IDipParent dipParent = (IDipParent) parentElementOpt.get();
		// удаляем старые
		for(TmpElement element: fResult.getOldTmpElements()) {
			IDipElement dipElement = dipParent.getChild(element.getName());
			if (dipElement == null) {
				throw new DIPException(Messages.MdJoinAction_MdJoinRedoException); //$NON-NLS-1$
			}
			DipUtilities.deleteElementsWithoutTmp(new IDipElement[] {dipElement}, false, fTableComposite.getShell());
		}
		// создаем новый
		IDipDocumentElement req = fResult.getNewTmpElement().recoveryDipDocElement();
		// обновить
		Optional<TableNode> nodeOpt =  fTableComposite.tableModel().findNodeByName(dipParent);
		if (nodeOpt.isEmpty()) {
			throw new DIPException(Messages.MdJoinAction_MdExtractUndoException); //$NON-NLS-1$
		}				
		fTableComposite.editor().updater().updateParent(nodeOpt.get().dipDocElement());
		// выделить
		fTableComposite.refreshTable();
		fTableComposite.selector().setSelection(req);		
	}

	@Override
	public void enableSeveralSelection() {
		Set<IDipDocumentElement> selectionObjs = fTableComposite.selector().getSelectedElements();	
		setEnabled(isAllMdUnits(selectionObjs));
	}
	
	private boolean isAllMdUnits(Collection<IDipDocumentElement> objs) {
		for (IDipDocumentElement obj: objs) {
			if (obj instanceof DipUnit) {
				TablePresentation  tablePresentation = ((DipUnit) obj).getUnitPresentation().getPresentation();
				boolean isMd =  tablePresentation instanceof MarkDownPresentation;
				if (!isMd) {
					return false;
				}
			} else {
				return false;
			}
		}
		return true;
	}
}
