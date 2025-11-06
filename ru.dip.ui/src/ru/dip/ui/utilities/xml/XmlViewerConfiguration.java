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
package ru.dip.ui.utilities.xml;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.presentation.IPresentationReconciler;
import org.eclipse.jface.text.presentation.PresentationReconciler;
import org.eclipse.jface.text.reconciler.IReconciler;
import org.eclipse.jface.text.reconciler.MonoReconciler;
import org.eclipse.jface.text.rules.DefaultDamagerRepairer;
import org.eclipse.jface.text.rules.ITokenScanner;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.editors.text.TextSourceViewerConfiguration;
import org.eclipse.ui.texteditor.spelling.SpellingReconcileStrategy;
import org.eclipse.ui.texteditor.spelling.SpellingService;

import ru.dip.core.form.partitioner.IPartitions;
import ru.dip.ui.preferences.ReqEditorSettings;
import ru.dip.ui.utilities.xml.color.NonRuleBasedDamagerRepairer;
import ru.dip.ui.utilities.xml.color.ProcInstrScanner;
import ru.dip.ui.utilities.xml.color.TagScanner;


public class XmlViewerConfiguration extends TextSourceViewerConfiguration {
	
	private TagScanner fTagScanner;
	private ProcInstrScanner fScanner;
	
	public XmlViewerConfiguration(){
		
	}
	
	public XmlViewerConfiguration(IPreferenceStore preferenceStore) {
		super(preferenceStore);
	}

	// =========================================================
	// методы для подсветки синтаксиса (назначаем цвета)
	@Override
	public IPresentationReconciler getPresentationReconciler(ISourceViewer sourceViewer) {		
		//PresentationReconciler reconciler = new PresentationReconciler();
		PresentationReconciler reconciler = (PresentationReconciler) super.getPresentationReconciler(sourceViewer);		
		// Сначала запускаем XMLTagScanner - определяет тег, пустой тег,
		DefaultDamagerRepairer dr = new DefaultDamagerRepairer(getTagScanner());
		reconciler.setDamager(dr, IPartitions.TAG);
		reconciler.setRepairer(dr, IPartitions.TAG);
		reconciler.setDamager(dr, IPartitions.INCOMPLETETAG);
		reconciler.setDamager(dr, IPartitions.ENDTAG);
		reconciler.setRepairer(dr, IPartitions.ENDTAG);
		reconciler.setDamager(dr, IPartitions.EMPTYTAG);
		reconciler.setRepairer(dr, IPartitions.EMPTYTAG);
		// определяет <? ?>
		dr = new DefaultDamagerRepairer(getProcInstrScanner());
		reconciler.setDamager(dr, IPartitions.TEXT);
		reconciler.setRepairer(dr, IPartitions.TEXT);
		// комментарии
		NonRuleBasedDamagerRepairer commentRepairer = new NonRuleBasedDamagerRepairer(
				new TextAttribute(ReqEditorSettings.getCommentColor()));
		/*NonRuleBasedDamagerRepairer commentRepairer = new NonRuleBasedDamagerRepairer(
				new TextAttribute(new Color(Display.getCurrent(), PlatformUI.getWorkbench().getThemeManager()
						.getCurrentTheme().getColorRegistry().getRGB(IColorConstants.XML_COMMENT))));*/
		
		
		reconciler.setDamager(commentRepairer, IPartitions.COMMENT);
		reconciler.setRepairer(commentRepairer, IPartitions.COMMENT);
		// декларации
		NonRuleBasedDamagerRepairer declarationRepairer = new NonRuleBasedDamagerRepairer(
				new TextAttribute(ReqEditorSettings.getDeclarationColor()));
		/*NonRuleBasedDamagerRepairer declarationRepairer = new NonRuleBasedDamagerRepairer(
				new TextAttribute(new Color(Display.getCurrent(), PlatformUI.getWorkbench().getThemeManager()
						.getCurrentTheme().getColorRegistry().getRGB(IColorConstants.DECLARATION))));*/
		reconciler.setDamager(declarationRepairer, IPartitions.DECLARATION);
		reconciler.setRepairer(declarationRepairer, IPartitions.DECLARATION);
		// проц. инструкции
		NonRuleBasedDamagerRepairer procInnstrRepairer = new NonRuleBasedDamagerRepairer(
				new TextAttribute(ReqEditorSettings.getProcessorInstructionColor()));
		/*NonRuleBasedDamagerRepairer procInnstrRepairer = new NonRuleBasedDamagerRepairer(
				new TextAttribute(new Color(Display.getCurrent(), PlatformUI.getWorkbench().getThemeManager()
						.getCurrentTheme().getColorRegistry().getRGB(IColorConstants.PROC_INSTR))));*/
		reconciler.setDamager(procInnstrRepairer, IPartitions.PI);
		reconciler.setRepairer(procInnstrRepairer, IPartitions.PI);
		// cdata
		NonRuleBasedDamagerRepairer cdataRepairer = new NonRuleBasedDamagerRepairer(
				new TextAttribute(ReqEditorSettings.getCdataColor()));
		/*NonRuleBasedDamagerRepairer cdataRepairer = new NonRuleBasedDamagerRepairer(
				new TextAttribute(new Color(Display.getCurrent(), PlatformUI.getWorkbench().getThemeManager()
						.getCurrentTheme().getColorRegistry().getRGB(IColorConstants.CDATA))));*/
		reconciler.setDamager(cdataRepairer, IPartitions.CDATA);
		reconciler.setRepairer(cdataRepairer, IPartitions.CDATA);
		return reconciler;
	}
	
	/**
	 *  для поиска инструкций типа: <? ?> - внутри текста
	 */
	protected ITokenScanner getProcInstrScanner() {
		if (fScanner == null) {
			fScanner = new ProcInstrScanner();
			fScanner.setDefaultReturnToken(
					new Token(new TextAttribute(ReqEditorSettings.getDefaultColor())));
			/*fScanner.setDefaultReturnToken(
					new Token(new TextAttribute(new Color(Display.getCurrent(), PlatformUI.getWorkbench()
							.getThemeManager().getCurrentTheme().getColorRegistry().getRGB(IColorConstants.DEFAULT)))));*/
		}
		return fScanner;
	}

	/**
	 *  для опредления цветов внутри тега
	 */
	protected TagScanner getTagScanner() {
		if (fTagScanner == null) {
			fTagScanner = new TagScanner();
			fTagScanner.setDefaultReturnToken(
					new Token(new TextAttribute(ReqEditorSettings.getTagColor())));
			/*fTagScanner.setDefaultReturnToken(
					new Token(new TextAttribute(new Color(Display.getCurrent(), PlatformUI.getWorkbench()
							.getThemeManager().getCurrentTheme().getColorRegistry().getRGB(IColorConstants.TAG)))));*/

		}
		return fTagScanner;
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

}
