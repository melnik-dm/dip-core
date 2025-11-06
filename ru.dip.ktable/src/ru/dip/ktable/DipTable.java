/**
 * KTable.java
 *
 * Copyright (C) 2004 by Friederich Kupzog Elektronik & Software
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Authors: 
 * Friederich Kupzog,  fkmk@kupzog.de, www.kupzog.de/fkmk
 * Lorenz Maierhofer, lorenz.maierhofer@logicmindguide.com
 * 
 * Modifications:
 * 2025 Denis Melnik.
 * 2025 Ruslan Sabirov.
 * 2025 Andrei Motorin.
 * Table scrolling has been changed
 * 
 * This work is derived from the original source code of the de.kupzog.ktable project.
 */

package ru.dip.ktable;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.ScrollBar;
import org.eclipse.swt.widgets.Shell;

import de.kupzog.ktable.KTable;
import de.kupzog.ktable.KTableCellEditor;
import de.kupzog.ktable.KTableCellSelectionAdapter;
import de.kupzog.ktable.SWTX;
import ru.dip.ktable.model.IKTableModel;

public final class DipTable extends KTable {
	
	public static enum RowVisible {
		FULL, ONLY_BOTTOM, ONLY_TOP, NOT_VISIBLE
	}
	
	private int fThumb = 60;
	
    public DipTable(Composite parent, int style) {
        super(parent, SWT.NO_BACKGROUND | SWT.NO_REDRAW_RESIZE | style);
    }

    /**
     * Создаются слушатели
     * Переделан слушатель getVerticalBar().addSelectionListener
     * Определяем верхний ряд, отрисовываем
     */
    @Override
    protected void createListeners() {
    	    
    	
        addPaintListener(new PaintListener() {
            public void paintControl(PaintEvent event) {
                onPaint(event);
            }
        });
        addControlListener(new ControlAdapter() {
            public void controlResized(ControlEvent e) {
                redraw();
            }
        });

        addMouseListener(new MouseAdapter() {
            public void mouseDown(MouseEvent e) {
                onMouseDown(e);
            }

            public void mouseUp(MouseEvent e) {
                onMouseUp(e);
            }

            public void mouseDoubleClick(MouseEvent e) {
                onMouseDoubleClick(e);
            }
        });

        addMouseMoveListener(new MouseMoveListener() {
            public void mouseMove(MouseEvent e) {
                onMouseMove(e);
            }
        });

        addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                onKeyDown(e);
            }
        });
        
        addCellSelectionListener(new KTableCellSelectionAdapter() {
            
            private Point[] oldSelections;
            
            public void cellSelected(int col, int row, int statemask) {
                if (isHighlightSelectionInHeader() && (statemask & SWT.SHIFT) == 0) {
					Point[] selections = getCellSelection();
					GC gc = new GC(DipTable.this);

					repaintRelevantCells(gc, oldSelections);
					repaintRelevantCells(gc, selections);
					
					gc.dispose();
					oldSelections = selections;
                }
        	}
            
			private void repaintRelevantCells(GC gc, Point[] selections) {
			    if (selections==null) return;
			    Rectangle bounds = getClientArea();
                Rectangle oldClipping = gc.getClipping();
                int fixedWidth =  0;
                int fixedHeight = 0;
                for (int k=0; k<m_Model.getFixedHeaderColumnCount(); k++)
                    fixedWidth += getCellRectIgnoreSpan(k, 0).width+1;
                for (int k=0; k<m_Model.getFixedHeaderRowCount(); k++)
                    fixedHeight += getCellRectIgnoreSpan(0, k).height+1;
                
                for (int i=0; i<selections.length; i++) {
			        int col = selections[i].x; 
                    int row = selections[i].y;
                    for (int j=0; j<getModel().getFixedHeaderColumnCount(); j++) {
                        Point valid = getValidCell(j, row);
                        // allow painting of GC only on columns, not on rows:
                        Rectangle rowClip = new Rectangle(1,1+fixedHeight,fixedWidth,bounds.height-1-fixedHeight);                        
                        rowClip.intersect(oldClipping);
                        gc.setClipping(rowClip);
                        drawCell(gc, valid.x, valid.y);
                    }
                    for (int j=0; j<getModel().getFixedHeaderRowCount(); j++) {
                        Point valid = getValidCell(col, j);
                        // allow painting of GC only on rows, not on cols:
                        Rectangle rowClip = new Rectangle(1+fixedWidth,1,bounds.width-1-fixedWidth,fixedHeight);                        
                       
                        rowClip.intersect(oldClipping);
                        gc.setClipping(rowClip);
                        drawCell(gc, valid.x, valid.y);
                    }
                    gc.setClipping(oldClipping);
			    }
			}
        });
        
        addFocusListener(new FocusListener() {

		    private Point[] oldSelection;

            public void focusGained(FocusEvent e) {
                if (!isShowSelectionWithoutFocus() && oldSelection != null) {
					setSelection(oldSelection, false);
					for (int i = 0; i < oldSelection.length; i++)
						updateCell(oldSelection[i].x, oldSelection[i].y);
					oldSelection = null;
		               }
			}

			public void focusLost(FocusEvent e) {
				if (!isShowSelectionWithoutFocus()) {
					oldSelection = getCellSelection();
					clearSelection();
					if (oldSelection != null)
						for (int i = 0; i < oldSelection.length; i++)
							updateCell(oldSelection[i].x, oldSelection[i].y);
				}
			}

		});

		TooltipListener tooltipListener = new TooltipListener();
		addListener(SWT.Dispose, tooltipListener);
		addListener(SWT.KeyDown, tooltipListener);
		addListener(SWT.MouseDown, tooltipListener);
		addListener(SWT.MouseDoubleClick, tooltipListener);
		addListener(SWT.MouseMove, tooltipListener);
		addListener(SWT.MouseHover, tooltipListener);
		addListener(SWT.MouseExit, tooltipListener);

		if (getVerticalBar() != null) {

			getVerticalBar().addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					if (getDipTableModel().isCtrlPressed()) {
						return;
					}
					getDipTableModel().clearFirstElement();
					m_TopRow = computeTopRow();
					redraw();
				}

			});
			getVerticalBar().addListener(SWT.Selection, tooltipListener);
		}

		if (getHorizontalBar() != null) {
			getHorizontalBar().addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					int oldLeftCol = m_LeftColumn;
					m_LeftColumn = getHorizontalBar().getSelection() + getFixedColumnCount();
					if (oldLeftCol != m_LeftColumn)
						redraw();
				}
			});
			getHorizontalBar().addListener(SWT.Selection, tooltipListener);
		}
	}

	/**
	 * Определяет первый ряд в зависимости от положения Scroll
	 */
	public int computeTopRow() {
		int selection = getVerticalBar().getSelection();
		int maxHeight = 0;
		int start = 0;
		getDipTableModel().clearFirstElement();
		
		for (int i = 1; i < getModel().getRowCount(); i++) {
			start = maxHeight;
			int currentHeight = getModel().getRowHeight(i);
			maxHeight += currentHeight;
			if (maxHeight > selection) {
				getDipTableModel().setFirstCell(i);
				getDipTableModel().setIndent(selection - start);
				return i;
			}
		}
		return m_TopRow;
	}

	private int getFixedHeight() {
		int height = 1;
		for (int i = 0; i < getFixedRowCount(); i++)
			height += m_Model.getRowHeight(i);
		return height;
	}

	private int getFullyVisibleRowCount(int fixedHeight) {
		Rectangle rect = getClientArea();

		int count = 0;
		int heightSum = fixedHeight;
		for (int i = m_TopRow; heightSum < rect.height && i < m_Model.getRowCount(); i++) {
			int rowHeight = m_Model.getRowHeight(i);
			if (heightSum + rowHeight <= rect.height) {
				count++;
				heightSum += rowHeight;
			} else {
				break;
			}
		}
		return count;
	}

	private int getFullyVisibleRowCountAtEndOfTable() {
		Rectangle rect = getClientArea();

		int count = 0;
		int heightSum = getFixedHeight();
		for (int i = m_Model.getRowCount() - 1; heightSum < rect.height && i >= getFixedRowCount(); i--) {
			int rowHeight = m_Model.getRowHeight(i);
			if (heightSum + rowHeight <= rect.height) {
				count++;
				heightSum += rowHeight;
			} else {
				break;
			}
		}
		return count;
	}

	private int getFullyVisibleColCountAtEndOfTable() {
		Rectangle rect = getClientArea();

		int count = 0;
		int widthSum = getFixedWidth();
		for (int i = m_Model.getColumnCount() - 1; widthSum < rect.width && i >= getFixedColumnCount(); i--) {
			int colWidth = m_Model.getColumnWidth(i);
			if (widthSum + colWidth <= rect.width) {
				count++;
				widthSum += colWidth;
			} else {
				break;
			}
		}
		return count;
	}

    @Override
	protected void doCalculations() {
        if (m_Model == null) {
            ScrollBar sb = getVerticalBar();
            if (sb != null) {
                sb.setMinimum(0);
                sb.setMaximum(1);
                sb.setPageIncrement(1);
                sb.setThumb(1);
                sb.setSelection(1);
            }
            return;
        }

		Rectangle rect = getClientArea();
		if (m_LeftColumn < getFixedColumnCount()) {
			m_LeftColumn = getFixedColumnCount();
		}
		if (m_LeftColumn > m_Model.getColumnCount())
			m_LeftColumn = 0;

		if (m_TopRow < getFixedRowCount()) {
			m_TopRow = getFixedRowCount();
		}
		if (m_TopRow > m_Model.getRowCount())
			m_TopRow = 0;

		/*
		 * If all columns or all rows are fully visible, we must make sure that the
		 * table is scrolled completely to the left and top. Otherwise, it can happen
		 * that the user scrolls to the right / bottom, then increases window size which
		 * leads to the scrollbars being deactivated but the table still scrolled to the
		 * right / bottom with no way to access the left / top because the scrollbars
		 * are deactivated.
		 */
		boolean allColumnsFullyVisible = getFullyVisibleColCountAtEndOfTable() == m_Model.getColumnCount()
				- getFixedColumnCount();
		boolean allRowsFullyVisible = getFullyVisibleRowCountAtEndOfTable() == m_Model.getRowCount()
				- getFixedRowCount();
		if (allColumnsFullyVisible) {
			m_LeftColumn = getFixedColumnCount();
		}
		if (allRowsFullyVisible) {
			m_TopRow = getFixedRowCount();
		}

		int fixedHeight = getFixedHeight();
		m_ColumnsVisible = 0;
		m_ColumnsFullyVisible = 0;

		if (m_Model.getColumnCount() > getFixedColumnCount()) {
			int runningWidth = getColumnLeft(m_LeftColumn);
			for (int col = m_LeftColumn; col < m_Model.getColumnCount(); col++) {
				if (runningWidth < rect.width + rect.x)
					m_ColumnsVisible++;
				runningWidth += getColumnWidth(col);
				if (runningWidth < rect.width + rect.x)
					m_ColumnsFullyVisible++;
				else
					break;
			}
		}

		m_RowsFullyVisible = getFullyVisibleRowCount(fixedHeight);
		m_RowsFullyVisible = Math.min(m_RowsFullyVisible, m_Model.getRowCount() - getFixedRowCount());
		m_RowsFullyVisible = Math.max(0, m_RowsFullyVisible);

		m_RowsVisible = m_RowsFullyVisible + 1;

		if (m_TopRow + m_RowsFullyVisible > m_Model.getRowCount()) {
			m_TopRow = Math.max(getFixedRowCount(), m_Model.getRowCount() - m_RowsFullyVisible);
		}

		if (m_TopRow + m_RowsFullyVisible >= m_Model.getRowCount()) {
			m_RowsVisible--;
		}

		// определяем высоту скролбара
		updateVerticalBarMaximum();
	}

	private int getColumnWidth(int col) {
		if (col == m_Model.getColumnCount() - 1 && (getStyle() & SWTX.FILL_WITH_LASTCOL) != 0) {
			// expand the width to grab all the remaining space with this last col.
			Rectangle cl = getClientArea();
			int remaining = cl.x + cl.width - 2 - getColumnLeft(col);
			return Math.max(remaining, m_Model.getColumnWidth(col));
		} else
			return m_Model.getColumnWidth(col);
	}

	private int computeMaxHeight() {
		int maxHeight = 0;
		for (int i = 0; i < getModel().getRowCount()/* - getFullyVisibleRowCountAtEndOfTable() */; i++) {
			maxHeight += getDipTableModel().getFullRowHeight(i);
		}
		return maxHeight + 5;
	}

	private void updateVerticalBarMaximum() {
		// определяем высоту скролбара
		ScrollBar sb = getVerticalBar();
		int maxHeight = computeMaxHeight();
		if (maxHeight <= 0) {
			maxHeight = 1;
			sb.setSelection(1);
		}
		int tableHeight = getClientArea().height;
		fThumb = tableHeight - getModel().getRowHeight(0);
		sb.setThumb(fThumb);
		sb.setIncrement(fThumb / 15);
		sb.setPageIncrement(fThumb / 8);
		sb.setMaximum(maxHeight);
		sb.setMinimum(0);
	}

	// был приватный
	public int getYforRow(int row) {
		if (row == 0)
			return 1;

		int y;
		if (row < getFixedRowCount()) {
			y = 1;
			for (int i = 0; i < row; i++)
				y += m_Model.getRowHeight(i);

		} else {
			y = getFixedHeight();
			for (int i = m_TopRow; i < row; i++)
				y += m_Model.getRowHeight(i);
		}
		return y;
	}

	/**
	 * Listener Class that implements fake tooltips. The tooltip content is
	 * retrieved from the tablemodel.
	 */
	class TooltipListener implements Listener {
		Shell tip = null;
		Label label = null;

		final Listener labelListener = new Listener() {
			public void handleEvent(Event event) {
				Label label = (Label) event.widget;
				Shell shell = label.getShell();
				// forward mouse events directly to the underlying KTable
				switch (event.type) {
				case SWT.MouseDown:
					Event e = new Event();
					e.item = DipTable.this;
					e.button = event.button;
					e.stateMask = event.stateMask;
					notifyListeners(SWT.MouseDown, e);
					// fall through
				default:
					shell.dispose();
					break;
				}
			}
		};

		public void handleEvent(Event event) {
			switch (event.type) {
			case SWT.Dispose:
			case SWT.KeyDown:
			case SWT.MouseDown:
			case SWT.MouseDoubleClick:
			case SWT.MouseMove:
			case SWT.Selection: // scrolling
			case SWT.MouseExit: {
				if (tip == null)
					break;
				tip.dispose();
				tip = null;
				label = null;
				break;
			}
			case SWT.MouseHover: {
				if (tip != null && !tip.isDisposed())
					tip.dispose();

				Point cell = getCellForCoordinates(event.x, event.y);
				String tooltip = null;
				if (cell.x >= 0 && cell.x < m_Model.getColumnCount() && cell.y >= 0 && cell.y < m_Model.getRowCount())
					tooltip = m_Model.getTooltipAt(cell.x, cell.y);

				// check if there is something to show, and abort otherwise:
				if (((tooltip == null || tooltip.equals("")) && //$NON-NLS-1$
						(m_nativTooltip == null || m_nativTooltip.equals(""))) //$NON-NLS-1$
						|| (cell == null || cell.x == -1 || cell.y == -1)) {
					tip = null;
					label = null;
					return;
				}

				tip = new Shell(getShell(), SWT.ON_TOP);
				GridLayout gl = new GridLayout();
				gl.marginWidth = 2;
				gl.marginHeight = 2;
				tip.setLayout(gl);
				tip.setBackground(getDisplay().getSystemColor(SWT.COLOR_INFO_BACKGROUND));
				label = new Label(tip, SWT.NONE);
				label.setLayoutData(new GridData(GridData.FILL_BOTH));
				label.setForeground(getDisplay().getSystemColor(SWT.COLOR_INFO_FOREGROUND));
				label.setBackground(getDisplay().getSystemColor(SWT.COLOR_INFO_BACKGROUND));
				if (tooltip != null && !tooltip.equals("")) //$NON-NLS-1$
					label.setText(tooltip);
				else
					label.setText(m_nativTooltip);
				label.addListener(SWT.MouseExit, labelListener);
				label.addListener(SWT.MouseDown, labelListener);
				label.addListener(SWT.MouseMove, labelListener);
				Point size = tip.computeSize(SWT.DEFAULT, SWT.DEFAULT);

				// TODO: Correctly position the tooltip below the cursor.
				int y = 20; // currently the windows default???
				int x = 0;
				if (m_defaultCursorSize != null && m_defaultCursorSize.x >= 0 && m_defaultCursorSize.y >= 0) {
					y = m_defaultCursorSize.y + 1;
					x = -m_defaultCursorSize.x;
				}
				// place the shell under the mouse, but check that the
				// bounds of the table are not overlapped.
				Rectangle tableBounds = DipTable.this.getBounds();
				if (event.x + x + size.x > tableBounds.x + tableBounds.width)
					event.x -= event.x + x + size.x - tableBounds.x - tableBounds.width;
				if (event.y + y + size.y > tableBounds.y + tableBounds.height)
					event.y -= event.y + y + size.y - tableBounds.y - tableBounds.height;

				Point pt = toDisplay(event.x + x, event.y + y);
				tip.setBounds(pt.x, pt.y, size.x, size.y);
				tip.setVisible(true);
			}
			}
		}
	}
	
	public void updateScroll() {
		if (getDipTableModel() != null) {
			m_TopRow = computeTopRow();
			redraw();
		}
	}
	
	/**
	 * Scrolls the table so that the given cell is top left.
	 * 
	 * @param col The column index.
	 * @param row The row index.
	 */
	@Override
	public void scroll(int col, int row) {
		if (col < 0 || col >= m_Model.getColumnCount() || row < 0 || row >= m_Model.getRowCount())
			return;

		m_TopRow = row;
		m_LeftColumn = col;
		redraw();
	}

	public void scrollToY(int y, int row) {
		// выставляем в середину экрана
		int y2 = y - (getClientArea().height - getModel().getRowHeight(0)) / 2 + 5 /* +THUMB *//* +fThumb */;
		if (y2 < 0) {
			y2 = 0;
		}
		updateVerticalBarMaximum();
		getVerticalBar().setSelection(y2);
		m_TopRow = computeTopRow();
		redraw();
	}

	/**
	 * 
	 * Показывает как отображается ряд на экране <0 не виден 0 - полностью 1 -	 * 
	 */
	public RowVisible isShowRow(int row) {
		int rowLocation = getDipTableModel().getRowLocation(row);
		int endRowLocation = getDipTableModel().getFullRowHeight(row + 1) + rowLocation;
		int currentLocation = getVerticalBar().getSelection();
		int endCurrent = getClientArea().height + currentLocation;
		// вычитаем первую строку
		currentLocation += 30;

		if (currentLocation > rowLocation) {
			if (currentLocation > endRowLocation) {
				return RowVisible.NOT_VISIBLE;
			} else {
				return RowVisible.ONLY_BOTTOM;
			}
		}
		if (endCurrent > rowLocation) {
			if (endCurrent > endRowLocation) {
				return RowVisible.FULL;
			} else {
				return RowVisible.ONLY_TOP;
			}
		}
		return RowVisible.NOT_VISIBLE;
	}

	@Override
	protected void scrollToFocus() {
		// NO OPERATIONS
	}

	private IKTableModel getDipTableModel() {
		return (IKTableModel) m_Model;
	}
	
	public int topRow() {
		return m_TopRow;
	}

	/*
	 * Установлено жесткое значение.
	 * Т.к. ширина ScrollBar меняется во время инициализации.
	 * При старте Eclipse возвращает 13 пикселей, потом 6.
	 * Возможно на Windows или других версиях GTK будут другие значения
	 */
	public int getVerticalBarWidth() {
		/*if (getDisplay() != null && getDisplay().getThread() == Thread.currentThread()) {
			return getVerticalBar().getSize().x;
		}*/
		return 3;
	}

	//===========================
	//  cell editors

    /*
     * Устанавливаем редактор вручную, чтобы он закрывался средаствами KTable р
     * при потере фокуса
     */
    public void setCellEditor(KTableCellEditor editor) {
    	m_CellEditor = editor;
	}
    
    public KTableCellEditor getCellEditor() {
    	return m_CellEditor;
    }
    
    @Override
    public Rectangle getCellRect(int col, int row) {
    	return super.getCellRect(col, row);
    }
    
}
