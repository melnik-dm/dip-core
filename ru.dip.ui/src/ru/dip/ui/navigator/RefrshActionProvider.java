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

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.window.IShellProvider;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbenchCommandConstants;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.actions.RefreshAction;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.eclipse.ui.navigator.CommonActionProvider;
import org.eclipse.ui.navigator.ICommonActionExtensionSite;

import ru.dip.core.model.DipTableContainer;
import ru.dip.core.model.interfaces.IDipElement;
import ru.dip.core.utilities.ResourcesUtilities;
import ru.dip.core.utilities.WorkbenchUtitlities;

public class RefrshActionProvider extends CommonActionProvider  {
		
	private RefreshAction fRefreshAction;
	private Shell shell;

	
	@Override
	public void init(ICommonActionExtensionSite aSite) {
		super.init(aSite);
		shell = aSite.getViewSite().getShell();
		makeActions();
	}
	
	
	@Override
	public void fillActionBars(IActionBars actionBars) {
		actionBars.setGlobalActionHandler(ActionFactory.REFRESH.getId(), fRefreshAction);
		updateActionBars();
	}
	
	protected void makeActions() {
		IShellProvider sp = new IShellProvider() {
			@Override
			public Shell getShell() {
				return shell;
			}
		};
		fRefreshAction = new RefreshAction(sp) {
			
			@Override
			public void run() {
				standardRefresh();
				dipRefresh();
				WorkbenchUtitlities.updateProjectExplorer();
			}
			
			private void dipRefresh(){
				IStructuredSelection selection = getStructuredSelection();
				if (selection != null && !selection.isEmpty()) {
					Object[] objs = selection.toArray();
					for (int i = objs.length - 1; i >= 0; i--) {
						if (objs[i] instanceof IDipElement) {
							if (objs[i] instanceof DipTableContainer) {
								((DipTableContainer) objs[i]).setNeedUpdate();
							}							
							ResourcesUtilities.updateProject(((IDipElement)objs[i]).dipProject().getProject());
							((IDipElement)objs[i]).dipProject().refresh();
						}
					}
				}
			}
			
			private void standardRefresh(){
				final IStatus[] errorStatus = new IStatus[1];
				errorStatus[0] = Status.OK_STATUS;
				final WorkspaceModifyOperation op = (WorkspaceModifyOperation) createOperation(errorStatus);
				WorkspaceJob job = new WorkspaceJob("refresh") { //$NON-NLS-1$

					@Override
					public IStatus runInWorkspace(IProgressMonitor monitor) throws CoreException {
						try {
							op.run(monitor);
							if (shell != null && !shell.isDisposed()) {
								shell.getDisplay().asyncExec(new Runnable() {
									@Override
									public void run() {
										StructuredViewer viewer = getActionSite().getStructuredViewer();
										if (viewer != null && viewer.getControl() != null && !viewer.getControl().isDisposed()) {
											viewer.refresh();
										}
									}
								});
							}
						} catch (InvocationTargetException e) {
							//String msg = NLS.bind(WorkbenchNavigatorMessages.ResourceMgmtActionProvider_logTitle, getClass().getName(), e.getTargetException());
							//throw new CoreException(new Status(IStatus.ERROR, NavigatorPlugin.PLUGIN_ID, IStatus.ERROR, msg, e.getTargetException()));
						} catch (InterruptedException e) {
							return Status.CANCEL_STATUS;
						}
						return errorStatus[0];
					}
				};
				ISchedulingRule rule = op.getRule();
				if (rule != null) {
					job.setRule(rule);
				}
				job.setUser(true);
				job.schedule();
			}
		};
		fRefreshAction.setActionDefinitionId(IWorkbenchCommandConstants.FILE_REFRESH);
	}
		
	@Override
	public void updateActionBars() {
		IStructuredSelection selection = (IStructuredSelection) getContext().getSelection();
		fRefreshAction.selectionChanged(selection);
	}

}
