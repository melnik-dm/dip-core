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

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.TextLayout;
import org.eclipse.swt.graphics.TextStyle;

import ru.dip.core.form.model.TextField;
import ru.dip.core.model.DipFolder;
import ru.dip.core.model.interfaces.IDipDocumentElement;
import ru.dip.core.model.interfaces.IDipDocumentElementHolder;
import ru.dip.core.model.interfaces.IDipParent;
import ru.dip.core.model.interfaces.IDipUnit;
import ru.dip.core.model.interfaces.IGlossaryPointsHolder;
import ru.dip.core.model.interfaces.ITextPresentation;
import ru.dip.core.model.interfaces.IUnitDescription;
import ru.dip.core.model.interfaces.IUnitPresentation;
import ru.dip.core.unit.ChangeLogPresentation;
import ru.dip.core.unit.CsvUnitPresentation;
import ru.dip.core.unit.DiaPresentation;
import ru.dip.core.unit.GlossaryPresentation;
import ru.dip.core.unit.HtmlUnitPresentation;
import ru.dip.core.unit.ImagePresentation;
import ru.dip.core.unit.JsonPresentation;
import ru.dip.core.unit.PagebreakPresentation;
import ru.dip.core.unit.PlantUmlPresentation;
import ru.dip.core.unit.ReportRefPresentation;
import ru.dip.core.unit.TablePresentation;
import ru.dip.core.unit.TableUnitPresentation;
import ru.dip.core.unit.TextPresentation;
import ru.dip.core.unit.TocRefPresentation;
import ru.dip.core.unit.UnitDescriptionPresentation;
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
import ru.dip.core.utilities.ui.swt.ColorProvider;
import ru.dip.core.utilities.ui.swt.FontDimension;
import ru.dip.ui.Messages;
import ru.dip.ui.preferences.MdPreferences;
import ru.dip.ui.table.ktable.KTableComposite;
import ru.dip.ui.table.ktable.model.ContentId;
import ru.dip.ui.table.ktable.model.ContentProvider;
import ru.dip.ui.table.ktable.model.ContentType;
import ru.dip.ui.table.ktable.model.DipTableModel;
import ru.dip.ui.table.ktable.model.IContentContainer;
import ru.dip.ui.table.ktable.model.ITableComposite;
import ru.dip.ui.table.ktable.model.TableElement;
import ru.dip.ui.table.table.TableSettings;
import ru.dip.ui.utilities.image.ImageProvider;

public class PresentationPainter {
	
	private static final ContentProvider contentProvider = PRESENTATION_CONTENT_PROVIDER;
	
	private final ITableComposite fTableComposite;
	
	public PresentationPainter(ITableComposite tableComposite){
		fTableComposite  = tableComposite;
	}
	
	public void updateTableElement(int width, TableElement presentationElement) {
		prepare(width, presentationElement);
	}
	
	private void prepare(int width, TableElement presentationElement) {
		IDipDocumentElement dde = presentationElement.dipDocElement();
		if (dde instanceof IUnitPresentation) {
			TablePresentation tablePresentation = ((IUnitPresentation) dde).getPresentation();
			prepare(width, tablePresentation, presentationElement);
		} else if (dde instanceof AbstractFormField) {
			prepare(width, (AbstractFormField) dde, presentationElement);
		} else if (dde instanceof DipFolder) {
			prepareFolder(width, (DipFolder) dde, presentationElement);
		} else if (dde instanceof IUnitDescription) {
			prepare(width, (IUnitDescription) dde, presentationElement);
		}
	}
	
	//=======================================
	// prepare req folder
	
	public void prepareFolder(int width, IDipParent folder, IContentContainer node) {		
		updateDipFolder(width, folder, node);
		contentProvider.setHeight(node, measureDipFolder(node, folder));
	}
	
	protected void updateDipFolder(int columnWidth, IDipParent folder, IContentContainer element) {
		String text = folder.getNumberDescrition(fTableComposite.isShowNumeration());		
		int width = getColumnWidth(columnWidth);
		text = FontDimension.getWrapText(text, TableSettings.boldPresentationFontDimension().getStringLength(width));
		
		contentProvider.setText(element, text);
		contentProvider.setFont(element, TableSettings.boldPresentationFont());

		// find points
		if (fTableComposite.isFindMode() && folder.hasFindResult()) {
			folder.updateFindedPoints(text);
		}
		
		// серый цвет для родительских директорий
		if (!((KTableComposite)fTableComposite).model().isTable(folder) && !((KTableComposite)fTableComposite).model().isChild(folder)) {
			element.put(ContentId.PRESENTATION, ContentType.FOREGRAOUND, ColorProvider.GRAY);
		}
	}
	
	protected int measureDipFolder (IContentContainer element, IDipParent folder){
		final String itemText = contentProvider.getText(element); 
		return getTextHeight(TableSettings.boldPresentationFontDimension(), itemText);		
	}
	
	//====================
	// prepare req descritpion
	
	public void prepare(int width, IUnitDescription description, IContentContainer element) {
		updateDescription(width, description, element);
		contentProvider.setHeight(element, measureDescriptionPresentation(element, description));
	}

	private void updateDescription(int columnWidth, IUnitDescription description, IContentContainer element) {		
		String text = description.fullLinkTitle();
		int width = getColumnWidth(columnWidth);
		text = FontDimension.getWrapText(text, TableSettings.italicPresentationFontDimension().getStringLength(width));
		// find points
		if (fTableComposite.isFindMode() && description.hasFindResult()) {
			description.updateFindedPoints(text);
		}
		contentProvider.setFont(element, TableSettings.italicPresentationFont());
		contentProvider.setText(element, text);
	}
	
	private int measureDescriptionPresentation (IContentContainer element, IUnitDescription description){
		String itemText = contentProvider.getText(element);
		if (itemText == null) {
			itemText = ""; //$NON-NLS-1$
		}			
		return getTextHeight(TableSettings.italicPresentationFontDimension(),itemText);		
	}
	
	//=======================
	// prepare table presentation
	
	public void prepare(int width, TablePresentation presentation, IContentContainer element) {
		if (presentation instanceof HtmlUnitPresentation
				|| presentation instanceof TableUnitPresentation) {
			updateHtmlPresentation(width, (HtmlUnitPresentation) presentation);
			contentProvider.setHeight(element, measureZomPresentation(element, (ZoomImagePresentation) presentation));
			
		} else if (presentation instanceof ImagePresentation 
			 || presentation instanceof PlantUmlPresentation) {
			updateImagePresentation(width, (ZoomImagePresentation) presentation);
			contentProvider.setHeight(element, measureZomPresentation(element, (ZoomImagePresentation) presentation));
		} else if (presentation instanceof SubMarkdownPresentation) { 
			updateSubMarkdownPresentation(width, (SubMarkdownPresentation) presentation, element);
			contentProvider.setHeight(element, measureSimpleTextPresentation(element));
		} else if (presentation instanceof MarkDownPresentation) {
			updateMarkdownPresentation(width, (MarkDownPresentation) presentation, element);
			contentProvider.setHeight(element, measureSimpleTextPresentation(element));
		} else if (presentation instanceof JsonPresentation) { 
			updateJsonPresentation(width, (JsonPresentation) presentation, element);
			contentProvider.setHeight(element, measureSimpleTextPresentation(element));
		} else if (presentation instanceof FormPresentation) {
			updateRequireemntPresentation(width, (FormPresentation) presentation, element);
			contentProvider.setHeight(element, measureSimpleTextPresentation(element));
		} else if (presentation instanceof TextPresentation) {
			updateTextPresentation(width, (TextPresentation) presentation, element);
			contentProvider.setHeight(element, measureSimpleTextPresentation(element));
		} else if (presentation instanceof CsvUnitPresentation) {
			updateCsvPresentation(width, (CsvUnitPresentation) presentation);
			contentProvider.setHeight(element, measureZomPresentation(element, (ZoomImagePresentation) presentation));
		} else if (presentation instanceof DiaPresentation) {
			updateDiaPresentation(width, (DiaPresentation) presentation, element);
			int height = measureDiaPresentation(element, (DiaPresentation) presentation);
			contentProvider.setHeight(element, height);			
		} else if (presentation instanceof ReportRefPresentation) {
			updateReportRefPresentation(width, (ReportRefPresentation) presentation, element);
			int height = measureSimpleTextPresentation(element);			
			contentProvider.setHeight(element, height);			
		} else if (presentation instanceof GlossaryPresentation) {
			updateGlossaryRefPresentation(width, (GlossaryPresentation) presentation, element);
			int height = measureSimpleTextPresentation(element);
			contentProvider.setHeight(element, height);			

		} else if (presentation instanceof TocRefPresentation 
				|| presentation instanceof ChangeLogPresentation
				|| presentation instanceof PagebreakPresentation) {			
			updateSimpleTextPresentation(width, presentation, element);
			int height =  measureSimpleTextPresentation(element);
			contentProvider.setHeight(element, height);

		} else {
			updateNullPresentation(width, element);
			contentProvider.setHeight(element, DipTableModel.DEFAULT_HEIGHT);			
		}
	}
	
	// можно объединить в один метод (маркдаун, json и  т.п.)
	private void updateTextPresentation(int columnWidth, TextPresentation textPresentation, IContentContainer element) {
		int width = getColumnWidth(columnWidth);
		if (!textPresentation.checkUpdate()) {
			textPresentation.read();
		}		
		String text = textPresentation.getText();
		updateTextPresentation(width, text, element, textPresentation);
	}	
	
	private void updateMarkdownPresentation(int columnWidth, MarkDownPresentation mdPresentation, IContentContainer element) {		
		int width = getColumnWidth(columnWidth);
		int lineLength =  TableSettings.presentationFontDimension().getStringLength(width);
		MarkdownSettings settings = TableSettings.markdownSettings(fTableComposite.isShowMdComment());
		String mdText = mdPresentation.parse(settings, lineLength);
		updateTextPresentation(width, mdText, element, mdPresentation);
	}
	
	private void updateSubMarkdownPresentation(int columnWidth, SubMarkdownPresentation subMdPresentation, IContentContainer element) {
		int width = getColumnWidth(columnWidth);		
		int lineLength =  TableSettings.presentationFontDimension().getStringLength(width);
		MarkdownSettings settings = TableSettings.markdownSettings(fTableComposite.isShowMdComment());
		String mdText = subMdPresentation.parse(settings, lineLength);
		String number = subMdPresentation.getNumber();
		if (number != null) {
			String indent = TagStringUtilities.getIndent(mdText);			
			mdText = indent + number + mdText.substring(indent.length());
		}		
		updateTextPresentation(width, mdText, element, subMdPresentation);
	}	
	
	// такой же метод как и для обычного текста (наверно можно убрать)
	private void updateJsonPresentation(int columnWidth, JsonPresentation jsonPresentation, IContentContainer element) {		
		int width = getColumnWidth(columnWidth);
		if (!jsonPresentation.checkUpdate()) {
			jsonPresentation.read();
		}		
		String text = jsonPresentation.getText();	
		updateTextPresentation(width, text, element, jsonPresentation);
	}
	
	private void updateTextPresentation(int columnWidth, String text, IContentContainer element, ITextPresentation textPresentation ) {
		text = FontDimension.getWrapText(text, TableSettings.presentationFontDimension().getStringLength(columnWidth));

		if (isFixedContent(element, textPresentation.getUnit())){
			text = getFixedText(text);
		}
		if (fTableComposite.isHighlightGloss()) {
			textPresentation.getGlossaryPoints().findGlossaryWords(text);
		}
		if (fTableComposite.isCheckSpellingEnable()) {
			textPresentation.findErrorWords(text);
		}
		
		if (fTableComposite.isFindMode() && textPresentation.hasFindResult()) {
			textPresentation.updateFindedPoints(text);
		}
		
		contentProvider.setFont(element, TableSettings.presentationFont());
		contentProvider.setText(element, text);
	}
	
	private String getFixedText(String text){	
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
	
	private void updateRequireemntPresentation(int columnWidth, FormPresentation reqPresentation, IContentContainer element) {
		int width = getColumnWidth(columnWidth);
		String text = computeFormPresentation(element, reqPresentation, width);
		updateTextPresentation(width, text, element, reqPresentation);
	}
	
	public String computeFormPresentation(IContentContainer element, FormPresentation reqPresentation, int width) {
		FormSettings settings = buildReqSettings(element, reqPresentation.getUnit());	
		int lineLength =  TableSettings.presentationFontDimension().getStringLength(width);
		MarkdownSettings mdSettings = TableSettings.markdownSettings(fTableComposite.isShowMdComment());
		return reqPresentation.tablePresentation(settings, mdSettings, lineLength);
	}
	
	private FormSettings buildReqSettings(IContentContainer element, IDipUnit unit) {
		return new FormSettings(fTableComposite.getFormSettings(), isFixedContent(element, unit));
	}
	
	private void updateHtmlPresentation(int columnWidth, HtmlUnitPresentation htmlPresentation) {
		if (isFindCurrentDipDocElement(htmlPresentation)) {
			int currentNumber = fTableComposite.getFinder().getCurrentNumber();
			htmlPresentation.setNewCurrentPointNumber(currentNumber);
		}
		updateImagePresentation(columnWidth, htmlPresentation);
	}
	
	private void updateImagePresentation(int columnWidth, ZoomImagePresentation imagePresentation) {	
		int width = getColumnWidth(columnWidth) - TableSettings.versionMarginLeft() - TableSettings.versionMarginRight() - 5;
		imagePresentation.checkUpdate();
		imagePresentation.trimImage(width);
	}
	
	private void updateCsvPresentation(int columnWidth,CsvUnitPresentation cvsPresentation) {
		int width = columnWidth - TableSettings.versionMarginLeft() - TableSettings.versionMarginRight() - 5;
		boolean wrapContent = TableSettings.isCsvColumnWidthByContent();
		cvsPresentation.checkUpdate();
		cvsPresentation.trimImage(width, wrapContent);
	}
	
	private void updateDiaPresentation(int columnWidth, DiaPresentation diaPresentation, IContentContainer element) {
		int width = getColumnWidth(columnWidth);
		diaPresentation.checkUpdate();
		diaPresentation.trimImage(width);
		if (diaPresentation.isDiaReadError()) {
			String text = DiaPresentation.DIA_NOT_INSTALLED;
			updateSimpleTextPresentation(width, text, element);	
		}		
	}
	
	private void updateReportRefPresentation(int columnWidth, ReportRefPresentation reportPresentation, IContentContainer element) {				
		reportPresentation.checkUpdate();
		int width = getColumnWidth(columnWidth);
		String text = reportPresentation.createText(TableSettings.presentationFontDimension().getStringLength(width));				
		if (!fTableComposite.selector().isSelect(element)) {
			text = reportPresentation.getFixedText();
		}
		updateSimpleTextPresentation(width, text, element);
	}		
	
	private void updateGlossaryRefPresentation(int columnWidth, GlossaryPresentation reportPresentation, IContentContainer element) {				
		String text = reportPresentation.getText();
		if (!fTableComposite.selector().isSelect(element)) {
			text = reportPresentation.getFixedText();
		}
		updateSimpleTextPresentation(columnWidth, text, element);
	}
	
	private void updateSimpleTextPresentation(int columnWidth, TablePresentation presentation, IContentContainer element) {				
		String text = presentation.getText();
		updateSimpleTextPresentation(columnWidth, text, element);
	}
	
	private void updateSimpleTextPresentation(int columnWidth, String text, IContentContainer element) {				
		int width = getColumnWidth(columnWidth);
		if (width <= 0) {
			return;
		}
		text = FontDimension.getWrapText(text, TableSettings.presentationFontDimension().getStringLength(width));	
		contentProvider.setFont(element, TableSettings.presentationFont());
		contentProvider.setText(element, text);
	}
			
	private void updateNullPresentation(int columnWidth, IContentContainer element) {
		String text = Messages.PresentationPainter_FilePresentationNotSupport;
		int width = getColumnWidth(columnWidth);
		if (width <= 0) {
			return;
		}
		text = FontDimension.getWrapText(text, TableSettings.presentationFontDimension().getStringLength(width));	
		contentProvider.setImage(element, ImageProvider.EMPTY_IMAGE);
		contentProvider.setFont(element, TableSettings.presentationFont());
	}
	
	//======================================
	// FormField
	
	public void prepare(int width, AbstractFormField formField, IContentContainer element) {
		boolean fixed = isFixedContent(element, formField.getDipUnit());	
		// при fixed-mode отображаем только первые 3 поля
		if (fixed && ((TableElement) element).getNumber() > 2) {
			contentProvider.setHeight(element, 0); // если поставить ноль, то если этот элемент будет последним, не отрисуется нижняя граница
		} else {
			updateFormField(width, formField, element);
			int height = measureSimpleTextPresentationWithoutMargins(element);
			if (((TableElement) element).isFirst()) {
				height += TableSettings.versionMarginTop();
			} else if (((TableElement) element).isLast()) {
				height += TableSettings.versionMarginBottom();
			}
			contentProvider.setHeight(element, height);
		}
	}
	
	private void updateFormField(int columnWidth, AbstractFormField formField, IContentContainer element) {
		IFormSettings settings = buildReqSettings(element, formField.getDipUnit());
		int width = getColumnWidth(columnWidth);
		if (width <= 0) {
			return;
		}
		int lineLength =  TableSettings.presentationFontDimension().getStringLength(width);	
		MarkdownSettings mdSettings = TableSettings.markdownSettings(fTableComposite.isShowMdComment());
		String text =  formField.tablePresentation(settings, mdSettings, lineLength, ((TableElement) element).isFirst(), ((TableElement) element).isLast());		
		text = FontDimension.getWrapText(text, TableSettings.presentationFontDimension().getStringLength(width));
		// если поле текстовое?
		if (isFixedContent(element, formField.getDipUnit())
				&& formField instanceof FormField 
				&& ((FormField)formField).getField() instanceof TextField){
			text = getFixedText(text);
		}
		// find points
		if (fTableComposite.isFindMode() && formField.hasFindResult()) {
			formField.updateFindedPoints(text);
		}

		if (isFixedContent(element, formField.getDipUnit())){
			text = getFixedText(text);
		} 	
		if (fTableComposite.isHighlightGloss()) {
			formField.getGlossaryPoints().findGlossaryWords(text);
		}
		if (fTableComposite.isCheckSpellingEnable()) {
			formField.findErrorWords(text);
		}
		
		contentProvider.setFont(element, TableSettings.presentationFont());
		contentProvider.setText(element, text);
	}
	
	//=======================================
	// measure
	
	private int measureDiaPresentation(IContentContainer element, DiaPresentation diaPresentation){
		if (diaPresentation.isDiaReadError()) {
			String itemText = contentProvider.getText(element);
			int stringCounter = 0;
			stringCounter = itemText.split(TagStringUtilities.LINE_SPLIT_REGEX).length;					
			int height = TableSettings.presentationFontDimension().charHeight() * stringCounter;
			height += TableSettings.versionMarginTop() + TableSettings.versionMarginBottom();			
			return height;
		}
		
		return measureZomPresentation(element, diaPresentation);
	}
	/*
	 * Определяет высоту картинки:
	 *   fixedContentImage - для fixed-режима
	 *   getZoomImage - для обычного
	 */
	private int measureZomPresentation(IContentContainer element, 
			ZoomImagePresentation zoomImagePresentation){
		if (isFixedContent(element, zoomImagePresentation.getUnit())){
			Image fixedContentImage = getFixedModeImage(zoomImagePresentation);
			return  getImageHeight(fixedContentImage);
		} else {
			Image image = zoomImagePresentation.getZoomImage();
			return  getImageHeight(image);
		}
	}
	
	private int getImageHeight(Image image) {
		return image == null ? 0 : image.getBounds().height 
				+ TableSettings.versionMarginTop() + TableSettings.versionMarginBottom();
	}
	
	protected int measureSimpleTextPresentation(IContentContainer element) {
		return measureSimpleTextPresentationWithoutMargins(element)
				+ TableSettings.versionMarginTop()
				+ TableSettings.versionMarginBottom();	
	}
	
	private int measureSimpleTextPresentationWithoutMargins(IContentContainer element) {
		final String itemText = contentProvider.getText(element); 
		return TableSettings.presentationFontDimension().getTextHeight(itemText);
	}
	
	//=======================================
	// paint
	
	public void paintDipFolder(int width, GC gc, Rectangle rect, DipFolder folder, IContentContainer element) {
		String itemText = contentProvider.getText(element); 
		if (itemText == null) {
			itemText = ""; //$NON-NLS-1$
		}
		
	    final TextLayout layout = GCUtils.createTextLayout(itemText, width, gc, TableSettings.boldPresentationFont());
		
	    if (!((KTableComposite)fTableComposite).model().isTable(folder) && !((KTableComposite)fTableComposite).model().isChild(folder)) {
		    layout.setStyle(GCUtils.GRAY_TEXT_STYLE, 0, itemText.length());
		} else {
		    TextStyle style = new TextStyle();
		    style.foreground = contentProvider.getForeground(element);
		    layout.setStyle(style, 0, itemText.length());
		}
	    
    	applyFindedStyles(layout, folder.getFindedPoints(), folder);
	    drawTextAndDispose(layout, rect, gc);
	}
	
	public void paintTablePresentation(int width, GC gc, Rectangle rect, TablePresentation tablePresentation,
			IContentContainer element) {
		if (tablePresentation instanceof JsonPresentation) {
			JsonPresentation jsonPresentation = (JsonPresentation) tablePresentation;
			paintJsonPresentation(width, gc, rect, jsonPresentation, element);
		} else if (tablePresentation instanceof TextPresentation) {
			TextPresentation textPresentation = (TextPresentation) tablePresentation;
			paintTextPresentation(width, gc, rect, textPresentation, element);
		} else if (tablePresentation instanceof MarkDownPresentation) {
			paintMarkdownPresentation(width, gc, rect, (MarkDownPresentation) tablePresentation, element);
		} else if (tablePresentation instanceof FormPresentation) {
			paintFormPresentation(width, gc, rect, (FormPresentation) tablePresentation, element);
		} else if (tablePresentation instanceof ZoomImagePresentation) { 
			paintZoomImagePresentation(width, gc, rect, (ZoomImagePresentation) tablePresentation, element);
		} else if (tablePresentation instanceof ReportRefPresentation) {
			paintReportRefPresentation(width, gc, rect, (ReportRefPresentation) tablePresentation, element);
		} else if (tablePresentation instanceof GlossaryPresentation
				|| tablePresentation instanceof TocRefPresentation
				|| tablePresentation instanceof ChangeLogPresentation
				|| tablePresentation instanceof PagebreakPresentation) {
			paintSimpleTextPresentation(width, gc, rect, element);
		} else {
			paintNullPresentation(width, gc, rect, element);
		}
	}

	public void paintTextPresentation(int width, GC gc, Rectangle rect, 
			TextPresentation textPresentation, IContentContainer element) {	
		String text = contentProvider.getText(element);
	    final TextLayout layout = createTextLayout(text, width, gc);
    	applyGlossaryStyles(layout, textPresentation);
    	applyErrorStyles(layout, textPresentation.errorsPoints());
    	applyFindedStyles(layout, textPresentation.getFindedPoints(), textPresentation); 	
	    drawTextAndDispose(layout, rect, gc);
	}
	
	public void paintJsonPresentation(int width, GC gc, Rectangle rect, 
			JsonPresentation jsonPresentation, IContentContainer element) {	
		String text = contentProvider.getText(element); 
	    final TextLayout layout = createTextLayout(text, width, gc);	    
    	applyGlossaryStyles(layout, jsonPresentation);
    	applyErrorStyles(layout, jsonPresentation.errorsPoints());		
    	applyFindedStyles(layout, jsonPresentation.getFindedPoints(), jsonPresentation); 	
	    drawTextAndDispose(layout, rect, gc);   
	}
	
	public void paintMarkdownPresentation(int width, GC gc, Rectangle rect, 
			MarkDownPresentation mdPresentation, IContentContainer element){
		String text = contentProvider.getText(element); 
		if (text == null) {
			updateMarkdownPresentation(width, mdPresentation, element);
			text = contentProvider.getText(element);
		}
	    final TextLayout layout = createTextLayout(text, width, gc);
 	    
	    GCUtils.applyFontStyle(layout, mdPresentation.boldPoints(), TableSettings.boldMDPresentationFont());	    
	    GCUtils.applyFontStyle(layout, mdPresentation.italicPoints(), TableSettings.italicPresentationFont());
	    GCUtils.applyFontStyle(layout, mdPresentation.boldItalicPoints(), TableSettings.boldItalicPresentationFont());

    	applyGlossaryStyles(layout, mdPresentation);
	    
    	GCUtils.applyColorStyles(layout, mdPresentation.fencedCodePoints(), null, MdPreferences.codePreferences().color());
    	GCUtils.applyColorStyles(layout, mdPresentation.codePoints(), null, MdPreferences.codePreferences().color());
    	if (fTableComposite.isShowMdComment()) {
    		GCUtils.applyColorStyles(layout, mdPresentation.commentPoints(), null, MdPreferences.commentPreferences().color());
    	}
    	
    	applyErrorStyles(layout, mdPresentation.errorsPoints());
    	applyFindedStyles(layout, mdPresentation.getFindedPoints(), mdPresentation);
	    drawTextAndDispose(layout, rect, gc);
	}
	
	private void paintFormPresentation(int width, GC gc, Rectangle rect,  FormPresentation formPresentation, IContentContainer element){
		String text = contentProvider.getText(element);
	    final TextLayout layout = createTextLayout(text, width, gc);

	    // bold title
	    if (fTableComposite.isShowFormNumeration() && formPresentation.titleBoldPoint() != null) {
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
    	if (fTableComposite.isShowMdComment()) {
    		GCUtils.applyColorStyles(layout, formPresentation.commentPoints(), null, MdPreferences.commentPreferences().color());
    	}
	      
    	applyGlossaryStyles(layout, formPresentation);
    	applyErrorStyles(layout, formPresentation.errorsPoints());   
    	applyFindedStyles(layout, formPresentation.getFindedPoints(), formPresentation);    
	    drawTextAndDispose(layout, rect, gc);   
	}
	
	private void paintZoomImagePresentation(int width, GC gc, Rectangle rect, ZoomImagePresentation zoomPresentation, IContentContainer element) {
		Image image = zoomPresentation.getZoomImage(width);
		if (image == null) {
			drawSimpleText(zoomPresentation.getText(), width, rect, gc);
			return;
		}
		if (isFixedContent(element, zoomPresentation.getUnit())){
			drawImage(gc, getFixedModeImage(zoomPresentation), rect);			
		} else { 	
			drawImage(gc, image, rect);
		}
	}
	
	private void paintReportRefPresentation(int width, GC gc, Rectangle rect,
			ReportRefPresentation reportPresentation, IContentContainer element){
		final String text = contentProvider.getText(element);
	    final TextLayout layout = createTextLayout(text, width, gc);	
		if (!fTableComposite.selector().isSelect(element)) {
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
		
	private void paintSimpleTextPresentation(int width, GC gc, Rectangle rect, IContentContainer element) {
		final String text = contentProvider.getText(element);
		drawSimpleText(text, width, rect, gc);
	}
		
	public void paintNullPresentation(int width, GC gc, Rectangle rect, 
			IContentContainer element) {	
		paintSimpleTextPresentation(width, gc, rect, element);;
	}
	
	public void paintDescription(int width, GC gc, Rectangle rect,
			UnitDescriptionPresentation description, IContentContainer element){
		String itemText = contentProvider.getText(element);
		if (itemText == null) {
			itemText = ""; //$NON-NLS-1$
		}				
	    final TextLayout layout = new TextLayout(gc.getDevice());
	    layout.setText(itemText);
	    layout.setIndent(0);
	    layout.setFont(TableSettings.italicPresentationFont());
	    layout.setWidth(width);
	    
		if (description.getFindedPoints() != null && fTableComposite.isFindMode()) {
			setFindedStyles(layout, description.getFindedPointsWithIndent(), description, null);
		}
	    drawTextAndDispose(layout, rect, gc);
	}
	
	public void paintFormField(int width, GC gc, Rectangle rect, AbstractFormField formField, IContentContainer element){
		String itemText = contentProvider.getText(element);
		if (itemText == null) {
			return;
		}
		
		final TextLayout layout = new TextLayout(gc.getDevice());
	    layout.setFont(TableSettings.presentationFont());
	    layout.setText(itemText);
	    layout.setWidth(width);
	    
	    // bold title
	    if (fTableComposite.isShowFormNumeration() && formField.titleBoldPoint() != null) {
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
    	if (fTableComposite.isShowMdComment()) {
    		GCUtils.applyColorStyles(layout, formField.commentPoints(), null, MdPreferences.commentPreferences().color());
    	}
	    
    	applyGlossaryStyles(layout, formField);
    	applyErrorStyles(layout, formField.errorsPoints());
    	
    	applyFindedPositionStyles(layout, formField.getFindedPoints());
    	if (fTableComposite.getFinder() != null &&
    			fTableComposite.getFinder().isCurrent(formField)) {  
    		applyCurrentFindedPoinStyle(layout, formField.getFindedPoints(), formField, null);
    	}
	
		int xLyaout = rect.x  + TableSettings.versionMarginLeft();
		int yLayout = rect.y;
		if (((TableElement)element).isFirst()) {
			yLayout += TableSettings.versionMarginTop();
		}
	    layout.draw(gc, xLyaout, yLayout);
	    layout.dispose();
	}
	
	private void setFormHeadersStyle(AbstractFormField formField, TextLayout layout) {
		for (Point p : ((IFormFields) formField).getFieldTitlePoints()) {
			layout.setStyle(GCUtils.FORM_HEADER_STYLE, p.x, p.y - 1);
		}
	}
	
	//=====================================
	
	private int getTextHeight(FontDimension fontDimension, String text) {
		int height = fontDimension.getTextHeight(text);
		height += TableSettings.versionMarginTop() + TableSettings.versionMarginBottom();	
		return height;
	}
	
	private int getColumnWidth(int  columnWidth){
		int width = columnWidth;		
		if (width <= 200){
			width = 200;
		}
		width = width - (TableSettings.versionMarginLeft() + TableSettings.versionMarginRight());
		return width;
	}
	
	protected boolean isFixedContent(IContentContainer element, IDipUnit unit){
		// для отключенных объектов
		if (TableSettings.isFixedModeForDisableObjs() && ((IDipDocumentElementHolder)element).isDisable()) {
			if (fTableComposite.selector().isSelect(element)) {
				return false;
			} 			
			return true;
		}
		
		if (!fTableComposite.isFixedContent()){
			return false;
		}
		if (fTableComposite.selector().isSelect(element)) {
			return false;
		} 
		if (fTableComposite.selector().hasParentInSelectElements(unit)) {
			return false;
		}
		return true;
	}
	
	private void drawImage(GC gc, Image image, Rectangle rect){
		gc.drawImage(image, rect.x + TableSettings.versionMarginLeft() + 1, rect.y + TableSettings.versionMarginTop());
	}
	
	private Image getFixedModeImage(TablePresentation presentation) {
		return presentation instanceof ImagePresentation ? ImageProvider.IMAGE
				: ImageProvider.UML;				
	}
	
	private void drawSimpleText(String text, int width,  Rectangle rect, GC gc) {
	    final TextLayout layout = createTextLayout(text, width, gc);
	    drawTextAndDispose(layout, rect, gc); 
	}
	
	protected TextLayout createTextLayout(String text, int width, GC gc) {
		return GCUtils.createTextLayout(text, width, gc, TableSettings.presentationFont());
	}
	
	protected void drawTextAndDispose(TextLayout layout, Rectangle rect, GC gc) {
	    layout.draw(gc, rect.x + TableSettings.versionMarginLeft(), 
	    		rect.y + TableSettings.versionMarginTop()); 
	    layout.dispose();
	}
	
	/*
	 * Подсветка терминов из глосария
	 */
	protected void applyGlossaryStyles(TextLayout layout, IGlossaryPointsHolder glossaryPointHolder) {
	    if (fTableComposite.isHighlightGloss()) {
	    	GCUtils.applyColorStyles(layout, glossaryPointHolder.getGlossaryPoints().glossaryPoints(), 
	    			null, MdPreferences.glossPreferences().color(), 1);
	    }
	}
	
	/*
	 * Подсветка орфографических ошибок 
	 */
	protected void applyErrorStyles(TextLayout layout, List<Point> errorPoints) {
		if (fTableComposite.isCheckSpellingEnable()) {
			GCUtils.applyErrorStyle(layout, errorPoints);
    	}
	}
		
	//=======================
	// поиск (подсветка найденных позиций)
	
	protected void applyFindedStyles(TextLayout layout, List<Point> findedPoints, TablePresentation presentation) {		
		List<Point> idPoints = presentation.getUnit().getUnitPresentation().getFindedIdPoints();
		setFindedStyles(layout, findedPoints, presentation.getUnit(), idPoints);
	}
	
	protected void applyFindedStyles(TextLayout layout, List<Point> findedPoints, DipFolder folder) {		
		List<Point> idPoints = folder.getFindedIdPoints();
		setFindedStyles(layout, findedPoints, folder, idPoints);
	}

	protected void setFindedStyles(TextLayout layout, List<Point> findedPoints, IDipDocumentElement dipDocElement, List<Point> idPoints) {
		applyFindedPositionStyles(layout, findedPoints);
		applyCurrentFindedPoinStyle(layout, findedPoints, dipDocElement, idPoints);
	}
	
	/**
	 * Подсветка всех  найденных позиций
	 */
	private void applyFindedPositionStyles(TextLayout layout, List<Point> findedPoints) {
		if (findedPoints != null && fTableComposite.isFindMode()) {
			for (Point p : findedPoints) {
				layout.setStyle(GCUtils.FIND_POINTS_STYLE, p.x, p.y - 1);
			}
		}
	}
	
	/**
	 * Подсветка текущей позиции
	 */
	private void applyCurrentFindedPoinStyle(TextLayout layout, List<Point> findedPoints, IDipDocumentElement dipDocElement, List<Point> idPoints) {
		if (findedPoints != null && fTableComposite.isFindMode()) {
	    	if (isFindCurrentDipDocElement(dipDocElement)) {    		    		
	    		int numberCurrentPoint = fTableComposite.getFinder().getCurrentPointNumber();
				
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
	
	private boolean isFindCurrentDipDocElement(TablePresentation presentation) {
		return isFindCurrentDipDocElement(presentation.getUnit());
	}
	
	private boolean isFindCurrentDipDocElement(IDipDocumentElement dipDocElement) {
		return fTableComposite.getFinder() != null 
    			&& fTableComposite.getFinder().isCurrent(dipDocElement);
	}
	
	//==================================
	// getters
	
	public ITableComposite tableComposite() {
		return fTableComposite;
	}
	
}
