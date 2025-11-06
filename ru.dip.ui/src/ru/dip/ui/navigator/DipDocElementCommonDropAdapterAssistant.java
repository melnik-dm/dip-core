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
package ru.dip.ui.navigator;

import java.util.ArrayList;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.util.LocalSelectionTransfer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.ui.navigator.CommonDropAdapter;
import org.eclipse.ui.navigator.CommonDropAdapterAssistant;

import ru.dip.core.DipCorePlugin;
import ru.dip.core.exception.CopyDIPException;
import ru.dip.core.exception.DeleteDIPException;
import ru.dip.core.exception.TmpCopyException;
import ru.dip.core.link.LinkInteractor;
import ru.dip.core.model.interfaces.IParent;
import ru.dip.core.model.DipElementType;
import ru.dip.core.model.interfaces.IDipElement;
import ru.dip.core.model.interfaces.IDipParent;
import ru.dip.core.utilities.DipTableUtilities;
import ru.dip.core.utilities.DipUtilities;
import ru.dip.core.utilities.ResourcesUtilities;
import ru.dip.core.utilities.WorkbenchUtitlities;
import ru.dip.core.utilities.status.StatusUtils;
import ru.dip.ui.Messages;
import ru.dip.ui.dialog.ErrorDialog;
import ru.dip.ui.dialog.QuestionDialog;

public class DipDocElementCommonDropAdapterAssistant extends CommonDropAdapterAssistant {

	public static final int COPY_OPERATION = 1;
	public static final int MOVE_OPERATION = 2;
	
	private IDipParent fDestiantion;
	private IStatus fValidateStatus;
	private Object[] fSourceItems;
	private int fOperation;
	

	@Override
	public IStatus validateDrop(Object target, int operation, TransferData transferType) {
		fOperation = operation;	
		fDestiantion = getDestinationElement(target);		
		if (fDestiantion == null){
			return StatusUtils.INVALID_TARGET_FOLDER;
		}
		if (fDestiantion.isReadOnly()) {
			return StatusUtils.READ_ONLY_TARGET_FOLDER;
		}
		
		// no elements
		fSourceItems = getSelection();
		if (fSourceItems == null){
			return StatusUtils.NOT_MOVED_OBJ;
		}
		// 1 element
		if (fSourceItems.length == 1){
			fValidateStatus = validateElement(fSourceItems[0]);
			if (fValidateStatus.getSeverity() == IStatus.WARNING){
				return Status.OK_STATUS;
			}
			return fValidateStatus;
		}
		// many elements
		fValidateStatus = validateElements(fSourceItems);			
		return fValidateStatus;
	}
	
	/**
	 * Для перемещения (копирования) доступны только: DipUnit и DipFolder
	 * либо простые ресурсы (file, folder)  
	 */
	public Object[] getSelection(){
		ISelection sel = LocalSelectionTransfer.getTransfer().getSelection();
		if (sel != null && !sel.isEmpty() && sel instanceof IStructuredSelection) {
			Object[] items =  ((IStructuredSelection) sel).toArray();
			ArrayList<Object> elements = new ArrayList<>();
			IDipElement currentElement = null;
			IResource currentResource = null;
			for (Object obj: items){
				if (obj instanceof IDipElement){
					currentResource = null;
					IDipElement dipElement = (IDipElement) obj;
					DipElementType type  = dipElement.type();
					if (type != DipElementType.UNIT && type != DipElementType.FOLDER){
						return null;
					}					
					// недолжно быть связей родитель-потомок
					if (currentElement != null && currentElement instanceof IParent && dipElement.hasParent((IParent) currentElement)) {						
						continue;				
					} else {
						currentElement = dipElement;
						elements.add(dipElement);
					}
				} else if (obj instanceof IFile || obj instanceof IFolder){
					currentElement = null;
					if (ResourcesUtilities.hasParent((IResource)obj, currentResource)){
						continue;
					} else {
						currentResource = (IResource) obj;
						elements.add(obj);
					}
					// !!!!
				} else {
					return null;
				}
			}
			if (elements.size() > 0){
				return elements.toArray();
			}
		}
		return null;
	}
	
	public IDipParent getDestinationElement(Object target){
		if (target instanceof IDipParent){
			return (IDipParent) target;
		} 
		if (target instanceof IParent){
			return null;
		}
		if (target instanceof IDipElement){
			IParent parent = ((IDipElement) target).parent();
			if (parent instanceof IDipParent){
				return (IDipParent) parent;
			}
		}
		return null;
	}
	
	public IStatus validateElements(Object[] items){
		for (Object item: items){
			IStatus status = validateElement(item);
			if (!status.isOK()){
				return status;
			}
		}
		return Status.OK_STATUS;		
	}
	
	public IStatus validateElement(Object item){
		if (item instanceof IDipElement){
			IDipElement dipElement = (IDipElement) item;
			if (fOperation == DND.DROP_MOVE && dipElement.parent() != null &&
					dipElement.parent().isReadOnly()) {
				return StatusUtils.READ_ONLY_MOVED_OBJ;
			}			
			return DipUtilities.canPaste(fDestiantion, dipElement);
			
		}
		if (item instanceof IResource){
			IResource resource = (IResource) item;
			return DipUtilities.canPaste(fDestiantion, resource);
		}
		return new Status(IStatus.ERROR, DipCorePlugin.PLUGIN_ID, Messages.DipDocElementCommonDropAdapterAssistant_InvalidElementTypeErrorMessage);			
	}
	
	@Override
	public IStatus handleDrop(CommonDropAdapter aDropAdapter, DropTargetEvent aDropTargetEvent, Object aTarget) {
		if (fOperation ==  DND.DROP_MOVE){
			try {
				doMove();
			} catch (CopyDIPException e) {
				e.printStackTrace();				
				MessageDialog.openError(getShell(), Messages.DipDocElementCommonDropAdapterAssistant_MoveErrorTitle, e.getMessage());
			}
		} else if (fOperation == DND.DROP_COPY){
			try {
				doCopy();
			} catch (CopyDIPException e) {
				e.printStackTrace();
				MessageDialog.openError(getShell(), Messages.DipDocElementCommonDropAdapterAssistant_CopyErrorTitle, e.getMessage());
			}			
		}	
		ResourcesUtilities.updateDipElement(fDestiantion);
		WorkbenchUtitlities.updateProjectExplorer();
		fDestiantion.dipProject().updateNumeration();
		DipTableUtilities.saveModel(fDestiantion);
		
		return Status.OK_STATUS;
	}
	
	private void doCopy() throws CopyDIPException{
		if (fValidateStatus.isOK()) {
			for (Object obj : fSourceItems) {
				IResource res;
				if (obj instanceof IResource) {
					res = (IResource) obj;
					DipUtilities.copyResource(fDestiantion, res, getShell());
				} else if (obj instanceof IDipElement) {
					IDipElement element = (IDipElement) obj;
					DipUtilities.copyElement(fDestiantion, element, getShell());
				} else {
					continue;
				}
			}
		} else {
			throw new CopyDIPException(fSourceItems, fValidateStatus.getMessage());

		}
	}
	
	private void doMove() throws CopyDIPException{
		if (fValidateStatus.isOK()){
			// нужно ли резервировать
			boolean needReserve = false;
			if (!DipCorePlugin.isDisableReservation() && hasReservedElement()){
				boolean one = fSourceItems.length == 1;
				needReserve = QuestionDialog.needReserveForMove(getShell(), one);
			}			
			doCopy();
			doDelete(needReserve);
			updateLinks();			
		} else {
			// show message
			ErrorDialog.openMoveErorrMessage(getShell(), fValidateStatus.getMessage());					
		}
	}
	
	private boolean hasReservedElement(){
		for (Object obj: fSourceItems){
			if (obj instanceof IDipElement){
				return true;
			}
		}
		return false;
	}
	
	private void doDelete(boolean needReserve){
		for (Object obj: fSourceItems){
			if (obj instanceof IResource){
				delete((IResource) obj);
			} else if (obj instanceof IDipElement){
				IDipElement element = (IDipElement) obj;
				delete(element, needReserve);
				IParent parent =element.parent();
				if (parent instanceof IDipParent) {
					DipTableUtilities.saveModel((IDipParent) parent);
				}
			}
		}
	}
	
	private void delete(IResource resource){
		try {
			ResourcesUtilities.deleteResource(resource, getShell());
		} catch (CoreException e) {
			e.printStackTrace();
			MessageDialog.openError(getShell(), Messages.DipDocElementCommonDropAdapterAssistant_DeleteErrorTitle, e.getMessage());
		}

	}
	
	private void delete(IDipElement element, boolean needReserve){
		try {
			DipUtilities.deleteElement(element, needReserve, getShell(), DipUtilities.NO_TMP);
		} catch (DeleteDIPException | TmpCopyException e) {
			e.printStackTrace();
			MessageDialog.openError(getShell(), Messages.DipDocElementCommonDropAdapterAssistant_DeleteError, e.getMessage());
		}
	}

	private void updateLinks() {
		for (Object obj: fSourceItems){
			if (obj instanceof IDipElement) {
				IDipElement lastElem = (IDipElement) obj;
				IDipElement newElem  = fDestiantion.getChild(lastElem.name());
				LinkInteractor.instance().updateLinks(lastElem, newElem);
			}			
		}
	}
}
