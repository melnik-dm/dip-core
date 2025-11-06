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
package ru.dip.ui.table.ktable;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;

import ru.dip.core.exception.CopyDIPException;
import ru.dip.core.exception.DeleteDIPException;
import ru.dip.core.model.DipUnit;
import ru.dip.core.model.interfaces.IDipParent;
import ru.dip.core.model.interfaces.IDipDocumentElement;
import ru.dip.core.utilities.ResourcesUtilities;
import ru.dip.ui.Messages;
import ru.dip.ui.table.ktable.model.DipTableModel;
import ru.dip.ui.table.ktable.model.IDipTableElement;
import ru.dip.ui.table.ktable.model.ITableNode;
import ru.dip.ui.table.ktable.model.TableNode;

public class MoveInteractor {
	
	private KTableComposite fTable;
	private IDipParent fTargetParent;
	private ITableNode fTargetNode;
	
	public MoveInteractor(KTableComposite table) {
		fTable = table;
	}

	//=================
	// move up
	
	/**
	 * Извлечь файлы на уровень выше 
	 */
	public void moveUp(TableNode tableNode) {	
		fTargetNode = tableNode.parent();
		if (fTargetNode == null) {
			return;
		}
		fTargetParent = fTargetNode.dipDocElement();
		List<IDipTableElement> forSelecting = doMoveIntoFolder(tableNode.allChildren());
		fTargetNode.delete(tableNode);	
		save();
		selectElements(forSelecting);
	}
	
	private List<IDipTableElement> doMoveIntoFolder(List<IDipTableElement> elements) {
		List<IDipTableElement> forSelecting = new ArrayList<>();
		for (int i = 0; i < elements.size(); i++) {
			try {
				IDipTableElement element = elements.get(i);
				if (element.isPresentation()) {
					IDipTableElement movedElement = doMoveIntoFolder(element);
					forSelecting.add(movedElement);
				}
			} catch (CopyDIPException | DeleteDIPException e) {
				e.printStackTrace();
				MessageDialog.openError(getShell(), Messages.KTableComposite_DropErrorTitle, e.getMessage());
				break;
			}
		}
		return forSelecting;
	}
	
	private IDipTableElement doMoveIntoFolder(IDipTableElement movedElement) throws CopyDIPException, DeleteDIPException {
		
		/*fTargetNode
		nodeOpt.get().updateChildren();
		fTable.updateNode(nodeOpt.get());*/
		
		//надо переделать на норм. обновление
		
		IDipDocumentElement movedDipDocElement = movedElement.dipDocElement();
		if (movedElement instanceof TableNode) {
			IDipParent targetParent = (IDipParent) fTargetParent.getChild(movedDipDocElement.dipName());
			ITableNode newNode = fTargetNode.addNewNeightborFolder(targetParent, movedElement.parent(), true);
			movedElement.delete();
			return newNode;
		} else {
			DipUnit unit = (DipUnit) fTargetParent.getChild(movedDipDocElement.dipName());			
			IDipTableElement newElement = fTargetNode.addNewUnitToEnd(unit);
			movedElement.delete();
			return newElement;
		}
	}
	
	//==================
	// utils
	

	private void save() {
		saveAndUpdateProject();
		tableModel().computeElements();	
	}
	
	private void saveAndUpdateProject() {
		ResourcesUtilities.updateProject(tableModel().getTableModel().resource());
		fTable.editor().doSave(null);
		ResourcesUtilities.updateProject(tableModel().getTableModel().resource());
	}

	private void selectElements(List<IDipTableElement> forSelecting) {		
		if (!fTargetNode.expand()) {
			fTable.setNodeExpand(fTargetNode, !fTargetNode.expand());
		}
		if (!forSelecting.isEmpty()) {
			selector().setManyTableElements(forSelecting);
			selector().setTopItemElement(forSelecting.get(0));
		}
	}
	
	//=================
	// getters
	
	private KDipTableSelector selector() {
		return fTable.selector();
	}
	
	private Shell getShell() {
		return fTable.getShell();
	}
	
	private DipTableModel tableModel() {
		return fTable.tableModel();
	}
	
}
