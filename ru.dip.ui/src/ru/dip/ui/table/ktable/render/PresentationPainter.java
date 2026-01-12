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
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.TextLayout;
import org.eclipse.swt.graphics.TextStyle;

import ru.dip.core.model.DipFolder;
import ru.dip.core.model.interfaces.IDipDocumentElement;
import ru.dip.core.model.interfaces.IDipParent;
import ru.dip.core.model.interfaces.IDipUnit;
import ru.dip.core.model.interfaces.IUnitDescription;
import ru.dip.core.model.interfaces.IUnitPresentation;
import ru.dip.core.unit.ChangeLogPresentation;
import ru.dip.core.unit.CsvUnitPresentation;
import ru.dip.core.unit.DiaPresentation;
import ru.dip.core.unit.GlossaryPresentation;
import ru.dip.core.unit.HtmlUnitPresentation;
import ru.dip.core.unit.ImagePresentation;
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
import ru.dip.core.unit.form.FormPresentation;
import ru.dip.core.unit.md.MarkDownPresentation;
import ru.dip.core.unit.md.SubMarkdownPresentation;
import ru.dip.core.utilities.ui.gc.GCUtils;
import ru.dip.core.utilities.ui.swt.ColorProvider;
import ru.dip.core.utilities.ui.swt.FontDimension;
import ru.dip.ui.table.ktable.KTableComposite;
import ru.dip.ui.table.ktable.model.ContentId;
import ru.dip.ui.table.ktable.model.ContentProvider;
import ru.dip.ui.table.ktable.model.ContentType;
import ru.dip.ui.table.ktable.model.DipTableModel;
import ru.dip.ui.table.ktable.model.IContentContainer;
import ru.dip.ui.table.ktable.model.IDipTableElement;
import ru.dip.ui.table.ktable.model.IFixedContentChecker;
import ru.dip.ui.table.ktable.model.ITableComposite;
//import ru.dip.ui.table.ktable.model.TableElement;
import ru.dip.ui.table.table.TableSettings;

public class PresentationPainter {
	
	protected static final ContentProvider contentProvider = PRESENTATION_CONTENT_PROVIDER;
	
	private final ITableComposite fTableComposite;
	protected final IFixedContentChecker fFixedChecker = new IFixedContentChecker() {
		
		@Override
		public boolean isFixedContent(IContentContainer element, Object obj) {
			return PresentationPainter.this.isFixedContent(element, obj);
		}
	};
	
	public PresentationPainter(ITableComposite tableComposite){
		fTableComposite  = tableComposite;
	}
	
	public void updateTableElement(int width, IDipTableElement presentationElement) {
		prepare(width, presentationElement);
	}
	
	protected void prepare(int width, IDipTableElement presentationElement) {
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
		String text = folder.getNumberDescrition(fTableComposite.getTableSettings().isShowNumeration());		
		int width = PaintPresentationUtils.getColumnWidth(columnWidth);
		text = FontDimension.getWrapText(text, TableSettings.boldPresentationFontDimension().getStringLength(width));
		
		contentProvider.setText(element, text);
		contentProvider.setFont(element, TableSettings.boldPresentationFont());

		// find points
		if (fTableComposite.getTableSettings().isFindMode() && folder.hasFindResult()) {
			folder.updateFindedPoints(text);
		}
		
		// серый цвет для родительских директорий
		if (!((KTableComposite)fTableComposite).model().isTable(folder) && !((KTableComposite)fTableComposite).model().isChild(folder)) {
			element.put(ContentId.PRESENTATION, ContentType.FOREGRAOUND, ColorProvider.GRAY);
		}
	}
	
	protected int measureDipFolder (IContentContainer element, IDipParent folder){
		final String itemText = contentProvider.getText(element); 
		return PaintPresentationUtils.getTextHeight(TableSettings.boldPresentationFontDimension(), itemText);		
	}
	
	//====================
	// prepare req descritpion
	
	public void prepare(int width, IUnitDescription description, IContentContainer element) {
		updateDescription(width, description, element);
		contentProvider.setHeight(element, measureDescriptionPresentation(element, description));
	}

	private void updateDescription(int columnWidth, IUnitDescription description, IContentContainer element) {		
		String text = description.fullLinkTitle();
		int width = PaintPresentationUtils.getColumnWidth(columnWidth);
		text = FontDimension.getWrapText(text, TableSettings.italicPresentationFontDimension().getStringLength(width));
		// find points
		if (fTableComposite.getTableSettings().isFindMode() && description.hasFindResult()) {
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
		return PaintPresentationUtils.getTextHeight(TableSettings.italicPresentationFontDimension(),itemText);		
	}
	
	//=======================
	// prepare table presentation
	
	public void prepare(int width, TablePresentation presentation, IContentContainer element) {
		if (presentation instanceof HtmlUnitPresentation
				|| presentation instanceof TableUnitPresentation) {
			updateHtmlPresetnation(width, presentation, element);
		} else if (presentation instanceof ImagePresentation 
			 || presentation instanceof PlantUmlPresentation) {						
			updateImagePresetnation(width, presentation, element);
		} else if (presentation instanceof SubMarkdownPresentation) {
			updateSubMarkdownPresetnation(width, presentation, element);
		} else if (presentation instanceof MarkDownPresentation) {
			updateMarkdownPresentation(width, presentation, element);
		} else if (presentation instanceof FormPresentation) {
			updateFormPresentation(width, presentation, element);
		} else if (presentation instanceof TextPresentation) {
			updateTextPresentation(width, presentation, element);
		} else if (presentation instanceof CsvUnitPresentation) {
			updateCsvPresentation(width, presentation, element);
		} else if (presentation instanceof DiaPresentation) {
			updateDiaPresentation(width, presentation, element);
		} else if (presentation instanceof ReportRefPresentation) {
			updateReportRefPresentation(width, presentation, element);
		} else if (presentation instanceof GlossaryPresentation) {
			updateGlossaryPresentation(width, presentation, element);
		} else if (presentation instanceof TocRefPresentation 
				|| presentation instanceof ChangeLogPresentation
				|| presentation instanceof PagebreakPresentation) {			
			updateSimpleTextPresentation(width, presentation, element);
		} else {
			updateNullPresentation(width, presentation, element);
		}
	}
	
	protected void updateHtmlPresetnation(int width, TablePresentation presentation, IContentContainer element) {
		PaintPresentationUtils.updateHtmlPresentation(width, (HtmlUnitPresentation) presentation, fTableComposite.getFinder());
		contentProvider.setHeight(element, PaintPresentationUtils.measureZomPresentation(element, (ZoomImagePresentation) presentation, fFixedChecker));		
	}
	
	protected void updateImagePresetnation(int width, TablePresentation presentation, IContentContainer element) {
		PaintPresentationUtils.updateImagePresentation(width, (ZoomImagePresentation) presentation);
		contentProvider.setHeight(element, PaintPresentationUtils.measureZomPresentation(element, (ZoomImagePresentation) presentation, fFixedChecker));
	}

	protected void updateSubMarkdownPresetnation(int width, TablePresentation presentation, IContentContainer element) {
		PaintPresentationUtils.updateSubMarkdownPresentation(width, (SubMarkdownPresentation) presentation, element, fFixedChecker, fTableComposite.getTableSettings());
		contentProvider.setHeight(element, PaintPresentationUtils.measureSimpleTextPresentation(element));
	}
	
	protected void updateMarkdownPresentation(int width, TablePresentation presentation, IContentContainer element) {
		PaintPresentationUtils.updateMarkdownPresentation(width, (MarkDownPresentation) presentation, element, fFixedChecker, fTableComposite.getTableSettings());
		contentProvider.setHeight(element, PaintPresentationUtils.measureSimpleTextPresentation(element));
	}
	
	protected void updateFormPresentation(int width, TablePresentation presentation, IContentContainer element) {
		PaintPresentationUtils.updateFormPresentation(width, (FormPresentation) presentation, element, fFixedChecker, fTableComposite.getTableSettings(), fTableComposite.getFormSettings());
		contentProvider.setHeight(element, PaintPresentationUtils.measureSimpleTextPresentation(element));
	}
	
	protected void updateTextPresentation(int width, TablePresentation presentation, IContentContainer element) {
		PaintPresentationUtils.updateTextPresentation(width, (TextPresentation) presentation, element, fFixedChecker, fTableComposite.getTableSettings());
		contentProvider.setHeight(element, PaintPresentationUtils.measureSimpleTextPresentation(element));
	}		

	protected void updateCsvPresentation(int width, TablePresentation presentation, IContentContainer element) {
		PaintPresentationUtils.updateCsvPresentation(width, (CsvUnitPresentation) presentation);
		contentProvider.setHeight(element, PaintPresentationUtils.measureZomPresentation(element, (ZoomImagePresentation) presentation, fFixedChecker));
	}

	protected void updateDiaPresentation(int width, TablePresentation presentation, IContentContainer element) {
		PaintPresentationUtils.updateDiaPresentation(width, (DiaPresentation) presentation, element);
		int height = PaintPresentationUtils.measureDiaPresentation(element, (DiaPresentation) presentation, fFixedChecker);
		contentProvider.setHeight(element, height);
	}

	protected void updateReportRefPresentation(int width, TablePresentation presentation, IContentContainer element) {
		PaintPresentationUtils.updateReportRefPresentation(width, (ReportRefPresentation) presentation, element, fFixedChecker);
		int height = PaintPresentationUtils.measureSimpleTextPresentation(element);			
		contentProvider.setHeight(element, height);
	}

	protected void updateGlossaryPresentation(int width, TablePresentation presentation, IContentContainer element) {
		PaintPresentationUtils.updateGlossaryRefPresentation(width, (GlossaryPresentation) presentation, element, fFixedChecker);
		int height = PaintPresentationUtils.measureSimpleTextPresentation(element);
		contentProvider.setHeight(element, height);		
	}

	protected void updateSimpleTextPresentation(int width, TablePresentation presentation, IContentContainer element) {
		PaintPresentationUtils.updateSimpleTextPresentation(width, presentation, element);
		int height =  PaintPresentationUtils.measureSimpleTextPresentation(element);
		contentProvider.setHeight(element, height);
	}

	protected void updateNullPresentation(int width, TablePresentation presentation, IContentContainer element) {
		PaintPresentationUtils.updateNullPresentation(width, element);
		contentProvider.setHeight(element, DipTableModel.DEFAULT_HEIGHT);
	}
	
	//======================================
	// FormField
	
	public void prepare(int width, AbstractFormField formField, IContentContainer element) {
		boolean fixed = isFixedContent(element, formField.getDipUnit());	
		// при fixed-mode отображаем только первые 3 поля
		if (fixed && ((IDipTableElement) element).getNumber() > 2) {
			contentProvider.setHeight(element, 0); // если поставить ноль, то если этот элемент будет последним, не отрисуется нижняя граница
		} else {
			PaintPresentationUtils.updateFormField(width, formField, element, fFixedChecker, fTableComposite.getFormSettings(), fTableComposite.getTableSettings());
			int height = PaintPresentationUtils.measureSimpleTextPresentationWithoutMargins(element);
			if (((IDipTableElement) element).isFirst()) {
				height += TableSettings.versionMarginTop();
			} else if (((IDipTableElement) element).isLast()) {
				height += TableSettings.versionMarginBottom();
			}
			contentProvider.setHeight(element, height);
		}
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
	    PaintPresentationUtils.drawTextAndDispose(layout, rect, gc);
	}
	
	protected void applyFindedStyles(TextLayout layout, List<Point> findedPoints, DipFolder folder) {
		List<Point> idPoints = folder.getFindedIdPoints();
		PaintPresentationUtils.setFindedStyles(layout, findedPoints, folder, idPoints, fTableComposite.getTableSettings(),  fTableComposite.getFinder());
	}
	
	public void paintTablePresentation(int width, GC gc, Rectangle rect, TablePresentation tablePresentation,
			IContentContainer element) {
		if (tablePresentation instanceof TextPresentation) {
			TextPresentation textPresentation = (TextPresentation) tablePresentation;
			PaintPresentationUtils.paintTextPresentation(width, gc, rect, textPresentation, element, fTableComposite.getTableSettings(), fTableComposite.getFinder());
		} else if (tablePresentation instanceof MarkDownPresentation) {
			PaintPresentationUtils.paintMarkdownPresentation(width, gc, rect, (MarkDownPresentation) tablePresentation, element, fFixedChecker, fTableComposite.getTableSettings(), fTableComposite.getFinder());
		} else if (tablePresentation instanceof FormPresentation) {
			PaintPresentationUtils.paintFormPresentation(width, gc, rect, (FormPresentation) tablePresentation, element, fTableComposite.getTableSettings(), fTableComposite.getFinder());
		} else if (tablePresentation instanceof ZoomImagePresentation) { 
			PaintPresentationUtils.paintZoomImagePresentation(width, gc, rect, (ZoomImagePresentation) tablePresentation, element, fFixedChecker);
		} else if (tablePresentation instanceof ReportRefPresentation) {
			PaintPresentationUtils.paintReportRefPresentation(width, gc, rect, (ReportRefPresentation) tablePresentation, element, fFixedChecker);
		} else if (tablePresentation instanceof GlossaryPresentation
				|| tablePresentation instanceof TocRefPresentation
				|| tablePresentation instanceof ChangeLogPresentation
				|| tablePresentation instanceof PagebreakPresentation) {
			PaintPresentationUtils.paintSimpleTextPresentation(width, gc, rect, element);
		} else {
			PaintPresentationUtils.paintNullPresentation(width, gc, rect, element);
		}
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
	    
		if (description.getFindedPoints() != null && fTableComposite.getTableSettings().isFindMode()) {
			PaintPresentationUtils.setFindedStyles(layout, description.getFindedPointsWithIndent(), description, null, fTableComposite.getTableSettings(), fTableComposite.getFinder());
		}
	    PaintPresentationUtils.drawTextAndDispose(layout, rect, gc);
	}
	
	public void paintFormField(int width, GC gc, Rectangle rect, AbstractFormField formField, IContentContainer element){
		PaintPresentationUtils.paintFormField(width, gc, rect, formField, element, fTableComposite.getTableSettings(), fTableComposite.getFinder());	
	}
	
	//=====================================
	
	protected boolean isFixedContent(IContentContainer element, Object obj){
		if (obj instanceof ReportRefPresentation) {
			return !fTableComposite.selector().isSelect(element);				
		}				
		if (obj instanceof GlossaryPresentation) {
			return !fTableComposite.selector().isSelect(element);				
		}
		
		if (obj instanceof IDipUnit) {
			IDipUnit unit = (IDipUnit) obj;
			
			// для отключенных объектов
			if (TableSettings.isFixedModeForDisableObjs() && unit.isDisabledInDocument()) {
				if (fTableComposite.selector().isSelect(element)) {
					return false;
				}
				return true;
			}

			if (!fTableComposite.getTableSettings().isFixedContent()) {
				return false;
			}
			if (fTableComposite.selector().isSelect(element)) {
				return false;
			}
			if (fTableComposite.selector().hasParentInSelectElements(unit)) {
				return false;
			}
		}
		return true;
	}
	
	//==================================
	// getters
	
	public ITableComposite tableComposite() {
		return fTableComposite;
	}
	
}
