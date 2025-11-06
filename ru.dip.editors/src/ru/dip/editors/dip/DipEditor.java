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
package ru.dip.editors.dip;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.ide.FileStoreEditorInput;
import org.eclipse.ui.part.FileEditorInput;

import ru.dip.core.manager.DipNatureManager;
import ru.dip.core.model.DipProject;
import ru.dip.core.model.DipRoot;
import ru.dip.core.utilities.DipUtilities;
import ru.dip.editors.Messages;
import ru.dip.ui.action.hyperlink.ReqLink;

public class DipEditor extends TextEditor {

	public DipEditor() {
		
	}

	@Override
	public void doSave(IProgressMonitor monitor) {

	}

	@Override
	public void doSaveAs() {

	}

	@Override
	public void init(IEditorSite site, IEditorInput input) throws PartInitException {
		// открываем таблицу
		Path projectPath = getProjectPath(input);
		if (projectPath != null) {
			openTable(projectPath, site);
		}
		// закрываем себя
		IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				page.closeEditor(DipEditor.this, false);
			}
		});			
		super.init(site, input);
	}
	
	private Path getProjectPath(IEditorInput input) {
		if (input instanceof FileStoreEditorInput) {
			FileStoreEditorInput filestore = (FileStoreEditorInput) input;
			Path path = Paths.get(filestore.getURI());
			return path.getParent();
		} else if (input instanceof IFileEditorInput) {		
			FileEditorInput fileEditorInput = (FileEditorInput) input;
			Path path = Paths.get(fileEditorInput.getFile().getLocationURI());
			return path.getParent();		
		}
		return null;
	}
	
	private void openTable(Path projectPath, IEditorSite site) {
		String projectName = projectPath.getFileName().toString();
		IProject project = getProject(projectName);
		if (project == null) {
			// нет проекта с таким именем в workspace
			if (isDIPProject(projectPath)) {
				DipProject dipProject = DipUtilities.importDipProject(projectName, 
						projectPath.toString(), false, site.getShell());
				ReqLink.openTable(dipProject.getTable());
			} else {
				MessageDialog.openError(site.getShell(), Messages.DipEditor_OpenDIPErrorTitle, projectName + Messages.DipEditor_NotDipProjectMessage);
			}
		} else {
			// есть проект с таким именем в workspace
			Path workspaceProjectPath = Paths.get(project.getLocationURI());
			if (projectPath.equals(workspaceProjectPath)) {
				if (DipNatureManager.hasNature(project)) {
					DipProject dipProject = DipRoot.getInstance().getDipProject(project);
					ReqLink.openTable(dipProject.getTable());
				} else {
					MessageDialog.openError(site.getShell(), Messages.DipEditor_OpenDIPErrorTitle,
							projectName + Messages.DipEditor_NotDipProjectMessage);
				}
			} else {
				// уже есть другой проект с таким именем
				String newName = openNewNameDialog(site.getShell(), projectName);
				if (newName != null){
					// нет проекта с таким именем в workspace
					if (isDIPProject(projectPath)) {
						DipProject dipProject = DipUtilities.importDipProject(newName, 
								projectPath.toString(), false, site.getShell());
						ReqLink.openTable(dipProject.getTable());
					} else {
						MessageDialog.openError(site.getShell(), Messages.DipEditor_OpenDIPErrorTitle, projectName + Messages.DipEditor_NotDipProjectMessage);
					}
				}
			}
		}
	}

	private IProject getProject(String name) {
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();		
		for (IProject project: root.getProjects()) {
			if (project.getName().equals(name)) {
				return project;
			}
		}
		return null;
	}
		
	private boolean isDIPProject(Path projectPath) {
		Path dnfoPath = projectPath.resolve(Messages.DipEditor_DnfoExtension);
		if (!Files.exists(dnfoPath)) {
			return false;
		}
		Path schema = projectPath.resolve(Messages.DipEditor_SchemaFolder);
		if (Files.exists(schema)) {
			return true;
		}
		
		Path dotProjectPath = projectPath.resolve(Messages.DipEditor_ProjectExtension);
		if (Files.exists(dotProjectPath)) {
			return true;
		}
		Path dotGlossPath = projectPath.resolve(Messages.DipEditor_GlossExtension);
		if (Files.exists(dotGlossPath)) {
			return true;
		}
		return false;
	}

	private String openNewNameDialog(Shell shell, String projectName) {
		
		InputDialog dialog = new InputDialog(shell, 
				Messages.DipEditor_OpenDipErrorTitle, 
				Messages.DipEditor_AlreadyExistsErrorTitle + projectName, 
				projectName, 
				new IInputValidator() {
					
					@Override
					public String isValid(String newText) {
						IStatus status = DipUtilities.checkProjectName(newText);
						if (status.isOK()) {
							return null;
						}
						return status.getMessage();
					}
				});
		if (dialog.open() == Dialog.OK) {
			String newName = dialog.getValue();
			return newName;
		} 
		return null;
	}
	
	@Override
	public void setFocus() {

	}

}
