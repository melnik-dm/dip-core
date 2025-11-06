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
package ru.dip.ui.table.table;

import java.util.List;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;

import ru.dip.core.DipCorePlugin;
import ru.dip.core.exception.CopyDIPException;
import ru.dip.core.exception.DeleteDIPException;
import ru.dip.core.link.LinkInteractor;
import ru.dip.core.model.DipFolder;
import ru.dip.core.model.DipReservedFolder;
import ru.dip.core.model.DipUnit;
import ru.dip.core.model.interfaces.IParent;
import ru.dip.core.model.interfaces.IDipElement;
import ru.dip.core.model.interfaces.IDipParent;
import ru.dip.core.model.interfaces.IDipDocumentElement;
import ru.dip.core.utilities.DipUtilities;
import ru.dip.ui.Messages;

public class DropAction {

	public static final int OK = 0;
	public static final int NOT_ALLOWED = 1;
	public static final int ALREADY_EXISTS = 2;
	public static final int HAS_RESERVED = 3;
	
	private static final String MOVING_ERROR_TITLE = Messages.DropAction_MoveError;
	private static final String MOVING_TITLE = Messages.DropAction_MoveElement;
	private static final String NEED_RESERVE_QUESTION = Messages.DropAction_MoveWithReservationQuest;
	private static final String ALREADY_EXISTS_MESSAGE = Messages.DropAction_AlreadyExist;
	private static final String HAS_RESERVED_MESSAGE = Messages.DropAction_Rezerzed;
	
	private Shell fShell;
	private TableModelProvider fModelProvider;
	private IDipDocumentElement fMovedElement;
	private IDipDocumentElement fBeforeTargetElement;
	private IDipParent fTargetFolder;
	private boolean fOneParent = false;
	boolean fNeedReserved = false;
	private int fDropStatus;
		
	public DropAction(TableModelProvider modelProvider, Shell shell){
		fModelProvider = modelProvider;
		fShell = shell;
	}
	
	//=================================
	// can drop
	
	public boolean canDrop(IDipDocumentElement beforeTargetElement, IDipDocumentElement movedElement){
		fDropStatus = computeDropStatus(beforeTargetElement, movedElement);
		if (fDropStatus == NOT_ALLOWED){
			return false;
		} else {
			return true;
		}
	}
	
	public int computeDropStatus(IDipDocumentElement beforeTargetElement, IDipDocumentElement movedElement){
		fBeforeTargetElement = beforeTargetElement;
		fMovedElement = movedElement;
		if (fMovedElement instanceof DipUnit){		
			return canUnitDrop();		
		} else if (fMovedElement instanceof IDipParent){
			return canParentDrop();
		}
		return NOT_ALLOWED;
	}
	
	private int canUnitDrop(){
		if (fBeforeTargetElement == null){
			return canUnitDropIntoBeginning();
		}
		if (fBeforeTargetElement instanceof DipUnit){
			fTargetFolder = fBeforeTargetElement.parent();
			fOneParent = (fTargetFolder.equals(fMovedElement.parent()));
			if (fOneParent){
				return canUnitDropInsideFolder();
			} else {
				return canUnitDropIntoFolder();
			}
		}
		if (fBeforeTargetElement instanceof IDipParent){
			fTargetFolder = (IDipParent) fBeforeTargetElement;
			fOneParent =fTargetFolder.equals(fMovedElement.parent());
			if (fOneParent){
				return canUnitDropInsideFolder();
			} else {
				return canUnitDropIntoFolder();
			}		
		}
		return NOT_ALLOWED;
	}
	
	private int canUnitDropIntoBeginning(){
		fTargetFolder = fModelProvider.getModel();
		fOneParent = (fTargetFolder.equals(fMovedElement.parent()));
		if (fOneParent){
			return canDropInsideBeginFolder();
		} else {
			return canUnitDropIntoFolder();
		}	
	}
	
	private int canDropInsideBeginFolder(){
		int index = fTargetFolder.getDipDocChildrenList().indexOf(fMovedElement);
		if (index == 0){
			return NOT_ALLOWED;
		} else {
			return OK;
		}
	}
	
	private int canUnitDropInsideFolder(){
		int oldIndex = fTargetFolder.getDipDocChildrenList().indexOf(fMovedElement);
		int newIndex = fTargetFolder.getDipDocChildrenList().indexOf(fBeforeTargetElement) + 1;
		if (oldIndex == newIndex){
			return NOT_ALLOWED;
		} else {
			return OK;
		}
	}
	
	public int canUnitDropIntoFolder(){
		boolean contains = DipUtilities.containsElement(fTargetFolder, fMovedElement);
		if (contains){
			return ALREADY_EXISTS;
		}
		boolean reserved = DipUtilities.containsReservedElement(fTargetFolder, fMovedElement);
		if (reserved){
			return HAS_RESERVED;
		}
		return OK;
	}
	
	private int canParentDrop(){
		if (fBeforeTargetElement == null){
			return canFolderDropIntoBeginning();
		}
		if (fBeforeTargetElement instanceof DipUnit){
			fTargetFolder = fBeforeTargetElement.parent();
			List<IDipDocumentElement> fTargetChildren = fTargetFolder.getDipDocChildrenList();
			int oldIndex = fTargetChildren.indexOf(fBeforeTargetElement);
			if (oldIndex < fTargetChildren.size() - 1){
				IDipDocumentElement nextElement = fTargetChildren.get(oldIndex + 1); 
				if (!(nextElement instanceof IDipParent)){
					return NOT_ALLOWED;
				}
			}
			
			fOneParent = (fTargetFolder.equals(fMovedElement.parent()));
			if (fOneParent){
				return canUnitDropInsideFolder();
			} else {
				return canUnitDropIntoFolder();
			}
		}
		if (fBeforeTargetElement instanceof DipFolder){
			fTargetFolder  = fBeforeTargetElement.parent();
			fOneParent = (fTargetFolder.equals(fMovedElement.parent()));
			if (fOneParent){
				return canUnitDropInsideFolder();
			} else {
				return canFolderDropIntoFolder();
			}
		}
		return NOT_ALLOWED;
	}
	
	private int canFolderDropIntoBeginning(){
		fTargetFolder = fModelProvider.getModel();
		IDipDocumentElement firstElement = fTargetFolder.getDipDocChildrenList().get(0);
		if (firstElement instanceof IDipParent){		
			fOneParent = (fTargetFolder.equals(fMovedElement.parent()));
			if (fOneParent) {
				return canDropInsideBeginFolder();
			} else {
				return canFolderDropIntoFolder();
			}
		}
		return NOT_ALLOWED;		
	}
	
	private int canFolderDropIntoFolder(){
		boolean contains = DipUtilities.containsElement(fTargetFolder, fMovedElement);
		if (fTargetFolder.equals(fMovedElement)){
			return ALREADY_EXISTS;
		}
		if (fMovedElement instanceof IParent && fTargetFolder.hasParent((IParent) fMovedElement)){
			return ALREADY_EXISTS;
		}		
		if (contains){
			IDipElement element = fTargetFolder.getChild(fMovedElement.name());
			if (element instanceof DipReservedFolder){
				return HAS_RESERVED;
			}						
			return ALREADY_EXISTS;
		}
		return OK;
	}
	
	public int canDropIntoFolder(){
		if (fMovedElement instanceof DipUnit){
			return canUnitDropIntoFolder();
		} else {
			return canFolderDropIntoFolder();
		}
	}
	
	
	//====================================
	// drop
	
	public boolean drop(IDipDocumentElement beforeTargetElement, IDipDocumentElement movedElement) throws DeleteDIPException, CopyDIPException{
		canDrop(beforeTargetElement, movedElement);
		if (fDropStatus == ALREADY_EXISTS){
			MessageDialog.openError(fShell, MOVING_ERROR_TITLE, ALREADY_EXISTS_MESSAGE);						
			return false;
		}
		if (fDropStatus == HAS_RESERVED){
			MessageDialog.openError(fShell, MOVING_ERROR_TITLE, HAS_RESERVED_MESSAGE);						
			return false;
		}
		if (fOneParent){
			dropInsideFolder();
		} else {
			moveIntoFolder();
		}
		return true;
	}

	private void dropInsideFolder(){
		if (fMovedElement instanceof DipUnit){
			dropUnitInsideFolder();
		} else {
			dropFolderInsideFolder();
		}	
	}
	
	private void dropUnitInsideFolder(){
		fTargetFolder.removeChild(fMovedElement);
		int index = getNewUnitIndex();
		fTargetFolder.addNewChild(fMovedElement, index);
	}
	
	private int getNewUnitIndex(){
		if (fBeforeTargetElement instanceof DipUnit){
			return fTargetFolder.getDipDocChildrenList().indexOf(fBeforeTargetElement) + 1;
		}
		return 0;
	}
	
	private void dropFolderInsideFolder(){
		fTargetFolder.removeChild(fMovedElement);
		int index = getNewFolderIndex();
		fTargetFolder.addNewChild(fMovedElement, index);
	}

	private int getNewFolderIndex(){
		if (fBeforeTargetElement instanceof DipUnit || fBeforeTargetElement instanceof IDipParent){
			return fTargetFolder.getDipDocChildrenList().indexOf(fBeforeTargetElement) + 1;
		}
		return 0;
	}
		
	public void moveIntoFolder() throws DeleteDIPException, CopyDIPException{
		if (!DipCorePlugin.isDisableReservation()) {
			fNeedReserved = MessageDialog.openQuestion(fShell, MOVING_TITLE, NEED_RESERVE_QUESTION);		
		}
		DipUtilities.copyElement(fTargetFolder, fMovedElement, fShell);
		DipUtilities.deleteElement(fMovedElement, fNeedReserved, fShell);	
		LinkInteractor.instance().updateLinks(fMovedElement, fTargetFolder.getChild(fMovedElement.name()));
	}
	
	//=====================
	// getter && setter
	
	public void setMovedElement(IDipDocumentElement movedElement){
		fMovedElement = movedElement;
	}
	
	public void setTargetFolder(IDipParent target){
		fTargetFolder = target;
	}

}
