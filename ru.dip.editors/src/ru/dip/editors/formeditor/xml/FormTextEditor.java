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
package ru.dip.editors.formeditor.xml;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.part.FileEditorInput;

import ru.dip.editors.formeditor.FormsEditor;
import ru.dip.editors.formeditor.model.PositionModel;
import ru.dip.editors.formeditor.model.FormModel;
import ru.dip.ui.utilities.xml.XmlDocumentProvider;
import ru.dip.ui.utilities.xml.XmlViewerConfiguration;

public class FormTextEditor extends TextEditor  implements IDocumentListener  {
	
	private XmlViewerConfiguration fConfiguration;
	private XmlDocumentProvider fProvider;
	private FormsEditor fFormsEditor;
	private FormModel fElementModel;
	private PositionModel fPositionModel;
	private IDocument fDocument;
	private IFile fFile;
	
	// for save-dirty
	private long fLastTimeStamp = 1;
	private long fCurrentLastTimeStamp = 1;	
	
	public FormTextEditor(FormsEditor editor) {
		fFormsEditor = editor;
		fElementModel = fFormsEditor.getFormModel();
		fConfiguration = new XmlViewerConfiguration(EditorsUI.getPreferenceStore());
		fProvider = new XmlDocumentProvider();
		setSourceViewerConfiguration(fConfiguration);	
		setDocumentProvider(fProvider);
	}
	
	@Override
	protected void doSetInput(IEditorInput input) throws CoreException {
		super.doSetInput(input);
		if (input instanceof FileEditorInput){
			FileEditorInput fileEditorInput = (FileEditorInput) input;
			fFile = fileEditorInput.getFile();
			fDocument = getDocumentProvider().getDocument(input);
			fDocument.addDocumentListener(this);
			fPositionModel = new PositionModel(this);
			fPositionModel.createModel();
		}
	}
	
	public IAnnotationModel getAnnotationModel() {
		return getSourceViewer().getAnnotationModel();
	}
	
	@Override
	public boolean isDirty() {				
		return fLastTimeStamp != fCurrentLastTimeStamp;
	}
	
	@Override
	public void doSave(IProgressMonitor progressMonitor) {
		fLastTimeStamp = fCurrentLastTimeStamp;
		super.doSave(progressMonitor);
	}
	
	//==============================
	// Document listener

	@Override
	public void documentAboutToBeChanged(DocumentEvent event) {
	
	}

	@Override
	public void documentChanged(DocumentEvent event) {
		fPositionModel.createModel();
		fCurrentLastTimeStamp = event.getModificationStamp();
		fFormsEditor.fireDirty();
	}
	
	//=======================================
	// getters & setters
	
	public IDocument getDocumnet(){
		return fDocument;
	}
	
	public PositionModel getPositionModel(){
		return fPositionModel;
	}

	public FormModel getElementModel() {
		return fElementModel;
	}
	
	public FormsEditor getFormsEditor(){
		return fFormsEditor;
	}
	
	public IFile getFile(){
		return fFile;
	}

}
