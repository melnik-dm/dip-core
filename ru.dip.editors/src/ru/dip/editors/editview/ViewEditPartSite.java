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
package ru.dip.editors.editview;

import org.eclipse.e4.core.contexts.ContextFunction;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorActionBarContributor;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.SubActionBars;
import org.eclipse.ui.contexts.IContextService;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.internal.EditorActionBars;
import org.eclipse.ui.internal.PartSite;
import org.eclipse.ui.internal.ViewSite;
import org.eclipse.ui.internal.contexts.NestableContextService;
import org.eclipse.ui.internal.e4.compatibility.ActionBars;
import org.eclipse.ui.internal.expressions.ActivePartExpression;
import org.eclipse.ui.internal.handlers.LegacyHandlerService;
import org.eclipse.ui.internal.services.INestable;
import org.eclipse.ui.internal.services.IServiceLocatorCreator;
import org.eclipse.ui.internal.services.IWorkbenchLocationService;
import org.eclipse.ui.internal.services.ServiceLocator;
import org.eclipse.ui.internal.services.WorkbenchLocationService;
import org.eclipse.ui.part.EditorActionBarContributor;

import ru.dip.editors.formeditor.FormsEditorActionBarContributor;
import ru.dip.editors.formeditor.FormsEditor;
import ru.dip.editors.md.MDEditorContributor;
import ru.dip.editors.md.MDEditorContributor.OpenViewAction;
import ru.dip.editors.md.MDEditorContributor.SyntaxHighlighting;
import ru.dip.editors.md.actions.BoldAction;
import ru.dip.editors.md.actions.ItalicAction;
import ru.dip.ui.table.editor.DipTableActionContributor;

@SuppressWarnings("unused")
public class ViewEditPartSite extends ViewSite implements IEditorSite,  INestable {

	private PartSite fWorkbenchPartSite;
	private EditorActionBarContributor fActiveContributor;

	public ViewEditPartSite(PartSite site, IEditorPart editor) {
		super(site.getModel(), site.getPart(), site.getPartReference(), null);
		fWorkbenchPartSite = (PartSite) site;		
	}

	@Override
	public IActionBars getActionBars() {
		IActionBars bars =  fWorkbenchPartSite.getActionBars();
		return bars;
	}
	
	@Override
	public void activateActionBars(boolean forceVisibility) {
		fWorkbenchPartSite.activateActionBars(forceVisibility);
	}
	

	@Override
	public void deactivateActionBars(boolean forceHide) {
		fWorkbenchPartSite.deactivateActionBars(forceHide);
	}

	
	@Override
	public void activate() {
	}

	@Override
	public void deactivate() {
		
	}

	@Override
	public IEditorActionBarContributor getActionBarContributor() {
		IActionBars bars = getActionBars();
		if (bars != null && bars instanceof EditorActionBars) {
			return ((EditorActionBars)bars).getEditorContributor();
		}
		return null;
	}
	
	public FormsEditorActionBarContributor getReqEditorContributor(IEditorPart editor) {
		FormsEditorActionBarContributor contributor = new FormsEditorActionBarContributor();
		contributor.init(getActionBars());
		contributor.setEditViewActivePage(getActionBars(), (FormsEditor) editor);
		return contributor;
	}
	
	public void initReqEditorContributor(IEditorPart editor) {
		FormsEditorActionBarContributor contributor = new FormsEditorActionBarContributor();
		contributor.contributeEditView(getActionBars(), editor);
		fActiveContributor = contributor;
	}
	
		
	public void initMDEditorContributor(IEditorPart editor) {
		MDEditorContributor contributor = new MDEditorContributor();	
		contributor.contributeEditView(getActionBars(), editor);
		fActiveContributor = contributor;
	}
	
	public void disposeContributor() {
		if (fActiveContributor instanceof MDEditorContributor) {
			IActionBars bars = getActionBars();
			((MDEditorContributor) fActiveContributor).disposeEditView(bars);
			fActiveContributor = null;
		} else if (fActiveContributor instanceof FormsEditorActionBarContributor) {
			IActionBars bars = getActionBars();
			((FormsEditorActionBarContributor) fActiveContributor).disposeEditView(bars);
			fActiveContributor = null;
		}
	}
	
	@Override
	public void registerContextMenu(MenuManager menuManager, ISelectionProvider selectionProvider,
			boolean includeEditorInput) {		
	}

	@Override
	public void registerContextMenu(String menuId, MenuManager menuManager, ISelectionProvider selectionProvider,
			boolean includeEditorInput) {
	}
	
}