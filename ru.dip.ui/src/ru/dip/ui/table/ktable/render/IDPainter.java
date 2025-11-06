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
import java.util.stream.Collectors;

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.TextLayout;

import ru.dip.core.model.DipFolder;
import ru.dip.core.model.DipProject;
import ru.dip.core.model.IncludeFolder;
import ru.dip.core.model.finder.IFindedIdPoints;
import ru.dip.core.model.interfaces.IDipDocumentElement;
import ru.dip.core.model.interfaces.IDipDocumentElementHolder;
import ru.dip.core.model.interfaces.IDipElement;
import ru.dip.core.model.interfaces.IDipParent;
import ru.dip.core.model.interfaces.IDipUnit;
import ru.dip.core.model.interfaces.IMarkable;
import ru.dip.core.model.interfaces.IUnitExtension;
import ru.dip.core.unit.UnitDescriptionPresentation;
import ru.dip.core.unit.UnitPresentation;
import ru.dip.core.unit.form.AbstractFormField;
import ru.dip.core.unit.form.FormField;
import ru.dip.core.utilities.DipUtilities;
import ru.dip.core.utilities.GITUtilities;
import ru.dip.core.utilities.TagStringUtilities;
import ru.dip.core.utilities.ui.gc.GCUtils;
import ru.dip.core.utilities.ui.swt.ColorProvider;
import ru.dip.ui.table.ktable.KTableComposite;
import ru.dip.ui.table.ktable.model.ContentProvider;
import ru.dip.ui.table.ktable.model.ContentType;
import ru.dip.ui.table.ktable.model.IContentContainer;
import ru.dip.ui.table.ktable.model.IDipTableElement;
import ru.dip.ui.table.ktable.model.ITableComposite;
import ru.dip.ui.table.table.TableSettings;
import ru.dip.ui.utilities.image.ImageProvider;

public class IDPainter {
	
	protected static final int INDENT_AFTER_ICON = 2;
	
	private static ContentProvider contentProvider = ContentProvider.ID_CONTENT_PROVIDER;

	private ITableComposite fTableComposite;

	public IDPainter(ITableComposite tableComposite) {
		fTableComposite = tableComposite;
	}

	// =========================================

	public void prepare(IDipTableElement element) {
		update(element);
		int height = measure(contentProvider.getText(element));
		contentProvider.setHeight(element, height);
		updateIdPoints(element);
	}
	
	public void onlyMeasureElements(IDipTableElement element) {
		int height = measure(contentProvider.getText(element));
		contentProvider.setHeight(element, height);
	}
	
	private void update(IDipTableElement element) {
		IDipDocumentElement dipDocElement = element.dipDocElement();
		if (dipDocElement instanceof UnitPresentation) {
			simpleUpdateCell(element, dipDocElement);
		} else if (dipDocElement instanceof UnitDescriptionPresentation) {
			updateCell((UnitDescriptionPresentation) dipDocElement, element);
		} else if (dipDocElement instanceof AbstractFormField) {
			simpleUpdateCell(element, dipDocElement);
		} else if (dipDocElement instanceof DipFolder) {
			updateCell((DipFolder) dipDocElement, element);
		} else if (dipDocElement instanceof DipProject) {
			updateCell((DipProject) dipDocElement, element);
		}
	}

	private void updateCell(DipFolder folder, IContentContainer element) {
		simpleUpdateCell(element, folder);
		// серый цвет для родительских директорий
		if (!isTable(folder) && !isChild(folder)) {
			contentProvider.setForeground(element, ColorProvider.GRAY);
		}
	}

	private String getIncludeFolderLabel(IncludeFolder includeFolder) {
		if (fTableComposite.isOneListMode()) {
			String result = includeFolder.realtiveProjectIncludeName();
			return result;
		} else {
			return includeFolder.includeName();
		}
	}

	protected String wrapIDText(String text) {
		if (!TableSettings.isWrapIdEnable()) {
			return text;
		}
		String separator = "/"; //$NON-NLS-1$
		return text.replaceAll(separator, separator + "\n"); //$NON-NLS-1$
	}

	private Image getNavigatorImage(IDipDocumentElement dipDocElement) {
		if (dipDocElement instanceof IUnitExtension || dipDocElement instanceof IDipUnit) {
			return ImageProvider.FILE;
		} else if (dipDocElement instanceof IDipParent) {
			return ImageProvider.FOLDER;
		}
		return null;
	}
	
	/**
	 * Обновляем id-points (т.к. они посчитаны для имени файла, 
	 * а во флэт-режиме добавляется весь id, происходит смещение
	 */
	protected void updateIdPoints(IContentContainer element) {
		IDipDocumentElement dipDocElement = getPresentation(element);
		contentProvider.put(element, ContentType.FINDED_POINTS, null);
		
		if (fTableComposite.isFindMode() && getIdPoints(dipDocElement) != null) {
			List<Point> namePoints = getIdPoints(dipDocElement);
			if (fTableComposite.isOneListMode()) {
				int delta = contentProvider.getText(element).length() - dipDocElement.name().length();
				if (delta > 0) {
					contentProvider.put(element, ContentType.FINDED_POINTS, namePoints.stream()
							.map(p -> new Point(p.x + delta, p.y + delta))
							.collect(Collectors.toList()));
					return;
				} 
			}
			contentProvider.put(element, ContentType.FINDED_POINTS, namePoints);
		}
	}
	
	private List<Point> getIdPoints(IDipDocumentElement dipDocElement){
		if (dipDocElement instanceof IFindedIdPoints) {
			return ((IFindedIdPoints) dipDocElement).getFindedIdPoints();
		}
		return null;
	}

	// =======================================
	// DipUnitDescription

	private void updateCell(UnitDescriptionPresentation description, IContentContainer tableElement) {
		contentProvider.setText(tableElement, "");
	}

	// =======================================
	// DipProject

	private void updateCell(DipProject project, IContentContainer tableElement) {
		simpleUpdateCell(tableElement, project);
		if (!isTable(project)) {
			contentProvider.setForeground(tableElement, ColorProvider.GRAY);
		}
	}

	protected void simpleUpdateCell(IContentContainer tableElement, IDipDocumentElement dipDocElement) {		
		String text = getFirstColumnText(dipDocElement);
		text = wrapIDText(text);
		updateCell(tableElement, text, getNavigatorImage(dipDocElement));
	}
	
	private String getFirstColumnText(IDipDocumentElement dipDocElement) {
		if (dipDocElement instanceof IncludeFolder) {
			return getIncludeFolderLabel((IncludeFolder) dipDocElement);
		}
		if (fTableComposite.isOneListMode()) {
			return getRelativeProjectName(dipDocElement);
		}
		return getDipDocElementName(dipDocElement);
	}

	private String getRelativeProjectName(IDipDocumentElement dipDocElement) {
		String relativeName = DipUtilities.relativeProjectID(dipDocElement);
		if (isNeedAppendHash(dipDocElement)) {
			return getNameWithHash(dipDocElement, relativeName);
		} else {
			return relativeName;
		}
	}
	
	private String getDipDocElementName(IDipDocumentElement dipDocElement) {
		if (isNeedAppendHash(dipDocElement)) {
			return getNameWithHash(dipDocElement, dipDocElement.name());
		}
		return dipDocElement.name();
	}
	
	
	private boolean isNeedAppendHash(IDipDocumentElement dde) {
		return (dde instanceof FormField || dde instanceof UnitPresentation) 
				&& fTableComposite.isShowFormVersion();
	}
	
	private String getNameWithHash(IDipDocumentElement dde, String name) {
		String hash = GITUtilities.getDescribe(dde);
		if (hash != null) {
			return name + "@" + hash;
		}
		return name;
	}
	
	
	protected void updateCell(IContentContainer tableElement, String text, Image image) {
		contentProvider.setText(tableElement, text);		
		contentProvider.setImage(tableElement, image);
		contentProvider.setFont(tableElement, TableSettings.idFont());
	}
	
 	protected int measure(String itemText) {
		if (itemText == null) {
			itemText = ""; //$NON-NLS-1$
		}

		int height =  TableSettings.idFontDimension().getTextHeight(itemText);	
		height += TableSettings.versionMarginTop() + TableSettings.versionMarginBottom();
		return height;
	}

	// =======================
	// paint

	public void paint(int width, GC gc, Rectangle rect, IContentContainer tableElement) {
		IDipDocumentElement dipDocElement = ((IDipDocumentElementHolder)tableElement).dipDocElement();
		Image image = getNavigatorImage((IDipDocumentElement) dipDocElement);
		String itemText = contentProvider.getText(tableElement);
		if (itemText == null) {
			itemText = ""; //$NON-NLS-1$
		}
		int y = computeY(image, itemText, rect);
		if (y < 0) {
			return;
		}
		
		int x = rect.x;	
		if (!(dipDocElement instanceof UnitDescriptionPresentation)) {
			if (((IDipDocumentElementHolder)tableElement).dipDocElement().strong() instanceof IMarkable) {
				IMarkable markable = (IMarkable) ((IDipDocumentElementHolder)tableElement).dipDocElement().strong();
				for (int markNumber = 0; markNumber < IMarkable.MARKS_SIZE; markNumber++) {
					if (markable.isMark(markNumber)) {
						gc.drawImage(ImageProvider.BOOKMARKS[markNumber], x, y);
						x += ImageProvider.BOOKMARKS[markNumber].getBounds().width - 4;  // можно обрезать картинки для меток (не нужно будет отступать 4 пикселя)
					}									
				}							
			}

			gc.drawImage(image, x, y);
			x += image.getBounds().width;
		}
		TextLayout layout = createTextLayout(itemText, width, gc); 				
		applyTextColor(layout, dipDocElement, itemText);	
		applyFindPoints(tableElement, dipDocElement, layout);
		
		x += INDENT_AFTER_ICON; 
		drawTextAndDispose(layout, gc, x, y);
	}
	
	protected int computeY(Image image, String itemText, Rectangle rect) {
		int startY = Math.max(rect.y, 0); // если отрицательное значение, выставляем ноль (видимый Y)
		int height = Math.min(rect.height, fTableComposite.tableHeight() - startY); // видимая высота
																									// ячейки
		int stringCounter = 0;
		stringCounter = itemText.split(TagStringUtilities.LINE_SPLIT_REGEX).length;
		int heightText = TableSettings.idFontDimension().charHeight() * stringCounter;

		if (image == null || rect == null) {
			return - 1;
		}
		return startY + Math.max(TableSettings.versionMarginTop(), (height - heightText) / 2);	
	}
	
	protected TextLayout createTextLayout(String text, int width, GC gc) {
		return GCUtils.createTextLayout(text, width, gc, TableSettings.idFont());
	}
	
	protected void drawTextAndDispose(TextLayout layout,  GC gc, int x, int y) {
		//int x = rect.x + ximage.getBounds().width + 2;		
		layout.draw(gc, x, y);
		layout.dispose();
	}
	
	private void applyFindPoints(IContentContainer tableElement, IDipDocumentElement dipDocElement, TextLayout layout) {
		if (fTableComposite.isFindMode() && contentProvider.get(tableElement, ContentType.FINDED_POINTS, List.class) != null) {
			@SuppressWarnings("unchecked")
			List<Point> findedPoints = contentProvider.get(tableElement, ContentType.FINDED_POINTS, List.class); 
			applyFindedPositionStyles(layout, findedPoints);
			applyCurrentFindPositionStyle(layout, dipDocElement.strong(), findedPoints);
		}
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
	
	private void applyCurrentFindPositionStyle(TextLayout layout, IDipDocumentElement dipDocElement, List<Point> findedPoints) {
		if (fTableComposite.getFinder() != null && fTableComposite.getFinder().isCurrent(dipDocElement)) {
			int numberCurrentPoint = fTableComposite.getFinder().getCurrentPointNumber();
			if (numberCurrentPoint >= 0 && numberCurrentPoint < findedPoints.size()) {
				Point p = findedPoints.get(numberCurrentPoint);
				layout.setStyle(GCUtils.FIND_CURRENT_POINT_STYLE, p.x, p.y - 1);
			}
		}
	}
		
	private void applyTextColor(TextLayout layout, IDipDocumentElement dipDocElement, String itemText) {
		if (isGrayStyle(dipDocElement)) {
			layout.setStyle(GCUtils.GRAY_TEXT_STYLE, 0, itemText.length() - 1);
		} else {
			layout.setStyle(GCUtils.BLACK_TEXT_STYLE, 0, itemText.length() - 1);
		}
	}
	
	/**
	 * Для родительских директорий (цвет текста серый)
	 */
	protected boolean isGrayStyle(IDipDocumentElement dipDocElement) {
		return dipDocElement instanceof IDipParent 
			&& !isTable(dipDocElement)
			&& !isChild(dipDocElement);
	}
	
	protected boolean isTable(IDipDocumentElement dipDocElement) {
		return ktableComposite().model().isTable((IDipElement) dipDocElement);
	}
	
	protected boolean isChild(IDipDocumentElement dipDocElement) {
		return ktableComposite().model().isChild((IDipElement) dipDocElement);
	}
	
	protected IDipDocumentElement getPresentation(IContentContainer element) {
		IDipDocumentElement req =  ((IDipDocumentElementHolder)element).dipDocElement();
		if (req instanceof IDipUnit) {
			return ((IDipUnit) req).getUnitPresentation();
		} 
		return req;
	}
	
	private KTableComposite ktableComposite() {
		return (KTableComposite) fTableComposite;
	}
	
}
