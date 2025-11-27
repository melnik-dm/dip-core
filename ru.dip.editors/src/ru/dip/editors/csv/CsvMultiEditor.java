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
package ru.dip.editors.csv;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.IPageChangedListener;
import org.eclipse.jface.dialogs.PageChangedEvent;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.part.FileEditorInput;

import ru.dip.core.utilities.FileUtilities;
import ru.dip.editors.Messages;

public class CsvMultiEditor extends FormEditor  {

	private CsvTextEditor fTextEditor;
	private CsvTablePage fTablePage;
	private IFile fFile;
	
	@Override
	protected void setInput(IEditorInput input) {
		super.setInput(input);
		if (input instanceof FileEditorInput){
			FileEditorInput fileEditorInput = (FileEditorInput) input;
			fFile = fileEditorInput.getFile();
			String filename = FileUtilities.getNameWithoutExtension(fFile);
			setPartName(filename + Messages.CsvMultiEditor_0);
		}	
	}
	
	@Override
	protected void addPages() {
		fTablePage = new CsvTablePage(this);
		fTextEditor = new CsvTextEditor();
		try {
			addPage(fTablePage);
			int pageNumber = addPage(((IEditorPart) fTextEditor), getEditorInput());
			setPageText(pageNumber, Messages.CsvMultiEditor_RawTextPageName);	
		} catch (PartInitException e) {
			e.printStackTrace();
		}
		addPageListener();		
	}
	
	private void addPageListener() {
		addPageChangedListener(new IPageChangedListener() {	

			@Override
			public void pageChanged(PageChangedEvent event) {
				int activePage = getActivePage();
				if (activePage == 0){
					fTablePage.update();
				} 
			}					
		});
	}

	//==============================
	// save
	
	@Override
	public void doSave(IProgressMonitor monitor) {
		fTextEditor.doSave(monitor);
	}

	@Override
	public void doSaveAs() {
		fTextEditor.doSaveAs();
	}

	@Override
	public boolean isSaveAsAllowed() {
		return true;
	}

	//=============================
	// getters & setters
	
	public String getText(){
		return fTextEditor.getDocument().get();
	}
	
	public IFile getFile(){
		return fTextEditor.getFile();
	}
	
	public CsvTextEditor getCsvTextEditor(){
		return fTextEditor;
	}
	
}
