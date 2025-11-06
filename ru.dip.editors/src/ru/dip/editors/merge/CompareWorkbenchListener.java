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
package ru.dip.editors.merge;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.eclipse.compare.CompareEditorInput;
import org.eclipse.compare.CompareViewerSwitchingPane;
import org.eclipse.compare.ICompareNavigator;
import org.eclipse.compare.internal.CompareEditorInputNavigator;
import org.eclipse.compare.structuremergeviewer.DiffNode;
import org.eclipse.compare.structuremergeviewer.DiffTreeViewer;
import org.eclipse.compare.structuremergeviewer.IDiffContainer;
import org.eclipse.compare.structuremergeviewer.IDiffElement;
import org.eclipse.egit.core.internal.storage.GitFileRevision;
import org.eclipse.egit.ui.internal.revision.FileRevisionTypedElement;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.part.EditorPart;

import ru.dip.core.utilities.DnfoUtils;
import ru.dip.core.utilities.FileUtilities;
import ru.dip.ui.preferences.OtherPreferences;

public class CompareWorkbenchListener implements IPartListener2 {

	private final static String COMPARE_EIDTOR_ID = "org.eclipse.compare.CompareEditor";

	@Override
	public void partOpened(IWorkbenchPartReference partRef) {
		if (COMPARE_EIDTOR_ID.equals(partRef.getId()) && OtherPreferences.isShowDescInCompareEditor()) {				
			setLabelProvider(partRef.getPart(false));
		}		
	}

	private void setLabelProvider(IWorkbenchPart part) {		
		IEditorInput input = ((EditorPart) part).getEditorInput();		
		if (input instanceof CompareEditorInput) {
			CompareEditorInput gitInput = (CompareEditorInput) input;	
			Path repoPath = getRepoPath(gitInput);
			if (repoPath != null) {
				DiffTreeViewer diffViewer = getDiffTreeViewer(gitInput);
				LabelProvider oldLabelProvider = (LabelProvider) diffViewer.getLabelProvider();
				diffViewer.setLabelProvider(new MergeLabelProvider(oldLabelProvider, repoPath));
			}
		}
	}
	
	private Path getRepoPath(CompareEditorInput input) {
		Object result = input.getCompareResult();
		if (result instanceof DiffNode) {
			DiffNode node = (DiffNode) result;
			FileRevisionTypedElement element = findRevisionElement(node);
			if (element != null) {
				return getRepo(element);
			}								
		}			
		return null;
	}
	
	private FileRevisionTypedElement findRevisionElement(DiffNode node) {
		for (IDiffElement element: node.getChildren()) {			
			if (element instanceof DiffNode) {
				DiffNode childNode = (DiffNode) element;				
				if (childNode.getId() instanceof FileRevisionTypedElement) {
					return (FileRevisionTypedElement) childNode.getId();
				}		
				FileRevisionTypedElement revElement = findRevisionElement(childNode);
				if (revElement != null) {
					return revElement;
				}
			}			
		}		
		return null;
	}
	
	private Path getRepo(FileRevisionTypedElement element) {
		GitFileRevision fileRev = (GitFileRevision) element.getFileRevision();
		Repository repo = fileRev.getRepository();
		return repo.getDirectory().getParentFile().toPath();
	}
		
	private DiffTreeViewer getDiffTreeViewer(CompareEditorInput input) {
		ICompareNavigator navigator = input.getNavigator();
		if (navigator instanceof CompareEditorInputNavigator) {			
			CompareEditorInputNavigator ceIN = (CompareEditorInputNavigator) navigator;	
			for (Object obj : ceIN.getPanes()) {
				if (obj instanceof CompareViewerSwitchingPane) {
					CompareViewerSwitchingPane pane = (CompareViewerSwitchingPane) obj;						
					Viewer viewer = pane.getViewer();			
					if (viewer instanceof DiffTreeViewer) {
						return (DiffTreeViewer) viewer;
					}
				}
			}
		}
		return null;
	}
	
	private class MergeLabelProvider extends LabelProvider {
		
		private final LabelProvider fOriginal;
		private final Path fRepoPath;
		
		public MergeLabelProvider(LabelProvider original, Path repoPath) {
			fOriginal = original;
			fRepoPath = repoPath;
		}
		
		@Override
		public String getText(Object element) {
			if (element instanceof DiffNode) {
				DiffNode dn = (DiffNode) element;			
				Path path = getFullPath(dn);
				String description = getDescription(path);
				if (description != null && !description.isEmpty()) {				
					return fOriginal.getText(element)+ "  [" + description + "]";
				}
			}
			
			return fOriginal.getText(element);
		}

		@Override
		public Image getImage(Object element) {
			return fOriginal.getImage(element);
		}

		private Path getFullPath(DiffNode node) {
			Path result = Paths.get(node.getName());
			IDiffContainer parent = node.getParent();
			
			while (parent instanceof DiffNode) {
				String name = parent.getName();
				parent = parent.getParent();
				if (parent != null) {
					result = Paths.get(name).resolve(result);
				}
			}
			return fRepoPath.resolve(result);		
		}
	}
	
	private String getDescription(Path path) {
		if (Files.isDirectory(path)) {
			return DnfoUtils.getDescription(path);
		} else {
			return getFileDescription(path);
		}		
	}
	
	private String getFileDescription(Path filePath) {
		Path descriptionPath = filePath.getParent().resolve(filePath.getFileName().toString() + ".d");
		if (Files.exists(descriptionPath)) {
			try {
				return FileUtilities.readFile(descriptionPath);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}							
		return null;
	}
	
}
