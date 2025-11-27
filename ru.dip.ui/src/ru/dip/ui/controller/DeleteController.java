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
package ru.dip.ui.controller;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.swt.widgets.Shell;

import ru.dip.core.exception.CopyDIPException;
import ru.dip.core.exception.DeleteDIPException;
import ru.dip.core.exception.TmpCopyException;
import ru.dip.core.link.LinkInteractor;
import ru.dip.core.model.DipFolder;
import ru.dip.core.model.DipProject;
import ru.dip.core.model.interfaces.IDipElement;
import ru.dip.core.utilities.DipUtilities;
import ru.dip.core.utilities.WorkbenchUtitlities;
import ru.dip.core.utilities.tmp.DeleteResultOperation;
import ru.dip.core.utilities.tmp.ErrorResult;
import ru.dip.core.utilities.tmp.ExtractResult;
import ru.dip.core.utilities.tmp.ResultOperation;
import ru.dip.ui.Messages;

public class DeleteController {
	
	public static class DeleteControllerBuilder {
		
		private IDipElement[] fElements;
		private boolean fReserve = false;
		private boolean fExtractContent = false;
		private boolean fDeleteProjectContent = true;
		
		public DeleteControllerBuilder elements(IDipElement[] elements) {
			fElements = elements;
			return this;
		}
		
		public DeleteControllerBuilder element(IDipElement element) {
			fElements = new IDipElement[] {element};
			return this;
		}
		
		public DeleteControllerBuilder reserve(boolean reserve) {
			fReserve = reserve;
			return this;
		}
		
		public DeleteControllerBuilder extract(boolean extract) {
			fExtractContent = extract;
			return this;
		}
		
		public DeleteControllerBuilder deleteProjectContent(boolean deleteProjectContent) {
			fDeleteProjectContent = deleteProjectContent;
			return this;
		}
		
		public DeleteController build() {
			DeleteController controller = new DeleteController(fElements);
			controller.fReserve = fReserve;
			controller.fExtractContent = fExtractContent;
			controller.fDeleteProjectContent = fDeleteProjectContent;
			return  controller;
		}
				
	}
		
	private IDipElement[] fElements;
	private boolean fReserve = false;
	private boolean fExtractContent = false;
	private boolean fDeleteProjectContent = true;
	
	private Set<DipProject> fChangedProjects = new HashSet<>();
	
	public DeleteController(IDipElement[] elements){
		fElements = elements;
	}
	
	public void setReserve() {
		fReserve = true;
	}
	
	public void setExtractContent() {
		fExtractContent = true;
	}
	
	public void setNotDeleteProjectContent() {
		fDeleteProjectContent = false;
	}
		
	public ResultOperation delete() {
		getChangedProject();
		try {			
			if (fExtractContent) {
				return doExtract();
			} else {
				return doDelete();
			}
		} catch (DeleteDIPException | CopyDIPException e) {
			e.printStackTrace();
			WorkbenchUtitlities.openError(Messages.DeleteDialog_Delete_error_title_message, e.getMessage());
			return new ErrorResult(e);
		}		
	}
	
	private void getChangedProject() {
		fChangedProjects.clear();
		for (IDipElement element: fElements) {
			if (element.dipProject() == null) {
				continue;
			}
			fChangedProjects.add(element.dipProject());
		}
	}
	
	private ExtractResult doExtract() throws DeleteDIPException, CopyDIPException {		
		IStatus status = DipUtilities.canExtract((DipFolder) fElements[0]);
		if (!status.isOK()) {
			WorkbenchUtitlities.openError(Messages.DeleteDialog_ExtractErrorTitle, status.getMessage());
			return null;
		}					
		return DipUtilities.moveUp((DipFolder) fElements[0], fReserve, getShell());		
	}
	
	private DeleteResultOperation doDelete() throws DeleteDIPException, TmpCopyException {
		return  DipUtilities.deleteElements(fElements, fReserve, fDeleteProjectContent, getShell());
	}
	
	public Shell getShell() {
		return WorkbenchUtitlities.getShell();
	}
	
	public void checkLinks() {
		for (DipProject project: fChangedProjects) {
			LinkInteractor.instance().checkLinksAfterDelete(project);
		}
	}

}
