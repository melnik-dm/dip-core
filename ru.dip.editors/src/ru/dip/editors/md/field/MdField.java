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
package ru.dip.editors.md.field;

import java.util.Iterator;

import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.text.TextViewerUndoManager;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.hyperlink.IHyperlinkDetector;
import org.eclipse.jface.text.source.AnnotationModel;
import org.eclipse.jface.text.source.IAnnotationAccess;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.texteditor.AnnotationPreference;
import org.eclipse.ui.texteditor.DefaultMarkerAnnotationAccess;
import org.eclipse.ui.texteditor.MarkerAnnotationPreferences;
import org.eclipse.ui.texteditor.SourceViewerDecorationSupport;

import ru.dip.core.model.DipProject;
import ru.dip.core.model.interfaces.IDipUnit;
import ru.dip.core.utilities.autocomplete.IDContentAssistProcessor;
import ru.dip.editors.md.DocumentPartitioner;
import ru.dip.editors.md.IMdEditor;
import ru.dip.editors.md.MDViewerConfiguration;
import ru.dip.editors.md.MarkdownDocument;
import ru.dip.editors.md.model.MdDocumentModel;
import ru.dip.editors.md.partitions.MdPartitionScanner;
import ru.dip.editors.md.partitions.PartitionStyles;
import ru.dip.ui.action.hyperlink.HyperlinkDetector;
import ru.dip.ui.glossary.GlossaryHover;
import ru.dip.ui.variable.VarHover;

public class MdField implements IMdEditor  {
	
	private static final int UNDO_LEVEL = 10;
	
	private SourceViewer fSourceViewer;
	private StyledText fStyledText;
	private int fTextStyle = SWT.NONE;
	private IDocument fDocument;
	private MdDocumentModel fMdModel;
	private MarkdownDocument fMdDocument;
	private MDViewerConfiguration fMdViewerConfiguration;
	
	private DipProject fProject;
	private IDipUnit fDipUnit;
	private String fContent;
	
	public static MdField createWithoutScroll(IDipUnit unit, String content) {
		return new MdField(unit, content);
	}
	
	public static MdField createWithScroll(IDipUnit unit) {
		MdField field = new MdField(unit, null);
		field.fTextStyle = SWT.V_SCROLL;
		return field;
	}
	
	
	protected MdField(IDipUnit unit, String content) {
		fDipUnit = unit;
		fContent = content;
	}
	
	public void createField(Composite parent){
		createSourceViewer(parent);
		createStylledText(fSourceViewer);	
		addAdditionalFeatures();
		createMdDocument();
		addFeatures();
	}
	
	private void createSourceViewer(Composite parent) {
		int style =  SWT.MULTI | SWT.WRAP | SWT.BORDER | fTextStyle;
		fSourceViewer = new SourceViewer(parent, null, null, true, style);
		setDocument(fSourceViewer);
		fSourceViewer.configure(fMdViewerConfiguration = new MDViewerConfiguration(fDipUnit, fDocument));		
		setSourceViewerDecorationSupport(fSourceViewer);
		setPartitioner(fSourceViewer);		
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
		IDocumentPartitioner partitioner = new DocumentPartitioner(new MdPartitionScanner(),
				new String[] { PartitionStyles.COMMENT, 
						PartitionStyles.EMPTY_LINE, 
						PartitionStyles.PARAGRAPH, 
						PartitionStyles.NUMBER_LIST_ITEM,
						PartitionStyles.GRAPHIC_LIST_ITEM,
						PartitionStyles.CODE });
		
		partitioner.connect(fDocument);
		fDocument.setDocumentPartitioner(partitioner);
	}
	
	
	protected void addAdditionalFeatures() {}
	
	private StyledText createStylledText(SourceViewer viewer){
		fStyledText = viewer.getTextWidget();
		GridData gd = new GridData(GridData.FILL_BOTH);
		fStyledText.setLayoutData(gd);		
		fStyledText.setAlwaysShowScrollBars(false);
		fStyledText.setIndent(0);
		return fStyledText;
	}
	
	private void createMdDocument() {
		fMdModel = new MdDocumentModel(fDocument, fProject);
		fMdModel.createModel();		
		fMdDocument = new MarkdownDocument(fStyledText, fDocument, fMdModel);
	}
	
	private void addFeatures() {
		setHyperlinkDetector(fSourceViewer);
		setUndoRedoManager();
		setAutocomplete(fProject);
		if (fDipUnit != null) {
			GlossaryHover.addTextSelectionListener(fStyledText, fDipUnit.resource());
			VarHover.addTextSelectionListener(fStyledText, fDipUnit.resource());
		}
	}
	
	private void setHyperlinkDetector(SourceViewer viewer) {
		IHyperlinkDetector detector = new HyperlinkDetector();		
		viewer.setHyperlinkDetectors(new IHyperlinkDetector[]{detector},  SWT.MOD1);
	}
	
	private void setUndoRedoManager() {
		TextViewerUndoManager undoMgr = new TextViewerUndoManager(UNDO_LEVEL);	
		fSourceViewer.setUndoManager(undoMgr);
		fSourceViewer.getUndoManager().connect(fSourceViewer);
		undoMgr.reset();	
	}

	private void setAutocomplete(DipProject project) {
		ContentAssistant assistant = new ContentAssistant();
		IDContentAssistProcessor processor = new IDContentAssistProcessor(fDipUnit, project);
		assistant.setContentAssistProcessor(processor, IDocument.DEFAULT_CONTENT_TYPE);
		assistant.setContentAssistProcessor(processor, PartitionStyles.PARAGRAPH);
		assistant.setContentAssistProcessor(processor, PartitionStyles.NUMBER_LIST_ITEM);
		assistant.setContentAssistProcessor(processor, PartitionStyles.GRAPHIC_LIST_ITEM);		
		assistant.setContentAssistProcessor(processor, PartitionStyles.COMMENT);
		assistant.setContentAssistProcessor(processor, PartitionStyles.EMPTY_LINE); 
		assistant.setContentAssistProcessor(processor, PartitionStyles.CODE);	
		assistant.install(fSourceViewer);
		
		fSourceViewer.getControl().addKeyListener(new KeyAdapter() {
	        public void keyPressed(KeyEvent e) {

				if (((e.stateMask & SWT.CTRL) == SWT.CTRL)
						&& (e.keyCode == SWT.SPACE )) {
		            assistant.showPossibleCompletions();

				}
	        }
	      });
	}
	
	public void createMdModel() {
		fMdModel.createModel();
	}

	//========================
	// getters 
		
	public StyledText styledText() {
		return fStyledText;
	}
	
	public String content() {
		return fContent;
	}

	public MarkdownDocument mdDocument() {
		return fMdDocument;		
	}
	
	public IDocument document() {
		return fDocument;
	}

	@Override
	public SourceViewer getMDViewer() {
		return fSourceViewer;
	}

	public MDViewerConfiguration getViewerConfiguration() {
		return fMdViewerConfiguration;
	}

}
