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

import ru.dip.core.external.editors.IDipHtmlRenderExtension;
import ru.dip.core.model.DipProject;
import ru.dip.core.unit.TextPresentation;
import ru.dip.core.utilities.DipUtilities;
import ru.dip.core.utilities.EditorUtils;
import ru.dip.core.utilities.FileUtilities;
import ru.dip.core.utilities.TagStringUtilities;
import ru.dip.core.utilities.xml.XmlStringUtilities;
import ru.dip.table.Messages;
import ru.dip.table.editor.table.TablePage;
import ru.dip.table.editor.text.TableTextEditor;

public class MultiPageTableEditor extends FormEditor implements IDipHtmlRenderExtension {

	public static final String ID = Messages.MultiPageTableEditor_ID;
		
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
		firePropertyChange(EditorUtils.SAVE_EVENT);
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
	
	//=======================
	// for dip render
	
	@Override
	public String getHtmlPresentation() {
		String html = FileUtilities.readFile(fFile, ""); //$NON-NLS-1$
		html = addBorderAttr(html);
		html = changeLinks(html, fFile);	
		return html;
	}
	
	private String addBorderAttr(String original) {
		String[] lines = original.split("\n");			 //$NON-NLS-1$
		if (lines.length > 0) {
			String tag = lines[0];
			String newTag = XmlStringUtilities. changeValueAttribut("border", "1", tag); //$NON-NLS-1$ //$NON-NLS-2$
			lines[0] = newTag;
			StringBuilder builder = new StringBuilder();
			for (String str : lines) {
				builder.append(str);
				builder.append(TagStringUtilities.lineSeparator());
			}
			return builder.toString();
		}
		return original;
	}
	
	private String changeLinks(String original, IFile file) {
		DipProject dipProject = DipUtilities.findDipProject(file);
		if (original != null && !original.isEmpty() && dipProject != null) {
			return TextPresentation.prepareTextWithoutUnit(original, dipProject);
		}
		return original;
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

	@Override
	public IFile getFile() {
		return fFile;
	}
}
