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

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;

import ru.dip.core.exception.DIPException;
import ru.dip.core.exception.IncludeFolderException;
import ru.dip.core.exception.NotFoundTableNodeException;
import ru.dip.core.model.IncludeFolder;
import ru.dip.core.model.DipProject;
import ru.dip.core.model.interfaces.IDipElement;
import ru.dip.core.model.interfaces.IDipParent;
import ru.dip.core.model.interfaces.IDipDocumentElement;
import ru.dip.core.utilities.DipUtilities;
import ru.dip.core.utilities.tmp.IncludeFolderResult;
import ru.dip.core.utilities.tmp.IncludeFolderResult.IncludeResultBuilder;
import ru.dip.ui.table.ktable.KDipTableSelector;
import ru.dip.ui.table.ktable.KTableComposite;
import ru.dip.ui.wizard.include.IncludeWizard;

public class IncludeInteractor {
	
	private static final String FIND_ELEMENT_EXCEPTION = "Find Element Exception ";
	
	private static final String INCLUDE_FOLDER_EXCEPTION = "Include Exception. ";
	private static final String REDO_INCLUDE_FOLDER_EXCEPTION = "Redo Include Folder Exception. ";
	private static final String UNDO_INCLUDE_FOLDER_EXCEPTION = "Undo Include Folder Exception. ";
	
	private KTableComposite fTableComposite;
	
	public IncludeInteractor(KTableComposite tableComposite) {
		fTableComposite = tableComposite;
	}

	public IncludeFolderResult includeFolderWithWizard() throws IncludeFolderException {
		IDipParent parent = selector().getSelectionParent();
		if (parent == null) {
			return null;
		}
		FolderIncluder folderIncluder = new FolderIncluder(parent);
		if (folderIncluder.openFileWizard(false)) {
			folderIncluder.createLinkFolder();
			folderIncluder.getNewIndex();
			folderIncluder.addToReqModel();
			folderIncluder.updateTableModel();
			return folderIncluder.getIncludeResult();
		}
		return null;
	}

	public IncludeFolderResult includeNeighbourFolderWithWizard(boolean before) throws IncludeFolderException {
		IDipDocumentElement neighbour = selector().getSelectedOneDipDocElement();
		if (neighbour == null) {
			return null;
		}
		FolderIncluder folderIncluder = new FolderIncluder((IDipParent) neighbour, before);
		if (folderIncluder.openFileWizard(before)) {
			folderIncluder.createLinkFolder();
			folderIncluder.getNewIndex();
			folderIncluder.addToReqModel();
			folderIncluder.updateTableModel();
			return folderIncluder.getIncludeResult();
		}
		return null;
	}

	public void undoInclude(IncludeFolderResult result) throws IncludeFolderException {
		Optional<IDipElement> parent = DipUtilities.findDipElementInProject(result.getParentId(),
				fTableComposite.dipProject());
		if (parent.isEmpty()) {
			throw new IncludeFolderException(UNDO_INCLUDE_FOLDER_EXCEPTION);
		}
		
		IDipDocumentElement dipDocElement = (IDipDocumentElement) ((IDipParent) parent.get()).getChild(result.getFolderName());
		if (dipDocElement == null) {
			throw new IncludeFolderException(UNDO_INCLUDE_FOLDER_EXCEPTION);
		}
		try {
			fTableComposite.deleteElementWithoutUI(dipDocElement);
		} catch (DIPException e) {
			e.printStackTrace();
			throw new IncludeFolderException(UNDO_INCLUDE_FOLDER_EXCEPTION, e);
		}
	}

	public void redoIncludeFolderFromResult(IncludeFolderResult result) throws IncludeFolderException {
		try {
			FolderIncluder folderIncluder = new FolderIncluder(result);
			folderIncluder.createLinkFolder();
			folderIncluder.addToReqModel();
			folderIncluder.updateTableModel();
		} catch (DIPException e) {
			throw new IncludeFolderException(REDO_INCLUDE_FOLDER_EXCEPTION, e);
		}
	}
	
	private class FolderIncluder {
		
		// input
		private IDipParent fTargetParent;
		private IDipParent fSelectedDipDocElement;
		private boolean fBefore;
		// from wizard
		private String fIncludePath;
		private String fIncludeName;
		private String fDescription;
		private boolean fReadOnly;
		// compute
		private IFolder fLinkFolder;
		private int fIndex;

		
		FolderIncluder(IDipParent targetParent){
			fTargetParent = targetParent;
		}
		
		FolderIncluder(IDipParent selected, boolean before){
			fTargetParent = selected.parent();
			fSelectedDipDocElement = selected;
			fBefore = before;
		}
		
		FolderIncluder(IncludeFolderResult result) throws DIPException {
			fTargetParent = getDipParentFromIncludeResult(result);
			fIncludeName = result.getFolderName();
			fIndex = result.getIndex();
			fDescription = result.getDescription();
			fIncludePath = result.getIncludePath();
			fReadOnly = result.isReadOnly();
		}
		
		private IDipParent getDipParentFromIncludeResult(IncludeFolderResult result) throws DIPException {
			Optional<IDipElement> dipElementOpt = DipUtilities.findDipElementInProject(result.getParentId(),
					dipProject());
			if (dipElementOpt.isEmpty()) {
				throw new DIPException(FIND_ELEMENT_EXCEPTION);
			}
			return (IDipParent) dipElementOpt.get();
		}
		
		boolean openFileWizard(boolean before) {
			IncludeWizard wizard = (fSelectedDipDocElement == null) ? new IncludeWizard(fTargetParent) 
					: new IncludeWizard(fSelectedDipDocElement, before); 
			WizardDialog dialog = new WizardDialog(getShell(), wizard);			
			if (dialog.open() == Dialog.OK) {
				fIncludePath = wizard.includePath();
				fIncludeName = wizard.includeName();
				fDescription = wizard.description();
				fReadOnly = wizard.isReadOnly();
				fBefore = wizard.isStart();							
				return true;
			}
			return false;
		}
		
		void getNewIndex() {
			if (fSelectedDipDocElement == null) {
				fIndex = fBefore ? -1 : fTargetParent.getDipDocChildrenList().size();
			} else {
				fIndex = fBefore ? fSelectedDipDocElement.getIndex() : fSelectedDipDocElement.getIndex() + 1;
			}
		}
		
		void createLinkFolder() throws IncludeFolderException {
			try {
				fLinkFolder = IncludeFolder.createLinkFolder(fIncludeName, fIncludePath, fTargetParent.resource());
			} catch (CoreException e) {
				e.printStackTrace();
				throw new IncludeFolderException(INCLUDE_FOLDER_EXCEPTION, e);
			}
		}
		
		void addToReqModel() {
			if (fIndex < 0) {
				fTargetParent.includeFolder(fLinkFolder, fIncludeName, fDescription, fReadOnly);
			} else {
				fTargetParent.includeFolder(fLinkFolder, fIndex, fIncludeName, fDescription, fReadOnly);
			}	
		}
		
		void updateTableModel() throws IncludeFolderException {
			try {
				fTableComposite.editor().updater().updateNewObject(fTargetParent, fIncludeName, true);
			} catch (NotFoundTableNodeException e) {
				throw new IncludeFolderException(e.getMessage());
			}
		}
		
		IncludeFolderResult getIncludeResult() {	
			String includeName = !fIncludeName.isEmpty() ? fIncludeName : fLinkFolder.getName();			
			return new IncludeResultBuilder()
					.buildParentId(DipUtilities.relativeProjectID(fTargetParent))
					.buildIndex(fIndex)
					.buildName(includeName)
					.buildDescritpion(fDescription)
					.buildIncludePath(fIncludePath)
					.buildReadOnly(fReadOnly)
					.build();
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


}
