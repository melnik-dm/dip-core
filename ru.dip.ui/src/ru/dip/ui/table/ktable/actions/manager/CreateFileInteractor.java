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

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;

import ru.dip.core.exception.CreateResourceException;
import ru.dip.core.exception.DIPException;
import ru.dip.core.exception.NotFoundTableNodeException;
import ru.dip.core.model.DipElementType;
import ru.dip.core.model.DipProject;
import ru.dip.core.model.DipReservedFolder;
import ru.dip.core.model.interfaces.IDipDocumentElement;
import ru.dip.core.model.interfaces.IDipElement;
import ru.dip.core.model.interfaces.IDipParent;
import ru.dip.core.utilities.DipTableUtilities;
import ru.dip.core.utilities.DipUtilities;
import ru.dip.core.utilities.ReservedUtilities;
import ru.dip.core.utilities.ResourcesUtilities;
import ru.dip.core.utilities.tmp.CreateFileResult;
import ru.dip.core.utilities.tmp.CreateFolderResult;
import ru.dip.core.utilities.tmp.CreateFolderResult.CreateFolderResultBuilder;
import ru.dip.ui.table.ktable.KDipTableSelector;
import ru.dip.ui.table.ktable.KTableComposite;
import ru.dip.ui.wizard.newtablefile.TableNewFileWizard;
import ru.dip.ui.wizard.tablenewfolder.TableNewFolderWizard;

public class CreateFileInteractor {

	private static final String FIND_ELEMENT_EXCEPTION = "Find Element Exception ";

	private static final String CREATE_FILE_EXCEPTION = "Create File Exception. ";
	private static final String REDO_CREATE_FILE_EXCEPTION = "Redo Create File Exception";
	private static final String UNDO_CREATE_FILE_EXCEPTION = "Undo Create File Exception";

	private static final String CREATE_FOLDER_EXCEPTION = "Create Folder Exception. ";
	private static final String REDO_CREATE_FOLDER_EXCEPTION = "Redo Create Folder Exception. ";
	private static final String UNDO_CREATE_FOLDER_EXCEPTION = "Undo Create Folder Exception. ";

	private KTableComposite fTableComposite;

	public CreateFileInteractor(KTableComposite tableComposite) {
		fTableComposite = tableComposite;
	}

	/**
	 * Импортировать после/перед выделенным файлом
	 */
	public CreateFileResult createNeighbourFileWithWizard(boolean before) throws CreateResourceException {
		IDipDocumentElement element = selector().getSelectedOneDipDocElement();
		// если нет выделенного элемента - значит корневая папка
		if (element == null) {
			element = selector().getSelectionParent();
		}
		if (element != null) {
			FileCreator creater = new FileCreator(element);
			boolean wizardOk = creater.openFileWizard(before);
			if (wizardOk) {
				creater.createFile();
				creater.getNewIndex();
				creater.getDipParentFromSelected();
				creater.addToReqModel();
				creater.updateTableModel();
				return creater.getCreateResult();
			}
		}
		return null;
	}

	public void redoCreateFromResult(CreateFileResult result) throws CreateResourceException {
		try {
			IDipParent targetParent = getDipParentFromCreateResult(result);
			FileCreator creater = new FileCreator(targetParent, result.getFileName(), result.getIndex());
			creater.createFile();
			creater.addToReqModel();
			creater.updateTableModel();
		} catch (CreateResourceException e) {
			throw new CreateResourceException(REDO_CREATE_FILE_EXCEPTION, e);
		}
	}

	private IDipParent getDipParentFromCreateResult(CreateFileResult result) throws CreateResourceException {
		Optional<IDipElement> dipElementOpt = DipUtilities.findDipElementInProject(result.getParentId(), dipProject());
		if (dipElementOpt.isEmpty()) {
			throw new CreateResourceException(FIND_ELEMENT_EXCEPTION + result.getParentId());
		}
		return (IDipParent) dipElementOpt.get();
	}

	public void undoCreate(CreateFileResult result) throws CreateResourceException {
		// delete file
		Optional<IDipElement> parent = DipUtilities.findDipElementInProject(result.getParentId(),
				fTableComposite.dipProject());
		if (parent.isEmpty()) {
			throw new CreateResourceException(UNDO_CREATE_FILE_EXCEPTION);
		}
		IDipDocumentElement dipDocElement = (IDipDocumentElement) ((IDipParent) parent.get()).getChild(result.getFileName());
		if (dipDocElement == null) {
			throw new CreateResourceException(UNDO_CREATE_FILE_EXCEPTION);
		}

		try {
			fTableComposite.deleteElementWithoutUI(dipDocElement);
		} catch (DIPException e) {
			e.printStackTrace();
			throw new CreateResourceException(UNDO_CREATE_FILE_EXCEPTION, e);
		}
	}

	private class FileCreator {

		// where
		private IDipDocumentElement fSelectedDipDocElement; // может папкой или файлом
		private boolean fBefore; // до/после файла или в начало/конец папки
		// result file
		private String fNewFileName;
		private IFile fNewFile;
		// result req model
		private IDipParent fTargetDipParent;
		private int fIndex; // индекс,куда вставлять (ReqModel)
		// reportref (если созадется ссылка на отчет)
		private IDipElement fReport;
		private boolean fRelativeReportRef;
		
		public FileCreator(IDipDocumentElement selectedDipDocElement) {
			fSelectedDipDocElement = selectedDipDocElement;
			if (fSelectedDipDocElement instanceof IDipParent) {
				fTargetDipParent = (IDipParent) fSelectedDipDocElement;
			} else {
				fTargetDipParent = fSelectedDipDocElement.parent();
			}
		}

		public FileCreator(IDipParent targetParent, String fileName, int index) {
			fTargetDipParent = targetParent;
			fNewFileName = fileName;
			fIndex = index;
		}

		boolean openFileWizard(boolean before) {
			TableNewFileWizard wizard = new TableNewFileWizard(fSelectedDipDocElement, before);
			setWizardLabels(wizard);
			WizardDialog wd = new WizardDialog(getShell(), wizard);
			wd.setTitle(wizard.getWindowTitle());
			if (wd.open() == WizardDialog.OK) {
				fBefore = wizard.isStart();
				fNewFileName = wizard.getFileName();
				if (wizard.isReportRef()) {
					fReport = wizard.getReport();
					fRelativeReportRef = wizard.getRelativeReportRef();
				}				
				return true;
			} else {
				return false;
			}
		}

		/**
		 * Устанавливает метки в визард. До и после файла, либо в начало или конец
		 * директории.
		 */
		private void setWizardLabels(TableNewFileWizard wizard) {
			if (fSelectedDipDocElement instanceof IDipParent) {
				if (fTargetDipParent.isFileNumeration()) {
					String start = DipTableUtilities.getStartNumberInFolder(fTargetDipParent);
					String end = DipTableUtilities.getEndNumberInFolder(fTargetDipParent);
					wizard.setAutoNumeration(start, end);
				}
			} else {
				if (fTargetDipParent.isFileNumeration()) {
					IDipDocumentElement previous = DipTableUtilities.getPreviousElement(fSelectedDipDocElement);
					String beforeName = DipTableUtilities.getNextNumber(fTargetDipParent, previous);
					String afterName = DipTableUtilities.getNextNumber(fTargetDipParent, fSelectedDipDocElement);
					wizard.setAutoNumeration(beforeName, afterName);
				}
			}
		}

		void createFile() throws CreateResourceException {
			IFile file = fTargetDipParent.resource().getFile(new Path(fNewFileName));
			try {
				fNewFile = ResourcesUtilities.createFile(file, getShell());
			} catch (CoreException e) {
				e.printStackTrace();
				throw new CreateResourceException(CREATE_FILE_EXCEPTION, e);
			}

			if (DipUtilities.isFormFile(fNewFile)) {
				DipUtilities.createFormFile(fNewFile);
			} else if (DipUtilities.isDiaFile(fNewFile)) {
				DipUtilities.createDiaFile(fNewFile);
			} else if (fReport != null) {
				DipUtilities.createReportRefFile(fNewFile, fReport, fRelativeReportRef);
			}
		}

		void getNewIndex() {
			if (fSelectedDipDocElement instanceof IDipParent) {
				fIndex = fBefore ? 0 : DipTableUtilities.getLastUnitIndex((IDipParent) fSelectedDipDocElement) + 1;
			} else {
				fIndex = fBefore ? fSelectedDipDocElement.getIndex() : fSelectedDipDocElement.getIndex() + 1;
			}
		}

		void getDipParentFromSelected() {
			if (fSelectedDipDocElement instanceof IDipParent) {
				fTargetDipParent = (IDipParent) fSelectedDipDocElement;
			} else {
				fTargetDipParent = fSelectedDipDocElement.parent();
			}
		}

		void addToReqModel() {
			DipTableUtilities.addNewFileByIndex(fTargetDipParent, fNewFile, fIndex);
		}

		void updateTableModel() throws CreateResourceException {
			try {
				fTableComposite.editor().updater().updateNewObject(fTargetDipParent, fNewFileName, true);
			} catch (NotFoundTableNodeException e) {
				throw new CreateResourceException(CREATE_FILE_EXCEPTION + e.getMessage());
			}
		}

		CreateFileResult getCreateResult() {
			return new CreateFileResult(DipUtilities.relativeProjectID(fTargetDipParent), fNewFileName, fIndex);
		}

	}

	// ==================================
	// crearte folder

	public CreateFolderResult createFolderWithWizard() throws CreateResourceException {
		IDipParent parent = selector().getSelectionParent();
		if (parent == null) {
			return null;
		}
		FolderCreater folderCreater = new FolderCreater(parent);
		if (folderCreater.openFolderWizard(false)) {
			folderCreater.createFolder();
			folderCreater.checkReserved();
			folderCreater.getNewIndex();
			folderCreater.addToReqModel();
			folderCreater.setOptions();
			folderCreater.updateTableModel();
			return folderCreater.getCreateResult();
		}
		return null;
	}

	public CreateFolderResult createNeighbourFolderWithWizard(boolean before) throws CreateResourceException {
		IDipDocumentElement neighbour = selector().getSelectedOneDipDocElement();
		if (neighbour == null) {
			return null;
		}
		FolderCreater folderCreater = new FolderCreater(neighbour, before);
		if (folderCreater.openFolderWizard(before)) {
			folderCreater.createFolder();
			folderCreater.checkReserved();
			folderCreater.getNewIndex();
			folderCreater.addToReqModel();
			folderCreater.setOptions();
			folderCreater.updateTableModel();
			return folderCreater.getCreateResult();
		}
		return null;
	}

	public void undoCreate(CreateFolderResult result) throws CreateResourceException {
		// delete file
		Optional<IDipElement> parent = DipUtilities.findDipElementInProject(result.getParentId(),
				fTableComposite.dipProject());
		if (parent.isEmpty()) {
			throw new CreateResourceException(UNDO_CREATE_FOLDER_EXCEPTION);
		}
		IDipDocumentElement dipDocElement = (IDipDocumentElement) ((IDipParent) parent.get()).getChild(result.getFolderName());
		if (dipDocElement == null) {
			throw new CreateResourceException(UNDO_CREATE_FOLDER_EXCEPTION);
		}
		try {
			fTableComposite.deleteElementWithoutUI(dipDocElement);
		} catch (DIPException e) {
			e.printStackTrace();
			throw new CreateResourceException(UNDO_CREATE_FOLDER_EXCEPTION, e);
		}
	}

	public void redoCreateFolderFromResult(CreateFolderResult result) throws CreateResourceException {
		try {
			FolderCreater folderCreater = new FolderCreater(result);
			folderCreater.createFolder();
			folderCreater.checkReserved();
			folderCreater.addToReqModel();
			folderCreater.setOptions();
			folderCreater.updateTableModel();
		} catch (CreateResourceException e) {
			throw new CreateResourceException(REDO_CREATE_FOLDER_EXCEPTION, e);
		}
	}

	private class FolderCreater {
		// where
		private IDipDocumentElement fSelectedDipDocElement; // может папкой или файлом
		private boolean fBefore; // до/после файла или в начало/конец папки
		// parameters
		private boolean fReserved;
		private String fFileStep;
		private String fFolderStep;
		// result folder
		private String fNewFolderName;
		private IFolder fNewFolder;
		private String fDescription;
		private IDipParent fResultDipFolder;
		// for result req model
		private IDipParent fTargetParent;
		private int fIndex; // индекс,куда вставлять (ReqModel)

		public FolderCreater(IDipParent targetParent) {
			fTargetParent = targetParent;
		}

		public FolderCreater(IDipDocumentElement selected, boolean before) {
			fSelectedDipDocElement = selected;
			fTargetParent = fSelectedDipDocElement.parent();
			fBefore = before;
		}

		public FolderCreater(CreateFolderResult result) throws CreateResourceException {
			fTargetParent = getDipParentFromCreateResult(result);
			fNewFolderName = result.getFolderName();
			fIndex = result.getIndex();
			fDescription = result.getDescription();
			fFileStep = result.getFileStep();
			fFolderStep = result.getFolderStep();
			fReserved = result.isReserved();
		}

		private IDipParent getDipParentFromCreateResult(CreateFolderResult result) throws CreateResourceException {
			Optional<IDipElement> dipElementOpt = DipUtilities.findDipElementInProject(result.getParentId(),
					dipProject());
			if (dipElementOpt.isEmpty()) {
				throw new CreateResourceException(FIND_ELEMENT_EXCEPTION);
			}
			return (IDipParent) dipElementOpt.get();
		}

		boolean openFolderWizard(boolean before) {
			TableNewFolderWizard wizard = new TableNewFolderWizard(fTargetParent, (IDipParent) fSelectedDipDocElement,
					before);
			setWizardLabels(wizard);
			WizardDialog wd = new WizardDialog(getShell(), wizard);
			wd.setTitle(wizard.getWindowTitle());
			if (wd.open() == WizardDialog.OK) {
				fBefore = wizard.isStart();
				fNewFolderName = wizard.getFolderName();
				fReserved = wizard.isReserved();
				if (wizard.isFileAutoNumbering()) {
					fFileStep = wizard.getFileStep();
				}
				if (wizard.isFolderAutoNumbering()) {
					fFolderStep = wizard.getFolderStep();
				}
				fDescription = wizard.getFolderDescription();
				return true;
			} else {
				return false;
			}
		}

		/**
		 * Устанвливает метки в визард. До и после папки, либо в начало или конец
		 * директории.
		 */
		private void setWizardLabels(TableNewFolderWizard wizard) {
			if (fSelectedDipDocElement instanceof IDipParent) {
				if (fTargetParent.isFolderNumeration()) {
					String start = DipTableUtilities.getStartFolderNumber(fTargetParent);
					String end = DipTableUtilities.getEndFolderNumber(fTargetParent);
					wizard.setAutoNumeration(start, end);
				}
			}
		}

		private void createFolder() throws CreateResourceException {
			fNewFolder = fTargetParent.resource().getFolder(new Path(fNewFolderName));
			if (!fReserved) {
				try {
					fNewFolder = ResourcesUtilities.createFolder(fNewFolder, getShell());
				} catch (CoreException e) {
					e.printStackTrace();
					throw new CreateResourceException(CREATE_FOLDER_EXCEPTION, e);
				}
			}
		}

		void getNewIndex() {
			if (fSelectedDipDocElement == null) {
				fIndex = fBefore ? -1 : fTargetParent.getDipDocChildrenList().size();
			} else {
				fIndex = fBefore ? fSelectedDipDocElement.getIndex() : fSelectedDipDocElement.getIndex() + 1;
			}
		}

		void checkReserved() {
			if (fReserved) {
				IDipElement reserverDipElement = fTargetParent.getChild(fNewFolderName);
				if (reserverDipElement.type() == DipElementType.RESERVED_FOLDER) {
					DipReservedFolder reservedFolder = (DipReservedFolder) reserverDipElement;
					ReservedUtilities.deleteUnreserveMarker(reservedFolder, getShell());
				}
			}
		}

		void addToReqModel() {
			if (fIndex < 0) {
				fResultDipFolder = DipTableUtilities.addNewFolderStart(fTargetParent, fNewFolder);
			} else {
				fResultDipFolder = DipTableUtilities.addNewFolderByIndex(fTargetParent, fNewFolder, fIndex);
			}
		}

		void setOptions() {
			if (fDescription != null && !fDescription.isEmpty()) {
				fResultDipFolder.updateDescription(fDescription);
			}
			if (fFileStep != null) {
				fResultDipFolder.setFileStep(fFileStep);
			}
			if (fFolderStep != null) {
				fResultDipFolder.setFolderStep(fFolderStep);
			}
		}

		void updateTableModel() throws CreateResourceException {
			try {
				fTableComposite.editor().updater().updateNewObject(fTargetParent, fNewFolderName, true);
			} catch (NotFoundTableNodeException e) {
				throw new CreateResourceException(CREATE_FOLDER_EXCEPTION + e.getMessage());
			}
		}

		CreateFolderResult getCreateResult() {
			return new CreateFolderResultBuilder().buildParentId(DipUtilities.relativeProjectID(fTargetParent))
					.buildIndex(fIndex).buildName(fNewFolderName).buildDescritpion(fDescription)
					.buildFileStep(fFileStep).buildFolderStep(fFolderStep).buildReserved(fReserved).build();
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
