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

import static ru.dip.ui.imageview.ImagesView.IMAGE_WIDTH;

import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.TextLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

import ru.dip.core.model.DipUnit;
import ru.dip.core.model.interfaces.IDipUnit;
import ru.dip.core.utilities.DipUtilities;
import ru.dip.core.utilities.ui.swt.ColorProvider;
import ru.dip.ui.imageview.ImageViewAllignment;
import ru.dip.ui.imageview.ImagesCacheProvider;
import ru.dip.ui.imageview.ImagesFontProvider;
import ru.dip.ui.imageview.interfaces.IImageComposite;
import ru.dip.ui.imageview.interfaces.IImageView;
import ru.dip.ui.imageview.interfaces.IImageViewPreferences;

public class VerticalImageComposite implements IImageComposite {

	private final IImageView fImageView;
	private Table fTable;

	public VerticalImageComposite(IImageView imagesView) {
		fImageView = imagesView;
	}

	@Override
	public void updateTable() {
		fTable.setItemCount(units().size());
		fTable.redraw();
	}

	public Table createTable(Composite parent) {
		fTable = new Table(parent, SWT.BORDER);
		fTable.setLinesVisible(true);
		fTable.setHeaderVisible(false);
		TableColumn column = new TableColumn(fTable, SWT.NONE);
		fTable.addListener(SWT.MeasureItem, this::verticalMeasure);
		fTable.addListener(SWT.EraseItem, new Listener() {
			public void handleEvent(Event event) {
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
		});
		fTable.addListener(SWT.PaintItem, this::verticalPaint);
		fTable.getColumn(0).pack();
		fTable.pack();
		column.setWidth(width());
		fTable.setItemCount(units().size());

		return fTable;
	}

	// ===============================
	// vertical measure

	private void verticalMeasure(Event event) {
		if (preferences().getAllignment() == ImageViewAllignment.IMAGE_RIGHT) {
			measureRightImageItem(event);
		} else if (preferences().getAllignment() == ImageViewAllignment.VERTICAL_ORDER) {
			measureVerticalOrderItem(event);
		} else {
			measureRightImageItem(event);
		}
	}

	private void measureRightImageItem(Event event) {
		TableItem item = (TableItem) event.item;
		int index = fTable.indexOf(item);
		IDipUnit unit = units().get(index);
		int idHeight = idHeight(unit, event);
		int idDelta = 0;
		if (idHeight > IMAGE_WIDTH) {
			idDelta = idHeight - IMAGE_WIDTH + 10;
		}
		int descriptionHeight = descriptionHeight(unit, event);
		int height = IMAGE_WIDTH + descriptionHeight + idDelta;
		event.width = width();
		event.height = height;
	}

	private int idHeight(IDipUnit unit, Event event) {
		final TextLayout layout = new TextLayout(event.gc.getDevice());
		layout.setFont(font());
		int width = width() - IMAGE_WIDTH - 10;
		if (preferences().getAllignment() == ImageViewAllignment.VERTICAL_ORDER) {
			width = width() - 5;
		}
		String text = fontProvider().getWrapIDText(DipUtilities.relativeProjectID(unit), width);
		layout.setText(text);
		int idHeight = layout.getBounds().height;
		return idHeight;
	}

	private int descriptionHeight(IDipUnit unit, Event event) {
		String description = ((DipUnit) unit).getUnitDescription().fullLinkTitle();
		final TextLayout descrLayout = new TextLayout(event.gc.getDevice());
		descrLayout.setFont(font());
		int descWidth = width() - 5;
		if (descWidth > 0) {
			String text = fontProvider().getWrapText(description, descWidth);
			descrLayout.setText(text);
		} else {
			descrLayout.setText(description);
		}
		int descriptionHeight = descrLayout.getBounds().height;
		return descriptionHeight;
	}

	private void measureVerticalOrderItem(Event event) {
		TableItem item = (TableItem) event.item;
		int index = fTable.indexOf(item);
		IDipUnit unit = units().get(index);
		int idHeight = idHeight(unit, event);
		int descriptionHeight = descriptionHeight(unit, event);
		int height = IMAGE_WIDTH + descriptionHeight + idHeight;
		event.width = width();
		event.height = height;
	}

	// =======================
	// vertical paint

	private void verticalPaint(Event event) {
		if (preferences().getAllignment() == ImageViewAllignment.IMAGE_RIGHT) {
			paintRightImageItem(event);
		} else if (preferences().getAllignment() == ImageViewAllignment.VERTICAL_ORDER) {
			int parentWidth = width();
			paintVerticalOrderItem(event, parentWidth);
		} else {
			paintLeftImageItem(event);
		}
	}

	private void paintRightImageItem(Event event) {
		TableItem item = (TableItem) event.item;
		int index = fTable.indexOf(item);

		int parentWidth = width();
		int xStart = event.x + 5;
		IDipUnit unit = units().get(index);

		// prepare image
		Image image = image(item, unit);
		int imageX = IMAGE_WIDTH / 2;
		int imageY = IMAGE_WIDTH / 2;
		if (image != null) {
			imageX = (IMAGE_WIDTH - image.getBounds().width) / 2;
			imageY = (IMAGE_WIDTH - image.getBounds().height) / 2;
		}

		// id
		String id = DipUtilities.relativeProjectID(unit);
		TextLayout idlayout = new TextLayout(event.gc.getDevice());
		int idWidth = parentWidth - IMAGE_WIDTH - 5;
		if (idWidth < 100) {
			idWidth = 100;
		}

		String idText = fontProvider().getWrapIDText(id, idWidth);
		idlayout.setFont(font());
		idlayout.setText(idText);
		int idHeight = idlayout.getBounds().height;

		int idY = (IMAGE_WIDTH - idHeight) / 2;
		if (idY < 0) {
			idY = 5;
		}

		event.gc.setForeground(ColorProvider.IMAGE_VIEW_ID_GRAY);
		idlayout.draw(event.gc, xStart, event.y + idY);
		// image
		if (image != null) {
			event.gc.drawImage(image, event.x + parentWidth - IMAGE_WIDTH + imageX, event.y + imageY);
		}
		// descritpion
		String description = ((DipUnit) unit).getUnitDescription().fullLinkTitle();
		TextLayout layout = new TextLayout(event.gc.getDevice());
		layout.setFont(font());
		String descText = fontProvider().getWrapText(description, parentWidth - 5);
		layout.setText(descText);
		Color defaultColor = event.gc.getForeground();
		if (index != fTable.getSelectionIndex()) {
			event.gc.setForeground(ColorProvider.SELECT);
		}

		int descrY = event.y + IMAGE_WIDTH;
		if (idHeight > IMAGE_WIDTH) {
			descrY = event.y + idHeight + 10;
		}
		layout.draw(event.gc, event.x + 5, descrY);
		event.gc.setForeground(defaultColor);
		// line
		event.gc.drawLine(xStart, event.y + event.height - 1, event.x + parentWidth - 5,
				event.y + event.height - 1/* lineY */);
	}

	private Image image(Item item, IDipUnit unit) {
		return imageProvider().getShowedImage(unit, IMAGE_WIDTH);
	}

	private void paintLeftImageItem(Event event) {
		TableItem item = (TableItem) event.item;
		int index = fTable.indexOf(item);
		int parentWidth = width();
		int xStart = event.x + 5;
		IDipUnit unit = units().get(index);

		// prepare image
		Image image = image(item, unit);
		if (image != null) {
			int imageX = (IMAGE_WIDTH - image.getBounds().width) / 2;
			int imageY = (IMAGE_WIDTH - image.getBounds().height) / 2;
			event.gc.drawImage(image, event.x + imageX, event.y + imageY);
		}

		// id
		String id = DipUtilities.relativeProjectID(unit);
		TextLayout idlayout = new TextLayout(event.gc.getDevice());

		int idWidth = parentWidth - IMAGE_WIDTH - 5;
		if (idWidth < 100) {
			idWidth = 100;
		}

		String idText = fontProvider().getWrapIDText(id, idWidth);
		idlayout.setFont(font());
		idlayout.setText(idText);

		int idHeight = idlayout.getBounds().height;
		int idY = (IMAGE_WIDTH - idHeight) / 2;
		if (idY < 0) {
			idY = 5;
		}

		event.gc.setForeground(ColorProvider.IMAGE_VIEW_ID_GRAY);
		idlayout.draw(event.gc, xStart + IMAGE_WIDTH, event.y + idY);
		// descritpion
		String description = ((DipUnit) unit).getUnitDescription().fullLinkTitle();
		TextLayout layout = new TextLayout(event.gc.getDevice());

		String descText = fontProvider().getWrapText(description, parentWidth - 5);
		layout.setText(descText);
		layout.setFont(font());

		// layout.setText(description);

		Color defaultColor = event.gc.getForeground();
		if (index != fTable.getSelectionIndex()) {
			event.gc.setForeground(ColorProvider.SELECT);
		}
		// layout.setWidth(parentWidth - 5);
		int descrY = event.y + IMAGE_WIDTH;
		if (idHeight > IMAGE_WIDTH) {
			descrY = event.y + idHeight + 10;
		}
		layout.draw(event.gc, event.x + 5, descrY);
		event.gc.setForeground(defaultColor);
		// line
		event.gc.drawLine(xStart, event.y + event.height - 1, event.x + parentWidth - 5,
				event.y + event.height - 1/* lineY */);
	}

	private void paintVerticalOrderItem(Event event, int parentWidth) {
		TableItem item = (TableItem) event.item;
		int index = fTable.indexOf(item);
		IDipUnit unit = units().get(index);
		int xStart = event.x + 5;
		// draw id
		String id = DipUtilities.relativeProjectID(unit);
		TextLayout idlayout = new TextLayout(event.gc.getDevice());
		String idText = fontProvider().getWrapIDText(id, parentWidth - 5);
		idlayout.setFont(font());
		idlayout.setText(idText);
		int idHeight = idlayout.getBounds().height;

		event.gc.setForeground(ColorProvider.IMAGE_VIEW_ID_GRAY);
		idlayout.draw(event.gc, xStart, event.y);
		// draw image
		Image image = image(item, unit);
		if (image != null) {
			int imageX = (IMAGE_WIDTH - image.getBounds().width) / 2;
			int imageY = (IMAGE_WIDTH - image.getBounds().height) / 2;
			event.gc.drawImage(image, xStart + imageX, event.y + idHeight + imageY);
		}
		// descritpion
		String description = ((DipUnit) unit).getUnitDescription().fullLinkTitle();
		TextLayout layout = new TextLayout(event.gc.getDevice());
		String descText = fontProvider().getWrapText(description, parentWidth - 5);
		layout.setText(descText);

		Color defaultColor = event.gc.getForeground();
		if (index != fTable.getSelectionIndex()) {
			event.gc.setForeground(ColorProvider.SELECT);
		}
		layout.setFont(font());
		int descrY = event.y + IMAGE_WIDTH + idHeight;
		layout.draw(event.gc, event.x + 5, descrY);
		event.gc.setForeground(defaultColor);
		// line
		event.gc.drawLine(xStart, event.y + event.height - 1, event.x + parentWidth - 5,
				event.y + event.height - 1/* lineY */);
	}

	// ======================
	// services

	private List<IDipUnit> units() {
		return fImageView.getUnits();
	}

	private int width() {
		return fImageView.getWidth();
	}

	private IImageViewPreferences preferences() {
		return fImageView.getPreferences();
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

}
