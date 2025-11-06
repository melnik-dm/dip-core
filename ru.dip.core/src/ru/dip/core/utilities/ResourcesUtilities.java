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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.actions.CopyFilesAndFoldersOperation;
import org.eclipse.ui.ide.undo.CreateFileOperation;
import org.eclipse.ui.ide.undo.CreateFolderOperation;
import org.eclipse.ui.ide.undo.CreateProjectOperation;
import org.eclipse.ui.ide.undo.DeleteResourcesOperation;
import org.eclipse.ui.ide.undo.WorkspaceUndoUtil;
import org.osgi.framework.Bundle;

import ru.dip.core.DipCorePlugin;
import ru.dip.core.exception.TmpCopyException;
import ru.dip.core.model.interfaces.IDipElement;
import ru.dip.core.utilities.status.ResultStatus;
import ru.dip.core.utilities.status.StatusUtils;
 
public class ResourcesUtilities {

	//===================================
	// переделать путь для Windows
	
	public static boolean isWindows = false;
	
	static {
		String os = System.getProperty("os.name");
		if (os != null && os.startsWith("Windows")){
			isWindows = true;
		}
	}
	
	public static String checkWindowsPath(String path) {
		if (isWindows && path.startsWith("/")) {
			return path.substring(1);
		}
		return path;
	}
	
	//==================================
	// Регистро-независимые операции
	
	public static boolean contains(IContainer container, String filename) throws CoreException, IOException {		
		return containsEclipseApi(container, filename) 
				|| containsNioApi(container, filename);
	}
	
	public static boolean containsDisableObject(IContainer container, String filename) throws CoreException, IOException {
		String disFilename = "dis." + filename;
		return contains(container, disFilename);
		
	}
	
	public static boolean containsFile(IContainer container, String filename) throws CoreException, IOException {
		return containsFileEclipseApi(container, filename) 
				|| containsFileNioApi(container, filename);
	}
	
	public static boolean containsFolder(IContainer container, String filename) throws CoreException, IOException {
		return containsFolderEclipseApi(container, filename) 
				|| containsFolderNioApi(container, filename);
	}
	
	private static boolean containsEclipseApi(IContainer container, String filename) throws CoreException {
		return Stream.of(container.members())
			.map(IResource::getName)
			.anyMatch(filename::equalsIgnoreCase);
	}
	
	
	private static boolean containsFileEclipseApi(IContainer container, String filename) throws CoreException {
		return Stream.of(container.members())
			.filter(IFile.class::isInstance)
			.map(IResource::getName)
			.anyMatch(filename::equalsIgnoreCase);
	}
	
	private static boolean containsFolderEclipseApi(IContainer container, String filename) throws CoreException {
		return Stream.of(container.members())
			.filter(IFolder.class::isInstance)
			.map(IResource::getName)
			.anyMatch(filename::equalsIgnoreCase);
	}
	
	private static boolean containsNioApi(IContainer container, String filename) throws IOException {		
		return Files.list(Paths.get(container.getLocationURI()))
			.map(Path::getFileName)
			.map(Path::toString)
			.anyMatch(filename::equalsIgnoreCase);
	}
	
	private static boolean containsFileNioApi(IContainer container, String filename) throws IOException {		
		return Files.list(Paths.get(container.getLocationURI()))
			.filter(Files::isRegularFile)
			.map(Path::getFileName)
			.map(Path::toString)
			.anyMatch(filename::equalsIgnoreCase);
	}
	
	private static boolean containsFolderNioApi(IContainer container, String filename) throws IOException {		
		return Files.list(Paths.get(container.getLocationURI()))
			.filter(Files::isDirectory)
			.map(Path::getFileName)
			.map(Path::toString)
			.anyMatch(filename::equalsIgnoreCase);
	}

	
	public static IFile getFile(IContainer container, String filename) {
		try {
			for (IResource resource: container.members()){
				if (resource instanceof IFile && filename.equalsIgnoreCase(resource.getName())) {
					return (IFile) resource;					
				}
			}
		} catch (CoreException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static IFolder getFolder(IContainer container, String filename) {
		try {
			for (IResource resource: container.members()){
				if (resource instanceof IFolder && filename.equalsIgnoreCase(resource.getName())) {
					return (IFolder) resource;					
				}
			}
		} catch (CoreException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static String[] getChildrenNames(IContainer resource) throws CoreException {
		return Stream
				.of(resource.members())					
				.map(IResource::getName)
				.map(String::toLowerCase)
				.toArray(String[]::new);
	}
	
	// ===================================
	// Получить, скопировать файл из плагина

	public static Path getPathFromPlugin(Plugin plugin, String path) throws IOException {
		URL url = plugin.getBundle().getResource(path);
		if (url == null) {
			return null;
		}
		String source = FileLocator.toFileURL(url).getPath();
		source = checkWindowsPath(source);
		return Paths.get(source).normalize();
	}

	public static URI getURIfromPlugin(Plugin plugin, String path) throws IOException {
		return getPathFromPlugin(plugin, path).toUri();
	}
	
	public static Path metadataPluginPath(Plugin plugin, String folder) {
		Bundle bundle = plugin.getBundle();
		IPath pathMetaData = Platform.getStateLocation(bundle);
		String metaData = pathMetaData.toOSString();
		if (folder == null) {
			return Paths.get(metaData);
		} else {
			return Paths.get(metaData, folder);
		}
	}
	

	// =================================
	// Создать файл/ папку

	public static IProject createProject(String name, Shell shell, String nature) throws CoreException {
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		WorkspaceJob job = new WorkspaceJob("CreateProject ") {
			
			@Override
			public IStatus runInWorkspace(IProgressMonitor monitor) throws CoreException {
				IProjectDescription description = workspace.newProjectDescription(name);
				String[] natures = new String[] { nature };
				description.setNatureIds(natures);
				CreateProjectOperation operation = new CreateProjectOperation(description, "New Project");
				try {
					operation.execute(null, WorkspaceUndoUtil.getUIInfoAdapter(shell));
					return Status.OK_STATUS;
				} catch (ExecutionException e) {
					e.printStackTrace();
					return StatusUtils.errorStatus(e);
				}				
			}
		};
		
		job.runInWorkspace(null);
		return root.getProject(name);
	}

	public static IProject createProject(String name, URI location, Shell shell, String nature) throws CoreException {
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IWorkspace workspace = ResourcesPlugin.getWorkspace();	
		WorkspaceJob job = new WorkspaceJob("CreateProject ") {
			
			@Override
			public IStatus runInWorkspace(IProgressMonitor monitor) throws CoreException {
				IProjectDescription description = workspace.newProjectDescription(name);
				description.setLocationURI(location);
				String[] natures = new String[] { nature };
				description.setNatureIds(natures);
				CreateProjectOperation operation = new CreateProjectOperation(description, "New Project");
				try {
					operation.execute(null, WorkspaceUndoUtil.getUIInfoAdapter(shell));
					return Status.OK_STATUS;
				} catch (ExecutionException e) {
					e.printStackTrace();
					return StatusUtils.errorStatus(e);
				}
			}
		};
		
		job.runInWorkspace(null);
		return root.getProject(name);
	}

	public static IFile createFile(IContainer parent, String fileName, Shell shell) throws CoreException {
		IFile file = parent.getFile(new org.eclipse.core.runtime.Path(fileName));
		return createFile(file, shell);
	}

	public static IFile createFile(IContainer parent, String fileName, String content, Shell shell)
			throws CoreException {
		IFile file = parent.getFile(new org.eclipse.core.runtime.Path(fileName));
		return createFile(file, content, shell);
	}

	public static IFile createFile(IFile file, Shell shell) throws CoreException {
		if (!file.exists()) {
			WorkspaceJob job = new WorkspaceJob("Create file") {
				
				@Override
				public IStatus runInWorkspace(IProgressMonitor monitor) throws CoreException {
					try {
						CreateFileOperation createFileOperation = new CreateFileOperation(file, null, null, "New File");
						createFileOperation.execute(null, WorkspaceUndoUtil.getUIInfoAdapter(shell));
						return Status.OK_STATUS;
					} catch (ExecutionException e) {
						e.printStackTrace();
						return StatusUtils.errorStatus(e);
					}					
				}
			};
			job.runInWorkspace(null);
		}
		return file;
	}

	public static IFile createFile(IFile file, String content, Shell shell) throws CoreException {
		if (!file.exists()) {
			WorkspaceJob job = new WorkspaceJob("Create file") {
				
				@Override
				public IStatus runInWorkspace(IProgressMonitor monitor) throws CoreException {
					try {
						InputStream streamContent = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
						CreateFileOperation createFileOperation = new CreateFileOperation(file, null, streamContent, "New File");
						createFileOperation.execute(null, WorkspaceUndoUtil.getUIInfoAdapter(shell));
						return Status.OK_STATUS;
					} catch (ExecutionException e) {
						e.printStackTrace();
						return StatusUtils.errorStatus(e);
					}					
				}
			};
			job.runInWorkspace(null);
		}
		return file;
	}

	public static IFolder createFolder(IContainer parent, String folderName, Shell shell) throws CoreException {
		IFolder folder = parent.getFolder(new org.eclipse.core.runtime.Path(folderName));
		return createFolder(folder, shell);
	}

	public static IFolder createFolder(IFolder folder, Shell shell) throws CoreException {
		if (!folder.exists()) {
			
			WorkspaceJob job = new WorkspaceJob("create folder") {
				
				@Override
				public IStatus runInWorkspace(IProgressMonitor monitor) throws CoreException {
					try {
						CreateFolderOperation createFolderOperation = new CreateFolderOperation(folder, null, "New Folder");
						createFolderOperation.execute(null, WorkspaceUndoUtil.getUIInfoAdapter(shell));
						return Status.OK_STATUS;
					} catch (ExecutionException e) {
						e.printStackTrace();
						return StatusUtils.errorStatus(e);
					}
				}
			};
			job.runInWorkspace(null);
		}
		return folder;
	}

	// ============================
	// копирование

	public static void copyResources(IContainer destination, URI[] sources, Shell shell) throws CoreException {
		updateProject(destination);
		WorkspaceJob job = new WorkspaceJob("Copy resources") {
			
			@Override
			public IStatus runInWorkspace(IProgressMonitor monitor) throws CoreException {		
				CopyFilesAndFoldersOperation copyOperation = new CopyFilesAndFoldersOperation(shell);
				copyOperation.copyFiles(sources, destination);
				return Status.OK_STATUS;
			}
		};
		job.runInWorkspace(null);
	}

	public static IResource[] copyResources(IContainer destination, IResource source, Shell shell) throws CoreException {
		updateProject(destination);
		WorkspaceJob job = new WorkspaceJob("Copy resources") {
			
			@Override
			public IStatus runInWorkspace(IProgressMonitor monitor) throws CoreException {
				CopyFilesAndFoldersOperation copyOperation = new CopyFilesAndFoldersOperation(shell);
				IResource[] result = copyOperation.copyResources(new IResource[] { source }, destination);;				
				return new ResultStatus(result);	
			}
		};
		IStatus status = job.runInWorkspace(null);
		return (IResource[]) ((ResultStatus) status).getData();
	}

	public static String copyFolderToWorkspace(String folderPath) {
		File importFolder = new File(folderPath);
		File workspaceFolder = new File(workspacePath());
		File projectFolder = new File(workspaceFolder, importFolder.getName());
		try {
			copyFolder(importFolder, projectFolder);
			return projectFolder.getAbsolutePath();
		} catch (IOException e) {

		}
		return null;
	}
	
	public static String copyFolderToWorkspace(String folderPath, String destinationName) {
		File importFolder = new File(folderPath);
		File workspaceFolder = new File(workspacePath());
		File projectFolder = new File(workspaceFolder, destinationName);
		try {
			copyFolder(importFolder, projectFolder);
			return projectFolder.getAbsolutePath();
		} catch (IOException e) {

		}
		return null;
	}

	public static void copyFolder(File source, File destination) throws IOException {
		Files.copy(source.toPath(), destination.toPath());
		for (File iter : source.listFiles()) {
			File dest = new File(destination, iter.getName());
			if (iter.isDirectory()) {
				copyFolder(iter, dest);
			} else {
				Files.copy(iter.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
			}
		}
	}

	public static void copyFolder(Path source, Path destination) throws IOException {
		if (!Files.exists(destination)) {
			Files.copy(source, destination);
		}
		Files.list(source).forEach((path) -> {
			Path name = path.getFileName();
			Path dest = destination.resolve(name);
			if (Files.isDirectory(path)) {
				try {
					copyFolder(path, dest);
				} catch (IOException e) {
					e.printStackTrace();
				}
			} else {
				try {
					Files.copy(path, dest, StandardCopyOption.REPLACE_EXISTING);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
	}

	public static Path createTmpCopy(IResource resource) throws TmpCopyException {
		if (resource instanceof IFile) {
			return createTmpCopy((IFile)resource);
		} else if (resource instanceof IContainer) {
			return createTmpCopy((IContainer)resource);
		}
		return null;
	}
	
	/**
	 * Создать временную копию
	 * @throws TmpCopyException 
	 */
	private static Path createTmpCopy(IFile file) throws TmpCopyException {	
		try {		
			Path tmp = Files.createTempFile(file.getName(), ".tmp");
			Files.copy(Paths.get(file.getLocationURI()), tmp, StandardCopyOption.REPLACE_EXISTING);
			return tmp;
		} catch (IOException e) {
			e.printStackTrace();
			throw new TmpCopyException(file, e.getMessage());
		}
	}	
	
	private static Path createTmpCopy(IContainer container) throws TmpCopyException {
		Path path = Paths.get(container.getLocationURI());
		try {
			Path destination = Files.createTempDirectory(container.getName());		
			copyFolder(path, destination);
			return destination;
		} catch (IOException e) {
			e.printStackTrace();
			throw new TmpCopyException(container, e.getMessage());
		}

	}
	
	public static void copyContent(IFile target, java.nio.file.Path inputPath ) throws IOException {
		java.nio.file.Path  outputPath = Paths.get(target.getLocationURI());
		Files.copy(inputPath, outputPath, StandardCopyOption.REPLACE_EXISTING);
	}
	
	// =============================
	// удалить

	public static IStatus deleteResource(IResource[] resources, Shell shell) throws CoreException {
		DipCorePlugin.logInfo("Delete " + Arrays.toString(resources));
		final Shell finalShell = WorkbenchUtitlities.checkShell(shell);
		if (resources.length > 0) {
			ResourcesUtilities.updateProject(resources[0]);
		}
		WorkspaceJob job = getDeleteJob(finalShell, resources);
		return job.runInWorkspace(null); 
	}

	public static IStatus deleteResource(IResource resource, Shell shell) throws CoreException {
		DipCorePlugin.logInfo("Delete " + resource.getLocation());
		if (resource == null || !resource.exists()) {
			return null;
		}
		IResource[] resources = new IResource[] { resource };
		ResourcesUtilities.updateProject(resource);
		WorkspaceJob job = getDeleteJob(shell, resources);
		return job.runInWorkspace(null);
	}
	
	private static WorkspaceJob getDeleteJob(Shell shell, IResource[] resources) {
		return new WorkspaceJob("Delete") {
			
			@Override
			public IStatus runInWorkspace(IProgressMonitor monitor) throws CoreException {
				DeleteResourcesOperation deleteOperation = new DeleteResourcesOperation(resources, "Delete", true);		
				try {
					return deleteOperation.execute(null, WorkspaceUndoUtil.getUIInfoAdapter(shell));
				} catch (ExecutionException e) {
					return StatusUtils.errorStatus(e);
				}
			}
		};
	}
	
	// ==============================
	// Обновить

	public static void updateRoot() {
		Display.getDefault().syncExec(() -> {
				try {
					ResourcesPlugin.getWorkspace().getRoot().refreshLocal(IResource.DEPTH_INFINITE, null);
				} catch (CoreException e) {
					e.printStackTrace();
				}
		});
	}

	public static void updateDipElement(IDipElement element) {
		updateProject(element.resource());
	}

	public static void updateProject(IResource resource) {
		IProject project = resource.getProject();
		try {
			project.refreshLocal(IResource.DEPTH_INFINITE, null);		
		} catch (CoreException e) {
			// NOP
			e.printStackTrace();
		}
	}
	
	public static void updateContainer(IContainer container) {
		try {
			container.refreshLocal(IResource.DEPTH_INFINITE, null);
		} catch (CoreException e) {
			// NOP
			e.printStackTrace();
		}
	}
	
	public static void updateProject(Path projectPath) {
		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectPath.getFileName().toString());
		if (project != null) {
			updateProject(project);
		}		
	}

	// ===============================
	// Найти файл

	public static IFile findFile(String location) {
		for (IProject project : ResourcesPlugin.getWorkspace().getRoot().getProjects()) {
			if (location.startsWith(project.getLocation().toOSString())) {
				Path path = Paths.get(location);
				Path projectPath = Paths.get(project.getLocationURI());
				Path relativePath = projectPath.relativize(path);
				IFile file = project.getFile(new org.eclipse.core.runtime.Path(relativePath.toString()));
				return file;							
			}
		}
		return null;
	}

	// ===============================

	public static boolean hasParent(IResource res, IResource container) {
		if (container == null || !(container instanceof IContainer)) {
			return false;
		}
		IResource currentRes = res;
		while (currentRes != null) {
			if (container.equals(currentRes.getParent())) {
				return true;
			}
			currentRes = currentRes.getParent();
		}
		return false;
	}
	
	public static String workspacePath() {
		return ResourcesPlugin.getWorkspace().getRoot().getLocation().toOSString();
	}
	
	//==========================
	// unzip
	
	public static void unzip(Path zip, Path targetDir) throws IOException {
		InputStream input = Files.newInputStream(zip);
		unzip(input,targetDir);
	}
	
	private static void unzip(InputStream is, Path targetDir) throws IOException {
	    targetDir = targetDir.toAbsolutePath();
	    try (ZipInputStream zipIn = new ZipInputStream(is)) {
	        for (ZipEntry ze; (ze = zipIn.getNextEntry()) != null; ) {
	            Path resolvedPath = targetDir.resolve(ze.getName()).normalize();
	            if (!resolvedPath.startsWith(targetDir)) {
	                throw new RuntimeException("Entry with an illegal path: " 
	                        + ze.getName());
	            }
	            if (ze.isDirectory()) {
	                Files.createDirectories(resolvedPath);
	            } else {
	                Files.createDirectories(resolvedPath.getParent());
	                Files.copy(zipIn, resolvedPath);
	            }
	        }
	    }
	}
	
	//======================
	// время изменения директории (или объектов внутри нее)
	
	public static long getLatestModifiedDate(File dir) {
	    File[] files = dir.listFiles();
	    long latestDate = 0;
	    for (File file : files) {
	        long fileModifiedDate = file.isDirectory() 
	                ? getLatestModifiedDate(file) : file.lastModified();
	        if (fileModifiedDate > latestDate) {
	            latestDate = fileModifiedDate;
	        }
	    }
	    return Math.max(latestDate, dir.lastModified());
	}

}
