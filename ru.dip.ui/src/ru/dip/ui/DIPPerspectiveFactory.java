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
package ru.dip.ui;

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;
import org.eclipse.ui.navigator.resources.ProjectExplorer;

import ru.dip.ui.glossary.GlossaryView;
import ru.dip.ui.imageview.ImagesView;
import ru.dip.ui.imageview.TablesView;
import ru.dip.ui.toc.DipTocView;

public class DIPPerspectiveFactory implements IPerspectiveFactory {

	private IPageLayout fFactory;
	public static final String ID = Messages.DIPPerspectiveFactory_DIPPerspectiveID;
	
	
	// надо вынести всё идентификаторы в одно место
	public static final String DIP_VIEW = Messages.DIPPerspectiveFactory_DIPViewID;
	public static final String EDIT_VIEW = Messages.DIPPerspectiveFactory_EditVIewID;
	public static final String INCORRECT_LINKS_VIEW = Messages.DIPPerspectiveFactory_LinksViewID;
	public static final String SPELL_CHECKING = "ru.dip.editors.view.checkspell";
	public static final String GIT_HISTORY = "org.eclipse.team.ui.GenericHistoryView";
	public static final String STAGING_VIEW = "org.eclipse.egit.ui.StagingView";
	
	@Override
	public void createInitialLayout(IPageLayout layout) {
		fFactory = layout;
		addViews();
	}

	private void addViews() {
		IFolderLayout topLeft = fFactory.createFolder("topLeft",  //$NON-NLS-1$
				IPageLayout.LEFT, 0.2f, fFactory.getEditorArea());
		topLeft.addView(ProjectExplorer.VIEW_ID);
		topLeft.addPlaceholder(IPageLayout.ID_PROJECT_EXPLORER);
		topLeft.addView(DipTocView.ID);
		
		IFolderLayout bottom = fFactory.createFolder("bottomRight",  //$NON-NLS-1$
				IPageLayout.BOTTOM, 0.6f, fFactory.getEditorArea());								
		bottom.addView(DIP_VIEW);
		bottom.addView(EDIT_VIEW);
		bottom.addView(INCORRECT_LINKS_VIEW);
		bottom.addView(SPELL_CHECKING);
		bottom.addView(GlossaryView.ID);
		bottom.addView(GIT_HISTORY);
		bottom.addView(STAGING_VIEW);
		
		IFolderLayout topRight = fFactory.createFolder("topRight", 
				IPageLayout.RIGHT, 0.8f, fFactory.getEditorArea());	
		topRight.addView(ImagesView.ID);
		topRight.addView(TablesView.ID);		
	}
	
}
