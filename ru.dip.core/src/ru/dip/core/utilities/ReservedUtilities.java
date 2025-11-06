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
package ru.dip.core.utilities;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Stream;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.widgets.Shell;

import ru.dip.core.exception.DeleteDIPException;
import ru.dip.core.exception.TmpCopyException;
import ru.dip.core.model.DipFolder;
import ru.dip.core.model.DipReservedFolder;
import ru.dip.core.model.DipReservedMarker;
import ru.dip.core.model.DnfoTable;
import ru.dip.core.model.DipUnit;
import ru.dip.core.model.interfaces.IParent;
import ru.dip.core.model.interfaces.IDipElement;
import ru.dip.core.model.interfaces.IDipParent;
import ru.dip.core.model.interfaces.IDipUnit;
import ru.dip.core.model.interfaces.IDipDocumentElement;

public class ReservedUtilities {
	
	public static final String RSVD_EXTENSION = ".rsvd";

	//==============================
	// check reserved
	
	/**
	 * Регистронезависимая проверка наличия зарезервированного файла
	 */
	public static boolean hasReservedFile(IContainer container, String filename) {
		String lowerCaseFilename = filename.toLowerCase() + RSVD_EXTENSION;
		try {
			return Stream.of(container.members())
			.map(IResource::getName)
			.filter(s -> s.endsWith(RSVD_EXTENSION))
			.map(String::toLowerCase)
			.anyMatch(lowerCaseFilename::equals);
		} catch (CoreException e) {
			e.printStackTrace();
			return false;
		}
	}
		
	public static boolean hasReservedFile(IFile file){
		Path  path = Paths.get(file.getLocation().toOSString() + RSVD_EXTENSION);
		return Files.exists(path);
	}
	
	public static boolean isReservedFolder(IFolder folder) {
		IFile file = folder.getFile(".rsvd");
		return file.exists();
	}
	
	public static boolean isReservedFolder(java.nio.file.Path path) {
		Path reservedMarkerPath = path.resolve(RSVD_EXTENSION);
		return Files.exists(reservedMarkerPath);
	}
	
	public static boolean isReservedFolder(String folderPath){
		Path path = Paths.get(folderPath, RSVD_EXTENSION);
		return Files.exists(path);
	}
	
	//=============================
	// reserved unit
	
	public static void createReserveUnit(Shell shell, IDipUnit unit) {
		DipUnit dipUnit = (DipUnit) unit;
		createReserveUnit(shell, dipUnit);
	}
	
	public static void createReserveUnit(Shell shell, DipUnit unit) {
		IFile file = createReservedFile(unit, shell);
		if (file != null && file.exists()) {
			IDipParent parent = unit.parent();
			parent.createReservedUnit(file);
		}
	}
	
	private static IFile createReservedFile(DipUnit unit, Shell shell){
		IFile unitFile = unit.resource();
		String newName = unitFile.getName() + RSVD_EXTENSION;
		IFile file = unitFile.getParent().getFile(new org.eclipse.core.runtime.Path(newName));		
		try {
			ResourcesUtilities.createFile(file, shell);
			return file;
		} catch (CoreException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static void reserveFolder(Shell shell, IDipParent folder) throws DeleteDIPException, TmpCopyException{
		List<IDipDocumentElement> children = folder.getDipDocChildrenList();
		for (int i =  children.size() - 1; i >= 0; i--){
			IDipElement element = children.get(i);
			if (element instanceof DipUnit){
				DipUtilities.deleteElement((DipUnit) element, DipUtilities.RESERVE, shell, DipUtilities.NO_TMP);
			} else if (element instanceof DipFolder){
				DipUtilities.deleteDipFolder((DipFolder) element,  DipUtilities.RESERVE, shell, DipUtilities.NO_TMP);
			} else if (element instanceof DnfoTable){	
				DipUtilities.deleteDipTable((DnfoTable) element, shell);
			}
		}
		createReservedMarker(folder, shell);	
	}
	
	private static void createReservedMarker(IDipParent folder, Shell shell){
		IFile file = folder.resource().getFile(new org.eclipse.core.runtime.Path(DipReservedMarker.RESERVED_MARKER_NAME));
		try {
			ResourcesUtilities.createFile(file, shell);
			if (file.exists()){
				folder.createReservedMarker(file);
			}
		} catch (CoreException e) {
			e.printStackTrace();
		}	
	}
	
	//==============================
	// unreserve
		
	/**
	 * Разрезервировать папку без добавления к родителю DipFolder
	 */
	public static void deleteUnreserveMarker(DipReservedFolder reservedFolder, Shell shell){
		IFolder folder = reservedFolder.resource();
		deleteReservedMarker(folder, shell);
		IParent parent = reservedFolder.parent();
		parent.removeChild(reservedFolder);
	}
	
	private static void deleteReservedMarker(IFolder folder, Shell shell){
		IFile file = folder.getFile(new org.eclipse.core.runtime.Path(DipReservedMarker.RESERVED_MARKER_NAME));
		if (file.exists()){
			try {
				ResourcesUtilities.deleteResource(file, shell);
			} catch (CoreException e) {
				e.printStackTrace();
			}
		}
	}

}
