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

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;

import ru.dip.core.model.DipFolder;
import ru.dip.core.model.DnfoTable;
import ru.dip.core.model.interfaces.IDipElement;
import ru.dip.core.model.interfaces.IDipParent;
import ru.dip.core.model.interfaces.IDipDocumentElement;
import ru.dip.core.table.TableWriter;
import ru.dip.core.utilities.DipTableUtilities;
import ru.dip.core.utilities.DipUtilities;
import ru.dip.core.utilities.ResourcesUtilities;
import ru.dip.core.utilities.WorkbenchUtitlities;
import ru.dip.ui.Messages;

public class AddToDocumentHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IFolder folder = getFolder(event);	
		if (folder == null || !folder.exists()) {
			return null;
		}
		
		String name = folder.getName();
		if (name.startsWith("__")) { //$NON-NLS-1$
			WorkbenchUtitlities.openError(Messages.AddToDocumentHandler_AddDocumentErrorTitle, Messages.AddToDocumentHandler_AddToDocumentErrorMessage);
			return null;
		}
		
		IContainer parentFolder = folder.getParent();
		IDipElement parent = DipUtilities.findElement(parentFolder);
		IDipParent dipParent = null;
		if (parent instanceof IDipParent) {
			dipParent = (IDipParent) parent;
		} else {
			WorkbenchUtitlities.openError(Messages.AddToDocumentHandler_AddDocumentErrorTitle, Messages.AddToDocumentHandler_ParentDirectoryNotDipProjectMessage);
			return null;
		}
				
		addDnfoFiles(folder);
		dipParent.refresh();	
		IDipElement element =  dipParent.createFolder(folder);
		if (element instanceof DipFolder) {	
			if (!dipParent.getDipDocChildrenList().contains(element)) {			
				dipParent.getDipDocChildrenList().add((IDipDocumentElement) element);
			}
			((DipFolder) element).computeAllChildren();
			DipTableUtilities.saveModel(dipParent);
		}
		WorkbenchUtitlities.updateProjectExplorer();
		return null;
	}

	private void addDnfoFiles(IFolder folder) {
		addDnfo(folder);
		try {
			for (IResource res: folder.members()){
				if (res instanceof IFolder) {
					if (res.getName().startsWith("__")) { //$NON-NLS-1$
						continue;
					}				
					addDnfoFiles((IFolder) res);
				}
			}
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}
	
	private void addDnfo(IFolder folder) {
		IFile file = folder.getFile(new Path(DnfoTable.TABLE_FILE_NAME));
		if (!file.exists()) {
			Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
			try {
				ResourcesUtilities.createFile(file, shell);
				TableWriter.saveEmptyModel(file);
			} catch (CoreException | ParserConfigurationException | IOException | TransformerException e) {
				e.printStackTrace();
			}
		}
	}

	private IFolder getFolder(ExecutionEvent event) {
		ISelection selection = HandlerUtil.getActiveMenuSelection(event);
		if (selection instanceof IStructuredSelection) {
			IStructuredSelection structuredSelection = (IStructuredSelection) selection;
			Object firstElement = structuredSelection.getFirstElement();
			if (firstElement instanceof IFolder) {
				return (IFolder) firstElement;
			}
		}
		return null;
	}

}
