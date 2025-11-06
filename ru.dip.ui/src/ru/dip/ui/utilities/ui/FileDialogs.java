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
package ru.dip.ui.utilities.ui;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.ElementTreeSelectionDialog;
import org.eclipse.ui.dialogs.ISelectionStatusValidator;
import org.eclipse.ui.model.WorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.eclipse.ui.views.navigator.ResourceComparator;

import ru.dip.core.model.DipProject;
import ru.dip.core.model.DipRoot;
import ru.dip.core.model.interfaces.IDipParent;
import ru.dip.core.utilities.ui.viewer.DipProjectFoldersContentProvider;
import ru.dip.core.utilities.ui.viewer.DipProjectLabelProvider;
import ru.dip.core.utilities.ui.viewer.DipProjectFoldersContentProvider.ParentHolder;
import ru.dip.ui.Messages;
import ru.dip.ui.navigator.NavigatorLabelProvider;

/**
 * Диалоги для получения файлов, папок
 */
public class FileDialogs {
	
	// названия окон, сообщения об ошибках
	public static final String FILE_SYSTEM_DIR_DIALOG_MSG = Messages.FileDialogs_SelectLocationDirMessage;
	public static final String WORKSPACE_FILE_DIALOG_TITLE = Messages.FileDialogs_FileSelectionMessage;
	public static final String WORKSPACE_FOLDER_DIALOG_TITLE = "Folder selection";
	public static final String WORKSPACE_PROJECT_DIALOG_TITLE = Messages.FileDialogs_ProjectSelectionMessage;
	public static final String WORKSPACE_DIR_DIALOG_MSG = Messages.FileDialogs_SelectFolderWorkspaceMeesage;
	public static final String WORKSPACE_PROJECT_DIALOG_MSG = Messages.FileDialogs_SelectProjectMessage;
	public static final String WORKSPACE_FILE_DIALOG_MSG = Messages.FileDialogs_SelectFileWorkspaceMessage;
	public static final String WORKSPACE_FILE_DIALOG_ERROR = Messages.FileDialogs_IsNotFileErrorMessage;
	public static final String WORKSPACE_FOLDER_DIALOG_ERROR = Messages.FileDialogs_IsNotFolderErrorMessage;
	public static final String PROJECT_FILE_DIALOG_MSG = Messages.FileDialogs_SelectFileProjectMessage;
	public static final String PROJECT_DIR_DIALOG_MSG = "Select a folder from project";

	// Обрезаем путь для воркспейса
	private final static String WSP_BEG = "${workspace_loc:";   //$NON-NLS-1$
	private final static String WSP_END = "}"; //$NON-NLS-1$
	
	public static String strip_wsp(String s) {
		s = s.trim();
		if (s.startsWith(WSP_BEG) && s.endsWith(WSP_END)) {
			int x = s.length() - WSP_END.length(); 
			s = s.substring(WSP_BEG.length(), x);
		}
		return s;
	}
	
	//============================================
	// Из файловой системы	
	
	/**
	 *  дилаог получения папки из файловой системы
	 */
	public static String getFolderFromFileSystem(Shell shell){
		String selectedDirectory = null;
		DirectoryDialog dialog = new DirectoryDialog(shell, SWT.SHEET);	
		dialog.setMessage(FILE_SYSTEM_DIR_DIALOG_MSG);
		selectedDirectory = dialog.open();
		return selectedDirectory;
	}
		
	public static String getFolderFromFileSystem(Shell shell, String title){
		DirectoryDialog dialog = new DirectoryDialog(shell, SWT.SHEET);	
		dialog.setMessage(FILE_SYSTEM_DIR_DIALOG_MSG);
		dialog.setText(title);
		return dialog.open();
	}
	
	/**
	 *  диалог получения папки из файловой системы
	 */
	public static String getFolderFromFileSystem(Shell shell, String title, String startDirectory){
		DirectoryDialog dialog = new DirectoryDialog(shell, SWT.SHEET);	
		dialog.setMessage(FILE_SYSTEM_DIR_DIALOG_MSG);
		dialog.setText(title);
		dialog.setFilterPath(startDirectory);
		return dialog.open();
	}
	
	/**
	 * диалог получения файла из файловой системы
	 */
	public static String getFileFromFileSystemDialog(Shell shell, String text) {
		FileDialog dialog = new FileDialog(shell);
		if(text != null && text.trim().length() != 0) dialog.setFilterPath(text);
		dialog.setText(""); //$NON-NLS-1$
		return dialog.open();
	}
	
	/**
	 * диалог получения файла из файловой системы с заданным именем (именами)
	 */
	public static String getFileFromFileSystemDialog(Shell shell, String title, String[] filter) {
		FileDialog dialog = new FileDialog(shell);
		if (title != null) {
			dialog.setText(title);
		}
		dialog.setFilterExtensions(filter);
		return dialog.open();
	}	

	
	//==============================================
	// Из Workspace

	/**
	 *  Общий диалог получения папки из Workspace
	 */	
	public static IContainer getWorkspaceFolderDialog(Shell shell){	
		// создали диалог
		ElementTreeSelectionDialog dialog = new ElementTreeSelectionDialog(shell,
				new WorkbenchLabelProvider(), new WorkbenchContentProvider());
		dialog.setInput(ResourcesPlugin.getWorkspace().getRoot());		
		dialog.setComparator(new ResourceComparator(ResourceComparator.NAME));
		dialog.setTitle(WORKSPACE_FILE_DIALOG_TITLE);
		dialog.setMessage(WORKSPACE_FILE_DIALOG_MSG);		
		dialog.setValidator(new ISelectionStatusValidator() {
			@Override
			public IStatus validate(Object[] selection) {
				if (selection != null)
					if (selection.length > 0)
						if (!(selection[0] instanceof IFolder) && !(selection[0] instanceof IProject))
							return new Status(IStatus.ERROR, Messages.FileDialogs_error, WORKSPACE_FILE_DIALOG_ERROR);			
				return new Status(IStatus.OK, Messages.FileDialogs_ok, null);
			}
		});
		
		if (dialog.open() == Window.OK) {
			IResource resource = (IResource) dialog.getFirstResult();
			return (IContainer) resource;
		}
		return null;
	}	
	
	/**
	 *  Общий диалог получения файла из Workspace
	 */	
	public static IFile getWorkspaceFileDialog(Shell shell){	
		// создали диалог
		ElementTreeSelectionDialog dialog = new ElementTreeSelectionDialog(shell,
				new WorkbenchLabelProvider(), new WorkbenchContentProvider());
		dialog.setInput(ResourcesPlugin.getWorkspace().getRoot());		
		dialog.setComparator(new ResourceComparator(ResourceComparator.NAME));
		dialog.setTitle(WORKSPACE_FILE_DIALOG_TITLE);
		dialog.setMessage(WORKSPACE_FILE_DIALOG_MSG);		
		dialog.setValidator(new ISelectionStatusValidator() {
			@Override
			public IStatus validate(Object[] selection) {
				if (selection != null)
					if (selection.length > 0)
						if (!(selection[0] instanceof IFile))
							return new Status(IStatus.ERROR, Messages.FileDialogs_error, WORKSPACE_FILE_DIALOG_ERROR);			
				return new Status(IStatus.OK, Messages.FileDialogs_ok, null);
			}
		});
		
		if (dialog.open() == Window.OK) {
			IFile resource = (IFile) dialog.getFirstResult();
			if (resource != null) {
				return resource;
			}
		}
		return null;
	}	
	
	/**
	 *  Общий диалог получения конкретного файла из Workspace
	 */		
	/*public static String getWorkspaceSpecialFileDialog(Shell shell, String filename){	
		String error = Не является файлом:  + filename;		
		// создали диалог
		ElementTreeSelectionDialog dialog = new ElementTreeSelectionDialog(shell,
				new WorkbenchLabelProvider(), new WorkbenchContentProvider());
		dialog.setInput(ResourcesPlugin.getWorkspace().getRoot());		
		dialog.setComparator(new ResourceComparator(ResourceComparator.NAME));
		dialog.setTitle(WORKSPACE_FILE_DIALOG_TITLE);
		dialog.setMessage(WORKSPACE_FILE_DIALOG_MSG);

		dialog.setValidator(new ISelectionStatusValidator() {
			@Override
			public IStatus validate(Object[] selection) {
				if (selection != null)
					if (selection.length > 0){
						if (!(selection[0] instanceof IFile))
							return new Status(IStatus.ERROR, filename, error);	
						if (!filename.equals(((IFile)selection[0]).getName()))
							return new Status(IStatus.ERROR, filename, error);
					}
				return new Status(IStatus.OK, Messages.FileDialogs_ok, null);
			}
		});
		
		if (dialog.open() == Window.OK) {
			IResource resource = (IResource) dialog.getFirstResult();
			if (resource != null) {
				StringBuilder buf = new StringBuilder();
				
				return buf.append("${").append("workspace_loc:").append(resource.getFullPath()).append("}").toString(); //$NON-NLS-3$ //$NON-NLS-2$ //$NON-NLS-1$
			}
		}
		return null;
	}*/
	
	/**
	 * Диалог получения проекта (полный путь) из workspace с заданным WorkbenchContentProvider
	 * WorkbenchContentProvider - используется чтобы отфильтровать проекты, которые не нужно отображать
	 */
	public static String getProjectLocFromWorkspace(Shell shell, WorkbenchContentProvider contentProvider, ILabelProvider labelProvider){
		// создали диалог
		ElementTreeSelectionDialog dialog = new ElementTreeSelectionDialog(shell,
				labelProvider, contentProvider);
		dialog.setInput(ResourcesPlugin.getWorkspace().getRoot());		
		dialog.setComparator(new ResourceComparator(ResourceComparator.NAME));
		dialog.setTitle(WORKSPACE_PROJECT_DIALOG_TITLE);
		dialog.setMessage(WORKSPACE_PROJECT_DIALOG_MSG);		
		dialog.setValidator(new ISelectionStatusValidator() {
			@Override
			public IStatus validate(Object[] selection) {
				if (selection != null)
					if (selection.length > 0)
						if (!(selection[0] instanceof IProject))
							return new Status(IStatus.ERROR, Messages.FileDialogs_error, WORKSPACE_FILE_DIALOG_ERROR);			
				return new Status(IStatus.OK, Messages.FileDialogs_ok, null);
			}
		});
		
		if (dialog.open() == Window.OK) {
			IResource resource = (IResource) dialog.getFirstResult();
			if (resource != null) {
				return resource.getLocation().toOSString(); //$NON-NLS-3$ //$NON-NLS-2$ //$NON-NLS-1$
			}
		}
		return null;
	}
	
	/**
	 * Диалог получение проекта
	 */
	public static IProject getProjectFromWorkspace(Shell shell, WorkbenchContentProvider contentProvider, ILabelProvider labelProvider){
		// создали диалог
		ElementTreeSelectionDialog dialog = new ElementTreeSelectionDialog(shell,
				labelProvider, contentProvider);
		dialog.setInput(ResourcesPlugin.getWorkspace().getRoot());		
		dialog.setComparator(new ResourceComparator(ResourceComparator.NAME));
		dialog.setTitle(WORKSPACE_PROJECT_DIALOG_TITLE);
		dialog.setMessage(WORKSPACE_PROJECT_DIALOG_MSG);		
		dialog.setValidator(new ISelectionStatusValidator() {
			@Override
			public IStatus validate(Object[] selection) {
				if (selection != null)
					if (selection.length > 0)
						if (!(selection[0] instanceof IProject))
							return new Status(IStatus.ERROR, Messages.FileDialogs_error, WORKSPACE_FILE_DIALOG_ERROR);			
				return new Status(IStatus.OK, Messages.FileDialogs_ok, null);
			}
		});
		
		if (dialog.open() == Window.OK) {
			IResource resource = (IResource) dialog.getFirstResult();
			if (resource != null && resource instanceof IProject) {
				return (IProject) resource;
			}
		}
		return null;
	}
	
	/**
	 *  Выбор DipProject - из workspace
	 */
	public static DipProject selectDipProject(Shell shell) {		
		ITreeContentProvider contentProvider = new ITreeContentProvider() {
			
			@Override
			public Object[] getElements(Object inputElement) {
				return DipRoot.getInstance().getProjects().toArray();
			}
			
			@Override
			public boolean hasChildren(Object element) {
				return false;
			}
			
			@Override
			public Object getParent(Object element) {
				return null;
			}
		
			@Override
			public Object[] getChildren(Object parentElement) {
				return null;
			}
		};
	
		ILabelProvider labelProvider = new NavigatorLabelProvider();
		ElementTreeSelectionDialog dialog = new ElementTreeSelectionDialog(shell, labelProvider, contentProvider);
		dialog.setComparator(new ViewerComparator());
		dialog.setTitle(WORKSPACE_PROJECT_DIALOG_TITLE);
		dialog.setMessage(WORKSPACE_PROJECT_DIALOG_MSG);
		dialog.setInput(DipRoot.getInstance());
		if (dialog.open() == Window.OK) {
			Object object = dialog.getFirstResult();
			if (object != null && object instanceof DipProject) {
				return (DipProject) object;
			}
		}
		return null;
	}
	
	
	//==============================================
	// Из проекта
	
	/**
	 * Диалог получения файла из проекта
	 */
	public static String getFileFromProjectDialog(Shell shell, IProject project){
		// создали диалог
		ElementTreeSelectionDialog dialog = new ElementTreeSelectionDialog(shell,
				new WorkbenchLabelProvider(), new WorkbenchContentProvider());
		dialog.setInput(project);		
		dialog.setComparator(new ResourceComparator(ResourceComparator.NAME));
		dialog.setTitle(WORKSPACE_FILE_DIALOG_TITLE);
		dialog.setMessage(PROJECT_FILE_DIALOG_MSG);	
		dialog.setValidator(new ISelectionStatusValidator() {
			@Override
			public IStatus validate(Object[] selection) {
				if (selection != null)
					if (selection.length > 0)
						if (!(selection[0] instanceof IFile))
							return new Status(IStatus.ERROR, Messages.FileDialogs_error, WORKSPACE_FILE_DIALOG_ERROR);			
				return new Status(IStatus.OK, Messages.FileDialogs_ok, null);
			}
		});
		
		if (dialog.open() == Window.OK) {
			IResource resource = (IResource) dialog.getFirstResult();
			if (resource != null) {
				return resource.getProjectRelativePath().toOSString();
			}
		}
		return null;		
	}
	
	/**
	 * Диалог получения файла из проекта
	 * Отображаются файлы только с указанным разрешением
	 */
	public static String getFileFromProjectDialog(Shell shell, IProject project, String extension){
		// создали диалог
		ElementTreeSelectionDialog dialog = new ElementTreeSelectionDialog(shell,
				new WorkbenchLabelProvider(), new WorkbenchContentProvider());
		dialog.setInput(project);		
		dialog.setComparator(new ResourceComparator(ResourceComparator.NAME));
		dialog.setTitle(WORKSPACE_FILE_DIALOG_TITLE);
		dialog.setMessage(PROJECT_FILE_DIALOG_MSG);			
		dialog.addFilter(new ViewerFilter() {			
			@Override
			public boolean select(Viewer viewer, Object parentElement, Object element) {				
				if (element instanceof IFile){
					return extension.equals(((IFile) element).getFileExtension());
				}
				return true;
			}
		});
		dialog.setValidator(new ISelectionStatusValidator() {
			@Override
			public IStatus validate(Object[] selection) {
				if (selection != null)
					if (selection.length > 0)
						if (!(selection[0] instanceof IFile))
							return new Status(IStatus.ERROR, Messages.FileDialogs_error, WORKSPACE_FILE_DIALOG_ERROR);			
				return new Status(IStatus.OK, Messages.FileDialogs_ok, null);
			}
		});
		
		if (dialog.open() == Window.OK) {
			IResource resource = (IResource) dialog.getFirstResult();
			if (resource != null) {
				return resource.getProjectRelativePath().toOSString();
			}
		}
		return null;		
	}
	
	 /** 
	  * 
	  * Диалог получения папки из проекта
	  */
	public static Object getFolderFromProject(Shell shell, ITreeContentProvider contentProvider, ILabelProvider labelProvider, IProject project){
		// создали диалог
		ElementTreeSelectionDialog dialog = new ElementTreeSelectionDialog(shell,
				labelProvider, contentProvider);
		dialog.setInput(project);		
		dialog.setComparator(new ResourceComparator(ResourceComparator.NAME));
		dialog.setTitle(WORKSPACE_PROJECT_DIALOG_TITLE);
		dialog.setMessage(WORKSPACE_PROJECT_DIALOG_MSG);
		if (dialog.open() == Window.OK) {
			Object object = dialog.getFirstResult();
			if (object != null) {
				return object; 
			}
		}
		return null;
	}
	
	
	public static IDipParent getFolderFromDipProject(Shell shell, IDipParent input, IDipParent initialSelection) {
		ElementTreeSelectionDialog dialog = new ElementTreeSelectionDialog(shell, 
				//new WorkbenchLabelProvider(),
				DipProjectLabelProvider.instance(),
				 DipProjectFoldersContentProvider.INSTANCE
				);
			
		dialog.setInput(new ParentHolder(input));		
		dialog.setComparator(new ResourceComparator(ResourceComparator.NAME));
		dialog.setInitialSelection(initialSelection);
		dialog.setTitle(WORKSPACE_FOLDER_DIALOG_TITLE);
		dialog.setMessage(PROJECT_DIR_DIALOG_MSG);
		if (dialog.open() == Window.OK) {
			Object object = dialog.getFirstResult();
			if (object instanceof IDipParent) {
				return (IDipParent) object; 
			}
		}
		return null;
	}
	
}
