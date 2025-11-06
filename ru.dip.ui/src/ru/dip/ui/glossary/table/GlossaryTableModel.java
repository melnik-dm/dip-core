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
package ru.dip.ui.glossary.table;

import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;

import de.kupzog.ktable.KTable;
import de.kupzog.ktable.KTableCellEditor;
import de.kupzog.ktable.KTableCellRenderer;
import de.kupzog.ktable.KTableModel;
import de.kupzog.ktable.renderers.TextCellRenderer;
import ru.dip.core.model.glossary.GlossaryField;
import ru.dip.core.model.glossary.GlossaryFolder;
import ru.dip.ui.glossary.GlossaryComposite;

public class GlossaryTableModel implements KTableModel {
		
	private final TextPainter fPainter = new TextPainter(this);
	private final TextCellRenderer fTextRender = new TextCellRenderer(TextCellRenderer.INDICATION_FOCUS | SWT.BOLD);
	
	private final GlossaryFolder fGlossaryFolder;	
	private final KTable fTable;
	private final GlossaryComposite fGlossaryComposite;
	
	private List<GlossaryEntry> fGlossaryElements;
	private int fIdWidth = 150;
	private int fPresentationWidth = 300;
	private int fSelectRow = 0;

	public GlossaryTableModel(KTable table, GlossaryFolder glossFolder, GlossaryComposite glossComposite) {
		fTable = table;
		fGlossaryFolder = glossFolder;
		fGlossaryComposite = glossComposite;
		fGlossaryElements = fGlossaryFolder.getChildren().stream().map(GlossaryEntry::new).collect(Collectors.toList());		
		update();
	}
	
	public void fullUpdate() {
		fGlossaryElements = fGlossaryFolder.getChildren().stream()
				.filter(fGlossaryComposite::checkFieldByFilter)
				.map(GlossaryEntry::new).collect(Collectors.toList());
		update();
	}
	
	public void update() {
		if (tableWidth() == 0) {
			return;
		}
		
		checkColumnWidth();
		prepare();
	}
	
	private void prepare() {
		fGlossaryElements.forEach(this::preparePresentation);
	}
	
	private void preparePresentation(GlossaryEntry entry) {		
		fPainter.updateGlossaryPresentation(fIdWidth, fPresentationWidth, entry);
	}
	
	@Override
	public Object getContentAt(int col, int row) {
		if (row == 0) {
			return col == 0 ? "Термин" : "Определение";
		}
		return fGlossaryElements.get(row - 1);
	}
	
	//====================
	// rows & columns
	
	@Override
	public int getRowCount() {
		return fGlossaryElements.size() + 1;
	}

	@Override
	public int getFixedHeaderRowCount() {
		return 1;
	}
	
	@Override
	public int getColumnCount() {
		return 2;
	}
	
	@Override
	public KTableCellRenderer getCellRenderer(int col, int row) {
		if (row > 0) {
			return fPainter;
		}
		
		return fTextRender;
	}

	//====================
	// width
	
	public void checkColumnWidth() {
		fPresentationWidth = tableWidth() - fIdWidth; 
	}
	
	public int tableWidth() {		
		return fTable.getBounds().width;
	}

	@Override
	public int getColumnWidth(int col) {
		return col == 0 ? fIdWidth : fPresentationWidth;
	}

	@Override
	public void setColumnWidth(int col, int width) {
		if (col == 0) {
			fIdWidth = width;
		} else if (col == 1) {
			fPresentationWidth = width;
		}
		update();
	}
	
	@Override
	public boolean isColumnResizable(int col) {
		return true;
	}
	
	//======================
	// height
	
	@Override
	public int getRowHeight(int row) {
		if (row == 0) {
			return 30;		
		}
		if (row < 0 || fGlossaryElements.size() < row) {
			return 0;
		}
		return fGlossaryElements.get(row - 1).getHeight();
	}


	@Override
	public int getRowHeightMinimum() {
		return 30;
	}

	@Override
	public void setRowHeight(int row, int value) {}
	
	//=====================
	// selection
	
	public void select(int col, int row) {
		fSelectRow = row;		
	}
	
	public GlossaryField getSelectedField() {
		if (fSelectRow == 0) {
			return null;
		}
		return fGlossaryElements.get(fSelectRow - 1).getField();
	}
		
	public int getSelectRow() {
		return fSelectRow;
	}

	public boolean isUnused(GlossaryEntry entry) {
		return fGlossaryComposite.isUnused(entry.getField());
	}
	
	//========================
		
	@Override
	public Point belongsToCell(int col, int row) {
		return null;
	}
	
	@Override
	public int getFixedSelectableRowCount() {
		return 0;
	}

	@Override
	public int getFixedHeaderColumnCount() {
		return 0;
	}

	@Override
	public int getFixedSelectableColumnCount() {
		return 0;
	}
	
	@Override
	public boolean isRowResizable(int row) {
		return false;
	}
	
	@Override
	public String getTooltipAt(int col, int row) {
		return null;
	}

	@Override
	public KTableCellEditor getCellEditor(int col, int row) {
		return null;
	}
	
	@Override
	public void setContentAt(int col, int row, Object value) {}
	
}
