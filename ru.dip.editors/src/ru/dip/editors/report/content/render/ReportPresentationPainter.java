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
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.TextLayout;

import ru.dip.core.model.interfaces.IDipDocumentElement;
import ru.dip.core.model.interfaces.IUnitDescription;
import ru.dip.core.model.interfaces.IUnitPresentation;
import ru.dip.core.unit.TablePresentation;
import ru.dip.core.unit.form.AbstractFormField;
import ru.dip.core.utilities.ui.gc.GCUtils;
import ru.dip.core.utilities.ui.swt.FontDimension;
import ru.dip.editors.report.content.model.ReportEntryPresentation;
import ru.dip.ui.table.ktable.model.IContentContainer;
import ru.dip.ui.table.ktable.model.IDipTableElement;
import ru.dip.ui.table.ktable.model.ITableComposite;
import ru.dip.ui.table.ktable.render.PresentationPainter;
import ru.dip.ui.table.ktable.render.PaintPresentationUtils;
import ru.dip.ui.table.table.TableSettings;

public class ReportPresentationPainter extends PresentationPainter {		
		
	public ReportPresentationPainter(ITableComposite tableComposite){
		super(tableComposite);
	}
	
	@Override
	protected void prepare(int width, IDipTableElement presentationElement) {
		IDipDocumentElement dde = presentationElement.dipDocElement();
		if (dde instanceof IUnitPresentation) {
			TablePresentation tablePresentation = ((IUnitPresentation) dde).getPresentation();
			prepare(width, tablePresentation, (IContentContainer)presentationElement);
		} else if (dde instanceof AbstractFormField) {
			prepare(width, (AbstractFormField) dde, presentationElement);
		} else if (dde instanceof IUnitDescription) {
			prepare(width, (IUnitDescription) dde, presentationElement);
		} else if (dde instanceof ReportEntryPresentation) {
			prepareReportPresentation(width, (ReportEntryPresentation) dde, presentationElement);
		}
	}
	
	private void prepareReportPresentation(int width, ReportEntryPresentation reportEntry, IContentContainer element) {
		updateReportPresentation(width, reportEntry, element);		
		contentProvider.setHeight(element, measureReportPresentation(element, reportEntry));
	}
	
	protected void updateReportPresentation(int columnWidth, ReportEntryPresentation folder, IContentContainer element) {
		String text = folder.getEntry().getName();		
		int width = PaintPresentationUtils.getColumnWidth(columnWidth);
		text = FontDimension.getWrapText(text, TableSettings.boldPresentationFontDimension().getStringLength(width));	
		contentProvider.setText(element, text);
		contentProvider.setFont(element, TableSettings.boldPresentationFont());
	}
	
	protected int measureReportPresentation (IContentContainer element, ReportEntryPresentation folder){
		final String itemText = contentProvider.getText(element); 
		return PaintPresentationUtils.getTextHeight(TableSettings.boldPresentationFontDimension(), itemText);		
	}
	
	//=======================================
	// paint
	
	public void paintReportEntryPresentation(int width, GC gc, Rectangle rect, ReportEntryPresentation folder, IContentContainer element) {
		String itemText = contentProvider.getText(element); 

		if (itemText == null) {
			itemText = ""; //$NON-NLS-1$
		}		
	    final TextLayout layout = GCUtils.createTextLayout(itemText, width, gc, TableSettings.boldPresentationFont());
	    PaintPresentationUtils.drawTextAndDispose(layout, rect, gc);
	}
	
}
