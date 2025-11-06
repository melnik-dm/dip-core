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
package ru.dip.ui.action.edit;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWTError;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.actions.SelectionListenerAction;
import org.eclipse.ui.part.ResourceTransfer;

import ru.dip.core.model.interfaces.IDipElement;
import ru.dip.core.utilities.WorkbenchUtitlities;
import ru.dip.ui.Messages;
import ru.dip.ui.glossary.GlossaryBuffer;
import ru.dip.ui.variable.utils.VariableBuffer;

public class CopyAction extends SelectionListenerAction {

	private Clipboard fClipboard;
	private List<IResource> fSelectedResources;
	private GlossaryBuffer fGlossaryBuffer = GlossaryBuffer.getInstance();
	private VariableBuffer fVariableBuffer = VariableBuffer.getInstance();
	private boolean fIsGlossary = false; // при копировании записей из глоссария
	private boolean fIsVariable = false;
	
	private Object[] fSelectedObjects;
	
	public CopyAction(Shell shell, Clipboard clipboard) {
		super(Messages.CopyAction_Action_name);
		fClipboard = clipboard;
	}

	@Override
	protected boolean updateSelection(IStructuredSelection selection) {
		if (selection == null || selection.isEmpty()) {
			return false;
		}		
		fSelectedResources = new ArrayList<>();
		fSelectedObjects = selection.toArray();
		fIsGlossary = false;
		fIsVariable = false;
		if (fGlossaryBuffer.isGlossaryFields(fSelectedObjects)){
			fIsGlossary = fGlossaryBuffer.checkGlossaryFields(fSelectedObjects);
			return fIsGlossary;
		}

		if (fVariableBuffer.isVariables(fSelectedObjects)) {
			fIsVariable = fVariableBuffer.checkVariables(fSelectedObjects);
			return fIsVariable;
		}
		
		for (Object obj : fSelectedObjects) {						
			if (obj instanceof IDipElement) {
				IDipElement element = (IDipElement) obj;				
				fSelectedResources.add(element.resource());
			} else if (obj instanceof IResource) {
				fSelectedResources.add((IResource) obj);
			}
		}
		return !fSelectedResources.isEmpty();

	}
	
	@Override
	public void run() {
		if (fIsGlossary){
			fGlossaryBuffer.setGlossaryFields(fSelectedObjects);
			return;
		}
		
		if (fIsVariable) {
			fVariableBuffer.setVariables(fSelectedObjects);
			return;
		}
	
		IResource[] resources = fSelectedResources.toArray(new IResource[fSelectedResources.size()]);
		// Get the file names and a string representation
		final int length = resources.length;
		int actualLength = 0;
		String[] fileNames = new String[length];
		StringBuffer buf = new StringBuffer();
		for (int i = 0; i < length; i++) {
			IPath location = resources[i].getLocation();
			// location may be null. See bug 29491.
			if (location != null) {
				fileNames[actualLength++] = location.toOSString();
			}
			if (i > 0) {
				buf.append("\n"); //$NON-NLS-1$
			}
			buf.append(resources[i].getName());
		}
		// was one or more of the locations null?
		if (actualLength < length) {
			String[] tempFileNames = fileNames;
			fileNames = new String[actualLength];
			for (int i = 0; i < actualLength; i++) {
				fileNames[i] = tempFileNames[i];
			}
		}
		setClipboard(resources, fileNames, buf.toString());
	}

	private void setClipboard(IResource[] resources, String[] fileNames, String names) {
		try {
			// set the clipboard contents
			if (fileNames.length > 0) {
				fClipboard.setContents(new Object[] { resources, fileNames, names }, new Transfer[] {
						ResourceTransfer.getInstance(), FileTransfer.getInstance(), TextTransfer.getInstance() });
			} else {
				fClipboard.setContents(new Object[] { resources, names },
						new Transfer[] { ResourceTransfer.getInstance(), TextTransfer.getInstance() });
			}
		} catch (SWTError e) {
			if (e.code != DND.ERROR_CANNOT_SET_CLIPBOARD) {
				throw e;
			}
			WorkbenchUtitlities.openError(Messages.CopyAction_Copy_problem_title, Messages.CopyAction_Copy_problem_question);
			//setClipboard(resources, fileNames, names);
			/*if (MessageDialog.openQuestion(fShell, Messages.CopyAction_Copy_problem_title, Messages.CopyAction_Copy_problem_question)) {
				setClipboard(resources, fileNames, names);
			}*/
		}
	}
	
}
