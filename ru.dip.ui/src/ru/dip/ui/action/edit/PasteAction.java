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
package ru.dip.ui.action.edit;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.actions.CopyFilesAndFoldersOperation;
import org.eclipse.ui.actions.CopyProjectOperation;
import org.eclipse.ui.actions.SelectionListenerAction;
import org.eclipse.ui.part.ResourceTransfer;

import ru.dip.core.model.DipSchemaElement;
import ru.dip.core.model.DipSchemaFolder;
import ru.dip.core.model.glossary.GlossaryField;
import ru.dip.core.model.glossary.GlossaryFolder;
import ru.dip.core.model.interfaces.IDipDocumentElement;
import ru.dip.core.model.interfaces.IDipParent;
import ru.dip.core.model.reports.ProjectReportFolder;
import ru.dip.core.model.reports.Report;
import ru.dip.core.model.vars.VarContainer;
import ru.dip.core.model.vars.Variable;
import ru.dip.core.utilities.DipTableUtilities;
import ru.dip.core.utilities.DipUtilities;
import ru.dip.core.utilities.ResourcesUtilities;
import ru.dip.core.utilities.WorkbenchUtitlities;
import ru.dip.core.utilities.ui.swt.FontManager;
import ru.dip.ui.Messages;
import ru.dip.ui.dialog.ErrorDialog;
import ru.dip.ui.glossary.GlossaryBuffer;
import ru.dip.ui.table.editor.DipEditorUpdater;
import ru.dip.ui.utilities.image.ImageProvider;
import ru.dip.ui.variable.dialog.VarMessagesDialogs;
import ru.dip.ui.variable.utils.VariableBuffer;

public class PasteAction extends SelectionListenerAction {

	private Shell fShell;
	private Clipboard fClipboard;
	private IDipParent fTargetParent;
	private IContainer fTargetResource;
	private ProjectReportFolder fReportFolder;
	
	private GlossaryBuffer fGlossaryBuffer;
	private GlossaryFolder fGlossaryFolder;
	
	private VariableBuffer fVariableBuffer;
	private VarContainer fVarContainer;
	
	private DipSchemaFolder fSchemaFolder;
	

	protected PasteAction(Shell shell, Clipboard clipboard) {
		super(Messages.PasteAction_Action_name);
		fClipboard = clipboard;
		fShell = shell;
		fGlossaryBuffer = GlossaryBuffer.getInstance();
		fVariableBuffer = VariableBuffer.getInstance();
	}

	@Override
	protected boolean updateSelection(IStructuredSelection selection) {			
		if (!super.updateSelection(selection)) {
			return false;
		}
		
		fTargetParent = null;
		fTargetResource = null;
		fReportFolder = null;
		fSchemaFolder = null;
				
		// если выделена папка с отчетами
		fReportFolder = getReportFolder(selection);
		if (fReportFolder != null) {
			IResource[] resourceData = getResourceFormClipboard();
			if (isAllReports(resourceData)) {
				fTargetParent = getSelection(selection);
				fTargetResource = fReportFolder.resource();
				return true;
			} else {
				return false;
			}			
		}
			
		// если выделен глоссарий
		fGlossaryFolder = getGlossaryFolder(selection);
		if (fGlossaryFolder != null){
			return !fGlossaryBuffer.isEmpty();
		}
		
		// если выделена папка со схемами
		fSchemaFolder = getSchemaFolder(selection);
		if (fSchemaFolder != null) {
			fTargetResource = fSchemaFolder.resource();
			IResource[] resourceData = getResourceFormClipboard();
			if (resourceData != null) {
				return checkResourceData(resourceData);
			} else {
				return checkFileTransferData();
			}
		}
		
		// если выделена папка с переменными
		fVarContainer = getVarContainer(selection);
		if (fVarContainer != null) {
			return !fVariableBuffer.isEmpty();
		}
				

		IResource[] resourceData = getResourceFormClipboard();
		
		// если вставляем проекты
		if (isFirstProjectRes(resourceData)) {
			return isAllOpenProjects(resourceData);
		}
		
		// если не ресурсы
		if (getSelectedNonResources().size() > 0) {
			return false;
		}
		
		// выделенный объект (можно ли туда вставлять)
		if (!setTarget(selection)) {
			return false;
		}
		
		
		if (resourceData != null) {
			return checkResourceData(resourceData);
		} else {
			return checkFileTransferData();
		}
	}
	

	/**
	 * Список ресурсов из буфера
	 */
	private IResource[] getResourceFormClipboard() {
		final IResource[][] clipboardData = new IResource[1][];
		fShell.getDisplay().syncExec(new Runnable() {
			@Override
			public void run() {
				// clipboard must have resources or files
				ResourceTransfer resTransfer = ResourceTransfer.getInstance();
				clipboardData[0] = (IResource[]) fClipboard.getContents(resTransfer);
			}
		});
		return clipboardData[0];
	}

	private boolean isFirstProjectRes(IResource[] resourceData) {
		return resourceData != null 
				&& resourceData.length > 0
				&& resourceData[0].getType() == IResource.PROJECT;
	}
	
	private boolean isAllOpenProjects(IResource[] resourceData) {
		for (IResource resoruce: resourceData) {
			if (resoruce.getType() != IResource.PROJECT
					|| ((IProject) resoruce).isOpen() == false) {
				return false;
			}
		}
		return true;
	}
	
	private boolean isAllReports(IResource[] resourceData) {
		if (resourceData == null) {
			return false;
		}
		
		for (IResource res : resourceData) {
			boolean checkRes = (res instanceof IFile
					&& Report.REPORT_EXTENSION.equals(((IFile) res).getFileExtension()));
			if (!checkRes) {
				return false;
			}

		}
		return true;
	}

	/**
	 * Получаем targetParent и targetResource
	 * true - если всё в порядке
	 * false - если:
	 * 		- target parent = null или readonly
	 * 		- target resource = null
	 * 		- если выделено много объектов (допускается выделение нескольких файлов внутри одной папки)
	 */
	private boolean setTarget(IStructuredSelection selection) {
		fTargetParent = getSelection(selection);
		if (fTargetParent == null) {
			return false;
		}
		
		if (fTargetParent.isReadOnly()) {
			return false;
		}
		
		fTargetResource = fTargetParent.resource();
		// targetResource is null if no valid target is selected (e.g., open project) or selection is empty
		if (fTargetResource == null) {
			return false;
		}
		// can paste files and folders to a single selection (file, folder,
		// open project) or multiple file selection with the same parent
		List<? extends IResource> selectedResources = getSelectedResources();
				
		if (selectedResources.size() > 1) {
			for (IResource resource : selectedResources) {
				if (resource.getType() != IResource.FILE) {
					return false;
				}
				if (!fTargetResource.equals(resource.getParent())) {
					return false;
				}
			}
		}
		return true;
	}
	
	/**
	 * Проверка ресурсов (ResourceTransfer)
	 */
	private boolean checkResourceData(IResource[] resourceData) {
		// linked resources can only be pasted into projects
		if (isLinked(resourceData)) {
			return false;
		}

		if (fTargetResource.getType() == IResource.FOLDER) {
			// don't try to copy folder to self
			for (int i = 0; i < resourceData.length; i++) {
				if (fTargetResource.equals(resourceData[i])) {
					return false;
				}
			}
		}
		return true;
	}
	
	/**
	 * Проверка объектов (FileTransfer)
	 */
	private boolean checkFileTransferData() {
		TransferData[] transfers = fClipboard.getAvailableTypes();
		FileTransfer fileTransfer = FileTransfer.getInstance();
		for (int i = 0; i < transfers.length; i++) {
			if (fileTransfer.isSupportedType(transfers[i])) {
				return true;
			}
		}
		return false;
	}
	
	
	//=========================
	// run

	@Override
	public void run() {
		doRun();
		if (fTargetResource != null){
			ResourcesUtilities.updateProject(fTargetResource);
		} else if (fGlossaryFolder != null) {
			ResourcesUtilities.updateProject(fGlossaryFolder.parent().resource());
		}
		WorkbenchUtitlities.updateProjectExplorer();
		if (fTargetParent != null) {
			fTargetParent.dipProject().updateNumeration();
			DipEditorUpdater.updateNewResInFolder(fTargetParent);
		}
	}

	public void doRun() {
		// try glossary transfer
		if (fGlossaryFolder != null){
			doGlossaryTransfer();
			return;
		}
		// try variables transfer
		if (fVarContainer != null) {
			doVariableTransfer();
			return;
		}
		
		ResourceTransfer resTransfer = ResourceTransfer.getInstance();
		IResource[] resourceData = (IResource[]) fClipboard.getContents(resTransfer);
		if (resourceData != null && resourceData.length > 0) {
			// try a resource transfer
			doRunResourceTransfer(resourceData);
		} else {
			// try a file transfer
			doRunFileTransfer();
		}
		saveTable();
	}
	
	private void saveTable() {
		if (fTargetParent != null) {
			DipTableUtilities.saveModel(fTargetParent);
		}
	}
	
	
	//========================
	// Report paste
	
	private ProjectReportFolder getReportFolder(IStructuredSelection selection) {
		if (selection != null){
			Object obj = selection.getFirstElement();
			if (obj instanceof ProjectReportFolder){
				return (ProjectReportFolder) obj;
			}
		}
		return null;
	}
		
	//=========================
	// Schema paste
	
	private DipSchemaFolder getSchemaFolder(IStructuredSelection selection){
		if (selection != null){
			Object obj = selection.getFirstElement();
			if (obj instanceof DipSchemaFolder){
				return (DipSchemaFolder) obj;
			} else if (obj instanceof DipSchemaElement){
				DipSchemaElement field = (DipSchemaElement) obj;
				return field.parent();
			}
		}	
		return null;
	}
	
	//=========================
	// Glossary paste
	
	private GlossaryFolder getGlossaryFolder(IStructuredSelection selection){
		if (selection != null){
			Object obj = selection.getFirstElement();
			if (obj instanceof GlossaryFolder){
				return (GlossaryFolder) obj;
			} else if (obj instanceof GlossaryField){
				GlossaryField field = (GlossaryField) obj;
				return field.parent();
			}
		}	
		return null;
	}
	
	private void doGlossaryTransfer(){
		if (fGlossaryFolder == null || fGlossaryBuffer.isEmpty()){
			return;
		}
		int forAll = -1;
		List<GlossaryField> fAddField = new ArrayList<>();
		List<GlossaryField> fChangeField = new ArrayList<>();
		for(GlossaryField field: fGlossaryBuffer.getFields()){
			GlossaryField containedField = fGlossaryFolder.getChild(field.name());
			if (containedField != null){
				if (forAll ==  -1){
					ChangeFieldDialog dialog = new ChangeFieldDialog(fShell, field.name());			
					int result = dialog.open();
					if (result == IDialogConstants.CANCEL_ID){
						return;
					} else if (result == IDialogConstants.OK_ID){
						fChangeField.add(field);
						if (dialog.fForAll){
							forAll = IDialogConstants.OK_ID;
						}
					} else if (result == IDialogConstants.IGNORE_ID){					
						if (dialog.fForAll){
							forAll = IDialogConstants.IGNORE_ID;
						}						
					}
				} else if (forAll == IDialogConstants.OK_ID){
					fChangeField.add(field);
				}
				
			} else {
				fAddField.add(field);
			}			
		}
		try {
			fGlossaryFolder.pasteFields(fAddField, fChangeField);
		} catch (IOException e) {			
			MessageDialog.openError(fShell, Messages.PasteAction_Glossary_error_title, Messages.PasteAction_Glossary_error_message);
			e.printStackTrace();
		}
	}
	
	private class ChangeFieldDialog extends Dialog {

		private String fFieldName;
		private boolean fForAll = false;
		
		
		protected ChangeFieldDialog(Shell parentShell, String name) {
			super(parentShell);
			fFieldName = name;
		}
		
		@Override
		protected Point getInitialSize() {
			Point p = super.getInitialSize();
			if (p.x < 600){
				p.x = 600;
			}		
			return p;
		}
		
		@Override
		protected void configureShell(Shell newShell) {			
			super.configureShell(newShell);
			newShell.setText(Messages.PasteAction_Paste_glossary_entry_title);
		}
		
		@Override
		protected Control createDialogArea(Composite parent) {
			Composite composite = new Composite(parent, SWT.NONE);
			composite.setLayout(new GridLayout());
			composite.setLayoutData(new GridData(GridData.FILL_BOTH));
			Composite labelComposite = new Composite(composite, SWT.NONE);
			labelComposite.setLayout(new GridLayout(3, false));
			Label imageLabel = new Label(labelComposite, SWT.NONE);
			imageLabel.setImage(ImageProvider.WARNING);
			
			Label label = new Label(labelComposite, SWT.NONE);
			label.setText(Messages.PasteAction_Glossary_already_contains_label);
			label.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_CENTER));
			
			Label fieldNameLabel = new Label(labelComposite, SWT.NONE);
			fieldNameLabel.setText(fFieldName);
			fieldNameLabel.setFont(FontManager.boldFont);
			fieldNameLabel.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_CENTER));
			new Label(composite, SWT.NONE);
			Button button = new Button(composite, SWT.CHECK);
			button.setText(Messages.PasteAction_Aply_for_all);
			GridData gd = new GridData();
			gd.horizontalIndent = 4;
			button.setLayoutData(gd);
			button.addSelectionListener(new SelectionAdapter() {
				
				@Override
				public void widgetSelected(SelectionEvent e) {
					fForAll = button.getSelection();
				}
			});
			return composite;
		}
		
		@Override
		protected void createButtonsForButtonBar(Composite parent) {
			createButton(parent, IDialogConstants.IGNORE_ID, Messages.PasteAction_Skip_button,
					true);
			createButton(parent, IDialogConstants.CANCEL_ID, Messages.PasteAction_Cancel_button,
					true);
			createButton(parent, IDialogConstants.OK_ID, Messages.PasteAction_Replace_button,
					true);
		}
		
		@Override
		protected void buttonPressed(int buttonId) {
			setReturnCode(buttonId);
			close();			
		}
		
	}

	// ======================================
	// variable paste
	
	private VarContainer getVarContainer(IStructuredSelection selection){
		if (selection != null){
			Object obj = selection.getFirstElement();
			if (obj instanceof VarContainer){
				return (VarContainer) obj;
			} else if (obj instanceof Variable){
				Variable variable = (Variable) obj;
				return variable.parent();
			}
		}	
		return null;
	}

	private void doVariableTransfer() {
		if (fVarContainer == null || fVariableBuffer.isEmpty()) {
			return;
		}
		int forAll = -1;
		List<Variable> fAddField = new ArrayList<>();
		List<Variable> fChangeField = new ArrayList<>();
		for (Variable field : fVariableBuffer.getVariables()) {
			Variable containedField = fVarContainer.getChild(field.name());
			if (containedField != null) {
				if (forAll == -1) {
					ChangeVariableDialog dialog = new ChangeVariableDialog(fShell, field.name());
					int result = dialog.open();
					if (result == IDialogConstants.CANCEL_ID) {
						return;
					} else if (result == IDialogConstants.OK_ID) {
						fChangeField.add(field);
						if (dialog.fForAll) {
							forAll = IDialogConstants.OK_ID;
						}
					} else if (result == IDialogConstants.IGNORE_ID) {
						if (dialog.fForAll) {
							forAll = IDialogConstants.IGNORE_ID;
						}
					}
				} else if (forAll == IDialogConstants.OK_ID) {
					fChangeField.add(field);
				}

			} else {
				fAddField.add(field);
			}
		}
		try {
			fVarContainer.pasteVariables(fAddField, fChangeField);
		} catch (IOException e) {
			VarMessagesDialogs.showSaveContainerError(fVarContainer, fShell);
			e.printStackTrace();
		}
	}

	private class ChangeVariableDialog extends Dialog {

		private String fFieldName;
		private boolean fForAll = false;

		protected ChangeVariableDialog(Shell parentShell, String name) {
			super(parentShell);
			fFieldName = name;
		}

		@Override
		protected Point getInitialSize() {
			Point p = super.getInitialSize();
			if (p.x < 600) {
				p.x = 600;
			}
			return p;
		}

		@Override
		protected void configureShell(Shell newShell) {
			super.configureShell(newShell);
			newShell.setText("Paste Variable");
		}

		@Override
		protected Control createDialogArea(Composite parent) {
			Composite composite = new Composite(parent, SWT.NONE);
			composite.setLayout(new GridLayout());
			composite.setLayoutData(new GridData(GridData.FILL_BOTH));
			Composite labelComposite = new Composite(composite, SWT.NONE);
			labelComposite.setLayout(new GridLayout(3, false));
			Label imageLabel = new Label(labelComposite, SWT.NONE);
			imageLabel.setImage(ImageProvider.WARNING);

			Label label = new Label(labelComposite, SWT.NONE);
			label.setText(Messages.PasteAction_Variable_already_contains_label);
			label.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_CENTER));

			Label fieldNameLabel = new Label(labelComposite, SWT.NONE);
			fieldNameLabel.setText(fFieldName);
			fieldNameLabel.setFont(FontManager.boldFont);
			fieldNameLabel.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_CENTER));
			new Label(composite, SWT.NONE);
			Button button = new Button(composite, SWT.CHECK);
			button.setText(Messages.PasteAction_Aply_for_all);
			GridData gd = new GridData();
			gd.horizontalIndent = 4;
			button.setLayoutData(gd);

			button.addSelectionListener(new SelectionAdapter() {

				@Override
				public void widgetSelected(SelectionEvent e) {
					fForAll = button.getSelection();
				}

			});
			return composite;
		}

		@Override
		protected void createButtonsForButtonBar(Composite parent) {
			createButton(parent, IDialogConstants.IGNORE_ID, Messages.PasteAction_Skip_button, true);
			createButton(parent, IDialogConstants.CANCEL_ID, Messages.PasteAction_Cancel_button, true);
			createButton(parent, IDialogConstants.OK_ID, Messages.PasteAction_Replace_button, true);
		}

		@Override
		protected void buttonPressed(int buttonId) {
			setReturnCode(buttonId);
			close();
		}

	}

	//=====================================
	// ResourceTransfer
	
	private void doRunResourceTransfer(IResource[] resourceData){
		if (resourceData[0].getType() == IResource.PROJECT) {
			// enablement checks for all projects
			doPasteProjects(resourceData);
		} else {
			// enablement should ensure that we always have access to a
			// container
			if (fTargetParent != null) {
				doPasteFiles(resourceData);
				DipTableUtilities.saveModel(fTargetParent);
			} else if (fReportFolder != null) {
				doPasteReports(resourceData);
			} else if (fSchemaFolder != null) {
				doPasteSchemas(resourceData);
			}
		}
	}
	
	private void doPasteProjects(IResource[] resourceData){
		for (int i = 0; i < resourceData.length; i++) {
			CopyProjectOperation operation = new CopyProjectOperation(fShell);
			operation.copyProject((IProject) resourceData[i]);
		}
	}
	
	private void doPasteFiles(IResource[] resourceData){
		IStatus status = DipUtilities.canPaste(fTargetParent, resourceData);
		if (!status.isOK()) {
			ErrorDialog.openMoveErorrMessage(fShell, status.getMessage());
			return;
		}
		IContainer container = getContainer();
		CopyFilesAndFoldersOperation operation = new CopyFilesAndFoldersOperation(fShell);
		
		// обновить ресурсы (иначе может быть внутренняя ошибка Eclipse синхронизации ресурсов)
		for (IResource res: resourceData) {
			try {
				res.refreshLocal(IResource.DEPTH_INFINITE, null);
			} catch (CoreException e) {
				e.printStackTrace();
			}
		}
		IResource[] resources = operation.copyResources(resourceData, container);
		for (int i = resources.length - 1; i >= 0; i--) {
			IResource res = resources[i];
			if (res instanceof IFile) {
				IFile file = fTargetResource.getFile(new Path(res.getName()));
				fTargetParent.createNewUnit(file);
			} else if (res instanceof IFolder) {
				IFolder folder = fTargetResource.getFolder(new Path(res.getName()));								
				if (!DipUtilities.isNotDnfo(folder)) {
					fTargetParent.createNewFolder(folder);
				}
			}
		}
	}
	
	private void doPasteReports(IResource[] resourceData){
		IStatus status = DipUtilities.canPasteReport(fReportFolder, resourceData);
		if (!status.isOK()) {
			ErrorDialog.openMoveErorrMessage(fShell, status.getMessage());
			return;
		}
		IContainer container = fReportFolder.resource();
		CopyFilesAndFoldersOperation operation = new CopyFilesAndFoldersOperation(fShell);
		IResource[] resources = operation.copyResources(resourceData, container);
		for (int i = resources.length - 1; i >= 0; i--) {
			IResource res = resources[i];
			if (res instanceof IFile) {
				IFile file = fTargetResource.getFile(new Path(res.getName()));
				fReportFolder.loadReport(file);
			} 	
		}
	}
	
	private void doPasteSchemas(IResource[] resourceData){
		IStatus status = DipUtilities.canPasteSchema(fSchemaFolder, resourceData);
		if (!status.isOK()) {
			ErrorDialog.openMoveErorrMessage(fShell, status.getMessage());
			return;
		}
		IContainer container = fSchemaFolder.resource();
		CopyFilesAndFoldersOperation operation = new CopyFilesAndFoldersOperation(fShell);
		IResource[] resources = operation.copyResources(resourceData, container);
		for (int i = resources.length - 1; i >= 0; i--) {
			IResource res = resources[i];
			if (res instanceof IFile) {
				IFile file = fTargetResource.getFile(new Path(res.getName()));
				fSchemaFolder.createSchema(file);
			} 	
		}
	}
			
	//=====================================
	// FileTransfer
	
	private void doRunFileTransfer(){
		FileTransfer fileTransfer = FileTransfer.getInstance();
		String[] fileData = (String[]) fClipboard.getContents(fileTransfer);
		if (fileData != null) {
			if (fTargetParent != null) {
				IStatus status = DipUtilities.canPaste(fTargetParent, fileData);
				if (!status.isOK()) {
					ErrorDialog.openMoveErorrMessage(fShell, status.getMessage());
					return;
				}
				// enablement should ensure that we always have access to a
				// container
				IContainer container = getContainer();
				CopyFilesAndFoldersOperation operation = new CopyFilesAndFoldersOperation(fShell);
				operation.copyFiles(fileData, container);

				for (String filePath : fileData) {
					java.nio.file.Path path = Paths.get(filePath);
					if (Files.isDirectory(path)) {
						IFolder folder = fTargetResource.getFolder(new Path(path.getFileName().toString()));
						if (!DipUtilities.isNotDnfo(folder)) {
							fTargetParent.createNewFolder(folder);
						}
					} else {
						IFile file = fTargetResource.getFile(new Path(path.getFileName().toString()));
						fTargetParent.createNewUnit(file);
					}
				}
			} else if (fReportFolder != null) {
				IStatus status = DipUtilities.canPasteReport(fReportFolder, fileData);
				if (!status.isOK()) {
					ErrorDialog.openMoveErorrMessage(fShell, status.getMessage());
					return;
				}
				// enablement should ensure that we always have access to a
				// container
				IContainer container = getContainer();
				CopyFilesAndFoldersOperation operation = new CopyFilesAndFoldersOperation(fShell);
				operation.copyFiles(fileData, container);

				for (String filePath : fileData) {
					java.nio.file.Path path = Paths.get(filePath);
					if (Files.isDirectory(path)) {
						// IFolder folder = fTargetResource.getFolder(new
						// Path(path.getFileName().toString()));
						// fTargetParent.createNewFolder(folder);
					} else {
						IFile file = fTargetResource.getFile(new Path(path.getFileName().toString()));
						fReportFolder.loadReport(file);
					}
				}
			} else if (fSchemaFolder != null) {
				IStatus status = DipUtilities.canPasteSchema(fSchemaFolder, fileData);
				if (!status.isOK()) {
					ErrorDialog.openMoveErorrMessage(fShell, status.getMessage());
					return;
				}
				// enablement should ensure that we always have access to a
				// container
				IContainer container = getContainer();
				CopyFilesAndFoldersOperation operation = new CopyFilesAndFoldersOperation(fShell);
				operation.copyFiles(fileData, container);

				for (String filePath : fileData) {
					java.nio.file.Path path = Paths.get(filePath);
					if (!Files.isDirectory(path)) {
						IFile file = fTargetResource.getFile(new Path(path.getFileName().toString()));
						fSchemaFolder.createSchema(file);
					}
				}
			}
		}
	}

	private IContainer getContainer() {
		if (fTargetParent != null) {
			return fTargetParent.resource();
		}
		if (fReportFolder != null) {
			return fReportFolder.resource();
		}
		return null;
	}

	private IDipParent getSelection(IStructuredSelection selection) {
		if (!selection.isEmpty() && selection.size() == 1) {
			Object object = selection.getFirstElement();
			if (object instanceof IDipParent) {
				return (IDipParent) object;
			}
			if (object instanceof IDipDocumentElement) {
				return ((IDipDocumentElement) object).parent();
			}
		}
		return null;
	}

	private boolean isLinked(IResource[] resources) {
		for (int i = 0; i < resources.length; i++) {
			if (resources[i].isLinked()) {
				return true;
			}
		}
		return false;
	}

}
