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
package ru.dip.ui.wizard.paste;

import java.util.stream.Stream;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;

import ru.dip.core.model.interfaces.IDipParent;
import ru.dip.core.utilities.DipUtilities;
import ru.dip.core.utilities.status.StatusUtils;

public abstract class PasteElement {
	
	private IDipParent fParent;
	private String fNewName;
	private boolean fEnabled = true;
	private IStatus fStatus;

	PasteElement(IDipParent targetParent) {
		fParent = targetParent;
	}

	public abstract boolean isFolder();

	public abstract java.nio.file.Path getPath();
	
	public abstract String getOldName();


	public String getId(IDipParent dipParent) {
		return getId(dipParent, getNewName());
	}	
	
	public String getId(IDipParent dipParent, String name) {
		return DipUtilities.relativeProjectID(dipParent) + "/" + name;

	}
	
	//===========================
	// status
	
	public void checkStatus(String[] names) {				
		fStatus = checkStatus(names, fNewName);
	}
	
	public IStatus checkStatus(String[] names, String newName) {
		IStatus status = DipUtilities.canPaste(fParent, getResourceWithNewName(newName));
		if (status.isOK()) {
			long nameCount = Stream.of(names).filter(newName::equals).count();
			if (nameCount > 1) {				
				status = StatusUtils.FILE_ALREADY_EXISTS;
			}
		}
		return status;
	}
	
	private IResource getResourceWithNewName(String newName) {
		if (isFolder()) {
			return fParent.resource().getFolder(new Path(newName));
		} else {
			return fParent.resource().getFile(new Path(newName));
		}
	}
	
	
	public String getStatusMessage() {
		if (fStatus.isOK()) {
			return "Ok";
		} else {
			return "Error";
		}
	}
		
	public String getOriginalStatusMessage() {
		return fStatus.getMessage();
	}
	
	public boolean isStatusOk() {
		return fStatus.isOK();
	}
	
	//====================
	// getter & setter
	
	public String getNewName() {
		return fNewName;
	}
	
	public void setNewName(String newName) {
		fNewName = newName;
	}
	
	public void setTargetParent(IDipParent newTargetParent) {
		fParent = newTargetParent;
	}
	
	public void setEnable(boolean enabled) {
		fEnabled = enabled;
	}
	
	public boolean isEnable() {
		return fEnabled;
	}

}
