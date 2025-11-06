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
package ru.dip.ui.properties;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.source.AnnotationModel;
import org.eclipse.jface.text.source.IAnnotationAccess;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.texteditor.AnnotationPreference;
import org.eclipse.ui.texteditor.DefaultMarkerAnnotationAccess;
import org.eclipse.ui.texteditor.MarkerAnnotationPreferences;
import org.eclipse.ui.texteditor.SourceViewerDecorationSupport;

import ru.dip.core.form.partitioner.DocumentPartitioner;
import ru.dip.core.form.partitioner.PartitionScanner;
import ru.dip.ui.Messages;
import ru.dip.ui.utilities.xml.XmlViewerConfiguration;

/**
 * Просмотр схемы (в данный момент не используется)
 */
public class ShowSchemaDialog extends Dialog {

	private StyledText fStyledText;
	private SourceViewer fSourceViewer;
	private IDocument fDocument;
	private String fContent;

	
	public ShowSchemaDialog(Shell parentShell, Path path) {
		super(parentShell);
		fContent = getFileContent(path);
	}
	
	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(Messages.ShowSchemaDialog_Title);
	}
	
	@Override
	protected Point getInitialSize() {		
		Point p =  super.getInitialSize();
		p.x += 80;		
		return p;
	}
	
	
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout());
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		createText(composite);
		fStyledText.setLayoutData(new GridData(GridData.FILL_BOTH));
		fStyledText.setText(fContent);
		fStyledText.setEditable(false);

		return composite;
	}
	
	
	private void createText(Composite parent){
		fSourceViewer = new SourceViewer(parent, null, null, true, SWT.MULTI | SWT.V_SCROLL | SWT.WRAP);
		fStyledText = createStylledText(fSourceViewer);
		setSourceConfiguration(fSourceViewer);		
		setDocument(fSourceViewer);
		setSourceViewerDecorationSupport(fSourceViewer);
		setPartitioner(fSourceViewer);				
		fSourceViewer.getTextWidget().setIndent(0);					
	}
	
	private StyledText createStylledText(SourceViewer viewer){
		StyledText styledText = viewer.getTextWidget();
		styledText.setLayoutData(new GridData(GridData.FILL_BOTH));
		return styledText;
	}
	
	private void setSourceConfiguration(SourceViewer viewer){
		XmlViewerConfiguration config = new XmlViewerConfiguration(EditorsUI.getPreferenceStore());
		viewer.configure(config);
	}
	
	private void setDocument(SourceViewer viewer){
		fDocument = new Document();
		viewer.setDocument(fDocument, new AnnotationModel());
	}
	
	private void setSourceViewerDecorationSupport(SourceViewer viewer){
		IAnnotationAccess annotationAccess = new DefaultMarkerAnnotationAccess();
		final SourceViewerDecorationSupport support = new SourceViewerDecorationSupport(viewer, null, annotationAccess, EditorsUI.getSharedTextColors());
		for (Iterator<?> e = new MarkerAnnotationPreferences().getAnnotationPreferences().iterator(); e.hasNext();) {
			AnnotationPreference preference = (AnnotationPreference) e.next();		
			support.setAnnotationPreference(preference);
		}
		support.install(EditorsUI.getPreferenceStore());
		viewer.showAnnotations(true);
	}
	
	private void setPartitioner(SourceViewer viewer){
		DocumentPartitioner partitioner = new DocumentPartitioner(new PartitionScanner(), PartitionScanner.CONTENT_TYPES);
		partitioner.connect(fDocument);
		fDocument.setDocumentPartitioner(partitioner);
	}
	
	private String getFileContent(Path path){
		try {
			return new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

}
