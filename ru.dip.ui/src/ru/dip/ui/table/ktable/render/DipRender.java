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

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;

import de.kupzog.ktable.KTableCellRenderer;
import de.kupzog.ktable.KTableModel;
import ru.dip.core.model.interfaces.IDipDocumentElement;
import ru.dip.core.model.interfaces.IDipParent;
import ru.dip.core.model.interfaces.IUnitDescription;
import ru.dip.core.model.interfaces.IUnitPresentation;
import ru.dip.core.unit.form.AbstractFormField;
import ru.dip.core.utilities.ui.gc.GCUtils;
import ru.dip.ui.table.ktable.model.DipTableModel;
import ru.dip.ui.table.ktable.model.IDipTableElement;
import ru.dip.ui.table.ktable.model.TableNode;
import ru.dip.ui.table.table.TableSettings;
import ru.dip.ui.utilities.image.ImageProvider;

public abstract class DipRender implements KTableCellRenderer {

	protected final DipTableModel fModel;

	public DipRender(DipTableModel table) {
		fModel = table;
	}
	
	@Override
	public int getOptimalWidth(GC arg0, int col, int arg2, Object arg3, boolean arg4, KTableModel arg5) {
		return 0;
	}
	
	void drawExpandNodeImage(GC gc, Rectangle rect, TableNode node, int indent) {
		if (!node.children().isEmpty()) {											
			Image expandImage = node.expand() ? ImageProvider.COLLAPSE : ImageProvider.EXPAND;			
			int x = rect.x + indent;
			int y = rect.y + (rect.height - expandImage.getBounds().height)/2;
			gc.drawImage(expandImage, x, y);
			Rectangle imageRectangle = expandImage.getBounds();
			imageRectangle.x = x;
			imageRectangle.y = y;					
			node.setImageExpandLoc(imageRectangle);
		}
	}
		
	protected void drawBorders(int col, int row, DipTableModel model, GC gc, Rectangle rect, IDipDocumentElement dipDocElement, IDipTableElement element) {
		Color currentForeground = gc.getForeground();
		gc.setForeground(TableSettings.lineColor());
		if (TableSettings.isShowLine()) {			
			if (dipDocElement instanceof IDipParent) {
				GCUtils.drawUpBorder(gc, rect);
				GCUtils.drawLeftBorder(gc, rect);
			} else if (dipDocElement instanceof IUnitPresentation) {
				GCUtils.drawUpBorder(gc, rect);
				GCUtils.drawLeftBorder(gc, rect);
			} else if (dipDocElement instanceof IUnitDescription) {
				GCUtils.drawLeftBorder(gc, rect);
			} else if (dipDocElement instanceof AbstractFormField) {
				if (element.background() != null) {
					clearUpBorders(gc, rect, element.background());
				}
				if (element.isFirst()) {
					GCUtils.drawUpBorder(gc, rect);
				}
				GCUtils.drawLeftBorder(gc, rect);
			}
		} else {
			if (col == 0) {
				GCUtils.drawLeftBorder(gc, rect);
			}
		}
		
		if (isLastRow(model, row, dipDocElement, element)) {
			GCUtils.drawDownBorder(gc, rect);
		}
		if (col == model.getColumnCount() - 1) {
			GCUtils.drawRightBorder(gc, rect);
		}
		gc.setForeground(currentForeground);
	}
	
	private void clearUpBorders(GC gc, Rectangle rect, Color color) {		
		Color current = gc.getForeground();
		gc.setForeground(color);
		GCUtils.drawUpBorder(gc, rect);
		GCUtils.drawDownBorder(gc, rect);
		gc.setForeground(current);
	}
	
	/**
	 * Для id, comment
	 */
	protected boolean isLastRow(KTableModel model, int row, IDipDocumentElement dipDocElement, IDipTableElement element) {
		if (dipDocElement instanceof AbstractFormField) {
			AbstractFormField field = (AbstractFormField) dipDocElement;			
			if (!fModel.isLastDipDocElement(field.getDipUnit())){
				return false;
			}
			if (dipDocElement.description() != null && !dipDocElement.description().isEmpty()) {
				return false;
			}
			return true;
		}		
		return row == model.getRowCount() - 1;
	}

}
