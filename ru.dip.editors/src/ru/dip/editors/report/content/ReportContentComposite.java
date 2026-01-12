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

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseWheelListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.IProgressService;

import de.kupzog.ktable.SWTX;
import ru.dip.core.model.DipUnit;
import ru.dip.core.model.finder.IFinder;
import ru.dip.core.model.interfaces.IDipDocumentElement;
import ru.dip.core.model.interfaces.IDipUnit;
import ru.dip.core.unit.form.IFormSettings;
import ru.dip.core.utilities.ui.swt.FontDimension;
import ru.dip.editors.report.ReportEditor;
import ru.dip.editors.report.content.model.RceDipElement;
import ru.dip.ktable.DipTable;
import ru.dip.ui.table.ktable.TableFormSettings;
import ru.dip.ui.table.ktable.actions.OpenAction;
import ru.dip.ui.table.ktable.model.IDipTableElement;
import ru.dip.ui.table.ktable.model.ISelector;
import ru.dip.ui.table.ktable.model.ITableComposite;
import ru.dip.ui.table.ktable.model.ITableCompositeSetting;
import ru.dip.ui.table.table.TableModel;
import ru.dip.ui.table.table.TableSettings;

public class ReportContentComposite extends Composite implements ITableComposite {

	private ReportEditor fEditor;
	
	private DipTable fTable;
	private ReportContentModel fTableModel;
	private ReportCompositeSettings fTableCompositeSettings;
	private ReportTableActionInteractor fTableActionInteractor;
	private IFormSettings fFormSettings;
	
	private MouseListener fMouseListener;
	private IDipTableElement fSelectedElement;
	
	private KeyDownListener fKeyDownListner;
	private KeyUpListener fKeyUpListener;
	private Display fListenerDisplay;
	private FocusLostListener fFocusLostListener;
	private TableMouseWheelListener fTableMouseWheelListener;

	private boolean fCtrlPressed = false; // зажат Ctrl
	private boolean fCtrlShiftPressed = false; // зажат Ctrl + Shift
	
	public ReportContentComposite(Composite parent, ReportEditor editor) {
		super(parent, SWT.NONE);
		fEditor = editor;
		fTableCompositeSettings = new ReportCompositeSettings(fEditor.getDipProject().dipProject());
		fFormSettings = new TableFormSettings(fTableCompositeSettings);		
		fTableActionInteractor = new ReportTableActionInteractor(this);
	}

	public void initialize() {
		initLayout();
		fTable = new DipTable(this,
				SWT.V_SCROLL | SWT.MULTI | SWT.FULL_SELECTION | SWTX.FILL_WITH_LASTCOL | SWTX.EDIT_ON_KEY);
		fTableModel = new ReportContentModel(fTable, fEditor, this);
		createTable();
	}
	
	private void initLayout() {
		setLayout(new FillLayout());
		setLayoutData(new GridData(GridData.FILL_BOTH));
	}
	
	private void createTable() {
		fTable.setModel(fTableModel);
		fTableModel.updateAllElements();
		// hide borders
		fTable.setColorLeftBorder(getBackground());
		fTable.setColorRightBorder(getBackground());
		fTable.setColorTopBorder(getBackground());

		// слушатель нажатия Ctrl/Ctrl-shift
		addCtrlKeyListener();
		// scroll listener
		addScrolListener();
		// слушатель на мышь (выделение, folding), double-click - редактирование		
		addMouseListener();
		// контекстное меню
		fTableActionInteractor.addContextMenu();
	}
	
	// ======================================
	// Key listener (ctrlmode/ctrlshift mode)

	private void addCtrlKeyListener() {
		fKeyUpListener = new KeyUpListener();
		fKeyDownListner = new KeyDownListener();
		fListenerDisplay = Display.getDefault();
		fListenerDisplay.addFilter(SWT.KeyUp, fKeyUpListener);
		fListenerDisplay.addFilter(SWT.KeyDown, fKeyDownListner);
		fTable.addFocusListener(fFocusLostListener = new FocusLostListener());
	}
	
	private class FocusLostListener implements FocusListener {

		@Override
		public void focusLost(FocusEvent e) {
			if (fCtrlPressed) {
				setCtrlPressed(false);
			}
			if (fCtrlShiftPressed) {
				setCtrlShiftPressed(false);
			}
		}

		@Override
		public void focusGained(FocusEvent e) {

		}
	}
	
	private void removeKeyListener() {
		if (fKeyUpListener != null) {
			fListenerDisplay.removeFilter(SWT.KeyUp, fKeyUpListener);
			fKeyUpListener = null;
		}
		if (fKeyDownListner != null) {
			fListenerDisplay.removeFilter(SWT.KeyDown, fKeyDownListner);
			fKeyDownListner = null;
		}
		fListenerDisplay = null;
	}

	private class KeyUpListener implements Listener {

		@Override
		public void handleEvent(Event event) {
			if (fCtrlShiftPressed) {
				setCtrlShiftPressed(false);
			}
			if (fCtrlPressed) {
				setCtrlPressed(false);
			}
		}
	}

	private class KeyDownListener implements Listener {

		@Override
		public void handleEvent(Event event) {
			if (((event.stateMask & SWT.CTRL) == SWT.CTRL) && (event.keyCode == SWT.SHIFT)) {
				setCtrlShiftPressed(true);
			} else if (event.keyCode == SWT.CTRL) {
				setCtrlPressed(true);
			}
		}
	}

	private void setCtrlShiftPressed(boolean newValue) {
		if (isDisposed()) {
			fCtrlShiftPressed = false;
			return;
		}
		if (!isFocus()) {
			fCtrlShiftPressed = false;
		} else {
			fCtrlShiftPressed = newValue;
		}
	}

	private void setCtrlPressed(boolean newValue) {		
		if (isDisposed()) {
			fCtrlPressed = false;
			return;
		}
		if (!isFocus()) {
			fCtrlPressed = false;
		} else {
			fCtrlPressed = newValue;
		}

		if (!fCtrlPressed) {
			fTableModel.checkNeedUpdateColumnWidth();
		}
		updateHeaders();
	}
	
	private void addScrolListener() {
		fTable.addMouseWheelListener(fTableMouseWheelListener = new TableMouseWheelListener());
	}
	
	private class TableMouseWheelListener implements MouseWheelListener {
		@Override
		public void mouseScrolled(MouseEvent e) {
			if (!fCtrlShiftPressed && !fCtrlPressed) {
				return;
			}
			if (fCtrlShiftPressed) {
				ctrlShiftScroll(e.count);
			} else if (fCtrlPressed) {
				ctrlScroll(e.count);
			}
		}
	}

	private void ctrlShiftScroll(int count) {
		int presentationSize = TableSettings.presentationFontSize();
		TableSettings settings = TableSettings.instance();
		if (count > 0) {
			if (presentationSize > FontDimension.MAX_FONT_SIZE) {
				return;
			}
			presentationSize++;
		} else {
			if (presentationSize < FontDimension.MIN_FONT_SIZE) {
				return;
			}
			presentationSize--;
		}

		settings.updatePresentationFontSize(presentationSize);
		fTableModel.updatePresentationFont();
		refreshTable();
	}

	private void ctrlScroll(int count) {
		int idSize = TableSettings.idFontSize();
		int presentationSize = TableSettings.presentationFontSize();
		int commentSize = TableSettings.commentFontSize();
		TableSettings settings = TableSettings.instance();
		if (count > 0) {
			if (idSize < FontDimension.MAX_FONT_SIZE) {
				idSize++;
			}
			if (presentationSize < FontDimension.MAX_FONT_SIZE) {
				presentationSize++;
			}
			if (commentSize < FontDimension.MAX_FONT_SIZE) {
				commentSize++;
			}
		} else {
			if (idSize > FontDimension.MIN_FONT_SIZE) {
				idSize--;
			}
			if (presentationSize > FontDimension.MIN_FONT_SIZE) {
				presentationSize--;
			}
			if (commentSize > FontDimension.MIN_FONT_SIZE) {
				commentSize--;
			}
		}
		settings.updateIdFontSize(idSize);
		settings.updatePresentationFontSize(presentationSize);
		settings.updateCommentFontSize(commentSize);
		fTableModel.updateTableFont();
		refreshTable();
	}
	
	// =============================
	// mouse listener (selection/folding)
		
	public IDipUnit getSelectedDipUnit() {
		if (fSelectedElement instanceof RceDipElement) {
			IDipDocumentElement dde = ((RceDipElement) fSelectedElement).dipResourceElement();
			if (dde instanceof IDipUnit) {
				return (IDipUnit) dde;
			}
		}	
		return null;
	}
		
	private void addMouseListener() {

		fTable.addMouseListener(fMouseListener = new MouseListener() {

			@Override
			public void mouseUp(MouseEvent e) {}

			@Override
			public void mouseDown(MouseEvent e) {
				Point p = fTable.getCellForCoordinates(e.x, e.y);
				Object object = fTable.getModel().getContentAt(p.x, p.y);
				if (object instanceof IDipTableElement) {
					fSelectedElement = (IDipTableElement) object;
				}
			}
			
			@Override
			public void mouseDoubleClick(MouseEvent e) {
				Point p = fTable.getCellForCoordinates(e.x, e.y);
				doDoubleClick(p.y, p.x);
			}
		});
	}
	
	private void doDoubleClick(int row, int column) {
		Object obj = fTableModel.getContentAt(column, row);
		if (obj instanceof IDipTableElement) {
			IDipTableElement element = (IDipTableElement) obj;
			IDipDocumentElement dipDocElement = element.dipResourceElement();
			if (dipDocElement instanceof DipUnit) {
				OpenAction.openFile((DipUnit) dipDocElement, getShell());
			}		
		}
	}
	
	public void openFile() {
		IDipUnit dipUnit = getSelectedDipUnit();
		if (dipUnit != null) {
			OpenAction.openFile((IDipUnit) dipUnit, getShell());
		}
	}

	//=====================================
	// update
	
	public void asyncRefreshTree() {
		IWorkbench wb = PlatformUI.getWorkbench();
		IProgressService ps = wb.getProgressService();
		try {
			ps.busyCursorWhile(new IRunnableWithProgress() {
				public void run(IProgressMonitor pm) {
					Display.getDefault().syncExec(() -> {
						updateTable();
						fTable.redraw();
					});
				}
			});
		} catch (InvocationTargetException | InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public void updateTable() {
		if (fTableModel.presentationWidth() < 10) {
			return;
		}
		fTableModel = (ReportContentModel) fTable.getModel();
		fTableModel.computeElements();
		fTableModel.updateAllElements();
		fTable.setModel(fTableModel);
	}
	
	
	/**
	 * Обновить заголовки таблицы
	 */
	private void updateHeaders() {
		refreshTable();
	}

	public void refreshTable() {
		fTableModel = (ReportContentModel) fTable.getModel();
		fTable.setModel(fTableModel);
	}
	
	//==========================
	// dispose
	
	@Override
	public void dispose() {
		removeKeyListener();
		if (!fTable.isDisposed()) {
			fTable.removeFocusListener(fFocusLostListener);
			fTable.removeMouseWheelListener(fTableMouseWheelListener);
			fTable.removeMouseListener(fMouseListener);
		}
		fFocusLostListener = null;
		fTableMouseWheelListener = null;
		super.dispose();
		
	}
	
	//=========================
	// getters
	
	public boolean isFocus() {
		return fTable.isFocusControl() || this.isFocusControl();
	}
	
	public boolean isCtrlPressed() {
		return fCtrlPressed;
	}
	
	public IFormSettings getFormSettings() {
		return fFormSettings;
	}
	
	@Override
	public ITableCompositeSetting getTableSettings() {
		return fTableCompositeSettings;
	}
	
	public DipTable table() {
		return fTable;
	}

	@Override
	public IFinder getFinder() {
		return null;
	}

	@Override
	public ISelector selector() {
		return null;
	}

	@Override
	public int tableHeight() {
		return 0;
	}

	@Override
	public void updateCommentFromCellEditor(IDipTableElement element, IDipDocumentElement dipDocElement,
			String newComment) {
		
	}

	@Override
	public void updateDescriptionFromCellEditor(IDipTableElement element, IDipDocumentElement dipDocElement,
			String newContent) {		
	}

	@Override
	public boolean isSelect(IDipTableElement tableElement) {
		return false;
	}

	@Override
	public TableModel model() {
		return null;
	}





}
