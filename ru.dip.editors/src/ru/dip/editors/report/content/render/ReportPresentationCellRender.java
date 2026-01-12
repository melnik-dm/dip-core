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
package ru.dip.editors.report.content.render;

import java.util.List;

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;

import de.kupzog.ktable.KTableModel;
import ru.dip.core.model.interfaces.IDipDocumentElement;
import ru.dip.core.unit.TablePresentation;
import ru.dip.core.unit.UnitDescriptionPresentation;
import ru.dip.core.unit.UnitPresentation;
import ru.dip.core.unit.form.AbstractFormField;
import ru.dip.core.utilities.ui.gc.GCUtils;
import ru.dip.core.utilities.ui.swt.ColorProvider;
import ru.dip.editors.report.content.ReportContentModel;
import ru.dip.editors.report.content.model.RceEntryElement;
import ru.dip.editors.report.content.model.ReportEntryPresentation;
import ru.dip.ktable.model.IKTableModel;
import ru.dip.ui.table.ktable.model.ContentId;
import ru.dip.ui.table.ktable.model.ContentType;
import ru.dip.ui.table.ktable.model.DipTableModel;
import ru.dip.ui.table.ktable.model.IContentContainer;
import ru.dip.ui.table.ktable.model.IDipTableElement;
import ru.dip.ui.table.table.TableSettings;

public class ReportPresentationCellRender extends ReportDipRender {
	
	protected final  Display m_Display;
	// ширина полоски для выделения diff-элементов
	
	public ReportPresentationCellRender(ReportContentModel tableModel) {
		super(tableModel);
		m_Display = Display.getDefault();
	}
	
	@Override
	public void drawCell(GC gc, Rectangle rect, int col, int row, Object object, boolean arg5, boolean arg6,
			boolean arg7, KTableModel model) {
		gc.setForeground(ColorProvider.BLACK);
		if (object instanceof IDipTableElement) {						
			IDipTableElement tableElement = (IDipTableElement) object;
			IDipDocumentElement dipDocElement = tableElement.dipDocElement();			
			if (tableElement instanceof RceEntryElement) {
				GCUtils.fillBackground(gc, rect, TableSettings.tableColor2());
			} else {
				GCUtils.fillBackground(gc, rect, TableSettings.tableColor1());
				
			}
			Rectangle indentRect = computeIndentRectangle(rect, col, (IKTableModel) model, tableElement, gc);
			drawDipDocElement(dipDocElement, row, tableElement, gc, indentRect);
			drawBorders(col, row, fModel, gc, rect, dipDocElement, tableElement);
		}
	}
	

	//====================
	// indent, rectangle
	
	/**
	 * Рассчитывает отступ, плюс отрисовывает маркер для узла (+-)
	 */
	private Rectangle computeIndentRectangle(Rectangle rect, int col, IKTableModel model, IDipTableElement element, GC gc) {
		Rectangle indentRect = new Rectangle(rect.x, rect.y, rect.width, rect.height);		
		if (!fModel.tableComposite().getTableSettings().isOneListMode()) {
			int indent = 0;
			if (col == 0) {
				indent = ((DipTableModel) model).getIndent(element) * 16;
				indentRect.x += indent + 16;
			}
		}
		return indentRect;
	}
	
	@Override
	protected boolean isLastRow(KTableModel model, int row, IDipDocumentElement dipDocElement, IDipTableElement element) {
		boolean last =  super.isLastRow(model, row, dipDocElement, element);
		if (!last || !element.isAbstractField()) {
			return last;
		}
		if (element.getInt(ContentId.PRESENTATION, ContentType.HEIGHT) == 0) {
			return false;
		}
		if (dipDocElement.description() != null && !dipDocElement.description().isEmpty()) {
			return false;
		}
		List<IDipTableElement> linked = element.linkedWithibleElements();
		int index = linked.indexOf(element);
		for (int i = index + 1; i < linked.size(); i++) {
			if (linked.get(i).getInt(ContentId.PRESENTATION, ContentType.HEIGHT) != 0) {
				return false;
			}			
		}
		return true;
	}
	
	//=============
	// draw presentation
	
	private void drawDipDocElement(IDipDocumentElement dipDocElement, int row, IDipTableElement tableElement, GC gc, Rectangle indentRect) {
		if (dipDocElement instanceof ReportEntryPresentation) {
			drawReportEntry(row, tableElement, (ReportEntryPresentation) dipDocElement, gc, indentRect);
		} else if (dipDocElement instanceof UnitPresentation) {	
			drawUnitPresentation(row, tableElement, (UnitPresentation) dipDocElement, gc, indentRect);
		} else if (dipDocElement instanceof UnitDescriptionPresentation) {			
			fModel.reqPainter().paintDescription(width(), gc, indentRect, (UnitDescriptionPresentation) dipDocElement, (IContentContainer) tableElement);			
		} else if (dipDocElement instanceof AbstractFormField) {
			drawFormField(row, tableElement, (AbstractFormField) dipDocElement, gc, indentRect);
		}
	}
	
	private void drawUnitPresentation(int row, IDipTableElement tableElement, UnitPresentation dipDocElement, GC gc, Rectangle indentRect) {
		TablePresentation tablePresentation = ((UnitPresentation) dipDocElement).getPresentation();
		Rectangle rect = rectangle(indentRect, row);		
		fModel.reqPainter().paintTablePresentation(width(), gc, rect, tablePresentation, (IContentContainer) tableElement);	
	}
	
	private void drawReportEntry(int row, IDipTableElement tableElement, 
			ReportEntryPresentation folder, GC gc, Rectangle indentRect) {
		Rectangle rect = rectangle(indentRect, row);			
		fModel.reqPainter().paintReportEntryPresentation(width(), gc, rect, folder, (IContentContainer) tableElement);
	}
	
	private void drawFormField(int row, IDipTableElement tableElement, AbstractFormField formField, GC gc, Rectangle indentRect) {
		Rectangle rect = rectangle(indentRect, row);		
		fModel.reqPainter().paintFormField(width(), gc, rect, formField, (IContentContainer) tableElement);	
	}
	
	private int width() {
		return fModel.presentationWidth();
	}
	
	private Rectangle rectangle(Rectangle original, int row) {
		if (row == fModel.firstCell()) {
			return new Rectangle(original.x, original.y - fModel.indent(), original.width, original.height);
		}
		return original;
	}

}
