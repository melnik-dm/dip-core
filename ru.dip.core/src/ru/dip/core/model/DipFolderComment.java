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
package ru.dip.core.model;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import ru.dip.core.model.interfaces.IParent;
import ru.dip.core.model.interfaces.IDipComment;
import ru.dip.core.model.interfaces.IDipElement;
import ru.dip.core.model.interfaces.IDipParent;
import ru.dip.core.model.interfaces.IDipDocumentElement;
import ru.dip.core.model.interfaces.ITextComment;
import ru.dip.core.utilities.FileUtilities;
import ru.dip.core.utilities.ResourcesUtilities;

public class DipFolderComment extends DipElement implements IDipComment  {

	public static final String FILE_NAME = ".r";
	
	public static DipFolderComment instance(IResource resource, IParent parent) {
		IDipElement element = DipRoot.getInstance().getElement(resource, parent, DipElementType.FOLDER_COMMENT);
		if (element == null) {
			DipFolderComment dipFolderComment = new DipFolderComment(resource, parent);
			DipRoot.getInstance().putElement(dipFolderComment);
			return dipFolderComment;
		} else {
			return (DipFolderComment) element;
		}
	}
	
	public static DipFolderComment createExistsDipComment(IFile file, IParent parent){
		DipFolderComment folderComment = DipFolderComment.instance(file, parent);
		folderComment.fCommentContent = folderComment.getCommentText();
		return folderComment;
	}
	
	public static DipFolderComment createNewDipComment(IDipParent dipParent, String descriptionContent){
		Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
		try {
			IFile file = ResourcesUtilities.createFile(dipParent.resource(), FILE_NAME, descriptionContent, shell);
			if (file.exists()){
				DipFolderComment folderComment = DipFolderComment.instance(file, dipParent);
				folderComment.fCommentContent = descriptionContent;
				return folderComment;
			}			
		} catch (CoreException e) {
			e.printStackTrace();
		}		
		return null;
	}
	
	private String fCommentContent;

	private DipFolderComment(IResource resource, IParent parent) {
		super(resource, parent);
	}
			
	private String getCommentText(){
		try {
			return FileUtilities.readFile(resource());
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public void updateCommentText(String newContent){
		if (newContent.equals(fCommentContent)){
			return;
		}
		fCommentContent = newContent;
		try (PrintWriter writer = new PrintWriter(resource().getLocation().toOSString(), StandardCharsets.UTF_8)){
			writer.print(fCommentContent);
		} catch (IOException e) {
			e.printStackTrace();
		}		
	}
	
	@Override
	public void delete() {
		try {
			ResourcesUtilities.deleteResource(resource(), null);
			parent().removeChild(this);
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void deleteMainContent() {
		fCommentContent = null;
	}
	
	@Override
	public String getFullContent() {
		return getCommentContent();
	}
	
	@Override
	public String getCommentContent(){
		return fCommentContent;
	}
		
	@Override
	public DipElementType type() {
		return DipElementType.FOLDER_COMMENT;
	}
	
	@Override
	public IFile resource() {
		return (IFile) super.resource();
	}

	public IDipDocumentElement getDipDocumentElement(){
		return (IDipDocumentElement) parent();
	}

	@Override
	public List<ITextComment> getTextComments() {
		// Not supported
		return null;
	}
	
	@Override
	public String getTextCommentsContent() {
		// Not supported
		return null;
	}

	@Override
	public boolean hasTextComments() {
		// Not supported
		return false;
	}

	@Override
	public void save() {
		if (fCommentContent == null || fCommentContent.isEmpty()) {
			delete();
			return;
		}
		
		try (PrintWriter writer = new PrintWriter(resource().getLocation().toOSString(), StandardCharsets.UTF_8)){
			writer.print(fCommentContent);
		} catch (IOException e) {
			e.printStackTrace();
		}	
	}

	@Override
	public boolean isEmpty() {		
		return fCommentContent == null || fCommentContent.isEmpty();
	}

	@Override
	public boolean hasCommentContent() {
		return fCommentContent != null || !fCommentContent.isEmpty();
	}

}
