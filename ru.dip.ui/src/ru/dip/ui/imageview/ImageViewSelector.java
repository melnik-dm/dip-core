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
package ru.dip.ui.imageview;

import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;

import ru.dip.core.model.interfaces.IDipUnit;
import ru.dip.core.utilities.ui.swt.ColorProvider;
import ru.dip.ui.imageview.interfaces.IImageView;
import ru.dip.ui.imageview.interfaces.IImageViewPreferences;
import ru.dip.ui.table.table.TableSettings;

public class ImageViewSelector {

	private int fSelectColumn = -1;
	private int fSelectItem = -1;
	private final IImageView fImageView;

	public ImageViewSelector(IImageView imageView) {
		fImageView = imageView;
	}

	/*
	 * Возвращает true - когда выбран пустой последний столбец
	 */
	public boolean setHorizontalSelection(TableItem item, Point pt) {
		int height = fImageView.getTable().getBounds().height;
		boolean result = false;
		for (int col = 0; col < fImageView.getTable().getColumnCount(); col++) {
			Rectangle rect = item.getBounds(col);
			rect.height = height;
			if (rect.contains(pt)) {
				if (col + 1 == fImageView.getTable().getColumnCount()) {
					fSelectColumn = -1;
					item.setBackground(col, ColorProvider.DEFAULT_COLOR);
					result = true;
				} else {
					fSelectColumn = col;
					item.setBackground(col, TableSettings.tableSelectionColor());
				}
			} else {
				item.setBackground(col, ColorProvider.DEFAULT_COLOR);
			}
		}
		return result;
	}

	public void setVerticalSelection(TableItem item) {
		int newItem = table().getSelectionIndex();
		if (fSelectItem != newItem) {
			if (fSelectItem >= 0 && fSelectItem < table().getItemCount()) {
				table().getItem(fSelectItem).setBackground(ColorProvider.DEFAULT_COLOR);
			}
			item.setBackground(TableSettings.tableSelectionColor());
			fSelectItem = newItem;
		}
	}

	public void clearSelection() {
		fSelectColumn = -1;
		fSelectItem = -1;
	}

	// ========================
	// get selection

	public IDipUnit selectionUnit() {
		if (fImageView.getPreferences().isHorizontalMode()) {
			return horizontalSelectionUnit();
		} else {
			return verticalSelctionUnit();
		}
	}

	private IDipUnit verticalSelctionUnit() {
		int index = fImageView.getTable().getSelectionIndex();
		if (index >= 0) {
			return fImageView.getUnits().get(index);
		}
		return null;
	}

	private IDipUnit horizontalSelectionUnit() {
		if (fSelectColumn >= 0) {
			return fImageView.getUnits().get(fSelectColumn);
		}
		return null;
	}

	public int getSelectedColumn() {
		return fSelectColumn;
	}

	public void setSelectedColumn(int selectColumn) {
		fSelectColumn = selectColumn;
	}

	public int getSelectedItem() {
		return fSelectItem;
	}

	public void setSelectedItem(int selectItem) {
		fSelectItem = selectItem;
	}

	// ==================
	// isPoint

	/**
	 * Принадлежит ли указанная точка картинке в указанной TableItem
	 */
	public boolean isImagePoint(TableItem item, Point pt) {
		if (item == null) {
			return false;
		}

		if (preferences().isHorizontalMode()) {
			return isHorizontalImage(item, pt);
		}

		if (preferences().getAllignment() == ImageViewAllignment.IMAGE_RIGHT) {
			return isRightAllignmentImage(item, pt);

		} else if (preferences().getAllignment() == ImageViewAllignment.VERTICAL_ORDER) {
			return isVerticalAllignmentImage(item, pt);
		} else {
			// revers
			return isLeftAllginmentImage(item, pt);
		}
	}

	private boolean isRightAllignmentImage(TableItem item, Point pt) {
		int parentWidth = fImageView.getWidth();
		return (pt.x > parentWidth - ImagesView.IMAGE_WIDTH && pt.y < ImagesView.IMAGE_WIDTH + item.getBounds().y);
	}

	private boolean isLeftAllginmentImage(TableItem item, Point pt) {
		return (pt.x < ImagesView.IMAGE_WIDTH && pt.y < ImagesView.IMAGE_WIDTH + item.getBounds().y);
	}

	private boolean isVerticalAllignmentImage(TableItem item, Point pt) {
		return (pt.x < ImagesView.IMAGE_WIDTH && pt.y > item.getBounds().y + 7
				&& pt.y < item.getBounds().y + item.getBounds().height - 7);
	}

	private boolean isHorizontalImage(TableItem item, Point pt) {
		int height = fImageView.getHeight();
		Rectangle rect = item.getBounds(fSelectColumn);
		rect.height = height;
		if (rect.contains(pt)) {
			return true;
		} else {
			return false;
		}
	}

	// ===================
	// services

	private IImageViewPreferences preferences() {
		return fImageView.getPreferences();
	}

	private Table table() {
		return fImageView.getTable();
	}

}
