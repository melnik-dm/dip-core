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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWTError;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.part.ResourceTransfer;

import de.kupzog.ktable.KTableCellEditor;
import ru.dip.core.exception.CreateResourceException;
import ru.dip.core.exception.DIPException;
import ru.dip.core.exception.DeleteDIPException;
import ru.dip.core.exception.TmpCopyException;
import ru.dip.core.model.DipContainer;
import ru.dip.core.model.DipTableContainer;
import ru.dip.core.model.interfaces.ICommentSupport;
import ru.dip.core.model.interfaces.IDescriptionSupport;
import ru.dip.core.model.interfaces.IDipDocumentElement;
import ru.dip.core.model.interfaces.IDipElement;
import ru.dip.core.model.interfaces.IDipParent;
import ru.dip.core.utilities.DipTableUtilities;
import ru.dip.core.utilities.DipUtilities;
import ru.dip.core.utilities.ResourcesUtilities;
import ru.dip.core.utilities.tmp.DeleteResultOperation;
import ru.dip.core.utilities.tmp.PasteFileResult;
import ru.dip.core.utilities.tmp.PasteFolderResult;
import ru.dip.core.utilities.tmp.PasteObjectResult;
import ru.dip.core.utilities.tmp.PasteObjectsResult;
import ru.dip.ui.Messages;
import ru.dip.ui.table.editor.toolbar.FilterToolBar;
import ru.dip.ui.table.ktable.KTableComposite;
import ru.dip.ui.table.ktable.celleditors.ITextActionSupport;
import ru.dip.ui.table.ktable.model.IDipTableElement;
import ru.dip.ui.wizard.paste.PasteElement;
import ru.dip.ui.wizard.paste.PasteResourceElement;
import ru.dip.ui.wizard.paste.PasteWizard;

public class PasteInteractor {
	
	private static final String PASTE_FILE_EXCEPTION = "Paste File Exception. ";
	private static final String UNDO_PASTE_FILE_EXCEPTION = "Undo Paste File Exception. ";

	private final KTableComposite fTableComposite;
	private final Clipboard fClipboard = new Clipboard(Display.getDefault());
	
	public PasteInteractor(KTableComposite composite) {
		fTableComposite = composite;
	}
	
	//====================
	// copy
	
	public void doCopyToBuffer() {
		if (isOpenCellEditor()) {
			KTableCellEditor editor = fTableComposite.table().getCellEditor();
			if (editor instanceof ITextActionSupport) {
				ITextActionSupport simpleCellEditor = (ITextActionSupport) editor;
				simpleCellEditor.doCopy();
				return;
			}
		}
		if (isFilterEntryHasFocus()) {
			fTableComposite.editor().getButtonManager().getFilterToolBar().getTextControl().copy();
			return;
		}
						
		String ids = getUnitIds();
		IResource[] resources = fTableComposite.selector().getSelectedElements()
				.stream()
				.map(IDipDocumentElement::resource)
				.toArray(IResource[]::new);
		final int length = resources.length;
		int actualLength = 0;
		String[] fileNames = new String[length];
		
		StringBuffer buf = new StringBuffer();
		for (int i = 0; i < length; i++) {
			IPath location = resources[i].getLocation();
			// location may be null. See bug 29491.
			if (location != null) {
				fileNames[actualLength++] = location.toOSString();
			}
			if (i > 0) {
				buf.append("\n"); //$NON-NLS-1$
			}
			buf.append(resources[i].getName());
		}
		
		// was one or more of the locations null?
		if (actualLength < length) {
			String[] tempFileNames = fileNames;
			fileNames = new String[actualLength];
			for (int i = 0; i < actualLength; i++) {
				fileNames[i] = tempFileNames[i];
			}
		}
		setClipboard(fClipboard, resources, fileNames, /*buf.toString()*/ids);
	}
	
	private String getUnitIds() {
		IDipDocumentElement[] dipDocElements = fTableComposite.selector().getSelectedElements()
				.stream()
				.toArray(IDipDocumentElement[]::new);
		final int length = dipDocElements.length;
		StringBuffer buf = new StringBuffer();
		for (int i = 0; i < length; i++) {
			String id = DipUtilities.relativeProjectID(dipDocElements[i]);
			if (i > 0) {
				buf.append("\n"); //$NON-NLS-1$
			}
			buf.append(id);
		}
		return buf.toString();
	}
	
	
	private void setClipboard(Clipboard clipboard, IResource[] resources, String[] fileNames, String names) {
		try {
			// set the clipboard contents
			if (fileNames.length > 0) {
				clipboard.setContents(new Object[] { resources, fileNames, names }, new Transfer[] {
						ResourceTransfer.getInstance(), FileTransfer.getInstance(), TextTransfer.getInstance() });
			} else {
				clipboard.setContents(new Object[] { resources, names },
						new Transfer[] { ResourceTransfer.getInstance(), TextTransfer.getInstance() });
			}
		} catch (SWTError e) {
			if (e.code != DND.ERROR_CANNOT_SET_CLIPBOARD) {
				throw e;
			}
			if (MessageDialog.openQuestion(Display.getDefault().getActiveShell(), Messages.CopyAction_Copy_problem_title, Messages.CopyAction_Copy_problem_question)) {
				setClipboard(clipboard, resources, fileNames, names);
			}
		}
	}
	
	//====================
	// paste
	
	/**
	 * Если выделена папка - вставляется в папку в начало
	 * Если файла, то после файла при условии, что не была измнена директория в визарде
	 */
	public PasteObjectsResult doPaste() {
		ResourceTransfer resTransfer = ResourceTransfer.getInstance();
		IResource[] resourceData = (IResource[]) fClipboard.getContents(resTransfer);
		if (resourceData != null && resourceData.length > 0) {
			// try a resource transfer
			return doRunResourceTransfer(resourceData);
		} else {
			FileTransfer fileTransfer = FileTransfer.getInstance();
			String[] fileData = (String[]) fClipboard.getContents(fileTransfer);
			if (fileData != null && fileData.length > 0) {
				// try a file transfer
				return doRunFileTransfer(fileData);
			} else {
				doPasteTextSelection();
				return null;
			}
		}
	}
	
	private void doPasteTextSelection() {
		if (isOpenCellEditor()) {
			KTableCellEditor editor = fTableComposite.table().getCellEditor();
			if (editor instanceof ITextActionSupport) {
				ITextActionSupport ed = (ITextActionSupport) editor;
				ed.doPaste();
			}
		} else if (isFilterEntryHasFocus()) {
			fTableComposite.editor().getButtonManager().getFilterToolBar().getTextControl().paste();
			return;
		}
	}
	
	private PasteObjectsResult doRunResourceTransfer(IResource[] resourceData){		
		IDipDocumentElement req = fTableComposite.selector().getLastSelectDipDocElement();
		IDipParent parent = req instanceof IDipParent ? (IDipParent) req : req.parent();
		IDipDocumentElement selectedElement = req instanceof IDipParent ? null : req;		
		try {
			return pasteIntoFolder(parent, resourceData, selectedElement);
		} catch (CreateResourceException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	private PasteObjectsResult doRunFileTransfer(String[] fileData){
		IDipDocumentElement req = fTableComposite.selector().getLastSelectDipDocElement();
		IDipParent parent = req instanceof IDipParent ? (IDipParent) req : req.parent();		
		IDipDocumentElement selectedElement = req instanceof IDipParent ? null : req;		
		try {
			return pasteIntoFolder(parent, fileData, selectedElement);
		} catch (CreateResourceException e) {
			e.printStackTrace();
		}
		return null;	
	}
	
	private PasteObjectsResult pasteIntoFolder(IDipParent targetParent, String[] files, IDipDocumentElement selectedElement) throws CreateResourceException {
		PasteWizard wizard = new PasteWizard(targetParent, files, selectedElement);		
		return doPasteIntoFolder(wizard, selectedElement);
	}
	
	private PasteObjectsResult pasteIntoFolder(IDipParent targetParent, IResource[] resources, IDipDocumentElement selectedElement) throws CreateResourceException {		
		PasteWizard wizard = new PasteWizard(targetParent, resources, selectedElement);	
		return doPasteIntoFolder(wizard, selectedElement);
	}
	
	private PasteObjectsResult doPasteIntoFolder(PasteWizard wizard, IDipDocumentElement selectedElement) throws CreateResourceException {
		WizardDialog wd = new WizardDialog(Display.getDefault().getActiveShell(), wizard);
		wd.setTitle(wizard.getWindowTitle());
		if (wd.open() == WizardDialog.OK) {
			PasteElement[] pasteElements = wizard.getCheckedPasteElements();
			int startFileIndex = 0;  // индекс для вставки файлов
			if (wizard.getSelectedElement() != null) {
				startFileIndex = selectedElement.strong().getIndex() + 1;
			} 
			
			Paster paster = new Paster(wizard.getTargetParent(), startFileIndex);
			List<PasteObjectResult> results = paster.paste(pasteElements);
			paster.updateTable();
			paster.selectPastedElements();
			return new PasteObjectsResult(DipUtilities.relativeProjectID(wizard.getTargetParent()), results);
		}	
		return null;
	}
	

	private class Paster {
		
		private IDipParent fTargetDipParent;
		private int fStartIndexFile = 0;
		private int fStartIndexFolder = -1;
				
		private List<IDipDocumentElement> fNewElements = new ArrayList<>();
				
		Paster(IDipParent targetParent, int startIndexFile){
			fTargetDipParent = targetParent;
			fStartIndexFile = startIndexFile;
		}
		
		private List<PasteObjectResult> paste(PasteElement[] pasteElements) throws CreateResourceException{
			List<PasteObjectResult> results = new ArrayList<>();
			for (PasteElement pasteElement: pasteElements) {
				PasteObjectResult result = paste(pasteElement);
				results.add(result);
			}
			return results;
		}
	
		private PasteObjectResult paste(PasteElement element) throws CreateResourceException {
			if (element.isFolder()) {
				return pasteFolder(element);
			} else {
				return pasteFile(element);
			}			
		}
		
		private PasteFolderResult pasteFolder(PasteElement element) throws CreateResourceException {
			IFolder newFolder = copyFolder(element);
			IDipParent newElement = null;
			if (fStartIndexFolder < 0) {
				newElement = DipTableUtilities.addNewFolderStart(fTargetDipParent, newFolder);
				fNewElements.add(newElement);
				fStartIndexFolder = newElement.getIndex() + 1;
			} else {
				newElement = DipTableUtilities.addNewFolderByIndex(fTargetDipParent, newFolder, fStartIndexFolder++);
				fNewElements.add(newElement);
			}
			if (newElement instanceof DipTableContainer) {
				((DipContainer) newElement).computeChildren();			
			}
			return new PasteFolderResult(element.getNewName());
		}
		
		private PasteFileResult pasteFile(PasteElement element) throws CreateResourceException {
			String comment = null;
			String description = null;
			if (element instanceof PasteResourceElement) {
			IDipElement dipElement = DipUtilities.findElement(((PasteResourceElement)element).getResource());
			if (dipElement instanceof IDipDocumentElement) {
				comment = ((ICommentSupport) dipElement).getCommentContent();
				description = ((IDescriptionSupport) dipElement).description();					
			}
			}
			
			IFile newFile = copyFile(element);
			IDipDocumentElement newElement = (IDipDocumentElement) DipTableUtilities.addNewFileByIndex(fTargetDipParent, newFile, fStartIndexFile++);
			if (comment != null && !comment.isEmpty()) {
				newElement.updateDipComment(comment);
			}
			if (description != null && !description.isEmpty()) {
				newElement.setDescription(description);
			}
			
			fNewElements.add((IDipDocumentElement) newElement);
			return new PasteFileResult(element.getNewName());
		}
		
		
		IFile copyFile(PasteElement element) throws CreateResourceException {
			IFile file = fTargetDipParent.resource().getFile(new Path(element.getNewName()));
			try {
				file = ResourcesUtilities.createFile(file, getShell());
				ResourcesUtilities.copyContent(file, element.getPath());
				return file;
			} catch (CoreException | IOException e) {
				e.printStackTrace();
				throw new CreateResourceException(PASTE_FILE_EXCEPTION, e);
			}
		}
		
		IFolder copyFolder(PasteElement element) throws CreateResourceException {
			try {
				IFolder newFolder = fTargetDipParent.resource().getFolder(new Path(element.getNewName()));
				newFolder = ResourcesUtilities.createFolder(newFolder, getShell());
				java.nio.file.Path newFolderPath = Paths.get(newFolder.getLocationURI());
				ResourcesUtilities.copyFolder(element.getPath(), newFolderPath);
				return newFolder;
			} catch (CoreException | IOException e) {
				e.printStackTrace();
				throw new CreateResourceException(PASTE_FILE_EXCEPTION, e);
			}
		}
		
		private void updateTable() {
			fTableComposite.editor().updater().updateParent(fTargetDipParent);
			for (IDipDocumentElement req: fNewElements) {
				if (req instanceof IDipParent) {
					((IDipParent) req).refresh();
					fTableComposite.editor().updater().updateParent((IDipParent) req);
				}
			}
		}
		
		private void selectPastedElements() {
			fTableComposite.selector().selectManyDipDocElementss(fNewElements);
			IDipTableElement element = fTableComposite.tableModel().findElement(fNewElements.get(0));
			if (element != null) {
				fTableComposite.selector().setTopItemElement(element);
			}
		}
		
	}

	//====================
	// undo
	
	public DeleteResultOperation undoCreate(PasteObjectsResult result) throws CreateResourceException {
		// delete file
		Optional<IDipElement> optParent = DipUtilities.findDipElementInProject(result.getParentId(),
				fTableComposite.dipProject());
		if (optParent.isEmpty()) {
			throw new CreateResourceException(UNDO_PASTE_FILE_EXCEPTION);
		}
		IDipParent parent = (IDipParent) optParent.get();		
		IDipDocumentElement[] dipDocElements = result.elementNames()
				.map(name ->  (IDipDocumentElement) parent.getChild(name))
				.toArray(IDipDocumentElement[]::new);
		if (dipDocElements == null || dipDocElements.length == 0) {
			throw new CreateResourceException(UNDO_PASTE_FILE_EXCEPTION);
		}

		try {
			DeleteResultOperation deleteResult = doDelete(dipDocElements);
			fTableComposite.editor().updater().updateAfterDelete(dipDocElements);
			return deleteResult;
		} catch (DIPException e) {
			e.printStackTrace();
			throw new CreateResourceException(UNDO_PASTE_FILE_EXCEPTION, e);
		}
	}
	
	private DeleteResultOperation doDelete(IDipDocumentElement[] dipDocElements) throws TmpCopyException, DeleteDIPException {
		return DipUtilities.deleteElements(dipDocElements, false, false, getShell());
	}
	
	//=========================
	// utitlites & getters
		
	private boolean isOpenCellEditor() {
		return fTableComposite.table().getCellEditor() != null;
	}
	
	private boolean isFilterEntryHasFocus() {
		FilterToolBar filterToolBar = fTableComposite.editor().getButtonManager().getFilterToolBar();
		return filterToolBar != null && filterToolBar.isFocus();
	}
	
	private Shell getShell() {
		return fTableComposite.getShell();
	}

	public void dispose() {
		fClipboard.dispose();
	}

}
