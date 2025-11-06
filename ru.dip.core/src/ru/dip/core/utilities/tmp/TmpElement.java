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
package ru.dip.core.utilities.tmp;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;

import ru.dip.core.model.DipElement;
import ru.dip.core.model.DipElementType;
import ru.dip.core.model.DipUnit;
import ru.dip.core.model.interfaces.IDipElement;
import ru.dip.core.model.interfaces.IDipParent;
import ru.dip.core.model.interfaces.ICommentSupport;
import ru.dip.core.model.interfaces.IDescriptionSupport;
import ru.dip.core.model.interfaces.IDipDocumentElement;
import ru.dip.core.utilities.DipTableUtilities;
import ru.dip.core.utilities.DipUtilities;
import ru.dip.core.utilities.ResourcesUtilities;

public class TmpElement {
	
	private final String fParentId;
	private final String fName;
	private final int fIndex;
	private final Path fTmpPath;
	private final DipElementType fType;	
	private String fDescription;
	private String fComment;
	
	private boolean fDisabled;
	private boolean fHorizontalOrientation;	
	private String fFileStep;
	private String fFolderStep;
	private boolean fActiveNumeration;
	
	
	public TmpElement(IDipElement element, Path tmpPath, DipElementType type) {
		fParentId = element.parent() != null ? element.parent().id() : null;
		if (element instanceof IDescriptionSupport) {
			fDescription = ((IDescriptionSupport) element).description();
		}
		if (element instanceof ICommentSupport) {
			fComment = ((DipElement) element).getCommentContent();
		}
		fName = element.name();
		if (element instanceof IDipDocumentElement) {
			fIndex = DipTableUtilities.getIndex((IDipDocumentElement) element);
		} else {
			fIndex = -1;
		}
		fTmpPath = tmpPath;
		fType = type;
			
		if (element instanceof DipUnit) {
			DipUnit unit = (DipUnit) element;
			fDisabled = unit.isDisabled();
			fHorizontalOrientation = unit.isHorizontalOrientation();
		}
		if (element instanceof IDipParent) {
			IDipParent parent = (IDipParent) element;
			fDisabled = parent.isDisabled();
			fFileStep = parent.getFileStep();
			fFolderStep = parent.getFolderStep();
			fActiveNumeration = parent.isActiveNumeration();
		}
	}
	
	/**
	 * Восстанавливает элемент на уровне ресурсов и dip-модели
	 */
	public IDipDocumentElement recoveryDipDocElement() throws IOException {
		IDipElement element = DipUtilities.findElement(getId());
		if (element instanceof IDipParent) {
			IDipParent parent = (IDipParent) element;
			IContainer parentContainer = parent.resource();			
			if (getType() == DipElementType.FOLDER) {			
				IFolder file =  parentContainer.getFolder(new org.eclipse.core.runtime.Path(getName()));
				ResourcesUtilities.copyFolder(getTmpPath(), Paths.get(file.getLocationURI()));
				ResourcesUtilities.updateDipElement(parent);
				if (getIndex() >= 0) {
					IDipElement recoveryElement =  DipTableUtilities.addNewFolderByIndex(parent, file, getIndex());										
					if (recoveryElement instanceof IDipDocumentElement) {
						setDescritpionAndComment((IDipDocumentElement) recoveryElement);
						return (IDipDocumentElement) recoveryElement;
					}	
				} 
			} else {
				IFile file =  parentContainer.getFile(new org.eclipse.core.runtime.Path(getName()));
				Files.copy(getTmpPath(), Paths.get(file.getLocationURI()));
				ResourcesUtilities.updateDipElement(parent);				
				if (getIndex() >= 0) {
					IDipElement recoveryElement =  DipTableUtilities.addNewFileByIndex(parent, file, getIndex());
					if (recoveryElement instanceof IDipDocumentElement) {
						setDescritpionAndComment((IDipDocumentElement) recoveryElement);
						return (IDipDocumentElement) recoveryElement;
					}			
				}
			}	
		}		
		return null;	
	}
	
	private void setDescritpionAndComment(IDipDocumentElement dipElement) {
		if (fDescription != null && !fDescription.isEmpty()) {
			dipElement.setDescription(fDescription);
		}
		if (fComment != null && !fComment.isEmpty()) {
			dipElement.updateDipComment(fComment);
		}
		
		if (dipElement instanceof DipUnit) {
			DipUnit unit = (DipUnit) dipElement;
			unit.setDisabled(fDisabled);
			unit.setHorizontalOrientation(fHorizontalOrientation);
		} 
		if (dipElement instanceof IDipParent) {
			IDipParent parent = (IDipParent) dipElement;
			parent.setDisabled(fDisabled);
			parent.setActiveNumeration(fActiveNumeration);
			parent.setFileStep(fFileStep);
			parent.setFolderStep(fFolderStep);
		}
		
	}
	
	//======================
	// getters & setters
		
	public String getId() {
		return fParentId;
	}
	
	public Path getTmpPath() {
		return fTmpPath;
	}
	
	public void setDescription(String description) {
		fDescription = description;
	}
	
	public void setComment(String comment) {
		fComment = comment;
	}
	
	public int getIndex() {
		return fIndex;
	}

	public DipElementType getType() {
		return fType;
	}

	public String getName() {
		return fName;
	}
	
}
