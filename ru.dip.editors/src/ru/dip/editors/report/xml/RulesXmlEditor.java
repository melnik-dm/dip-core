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
package ru.dip.editors.report.xml;

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.part.FileEditorInput;

import ru.dip.core.report.model.report.ReportEntry;
import ru.dip.core.report.model.report.RulesModel;
import ru.dip.editors.report.ReportEditor;
import ru.dip.ui.utilities.xml.XmlDocumentProvider;
import ru.dip.ui.utilities.xml.XmlViewerConfiguration;


public class RulesXmlEditor extends TextEditor  implements IDocumentListener {

	@SuppressWarnings("unused")
	private ReportEditor fReportEditor;
	private XmlViewerConfiguration fViewerConfiguration;
	private XmlDocumentProvider fProvider;
	private RulesModel fRulesModel;
	private IDocument fDocument;

	
	public RulesXmlEditor(FormEditor reportEditor) {
		if (reportEditor instanceof ReportEditor){
			fReportEditor = (ReportEditor) reportEditor;
		}
		fProvider = new XmlDocumentProvider();
		setDocumentProvider(fProvider);
		fViewerConfiguration = new XmlViewerConfiguration();
		setSourceViewerConfiguration(fViewerConfiguration);
	}
	
	@Override
	protected void doSetInput(IEditorInput input) throws CoreException {
		super.doSetInput(input);
		if (input instanceof FileEditorInput){
			fDocument = getDocumentProvider().getDocument(input);
			fDocument.addDocumentListener(this);
			fRulesModel = new RulesModel(fDocument);
			fRulesModel.createModel();
		}
	}

	public IAnnotationModel getAnnotationModel() {
		return getSourceViewer().getAnnotationModel();
	}
	
	@Override
	public void documentAboutToBeChanged(DocumentEvent event) {
		
	}

	@Override
	public void documentChanged(DocumentEvent event) {
		fRulesModel.createModel();
	}

	public void createModel(){
		fRulesModel.createModel();
	}
	
	
	public IDocument getDocumnet(){
		return fDocument;
	}
	
	public List<ReportEntry> getEntries(){
		return fRulesModel.getEntries();
	}
	
	public String getDescription(){
		return fRulesModel.getDescription();
	}
}
