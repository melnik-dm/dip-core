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
package ru.dip.ui.action.open;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.IWorkbenchSite;
import org.eclipse.ui.actions.ActionGroup;
import org.eclipse.ui.navigator.ICommonMenuConstants;

import ru.dip.core.model.DipProject;
import ru.dip.core.model.DnfoTable;

public class OpenEditorActionGroup extends ActionGroup {

	private IWorkbenchSite fSite;
	private boolean fIsEditorOwner;
	private OpenAction fOpen;
	private OpenInDocumentAction fOpenInDocument;
	private ISelectionProvider fSelectionProvider;

	public OpenEditorActionGroup(IViewPart part) {
		this(part.getSite(), null);
	}

	public OpenEditorActionGroup(IWorkbenchPartSite site, ISelectionProvider specialSelectionProvider) {
		fSite= site;
		fOpen= new OpenAction(fSite);
		fOpenInDocument = new OpenInDocumentAction(fSite);
		fSelectionProvider= specialSelectionProvider == null ? fSite.getSelectionProvider() : specialSelectionProvider;
		initialize();
		if (specialSelectionProvider != null){
			fOpen.setSpecialSelectionProvider(specialSelectionProvider);
		}
	}

	public IAction getOpenAction() {
		return fOpen;
	}

	private void initialize() {
		ISelection selection= fSelectionProvider.getSelection();
		fOpen.update(selection);
		if (!fIsEditorOwner) {
			fSelectionProvider.addSelectionChangedListener(fOpen);
		}
	}

	@Override
	public void fillActionBars(IActionBars actionBar) {
		super.fillActionBars(actionBar);
		setGlobalActionHandlers(actionBar);
	}

	@Override
	public void fillContextMenu(IMenuManager menu) {
		IStructuredSelection selection = (IStructuredSelection) getContext().getSelection();
		if (selection != null && selection.size() == 1 && selection.getFirstElement() instanceof DnfoTable){		
			appendToGroup(menu, fOpen);
			DnfoTable table = (DnfoTable) selection.getFirstElement();
			if (!(table.parent() instanceof DipProject)) {
				appendToGroup(menu, fOpenInDocument);
			}
		}
	}

	@Override
	public void dispose() {
		fSelectionProvider.removeSelectionChangedListener(fOpen);
		super.dispose();
	}

	private void setGlobalActionHandlers(IActionBars actionBars) {
		actionBars.setGlobalActionHandler(ICommonMenuConstants.GROUP_OPEN, fOpen);
	}

	private void appendToGroup(IMenuManager menu, IAction action) {
		if (action.isEnabled())
			menu.appendToGroup(ICommonMenuConstants.GROUP_OPEN, action);
	}

}
