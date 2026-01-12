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
package ru.dip.editors.report;

import java.io.File;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.part.FileEditorInput;

import ru.dip.core.model.DipProject;
import ru.dip.core.model.interfaces.IDipElement;
import ru.dip.core.model.interfaces.IDipParent;
import ru.dip.core.model.interfaces.ISchemaContainer;
import ru.dip.core.report.model.report.ReportEntry;
import ru.dip.core.utilities.DipUtilities;
import ru.dip.core.utilities.ResourcesUtilities;
import ru.dip.editors.Messages;
import ru.dip.editors.report.content.ContentPage;
//import ru.dip.editors.report.content.ContentPage;
import ru.dip.editors.report.xml.RulesXmlEditor;

public class ReportEditor  extends  FormEditor {
	
	private ReportPage fReportPage;
	private ContentPage fContentPage;
	private RulesXmlEditor fRulesEditor;
	
	// model
	private DipProject fDipProject;
	private List<ISchemaContainer> fSchemaContainers;
	private List<Repository> fProjectRepositories;
	private IFile fFile;
	private File fProjectFile;
	private long fLastModifiedTime = -1; // время последнего изменения проекта
	
	public ReportEditor() {}
	
	@Override
	protected void setInput(IEditorInput input) {
		super.setInput(input);
		if (input instanceof FileEditorInput){
			FileEditorInput fileEditorInput = (FileEditorInput) input;
			fFile = fileEditorInput.getFile();
			fDipProject = DipUtilities.findDipProject(fFile);
			fSchemaContainers = List.of(fDipProject);
			fProjectRepositories = fDipProject.getGitRepo() != null ?
					List.of(fDipProject.getGitRepo()) : Collections.emptyList();
			fProjectFile = Path.of(fFile.getProject().getLocationURI()).toFile();
			setPartName(fFile.getName());
		}
	}
	
	@Override
	protected void addPages() {
		addTextEntriesPage();		
		addContentPage();		
		addTextEditorPage();
		addPageListener();
	}
	
	private void addTextEntriesPage(){
		fReportPage = new ReportPage(this, Messages.ReportEditor_ReportPageTitle);
		try {
			addPage(fReportPage);
		} catch (PartInitException e) {
			e.printStackTrace();
		}
	}
	
	private void addContentPage() {
		fContentPage = new ContentPage(this, Messages.ReportEditor_ContentPageTitle);
		try {
			addPage(fContentPage);
		} catch (PartInitException e) {
			e.printStackTrace();
		}
	}
	
	private void addTextEditorPage(){
		fRulesEditor = new RulesXmlEditor(this);
		try {
			int pageNumber = addPage(((IEditorPart) fRulesEditor), getEditorInput());
			setPageText(pageNumber, Messages.ReportEditor_RulesPageTitle);	
		} catch (PartInitException e) {
			e.printStackTrace();
		}
	}
	
	private void addPageListener() {
		addPageChangedListener(e -> updatePage());
	}
	
	@Override
	public void setFocus() {
		super.setFocus();
		updatePage();
	}
	
	/**
	 * Обновить страницу если были изменения в проекте (в т.ч. в самом отчете)
	 */
	private void updatePage() {
		int activePage = getActivePage();
		if (activePage <= 1 && fProjectFile != null){
			long currentModifiedProjectTime = ResourcesUtilities.getLatestModifiedDate(fProjectFile);
			if (currentModifiedProjectTime != fLastModifiedTime) {
				fReportPage.update();
				fContentPage.update();
				fLastModifiedTime = currentModifiedProjectTime;
			}
		} 
	}
	
	@Override
	public void doSave(IProgressMonitor monitor) {
		fRulesEditor.doSave(monitor);
	}

	@Override
	public void doSaveAs() {
		fRulesEditor.doSaveAs();
	}

	@Override
	public boolean isSaveAsAllowed() {
		return false;
	} 
	
	public List<ReportEntry> getEntries(){
		return fRulesEditor.getEntries();
	}
	
	public String getDescription(){
		return fRulesEditor.getDescription();
	}
	
	public IDipParent getDipProject(){
		IDipElement element = DipUtilities.findElement(fFile);
		if (element != null){
			return element.dipProject();
		}
		return null;
	}

	public ReportPage getReportPage() {
		return fReportPage;
	}
	
	public List<ISchemaContainer> getSchemaContainers(){
		return fSchemaContainers;
	}
	
	public List<Repository> getProjectRepositories(){
		return fProjectRepositories;
	}
	
}
