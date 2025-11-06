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
package ru.dip.editors.formeditor;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.IPageChangedListener;
import org.eclipse.jface.dialogs.PageChangedEvent;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.ide.IGotoMarker;
import org.eclipse.ui.part.FileEditorInput;
import org.xml.sax.SAXException;

import ru.dip.core.form.model.Tag;
import ru.dip.core.manager.DipNatureManager;
import ru.dip.core.model.DipProject;
import ru.dip.core.model.DipUnit;
import ru.dip.core.model.interfaces.IDipElement;
import ru.dip.core.schema.SchemaReader;
import ru.dip.core.unit.form.FormPresentation;
import ru.dip.core.utilities.DipUtilities;
import ru.dip.core.utilities.WorkbenchUtitlities;
import ru.dip.editors.Messages;
import ru.dip.editors.formeditor.fieldpage.FieldsPage;
import ru.dip.editors.formeditor.model.FormModel;
import ru.dip.editors.formeditor.xml.FormTextEditor;
import ru.dip.editors.md.ISaveNotifier;
import ru.dip.editors.md.MDEditor;
import ru.dip.ui.dialog.DefaultSchemaMessage;

public class FormsEditor extends  FormEditor implements IGotoMarker, ISaveNotifier {

	public static final String editorId = Messages.FormsEditor_ID;
	
	private FieldsPage fFieldsPage;
	private FormTextEditor fTextEditor;
	private FormModel fFormModel = new FormModel(this);
	private SchemaReader fSchemaReader;
	private FormsEditorActionBarContributor fActionBarContributor;
	private IDipElement fDipElement;
	protected IDocument fDocument;
	protected IFile fFile;
	
	private boolean fShowWarningMessage = true;
	private boolean fUndoRedoProcessing = false;  // флаг, когда true - значит выполняется undo/redo command
			
	//=========================
	// pages
	
	@Override
	protected void addPages() {
		addTextEntriesPage();
		addTextEditorPage();
		addPageListener();
	}

	@Override
	public void setFocus() {
		super.setFocus();
		if (isOpenDefaultMessage()){
			openDefaultSchemaMessage();
			fShowWarningMessage = false;
		}
	}
		
	@Override
	protected void setInput(IEditorInput input) {
		super.setInput(input);
		if (input instanceof FileEditorInput){
			FileEditorInput fileEditorInput = (FileEditorInput) input;
			fFile = fileEditorInput.getFile();
			DipProject dipProject = DipUtilities.findDipProject(fFile);
			fDipElement = DipUtilities.findDipElementInProject(fFile, dipProject);
			setPartName(fFile.getName());
			readSchema();
		} /*else if (input instanceof FileRevisionEditorInput){
			// надо найти схему (при вызове из history)
			// implements org.eclipse.ui.model.IWorkbenchAdapte
			// implements  org.eclipse.ui.IPathEditorInput
		}*/
	}
	
	private void readSchema(){
		try {
			fSchemaReader = new SchemaReader(fFormModel, fFile);
			fFormModel.setFields(fSchemaReader.getFields());
		} catch (ParserConfigurationException | SAXException | IOException e) {
			e.printStackTrace();
		}

	}
	
	@Override
	public void init(IEditorSite site, IEditorInput input) throws PartInitException {
		super.init(site, input);
	}
	
	private boolean isOpenDefaultMessage(){
		return fSchemaReader != null && 
				fSchemaReader.isDefault() &&
				fShowWarningMessage;
	}
	
	private void openDefaultSchemaMessage(){
		if (DipNatureManager.hasNature(fFile.getProject())){
			DefaultSchemaMessage defaultSchemaDialog = new DefaultSchemaMessage(getSite().getShell(), fFile);
			defaultSchemaDialog.open();
		} else {
			WorkbenchUtitlities.openError(Messages.FormsEditor_OpenErrorTitle, fFile.getProject().getName() + Messages.FormsEditor_IsNotDipRpoject
					+ Messages.FormsEditor_CanNotFindSchema);
		}
	}
	
	@Override
	public String getTitle() {
		if (fFile != null){
			return fFile.getName();
		}
		return "title"; //$NON-NLS-1$
	}
	
	private void addTextEntriesPage(){
		fFieldsPage = new FieldsPage(this, Messages.FormsEditor_GuiPageTitle);
		try {
			addPage(fFieldsPage);
		} catch (PartInitException e) {
			e.printStackTrace();
		}
	}
	
	private void addTextEditorPage(){
		fTextEditor = new FormTextEditor(this);
		try {
			int pageNumber = addPage(((IEditorPart) fTextEditor), getEditorInput());
			setPageText(pageNumber, Messages.FormsEditor_RawTextPageTitle);	
		} catch (PartInitException e) {
			e.printStackTrace();
		}
	}
	
	private void addPageListener() {
		addPageChangedListener(new IPageChangedListener() {	

			@Override
			public void pageChanged(PageChangedEvent event) {
				int activePage = getActivePage();
				if (activePage == 0){
					fFieldsPage.update();
				} 
			}					
		});
	}
	
	public void selectText(String text) {
		fFieldsPage.selectText(text);
	}

	//===========================
	// save
	
	@Override
	public void doSave(IProgressMonitor monitor) {
		if (getActivePage() == 0) {		
			fFieldsPage.updateComboItems();
			fFieldsPage.updateTagValues();
		} 
		fFormModel.getPositionModel().createMainTag();
		fTextEditor.doSave(monitor);
		IDipElement element = DipUtilities.findElement(fFile);
		if (element instanceof DipUnit){
			DipUnit unit = (DipUnit) element;
			if (unit.getUnitPresentation().getPresentation() instanceof FormPresentation) {		
				((FormPresentation)unit.getUnitPresentation().getPresentation()).updateFieldsFromFile();
			}
		}
		fFieldsPage.setDirty(false);
		fireDirty();
		fireSave();
	}

	@Override
	public void doSaveAs() {
		fTextEditor.doSaveAs();
		fFieldsPage.setDirty(false);
	}

	@Override
	public boolean isSaveAsAllowed() {
		return fTextEditor.isSaveAsAllowed();
	}
	
	private void fireSave() {
		super.firePropertyChange(MDEditor.SAVE_EVENT);
	}
	
	//=======================
	// dirty
	
	public boolean isFieldsPageDirty() {
		return fFieldsPage.isDirty();
	}
	
	public void checkDirty() {
		getFieldsPage().setDirty(getTextEditor().isDirty());
		fireDirty();
	}
	
	public void fireDirty() {
		super.firePropertyChange(PROP_DIRTY);
	}
	
	//===========================
	// geterrs & setters
	
	public FormModel getFormModel(){
		return fFormModel;
	}
	
	public FormTextEditor getTextEditor() {
		return fTextEditor;	
	}
	
	public FieldsPage getFieldsPage(){
		return fFieldsPage;
	}
	
	public IFile getFile(){
		return fTextEditor.getFile();
	}
	
	public IDipElement getDipElement() {
		return fDipElement;
	}
	
	@Override
	public Composite getContainer() {
		return super.getContainer();
	}

	public FormsEditorActionBarContributor getActionBarContributor() {
		return fActionBarContributor;
	}
	
	public void setActionBarContributor(FormsEditorActionBarContributor contributor) {
		fActionBarContributor = contributor;
	}
	
	public void setUndoRedoProcessing(boolean value) {
		fUndoRedoProcessing = value;
	}
	
	public boolean isUndoRedoProcessing() {
		return fUndoRedoProcessing;
	}
	
	//===================================
	// search
	
	@Override
	public void gotoMarker(IMarker marker) {
		int charStart = marker.getAttribute("charStart", -1); //$NON-NLS-1$
		int charEnd = marker.getAttribute("charEnd", Integer.MAX_VALUE); //$NON-NLS-1$
		Tag tag = fFormModel.findTagByOffset(charStart, charEnd);
		setActivePage(0);
		try {
			String text = fTextEditor.getDocumnet().get(charStart, charEnd - charStart);
			fFieldsPage.select(tag, text);
			// выделяет всегда первое совпадение внутри тега,
			// можно попробовать вычислять offset относительно начала тега
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
	}

}
