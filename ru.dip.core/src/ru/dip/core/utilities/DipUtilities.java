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

import static ru.dip.core.utilities.DnfoUtils.DNFO_FILENAME;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.undo.MoveResourcesOperation;
import org.eclipse.ui.ide.undo.WorkspaceUndoUtil;

import ru.dip.core.DipCorePlugin;
import ru.dip.core.exception.CopyDIPException;
import ru.dip.core.exception.DeleteDIPException;
import ru.dip.core.exception.RenameDIPException;
import ru.dip.core.exception.SaveTableDIPException;
import ru.dip.core.exception.TmpCopyException;
import ru.dip.core.form.FormReader;
import ru.dip.core.form.model.Field;
import ru.dip.core.link.LinkInteractor;
import ru.dip.core.manager.DipNatureManager;
import ru.dip.core.manager.DipProjectResourceCreator;
import ru.dip.core.model.Appendix;
import ru.dip.core.model.DipDescription;
import ru.dip.core.model.DipElement;
import ru.dip.core.model.DipElementType;
import ru.dip.core.model.DipFolder;
import ru.dip.core.model.DipProject;
import ru.dip.core.model.DipProjectSchemaModel;
import ru.dip.core.model.DipRoot;
import ru.dip.core.model.DipSchemaElement;
import ru.dip.core.model.DipSchemaFolder;
import ru.dip.core.model.DnfoTable;
import ru.dip.core.model.IncludeFolder;
import ru.dip.core.model.interfaces.IDipComment;
import ru.dip.core.model.interfaces.IDipDocumentElement;
import ru.dip.core.model.interfaces.IDipElement;
import ru.dip.core.model.interfaces.IDipParent;
import ru.dip.core.model.interfaces.IDipUnit;
import ru.dip.core.model.interfaces.IDisable;
import ru.dip.core.model.interfaces.IParent;
import ru.dip.core.model.interfaces.ITextComment;
import ru.dip.core.model.reports.ProjectReportFolder;
import ru.dip.core.model.reports.Report;
import ru.dip.core.table.TableReader;
import ru.dip.core.table.TableWriter;
import ru.dip.core.unit.GlossaryPresentation;
import ru.dip.core.unit.TocRefPresentation;
import ru.dip.core.unit.UnitType;
import ru.dip.core.utilities.status.DipStatus;
import ru.dip.core.utilities.status.ErrorStatus;
import ru.dip.core.utilities.status.StatusUtils;
import ru.dip.core.utilities.tmp.DeleteResultOperation;
import ru.dip.core.utilities.tmp.ExtractResult;
import ru.dip.core.utilities.tmp.IncludeTmpElement;
import ru.dip.core.utilities.tmp.MoveResult;
import ru.dip.core.utilities.tmp.TmpElement;

public class DipUtilities {
		
	//===========================
	// check dip name
	
	public static boolean isDipProject(java.nio.file.Path path) {
		java.nio.file.Path dnfoPath = path.resolve(DNFO_FILENAME);		 //$NON-NLS-1$
		if (!Files.exists(dnfoPath)){
			return false;
		}
		return TableReader.isRoot(dnfoPath.toUri());
	}
	
	public static boolean isDipName(String filename){		
		String regex = "^[-_\\.a-zA-Z0-9]*$";
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(filename);		
		return matcher.find();
	}
	
	public static boolean isHideFile(String filename){
		return (filename.startsWith(".") || filename.endsWith(ReservedUtilities.RSVD_EXTENSION));
	}
		
	public static IStatus checkReqName(String fileName){
		if (fileName.isEmpty()){
			return StatusUtils.NO_NAME; 
		}		
		if (!isDipName(fileName)){
			return StatusUtils.INVALID_NAME;		
		}
		if (GlossaryPresentation.FILE_NAME.equals(fileName)){
			return DipStatus.OK_STATUS;
		}
		if (TocRefPresentation.FILE_NAME.equals(fileName)) {
			return DipStatus.OK_STATUS;
		}		
		if (UnitType.isChangeLog(fileName)){
			return DipStatus.OK_STATUS;
		}		
		if (isHideFile(fileName)){
			return StatusUtils.HIDE_NAME;
		}		
		return DipStatus.OK_STATUS;
	}
	
	public static boolean isServedFolder(String name) {
		return name.startsWith("__");
	}
	
	public static boolean isServedFolder(IFolder folder) {
		return isServedFolder(folder.getName());
	}
	
	public static boolean isNotDnfo(IFolder folder) {		
		IFile file = folder.getFile(DNFO_FILENAME);
		return !file.exists();
	}
	
	public static boolean isServiceResource(IResource resource) {
		String name = resource.getName();
		if (DNFO_FILENAME.equals(name)) {
			return true;
		}
		if (resource.getParent() instanceof IProject) {
			return ".glos".equals(name) || "schema".equals(name)
					|| "Reports".equals(name);
		}
		return false;		
	}
	
	//=====================
	// create files
	
	public static IFile createTableFile(IContainer container, Shell shell){
		try {
			return ResourcesUtilities.createFile(container, DnfoTable.TABLE_FILE_NAME, shell);
		} catch (CoreException e) {
			e.printStackTrace();
			MessageDialog.openError(shell, "Error", "Ошибка создания таблицы директории.");
			return null;
		}
	}
	
	public static IStatus canCreateFile(IContainer container, String fileName) {
		try {
			if (ResourcesUtilities.contains(container, fileName)){
				return StatusUtils.FILE_ALREADY_EXISTS;
			}
			if (ResourcesUtilities.containsDisableObject(container, fileName)) {
				return StatusUtils.FILE_ALREADY_EXISTS;
			}
		} catch (CoreException | IOException e) {
			return StatusUtils.errorStatus(e);
		}

		IFolder fNewFolder = ResourcesUtilities.getFolder(container, fileName);
		if (fNewFolder != null){			
			if (ReservedUtilities.isReservedFolder(fNewFolder)){
				return StatusUtils.RESERVED_FOLDER;
			} else {
				return StatusUtils.FOLDER_ALREADY_EXISTS;
			}
		}
		if (ReservedUtilities.hasReservedFile(container ,fileName)){
			return StatusUtils.RESERVED_FILE;
		}
		return Status.OK_STATUS;
	}
	
	public static IStatus canCreateFolder(IContainer container, String folderName){
		if (!validateFolderContainer(container)){
			return StatusUtils.MAX_NESTING;
		}	
		try {
			if (ResourcesUtilities.containsDisableObject(container, folderName)) {
				return StatusUtils.FILE_ALREADY_EXISTS;
			}
			if (ResourcesUtilities.contains(container, folderName)){
				return StatusUtils.FILE_ALREADY_EXISTS;
			}
		} catch (CoreException | IOException e) {
			return StatusUtils.errorStatus(e);
		}
		if (ReservedUtilities.hasReservedFile(container ,folderName)){
			return StatusUtils.RESERVED_FILE;
		}				
		IFolder newFodler = ResourcesUtilities.getFolder(container, folderName);
		if (newFodler != null){			
			if (ReservedUtilities.isReservedFolder(newFodler)){
				return StatusUtils.WARNING_RESERVED_FOLDER;
			}			
			return StatusUtils.FOLDER_ALREADY_EXISTS;
		}				
		return Status.OK_STATUS;
	}
	
	public static DipStatus canCreateProject(String projectName){
		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
		if (project != null && project.exists()){
			return StatusUtils.PROJECT_ALREADY_EXISTS;
		}
		return DipStatus.OK_STATUS;
	}
	
	private static boolean validateFolderContainer(IContainer folder){
		int n = getNestedLevel(folder);
		return n < 6;
	}
	
	public static int getNestedLevel(IContainer folder) {
		java.nio.file.Path path = Paths.get(folder.getProjectRelativePath().toOSString());
		return path.getNameCount();
	}
	
	/**
	 * Является ли файлом форм 
	 * Проверяется при создании, 
	 */
	public static boolean isFormFile(IFile file){
		String fileExtension = file.getFileExtension();
		if (fileExtension == null || fileExtension.isEmpty()){
			return false;
		}
		DipProject dipProject = DipRoot.getInstance().getDipProject(file.getProject());
		if (dipProject != null){
			DipProjectSchemaModel schemaModel = dipProject.getSchemaModel();
			if (schemaModel != null){
				return schemaModel.containsFileExtension(fileExtension);
			}
		}		
		return false;
	}
	
	public static void createFormFile(IFile file){
		FormReader fReader =  new FormReader(file);
		fReader.read();
		for (Field field: fReader.getFields()){
			field.applayDefaultValue();
			field.getFormModel().getPositionModel().createModel();
		}	
		if (!fReader.isEmptyDocument()){
			fReader.saveDocument();
		}
	}
	
	public static boolean isDiaFile(IFile file) {
		String fileExtension = file.getFileExtension();
		return "dia".equals(fileExtension);
	}
	
	public static void createDiaFile(IFile file) {
		String emptyDiaPath = "project_content/empty.dia";
		try {
			java.nio.file.Path inputPath;	inputPath = ResourcesUtilities.getPathFromPlugin(DipCorePlugin.getDefault(), emptyDiaPath);
			java.nio.file.Path  outputPath = Paths.get(file.getLocationURI());
			Files.copy(inputPath, outputPath, StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	//====================================
	// create report
	
	public static void createReportRefFile(IFile file, IDipElement report, boolean relative) {
		String relativePath = null;
		IPath reportPath = report.resource().getProjectRelativePath();
		if (relative) {
			IPath targetPath = file.getParent().getProjectRelativePath();
			relativePath = reportPath.makeRelativeTo(targetPath).toOSString();
			if (!relativePath.startsWith(".")) {
				relativePath = "./" + relativePath;
			}
		} else {
			relativePath = reportPath.toOSString();
		}
		
		try {
			FileUtilities.writeFile(file, relativePath);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	//=====================================
	// paste
	
	public static IStatus canPaste(IDipParent parent, Iterable<IDipDocumentElement> elements) {
		for (IDipDocumentElement dipDocumentElement: elements) {
			IResource res = dipDocumentElement.resource();
			IStatus status = canPaste(parent, res);
			if (!status.isOK()){
				return status;
			}						
		}
		return DipStatus.OK_STATUS;		
	}
	
	public static IStatus canPaste(IDipParent parent, IResource[] resources){
		for (IResource res: resources){
			IStatus status = canPaste(parent, res);
			if (!status.isOK()){
				return status;
			}
		}
		return DipStatus.OK_STATUS;		
	}
	
	public static IStatus canPaste(IDipParent parent, IResource resource){
		IContainer container = (IContainer) parent.resource();
		if (resource instanceof IFile){
			if (parent instanceof Appendix) {
				return StatusUtils.CAN_NOT_CREATE_FILE_IN_APPENDIX;
			}			
			return canPasteFile(container, (IFile) resource);
		} 
		if (resource instanceof IFolder){
			return canPasteFolder(container, (IFolder) resource);
		}
		return StatusUtils.INVALID_TYPE;
	}
	
	public static IStatus canPaste(IDipParent parent, String[] filePaths){
		if (filePaths.length == 1){
			return canPaste(parent, filePaths[0]);
		} else {
			for (String filename: filePaths){
				IStatus status = canPaste(parent, filename);
				if (!status.isOK()){
					return status;
				}
			}
			return DipStatus.OK_STATUS;
		}
	}
	
	public static IStatus canPaste(IDipParent parent, String filePath){
		IContainer container = (IContainer) parent.resource();
		java.nio.file.Path path = Paths.get(filePath);
		if (Files.isDirectory(path)){
			return canPasteFolder(container, path);
		} else {
			if (parent instanceof Appendix) {
				return StatusUtils.CAN_NOT_CREATE_FILE_IN_APPENDIX;
			}
			return canPasteFile(container, path);
		}		
	}
	
	/**
	 * 	Нельзя вставлять с невалидным именем
	 *  Нельзя вставлять зарезервированный объект
	 *  Нельзя вставлять если файл зарезервирвоан
	 *  Нельзя вставлять если существует директория с ттаким именем
	 */
	private static IStatus canPasteFile(IContainer container, IFile file){
		String fileName = file.getName();
		return canPasteFile(container, fileName);

	}
	
	private static IStatus canPasteFile(IContainer container, java.nio.file.Path filePath){
		String fileName = filePath.getFileName().toString();
		return canPasteFile(container, fileName);
	}
	
	private static IStatus canPasteFile(IContainer container, String fileName){
		IStatus status = DipUtilities.checkReqName(fileName);
		if (!status.isOK()){
			return status;
		}	
		if (fileName.endsWith(ReservedUtilities.RSVD_EXTENSION)){
			return StatusUtils.MOVED_RESERVED_OBJECT;
		}		
		try {
			if (ResourcesUtilities.containsFile(container, fileName)){
				return StatusUtils.FILE_ALREADY_EXISTS;
			}
		} catch (CoreException | IOException e) {
			return StatusUtils.errorStatus(e);
		}
		if (ReservedUtilities.hasReservedFile(container, fileName)){
			return StatusUtils.RESERVED_FILE;
		}		
		IFolder newFolder = ResourcesUtilities.getFolder(container, fileName);
		if (newFolder != null){			
			if (ReservedUtilities.isReservedFolder(newFolder)){
				return StatusUtils.RESERVED_FOLDER;
			} else {
				return StatusUtils.FOLDER_ALREADY_EXISTS;
			}
		}	
		return DipStatus.OK_STATUS;
	}
	
	/**
	 * 	Нельзя вставлять с невалидным именем
	 *  Нельзя вставлять если вложенность директорий > 5
	 *  Нельзя вставлять зарезервированный объект
	 *  Нельзя вставлять если существует файл с таким именем
	 *  Нельзя вставлять если файл с таким именем зарезервирован
	 *  Нельзя вставлять если существует директория с таким именем
	 *  Нельзя вставлять если директория с таким зарезервирована
	 */
	private static IStatus canPasteFolder(IContainer container, IFolder folder){
		String folderName = folder.getName();
		IStatus status = DipUtilities.checkReqName(folderName);
		if (!status.isOK()){
			return status;
		}
		if (!validateFolderContainer(container)){
			return StatusUtils.MAX_NESTING;
		}	
		
		if (ReservedUtilities.isReservedFolder(folder)){
			return StatusUtils.MOVED_RESERVED_OBJECT;
		}
		return canPasteFolder(container, folderName);
	}
	
	private static IStatus canPasteFolder(IContainer container, java.nio.file.Path path){
		String folderName = path.getFileName().toString();

		IStatus status = DipUtilities.checkReqName(folderName);
		if (!status.isOK()){
			return status;
		}
		if (!validateFolderContainer(container)){
			return StatusUtils.MAX_NESTING;
		}	
		if (ReservedUtilities.isReservedFolder(path)){
			return StatusUtils.MOVED_RESERVED_OBJECT;

		}		
		return canPasteFolder(container, folderName);
	}
	
	private static IStatus canPasteFolder(IContainer container, String folderName){
		try {
			if (ResourcesUtilities.containsFile(container, folderName)){
				return StatusUtils.FILE_ALREADY_EXISTS;
			}
		} catch (CoreException | IOException e) {
			return StatusUtils.errorStatus(e);
		}			
		if (ReservedUtilities.hasReservedFile(container, folderName)){
			return StatusUtils.RESERVED_FILE;
		}				
		IFolder newFodler = ResourcesUtilities.getFolder(container, folderName);
		if (newFodler != null){			
			if (ReservedUtilities.isReservedFolder(newFodler)){
				return StatusUtils.RESERVED_FOLDER;
			}			
			return StatusUtils.FOLDER_ALREADY_EXISTS;
		}				
		return DipStatus.OK_STATUS;
	}
	
	public static IStatus canPaste(IDipParent parent, IDipElement element){
		IContainer container = (IContainer) parent.resource();
		if (element instanceof IDipUnit){
			if (parent instanceof Appendix) {
				return StatusUtils.CAN_NOT_CREATE_FILE_IN_APPENDIX;
			}		
			return canPasteFile(container, (IFile) element.resource());
		} else if (element instanceof IDipParent){
			return canPasteFolder(container, (IFolder) element.resource());
		}
		return StatusUtils.INVALID_TYPE;
	}
		
	public static IStatus canPasteReport(ProjectReportFolder parent, IResource[] resources){
		if (resources.length == 1){
			return canPasteReport(parent, resources[0]);
		} else {
			for (IResource res: resources){
				IStatus status = canPasteReport(parent, res);
				if (!status.isOK()){
					return status;
				}
			}
			return DipStatus.OK_STATUS;
		}
	}
	
	private static IStatus canPasteReport(ProjectReportFolder parent, IResource resource){				
		IContainer container = (IContainer) parent.resource();
		if (resource instanceof IFile){
			return canPasteReport(container, (IFile) resource);
		} 
		return StatusUtils.INVALID_TYPE;			
	}
	
	private static IStatus canPasteReport(IContainer container, IFile file){
		String fileName = file.getName();
		IStatus status = DipUtilities.checkReqName(fileName);
		if (!status.isOK()){
			return status;
		}		
		String extension = file.getFileExtension();
		if (!Report.REPORT_EXTENSION.equals(extension)){
			return StatusUtils.INVALID_TYPE;			
		}	
		return canPasteByName(container, fileName);
	}
		
	public static IStatus canPasteReport(ProjectReportFolder parent, String[] filePaths){
		if (filePaths.length == 1){
			return canPasteReport(parent, filePaths[0]);
		} else {
			for (String filename: filePaths){
				IStatus status = canPasteReport(parent, filename);
				if (!status.isOK()){
					return status;
				}
			}
			return DipStatus.OK_STATUS;
		}
	}
	
	private static IStatus canPasteReport(ProjectReportFolder parent, String filePath){
		IContainer container = (IContainer) parent.resource();
		java.nio.file.Path path = Paths.get(filePath);
		if (Files.isDirectory(path)){
			return StatusUtils.INVALID_TYPE;
 
		} else {
			return canPasteReport(container, path);
		}		
	}
	
	private static IStatus canPasteReport(IContainer container, java.nio.file.Path filePath){
		String fileName = filePath.getFileName().toString();
		IStatus status = DipUtilities.checkReqName(fileName);
		if (!status.isOK()){
			return status;
		}
		if (!fileName.endsWith(Report.REPORT_EXTENSION)){
			return StatusUtils.INVALID_TYPE;			
		}
		return canPasteByName(container, fileName);
	}
	
	public static IStatus canPasteSchema(DipSchemaFolder parent, IResource[] resources){
		if (resources.length == 1){
			return canPasteSchema(parent, resources[0]);
		} else {
			for (IResource res: resources){
				IStatus status = canPasteSchema(parent, res);
				if (!status.isOK()){
					return status;
				}
			}
			return DipStatus.OK_STATUS;
		}
	}
	
	private static IStatus canPasteSchema(DipSchemaFolder parent, IResource resource){				
		IContainer container = (IContainer) parent.resource();
		if (resource instanceof IFile){
			return canPasteSchema(container, (IFile) resource);
		} 
		return StatusUtils.INVALID_TYPE;
	}
	
	
	private static IStatus canPasteSchema(IContainer container, IFile file){
		String fileName = file.getName();
		IStatus status = DipUtilities.checkReqName(fileName);
		if (!status.isOK()){
			return status;
		}
		String extension = file.getFileExtension();
		if (!"xml".equals(extension)){
			return StatusUtils.INVALID_TYPE;
		}	
		return canPasteByName(container, fileName);
	}
	
	public static IStatus canPasteSchema(DipSchemaFolder parent, String[] filePaths){
		if (filePaths.length == 1){
			return canPasteSchema(parent, filePaths[0]);
		} else {
			for (String filename: filePaths){
				IStatus status = canPasteSchema(parent, filename);
				if (!status.isOK()){
					return status;
				}
			}
			return DipStatus.OK_STATUS;
		}
	}
	
	private static IStatus canPasteSchema(DipSchemaFolder parent, String filePath){
		IContainer container = (IContainer) parent.resource();
		java.nio.file.Path path = Paths.get(filePath);
		if (Files.isDirectory(path)){
			return StatusUtils.INVALID_TYPE;
		} else {
			return canPasteSchema(container, path);
		}
	}
	
	private static IStatus canPasteSchema(IContainer container, java.nio.file.Path filePath){
		String fileName = filePath.getFileName().toString();
		IStatus status = DipUtilities.checkReqName(fileName);
		if (!status.isOK()){
			return status;
		}
		if (!fileName.endsWith(".xml")){
			return StatusUtils.INVALID_TYPE;
		}
		return canPasteByName(container, fileName);
	}
	
	private static IStatus canPasteByName(IContainer container, String fileName) {
		try {
			if (ResourcesUtilities.containsFile(container, fileName)){
				return StatusUtils.FILE_ALREADY_EXISTS;
			}
			if (ResourcesUtilities.containsFolder(container, fileName)){
				return StatusUtils.FOLDER_ALREADY_EXISTS;
			}
		} catch (CoreException | IOException e) {
			return StatusUtils.errorStatus(e);
		}	
		return DipStatus.OK_STATUS;
	}
	

	//===================================
	// copy
	
	public static void copyElement(IDipParent destination, IDipElement element, Shell shell) throws CopyDIPException{
		IResource source = element.resource();
		if (element instanceof IDipUnit) {
			copyUnit(destination,(IDipUnit) element, shell);					
		} else if (source instanceof IFile){
			// возможно этот код не будет выполняться (если только для отчетов, схем  и прочих элементах при копировании из PE) 
			copyFile(destination, source, shell);
		} else if (source instanceof IFolder){
			copyFolder(destination, source, shell);
		}	
	}	
	
	public static IDipElement copyElement(IDipParent destination, IDipElement element, int index, Shell shell) throws CopyDIPException{
		IResource source = element.resource();
		if (element instanceof IDipUnit) {
			return copyUnit(destination,(IDipUnit) element, index, shell);					
		} else if (source instanceof IFile){
			// возможно этот код не будет выполняться (если только для отчетов, схем  и прочих элементах при копировании из PE) 
			return copyFile(destination, source, shell);
		} else if (source instanceof IFolder){
			return copyFolder(destination, (IDipParent)element, index, shell);
		}	
		throw new CopyDIPException(source, "Недопустимый тип элемента для копирвоания: " + source.getName());
	}	
	
	public static void copyResource(IDipParent destination, IResource source, Shell shell) throws CopyDIPException{
		if (source instanceof IFile){
			copyFile(destination, source, shell);
		} else if (source instanceof IFolder){
			copyFolder(destination, source, shell);
		}	
	}	
	
	private static IDipUnit copyUnit(IDipParent destination, IDipUnit unit, Shell shell) throws CopyDIPException {
		return copyUnit(destination, unit, -1, shell);
	}
	
	private static IDipUnit copyUnit(IDipParent destination, IDipUnit unit, int index, Shell shell) throws CopyDIPException {
		IResource source = unit.resource();
		String description = unit.description();
		String comment = unit.getCommentContent();
		List<ITextComment> textComments = null;
		if (unit.comment() != null) {		
			textComments = unit.comment().getTextComments();
		}

		try {
			ResourcesUtilities.copyResources((IContainer)destination.resource(), source, shell);
		} catch (CoreException e) {
			e.printStackTrace();
			throw new CopyDIPException(source, "Error copy to " + destination.name());
		}						
		IFile newFile = destination.resource().getFile(new Path(source.getName()));
		if (newFile.exists()){
			IDipUnit newElement = 
					(index >= 0) ? (IDipUnit) destination.createNewUnit(newFile, index)
					: (IDipUnit) destination.createNewUnit(newFile);
			// copy description
			if (description != null && !description.isEmpty()) {
				newElement.updateDescription(description);
			}
			// copy main comment
			if (comment != null && !comment.isEmpty()) {
				newElement.updateDipComment(comment);
			}
			// copy text comments
			if (textComments != null && !textComments.isEmpty()) {
				newElement.updateTextAnnotations(textComments);
			}
			return newElement;
		} else {
			throw new CopyDIPException(source, "Error copy to " + destination.name());
		}
	}
	
	private static IDipElement copyFile(IDipParent destination, IResource source, Shell shell) throws CopyDIPException {
		try {
			ResourcesUtilities.copyResources((IContainer)destination.resource(), source, shell);
		} catch (CoreException e) {
			throw new CopyDIPException(source, "Error copy to " + destination.name());

		}						
		IFile newFile = destination.resource().getFile(new Path(source.getName()));
		if (newFile.exists()){
			return destination.createNewUnit(newFile);
		} else {
			throw new CopyDIPException(source, "Error copy to " + destination.name());
		}
	}
	
	private static void copyFolder(IDipParent destination, IResource source, Shell shell) throws CopyDIPException {
		try {
			ResourcesUtilities.copyResources((IContainer)destination.resource(), source, shell);
		} catch (CoreException e) {
			e.printStackTrace();
			throw new CopyDIPException(source, "Error copy to " + destination.name());
		}
		IFolder newFolder = destination.resource().getFolder(new Path(source.getName()));
		if (newFolder.exists()){
			if (!DipUtilities.isNotDnfo(newFolder)) {
				destination.createNewFolder(newFolder);
			}
		} else {
			throw new CopyDIPException(source, "Error copy to " + destination.name());
		}
	}
	
	private static IDipParent copyFolder(IDipParent destination, IDipParent folder, int index, Shell shell) throws CopyDIPException {
		IResource source = folder.resource();
		try {
			ResourcesUtilities.copyResources((IContainer)destination.resource(), source, shell);
		} catch (CoreException e) {
			e.printStackTrace();
			throw new CopyDIPException(source, "Error copy to " + destination.name());
		}
		IFolder newFolder = destination.resource().getFolder(new Path(source.getName()));
		if (newFolder.exists()){
			if (!DipUtilities.isNotDnfo(newFolder)) {
				String description = folder.description();
				String comment = folder.getCommentContent();
				IDipParent result =  destination.createNewFolder(newFolder, index);
				if (description != null) {
					result.setDescription(description);
				}
				if (comment != null) {
					result.updateDipComment(comment);
				}
				return result;
			}
		} 
		throw new CopyDIPException(source, "Error copy to " + destination.name());
	}
	
	//===================================
	// disable
	
	public static void doDisableObject(IDipDocumentElement dipDocElement, Shell shell, boolean rename) {
		boolean doEnable = dipDocElement.isDisabled();
		if (doEnable) {
			dipDocElement.setDisabled(false);
			// delete_extension
			if (rename) {
				deleteMarker(dipDocElement, shell);
			}
		} else {
			dipDocElement.setDisabled(true);
			// add_extension
			if (rename) {
				addMarker(dipDocElement, shell);
			}
		}
	}
	
	private static void addMarker(IDipDocumentElement dipDocElement, Shell shell) {
		try {
			String oldName = dipDocElement.dipName();
			if (!oldName.startsWith(IDisable.DISABLE_MARKER)) {
				DipUtilities.renameElement(dipDocElement, IDisable.DISABLE_MARKER + dipDocElement.dipName(),
					false, shell);
				WorkbenchUtitlities.updateProjectExplorer();	
			}
		} catch (RenameDIPException | SaveTableDIPException e) {
			e.printStackTrace();
		}
	}
	
	private static void deleteMarker(IDipDocumentElement dipDocumentElement, Shell shell) {						
		try {
			String oldName = dipDocumentElement.dipName();
			if (oldName.startsWith(IDisable.DISABLE_MARKER)) {
				String newName = oldName.substring(IDisable.DISABLE_MARKER.length());
				DipUtilities.renameElement(dipDocumentElement,newName,
						false, shell);
				WorkbenchUtitlities.updateProjectExplorer();
			}									
		} catch (RenameDIPException | SaveTableDIPException e) {
			e.printStackTrace();
		}
	}
	
	//===================================
	// rename
	
	public static void lightRenameElement(IDipElement element, String newName, 
			boolean reserve, Shell shell)
			throws RenameDIPException, SaveTableDIPException {
		Shell finalShell = WorkbenchUtitlities.checkShell(shell);	
		String oldID = DipRoot.getInstance().mapID(element);
		WorkspaceJob job = getRenameJob(element, newName, 
				finalShell, reserve, oldID, false); 
		runRenameJobWithCheckDisable(element, newName, job);
	}
	
	public static void renameElement(IDipElement element, String newName,
			boolean reserve, Shell shell) throws  RenameDIPException, SaveTableDIPException{
		Shell finalShell = WorkbenchUtitlities.checkShell(shell);	
		String oldID = DipRoot.getInstance().mapID(element);
		WorkspaceJob job = getRenameJob(element, newName,
				finalShell, reserve, oldID, true);
		
		runRenameJobWithCheckDisable(element, newName, job);
	}
	
	private static void runRenameJobWithCheckDisable(IDipElement element, String newName, WorkspaceJob job) throws RenameDIPException, SaveTableDIPException {
		if (element instanceof IDisable
			&&	element.dipName().startsWith("dis.") != newName.startsWith("dis.")){
			((IDisable) element).setDisabled(newName.startsWith("dis."));
		}	
		runRenameJob(element, job);		
	}
	
	private static WorkspaceJob getRenameJob(IDipElement element, String newName, Shell shell, boolean reserve,
			String oldID, boolean needSaveTable) throws SaveTableDIPException, RenameDIPException {

		return new WorkspaceJob("Rename") {

			@Override
			public IStatus runInWorkspace(IProgressMonitor monitor) throws CoreException {
				try {

					if (element instanceof IncludeFolder) {
						IncludeFolder include = (IncludeFolder) element;
						include.setLinkName(newName);
						if (needSaveTable) {
							saveTable(include.parent());
						}
					} else if (element instanceof IDipUnit) {
						IDipUnit unit = (IDipUnit) element;
						renameUnit(unit, newName, reserve, shell);
						if (needSaveTable) {
							saveTable(unit.parent());
						}
					} else if (element instanceof DipProject) {
						DipProject project = (DipProject) element;
						renameProject(project, newName, shell);
					} else if (element instanceof IDipParent) {
						IDipParent dipParent = (IDipParent) element;
						renameFolder(dipParent, newName, reserve, shell);
						if (needSaveTable) {
							saveTable(dipParent.parent());
						}
					}else if (element instanceof DipElement && element.resource() instanceof IFile) {
						DipElement report = (DipElement) element;
						renameReport(report, newName, shell);
					}
					DipRoot.getInstance().updateID(element, oldID);
					return Status.OK_STATUS;
				} catch (RenameDIPException | SaveTableDIPException e) {
					e.printStackTrace();
					return new ErrorStatus(e);
				}
			}
		};
	}
	
	private static void runRenameJob(IDipElement element, WorkspaceJob job) throws RenameDIPException, SaveTableDIPException {
		try {
			IStatus status = job.runInWorkspace(null);
			if (status instanceof ErrorStatus) {
				Throwable exception = ((ErrorStatus) status).getException();
				if (exception instanceof RenameDIPException) {
					throw (RenameDIPException)exception;
				} else if (exception instanceof SaveTableDIPException) {
					throw  (SaveTableDIPException)exception;
				}
			}		
		} catch (CoreException e) {
			e.printStackTrace();			
			throw new RenameDIPException(element, e.getMessage());
		}
	}
	
	private static void renameUnit(IDipUnit unit, String newName, boolean reserve, Shell shell) throws RenameDIPException{
		String lastID =  relativeProjectID(unit);
		String includeID = null;
		if (unit.isIncluded()) {
			includeID = relativeIncludeElement(unit);
		}
		
		if (reserve){
			ReservedUtilities.createReserveUnit(shell, unit);
		}	
		IFile file = (IFile) unit.resource();
		// description
		DipDescription description = unit.dipDescription();
		String descriptionContent = null;
		if (description != null) {
			descriptionContent = description.getDescriptionContent();
		}
		// comment
		IDipComment comment = unit.comment();
		String commentContent = null;
		if (comment != null) {
			commentContent = comment.getCommentContent();
		}
		List<ITextComment> textComments = null;
		if (comment != null) {
			textComments = comment.getTextComments();
		}
	
		IPath newPath = file.getFullPath().removeLastSegments(1).append(newName);
		MoveResourcesOperation mp = new MoveResourcesOperation(file, newPath, "Move resource");
		try {
			IStatus status = mp.execute(null, WorkspaceUndoUtil.getUIInfoAdapter(shell));
			if (!status.isOK()){
				throw new RenameDIPException(unit, status.getMessage());
			}
			IFile newFile = file.getParent().getFile(new Path(newName));
			unit.setResource(newFile);
			updateReqDescription(unit, description, descriptionContent);
			updateReqComment(unit, comment, commentContent, textComments);
			
			String newRelativeID = relativeProjectID(unit);
			LinkInteractor.instance().updateLinks(lastID, newRelativeID, unit.dipProject(), false);									
			if (unit.isIncluded()) {
				String newIncludeID = relativeIncludeElement(unit);								
				IncludeFolder folder = findIncludeFolder(unit);
				LinkInteractor.instance().updateLinks(includeID, newIncludeID, folder, false);									
			}
		} catch (ExecutionException e) {
			e.printStackTrace();
			throw new RenameDIPException(unit, e.getMessage());
		}
		ResourcesUtilities.updateDipElement(unit);
	}
	
	/**
	 *  Обновляет описание после переименования
	 */
	private static void updateReqDescription(IDipUnit unit, DipDescription description, String content) {
		if (description != null){
			description.delete();
		}
		unit.removeDescription();
		if (content != null && !content.isEmpty()) {
			unit.updateDescription(content);
		}
	}
	
	/**
	 * Обновляет комментарий после переименования
	 */
	private static void updateReqComment(IDipUnit unit, IDipComment comment, String content, List<ITextComment> textComments) {
		// удалить старый коммент
		if (comment != null){
			comment.delete();
		}
		unit.deleteDipComment();
		// создаем новый
		if (content != null && !content.isEmpty()) {
			unit.updateDipComment(content);
		}
		if (textComments != null && !textComments.isEmpty()) {
			unit.updateTextAnnotations(textComments);
		}
	}
	
	public static void renameSchema(DipSchemaElement schema, String newName, Shell shell) throws RenameDIPException{
		if (shell == null){
			shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
		}	
		IFile file = schema.resource();
		IPath newPath = file.getFullPath().removeLastSegments(1).append(newName);
		MoveResourcesOperation mp = new MoveResourcesOperation(file, newPath, "Move resource");
		try {
			IStatus status = mp.execute(null, WorkspaceUndoUtil.getUIInfoAdapter(shell));
			if (!status.isOK()){
				throw new RenameDIPException(schema, status.getMessage());
			}
			IFile newFile = file.getParent().getFile(new Path(newName));
			schema.setResource(newFile);
		} catch (ExecutionException e) {
			e.printStackTrace();
			throw new RenameDIPException(schema, e.getMessage());
		}
		ResourcesUtilities.updateDipElement(schema);
	}
	
	private static void renameFolder(IDipParent dipParent, String newName, boolean reserve, Shell shell) throws RenameDIPException{
		String lastID = relativeProjectID(dipParent);
		String includeID = null;
		if (dipParent.isIncluded()) {
			includeID = relativeIncludeElement(dipParent);
		}
		
		DipCorePlugin.logInfo("Rename folder " + dipParent.id());
		IFolder folder = (IFolder) dipParent.resource();
		IPath newPath = folder.getFullPath().removeLastSegments(1).append(newName);
		try {
			if (reserve) {
				folder.copy(newPath, true, null);
				IFolder newFolder = folder.getParent().getFolder(new Path(newName));
				afterRename(dipParent, newFolder);
				IDipParent oldDipFolder = dipParent.parent().createNewFolder(folder);
				try {
					deleteElement(oldDipFolder, true, shell, NO_TMP);
				} catch (TmpCopyException e) {
					// NOP
				}
			} else {
				folder.move(newPath, true, null);
				IFolder newFolder = folder.getParent().getFolder(new Path(newName));
				afterRename(dipParent, newFolder);
			}
			
			// обновить ссылки
			String newRelativeID = relativeProjectID(dipParent);
			LinkInteractor.instance().updateLinks(lastID, newRelativeID, dipParent.dipProject(), true);
			if (dipParent.isIncluded()) {
				String newIncludeID = relativeIncludeElement(dipParent);								
				IncludeFolder includeFolder = findIncludeFolder(dipParent);
				LinkInteractor.instance().updateLinks(includeID, newIncludeID, includeFolder, true);									
			}
		} catch (CoreException | DeleteDIPException e) {
			e.printStackTrace();
			throw new RenameDIPException(dipParent, e.getMessage());
		}
		ResourcesUtilities.updateDipElement(dipParent.parent());
		WorkbenchUtitlities.updateProjectExplorer();
	}

	private static void renameProject(DipProject dipProject, String newName, Shell shell) throws RenameDIPException {
		DipCorePlugin.logInfo("Rename Project " + dipProject.name());
		IProject project = dipProject.getProject();
		IPath path = project.getFullPath().removeLastSegments(1).append(newName);
		try {
			project.move(path, true, null);
			ResourcesUtilities.updateProject(project);		
			IProject newProject = ResourcesPlugin.getWorkspace().getRoot().getProject(newName);
			afterRenameProject(dipProject, newProject);
		} catch (CoreException e) {
			e.printStackTrace();
			throw new RenameDIPException(dipProject, e.getMessage());
		}
	}
	
	private static void afterRename(IParent renamedFolder, IContainer newContainer){
		if (DipUtilities.isServedFolder(newContainer.getName())) {
			renamedFolder.parent().removeChild(renamedFolder);
			return;
		}
		
		// при переименовании проекта
		if (renamedFolder.parent() == null) {
			renamedFolder.setResource(newContainer);
		} else {		
			renamedFolder.parent().getChild(renamedFolder.name()).setResource(newContainer);
			renamedFolder.setResource(newContainer);
		}
		
		updateChildrenAfterRename(renamedFolder, newContainer);	
	}
	
	private static void afterRenameProject(IParent renamedFolder, IContainer newContainer){
		renamedFolder.setResource(newContainer);
		updateChildrenAfterRename(renamedFolder, newContainer);
		DipRoot.getInstance().getDipProject(newContainer.getProject()).refresh();
	}
	
	private static void updateChildrenAfterRename(IParent renamedFolder, IContainer newContainer) {
		for (IDipElement element: renamedFolder.getChildren()){			
			IResource res = element.resource();
			if (res == null){
				continue;
			}
			String name = element.name();

			if (res instanceof IFile){
				IFile file = newContainer.getFile(new Path(name));
				element.setResource(file);
			}
			if (res instanceof IFolder){
				IFolder folder = newContainer.getFolder(new Path(name));
				element.setResource(folder);
				if (element instanceof IParent){
					afterRename((IParent) element, folder);
				}
			}			
		}	
	}
	
	private static void renameReport(DipElement report, String newName, Shell shell) throws RenameDIPException{
		IFile file = (IFile) report.resource();
		IPath newPath = file.getFullPath().removeLastSegments(1).append(newName);
		MoveResourcesOperation mp = new MoveResourcesOperation(file, newPath, "Move resource");
		try {
			IStatus status = mp.execute(null, WorkspaceUndoUtil.getUIInfoAdapter(shell));
			if (!status.isOK()){
				throw new RenameDIPException(report, status.getMessage());
			}
			IFile newFile = file.getParent().getFile(new Path(newName));
			report.setResource(newFile);
		} catch (ExecutionException e) {
			e.printStackTrace();
			throw new RenameDIPException(report, e.getMessage());
		}
		ResourcesUtilities.updateDipElement(report);
	}
	
	//====================================
	// create tmp-resorces
	
	public static TmpElement tmpUnit(IDipUnit unit) throws TmpCopyException {
		java.nio.file.Path tmpPath = ResourcesUtilities.createTmpCopy(unit.resource());	
		return new TmpElement(unit, tmpPath, unit.type());
	}
	
	//====================================
	// delete
	
	public final static boolean CREATE_TMP = true;
	public final static boolean NO_TMP = false;
	public final static boolean RESERVE = true;
	public final static boolean NO_RESERVE = false;
		
	public static boolean canDeleteElements(Object[] objects){
		for (Object obj:objects){
			if (!canDeleteElement(obj)){
				return false;
			}
		}
		return true;
	}
	
	public static boolean canDeleteElement(Object obj){
		if (obj instanceof IDipElement){
			IDipElement dipElement = (IDipElement) obj;
			DipElementType type = dipElement.type();
			if (dipElement.isReadOnly() && DipElementType.INCLUDE_FOLDER != type ) {
				return false;
			}			
			return DipElementType.RPOJECT == type 
					|| DipElementType.FOLDER == type
					|| DipElementType.INCLUDE_FOLDER == type 
					|| DipElementType.UNIT == type
					|| DipElementType.RESERVED_FOLDER == type 
					|| DipElementType.RESERVED_UNIT == type
					|| DipElementType.REPORT == type 
					|| DipElementType.EXPORT_CONFIG == type;		
		}
		return false;
	}
		
	public static DeleteResultOperation deleteElements(IDipElement[] elements,  boolean reserve, 
			boolean deleteProjectContent,  Shell shell) throws DeleteDIPException, TmpCopyException{
		List<TmpElement> tmpElements = new ArrayList<>();
		for (IDipElement element: elements){
			if (element instanceof DipProject && !deleteProjectContent){
				deleteProjectFromWorkspace((DipProject) element);
			} else {
				TmpElement tmp = deleteElement(element, reserve, shell);
				if (tmp != null) {
					tmpElements.add(tmp);
				}
			}
		}
		return new DeleteResultOperation(tmpElements);
	}
	
	public static void deleteElementsWithoutTmp(IDipElement[] elements,  boolean reserve, 
			Shell shell) throws DeleteDIPException, TmpCopyException{
		for (IDipElement element: elements){
			deleteElement(element, reserve, shell, false);
		}
	}
	
	public static TmpElement deleteElement(IDipElement element, boolean reserve, Shell shell) throws DeleteDIPException, TmpCopyException{
		// пока везде вызываем с false, tmp - файлы не создаем		
		return deleteElement(element, reserve, shell, CREATE_TMP);
	}	
	
	public static TmpElement deleteElement(IDipElement element, boolean reserve, Shell shell, boolean tmp) throws DeleteDIPException, TmpCopyException{		
		if (shell == null){
			shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
		}
		DipElementType type = element.type();
		switch (type){
		case UNIT:{
			return deleteUnit((IDipUnit) element, reserve, shell, tmp);
		}
		case INCLUDE_FOLDER:{
			return deleteIncludeFolder((IncludeFolder) element, shell);
		}
		case FOLDER:{
			return deleteDipFolder((IDipParent) element, reserve, shell, tmp);
		}
		default:{
			return fullDeleteElement(element, shell, tmp);
		}		
		}
	}
	
	public static TmpElement deleteIncludeFolder(IncludeFolder includeFolder, Shell shell) throws DeleteDIPException, TmpCopyException {
		if (includeFolder.isErrorLink()) {
			includeFolder.parent().removeChild(includeFolder);
			return null;
		}
		IncludeTmpElement tmp = new IncludeTmpElement(includeFolder);
		includeFolder.dipProject().removeIncludeFolder(includeFolder);
		fullDeleteElement(includeFolder, shell, false);
		return tmp;
	}
	
	private static TmpElement deleteUnit(IDipUnit dipUnit, boolean reserve, Shell shell, boolean tmp) throws TmpCopyException, DeleteDIPException{	
		TmpElement tmpUnit = null;
		if (tmp) {
			tmpUnit = tmpUnit(dipUnit);
		} 
		
		deleteDipDescription(dipUnit, false);
		deleteDipComment(dipUnit, false);
		
		if (reserve){
			ReservedUtilities.createReserveUnit(shell, dipUnit);
		}
		fullDeleteElement(dipUnit, shell , false);
		return tmpUnit;
	}
	
	/**
	 * Возвращает путь, до временной копии (на случай восстановления)
	 */
	private static java.nio.file.Path deleteDipComment(IDipUnit dipUnit, boolean tmp) throws TmpCopyException{
		IDipComment dipComment = dipUnit.comment();
		java.nio.file.Path tmpPath = null;
		if (dipComment != null){
			if (tmp) {
				tmpPath = ResourcesUtilities.createTmpCopy(dipComment.resource());
			}
			dipComment.delete();
			return tmpPath;
		}
		return null;
	}
	
	/**
	 * Возвращает путь, до временной копии (на случай восстановления)
	 */
	private static java.nio.file.Path deleteDipDescription(IDipUnit dipUnit, boolean tmp) throws TmpCopyException{
		DipDescription description = dipUnit.dipDescription();
		java.nio.file.Path tmpPath = null;
		if (description != null){
			if (tmp) {
				tmpPath = ResourcesUtilities.createTmpCopy(description.resource());
			}
			DipRoot.getInstance().removeElement(description);
			description.delete();
			return tmpPath;
		}
		return null;
	}
	
	public static TmpElement deleteDipFolder(IDipParent dipFolder, boolean reserve, Shell shell, boolean tmp) throws DeleteDIPException, TmpCopyException{
		if (reserve && dipFolder.getDipChildren().length > 0){						
			return reservedFolder(dipFolder, shell, tmp);
		} else {
			return fullDeleteElement(dipFolder, shell, tmp);
		}
	}
	
	private static TmpElement reservedFolder(IDipParent dipFolder, Shell shell, boolean tmp) throws DeleteDIPException, TmpCopyException{
		TmpElement tmpElement = null;
		ReservedUtilities.reserveFolder(shell, dipFolder);
		IFolder folder = (IFolder) dipFolder.resource();
		IDipParent parent = (IDipParent) dipFolder.parent();
		if (parent != null){
			parent.removeChild(dipFolder);
			parent.createReservedFolder(folder);
			if (tmp) {
				java.nio.file.Path tmpPath = ResourcesUtilities.createTmpCopy(dipFolder.resource());
				tmpElement = new TmpElement(dipFolder, tmpPath, dipFolder.type());
			}
		}
		return tmpElement;
	}
	
	public static void deleteDipTable(DnfoTable table, Shell shell) throws DeleteDIPException {
		IFile file = table.resource();
		if (file.exists()) {
			try {
				file.refreshLocal(IResource.DEPTH_ONE, null);
				IStatus status = ResourcesUtilities.deleteResource(file, shell);
				if (!status.isOK()){
					throw new DeleteDIPException(table, status.getMessage());
				}
			} catch (CoreException e) {
				e.printStackTrace();
				throw new DeleteDIPException(table, e.getMessage());
			}
		}
		IParent parent = table.parent();
		if (parent != null) {
			parent.removeChild(table);
		}
	}
	
	private static TmpElement fullDeleteElement(IDipElement element, Shell shell, boolean tmp) throws DeleteDIPException, TmpCopyException{
		TmpElement tmpElement = null;
		try {
			if (tmp && element.type() != DipElementType.RPOJECT) {
				java.nio.file.Path tmpPath = ResourcesUtilities.createTmpCopy(element.resource());				
				tmpElement = new TmpElement(element, tmpPath, element.type());
				if (element instanceof IDipParent) {
					tmpElement.setDescription(((IDipParent) element).description());
					tmpElement.setComment(((IDipParent) element).getCommentContent());
				}
			}
			DipRoot.getInstance().removeElement(element);
			IStatus status = ResourcesUtilities.deleteResource(element.resource(), shell);
			WorkbenchUtitlities.closeEditorsIfInputNotExists();
			
			if (!status.isOK()){
				throw new DeleteDIPException(element, status.getMessage());
			}
		} catch (CoreException e) {
			e.printStackTrace();
			throw new DeleteDIPException(element, e.getMessage());
		}
				
		IParent parent = element.parent();
		if (parent != null){
			parent.removeChild(element);		
		}
		return tmpElement;
	}	
	
	public static void deleteProjectFromWorkspace(DipProject project) throws DeleteDIPException{
		try {
			DipCorePlugin.logInfo("Delete project" + project.resource().getLocation());
			DipRoot.getInstance().removeProject(project);
			DipRoot.getInstance().removeElement(project);
			project.getProject().delete(false, true, null);
		} catch (CoreException e) {
			e.printStackTrace();
			throw new DeleteDIPException(project, e.getMessage());
		}
	}
	
	//====================================
	// extract elements from folder
	
	public static IStatus canExtract(IDipParent folder) {
		List<IDipDocumentElement> sources = folder.getDipDocChildrenList();
		IDipParent parent = folder.parent();
		for (IDipDocumentElement source: sources) {
			IStatus status = canPaste(parent, source);
			if (!status.isOK()) {
				if (status instanceof DipStatus) {
					((DipStatus)status).setMessage(source.dipName() + ": " + status.getMessage());
				}
				return status;
			}			
		}
		return DipStatus.OK_STATUS;
	}
	
	public static ExtractResult moveUp(IDipParent folder, boolean reserve, Shell shell) throws DeleteDIPException, CopyDIPException {
		IDipParent target  = folder.parent();
		List<IDipDocumentElement> sources = new ArrayList<>(folder.getDipDocChildrenList());
		int index =  folder.getIndex();
		List<MoveResult> moveResults = new ArrayList<>();		
		for (IDipDocumentElement source: sources) {						
			MoveResult moveResult = moveElement(source, target, reserve, index, shell);
			moveResults.add(moveResult);
			index++;
		}
		TmpElement folderTmp = deleteDipFolder(folder, reserve, shell, true);				
		return new ExtractResult(moveResults, folderTmp, DipUtilities.relativeProjectID(folder));
	}
	
	public static void moveUpWithoutTmp(IDipParent folder, boolean reserve, Shell shell) throws DeleteDIPException, CopyDIPException {
		IDipParent target  = folder.parent();
		List<IDipDocumentElement> sources = new ArrayList<>(folder.getDipDocChildrenList());
		int index =  folder.getIndex();
		List<MoveResult> moveResults = new ArrayList<>();		
		for (IDipDocumentElement source: sources) {						
			MoveResult moveResult = moveElement(source, target, reserve, index, shell);
			moveResults.add(moveResult);
			index++;
		}
		deleteDipFolder(folder, reserve, shell, false);				
	}
	
	public static MoveResult moveElement(IDipDocumentElement moved, IDipParent target, 
			boolean reserve, int index, Shell shell) throws DeleteDIPException, CopyDIPException {		
		if (moved instanceof IDipUnit) {
			index = DipTableUtilities.getLastUnitIndex(target) + 1;
		}
		String oldParentID = moved.parent().id();
		int oldIndex = DipTableUtilities.getIndex(moved);
		IDipElement newElement = DipUtilities.copyElement(target, moved, index, shell);
		DipUtilities.deleteElement(moved, reserve, shell, NO_TMP);
		LinkInteractor.instance().updateLinks(moved, target.getChild(moved.name()));
		return new MoveResult(newElement.name(), oldParentID, target.id(), oldIndex);
	}
	
	//====================================
	// find dip element
	
	public static IDipElement findElement(String path){
		String[] names = path.split(TagStringUtilities.PATH_SEPARATOR_REGEX);
		if (names.length > 0){
			String projectName = names[0];
			IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
			DipProject dipProject = DipRoot.getInstance().findDipProject(project);
			IDipElement resultElement = dipProject;
			for (int i = 1; i < names.length; i++){
				if (resultElement instanceof IParent){				
					resultElement = ((IParent) resultElement).getChild(names[i]);
					if (resultElement == null){
						return null;
					}
				} else {
					return null;
				}
			}
			return resultElement;
		}
		return null;		
	}
	
	public static IDipElement findElement(IDipElement element, String link) {
		// преобразовать относительную ссылку
		if (link.startsWith("./")) {
			IParent parent = element.parent();
			if (parent == null) {
				return null;
			}		
			link = link.substring(1);	
			while (link.startsWith("/../")) {
				// для вложенных папок
				if (parent instanceof IncludeFolder) {
					IncludeFolder include = (IncludeFolder) parent;
					IDipElement findElement = findElement(include.parent(), link.substring(4));
					return findElement;
				}				
				parent = parent.parent();
				if (parent == null) {
					return null;
				}
				link = link.substring(3);
			}
			link = parent.resource().getProjectRelativePath().toString()
					+ link;	
			if (link.startsWith("/")) {
				link = link.substring(1);
			}
		}
		
		IDipElement resultElement =  findElement(element.dipProject(), link);
		if (resultElement == null) {
			if (element != null && element.isIncluded()) {
				IncludeFolder includefolder = DipUtilities.findIncludeFolder(element);
				return findElement(includefolder, link);
			}
		}
		return resultElement;
	}
	
	public static IDipElement findDiffElement(IParent parent, String path){
		String[] segments = path.split(TagStringUtilities.PATH_SEPARATOR_REGEX);
		IDipElement resultElement = parent;
		if (segments.length == 1 && segments[0].isEmpty()) {
			return parent;
		}
		for (String segment: segments) {
			if (resultElement instanceof IDipParent) {
				resultElement = ((IDipParent)resultElement).getChild(segment);
			} else {
				resultElement = null;
				break;
			}
		}
		
		DipProject project = parent.dipProject();
		List<IncludeFolder> folders = project.getIncludeFolders();

		if (resultElement != null || segments.length == 0 || folders.isEmpty()) {
			return resultElement;
		}
		
		// поиск в инклюдах
		for (IncludeFolder includeFolder: folders) {			
			java.nio.file.Path includeRelativePath = Paths.get(includeFolder.getLinkRelativePath());
			java.nio.file.Path searchObjectPath = Paths.get(path);
			if (searchObjectPath.startsWith(includeRelativePath)) {
				java.nio.file.Path searchRelativeInclude = includeRelativePath.relativize(searchObjectPath);
				String relativeInclude = DipUtilities.relativeProjectID(includeFolder);
				String relativeProject = Paths.get(relativeInclude).resolve(searchRelativeInclude).toString();			
				// нужно relative от parent )
				// пока подразумевается, что parent - это project
				return findDiffElement(parent, relativeProject);
			}
		}
		return resultElement;
	}
	
	private static IDipElement findElement(IParent project, String path){
		String[] segments = path.split(TagStringUtilities.PATH_SEPARATOR_REGEX);
		IDipElement resultElement = project;
		for (String segment: segments) {
			if (resultElement instanceof IDipParent) {
				resultElement = ((IDipParent)resultElement).getChild(segment);
			} else {
				resultElement = null;
				break;
			}
		}
		return resultElement;
	}
	
	public static IDipElement findElement(IResource resource){
		IProject project = resource.getProject();
		DipProject dipProject = DipRoot.getInstance().findDipProject(project);
		if (dipProject != null){
			return findDipElementInProject(resource, dipProject);
		}
		return null;	
	}
	
	public static IDipElement findDipElementInProject(IResource res, DipProject project){
		if (res instanceof IProject && res.equals(project.resource())){
			return project;
		}		
		IDipElement result = project;		
		for (String segment: res.getProjectRelativePath().segments()){
			if (result == null){
				return null;
			}			
			if (result instanceof IParent){
				IDipElement element = ((IParent) result).getChild(segment);
				if (element == null && segment.endsWith(".report") && result instanceof DipFolder) {
					result = ((DipFolder) result).getOrCreateReportContainer().getChild(segment);
				} else {
					result = element;
				}
			}
		}
		if (result == null || result.resource() == null) {
			return null;
		}
		if (result.resource().equals(res)){
			return result;
		} else {
			return null;
		}	
	}
	
	public static  Optional<IDipElement> findDipElementInProject(String relativePath, DipProject project){
		if (relativePath.isEmpty()) {
			return Optional.of(project);
		}
		
		IDipElement result = project;	
		IPath path = new Path(relativePath);
		for (String segment: path.segments()){
			if (result == null){
				return Optional.empty();
			}			
			if (result instanceof IParent){
				result = ((IParent) result).getChild(segment);
			}			
		}
		if (result == null){
			return Optional.empty();
		}
		return Optional.of(result);
	}
	
	public static DipProject findDipProject(IResource resource) {
		IProject project = resource.getProject();
		return DipRoot.getInstance().findDipProject(project);
	}
	
	public static boolean containsElement(IDipParent parent, IDipDocumentElement element){
		java.nio.file.Path path = Paths.get(parent.resource().getLocation().toOSString(), element.name());
		return Files.exists(path);
	}
	
	public static boolean containsReservedElement(IDipParent parent, IDipElement element){
		java.nio.file.Path path = Paths.get(parent.resource().getLocation().toOSString(), element.name() + ReservedUtilities.RSVD_EXTENSION);
		return Files.exists(path);
	}
	
	public static DipProject findDipProjectInWorkspace(String projectName) {
		IProject project = findProject(projectName);
		if (project != null) {
			return DipUtilities.findDipProject(project);
		}		
		return null;
	}
	
	private static IProject findProject(String projectName) {
		for (IProject project: ResourcesPlugin.getWorkspace().getRoot().getProjects()) {
			if (project.getName().equals(projectName)) {
				return project;
			}
			if (Paths.get(project.getLocationURI()).getFileName().toString().equals(projectName)){
				return project;
			}						
		}
		return null;
	}
	
	/**
	 * Возвращает все Dip-проекты которые содержатся в указанной директории
	 */
	public static List<DipProject> findProjectsInFolder(java.nio.file.Path folderPath){		
		List<DipProject> result = new ArrayList<>();
		for (DipProject project: DipRoot.getInstance().getProjects()) {
			if (project.resource() == null || project.resource().getLocationURI() == null) {
				continue;
			}
			// иногда падает с null pointer вероятно project.resource()
			java.nio.file.Path projectPath = Paths.get(project.resource().getLocationURI());
			if (projectPath.startsWith(folderPath)) {
				result.add(project);				
			}					
		}				
		return result;
	}
	
	
	/**
	 * Возвращает всю цепочку родителей, от текущего элемента (если он родитель), до DipProject включительно
	 */
	public static List<IDipParent> getParentsChain(IDipDocumentElement dde) {
		List<IDipParent> parents = new ArrayList<>();
		IDipParent parent = dde instanceof IDipParent ? (IDipParent) dde : dde.parent();
		while (parent != null) {
			parents.add(parent);
			if (parent instanceof DipProject) {
				break;
			}			
			parent = parent.parent();						
		}
		return parents;
	}
	
	
	//================================
	// NewProject
	
	public static IStatus checkProjectName(String name) {
		IStatus status = checkReqName(name);
		if (!status.isOK()) {
			return status;
		}
		return canCreateProject(name);
	}
	
	public static DipProject createDemoProject(String name, Shell shell) {
		ResourcesUtilities.copyFolderToWorkspace(getDemoProjectPath().toString(), name);
		String projectPath = Paths.get(ResourcesUtilities.workspacePath(), name).toString();
		DipProject dipProject = importDipProject(name, projectPath, false, shell);
		return dipProject;
	}
	
	public static java.nio.file.Path getDemoProjectPath() {
		try {
			java.nio.file.Path demo = ResourcesUtilities.getPathFromPlugin(DipCorePlugin.getDefault(), "project_content/demo");
			return demo;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static DipProject createStandartProject(String name, Shell shell) {
		ResourcesUtilities.copyFolderToWorkspace(getStandartProjectPath().toString(), name);
		String projectPath = Paths.get(ResourcesUtilities.workspacePath(), name).toString();
		DipProject dipProject = importDipProject(name, projectPath, false, shell);
		return dipProject;
	}
	
	public static java.nio.file.Path getStandartProjectPath() {
		try {
			java.nio.file.Path demo = ResourcesUtilities.getPathFromPlugin(DipCorePlugin.getDefault(), "project_content/tmpl");
			return demo;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	//=================================
	// Import DipProject
	
	public static IStatus canImportProject(String projectPath){
		java.nio.file.Path path = Paths.get(projectPath);
		if (!Files.exists(path)){
			return StatusUtils.NOT_FOLDER_EXISTS;
		}
		if (!Files.isDirectory(path)){
			return StatusUtils.INVALID_FOLDER;
		}
		
		String name = path.getFileName().toString();		
		IStatus result = checkReqName(name);
		if (!result.isOK()){
			return result;
		}
		return DipStatus.OK_STATUS;
	}
	
	public static DipProject importDipProject(String name, String projectPath, boolean copy, Shell shell){
		IProject project = importProject(name, projectPath, copy, shell); 
		if (project != null){
			DipProject dipProject = DipRoot.getInstance().getDipProject(project);
			DipProjectResourceCreator.checkSchemaFolder(shell, dipProject);
			DipNatureManager.checkNature(dipProject);
			ResourcesUtilities.updateProject(project);
			SchemaUtilities.updateProperties(dipProject);		
			return dipProject;
		}		
		return null;
	}
	
	public static IProject importProject(String name, String projectPath, boolean copy, Shell shell) {
		if (copy){
			return importProject(projectPath, shell);
		} else {
			return importLinkProject(name, projectPath);
		}
	}
	
	public static IProject importProject(String projectPath, Shell shell){		
		java.nio.file.Path path = Paths.get(projectPath);
		String name = path.getFileName().toString();		
		try {
			ResourcesUtilities.copyFolderToWorkspace(projectPath);
			IProject project = ResourcesUtilities.createProject(name, shell, DipNatureManager.NATURE_ID);
			return project;

		} catch (CoreException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static IProject importLinkProject(String name, String projectPath){
		java.nio.file.Path path = Paths.get(projectPath);
		try {			
			IProjectDescription description = null;
			java.nio.file.Path projectFilePath = path.resolve(".project");
			if (!Files.exists(projectFilePath)){
				IWorkspace workspace = ResourcesPlugin.getWorkspace();
				description = workspace.newProjectDescription(name);	
				description.setLocation(new Path(path.toAbsolutePath().toString()));			
			} else {
				description = ResourcesPlugin.getWorkspace().loadProjectDescription(new Path(projectFilePath.toAbsolutePath().toString()));
				description.setName(name);
			}
			IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(description.getName());			
			project.create(description, null);		
			project.open(null);
			return project;
		} catch (CoreException e) {
			e.printStackTrace();
		}	
		return null;
	}
	
	//=================================
	// Include
	
	public static boolean inIncludeFolder(IDipElement element) {
		while (!(element instanceof DipProject)) {		
			if (element instanceof IncludeFolder) {
				return true;
			}
			element = element.parent();
		}			
		return false;
	}
	
	public static IncludeFolder findIncludeFolder(IDipElement element) {
		while (!(element instanceof DipProject)) {		
			if (element instanceof IncludeFolder) {
				return (IncludeFolder) element;
			}
			element = element.parent();
		}				
		return null;
	}
	
	public static String includeElementFullID(IDipElement element) {
		IncludeFolder includeFolder = findIncludeFolder(element);
		String includeFolderName = includeFolder.dipName();
		if (element instanceof IncludeFolder) {
			return includeFolderName;
		}		
		StringBuilder builder = new StringBuilder();
		builder.append(includeFolderName);
		builder.append(TagStringUtilities.PATH_SEPARATOR);
		String relative = relativeID(element, includeFolder);
		builder.append(relative);
		return builder.toString();
	}
	
	public static String relativeIncludeElement(IDipElement element) {
		IncludeFolder findIncludeFolder = findIncludeFolder(element);
		return relativeID(element, findIncludeFolder);
	}
	
	public static String readOnlyOpenMessage(IDipDocumentElement unit) {
		StringBuilder builder = new StringBuilder();
		builder.append("Файл ");
		builder.append(unit.name());
		builder.append(" предназначен только для чтения. Редактирование файла запрещено. ");
		IncludeFolder include = findIncludeFolder(unit);
		if (include != null) {
			builder.append("Для редактирования файла откройте его в проекте ");
			builder.append(include.name());
			builder.append(".");
		}
		return builder.toString();
	}
	
	
	//=================================
	//  ID
	
	public static String fullID(IDipElement element) {
		String fullIDWithoutRevision = fullIDWithoutRevision(element);
		StringBuilder builder = new StringBuilder(fullIDWithoutRevision);
		IResource res = element.resource();
		String hash = GITUtilities.getFullId(res);
		if (hash != null) {
			builder.append("@");
			builder.append(hash);
		}
		return builder.toString();
	}
	
	public static String fullIDWithoutRevision(IDipElement element) {
		ArrayList<String> segments = new ArrayList<>();
		IDipElement segmentElement = element;
		while (segmentElement != null) {
			segments.add(0, segmentElement.dipName());		
			if (segmentElement instanceof DipProject) {
				break;
			}			
			segmentElement = segmentElement.parent();
		}
		StringBuilder builder = new StringBuilder();
		for (String segment: segments) {
			builder.append(segment);
			builder.append(Path.SEPARATOR);
		}
		builder.deleteCharAt(builder.length() - 1);
		return builder.toString();
	}
	
	public static String relativeProjectID(IDipElement element) {
		ArrayList<String> segments = new ArrayList<>();
		IDipElement segmentElement = element;
		while (segmentElement != null) {
			segments.add(0, segmentElement.dipName());
			segmentElement = segmentElement.parent();
			if (segmentElement instanceof DipProject) {
				break;
			}
		}
		StringBuilder builder = new StringBuilder();
		for (String segment: segments) {
			builder.append(segment);
			builder.append(Path.SEPARATOR);
		}
		builder.deleteCharAt(builder.length() - 1);		
		return builder.toString();
	}
	
	public static String relativeID(IDipElement element, IParent parent){
		java.nio.file.Path parentPath = Paths.get(parent.resource().getLocationURI());
		java.nio.file.Path childPath = Paths.get(element.resource().getLocationURI());
		return parentPath.relativize(childPath).toString();		
	}

	public static String copyFullIdClipboard(IDipElement element, Display display) {
		if (element != null){
			String fullID = fullIDWithoutRevision(element);				
			WorkbenchUtitlities.setToClipboard(fullID, display);
			return fullID;
		}
		return null;
	}
	
	public static String copyFullIdRevisionClipboard(IDipElement element, Display display) {
		if (element != null){
			String fullID = fullID(element);				
			WorkbenchUtitlities.setToClipboard(fullID, display);
			return fullID;
		}
		return null;
	}
	
	
	
	public static String copyRelativeIdClipboard(IDipElement element, Display display) {
		if (element != null){
			String relativeId = relativeProjectID(element);
			WorkbenchUtitlities.setToClipboard(relativeId, display);
			return relativeId;
		}
		return null;
	}
	
	public static void copyRelativeIdsClipboard(Stream<? extends IDipElement> elements, Display display) {				
		String ids = elements.map(DipUtilities::relativeProjectID).collect(Collectors.joining("\n"));
		if (!ids.isEmpty()) {
			WorkbenchUtitlities.setToClipboard(ids, display);
		}
	}
	
	public static void copyFullIdsClipboard(Stream<? extends IDipElement> elements, Display display) {
		String ids = elements.map(DipUtilities::fullID).collect(Collectors.joining("\n"));
		if (!ids.isEmpty()) {
			WorkbenchUtitlities.setToClipboard(ids, display);
		}
	}
	
	//====================================
	// save table
	
	public static void saveTable(IDipParent parent) throws SaveTableDIPException {
		try {
			TableWriter.saveModel(parent);
			ResourcesUtilities.updateProject(parent.resource());
		} catch (ParserConfigurationException | IOException e) {
			e.printStackTrace();
			throw new SaveTableDIPException(parent, e.getMessage());
		}
	}
	
	//=======================================
	// Count units
	
	/**
	 * Скитает количество DipUnit (за исключением disable
	 */
	public static long countUnits(IDipParent parent) {
		long result = parent.getDipDocChildrenList().stream()
			.filter(IDipUnit.class::isInstance)
			.filter(u -> !u.isDisabled())
			.count();
		result += parent.getDipDocChildrenList().stream()
			.filter(IDipParent.class::isInstance)
			.filter(p -> !p.isDisabled())
			.map(IDipParent.class::cast)
			.mapToLong(DipUtilities::countUnits)
			.sum();
		return result;
	}
	
	
}
