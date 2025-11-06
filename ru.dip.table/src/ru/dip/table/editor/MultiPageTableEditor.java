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
package ru.dip.table.editor;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.IPageChangedListener;
import org.eclipse.jface.dialogs.PageChangedEvent;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.part.FileEditorInput;

import ru.dip.table.Messages;
import ru.dip.table.editor.table.TablePage;
import ru.dip.table.editor.text.TableTextEditor;

public class MultiPageTableEditor extends FormEditor {

	public static final String ID = Messages.MultiPageTableEditor_ID;
	
	public static final int SAVE_EVENT = 88;
	public static final int VISIBLE_EVENT = 89;
	public static final int HIDE_EVENT = 90;
	
	private TableTextEditor fTextEditor;
	private TablePage fTablePage;
	private IFile fFile;
	private boolean fDirty = false;
	private boolean fStart = true;
	
	@Override
	protected void setInput(IEditorInput input) {
		super.setInput(input);
		if (input instanceof FileEditorInput){
			FileEditorInput fileEditorInput = (FileEditorInput) input;
			fFile = fileEditorInput.getFile();
			setPartName(fFile.getName());
		}
	}
	
	@Override
	public String getTitle() {
		if (fFile != null){
			return fFile.getName();
		}
		return "title"; //$NON-NLS-1$
	}
	
	@Override
	protected void addPages() {
		addTablePage();
		addTextEditorPage();
		addPageListener();
	}

	private void addTablePage(){
		fTablePage = new TablePage(this, Messages.MultiPageTableEditor_TablePageName, Messages.MultiPageTableEditor_TablePageTitle);
		try {
			addPage(fTablePage);
		} catch (PartInitException e) {
			e.printStackTrace();
		}
	}
	
	private void addTextEditorPage(){
		fTextEditor = new TableTextEditor(this);
		try {
			int pageNumber = addPage(((IEditorPart) fTextEditor), getEditorInput());
			setPageText(pageNumber, Messages.MultiPageTableEditor_RawTextTitle);	
		} catch (PartInitException e) {
			e.printStackTrace();
		}
	}
	
	private void addPageListener() {
		addPageChangedListener(new IPageChangedListener() {	

			@Override
			public void pageChanged(PageChangedEvent event) {
				if (fStart) {
					fStart = false;
					return;
				}
				
				int activePage = getActivePage();
				if (activePage == 0){
					fTablePage.updateBrowser();
				} else {
					if (fDirty) {
						fTextEditor.updateContent(fTablePage.getContent());
					}
				}
			}					
		});
	}
		
	//==================
	// events
	
	public void visible() {
		firePropertyChange(VISIBLE_EVENT);
	}
	
	public void hide() {
		firePropertyChange(HIDE_EVENT);
	}
	
	//=======================
	// save
	
	@Override
	public void doSave(IProgressMonitor monitor) {
		fDirty = false;
		
		Display.getDefault().syncExec(new Runnable() {
			
			@Override
			public void run() {
				if (getActivePage() == 0) {
					fTextEditor.updateContent(fTablePage.getContent());
				}
				fTextEditor.doSave(monitor);
			}
		});
		
		fireDirtyPropertyChange();
	}

	public void fireDirtyPropertyChange() {
		firePropertyChange(PROP_DIRTY);
		firePropertyChange(SAVE_EVENT);
	}
	
	@Override
	public void doSaveAs() {
		fTextEditor.doSaveAs();
	}

	@Override
	public boolean isSaveAsAllowed() {
		return false;
	}
	
	//========================
	// dirty
	
	@Override
	public boolean isDirty() {
		return fDirty || fTextEditor.isDirty();
	}
	
	public void setDirty(boolean b) {
		fDirty = b;
	}
	
	public void setDirty() {
		fDirty = true;
		firePropertyChange(PROP_DIRTY);
	}
	
	//=========================
	// getters
	
	public String content() {
		String result =  fTextEditor.content()
				.replace("\r\n", "")
				.replace("\n", "") //$NON-NLS-1$ //$NON-NLS-2$
				.replace("\\", "\\\\");
		return result;
	}
	
	public TableTextEditor textEditor() {
		return fTextEditor;
	}
}
