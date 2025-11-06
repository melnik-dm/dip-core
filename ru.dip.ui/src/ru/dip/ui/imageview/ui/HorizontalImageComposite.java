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
package ru.dip.ui.imageview.ui;

import static ru.dip.ui.imageview.ImagesView.HORIZONTAL_IMAGE_WIDTH;

import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseWheelListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.TextLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.ScrollBar;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

import ru.dip.core.model.DipUnit;
import ru.dip.core.model.interfaces.IDipUnit;
import ru.dip.core.utilities.DipUtilities;
import ru.dip.core.utilities.ui.swt.ColorProvider;
import ru.dip.ui.imageview.ImageViewSelector;
import ru.dip.ui.imageview.ImagesCacheProvider;
import ru.dip.ui.imageview.ImagesFontProvider;
import ru.dip.ui.imageview.interfaces.IImageComposite;
import ru.dip.ui.imageview.interfaces.IImageView;

public class HorizontalImageComposite implements IImageComposite {

	private final IImageView fImageView;
	private Table fTable;

	public HorizontalImageComposite(IImageView imagesView) {
		fImageView = imagesView;
	}

	public Table createTable(Composite parent) {
		fTable = new Table(parent, SWT.BORDER | SWT.HIDE_SELECTION);
		fTable.setHeaderVisible(false);
		fTable.setLinesVisible(true);
		fTable.setItemCount(1);
		setColumnsWidth();
		addPaintListeners();
		addListeners();
		return fTable;
	}

	private void setColumnsWidth() {
		for (int i = 0; i < units().size(); i++) {
			TableColumn column = new TableColumn(fTable, SWT.NONE);
			column.setWidth(HORIZONTAL_IMAGE_WIDTH + 10);
		}
		TableColumn lastColumn = new TableColumn(fTable, SWT.NONE);
		lastColumn.setWidth(1);
	}

	private void addPaintListeners() {
		fTable.addListener(SWT.MeasureItem, this::horizontalMeasure);
		fTable.addListener(SWT.EraseItem, this::erase);
		fTable.addListener(SWT.PaintItem, this::horizontalPaint);
	}

	private void addListeners() {
		fTable.addMouseWheelListener(new MouseWheelListener() {

			@Override
			public void mouseScrolled(MouseEvent e) {
				ScrollBar bar = fTable.getHorizontalBar();
				int selection = bar.getSelection();
				int delta = e.count > 0 ? bar.getPageIncrement() : -bar.getPageIncrement();
				delta = delta / 3;
				fTable.getHorizontalBar().setSelection(selection + delta);
			}
		});

		fTable.addListener(SWT.Selection, new Listener() {

			@Override
			public void handleEvent(Event event) {
				fTable.deselectAll();
			}
		});
		fTable.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				fTable.deselectAll();
			}
		});

		fTable.addFocusListener(new FocusAdapter() {

			@Override
			public void focusLost(FocusEvent e) {
				fTable.deselectAll();
				if (selector().getSelectedColumn() >= 0) {
					fTable.getItem(0).setBackground(selector().getSelectedColumn(), ColorProvider.DEFAULT_COLOR);
				}
				selector().setSelectedColumn(-1);
			}
		});
	}

	// ==========================
	// update table

	@Override
	public void updateTable() {
		int units = units().size();
		int delta = units - (fTable.getColumnCount() - 1);

		// если нужно добавить или столбцы
		if (delta > 0) {
			addColumns(delta);
		} else if (delta < 0) {
			removeColumns(delta);
		}
		clearData();
		fTable.clearAll();
		fTable.redraw();
	}

	private void addColumns(int newColumns) {
		fTable.getColumn(fTable.getColumnCount() - 1).dispose();
		for (int i = 0; i < newColumns; i++) {
			TableColumn column = new TableColumn(fTable, SWT.NONE);
			column.setWidth(HORIZONTAL_IMAGE_WIDTH + 10);
		}
		TableColumn lastColumn = new TableColumn(fTable, SWT.NONE);
		lastColumn.setWidth(1);
	}

	private void removeColumns(int removeCount) {
		for (int i = removeCount; i < 0; i++) {
			fTable.getColumn(fTable.getColumnCount() - 2).dispose();
		}
	}

	private void clearData() {
		for (TableColumn column : fTable.getColumns()) {
			column.setData(null);
		}
	}

	// =========================
	// paint

	private void erase(Event event) {
		// Selection:
		event.detail &= ~SWT.SELECTED;
		// Expect: selection now has no visual effect.
		// Actual: selection remains but changes from light blue to white.
		// MouseOver:
		event.detail &= ~SWT.HOT;
		// Expect: mouse over now has no visual effect.
		// Actual: behavior remains unchanged.
		GC gc = event.gc;
		TableItem item = (TableItem) event.item;
		gc.setBackground(item.getBackground(event.index));
		gc.fillRectangle(event.x, event.y, event.width, event.height);
	}

	private void horizontalMeasure(Event event) {
		measureHorizontal(event);
	}

	private void measureHorizontal(Event event) {
		if (event.index == fTable.getColumnCount() - 1) {
			return;
		}
		event.height = height();
		event.width = HORIZONTAL_IMAGE_WIDTH;
	}

	private void horizontalPaint(Event event) {
		if (event.index == fTable.getColumnCount() - 1) {
			return;
		}
		paintHorizontal(event);
	}

	private void paintHorizontal(Event event) {
		if (fTable.getColumnCount() <= 0) {
			return;
		}
		// get data
		Color defaultColor = event.gc.getForeground();
		int index = event.index;
		TableColumn column = fTable.getColumn(index);
		ColumnData columnData = null;
		Object data = column.getData();
		if (data == null) {
			IDipUnit unit = units().get(index);
			column.setData(columnData = new ColumnData(unit, event));
		} else {
			columnData = (ColumnData) data;
		}
		// draw id
		int xStart = event.x + 5;
		event.gc.setForeground(ColorProvider.IMAGE_VIEW_ID_GRAY);
		columnData.fIdLayout.draw(event.gc, xStart, event.y + 5);
		event.gc.setForeground(defaultColor);

		// draw image
		int imageY = 70;
		int factImageY = columnData.fIdHeight + 10;
		if (factImageY > imageY) {
			imageY = factImageY;
		}

		if (columnData.fImage != null) {
			event.gc.drawImage(columnData.fImage, event.x + columnData.fImageX,
					imageY + event.y/* columnData.idHeight + event.y + 10 */);
		}

		if (index != selector().getSelectedColumn()) {
			event.gc.setForeground(ColorProvider.SELECT);
		}
		// draw description
		int factY = event.y + columnData.imageHeight() + columnData.fIdHeight + 15;
		int descrY = event.y + HORIZONTAL_IMAGE_WIDTH + 50;
		if (descrY < factY) {
			descrY = factY;
		}
		columnData.fDescriptionLayout.draw(event.gc, event.x + 5, descrY);
		event.gc.setForeground(defaultColor);
	}

	private class ColumnData {

		private Image fImage;
		private TextLayout fIdLayout;
		private TextLayout fDescriptionLayout;
		private IDipUnit fUnit;

		private int fIdHeight;
		private int fImageX;

		public ColumnData(IDipUnit unit, Event event) {
			fUnit = unit;
			createID();
			createImage();
			createDescription();
		}

		private void createID() {
			String id = DipUtilities.relativeProjectID(fUnit);
			fIdLayout = new TextLayout(Display.getDefault());
			fIdLayout.setFont(fontProvider().getFont());
			String text = fontProvider().getWrapIDText(id, HORIZONTAL_IMAGE_WIDTH);
			fIdLayout.setText(text);
			fIdHeight = fIdLayout.getBounds().height;
		}

		private void createImage() {
			fImage = imageProvider().getShowedImage(fUnit, HORIZONTAL_IMAGE_WIDTH);
			if (fImage != null) {
				fImageX = (HORIZONTAL_IMAGE_WIDTH - fImage.getBounds().width) / 2 + 5;
			}
		}

		private void createDescription() {
			String description = ((DipUnit) fUnit).getUnitDescription().fullLinkTitle();
			fDescriptionLayout = new TextLayout(Display.getDefault());
			fDescriptionLayout.setFont(font());
			String text = fontProvider().getWrapText(description, HORIZONTAL_IMAGE_WIDTH);
			fDescriptionLayout.setText(text);
		}

		private int imageHeight() {
			if (fImage == null) {
				return 0;
			} else {
				return fImage.getBounds().height;
			}
		}
	}

	// ======================
	// services

	private List<IDipUnit> units() {
		return fImageView.getUnits();
	}

	private int height() {
		return fImageView.getHeight() - 10;
	}

	private Font font() {
		return fontProvider().getFont();
	}

	private ImagesFontProvider fontProvider() {
		return fImageView.getFontProvider();
	}

	private ImagesCacheProvider imageProvider() {
		return fImageView.getImageProvider();
	}

	private ImageViewSelector selector() {
		return fImageView.getImageSelector();
	}

}
