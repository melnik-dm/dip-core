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
import java.nio.file.Paths;
import java.util.Optional;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.actions.CopyFilesAndFoldersOperation;

import ru.dip.core.exception.ImportException;
import ru.dip.core.exception.NotFoundTableNodeException;
import ru.dip.core.model.DipProject;
import ru.dip.core.model.interfaces.IDipElement;
import ru.dip.core.model.interfaces.IDipParent;
import ru.dip.core.model.interfaces.IDipDocumentElement;
import ru.dip.core.utilities.DipTableUtilities;
import ru.dip.core.utilities.DipUtilities;
import ru.dip.core.utilities.ResourcesUtilities;
import ru.dip.core.utilities.tmp.ImportResult;
import ru.dip.ui.table.ktable.KDipTableSelector;
import ru.dip.ui.table.ktable.KTableComposite;
import ru.dip.ui.table.ktable.dialog.ImportFolderDialog;
import ru.dip.ui.wizard.importfile.ImportFileWizard;

public class ImportActionInteractor {

	private KTableComposite fTableComposite;
	
	public ImportActionInteractor(KTableComposite composite) {
		fTableComposite = composite;
	}
	
	// ============================
	// import file

	public ImportResult importFileWithWizard() throws ImportException {	
		return importNeighbourFileWithWizard(false);
	}

	/**
	 * Импортировать после/перед выделенным файлом
	 */
	public ImportResult importNeighbourFileWithWizard(boolean before) throws ImportException {
		IDipDocumentElement element = selector().getSelectedOneDipDocElement();
		if (element != null) {
			Importer importer = new Importer(element);
			boolean wizardOk = importer.openFileWizard(before);
			if (wizardOk) {			
				importer.copyFile();
				importer.getNewIndex();
				importer.getDipParentFromSelected();
				importer.importToReqModel();
				importer.updateTableModel();
				return importer.getImportResult();
			}
		}
		return null;
	}

	public void redoImportFromResult(ImportResult result) throws ImportException {
		IDipParent targetParent = getDipParentFromImportResult(result);		
		Importer importer = new Importer(targetParent, result.getFileName(), result.getIndex(), result.getFilePath());
		importer.copyFile();
		importer.importToReqModel();
		importer.updateTableModel();	
	}
	
	private IDipParent getDipParentFromImportResult(ImportResult result) throws ImportException {
		Optional<IDipElement> dipElementOpt = DipUtilities.findDipElementInProject(result.getParentId(), dipProject());
		if (dipElementOpt.isEmpty()) {
			throw new ImportException("Redo Import Exception");
		}
		return (IDipParent) dipElementOpt.get();
	}
	
	public void undoImport(ImportResult result) throws Exception {
		// delete file		
		Optional<IDipElement> parent = DipUtilities.findDipElementInProject(result.getParentId(), fTableComposite.dipProject());
		if (parent.isEmpty()) {
			throw new ImportException("Undo Import Exception");
		}
		IDipDocumentElement dipDocElement = (IDipDocumentElement) ((IDipParent)parent.get()).getChild(result.getFileName());
		if (dipDocElement == null) {
			throw new ImportException("Undo Import Exception");
		}
		fTableComposite.deleteElementWithoutUI(dipDocElement);
	}	
	
	private class Importer {
		
		// where
		private IDipDocumentElement fSelectedDipDocElement;  // может папкой или файлом
		private boolean fBefore;					// до/после файла или в начало/конец папки
		// from
		private String fImportPath;	
		// result file
		private String fNewFileName;		
		private IFile fNewFile;
		private IContainer fParentContainer;
		// result req model
		private IDipParent fTargetDipParent;
		private int fIndex;  // индекс,куда вставлять (ReqModel)
		
		public Importer(IDipDocumentElement selectedDipDocElement) {
			fSelectedDipDocElement = selectedDipDocElement;
		}
					
		public Importer(IDipParent targetParent, String fileName, int index, String filePath) {
			fTargetDipParent = targetParent;
			fNewFileName = fileName;
			fIndex = index;
			fImportPath = filePath;
			fParentContainer = fTargetDipParent.resource();
		}

		boolean openFileWizard(boolean before) {
			ImportFileWizard wizard = new ImportFileWizard(fSelectedDipDocElement, before);
			WizardDialog wd = new WizardDialog(getShell(), wizard);
			wd.setTitle(wizard.getWindowTitle());
			if (wd.open() == WizardDialog.OK) {
				fParentContainer = wizard.getParentContainer();
				fImportPath = wizard.getImportPath();
				fNewFileName = wizard.getImportFileName();	
				fBefore = wizard.isStart();
				return true;
			} else {
				return false;
			}		
		}
			
		void copyFile() {
			CopyFilesAndFoldersOperation operation = new CopyFilesAndFoldersOperation(getShell());
			operation.copyFiles(new String[] { fImportPath }, fParentContainer);
			fNewFile =  fParentContainer.getFile(new Path(fNewFileName));
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
		
		void importToReqModel() {
			DipTableUtilities.addNewFileByIndex(fTargetDipParent, fNewFile, fIndex);			
		}
		
		void updateTableModel() throws ImportException {
			try {
				fTableComposite.editor().updater().updateNewObject(fTargetDipParent, fNewFileName, true);
			} catch (NotFoundTableNodeException e) {
				throw new ImportException(e.getMessage());
			}
		}	
		
		ImportResult getImportResult() {			
			return new ImportResult(DipUtilities.relativeProjectID(fTargetDipParent), fNewFileName, fIndex, fImportPath);
		}
	}

	// ==================================
	// import folder
	
	public ImportResult importFolderWithWizard() throws IOException, CoreException {
		IDipParent parent = selector().getSelectionParent();
		if (parent == null) {
			return null;
		}		
		FolderImporter importer = new FolderImporter(parent);
		if (importer.openImportFolderDialog() != null) {	
			importer.validateName();			
			importer.copyFolder();
			importer.importToReqModel();
			importer.updateTableModel();
			return importer.getImportResult();				
		}
		return null;	
	}
	
	public ImportResult importNeighbourFolderWithWizard(boolean before) throws IOException, CoreException {
		IDipParent parent = selector().getSelectionParent();
		if (parent == null) {
			return null;
		}		
		FolderImporter importer = new FolderImporter(parent, before);
		if (importer.openImportFolderDialog() != null) {			
			importer.validateName();						
			importer.copyFolder();	
			importer.computeIndex();
			importer.importToReqModelByIndex();
			importer.updateTableModel();
			return importer.getImportResult();				
		}
		return null;	
	}
	
	public void redoImportFolderFromResult(ImportResult result) throws IOException, CoreException {
		IDipParent targetParent = getDipParentFromImportResult(result);	
		int index = result.getIndex();
		FolderImporter importer = new FolderImporter(targetParent, result.getFileName(), index, result.getFilePath());
		importer.copyFolder();
		if (index >= 0) {
			importer.importToReqModelByIndex();
		} else {
			importer.importToReqModel();
		}
		importer.updateTableModel();	
	}
	
	private class FolderImporter {	
		// where
		private IDipDocumentElement fSelectedDipParent;  // может папкой или файлом
		private boolean fBefore;					// до/после файла или в начало/конец папки
		// from
		private String fImportPath;	
		private java.nio.file.Path fPath;
		// result file
		private String fNewFolderName;		
		private IFolder fNewFolder;
		private IContainer fParentContainer;
		// result req model
		private IDipParent fTargetDipParent;
		private int fIndex = -1;  // индекс,куда вставлять (ReqModel)
		
		public FolderImporter(IDipParent dipParent) {
			fTargetDipParent = dipParent;
			fParentContainer = fTargetDipParent.resource();
		}
		
		public FolderImporter(IDipParent dipParent, boolean before) {
			fSelectedDipParent = dipParent;
			fBefore = before;
			fTargetDipParent = fSelectedDipParent.parent();
			fParentContainer = fTargetDipParent.resource();
		}
					
		public FolderImporter(IDipParent targetParent, String folderName, int index, String filePath) {
			fTargetDipParent = targetParent;
			fNewFolderName = folderName;
			fIndex = index;
			fImportPath = filePath;
			fParentContainer = fTargetDipParent.resource();
			fPath = Paths.get(fImportPath);
		}
		
		String openImportFolderDialog() {
			ImportFolderDialog dialog = new ImportFolderDialog(getShell());
			if (dialog.open() == Window.OK) {
				fImportPath =  dialog.getFolderPath();
			}
			return fImportPath;
		}
		
		void validateName() throws ImportException {
			fPath = Paths.get(fImportPath);
			fNewFolderName = fPath.getFileName().toString();
			IStatus status = DipUtilities.canCreateFolder(fParentContainer, fNewFolderName);
			if (!status.isOK()) {
				throw new ImportException(status.getMessage());
			}
		}
		
		void copyFolder() throws IOException, CoreException {
			fNewFolder = fParentContainer.getFolder(new Path(fNewFolderName));
			fNewFolder = ResourcesUtilities.createFolder(fNewFolder, getShell());
			java.nio.file.Path newFolderPath = Paths.get(fNewFolder.getLocationURI());			
			ResourcesUtilities.copyFolder(fPath, newFolderPath);
		}
		
		void importToReqModel() {
			IDipParent newFolder = DipTableUtilities.addNewFolderEnd(fTargetDipParent, fNewFolder);		
			ResourcesUtilities.updateDipElement(newFolder);
			newFolder.refresh();
		}
		
		void computeIndex() {
			fIndex = fBefore ? fSelectedDipParent.getIndex() : fSelectedDipParent.getIndex() + 1;	
		}
		
		void importToReqModelByIndex() {
			IFolder folder = fParentContainer.getFolder(new Path(fNewFolderName));
			IDipParent newFolder =  DipTableUtilities.addNewFolderByIndex(fTargetDipParent, folder, fIndex);
			ResourcesUtilities.updateDipElement(newFolder);
			newFolder.refresh();
		}
		
		void updateTableModel() throws ImportException {
			try {
				fTableComposite.editor().updater().updateNewObject(fTargetDipParent, fNewFolderName, true);
			} catch (NotFoundTableNodeException e) {
				throw new ImportException(e.getMessage());
			}
		}	
		
		ImportResult getImportResult() {			
			return new ImportResult(DipUtilities.relativeProjectID(fTargetDipParent), fNewFolderName, fIndex, fImportPath);
		}	
	}
	
	//=========================
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
