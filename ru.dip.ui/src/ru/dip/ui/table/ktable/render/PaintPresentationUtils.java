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
package ru.dip.ui.table.ktable.render;

import static ru.dip.ui.table.ktable.model.ContentProvider.PRESENTATION_CONTENT_PROVIDER;

import java.util.List;
import java.util.Map;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.TextLayout;

import ru.dip.core.form.model.TextField;
import ru.dip.core.model.finder.IFinder;
import ru.dip.core.model.interfaces.IDipDocumentElement;
import ru.dip.core.model.interfaces.IGlossaryPointsHolder;
import ru.dip.core.model.interfaces.ITextPresentation;
import ru.dip.core.model.interfaces.IUnitPresentation;
import ru.dip.core.unit.ChangeLogPresentation;
import ru.dip.core.unit.CsvUnitPresentation;
import ru.dip.core.unit.DiaPresentation;
import ru.dip.core.unit.GlossaryPresentation;
import ru.dip.core.unit.HtmlUnitPresentation;
import ru.dip.core.unit.ImagePresentation;
import ru.dip.core.unit.ReportRefPresentation;
import ru.dip.core.unit.TablePresentation;
import ru.dip.core.unit.TextPresentation;
import ru.dip.core.unit.TocRefPresentation;
import ru.dip.core.unit.ZoomImagePresentation;
import ru.dip.core.unit.form.AbstractFormField;
import ru.dip.core.unit.form.FormField;
import ru.dip.core.unit.form.FormPresentation;
import ru.dip.core.unit.form.FormSettings;
import ru.dip.core.unit.form.IFormFields;
import ru.dip.core.unit.form.IFormSettings;
import ru.dip.core.unit.md.MarkDownPresentation;
import ru.dip.core.unit.md.MarkdownSettings;
import ru.dip.core.unit.md.SubMarkdownPresentation;
import ru.dip.core.utilities.TagStringUtilities;
import ru.dip.core.utilities.ui.gc.GCUtils;
import ru.dip.core.utilities.ui.swt.FontDimension;
import ru.dip.ui.Messages;
import ru.dip.ui.preferences.MdPreferences;
import ru.dip.ui.table.ktable.model.ContentProvider;
import ru.dip.ui.table.ktable.model.IContentContainer;
import ru.dip.ui.table.ktable.model.IDipTableElement;
import ru.dip.ui.table.ktable.model.IFixedContentChecker;
import ru.dip.ui.table.ktable.model.ITableCompositeSetting;
import ru.dip.ui.table.table.TableSettings;
import ru.dip.ui.utilities.image.ImageProvider;

public class PaintPresentationUtils {
		
	private static final ContentProvider contentProvider = PRESENTATION_CONTENT_PROVIDER;
	
	public static boolean hasFontPresentation(IDipTableElement element) {				
		if (element.isPresentation()) {
			IUnitPresentation presentation = (IUnitPresentation) element.dipDocElement();
			TablePresentation tablePresentation = presentation.getPresentation();
			return tablePresentation instanceof TextPresentation 
					|| tablePresentation instanceof MarkDownPresentation
					|| tablePresentation instanceof FormPresentation
					|| tablePresentation instanceof ChangeLogPresentation
					|| tablePresentation instanceof TocRefPresentation
					|| tablePresentation instanceof GlossaryPresentation;
		} else {
			return true;
		}
	}
	
	//==================
	// html
	
	public static void updateHtmlPresentation(int columnWidth, HtmlUnitPresentation htmlPresentation, IFinder finder) {
		if (PaintPresentationUtils.isFindCurrentDipDocElement(htmlPresentation, finder)) {
			int currentNumber = finder.getCurrentNumber();
			htmlPresentation.setNewCurrentPointNumber(currentNumber);
		}
		PaintPresentationUtils.updateImagePresentation(columnWidth, htmlPresentation);
	}
	
	//=================================
	// image presentation
	
	public static void updateImagePresentation(int columnWidth, ZoomImagePresentation imagePresentation) {	
		int width = getColumnWidth(columnWidth) - TableSettings.versionMarginLeft() - TableSettings.versionMarginRight() - 5;
		imagePresentation.checkUpdate();
		imagePresentation.trimImage(width);
	}
	
	//=================================
	// csv presentation
	
	public static void updateCsvPresentation(int columnWidth,CsvUnitPresentation cvsPresentation) {
		int width = columnWidth - TableSettings.versionMarginLeft() - TableSettings.versionMarginRight() - 5;
		boolean wrapContent = TableSettings.isCsvColumnWidthByContent();
		cvsPresentation.checkUpdate();
		cvsPresentation.trimImage(width, wrapContent);
	}
	
	//===========================
	// markdown
	
	public static void updateMarkdownPresentation(int columnWidth, 
			MarkDownPresentation mdPresentation, 
			IContentContainer element,
			IFixedContentChecker fixedChecker,
			ITableCompositeSetting tableSettings) {		
		int width = getColumnWidth(columnWidth);
		int lineLength =  TableSettings.presentationFontDimension().getStringLength(width);
		MarkdownSettings settings = TableSettings.markdownSettings(tableSettings.isShowMdComment());
		String mdText = mdPresentation.parse(settings, lineLength);
		updateTextPresentation(width, mdText, element, mdPresentation, fixedChecker, tableSettings);
	}
	
	public static void updateSubMarkdownPresentation(
			int columnWidth, 
			SubMarkdownPresentation subMdPresentation, 
			IContentContainer element,
			IFixedContentChecker fixedChecker,
			ITableCompositeSetting tableSettings) {
		int width = getColumnWidth(columnWidth);		
		int lineLength =  TableSettings.presentationFontDimension().getStringLength(width);
		MarkdownSettings settings = TableSettings.markdownSettings(tableSettings.isShowMdComment());
		String mdText = subMdPresentation.parse(settings, lineLength);
		String number = subMdPresentation.getNumber();
		if (number != null) {
			String indent = TagStringUtilities.getIndent(mdText);			
			mdText = indent + number + mdText.substring(indent.length());
		}		
		updateTextPresentation(width, mdText, element, subMdPresentation, fixedChecker, tableSettings);
	}
	
	
	//=================================
	// text presentation
	
	public static void updateTextPresentation(int columnWidth, 
			TextPresentation textPresentation, 
			IContentContainer element, 
			IFixedContentChecker fixedChecker, 
			ITableCompositeSetting settings) {
		int width = getColumnWidth(columnWidth);
		if (!textPresentation.checkUpdate()) {
			textPresentation.read();
		}		
		String text = textPresentation.getText();
		updateTextPresentation(width, text, element, textPresentation, fixedChecker, settings);
	}	
	
	public static void updateTextPresentation(int columnWidth, 
			String text, 
			IContentContainer element, 
			ITextPresentation textPresentation, IFixedContentChecker fixedChecker, ITableCompositeSetting settings) {
		boolean fixedContent = fixedChecker.isFixedContent(element, textPresentation.getUnit());
		PaintPresentationUtils.updateTextPresentation(
				columnWidth, 
				text, 
				element, 
				textPresentation,
				fixedContent,
				settings);
	}
	
	public static void updateTextPresentation(
			int columnWidth, 
			String text, 
			IContentContainer element, 
			ITextPresentation textPresentation,
			boolean isFixedContent,
			ITableCompositeSetting settings) {
		text = FontDimension.getWrapText(text, TableSettings.presentationFontDimension().getStringLength(columnWidth));

		if (isFixedContent){
			text = getFixedText(text);
		}
		if (settings.isHighlightGloss()) {
			textPresentation.getGlossaryPoints().findGlossaryWords(text);
		}
		if (settings.isCheckSpellingEnable()) {
			textPresentation.findErrorWords(text);
		}
		
		if (settings.isFindMode() && textPresentation.hasFindResult()) {
			textPresentation.updateFindedPoints(text);
		}
		
		contentProvider.setFont(element, TableSettings.presentationFont());
		contentProvider.setText(element, text);
	}
	
	public static void updateSimpleTextPresentation(int columnWidth, TablePresentation presentation, IContentContainer element) {				
		String text = presentation.getText();
		updateSimpleTextPresentation(columnWidth, text, element);
	}
	
	public static void updateSimpleTextPresentation(int columnWidth, String text, IContentContainer element) {				
		int width = getColumnWidth(columnWidth);
		if (width <= 0) {
			return;
		}
		text = FontDimension.getWrapText(text, TableSettings.presentationFontDimension().getStringLength(width));	
		contentProvider.setFont(element, TableSettings.presentationFont());
		contentProvider.setText(element, text);
	}
	
	
	public static String getFixedText(String text){	
		if (text == null) {
			text = ""; //$NON-NLS-1$
		}
		
		String[] lines = text.split(TagStringUtilities.LINE_SPLIT_REGEX);
		if (lines.length <= 3){
			return text;
		}
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < 3; i++){
			builder.append(lines[i]);
			builder.append("\n"); //$NON-NLS-1$
		}
		builder.append("..."); //$NON-NLS-1$
		return builder.toString();
	}

	//============================
	// FormPresentation
	
	public static void updateFormPresentation(int columnWidth, 
			FormPresentation reqPresentation, 
			IContentContainer element, 
			IFixedContentChecker fixedChecker, 
			ITableCompositeSetting tableSetting, 
			IFormSettings formSettings) {
		int width = getColumnWidth(columnWidth);
		String text = computeFormPresentation(element, reqPresentation, width, fixedChecker, tableSetting, formSettings);
		updateTextPresentation(width, text, element, reqPresentation, fixedChecker, tableSetting);
	}
	
	public static String computeFormPresentation(IContentContainer element, 
			FormPresentation reqPresentation, 
			int width, 
			IFixedContentChecker fixedChecker, 
			ITableCompositeSetting tableSetting, 
			IFormSettings formSettings) {
		boolean isFixedContent = fixedChecker.isFixedContent(element, reqPresentation.getDipUnit());
		FormSettings settings = new FormSettings(formSettings, isFixedContent);	
		int lineLength =  TableSettings.presentationFontDimension().getStringLength(width);
		MarkdownSettings mdSettings = TableSettings.markdownSettings(tableSetting.isShowMdComment());
		return reqPresentation.tablePresentation(settings, mdSettings, lineLength);
	}
	
	//==========================
	// DIA
	
	public static void updateDiaPresentation(int columnWidth, DiaPresentation diaPresentation, IContentContainer element) {
		int width = getColumnWidth(columnWidth);
		diaPresentation.checkUpdate();
		diaPresentation.trimImage(width);
		if (diaPresentation.isDiaReadError()) {
			String text = DiaPresentation.DIA_NOT_INSTALLED;
			updateSimpleTextPresentation(width, text, element);	
		}		
	}

	//==========================
	// other
	
	public static void updateReportRefPresentation(int columnWidth, ReportRefPresentation reportPresentation, IContentContainer element, IFixedContentChecker fixedChecker) {				
		reportPresentation.checkUpdate();
		int width = getColumnWidth(columnWidth);
		String text = reportPresentation.createText(TableSettings.presentationFontDimension().getStringLength(width));				
		if (fixedChecker.isFixedContent(element, reportPresentation)) {
			text = reportPresentation.getFixedText();
		}
		PaintPresentationUtils.updateSimpleTextPresentation(width, text, element);
	}		
	
	public static void updateGlossaryRefPresentation(int columnWidth, GlossaryPresentation reportPresentation, IContentContainer element, IFixedContentChecker fixedChecker) {				
		String text = reportPresentation.getText();
		if (fixedChecker.isFixedContent(element, reportPresentation)) {
			text = reportPresentation.getFixedText();
		}
		PaintPresentationUtils.updateSimpleTextPresentation(columnWidth, text, element);
	}
	
	public static void updateNullPresentation(int columnWidth, IContentContainer element) {
		String text = Messages.PresentationPainter_FilePresentationNotSupport;
		int width = getColumnWidth(columnWidth);
		if (width <= 0) {
			return;
		}
		text = FontDimension.getWrapText(text, TableSettings.presentationFontDimension().getStringLength(width));	
		contentProvider.setImage(element, ImageProvider.EMPTY_IMAGE);
		contentProvider.setFont(element, TableSettings.presentationFont());
	}
	
	//============================
	// FormField
	
	public static void updateFormField(
			int columnWidth, 
			AbstractFormField formField, 
			IContentContainer element,
			IFixedContentChecker fixedChecker,
			IFormSettings formSetting,
			ITableCompositeSetting tableSetting) {
		boolean isFixedContent = fixedChecker.isFixedContent(element, formField.getDipUnit());
		PaintPresentationUtils.updateFormField( 
				columnWidth, formField, element, 
				isFixedContent, 
				formSetting, 
				tableSetting);
	}
	
	
	public static void updateFormField(
			int columnWidth, 
			AbstractFormField formField, 
			IContentContainer element,
			boolean isFixedContent, 
			IFormSettings formSettings, 
			ITableCompositeSetting tableSettings) {
		IFormSettings settings = new FormSettings(formSettings, isFixedContent);
		int width = getColumnWidth(columnWidth);
		if (width <= 0) {
			return;
		}
		int lineLength =  TableSettings.presentationFontDimension().getStringLength(width);	
		MarkdownSettings mdSettings = TableSettings.markdownSettings(tableSettings.isShowMdComment());
		String text =  formField.tablePresentation(settings, mdSettings, lineLength, ((IDipTableElement) element).isFirst(), ((IDipTableElement) element).isLast());		
		text = FontDimension.getWrapText(text, TableSettings.presentationFontDimension().getStringLength(width));
		// если поле текстовое?
		if (isFixedContent
				&& formField instanceof FormField 
				&& ((FormField)formField).getField() instanceof TextField){
			text = PaintPresentationUtils.getFixedText(text);
		}
		// find points
		if (tableSettings.isFindMode() && formField.hasFindResult()) {
			formField.updateFindedPoints(text);
		}

		if (isFixedContent){
			text = PaintPresentationUtils.getFixedText(text);
		} 	
		if (tableSettings.isHighlightGloss()) {
			formField.getGlossaryPoints().findGlossaryWords(text);
		}
		if (tableSettings.isCheckSpellingEnable()) {
			formField.findErrorWords(text);
		}	
		contentProvider.setFont(element, TableSettings.presentationFont());
		contentProvider.setText(element, text);
	}
	
	//=======================================
	// measure
	
	public static int measureDiaPresentation(IContentContainer element, 
			DiaPresentation diaPresentation,
			IFixedContentChecker fixedChecker){
		if (diaPresentation.isDiaReadError()) {
			String itemText = contentProvider.getText(element);
			int stringCounter = 0;
			stringCounter = itemText.split(TagStringUtilities.LINE_SPLIT_REGEX).length;					
			int height = TableSettings.presentationFontDimension().charHeight() * stringCounter;
			height += TableSettings.versionMarginTop() + TableSettings.versionMarginBottom();			
			return height;
		}
		
		return measureZomPresentation(element, diaPresentation, fixedChecker);
	}
	/*
	 * Определяет высоту картинки:
	 *   fixedContentImage - для fixed-режима
	 *   getZoomImage - для обычного
	 */
	public static  int measureZomPresentation(IContentContainer element, 
			ZoomImagePresentation zoomImagePresentation,
			IFixedContentChecker fixedChecker){
		if (fixedChecker.isFixedContent(element, zoomImagePresentation.getUnit())){
			Image fixedContentImage = getFixedModeImage(zoomImagePresentation);
			return  getImageHeight(fixedContentImage);
		} else {
			Image image = zoomImagePresentation.getZoomImage();
			return  getImageHeight(image);
		}
	}
	
	public static  int getImageHeight(Image image) {
		return image == null ? 0 : image.getBounds().height 
				+ TableSettings.versionMarginTop() + TableSettings.versionMarginBottom();
	}
	
	public static  int measureSimpleTextPresentation(IContentContainer element) {
		return measureSimpleTextPresentationWithoutMargins(element)
				+ TableSettings.versionMarginTop()
				+ TableSettings.versionMarginBottom();	
	}
	
	public static  int measureSimpleTextPresentationWithoutMargins(IContentContainer element) {
		final String itemText = contentProvider.getText(element); 		
		return TableSettings.presentationFontDimension().getTextHeight(itemText);
	}
	
	//================================
	// paint
	
	public static void paintTextPresentation(int width,GC gc, Rectangle rect, 
			TextPresentation textPresentation, 
			IContentContainer element,
			ITableCompositeSetting tableSetting,
			IFinder finder) {	
		String text = contentProvider.getText(element);
	    final TextLayout layout = createTextLayout(text, width, gc);
    	applyGlossaryStyles(layout, textPresentation, tableSetting);
    	applyErrorStyles(layout, textPresentation.errorsPoints(), tableSetting);
    	
    	Map<List<Point>, Color> specialPoints = textPresentation.getSpecialPoints();
    	if (specialPoints != null) {
    		specialPoints.entrySet().forEach(e -> GCUtils.applyColorStyles(layout, e.getKey(), null, e.getValue()));
    	}
    	
    	applyFindedStyles(layout, textPresentation.getFindedPoints(), textPresentation, tableSetting, finder); 	
	    drawTextAndDispose(layout, rect, gc);
	}
	
	public static void paintMarkdownPresentation(int width, GC gc, Rectangle rect, 
			MarkDownPresentation mdPresentation, 
			IContentContainer element,
			IFixedContentChecker fixedChecker,
			ITableCompositeSetting tableSetting,
			IFinder finder){
		String text = contentProvider.getText(element); 
		if (text == null) {
			updateMarkdownPresentation(width, mdPresentation, element, fixedChecker, tableSetting);
			text = contentProvider.getText(element);
		}
	    final TextLayout layout = createTextLayout(text, width, gc);
 	    
	    GCUtils.applyFontStyle(layout, mdPresentation.boldPoints(), TableSettings.boldMDPresentationFont());	    
	    GCUtils.applyFontStyle(layout, mdPresentation.italicPoints(), TableSettings.italicPresentationFont());
	    GCUtils.applyFontStyle(layout, mdPresentation.boldItalicPoints(), TableSettings.boldItalicPresentationFont());

    	applyGlossaryStyles(layout, mdPresentation, tableSetting);
	    
    	GCUtils.applyColorStyles(layout, mdPresentation.fencedCodePoints(), null, MdPreferences.codePreferences().color());
    	GCUtils.applyColorStyles(layout, mdPresentation.codePoints(), null, MdPreferences.codePreferences().color());
    	if (tableSetting.isShowMdComment()) {
    		GCUtils.applyColorStyles(layout, mdPresentation.commentPoints(), null, MdPreferences.commentPreferences().color());
    	}
    	
    	applyErrorStyles(layout, mdPresentation.errorsPoints(), tableSetting);
    	applyFindedStyles(layout, mdPresentation.getFindedPoints(), mdPresentation, tableSetting, finder);
	    drawTextAndDispose(layout, rect, gc);
	}
	
	public static void paintFormPresentation(int width, GC gc, Rectangle rect,  
			FormPresentation formPresentation, 
			IContentContainer element, 
			ITableCompositeSetting tableSetting,
			IFinder finder){
		String text = contentProvider.getText(element);
	    final TextLayout layout = createTextLayout(text, width, gc);

	    // bold title
	    if (tableSetting.isShowFormNumeration() && formPresentation.titleBoldPoint() != null) {
		    Point p = formPresentation.titleBoldPoint();		    
		    layout.setStyle(TableSettings.boldMdPresentationStyle(), p.x, p.y - 1);
	    }
	    // select headers
	    for (Point p: formPresentation.getFieldTitlePoints()){			    
	    	layout.setStyle(GCUtils.FORM_HEADER_STYLE, p.x, p.y - 1);
	    }
	    
	    // text styles (markdown)
	    GCUtils.applyFontStyle(layout, formPresentation.boldPoints(), TableSettings.boldMDPresentationFont());	    
	    GCUtils.applyFontStyle(layout, formPresentation.italicPoints(), TableSettings.italicPresentationFont());
	    GCUtils.applyFontStyle(layout, formPresentation.boldItalicPoints(), TableSettings.boldItalicPresentationFont());
	    GCUtils.applyColorStyles(layout, formPresentation.fencedCodePoints(), null, MdPreferences.codePreferences().color());
	    GCUtils.applyColorStyles(layout, formPresentation.codePoints(), null, MdPreferences.codePreferences().color());
    	if (tableSetting.isShowMdComment()) {
    		GCUtils.applyColorStyles(layout, formPresentation.commentPoints(), null, MdPreferences.commentPreferences().color());
    	}
	      
    	applyGlossaryStyles(layout, formPresentation, tableSetting);
    	applyErrorStyles(layout, formPresentation.errorsPoints(), tableSetting);   
    	applyFindedStyles(layout, formPresentation.getFindedPoints(), formPresentation, tableSetting, finder);    
	    drawTextAndDispose(layout, rect, gc);   
	}
	
	public static void paintZoomImagePresentation(int width, GC gc, Rectangle rect, ZoomImagePresentation zoomPresentation, IContentContainer element, IFixedContentChecker fixedChecker) {
		Image image = zoomPresentation.getZoomImage(width);
		if (image == null) {
			drawSimpleText(zoomPresentation.getText(), width, rect, gc);
			return;
		}
		if (fixedChecker.isFixedContent(element, zoomPresentation.getUnit())){
			drawImage(gc, getFixedModeImage(zoomPresentation), rect);			
		} else { 	
			drawImage(gc, image, rect);
		}
	}
	
	public static void paintReportRefPresentation(int width, GC gc, Rectangle rect,
			ReportRefPresentation reportPresentation, IContentContainer element, IFixedContentChecker fixedChecker){
		final String text = contentProvider.getText(element);
	    final TextLayout layout = createTextLayout(text, width, gc);	
		if (fixedChecker.isFixedContent(element, reportPresentation)) {
			for (Point p : reportPresentation.getFixedPoints()) {
				layout.setStyle(GCUtils.FORM_HEADER_STYLE, p.x, p.y - 1);
			}
		} else {
			for (Point p : reportPresentation.getPoints()) {
				layout.setStyle(GCUtils.FORM_HEADER_STYLE, p.x, p.y - 1);
			}
		}
		
	    drawTextAndDispose(layout, rect, gc);   
	}
		
	public static void paintSimpleTextPresentation(int width, GC gc, Rectangle rect, IContentContainer element) {
		final String text = contentProvider.getText(element);
		drawSimpleText(text, width, rect, gc);
	}
		
	public static void paintNullPresentation(int width, GC gc, Rectangle rect, 
			IContentContainer element) {	
		paintSimpleTextPresentation(width, gc, rect, element);;
	}
	
	//=============================
	// paint form field
	
	public static void paintFormField(int width, GC gc, Rectangle rect, 
			AbstractFormField formField, 
			IContentContainer element,
			ITableCompositeSetting tableSetting,
			IFinder finder){
		String itemText = contentProvider.getText(element);
		if (itemText == null) {
			return;
		}
		
		final TextLayout layout = new TextLayout(gc.getDevice());
	    layout.setFont(TableSettings.presentationFont());
	    layout.setText(itemText);
	    layout.setWidth(width);
	    
	    // bold title
	    if (tableSetting.isShowFormNumeration() && formField.titleBoldPoint() != null) {
		    Point p = formField.titleBoldPoint();		    
		    layout.setStyle(TableSettings.boldMdPresentationStyle(), p.x, p.y - 1);
	    }
	    // select headers
	    setFormHeadersStyle(formField, layout);
	    // text styles (markdown)
	    GCUtils.applyFontStyle(layout, formField.boldPoints(), TableSettings.boldMDPresentationFont());	    
	    GCUtils.applyFontStyle(layout, formField.italicPoints(), TableSettings.italicPresentationFont());
	    GCUtils.applyFontStyle(layout, formField.boldItalicPoints(), TableSettings.boldItalicPresentationFont());
	    GCUtils.applyColorStyles(layout, formField.fencedCodePoints(), null, MdPreferences.codePreferences().color());
	    GCUtils.applyColorStyles(layout, formField.codePoints(), null, MdPreferences.codePreferences().color());
    	if (tableSetting.isShowMdComment()) {
    		GCUtils.applyColorStyles(layout, formField.commentPoints(), null, MdPreferences.commentPreferences().color());
    	}
	    
    	applyGlossaryStyles(layout, formField, tableSetting);
    	applyErrorStyles(layout, formField.errorsPoints(), tableSetting);
    	
    	applyFindedPositionStyles(layout, formField.getFindedPoints(), tableSetting);
    	if (finder != null &&
    			finder.isCurrent(formField)) {  
    		applyCurrentFindedPoinStyle(layout, formField.getFindedPoints(), formField, null, tableSetting, finder);
    	}
	
		int xLyaout = rect.x  + TableSettings.versionMarginLeft();
		int yLayout = rect.y;
		if (((IDipTableElement)element).isFirst()) {
			yLayout += TableSettings.versionMarginTop();
		}
	    layout.draw(gc, xLyaout, yLayout);
	    layout.dispose();
	}
	
	private static void setFormHeadersStyle(AbstractFormField formField, TextLayout layout) {
		for (Point p : ((IFormFields) formField).getFieldTitlePoints()) {
			layout.setStyle(GCUtils.FORM_HEADER_STYLE, p.x, p.y - 1);
		}
	}
	
	//================================
	// draw utils
	
	public static int getTextHeight(FontDimension fontDimension, String text) {
		int height = fontDimension.getTextHeight(text);
		height += TableSettings.versionMarginTop() + TableSettings.versionMarginBottom();	
		return height;
	}
	
	/*
	 * Подсветка терминов из глосария
	 */
	protected static void applyGlossaryStyles(TextLayout layout, IGlossaryPointsHolder glossaryPointHolder, ITableCompositeSetting tableSetting) {
	    if (tableSetting.isHighlightGloss()) {
	    	GCUtils.applyColorStyles(layout, glossaryPointHolder.getGlossaryPoints().glossaryPoints(), 
	    			null, MdPreferences.glossPreferences().color(), 1);
	    }
	}
	
	/*
	 * Подсветка орфографических ошибок 
	 */
	protected static void applyErrorStyles(TextLayout layout, List<Point> errorPoints, ITableCompositeSetting tableSetting) {
		if (tableSetting.isCheckSpellingEnable()) {
			GCUtils.applyErrorStyle(layout, errorPoints);
    	}
	}
	
	public static int getColumnWidth(int  columnWidth){
		int width = columnWidth;		
		if (width <= 200){
			width = 200;
		}
		width = width - (TableSettings.versionMarginLeft() + TableSettings.versionMarginRight());
		return width;
	}
	
	private static void drawImage(GC gc, Image image, Rectangle rect){
		gc.drawImage(image, rect.x + TableSettings.versionMarginLeft() + 1, rect.y + TableSettings.versionMarginTop());
	}
	
	private static Image getFixedModeImage(TablePresentation presentation) {
		return presentation instanceof ImagePresentation ? ImageProvider.IMAGE
				: ImageProvider.UML;				
	}
	
	private static void drawSimpleText(String text, int width,  Rectangle rect, GC gc) {
	    final TextLayout layout = createTextLayout(text, width, gc);
	    drawTextAndDispose(layout, rect, gc); 
	}
	
	protected static TextLayout createTextLayout(String text, int width, GC gc) {
		return GCUtils.createTextLayout(text, width, gc, TableSettings.presentationFont());
	}
	
	public static void drawTextAndDispose(TextLayout layout, Rectangle rect, GC gc) {
	    layout.draw(gc, rect.x + TableSettings.versionMarginLeft(), 
	    		rect.y + TableSettings.versionMarginTop()); 
	    layout.dispose();
	}
	
	//=======================
	// поиск (подсветка найденных позиций)

	protected static void applyFindedStyles(TextLayout layout, List<Point> findedPoints,
			TablePresentation presentation,
			ITableCompositeSetting tableSetting,
			IFinder finder) {
		List<Point> idPoints = presentation.getUnit().getUnitPresentation().getFindedIdPoints();
		setFindedStyles(layout, findedPoints, presentation.getUnit(), idPoints, tableSetting, finder);
	}

	public static void setFindedStyles(TextLayout layout, List<Point> findedPoints, 
			IDipDocumentElement dipDocElement,
			List<Point> idPoints, 
			ITableCompositeSetting tableSetting,
			IFinder finder) {
		applyFindedPositionStyles(layout, findedPoints, tableSetting);
		applyCurrentFindedPoinStyle(layout, findedPoints, dipDocElement, idPoints, tableSetting, finder);
	}

	/**
	 * Подсветка всех найденных позиций
	 */
	private static void applyFindedPositionStyles(TextLayout layout, List<Point> findedPoints, ITableCompositeSetting tableSetting) {
		if (findedPoints != null && tableSetting.isFindMode()) {
			for (Point p : findedPoints) {
				layout.setStyle(GCUtils.FIND_POINTS_STYLE, p.x, p.y - 1);
			}
		}
	}

	/**
	 * Подсветка текущей позиции
	 */
	private static void applyCurrentFindedPoinStyle(TextLayout layout, 
			List<Point> findedPoints,
			IDipDocumentElement dipDocElement, 
			List<Point> idPoints,
			ITableCompositeSetting tableSetting,
			IFinder finder) {
		if (findedPoints != null && tableSetting.isFindMode()) {
			if (isFindCurrentDipDocElement(dipDocElement, finder)) {
				int numberCurrentPoint = finder.getCurrentPointNumber();

				// если есть найденные позиции в столбце id
				int idPointsLength = 0;
				if (idPoints != null) {
					idPointsLength = idPoints.size();
				}
				numberCurrentPoint -= idPointsLength;

				if (numberCurrentPoint >= 0) {
					Point p = findedPoints.get(numberCurrentPoint);
					layout.setStyle(GCUtils.FIND_CURRENT_POINT_STYLE, p.x, p.y - 1);
				}
			}
		}
	}

	private static boolean isFindCurrentDipDocElement(TablePresentation presentation, IFinder finder) {
		return isFindCurrentDipDocElement(presentation.getUnit(), finder);
	}

	private static boolean isFindCurrentDipDocElement(IDipDocumentElement dipDocElement, IFinder finder) {
		return finder != null && finder.isCurrent(dipDocElement);
	}

}
