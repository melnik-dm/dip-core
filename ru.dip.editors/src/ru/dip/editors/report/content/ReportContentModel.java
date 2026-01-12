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
package ru.dip.editors.report.content;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.swt.graphics.Point;

import de.kupzog.ktable.KTableCellEditor;
import de.kupzog.ktable.KTableCellRenderer;
import de.kupzog.ktable.KTableModel;
import ru.dip.core.model.interfaces.IDipDocumentElement;
import ru.dip.core.model.interfaces.IDipElement;
import ru.dip.core.model.interfaces.IDipParent;
import ru.dip.core.model.interfaces.IUnitPresentation;
import ru.dip.core.report.checker.ReportRuleSyntaxException;
import ru.dip.core.report.model.report.ReportEntry;
import ru.dip.core.unit.UnitType;
import ru.dip.editors.report.ReportEditor;
import ru.dip.editors.report.content.model.RceEntryElement;
import ru.dip.editors.report.content.model.ReportEntryPresentation;
import ru.dip.editors.report.content.render.ReportHeaderCellRender;
import ru.dip.editors.report.content.render.ReportIDCellRender;
import ru.dip.editors.report.content.render.ReportIDPainter;
import ru.dip.editors.report.content.render.ReportPresentationCellRender;
import ru.dip.editors.report.content.render.ReportPresentationPainter;
import ru.dip.ktable.DipTable;
import ru.dip.ui.table.ktable.celleditors.CellInfo;
import ru.dip.ui.table.ktable.model.IDipTableElement;
import ru.dip.ui.table.ktable.model.IDipTableModel;
import ru.dip.ui.table.ktable.render.PaintPresentationUtils;
import ru.dip.ui.table.table.TableModel;

public class ReportContentModel implements KTableModel, IDipTableModel {
	
	public static final int DEFAULT_HEIGHT = 30;
	
	// renderer & painters
	private ReportIDCellRender fIdRender;
	private ReportPresentationCellRender fPresentationRender;
	private ReportHeaderCellRender fHeaderRender;
	private ReportPresentationPainter fPainter;
	private ReportIDPainter fIDPainter;
	
	private final ReportEditor fEditor;
	private ReportContentComposite fTableComposite;
	
	// номера столбцов
	private int fColumnCount = 2;

	// ширина столбцов
	private int fIdWidth = 300;
	private int fPresentationWidth = 600;
	private int fCommentWidth = 0;
	
	private int fIdColumnNumber = 0;
	private int fPresentationColumnNumber = 1;
	private int fCommentColumnNumber = 2;
	
	// номер и высота ячейки открытой для редактирования
	private int fOpenEditingRow = -1;   // 
	private int fCellEditorHeight = 0;
	
	// первая ячейка и отступ (для IDipTableModel)
	private int fFirstCell = -1;
	private int fIndent = 0;

	private boolean fNeedUpdate = false;
	private final DipTable fTable;

	private List<IDipTableElement> fElements = new ArrayList<>(); 
	
	public ReportContentModel(DipTable table, ReportEditor editor, ReportContentComposite tableComposite) {
		fTable = table;
		fEditor = editor;
		fTableComposite = tableComposite;	
		fIdRender = new ReportIDCellRender(this);
		fPainter = new ReportPresentationPainter(fTableComposite);
		fIDPainter = new ReportIDPainter(fTableComposite);
		fPresentationRender = new ReportPresentationCellRender(this);
		fHeaderRender = new ReportHeaderCellRender(this);		
		computeElements();
	}
	
	public void computeElements() {				
		fElements = new ArrayList<>(); 
		
		IDipParent parent = fEditor.getDipProject();		
		if (parent == null){
			return;
		}							
		List<ReportEntry> entries = fEditor.getEntries();
		for (ReportEntry entry: entries) {
			ReportEntryPresentation reportPresentation = new ReportEntryPresentation(entry);			
			RceEntryElement entryElement = new RceEntryElement(this, reportPresentation);			
			fElements.add(entryElement);		
			try {
				entryElement.computeChildren();
				fElements.addAll(entryElement.children());
			} catch (ReportRuleSyntaxException e) {
				// добавить ошибочный элемент
				e.printStackTrace();
			}
		}		
	}
	
	@Override
	public int getRowLocation(int row) {
		int y = 0;
		for (int i = 0; i < row + 1; i++) {
			y+= getFullRowHeight(i);
		}		
		return y;
	}
		
	@Override
	public boolean isCtrlPressed() {
		return tableComposite().isCtrlPressed();
	}

	@Override
	public void setEditRow(int row) {
		fOpenEditingRow = row;		
	}
		
	@Override
	public Object getContentAt(int col, int row) {
		if (row < 1) {
			return null;
		}
		if (row == 0) {
			return "header"; //$NON-NLS-1$
		}
		return fElements.get(row - 1);
	}


	@Override
	public KTableCellRenderer getCellRenderer(int col, int row) {		
		if (row == 0) {
			return fHeaderRender;
		}
		if (col == fIdColumnNumber) {
			return fIdRender;
		} else if (col == fPresentationColumnNumber) {
			return fPresentationRender;
		}
		return null;
	}

	@Override
	public Point belongsToCell(int col, int row) {
		if (col != presentationColumnNumber() && row > 0) {
			IDipTableElement element = fElements.get(row - 1);			
			IDipTableElement prev = element;
			if (prev.isAbstractField()) {
				if (element.isFirst()) {
					return null;
				}
				row--;
				prev = fElements.get(row - 1);
				while (prev.isAbstractField() && !prev.isFirst()) {
					row--;
					prev = fElements.get(row - 1);
				}
				
				return new Point(col,row);				
			}
		}
		return null;	
	}

	@Override
	public int getRowCount() {
		return fElements.size() + 1;
	}

	@Override
	public int getColumnCount() {
		return fColumnCount;
	}

	//========================
	// 1-я строка и доступ к ней
	
	@Override
	public void clearFirstElement() {
		fFirstCell = -1;
		fIndent = 0;
	}
	
	@Override
	public int firstCell() {
		return fFirstCell;
	}
	
	@Override
	public int indent() {
		return fIndent;
	}
		
	@Override
	public void setFirstCell(int firstCell) {
		fFirstCell = firstCell;
	}
	
	@Override
	public void setIndent(int indent) {
		fIndent = indent;
	}

	//==========================
	// size
	
	public int tableWidth() {
		return fTable.getBounds().width;
	}
	
	/**
	 * Проверка ширины столбцов, относительно общей ширины таблицы
	 */
	public void checkWidth() {
		if (tableWidth() > fIdWidth + fPresentationWidth + fCommentWidth + 5) {
			fPresentationWidth = tableWidth() - fIdWidth - fCommentWidth;		
		} else if (tableWidth() < fIdWidth + fPresentationWidth + fCommentWidth) {
			fPresentationWidth = tableWidth() - fIdWidth - fCommentWidth;
			if (fPresentationWidth < 60) {
				fPresentationWidth = 60;
				if (isShowId()) {
					fIdWidth = tableWidth() - fPresentationWidth - fCommentWidth;
					if (fIdWidth < 60) {
						fIdWidth = 60;
					}
				}
				if (isShowComment()) {
					fCommentWidth = tableWidth() - fPresentationWidth - fIdWidth;
					if (fCommentWidth < 60) {
						fCommentWidth = 60;
					}
				}
			}	
		}	
		if (fPresentationWidth < 60) {
			fPresentationWidth = 60;
			if (isShowId()) {
				fIdWidth = tableWidth() - fPresentationWidth - fCommentWidth;
			}
		}			
	}
	
	@Override
	public void setColumnWidth(int column, int width) {		
		if (!fTableComposite.isCtrlPressed()) {
			return;
		}
		
		fNeedUpdate = true;
		
		// установить ширину столбца
		if (column == 0) {
			fIdWidth = width;
			fPresentationWidth = tableWidth() - fIdWidth - fCommentWidth;
		} else if (column == 1) {
			fPresentationWidth = width;
			fCommentWidth = tableWidth() - fIdWidth - fPresentationWidth;
		}

		checkWidth();

		if (fPresentationWidth < 0) {
			throw new RuntimeException();
		}
	}

	@Override
	public int getColumnWidth(int col) {
		if (col == fIdColumnNumber) {
			return fIdWidth;
		} else if (col == fPresentationColumnNumber) {
			return fPresentationWidth;
		} else {
			return fCommentWidth;
		}
	}
	
	@Override
	public int getRowHeight(int row) {	
		if (row < 0 || row > fElements.size()) {
			return DEFAULT_HEIGHT;
		}
		// header
		if (row == 0) {
			return DEFAULT_HEIGHT;
		}
	
		IDipTableElement element = fElements.get(row - 1);
		if (element == null) {
			return DEFAULT_HEIGHT;
		}
		
		int height = element.height(this);
		
		if (row == fOpenEditingRow) {
			return Math.max(fCellEditorHeight, height);
		}
			
		if (row == fFirstCell) {
			height = height - fIndent;
		}
		
		if (height >= 0) {
			return height;
		}
		return DEFAULT_HEIGHT;
	}
	
	@Override
	public int getFullRowHeight(int row) {		
		if (row < 0 || row > fElements.size()) {
			return DEFAULT_HEIGHT;
		}
		// header
		if (row == 0) {
			return DEFAULT_HEIGHT;
		}
	
		IDipTableElement element = fElements.get(row - 1);
		if (element == null) {
			return 40;
		}
		
		int height = element.height(this);
		if (height >= 0) {
			return height;
		}
		return 40;
	}
	
	@Override
	public void setEditRowHeight(int cellEditorHeight) {
		fCellEditorHeight = cellEditorHeight;
	}

	//=========================
	// update
	
	public void updateAllElements() {
		updateElements(fElements);
	}
	
	/**
	 * Обновляет элементы
	 */
	public void updateElements(List<IDipTableElement> elements) {
		// подготовка идентификаторов
		if (isShowId()) {
			elements.parallelStream()
				.map(IDipTableElement.class::cast)
				.forEach(ReportContentModel.this::prepareID);
		}

		// подготовка Presentation (что можно делать вне UI-потока обрабатываем параллельно
		// где создаются объекты Image, нужен Display, обрабатываем последовательно
		List<IDipTableElement> paralles = new ArrayList<>();
		List<IDipTableElement> notParallel = new ArrayList<>();
		for (IDipTableElement element: elements) {
			if (element.isPresentation()) {
				IDipDocumentElement dde = element.dipDocElement();
				if (dde instanceof IUnitPresentation) {
					IUnitPresentation up = (IUnitPresentation) dde;
					UnitType type = up.getUnitType();
					if (type.isImageType() || type.isHtmlType() || type == UnitType.CSV) {
						notParallel.add(element);
						continue;
					}					
				}
			}
			paralles.add(element);
			
		}
		paralles.parallelStream()
			.forEach(ReportContentModel.this::preparePresentation);
		notParallel.stream()
			.forEach(ReportContentModel.this::preparePresentation);
	}

	
	private void prepareID(IDipTableElement element) {
		if (isShowId()) {
			idPainter().prepare(element);
		}	
	}
	
	private void preparePresentation(IDipTableElement element) {
		int indent = 0;
		if (fPresentationColumnNumber == 0 && !fTableComposite.getTableSettings().isOneListMode()) {
			indent = getIndent(element) * 16;
		}		
		if (indent > 0) {
			indent += 16;
		}
		element.prepare(presentationWidth() - indent, this);
	}
	
	public void checkNeedUpdateColumnWidth() {
		if (fNeedUpdate) {
			fNeedUpdate = false;
			fTableComposite.asyncRefreshTree();
		}
	}
	
	public void updateTableFont() {
		if (isShowId()) {
			updateIDColumn();
		}
		updatePresentationFont();
	}
	
	public void updatePresentationFont() {
		fElements.stream()
			.filter(PaintPresentationUtils::hasFontPresentation)
			.forEach(this::updatePresentation);
	}
	
	private void updatePresentation(IDipTableElement element) {
		int indent = 0;
		if (fPresentationColumnNumber == 0 && !fTableComposite.getTableSettings().isOneListMode()) {
			indent = getIndent(element) * 16;
		}		
		if (indent > 0) {
			indent += 16;
		}
		element.prepare(presentationWidth() - indent, this);		
	}
	
	private void updateIDColumn() {
		List<IDipTableElement> elementsToUpdate = fElements.stream()
				.filter(IDipTableElement::isVisible)
				.collect(Collectors.toList());
		elementsToUpdate.stream().forEach(element -> idPainter().onlyMeasureElements((IDipTableElement) element));
	}
	
	//===============================
	
	public ReportContentComposite tableComposite() {
		return fTableComposite;
	}
	
	public IDipParent getDipProject() {
		return fEditor.getDipProject();
	}
	
	public int idColumnNumber() {
		return fIdColumnNumber;
	}

	public int presentationColumnNumber() {
		return fPresentationColumnNumber;
	}

	public int commentColumnNumber() {
		return fCommentColumnNumber;
	}
	
	//=========================
	// NOT USED
	
	@Override
	public String getTooltipAt(int col, int row) {
		return null;
	}

	@Override
	public int getFixedHeaderColumnCount() {
		return 0;
	}

	@Override
	public int getFixedHeaderRowCount() {
		return 1;
	}

	@Override
	public int getFixedSelectableRowCount() {
		return 0;
	}
	
	@Override
	public int getFixedSelectableColumnCount() {
		return 0;
	}
	
	@Override
	public int getRowHeightMinimum() {
		return 0;
	}
	
	@Override
	public boolean isColumnResizable(int col) {
		return true;
	}
	
	@Override
	public boolean isRowResizable(int row) {
		return false;
	}
	
	@Override
	public void setContentAt(int col, int row, Object value) {
		
	}
	
	@Override
	public void setRowHeight(int row, int value) {
	}
	
	@Override
	public KTableCellEditor getCellEditor(int col, int row) {
		return null;
	}

	@Override
	public boolean isShowId() {
		return true;
	}

	@Override
	public boolean isShowComment() {
		return false;
	}

	public int idWidth() {
		return fIdWidth;
	}

	public ReportIDPainter idPainter() {
		return fIDPainter;
	}

	public boolean isLastDipDocElement(IDipElement dipUnit) {
		return false;
	}

	public int commentWidth() {
		return fCommentWidth;
	}
	
	public int presentationWidth() {
		return fPresentationWidth;
	}

	public int getIndent(IDipTableElement tableElement) {
		return 0;
	}

	public boolean isParent(IDipDocumentElement dipDocElement) {
		return false;
	}

	public ReportPresentationPainter reqPainter() {
		return fPainter;
	}

	public void setPresentationWidth(int width) {
		fPresentationWidth = width;
	}

	//======================
	// stub

	@Override
	public DipTable getTable() {
		return null;
	}

	@Override
	public CellInfo getEditedCellInfo() {
		return null;
	}

	@Override
	public void setEditedCellInfo(Object cellInfo) {}
	
	@Override
	public void saveUnit(String startContent, String newContent, Object dipDocElement) {}

	@Override
	public TableModel getTableModel() {
		return null;
	}



}
