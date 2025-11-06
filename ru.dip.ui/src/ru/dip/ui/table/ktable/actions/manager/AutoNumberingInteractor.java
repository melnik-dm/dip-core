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

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import ru.dip.core.exception.DIPException;
import ru.dip.core.exception.RenameDIPException;
import ru.dip.core.exception.SaveTableDIPException;
import ru.dip.core.model.DipProject;
import ru.dip.core.model.interfaces.IDipElement;
import ru.dip.core.model.interfaces.IDipParent;
import ru.dip.core.model.interfaces.IDipDocumentElement;
import ru.dip.core.utilities.DipUtilities;
import ru.dip.core.utilities.WorkbenchUtitlities;
import ru.dip.core.utilities.tmp.ApplyAutoNumberingResult;
import ru.dip.core.utilities.tmp.AutoNumberingSettingResult;
import ru.dip.core.utilities.tmp.ApplyAutoNumberingResult.RenamedElement;
import ru.dip.ui.table.ktable.KDipTableSelector;
import ru.dip.ui.table.ktable.KTableComposite;
import ru.dip.ui.table.ktable.dialog.ApplyNumberingDialog;
import ru.dip.ui.table.ktable.dialog.ApplyNumberingDialog.RenamingElement;
import ru.dip.ui.table.ktable.model.DipTableModel;
import ru.dip.ui.table.ktable.model.TableNode;

public class AutoNumberingInteractor {
	
	private KTableComposite fTableComposite;
	
	public AutoNumberingInteractor(KTableComposite composite) {
		fTableComposite = composite;
	}
	
	public ApplyAutoNumberingResult doRenameWithDialog() throws RenameDIPException {
		AutoRenamer renamer = new AutoRenamer();
		renamer.setParentFromSelect();
		if (renamer.openApplyNumberingDialog()) {
			renamer.doRenameWithCursorBusy();
			renamer.updateAfterRename();
			return renamer.getResult();
		}
		return null;
	}
	
	public void undoRedoRename(ApplyAutoNumberingResult result, boolean undo) throws DIPException {
		//AutoRenamer renamer()
		AutoRenamer renamer = new AutoRenamer();
		renamer.setInputFromResult(result, undo);
		renamer.doUndoRenameWithCursorBusy();
		renamer.updateAfterRename();
	}
	
	private class AutoRenamer {
		
		private IDipParent fParent;
		private String fOldFileStep;
		private String fOldFolderStep;
		private String fFileStep;
		private String fFolderStep;
		private List<RenamingElement> fRenamingElements;		
		// from result

		private Map<IDipDocumentElement, String> fNewNameByDipDocElement = new HashMap<>();
		
		private void setParentFromSelect() {
			fParent = (IDipParent) selector().getSelectedOneDipDocElement();
			fOldFileStep = fParent.getFileStep();
			fOldFolderStep = fParent.getFolderStep();
		}
							
		private boolean openApplyNumberingDialog() {
			ApplyNumberingDialog dialog = new ApplyNumberingDialog(getShell(), fParent);
			if (dialog.open() == Dialog.OK) {				
				fFileStep = dialog.getFileStep();
				fFolderStep = dialog.getFolderStep();
				fRenamingElements = dialog.getRenamingElements();				
				return true;
			}
			return false;
		}
		
		//===================================
		// auto-renaming
		
		private void doRenameWithCursorBusy() throws RenameDIPException {
			try {
				WorkbenchUtitlities.runWithCursorBusy(getRunnableWithProgress());
			} catch (InvocationTargetException | InterruptedException e) {
				e.printStackTrace();				
				throw new RenameDIPException("Rename Error");
			}
		}
		
		private IRunnableWithProgress getRunnableWithProgress() {
			return new IRunnableWithProgress() {
				
				@Override
				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					doRename();
				}
			};
		}
					
		private void doRename() {
			Display.getDefault().asyncExec(() -> {
				try {
					tmpRename();
					rename();
					fParent.setFileStep(fFileStep);
					fParent.setFolderStep(fFolderStep);
				} catch (RenameDIPException | SaveTableDIPException e) {
					e.printStackTrace();
					WorkbenchUtitlities.openError("AutoNumbering Error", e.getMessage());
				}	
			});
		}
		
		/*
		 * Чтобы не было совпадения имен
		 */
		private void tmpRename() throws RenameDIPException, SaveTableDIPException {
			for (int i = 0; i < fRenamingElements.size(); i++) {
				RenamingElement element = fRenamingElements.get(i);
				DipUtilities.lightRenameElement(element.getDipDocElement(), "already_existd___ " + i, false,
							getShell());
			}
		}
		
		private void rename() throws RenameDIPException, SaveTableDIPException {
			for (int i = 0; i < fRenamingElements.size(); i++) {
				RenamingElement element = fRenamingElements.get(i);
				DipUtilities.renameElement(element.getDipDocElement(), element.getNewName(), false, getShell());
			}
		}
		
		//========================
		// undo -redo
		
		private void setInputFromResult(ApplyAutoNumberingResult result, boolean undo) throws DIPException {
			Optional<IDipElement> parentOpt = DipUtilities.findDipElementInProject(result.getSettingResult().getParentId(), dipProject());
			if (parentOpt.isEmpty()) {
				throw new DIPException("Element not found.");
			}			
			fParent = (IDipParent) parentOpt.get();
			
			for (RenamedElement element: result.getRenamedElements()) {
				String name = undo ? element.getNewName() : element.getOldName(); 
				String newName = undo ? element.getOldName() : element.getNewName(); 
				IDipElement dipElement = fParent.getChild(name);
				if (dipElement == null) {
					throw new DIPException("Element not found.");
				}
				fNewNameByDipDocElement.put((IDipDocumentElement)dipElement, newName);
			}		
			
			if (undo) {
				fFileStep = result.getSettingResult().getOldFileStep();
				fFolderStep = result.getSettingResult().getOldFileStep();				
			} else {
				fFileStep = result.getSettingResult().getNewFileStep();
				fFolderStep = result.getSettingResult().getNewFileStep();	
			}
			
		}
		
		private void doUndoRenameWithCursorBusy() throws RenameDIPException {
			try {
				WorkbenchUtitlities.runWithCursorBusy(getUndoRunnableWithProgress());
			} catch (InvocationTargetException | InterruptedException e) {
				e.printStackTrace();				
				throw new RenameDIPException("Rename Error");
			}
		}
		
		private IRunnableWithProgress getUndoRunnableWithProgress() {
			return new IRunnableWithProgress() {
				
				@Override
				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					doUndoRename();
				}
			};
		}
			
		private void doUndoRename() {
			Display.getDefault().asyncExec(() -> {
				try {
					undoRedoRename();
					fParent.setFileStep(fFileStep);
					fParent.setFolderStep(fFolderStep);
				} catch (RenameDIPException | SaveTableDIPException e) {
					e.printStackTrace();
					WorkbenchUtitlities.openError("AutoNumbering Error", e.getMessage());
				}
			});
		}
		
		private void undoRedoRename() throws RenameDIPException, SaveTableDIPException {
			int n = 0;
			
			for (Entry<IDipDocumentElement, String> entry: fNewNameByDipDocElement.entrySet()) {							
				DipUtilities.lightRenameElement(entry.getKey(), "undo_already_existd___ " + n++, false, getShell());
			}
			for (Entry<IDipDocumentElement, String> entry: fNewNameByDipDocElement.entrySet()) {	
				DipUtilities.renameElement(entry.getKey(), entry.getValue(), false, getShell());
			}		
		}
	
		private void updateAfterRename() throws RenameDIPException {
			Optional<TableNode> nodeOpt = tableModel().findNodeByName(fParent);
			if (nodeOpt.isEmpty()) {
				throw new RenameDIPException("Not found Node");
			}
			fTableComposite.editor().updater().updateParent(fParent);
			fTableComposite.selector().setSelection(nodeOpt.get());			
		}
		
		private ApplyAutoNumberingResult getResult() {
			AutoNumberingSettingResult settingResult = new AutoNumberingSettingResult(
					DipUtilities.relativeProjectID(fParent), fOldFileStep, fOldFolderStep, fFileStep, fFolderStep);			
			List<RenamedElement> renamedElements = fRenamingElements.stream()
			.map(renaming -> new RenamedElement(renaming.getOldName(), renaming.getNewName()))
			.collect(Collectors.toList());
			
			return new ApplyAutoNumberingResult(settingResult, renamedElements);
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
	
	private DipTableModel tableModel() {
		return fTableComposite.tableModel();
	}
	
}
