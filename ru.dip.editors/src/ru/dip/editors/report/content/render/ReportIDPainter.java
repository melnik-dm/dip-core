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

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.TextLayout;

import ru.dip.core.model.interfaces.IDipDocumentElement;
import ru.dip.core.model.interfaces.IDipElement;
import ru.dip.core.unit.UnitDescriptionPresentation;
import ru.dip.core.unit.UnitExtension;
import ru.dip.core.unit.UnitPresentation;
import ru.dip.core.unit.form.AbstractFormField;
import ru.dip.core.unit.form.FormField;
import ru.dip.core.utilities.DipUtilities;
import ru.dip.core.utilities.GITUtilities;
import ru.dip.core.utilities.TagStringUtilities;
import ru.dip.core.utilities.ui.gc.GCUtils;
import ru.dip.editors.report.content.model.RceDipElement;
import ru.dip.editors.report.content.model.RceNotDocumentElement;
import ru.dip.ui.table.ktable.model.ContentProvider;
import ru.dip.ui.table.ktable.model.IContentContainer;
import ru.dip.ui.table.ktable.model.IDipTableElement;
import ru.dip.ui.table.ktable.model.ITableComposite;
import ru.dip.ui.table.table.TableSettings;
import ru.dip.ui.utilities.image.ImageProvider;

public class ReportIDPainter {
	
	protected static final int INDENT_AFTER_ICON = 2;

	private static ContentProvider contentProvider = ContentProvider.ID_CONTENT_PROVIDER;

	
	private ITableComposite fTableComposite;

	public ReportIDPainter(ITableComposite tableComposite) {
		fTableComposite = tableComposite;
	}

	// =========================================

	public void prepare(IDipTableElement element) {
		update(element);		
		int height = measure(contentProvider.getText(element));
		contentProvider.setHeight(element, height);
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
		} else if (element instanceof RceNotDocumentElement) {
			IDipElement dipElement = ((RceNotDocumentElement) element).getDipElement();
			simpleUpdateCell(element, dipElement);
		}
	}

	protected String wrapIDText(String text) {
		if (!TableSettings.isWrapIdEnable()) {
			return text;
		}
		String separator = "/"; //$NON-NLS-1$
		return text.replaceAll(separator, separator + "\n"); //$NON-NLS-1$
	}

	private Image getNavigatorImage(IDipElement dipDocElement) {
		if (dipDocElement instanceof UnitExtension) {
			return ImageProvider.FILE;
		}
		return null;
	}

	// =======================================
	// DipUnitDescription

	private void updateCell(UnitDescriptionPresentation description, IContentContainer tableElement) {
		contentProvider.setText(tableElement, "");
	}

	protected void simpleUpdateCell(IContentContainer tableElement, IDipElement dipDocElement) {				
		String text = getFirstColumnText(dipDocElement);
		text = wrapIDText(text);
		updateCell(tableElement, text, getNavigatorImage(dipDocElement));
	}
	
	private String getFirstColumnText(IDipElement dipDocElement) {
		return getRelativeProjectName(dipDocElement);
	}

	private String getRelativeProjectName(IDipElement dipDocElement) {
		String relativeName = DipUtilities.relativeProjectID(dipDocElement);
		if (isNeedAppendHash(dipDocElement)) {
			return getNameWithHash(dipDocElement, relativeName);
		} else {
			return relativeName;
		}
	}

	private boolean isNeedAppendHash(IDipElement dde) {
		return (dde instanceof FormField || dde instanceof UnitPresentation) 
				&& fTableComposite.getTableSettings().isShowFormVersion();
	}
	
	private String getNameWithHash(IDipElement dde, String name) {
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
		if (tableElement instanceof RceDipElement) {
			IDipDocumentElement dipDocElement = ((RceDipElement)tableElement).dipDocElement();
			Image image = ImageProvider.FILE;				
			
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
				gc.drawImage(image, x, y);
				x += image.getBounds().width;
			}			
			TextLayout layout = createTextLayout(itemText, width, gc); 				
			applyTextColor(layout, itemText);	
			
			x += INDENT_AFTER_ICON; 
			drawTextAndDispose(layout, gc, x, y);			
		} else if (tableElement instanceof RceNotDocumentElement) {
			Image image = ImageProvider.FILE;
			String itemText = contentProvider.getText(tableElement);
			if (itemText == null) {
				itemText = ""; //$NON-NLS-1$
			}
			
			int y = computeY(image, itemText, rect);
			if (y < 0) {
				return;
			}
			int x = rect.x;	
			gc.drawImage(image, x, y);
			x += image.getBounds().width;					
			TextLayout layout = createTextLayout(itemText, width, gc); 				
			applyTextColor(layout, itemText);	
			
			x += INDENT_AFTER_ICON; 
			drawTextAndDispose(layout, gc, x, y);			
		}
	}
	
	private int computeY(Image image, String itemText, Rectangle rect) {
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
	
	private TextLayout createTextLayout(String text, int width, GC gc) {
		return GCUtils.createTextLayout(text, width, gc, TableSettings.idFont());
	}
	
	private void drawTextAndDispose(TextLayout layout,  GC gc, int x, int y) {
		layout.draw(gc, x, y);
		layout.dispose();
	}
		
	private void applyTextColor(TextLayout layout, String itemText) {
		layout.setStyle(GCUtils.BLACK_TEXT_STYLE, 0, itemText.length() - 1);
	}
	
}
