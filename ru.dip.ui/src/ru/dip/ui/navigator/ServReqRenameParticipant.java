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
package ru.dip.ui.navigator;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.CheckConditionsContext;
import org.eclipse.ltk.core.refactoring.participants.RenameParticipant;
import org.eclipse.swt.widgets.Display;

import ru.dip.core.manager.DipNatureManager;
import ru.dip.core.model.DipProject;
import ru.dip.core.model.DipRoot;
import ru.dip.core.model.interfaces.IParent;
import ru.dip.core.model.interfaces.IDipElement;
import ru.dip.core.model.interfaces.IDipParent;
import ru.dip.core.utilities.DipUtilities;
import ru.dip.core.utilities.WorkbenchUtitlities;

public class ServReqRenameParticipant extends RenameParticipant {

	private IFolder fFolder;
	private IProject fProject;
	private String fNewName;
	
	public ServReqRenameParticipant() {
	}
	
	@Override
	protected boolean initialize(Object element) {				
		return isServeRename(element);
	}
	
	private boolean isServeRename(Object element) {
		if (element instanceof IFolder) {			
			fFolder = (IFolder) element;
			if (!fFolder.getName().startsWith("__")) { //$NON-NLS-1$
				return false;
			}			
			fProject = ((IFolder) element).getProject();
			return DipNatureManager.hasNature(fProject);
		}
		return false;
	}

	@Override
	public String getName() {
		return "name"; //$NON-NLS-1$
	}

	@Override
	public RefactoringStatus checkConditions(IProgressMonitor pm, CheckConditionsContext context)
			throws OperationCanceledException {	
		return null;
	}

	@Override
	public Change createChange(IProgressMonitor pm) throws CoreException, OperationCanceledException {		
		fNewName = getArguments().getNewName();
		if (!fNewName.startsWith("__")) {			 //$NON-NLS-1$
			return new ReqChange();			
		} 
		return null;
	}
	
	class ReqChange extends Change {
		
		@Override
		public String getName() {
			return null;
		}

		@Override
		public void initializeValidationData(IProgressMonitor pm) {
		}

		@Override
		public RefactoringStatus isValid(IProgressMonitor pm) throws CoreException, OperationCanceledException {
			return null;
		}

		@Override
		public Change perform(IProgressMonitor pm) throws CoreException {
			return null;
		}

		@Override
		public Object getModifiedElement() {
			return null;
		}

		@Override
		public void dispose() {
			createDipFolder();
			super.dispose();
			updateProjectExpolorer();
		}
		
		private void createDipFolder() {
			DipProject project = DipRoot.getInstance().getDipProject(fProject);
			IContainer folder = fFolder.getParent();
			IFolder newFolder = fFolder.getParent().getFolder(new Path(fNewName));
			IDipElement element = DipUtilities.findDipElementInProject(folder, project);
			if (element instanceof IDipParent) {
				((IDipParent) element).createFolder(newFolder);
				((IDipParent) element).refresh();
			}
			if (element instanceof IParent) {
				((IParent) element).refresh();
			}
			project.refresh();
		}
		
		private void updateProjectExpolorer() {
			Display.getDefault().asyncExec(new Runnable() {

				@Override
				public void run() {
					WorkbenchUtitlities.updateProjectExplorer();
				}
			});
		}

	}

}
