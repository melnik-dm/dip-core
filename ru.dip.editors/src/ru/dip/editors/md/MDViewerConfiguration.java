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
package ru.dip.editors.md;

import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.contentassist.IContentAssistant;
import org.eclipse.jface.text.formatter.ContextBasedFormattingStrategy;
import org.eclipse.jface.text.formatter.IContentFormatter;
import org.eclipse.jface.text.presentation.IPresentationReconciler;
import org.eclipse.jface.text.presentation.PresentationReconciler;
import org.eclipse.jface.text.reconciler.IReconciler;
import org.eclipse.jface.text.reconciler.MonoReconciler;
import org.eclipse.jface.text.rules.DefaultDamagerRepairer;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.swt.SWT;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.editors.text.TextSourceViewerConfiguration;
import org.eclipse.ui.texteditor.spelling.SpellingReconcileStrategy;
import org.eclipse.ui.texteditor.spelling.SpellingService;

import ru.dip.core.model.DipProject;
import ru.dip.core.model.interfaces.IDipElement;
import ru.dip.core.utilities.autocomplete.IDContentAssistProcessor;
import ru.dip.core.utilities.md.MDWrapper;
import ru.dip.core.utilities.ui.swt.ColorProvider;
import ru.dip.editors.md.actions.MDContentFormatter;
import ru.dip.editors.md.partitions.ListItemScanner;
import ru.dip.editors.md.partitions.ParagraphScanner;
import ru.dip.editors.md.partitions.PartitionStyles;
import ru.dip.ui.ReqUIPlugin;
import ru.dip.ui.preferences.MdPreferences;

public class MDViewerConfiguration extends TextSourceViewerConfiguration {
		
	private IDipElement fElement;
	private IDocument fDocument;
	private DipProject fDipProject;
	private MDEditor fEditor;
	private MDContentFormatter fContentFormatter;
	
	public MDViewerConfiguration(IDocument document) {
		this(null, null, document);
	}
	
	public MDViewerConfiguration(IDipElement element, IDocument document) {
		this(null, element, document);
	}
	
	public MDViewerConfiguration(MDEditor editor, IDipElement element, IDocument document) {
		super(EditorsUI.getPreferenceStore());
		if (element != null) {
			fDipProject = element.dipProject();
		}
		fDocument = document;
		fElement = element;
		fEditor = editor;
	}
	
	@Override
	public String[] getConfiguredContentTypes(ISourceViewer sourceViewer) {
		return new String[] {
			IDocument.DEFAULT_CONTENT_TYPE,
			PartitionStyles.EMPTY_LINE,
			PartitionStyles.PARAGRAPH,
			PartitionStyles.COMMENT,
			PartitionStyles.NUMBER_LIST_ITEM,
			PartitionStyles.GRAPHIC_LIST_ITEM,
			PartitionStyles.CODE};
	}
	
	@Override
	public IPresentationReconciler getPresentationReconciler(ISourceViewer sourceViewer) {
		PresentationReconciler reconciler = (PresentationReconciler) super.getPresentationReconciler(sourceViewer);		
		NonRuleBasedDamagerRepairer italicRepair = new NonRuleBasedDamagerRepairer(
				new TextAttribute(null,null, SWT.ITALIC));
		reconciler.setDamager(italicRepair, PartitionStyles.EMPTY_LINE);
		reconciler.setRepairer(italicRepair, PartitionStyles.EMPTY_LINE);
		// параграф
		DefaultDamagerRepairer paragraphRepair = new DefaultDamagerRepairer(new ParagraphScanner(fDipProject, fDocument)) {
			@Override
			public IRegion getDamageRegion(ITypedRegion partition, DocumentEvent e, boolean documentPartitioningChanged) {
		    	return new Region(0, fDocument.getLength());
			}
		};
		reconciler.setDamager(paragraphRepair, PartitionStyles.PARAGRAPH);
		reconciler.setRepairer(paragraphRepair, PartitionStyles.PARAGRAPH);
		// комментарий		
		TextAttribute commentAttribute = MdPreferences.commentPreferences().textAttribute();
		NonRuleBasedDamagerRepairer commentRepair = new NonRuleBasedDamagerRepairer(commentAttribute);
		reconciler.setDamager(commentRepair, PartitionStyles.COMMENT);
		reconciler.setRepairer(commentRepair, PartitionStyles.COMMENT);
		// список
		DefaultDamagerRepairer listRepair = new DefaultDamagerRepairer(new ListItemScanner(fDipProject, fDocument)) {				
			@Override
			public IRegion getDamageRegion(ITypedRegion partition, DocumentEvent e, boolean documentPartitioningChanged) {
		    	return new Region(0, fDocument.getLength());
			}
		
		};
		reconciler.setDamager(listRepair, PartitionStyles.NUMBER_LIST_ITEM);
		reconciler.setRepairer(listRepair, PartitionStyles.NUMBER_LIST_ITEM);
		
		reconciler.setDamager(listRepair, PartitionStyles.GRAPHIC_LIST_ITEM);
		reconciler.setRepairer(listRepair, PartitionStyles.GRAPHIC_LIST_ITEM);
		
		// код	
		NonRuleBasedDamagerRepairer codeRepair = new NonRuleBasedDamagerRepairer(
				MdPreferences.codePreferences().textAttribute());
		reconciler.setDamager(codeRepair, PartitionStyles.CODE);
		reconciler.setRepairer(codeRepair, PartitionStyles.CODE);

		NonRuleBasedDamagerRepairer dr = new NonRuleBasedDamagerRepairer(new TextAttribute(ColorProvider.RED));
		reconciler.setDamager(dr, IDocument.DEFAULT_CONTENT_TYPE);
		reconciler.setRepairer(dr, IDocument.DEFAULT_CONTENT_TYPE);
		return reconciler;
	}
	
	@Override
	public IReconciler getReconciler(ISourceViewer sourceViewer) {
		if (fPreferenceStore == null || !fPreferenceStore.getBoolean(SpellingService.PREFERENCE_SPELLING_ENABLED))
			return null;

		SpellingService spellingService= EditorsUI.getSpellingService();
		if (spellingService.getActiveSpellingEngineDescriptor(fPreferenceStore) == null)
			return null;

		SpellingReconcileStrategy strategy= new SpellingReconcileStrategy(sourceViewer, spellingService);
		MonoReconciler reconciler= new MonoReconciler(strategy, false);
		reconciler.setDelay(500);
		return reconciler;
	}
	
	//====================================
	// autoformat
	
	@Override
	public IContentFormatter getContentFormatter(ISourceViewer sourceViewer) {
		fContentFormatter = new MDContentFormatter(fEditor);
		//formatter.enablePartitionAwareFormatting(false);
		TextFormattingStrategy textStrategy = new TextFormattingStrategy();
		fContentFormatter.setFormattingStrategy(textStrategy, IDocument.DEFAULT_CONTENT_TYPE);
		fContentFormatter.setFormattingStrategy(textStrategy, PartitionStyles.PARAGRAPH);
		fContentFormatter.setFormattingStrategy(textStrategy, PartitionStyles.NUMBER_LIST_ITEM);
		fContentFormatter.setFormattingStrategy(textStrategy, PartitionStyles.GRAPHIC_LIST_ITEM);
		fContentFormatter.setFormattingStrategy(textStrategy, PartitionStyles.COMMENT);
		fContentFormatter.setFormattingStrategy(textStrategy, PartitionStyles.CODE);
		fContentFormatter.setFormattingStrategy(textStrategy, PartitionStyles.EMPTY_LINE);		
		ListFormattingStrategy listStrategy = new ListFormattingStrategy();
		fContentFormatter.setFormattingStrategy(listStrategy, PartitionStyles.NUMBER_LIST_ITEM);
		fContentFormatter.setFormattingStrategy(listStrategy, PartitionStyles.GRAPHIC_LIST_ITEM);
		return fContentFormatter;
	}
	
	public void removeCodeBlockFormatStrategy() {
		fContentFormatter.setFormattingStrategy(null, PartitionStyles.CODE);
	}
	
	public void addCodeBlockFormatStrategy() {
		fContentFormatter.setFormattingStrategy(new TextFormattingStrategy(), PartitionStyles.CODE);
	}
	
	public boolean hasBlockFormatStrategy() {
		return fContentFormatter.getFormattingStrategy(PartitionStyles.CODE) != null;
	}
	
	public class TextFormattingStrategy extends ContextBasedFormattingStrategy {

		@Override
		public String format(String content, boolean isLineStart, String indentation, int[] positions) {
			if (MdPreferences.formatBlockCode()) {
				if (!hasBlockFormatStrategy()) {
					addCodeBlockFormatStrategy();
				}
			} else {
				if (hasBlockFormatStrategy()) {
					removeCodeBlockFormatStrategy();
				}
			}
			
			return MDWrapper.parse(content, ReqUIPlugin.getMarkdownMaxLine());
		}
	}
	
	public class ListFormattingStrategy extends ContextBasedFormattingStrategy {

		@Override
		public String format(String content, boolean isLineStart, String indentation, int[] positions) {
			return MDWrapper.parseListItem(content, ReqUIPlugin.getMarkdownMaxLine());
		}
	}

	//====================================
	// auto-completion

	@Override
	public IContentAssistant getContentAssistant(ISourceViewer sourceViewer) {
		if (fDipProject == null) {
			return null;
		}		
		ContentAssistant assistant = new ContentAssistant();
		IDContentAssistProcessor processor = new IDContentAssistProcessor(fElement, fDipProject);
		assistant.setContentAssistProcessor(processor, IDocument.DEFAULT_CONTENT_TYPE);
		assistant.setContentAssistProcessor(processor, PartitionStyles.PARAGRAPH);
		assistant.setContentAssistProcessor(processor, PartitionStyles.NUMBER_LIST_ITEM);
		assistant.setContentAssistProcessor(processor, PartitionStyles.GRAPHIC_LIST_ITEM);		
		assistant.setContentAssistProcessor(processor, PartitionStyles.COMMENT);
		assistant.setContentAssistProcessor(processor, PartitionStyles.EMPTY_LINE); 
		assistant.setContentAssistProcessor(processor, PartitionStyles.CODE);
		return assistant;
	}
	
}
