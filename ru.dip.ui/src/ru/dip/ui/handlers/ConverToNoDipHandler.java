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
package ru.dip.ui.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.handlers.HandlerUtil;

import ru.dip.core.manager.DipNatureManager;
import ru.dip.core.model.DipProject;
import ru.dip.core.model.DipSchemaFolder;
import ru.dip.core.model.DnfoTable;
import ru.dip.core.model.glossary.GlossaryFolder;
import ru.dip.core.model.reports.ProjectReportFolder;
import ru.dip.core.utilities.HandlerUtilities;
import ru.dip.core.utilities.ResourcesUtilities;
import ru.dip.core.utilities.WorkbenchUtitlities;
import ru.dip.ui.Messages;

public class ConverToNoDipHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		DipProject dipProject = HandlerUtilities.getDipProject(event);
		if (dipProject == null) {
			return null;
		}
		Shell shell = HandlerUtil.getActiveShell(event);
		boolean isContinue = MessageDialog.openQuestion(shell, Messages.ConverToNoDipHandler_ShellTitle, Messages.ConverToNoDipHandler_ConfirmMessage);
		if (!isContinue) {
			return null;
		}
	
		try {
			IProject project = convertToNoDipProject(dipProject, shell);
			WorkbenchUtitlities.selectInProjectExploter(project);
		} catch (CoreException e) {
			e.printStackTrace();
		}
		return null;
	}	

	private IProject convertToNoDipProject(DipProject dipProject, Shell shell) throws CoreException {
		IProject project = dipProject.getProject();
		DipNatureManager.removeNatrue(project);
		WorkbenchUtitlities.updateProjectExplorer();
		deleteSchemaFolder(project, shell);
		deleteReportFolder(project, shell);
		deleteGlossary(project, shell);
		deleteDnfo(project, shell);
		deleteProjectDip(project, shell);
		ResourcesUtilities.updateProject(project);
		return project;
	}
	
	private void deleteSchemaFolder(IProject project, Shell shell) throws CoreException {
		IFolder folder = project.getFolder(DipSchemaFolder.SCHEMA_FOLDER_NAME);
		if (folder.exists()) {
			ResourcesUtilities.deleteResource(folder, shell);
		}
	}
	
	private void deleteReportFolder(IProject project, Shell shell) throws CoreException {
		IFolder folder = project.getFolder(ProjectReportFolder.REPORT_FOLDER_NAME);
		if (folder.exists()) {
			ResourcesUtilities.deleteResource(folder, shell);
		}
	}
	
	private void deleteGlossary(IProject project, Shell shell) throws CoreException {
		IFile file = project.getFile(GlossaryFolder.GLOS_FILE);
		if (file.exists()) {
			ResourcesUtilities.deleteResource(file, shell);
		}
	}
	
	private void deleteDnfo(IProject project, Shell shell) throws CoreException {
		IFile file = project.getFile(DnfoTable.TABLE_FILE_NAME);
		if (file.exists()) {
			ResourcesUtilities.deleteResource(file, shell);
		}
	}
	
	private void deleteProjectDip(IProject project, Shell shell) throws CoreException {
		IFile file = project.getFile(DipProject.DIP_START_FILE_NAME);
		if (file.exists()) {
			ResourcesUtilities.deleteResource(file, shell);
		}
	}

}
