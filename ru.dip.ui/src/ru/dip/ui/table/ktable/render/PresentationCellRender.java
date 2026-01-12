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
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;

import de.kupzog.ktable.KTableModel;
import ru.dip.core.model.DipFolder;
import ru.dip.core.model.interfaces.IDipDocumentElement;
import ru.dip.core.unit.TablePresentation;
import ru.dip.core.unit.UnitDescriptionPresentation;
import ru.dip.core.unit.UnitPresentation;
import ru.dip.core.unit.form.AbstractFormField;
import ru.dip.core.utilities.ui.gc.GCUtils;
import ru.dip.core.utilities.ui.swt.ColorProvider;
import ru.dip.ui.table.ktable.diff.DiffModel;
import ru.dip.ui.table.ktable.diff.model.ReqDiffStatus;
import ru.dip.ui.table.ktable.model.ContentId;
import ru.dip.ui.table.ktable.model.ContentType;
import ru.dip.ui.table.ktable.model.DipTableModel;
import ru.dip.ui.table.ktable.model.IDipTableElement;
import ru.dip.ui.table.ktable.model.TableElement;
import ru.dip.ui.table.ktable.model.TableNode;
import ru.dip.ui.table.table.TableModel;
import ru.dip.ui.table.table.TableSettings;

public class PresentationCellRender extends DipRender {

	protected final  Display m_Display;
	// ширина полоски для выделения diff-элементов
	private final int fDiffIndent = TableSettings.versionMarginLeft() - 1 > 1 ? TableSettings.versionMarginLeft() - 1 : 2;
	
	public PresentationCellRender(DipTableModel tableModel) {
		super(tableModel);
		m_Display = Display.getDefault();
	}
	
	@Override
	public void drawCell(GC gc, Rectangle rect, int col, int row, Object object, boolean arg5, boolean arg6,
			boolean arg7, KTableModel model) {
		gc.setForeground(ColorProvider.BLACK);
		if (object instanceof TableElement) {						
			TableElement tableElement = (TableElement) object;
			IDipDocumentElement dipDocElement = tableElement.dipDocElement();				
			GCUtils.fillBackground(gc, rect, tableElement);
			drawDiff(gc, rect, dipDocElement);
			Rectangle indentRect = computeIndentRectangle(rect, col, (DipTableModel) model, tableElement, gc);
			drawDipDocElement(dipDocElement, row, tableElement, gc, indentRect);
			drawBorders(col, row, fModel, gc, rect, dipDocElement, tableElement);
		}
	}
	
	//==========================
	// diff
	
	private void drawDiff(GC gc, Rectangle rect, IDipDocumentElement dipDocElement) {
		ReqDiffStatus status = getReqDiffStatus(dipDocElement);
		if (status != null) {
			if (status.isStaging()) {
				drawDiffBackground(status.getColor(), gc, rect);
			} else {
				drawDiffOriginBackground(status.getColor(), gc, rect);
			}
		}
	}
	
	private ReqDiffStatus getReqDiffStatus(IDipDocumentElement dipDocElement) {
		TableModel model = fModel.getTableModel();
		if (model instanceof DiffModel) {
			return ((DiffModel) model).getDiffStatus(dipDocElement);
		}
		return null;
	}
	
	/*
	 * Отметка diff. Сплошная линия
	 */
	private void drawDiffBackground(Color diffColor, GC gc, Rectangle rect) {						
		if (diffColor != null) {
			Color back = gc.getBackground();
			Color fore = gc.getForeground();
			gc.setForeground(diffColor);
			gc.setBackground(diffColor);
			gc.fillRectangle(rect.x, rect.y, fDiffIndent, rect.height);
			gc.setBackground(back);
			gc.setForeground(fore);
		}
	}
	
	/*
	 * Отметка diff. Пунктирная линия
	 */
	private void drawDiffOriginBackground(Color diffColor, GC gc, Rectangle rect) {						
		if (diffColor != null) {
			Color back = gc.getBackground();
			Color fore = gc.getForeground();
			gc.setForeground(diffColor);
			gc.setBackground(diffColor);			
			for (int y = 0; y < rect.height - 8; y += 15) {
				gc.fillRectangle(rect.x, rect.y + y, fDiffIndent, 10);				
			}
			gc.setBackground(back);
			gc.setForeground(fore);
		}
	}
	
	//====================
	// indent, rectangle
	
	/**
	 * Рассчитывает отступ, плюс отрисовывает маркер для узла (+-)
	 */
	private Rectangle computeIndentRectangle(Rectangle rect, int col, DipTableModel model, TableElement element, GC gc) {
		Rectangle indentRect = new Rectangle(rect.x, rect.y, rect.width, rect.height);		
		if (!fModel.tableComposite().getTableSettings().isOneListMode()) {
			int indent = 0;
			if (col == 0) {
				indent = ((DipTableModel) model).getIndent(element) * 16;
				if (element instanceof TableNode) {
					drawExpandNodeImage(gc, rect, (TableNode) element, indent);
				}
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
	
	private void drawDipDocElement(IDipDocumentElement dipDocElement, int row, TableElement tableElement, GC gc, Rectangle indentRect) {
		if (dipDocElement instanceof DipFolder) {
			drawDipFolder(row, tableElement, (DipFolder) dipDocElement, gc, indentRect);
		} else if (dipDocElement instanceof UnitPresentation) {	
			drawUnitPresentation(row, tableElement, (UnitPresentation) dipDocElement, gc, indentRect);
		} else if (dipDocElement instanceof UnitDescriptionPresentation) {			
			fModel.reqPainter().paintDescription(width(), gc, indentRect, (UnitDescriptionPresentation) dipDocElement, tableElement);			
		} else if (dipDocElement instanceof AbstractFormField) {
			drawFormField(row, tableElement, (AbstractFormField) dipDocElement, gc, indentRect);

		}
	}
	
	private void drawUnitPresentation(int row, TableElement tableElement, UnitPresentation dipDocElement, GC gc, Rectangle indentRect) {
		TablePresentation tablePresentation = ((UnitPresentation) dipDocElement).getPresentation();
		Rectangle rect = rectangle(indentRect, row);		
		fModel.reqPainter().paintTablePresentation(width(), gc, rect, tablePresentation, tableElement);	
	}
	
	private void drawDipFolder(int row, TableElement tableElement, 
			DipFolder folder, GC gc, Rectangle indentRect) {
		Rectangle rect = rectangle(indentRect, row);			
		fModel.reqPainter().paintDipFolder(width(), gc, rect, folder, tableElement);
	}
	
	private void drawFormField(int row, TableElement tableElement, AbstractFormField formField, GC gc, Rectangle indentRect) {
		Rectangle rect = rectangle(indentRect, row);		
		fModel.reqPainter().paintFormField(width(), gc, rect, formField, tableElement);	
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
