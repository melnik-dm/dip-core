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

import java.util.List;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.TextLayout;

import ru.dip.core.model.DipComment;
import ru.dip.core.model.DipFolder;
import ru.dip.core.model.DipProject;
import ru.dip.core.model.DipTableContainer;
import ru.dip.core.model.TextCommentContainer;
import ru.dip.core.model.interfaces.IDipComment;
import ru.dip.core.model.interfaces.IDipDocumentElement;
import ru.dip.core.model.interfaces.ITextComment;
import ru.dip.core.unit.UnitExtension;
import ru.dip.core.unit.UnitPresentation;
import ru.dip.core.unit.form.AbstractFormField;
import ru.dip.core.utilities.md.MdUtilities;
import ru.dip.core.utilities.ui.gc.GCUtils;
import ru.dip.core.utilities.ui.swt.ColorProvider;
import ru.dip.core.utilities.ui.swt.FontDimension;
import ru.dip.ui.table.ktable.KTableComposite;
import ru.dip.ui.table.ktable.model.ContentProvider;
import ru.dip.ui.table.ktable.model.IDipTableElement;
import ru.dip.ui.table.table.TableSettings;
import ru.dip.ui.utilities.image.ImageProvider;

public class CommentPainter  {

	private static ContentProvider contentProvider = ContentProvider.COMMENT_CONTENT_PROVIDER;
	
	private KTableComposite fTableComposite;
	
	public CommentPainter(KTableComposite tableComposite){
		fTableComposite  = tableComposite;
	}
	
	public void prepare(int width, IDipTableElement element) {
		update(width, element);
		contentProvider.setHeight(element, measure(element));
	}
	
	private void update(int width, IDipTableElement element) {
		IDipDocumentElement object = element.dipDocElement();
		if (object instanceof DipFolder) {
			updateCell(width, (DipFolder) object, element);
		} else if (object instanceof UnitPresentation || object instanceof AbstractFormField) {
			updateCell(width, (UnitExtension) object, element);
		} else if (object instanceof DipProject){
			updateCell(width, (DipProject) object, element);			
		}
	}

	// ================================
	// folder

	private void updateCell(int width, DipFolder folder, IDipTableElement element) {
		IDipComment comment = folder.comment();
		if (comment != null){
			updateCell(width, comment, element);
		} else {
			contentProvider.setText(element, "");
		}
		// серый цвет для родительских директорий
		if (!fTableComposite.model().isTable(folder) && !fTableComposite.model().isChild(folder)){
			contentProvider.setForeground(element, ColorProvider.GRAY);
		}
	}

	// ================================
	// UnitPresentation

	private void updateCell(int width, UnitExtension unitExtension, IDipTableElement element) {		
		IDipComment comment = getComment(unitExtension);
		if (comment == null) {
			contentProvider.setText(element, "");
			return;
		}
		if (!fTableComposite.isShowStrictMdComment() || !comment.hasTextComments() || comment.isEmpty()) {
			updateCell(width, comment, element);
			element.setCommentMainHeight(0);
		} else {
			updateWithStricComments(width, comment, element);
		}		
	}
	
	private IDipComment getComment(UnitExtension unitExtension) {
		IDipComment comment = unitExtension.getDipUnit().comment();
		if (comment == null) {
			// если нет основного комментария, то текстовые не считываются по умолчанию, нужно отдельно их проверить
			if (unitExtension.getDipUnit().getUnitType().isMarkdown()) {
				List<ITextComment> textComments = MdUtilities.readTextComments(unitExtension.getDipUnit());
				if (!textComments.isEmpty()) {
					return new TextCommentContainer(unitExtension.getDipUnit(), textComments);
				} 
				return null;				
			}
		} else if (comment instanceof DipComment) {
			DipComment reqComment = (DipComment) comment;
			reqComment.read();
		}
		return comment;
	}
	
	private void updateWithStricComments(int width, IDipComment comment, IDipTableElement element) {	
		width = prepareWidth(width);
		String main = getMainCommentText(width, comment);		
		int height = TableSettings.commentFontDimension().getTextHeight(main) 
				+ TableSettings.versionMarginTop() + 2;
		element.setCommentMainHeight(height);		
		String textComment = comment.getTextCommentsContent();			
		String text = main + "\n\n" + textComment;
				
		text = FontDimension.getWrapText(text, TableSettings.commentFontDimension().getStringLength(width));		
		contentProvider.setFont(element, TableSettings.commentFont());
		contentProvider.setText(element, text);
	}
	
	private String getMainCommentText(int width, IDipComment comment) {
		String main = comment.getCommentContent();
		if (main == null || main.isEmpty()) {
			main = "\n";
		}
		main = FontDimension.getWrapText(main, TableSettings.commentFontDimension().getStringLength(width));
		return main;
	}

	//=================================
	// DipProject

	private void updateCell(int width, DipProject project, IDipTableElement element) {
		IDipComment comment = project.comment();
		if (comment != null){
			updateCell(width, comment, element);
		} else {
			contentProvider.setText(element, "");
		}
		if (!fTableComposite.model().isTable(project)){
			contentProvider.setForeground(element, ColorProvider.GRAY);
		}
	}
		
	//================================
	// DipComment
	
	private void updateCell(int width, IDipComment comment, IDipTableElement element) {
		String text = fTableComposite.isShowStrictMdComment() ? comment.getFullContent() : comment.getCommentContent();
		if (text == null || text.isEmpty()) {
			return;
		}
		width = prepareWidth(width);
		text = FontDimension.getWrapText(text, TableSettings.commentFontDimension().getStringLength(width));
		contentProvider.setFont(element, TableSettings.commentFont());
		contentProvider.setText(element, text);
	}
		
	private int prepareWidth(int width) {
		if (width <= 150) {
			width = 150;
		}
		return width - (TableSettings.versionMarginLeft() + TableSettings.versionMarginRight());
	}
	
	//============================
	// measure     
	
	private int measure(IDipTableElement element) {
		IDipDocumentElement dipElement = element.dipDocElement();
		if (dipElement instanceof UnitExtension/* && event.index == 2*/){
			UnitExtension unitExtension = (UnitExtension) dipElement;
			IDipDocumentElement dipDocElement = unitExtension.getDipUnit();
			return measureCommentPresentation(element, dipDocElement);
		} else if (dipElement instanceof DipTableContainer/* && event.index == 2*/){
			DipTableContainer container = (DipTableContainer) dipElement;
			return measureCommentPresentation(element, container);	
		}
		return 0;
	}
	
	private int measureCommentPresentation(IDipTableElement element, IDipDocumentElement dipDocElement){		
		IDipComment comment = dipDocElement.comment();
		if (comment != null){
			String text = contentProvider.getText(element);			
			if (text != null && !text.trim().isEmpty()){
				if (isFullComment(dipDocElement)){
					String itemText = contentProvider.getText(element);	
					int height = TableSettings.commentFontDimension().getTextHeight(itemText);			
					height += TableSettings.versionMarginTop() + TableSettings.versionMarginBottom();			
					return height;	
				} else {
					int height = ImageProvider.COMMENT.getBounds().height + TableSettings.versionMarginTop() + TableSettings.versionMarginBottom();
					return height;	
				}
			}
		}
		return 0;
	}
	
	private boolean isFullComment(IDipDocumentElement dipDocElement){
		if (fTableComposite.selector().isSelectDipDocElement(dipDocElement.strong())) {
			return true;
		}		
		return fTableComposite.tableModel().isFullComment() && !fTableComposite.isFixedContent();
	}
		
	//=======================
	// paint
	
	public void paint(int width, GC gc, Rectangle rect, IDipTableElement element) {
		paintCommentColumn(width, gc, rect, element);
	}
	
	private void paintCommentColumn(int width, GC gc, Rectangle rect, IDipTableElement tableElement){		
		String text = contentProvider.getText(tableElement);
		if (text != null && !text.isEmpty()) {
			if (isFullComment(tableElement.dipDocElement())){
				if (tableElement.getCommentMainHeight() > 0) {
					drawLine(gc, rect, tableElement);
				}
				drawText(width, gc, rect, tableElement);		
			} else {
				drawImage(ImageProvider.COMMENT, rect, gc);							
			}
		} else {
			drawText(width, gc, rect, tableElement);		
		}
	}
	
	private void drawText(int width, GC gc, Rectangle rect, IDipTableElement tableElement) {
		String itemText = contentProvider.getText(tableElement); 
	    final TextLayout layout = GCUtils.createTextLayout(itemText, width, gc, TableSettings.commentFont());
	    layout.draw(gc, rect.x + TableSettings.versionMarginLeft(), 
	    		rect.y + TableSettings.versionMarginTop()); 
	    layout.dispose();	
	}
	
	private void drawImage(Image image, Rectangle rect, GC gc){
		gc.drawImage(image, rect.x + TableSettings.versionMarginLeft() + 1, rect.y + TableSettings.versionMarginTop());
	}
	
	private void drawLine(GC gc, Rectangle rect, IDipTableElement tableElement) {
		int commentMainHeight = rect.y + tableElement.getCommentMainHeight();
		Color oldForeground = gc.getForeground();
		gc.setForeground(TableSettings.lineColor());
		gc.drawLine(rect.x, commentMainHeight, rect.x + rect.width, commentMainHeight);
		gc.setForeground(oldForeground);
	}

}
