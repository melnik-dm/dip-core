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

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Adapters;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.navigator.CommonViewer;
import org.eclipse.ui.navigator.IExtensionActivationListener;
import org.eclipse.ui.navigator.INavigatorContentService;
import org.eclipse.ui.navigator.resources.ProjectExplorer;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.part.ISetSelectionTarget;
import org.eclipse.ui.progress.IProgressService;
import org.eclipse.ui.progress.WorkbenchJob;

import ru.dip.core.manager.DipNatureManager;
import ru.dip.core.model.DipProject;
import ru.dip.core.model.DipRoot;
import ru.dip.core.model.glossary.GlossaryField;
import ru.dip.core.model.glossary.GlossaryFolder;
import ru.dip.core.model.interfaces.IDipEditor;
import ru.dip.core.model.interfaces.IParent;
import ru.dip.core.model.interfaces.IDipElement;
import ru.dip.core.model.interfaces.IDipParent;
import ru.dip.core.model.interfaces.IDipDocumentElement;
import ru.dip.core.table.IContainerEditorInput;
import ru.dip.core.unit.UnitType;
import ru.dip.core.utilities.md.UnityMdInput;

public class WorkbenchUtitlities {
	
	public static Shell checkShell(Shell shell) {
		if (shell == null) {
			return PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
		}
		return shell;
	}
	
	//========================================
	// ProjectExplorer
	
	public static DipProject getSelectedDipProjectFromProjectExplorer() {
		Object selectionObject = getSelectedObjFromProjectExplorer();
		if (selectionObject instanceof IDipElement) {
			return ((IDipElement) selectionObject).dipProject();
		}
		return null;
	}
	
	public static IParent getSelectedContainerFromProjectExplorer() {
		Object selectionObject = getSelectedObjFromProjectExplorer();
		
		if (selectionObject instanceof IParent) {
			return (IParent) selectionObject;
		}
		if (selectionObject instanceof IDipElement) {
			return ((IParent) selectionObject).parent();
		}
		
		return null;
	}
	
	public static IDipParent getSelectedDipContainerFromProjectExplorer() {
		Object selectionObject = getSelectedObjFromProjectExplorer();

		if (selectionObject instanceof IDipParent) {
			return (IDipParent) selectionObject;
		}
		if (selectionObject instanceof IDipElement) {
			selectionObject = ((IDipElement) selectionObject).parent();
			if (selectionObject instanceof IDipParent) {
				return (IDipParent) selectionObject;
			}
		}
		return null;
	}

	public static Object getSelectedObjFromProjectExplorer() {
		ISelectionService selectionService = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getSelectionService();
		ISelection selection = selectionService.getSelection(ProjectExplorer.VIEW_ID);
		if (selection instanceof IStructuredSelection) {
			IStructuredSelection structuredSelection = (IStructuredSelection) selection;
			if (!structuredSelection.isEmpty()) {
				return structuredSelection.getFirstElement();								
			}
		}
		return null;
	}
	
	
	public static void selectAndReveal(IResource resource) {
		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		// validate the input
		if (window == null || resource == null) {
			return;
		}
		IWorkbenchPage page = window.getActivePage();
		if (page == null) {
			return;
		}
		// get all the view and editor parts
		List<IWorkbenchPart> parts = new ArrayList<>();
		IWorkbenchPartReference refs[] = page.getViewReferences();
		for (int i = 0; i < refs.length; i++) {
			IWorkbenchPart part = refs[i].getPart(false);
			if (part != null) {
				parts.add(part);
			}
		}
		refs = page.getEditorReferences();
		for (int i = 0; i < refs.length; i++) {
			if (refs[i].getPart(false) != null) {
				parts.add(refs[i].getPart(false));
			}
		}

		final ISelection selection = new StructuredSelection(resource);
		Iterator<?> itr = parts.iterator();
		while (itr.hasNext()) {
			IWorkbenchPart part = (IWorkbenchPart) itr.next();

			// get the part's ISetSelectionTarget implementation
			ISetSelectionTarget target = Adapters.adapt(part, ISetSelectionTarget.class);
			if (target != null) {
				// select and reveal resource
				final ISetSelectionTarget finalTarget = target;
				window.getShell().getDisplay().asyncExec(() -> finalTarget.selectReveal(selection));
			}
		}
	}
	
	public static void selectAndReveal(IDipElement dipElement) {
		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		// validate the input
		if (window == null || dipElement == null) {
			return;
		}
		IWorkbenchPage page = window.getActivePage();
		if (page == null) {
			return;
		}
		// get all the view and editor parts
		List<IWorkbenchPart> parts = new ArrayList<>();
		IWorkbenchPartReference refs[] = page.getViewReferences();
		for (int i = 0; i < refs.length; i++) {
			IWorkbenchPart part = refs[i].getPart(false);
			if (part != null) {
				parts.add(part);
			}
		}
		refs = page.getEditorReferences();
		for (int i = 0; i < refs.length; i++) {
			if (refs[i].getPart(false) != null) {
				parts.add(refs[i].getPart(false));
			}
		}

		final ISelection selection = new StructuredSelection(dipElement);
		Iterator<?> itr = parts.iterator();
		while (itr.hasNext()) {
			IWorkbenchPart part = (IWorkbenchPart) itr.next();
			// get the part's ISetSelectionTarget implementation
			ISetSelectionTarget target = Adapters.adapt(part, ISetSelectionTarget.class);			
			if (target != null) {
				// select and reveal resource
				final ISetSelectionTarget finalTarget = target;
				window.getShell().getDisplay().asyncExec(() -> finalTarget.selectReveal(selection));
			}
		}
	}
	
	public static void selectInProjectExploter(List<IProject> object) {
		ProjectExplorer projectExplorer = WorkbenchUtitlities.getProjectExplorer();
		if (projectExplorer != null) {
			StructuredSelection newSelection = new StructuredSelection(object);
			projectExplorer.selectReveal(newSelection);
			projectExplorer.getCommonViewer().expandToLevel(object, 1);
		}
	}
	
	public static void selectInProjectExploter(Object object) {
		ProjectExplorer projectExplorer = WorkbenchUtitlities.getProjectExplorer();
		if (projectExplorer != null) {
			StructuredSelection newSelection = new StructuredSelection(object);
			projectExplorer.selectReveal(newSelection);
			projectExplorer.getCommonViewer().expandToLevel(object, 1);
		}
	}
	
	public static void selectInProjectExploter(Object[]  objects) {
		ProjectExplorer projectExplorer = WorkbenchUtitlities.getProjectExplorer();
		if (projectExplorer != null) {
			StructuredSelection newSelection = new StructuredSelection(objects);
			projectExplorer.selectReveal(newSelection);
		}
	}
	
	public static ProjectExplorer getProjectExplorer(){
		for (IViewReference viewRef : getWorkbenchWindow().getActivePage()
				.getViewReferences()) {
			if (ProjectExplorer.VIEW_ID.equals(viewRef.getId())) {
				return (ProjectExplorer) viewRef.getPart(false);
			}
		}
		return null;
	}

	public static void updateInputProjectExplorer() {
		ProjectExplorer explorer = getProjectExplorer();
		if (explorer == null) {
			return;
		}
		
		WorkbenchJob job = new WorkbenchJob("Udpate Input Navigator") {
			
			@Override
			public IStatus runInUIThread(IProgressMonitor monitor) {
				CommonViewer commonViewer = explorer.getCommonViewer();		
				commonViewer.setInput(ResourcesPlugin.getWorkspace().getRoot());
				return Status.OK_STATUS;
			}
		};
		job.runInUIThread(null);	
	}
	
	public static void updateNavigatorServiceExplorer() {
		ProjectExplorer explorer = getProjectExplorer();
		if (explorer == null) {
			return;
		}
		
		WorkbenchJob job = new WorkbenchJob("Udpate Input Navigator") {
			
			@Override
			public IStatus runInUIThread(IProgressMonitor monitor) {
				CommonViewer commonViewer = explorer.getCommonViewer();		
				commonViewer.setInput(ResourcesPlugin.getWorkspace().getRoot());
				INavigatorContentService service = explorer.getNavigatorContentService();
				if (service instanceof IExtensionActivationListener) {
					((IExtensionActivationListener) service).onExtensionActivation("", null, true);
				}
				return Status.OK_STATUS;
			}
		};
		job.runInUIThread(null);	
	}
	
	public static void updateProjectExplorer() {
		for (IViewReference viewRef : PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
				.getViewReferences()) {
			if (ProjectExplorer.VIEW_ID.equals(viewRef.getId())) {
				ProjectExplorer explorer = (ProjectExplorer) viewRef.getPart(false);
				if (explorer == null) {
					return;
				}
				WorkbenchJob job = new WorkbenchJob("Udpate navigator") {
					
					@Override
					public IStatus runInUIThread(IProgressMonitor monitor) {
						explorer.getCommonViewer().refresh(true);
						explorer.getCommonViewer().getTree().redraw();
						return Status.OK_STATUS;
					}
				};
				job.runInUIThread(null);
				return;
			}
		}
	}
	
	public static void updateProjectExplorer(Object element) {
		for (IViewReference viewRef : PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
				.getViewReferences()) {
			if (ProjectExplorer.VIEW_ID.equals(viewRef.getId())) {
				ProjectExplorer explorer = (ProjectExplorer) viewRef.getPart(false);
				explorer.getNavigatorContentService().update();
				explorer.getCommonViewer().refresh(element, true);
			}
		}
	}
	
	public static void updateAfterGitChanges(File repoDir) {
		// get project
		List<DipProject> projects = DipUtilities.findProjectsInFolder(repoDir.toPath());
		// update content
		Display.getDefault().asyncExec(() -> {
			projects.stream().forEach(DipProject::computeChildren);
		});
		// get open editors
		List<IDipEditor> editors = WorkbenchUtitlities.getOpenedDocumentEditors()
				.stream()
				.filter(editor -> projects.contains(editor.dipProject()))
				.collect(Collectors.toList());
		// udpate editors
		for (IDipEditor editor : editors) {
			IEditorPart part = (IEditorPart) editor;
			if (WorkbenchUtitlities.getWorkbenchWindow().getActivePage().isPartVisible(part)) {
				
				Display.getDefault().asyncExec(() -> {
					editor.updateEditor();
				});
			} else {
				editor.setModelChanged();
			}
		}
	}
	
	public static void updateRoot() {
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();		
		IProject[] projects =  root.getProjects();
		for (int i = 0; i < projects.length; i++){
			if (DipNatureManager.hasNature(projects[i])){
				DipProject dipProejct = DipRoot.getInstance().getDipProject(projects[i]);
				ResourcesUtilities.updateProject(dipProejct.getProject());
				dipProejct.computeChildren();
			} 
		}
	}
	
	//===================================
	// open file in editor

	public static IEditorPart openFile(IFile file) {
		// Open editor on new file.
		IWorkbenchWindow dw = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		try {
			if (dw != null) {
				IWorkbenchPage page = dw.getActivePage();
				if (page != null) {
					if (UnitType.isForm(file)) {
						return IDE.openEditor(page, new FileEditorInput(file), "ru.dip.ui.form.editor");
					}
					IEditorPart editorPart = IDE.openEditor(page, file, true);
					return editorPart;
				}
			}
		} catch (PartInitException e) {
			openError(dw.getShell(), "Problems Opening Editor", e.getMessage(), e);
		}
		return null;
	}	
	
	public static void selectText(TextEditor editor, String text) {
		IEditorInput input = editor.getEditorInput();
		IDocument document = editor.getDocumentProvider().getDocument(input);
		if (document != null) {
			int offset = document.get().indexOf(text);
			if (offset >= 0) {
				editor.selectAndReveal(offset, text.length());				
			}
		}
	}
	
	public static IEditorPart openUnityMdEditor(IContainer container) {
		UnityMdInput input = new UnityMdInput(container);
		IWorkbenchWindow dw = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		IWorkbenchPage page = dw.getActivePage();
		try {
			return IDE.openEditor(page, input, UnityMdInput.EditorID);
		} catch (PartInitException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	//===================================
	// open message
	
	public static void openError(Shell parent, String title, String message, PartInitException exception) {
		// Check for a nested CoreException
		CoreException nestedException = null;
		IStatus status = exception.getStatus();
		if (status != null && status.getException() instanceof CoreException) {
			nestedException = (CoreException) status.getException();
		}
		if (nestedException != null) {
			ErrorDialog.openError(parent, title, message, nestedException.getStatus());
		} else {
			MessageDialog.openError(parent, title, message);
		}
	}
	
	public static void openError(String title, String message){
		Display.getDefault().asyncExec(new Runnable() {
			
			@Override
			public void run() {
				Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
				MessageDialog.openError(shell, title, message);				
			}
		});
		
	}

	public static void openReadOnlyErrorMessage(IDipDocumentElement element) {
		Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
		openReadOnlyErrorMessage(shell, element);
	}
	
	public static void openReadOnlyErrorMessage(Shell shell, IDipDocumentElement element) {
		String message = DipUtilities.readOnlyOpenMessage(element);	
		MessageDialog.openInformation(shell, "Read-only", message);
	}
	
	//==================================
	// Open view
	
	public static void openView(String viewID) {
		try {
			PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(viewID);
		} catch (PartInitException e) {
			e.printStackTrace();
		}
	}
	
	//============================
	// close editors
	
	/**
	 * Закрыть редакторы если нет файлов, из которых они были открыты
	 * Например, при удалении
	 */
	public static void closeEditorsIfInputNotExists() {
		IWorkbenchPage page =  PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		IEditorReference[] editorRefs = page.getEditorReferences();
		for (IEditorReference editorRef: editorRefs) {
			IEditorPart editor = editorRef.getEditor(true);
			IEditorInput input = editor.getEditorInput();
			if (!input.exists()) {
				page.closeEditor(editor, false);
			}
		}		
	}
	
	//===================================
	// clipboard
	
	public static void setToClipboard(String content, Display display) {
		if (display == null) {
			display = Display.getDefault();
		}
		Clipboard clipboard = new Clipboard(display);
		Transfer[] transfers = new Transfer[] { TextTransfer.getInstance() };
		Object[] data = new Object[] {content};
		clipboard.setContents(data, transfers);
	}
	
	
	//=================================
	// WorkbenchPart

	public static IFile getFileFromOpenedEditor(){
		IEditorPart editPart = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
		if (editPart != null){
			return getFileFromOpenedEditor(editPart);
		}
		return null;
	}
	
	public static IFile getFileFromOpenedEditor(IWorkbenchPart part){
		if (part instanceof IEditorPart){
			IEditorPart editorPart = (IEditorPart) part;
			IEditorInput input = editorPart.getEditorInput();
			if (input instanceof IFileEditorInput){
				IFileEditorInput fileEditorInput = (IFileEditorInput) input;
				return fileEditorInput.getFile();		
			} 
		}
		return null;
	}
	
	public static DipProject getDipProjectFromOpenedEditor() {
		if (PlatformUI.getWorkbench().getActiveWorkbenchWindow() == null) {
			return null;
		}
		IEditorPart editPart = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
		return getDipProjectFromOpenedEditor(editPart);	
	}
	
	public static DipProject getDipProjectFromOpenedEditor(IWorkbenchPart part){
		IEditorInput input = getOpenedEditorInput(part);
		IResource res = getResFromInput(input);
		if (res == null) {
			return null;
		}	
		return DipUtilities.findDipProject(res);
	}
	
	private static IEditorInput getOpenedEditorInput(IWorkbenchPart part) {
		if (part instanceof IEditorPart){
			IEditorPart editorPart = (IEditorPart) part;
			return editorPart.getEditorInput();
		}
		return null;
	}
	
	private static IResource getResFromInput(IEditorInput input) {
		if (input instanceof IFileEditorInput) {
			return ((IFileEditorInput)input).getFile();
		} else if (input instanceof IContainerEditorInput) {
			return ((IContainerEditorInput)input).getContainer();
		}
		return null;
	}
	
	public static GlossaryField getField(TextSelection selection){
		String text = selection.getText();
		if (text == null || text.isEmpty() || text.length() > GlossaryField.NAME_MAX_LENGTH){
			return null;
		}			
		IWorkbenchPart part = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActivePart();
		IFile file = WorkbenchUtitlities.getFileFromOpenedEditor(part);
		if (file == null){
			return null;
		}
		if (!DipNatureManager.hasNature(file)){
			return null;
		}
		DipProject project = DipRoot.getInstance().getDipProject(file.getProject());
		if (project == null){
			return null;
		}
		GlossaryFolder glossFolder = project.getGlossaryFolder();
		if (glossFolder == null){
			return null;
		}
		GlossaryField field = glossFolder.getChild(text);
		return field;		
	}

	public static IEditorPart getActiveEditor() {
		if (getWorkbenchWindow() == null) {
			return null;
		}
		return PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();

	}
	
	public static List<IDipEditor> getOpenedDocumentEditors() {
		IWorkbenchWindow window = getWorkbenchWindow();		
		if (window == null) {
			return null;
		}		
		IEditorReference[] editorReferences = window.getActivePage().getEditorReferences();
		return Stream.of(editorReferences)
				.map(ref -> ref.getEditor(false))
				.filter(IDipEditor.class::isInstance)
				.map(IDipEditor.class::cast)
				.collect(Collectors.toList());
	}
	
	public static IWorkbenchWindow getWorkbenchWindow() {
		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();		
		if (window == null && PlatformUI.getWorkbench().getWorkbenchWindows().length > 0) {
			window = PlatformUI.getWorkbench().getWorkbenchWindows()[0];
		}	
		return window;
	}
	
	//===================================
	// run with annimation
	
	public static void runWithCursorBusy(IRunnableWithProgress runnableWithProgress) throws InvocationTargetException, InterruptedException {
		IWorkbench wb = PlatformUI.getWorkbench();
		IProgressService ps = wb.getProgressService();
		ps.busyCursorWhile(runnableWithProgress);
	}

	public static Shell getShell() {
		return Display.getDefault().getActiveShell();
	}
}
