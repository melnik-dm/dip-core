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

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;

import ru.dip.core.model.interfaces.IDipUnit;
import ru.dip.core.unit.CsvUnitPresentation;
import ru.dip.core.unit.HtmlImagePresentation;
import ru.dip.core.unit.TablePresentation;
import ru.dip.core.utilities.ui.image.ImageUtilities;
import ru.dip.ui.imageview.interfaces.IImageView;
import ru.dip.ui.imageview.interfaces.IImageViewPreferences;
import ru.dip.ui.table.table.TableSettings;
import ru.dip.ui.utilities.GlobalKeyListener;
import ru.dip.ui.utilities.dip.DipUnitManager;

public class ImageViewMouseAdapter extends MouseAdapter {

	private final IImageView fImageView;
	private volatile Shell fShell;
	private Image fTrimImage;
	private volatile boolean fDown; // зажата ли кнопка вниз

	public ImageViewMouseAdapter(IImageView imageView) {
		fImageView = imageView;
	}

	@Override
	public void mouseUp(MouseEvent e) {
		closeShell();
		fDown = false;
	}

	@Override
	public void mouseDown(MouseEvent e) {
		fDown = true;
		Point pt = new Point(e.x, e.y);
		TableItem item = getItem(pt);
		if (item == null) {
			return;
		}
		setSelection(item, pt);
		if (e.button == 1) {
			runShowImageThread(item, pt);
		}
	}
	
	/**
	 * Возвращает ячейку (в горизонтальном отображениии всегда возвращает первую ячейку)
	 */
	private TableItem getItem(Point pt) {
		if (preferences().isHorizontalMode()) {			
			if (table().getItemCount() > 0) {
				return table().getItem(0);							
			}
			return null;
		} else {
			return table().getItem(pt);
		}
	}
	
	private void setSelection(TableItem item, Point pt) {
		if (preferences().isHorizontalMode() && item != null) {
			fImageView.getImageSelector().setHorizontalSelection(item, pt);
		}
		if (!preferences().isHorizontalMode()) {
			fImageView.getImageSelector().setVerticalSelection(item);
		}
	}

	private void runShowImageThread(TableItem item, Point pt) {
		Thread thread = new Thread(() -> {
			try {
				Thread.sleep(300);
				Display.getDefault().asyncExec(() -> {
					if (fDown && fImageView.getImageSelector().isImagePoint(item, pt)) {
						showImage(item);
					}
				});
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
		});
		thread.start();
	}

	private synchronized void showImage(TableItem item) {
		if (fShell != null && !fShell.isDisposed() && fShell.isVisible()) {
			return;
		}

		IDipUnit unit = getDipUnit(item);
		int size = getHoverImageSize();
		final Display display = Display.getCurrent();
		Point location = getHoverImageLocation(item);

		fTrimImage = getScaleImage(unit, size);
		if (fTrimImage == null) {
			return;
		}

		Point imageComp = table().toDisplay(location);
		imageComp.x -= fTrimImage.getBounds().width;

		fShell = new Shell(display, SWT.ON_TOP);
		fShell.setSize(size, size);
		fShell.setLocation(imageComp);

		GridLayout gridLayout = new GridLayout();
		fShell.setLayout(gridLayout);

		Label imageLabel = new Label(fShell, SWT.NONE);
		if (fTrimImage != null) {
			imageLabel.setImage(fTrimImage);
		}

		imageLabel.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseUp(MouseEvent e) {
				closeShell();
			}
		});

		fShell.addFocusListener(new FocusAdapter() {

			@Override
			public void focusLost(FocusEvent e) {
				fShell.setVisible(false);
			}
		});

		GridData gd = new GridData(GridData.FILL_BOTH);
		gd.widthHint = size;
		gd.heightHint = size;
		fShell.pack();
		fShell.setVisible(true);
		fShell.setActive();
	}

	private IDipUnit getDipUnit(TableItem item) {
		int index = table().indexOf(item);
		if (preferences().isHorizontalMode()) {
			index = fImageView.getImageSelector().getSelectedColumn();
		}
		return fImageView.getUnits().get(index);
	}

	private int getHoverImageSize() {
		int size = 300;
		if (GlobalKeyListener.isShiftCtrl()) {
			size = 1200;
		} else if (GlobalKeyListener.isCtrl()) {
			size = 600;
		}
		return size;
	}

	private Point getHoverImageLocation(TableItem item) {
		int x = 0;
		if (preferences().isHorizontalMode()) {
			x = item.getBounds(fImageView.getImageSelector().getSelectedColumn()).x;
		} else if (preferences().getAllignment() == ImageViewAllignment.IMAGE_RIGHT) {
			x = item.getBounds().x + fImageView.getWidth() - 15;
		} else {
			x = item.getBounds().x;
		}
		int y = item.getBounds().y;
		return new Point(x, y);
	}

	private Image getScaleImage(IDipUnit unit, int width) {
		Image trimImage = null;
		TablePresentation presentation = unit.getUnitPresentation().getPresentation();
		if (presentation instanceof HtmlImagePresentation) {
			trimImage = ((HtmlImagePresentation) presentation).getExternalImage(width);
		} else if (presentation instanceof CsvUnitPresentation) {
			boolean wrapTable = TableSettings.isCsvColumnWidthByContent();
			trimImage = ((CsvUnitPresentation) presentation).getExternalImage(width, wrapTable);
		} else {
			Image image = presentation.getImage();
			trimImage = ImageUtilities.trimImage(width, image);
		}
		return trimImage;
	}

	private synchronized void closeShell() {
		if (fShell != null && !fShell.isDisposed()) {
			fShell.close();
		}
		if (fTrimImage != null && !fTrimImage.isDisposed()) {
			fTrimImage.dispose();
		}
	}

	@Override
	public void mouseDoubleClick(MouseEvent e) {
		if (preferences().isHorizontalMode()) {
			table().deselectAll();
		}
		dipUnitManager().openInDocument();
	}

	// ===================
	// services

	private IImageViewPreferences preferences() {
		return fImageView.getPreferences();
	}

	private Table table() {
		return fImageView.getTable();
	}

	private DipUnitManager dipUnitManager() {
		return fImageView.getDipUnitManager();
	}

}
