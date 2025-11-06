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
package ru.dip.core.manager;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import ru.dip.core.DipCorePlugin;
import ru.dip.core.exception.DIPException;
import ru.dip.core.model.DipProject;
import ru.dip.core.model.DipSchemaFolder;
import ru.dip.core.model.glossary.GlossaryFolder;
import ru.dip.core.model.vars.VarContainer;
import ru.dip.core.utilities.ResourcesUtilities;
import ru.dip.core.utilities.WorkbenchUtitlities;

public class DipProjectResourceCreator {
	
	public static void createDipProject(DipProject dipProject){
		Shell shell =  PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
		createGlossaryFile(shell, dipProject);
		createVariablesFile(shell, dipProject);
		createDefaultSchemaFolder(shell, dipProject);
		createDipFile(dipProject);
		ResourcesUtilities.updateDipElement(dipProject);
		dipProject.refresh();
		WorkbenchUtitlities.updateProjectExplorer();
	}

	//=====================
	// schema
	
	public static IFolder createDefaultSchemaFolder(Shell shell, DipProject dipProject){
		IFolder schemaFolder = createSchemaFolder(shell, dipProject);
		return schemaFolder;
	}
	
	private static IFolder createSchemaFolder(Shell shell, DipProject dipProject){
		IFolder folder = dipProject.getProject().getFolder(DipSchemaFolder.SCHEMA_FOLDER_NAME);
		if (folder.exists()) {
			return folder;
		}
		try {
			return ResourcesUtilities.createFolder(folder, shell);
		} catch (CoreException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static void checkSchemaFolder(Shell shell, DipProject dipProject){
		IFolder schemaFolder = dipProject.resource().getFolder(DipSchemaFolder.SCHEMA_FOLDER_NAME);
		if (!schemaFolder.exists()) {
			createDefaultSchemaFolder(shell, dipProject);
		}
	}
		
	//=====================
	// glossary
	
	public static IFile createGlossaryFile(Shell shell, DipProject project) {
		IFile file = project.getProject().getFile(GlossaryFolder.GLOS_FILE);
		try {
			return ResourcesUtilities.createFile(file, shell);
		} catch (CoreException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	//===================
	// variable
	
	public static IFile createVariablesFile(Shell shell, DipProject project) {
		IFile file = project.getProject().getFile(VarContainer.VAR_FILE);
		try {
			return ResourcesUtilities.createFile(file, shell);
		} catch (CoreException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	//============================
	// dip-file
	
	public static void createDipFile(DipProject dipProject) {
		try {
			Path path = getDipStartPath();
			if (Files.exists(path)) {
				Path destinationPath = Paths.get(dipProject.getProject().getLocation().toOSString(), "project.dip");
				Files.copy(path, destinationPath, StandardCopyOption.REPLACE_EXISTING);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private static Path getDipStartPath() throws DIPException {
		try {
			return ResourcesUtilities.getPathFromPlugin(DipCorePlugin.getDefault(), DipProject.DIP_START_PATH);
		} catch (IOException e) {
			e.printStackTrace();
			throw new DIPException("Не найден файл project.dip");
		}
	}
	
	public static void checkDipFile(DipProject dipProject) {
		if (!containsDipFile(dipProject)) {
			createDipFile(dipProject);
		}
	}
	
	private static boolean containsDipFile(DipProject dipProject) {
		return dipProject.getProject().getFile(DipProject.DIP_START_FILE_NAME).exists();
	}
	
}
