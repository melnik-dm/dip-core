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
package ru.dip.ui.table.ktable;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseWheelListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.IProgressService;
import org.eclipse.ui.progress.WorkbenchJob;

import de.kupzog.ktable.KTableCellResizeListener;
import de.kupzog.ktable.SWTX;
import ru.dip.core.exception.CopyDIPException;
import ru.dip.core.exception.CreateResourceException;
import ru.dip.core.exception.DIPException;
import ru.dip.core.exception.DeleteDIPException;
import ru.dip.core.exception.ImportException;
import ru.dip.core.exception.IncludeFolderException;
import ru.dip.core.exception.RenameDIPException;
import ru.dip.core.exception.TmpCopyException;
import ru.dip.core.model.DipFolder;
import ru.dip.core.model.DipProject;
import ru.dip.core.model.DipRoot;
import ru.dip.core.model.DipTableContainer;
import ru.dip.core.model.DipUnit;
import ru.dip.core.model.IncludeFolder;
import ru.dip.core.model.TocRef;
import ru.dip.core.model.finder.FindSettings;
import ru.dip.core.model.finder.IFinder;
import ru.dip.core.model.finder.WordFinder;
import ru.dip.core.model.glossary.GlossaryFolder;
import ru.dip.core.model.interfaces.IDipDocumentElement;
import ru.dip.core.model.interfaces.IDipElement;
import ru.dip.core.model.interfaces.IDipParent;
import ru.dip.core.model.interfaces.IEmptyResultFindable;
import ru.dip.core.model.interfaces.IFindable;
import ru.dip.core.model.interfaces.ISearchElementsHolder;
import ru.dip.core.report.model.condition.Condition;
import ru.dip.core.report.scanner.RuleScanner;
import ru.dip.core.unit.ReportRefPresentation;
import ru.dip.core.unit.UnitPresentation;
import ru.dip.core.unit.UnitType;
import ru.dip.core.unit.form.IFormSettings;
import ru.dip.core.utilities.ArrayUtils;
import ru.dip.core.utilities.DipTableUtilities;
import ru.dip.core.utilities.DipUtilities;
import ru.dip.core.utilities.ResourcesUtilities;
import ru.dip.core.utilities.WorkbenchUtitlities;
import ru.dip.core.utilities.tmp.ActivateNumerationResult;
import ru.dip.core.utilities.tmp.ApplyAutoNumberingResult;
import ru.dip.core.utilities.tmp.AutoNumberingSettingResult;
import ru.dip.core.utilities.tmp.CreateFileResult;
import ru.dip.core.utilities.tmp.CreateFolderResult;
import ru.dip.core.utilities.tmp.EditCommentResult;
import ru.dip.core.utilities.tmp.EditDescriptionResult;
import ru.dip.core.utilities.tmp.ImportResult;
import ru.dip.core.utilities.tmp.IncludeFolderResult;
import ru.dip.core.utilities.tmp.MDJoinResult;
import ru.dip.core.utilities.tmp.MdExtractResult;
import ru.dip.core.utilities.tmp.MoveResult;
import ru.dip.core.utilities.tmp.OrientationResult;
import ru.dip.core.utilities.tmp.PageBreakResult;
import ru.dip.core.utilities.tmp.PasteObjectsResult;
import ru.dip.core.utilities.tmp.RenameResult;
import ru.dip.core.utilities.tmp.ResultOperation;
import ru.dip.core.utilities.tmp.SortedResult;
import ru.dip.core.utilities.tmp.UpDownResult;
import ru.dip.core.utilities.ui.swt.FontDimension;
import ru.dip.core.utilities.ui.swt.KeyMode;
import ru.dip.ktable.DipTable;
import ru.dip.ui.Messages;
import ru.dip.ui.action.hyperlink.ReqLink;
import ru.dip.ui.controller.RenameController;
import ru.dip.ui.glossary.GlossaryDialog;
import ru.dip.ui.table.editor.DipTableEditor;
import ru.dip.ui.table.ktable.TableSizeInteractor.CompositeControlListener;
import ru.dip.ui.table.ktable.actions.EditCommentAction;
import ru.dip.ui.table.ktable.actions.EditDescriptionAction;
import ru.dip.ui.table.ktable.actions.manager.AutoNumberingInteractor;
import ru.dip.ui.table.ktable.actions.manager.CopyIdIneractor;
import ru.dip.ui.table.ktable.actions.manager.CreateFileInteractor;
import ru.dip.ui.table.ktable.actions.manager.DeleteFileInteractor;
import ru.dip.ui.table.ktable.actions.manager.EditCommentInteractor;
import ru.dip.ui.table.ktable.actions.manager.EditDescriptionInteractor;
import ru.dip.ui.table.ktable.actions.manager.ImportActionInteractor;
import ru.dip.ui.table.ktable.actions.manager.IncludeInteractor;
import ru.dip.ui.table.ktable.actions.manager.IntoFolderInteractor;
import ru.dip.ui.table.ktable.actions.manager.PasteInteractor;
import ru.dip.ui.table.ktable.actions.undo.ActionStack;
import ru.dip.ui.table.ktable.celleditors.CellEditorManager;
import ru.dip.ui.table.ktable.dialog.AutoNumberingDialog;
import ru.dip.ui.table.ktable.dialog.HelpCtrlDialog;
import ru.dip.ui.table.ktable.diff.DiffInteractor;
import ru.dip.ui.table.ktable.model.DipTableModel;
import ru.dip.ui.table.ktable.model.HideElements;
import ru.dip.ui.table.ktable.model.IDipTableElement;
import ru.dip.ui.table.ktable.model.ITableComposite;
import ru.dip.ui.table.ktable.model.ITableNode;
import ru.dip.ui.table.ktable.model.TableElement;
import ru.dip.ui.table.ktable.model.TableNode;
import ru.dip.ui.table.table.DropAction;
import ru.dip.ui.table.table.TableModel;
import ru.dip.ui.table.table.TableModelProvider;
import ru.dip.ui.table.table.TableSettings;
import ru.dip.ui.toc.DipTocView;
import ru.dip.ui.wizard.include.SetIncludeLinkWizard;
import ru.dip.ui.wizard.rename.RenameWizard;

public class KTableComposite extends Composite implements ITableComposite {

	private DipTableEditor fEditor;
	private DipTableModel fDipTableModel;
	private DipTable fTable;
	private TableModel fModel;
	private CellEditorManager fCellEditorsManager;

	private KDipTableSelector fSelector;
	private KTableColorInteractor fColorInteractor;
	private TableSizeInteractor fSizeInteractor;
	private KTableActionInteractor fTableActionInteractor;
	private DiffInteractor fDiffInteractor;
	private DropAction fDropAction;
	private MdActionInteractor fMdActionItenractor;
	private ActionStack fActionStack;
	private ImportActionInteractor fImportActionInteractor;
	private CreateFileInteractor fCreateFileInteractor;
	private IncludeInteractor fIncludeInteractor;
	private DeleteFileInteractor fDeleteInteractor;
	private EditDescriptionInteractor fEditDescriptionInteractor;
	private EditCommentInteractor fEditCommentInteractor;
	private AutoNumberingInteractor fAutoNumberingInteractor;
	private IntoFolderInteractor fIntoFolderInteractor;
	private PasteInteractor fPasteInteractor;
	private CopyIdIneractor fCopyIdIneractor;
	
	// listeners
	KTableCellResizeListener fTableCellResizeListener;
	private ControlListener controlListener;
	DisposeListener fDisposeListener;
	private TableMouseWheelListener fTableMouseWheelListener;
	private FocusLostListener fFocusLostListener;
	private MouseListener fMouseListener;
	@SuppressWarnings("unused")
	private CompositeControlListener fCompositeControlLlistener;
	private KeyListener fKeyListener;

	
	// settings
	private boolean fOneListMode = false; // отображение одним списком (без узлов)
	private boolean fHighlightGlossMode = false;
	private boolean fHideDisableObjEnable = false;
	private boolean fCheckSpellingEnable = true;
	private boolean fShowMDComment = true;
	private boolean fShowStrictMDComment = true;
	private boolean fShowFormNumeration = true;
	private boolean fFormShowPreferenciesEnable = false;
	private boolean fFixedContent = false;
	private boolean fShowNumeration = true;
	private boolean fShowFormVersion = false;
	private IFormSettings fFormSettings;
	
	// find mode
	private IFinder fFinder;
	private boolean fFindMode = false;
	// diff mode
	private boolean fDinamicallyDiffMode = false;   // режим с равнения с HEAD/origin-HEAD в реальном времени 
	private boolean fDiffMode = false; // режим сравнения с другим коммитом
	private boolean fOnlyDiffMode = false; // режим сравнения с другим коммитом, только измененные объекты

	// ctrl, ctrl-shift mode
	private boolean fCtrlPressed = false; // зажат Ctrl
	private boolean fCtrlShiftPressed = false; // зажат Ctrl + Shift
	private int fNumberClickC = 0;  // количество нажати на клавишу 'C' при зажатом Ctrl + Shift
	private KeyDownListener fKeyDownListner;
	private KeyUpListener fKeyUpListener;
	private HelpCtrlDialog fHelpDialog;

	public KTableComposite(Composite parent, DipTableEditor editor) {
		super(parent, SWT.NONE);
		fEditor = editor;
		fModel = fEditor.model();
		fActionStack = new ActionStack();
		fSelector = new KDipTableSelector(this);
		fColorInteractor = new KTableColorInteractor(this);
		fTableActionInteractor = new KTableActionInteractor(this);
		fMdActionItenractor = new MdActionInteractor(this);
		fImportActionInteractor = new ImportActionInteractor(this);
		fCreateFileInteractor = new CreateFileInteractor(this);
		fIncludeInteractor = new IncludeInteractor(this);
		fDeleteInteractor = new DeleteFileInteractor(this);
		fEditDescriptionInteractor = new EditDescriptionInteractor(this);
		fEditCommentInteractor = new EditCommentInteractor(this);
		fAutoNumberingInteractor = new AutoNumberingInteractor(this);
		fIntoFolderInteractor = new IntoFolderInteractor(this);
		fDiffInteractor = new DiffInteractor(this);	
		fPasteInteractor = new PasteInteractor(this);
		fCopyIdIneractor = new CopyIdIneractor(this);
		fFormSettings = new TableFormSettings(this);		
	}

	/**
	 * Устанавливает настройки, создаёт таблицу
	 * Основна работа, нужно вызывать после создания конструктора
	 */
	public void initialize() {
		initLayout();
		setViewModeProperties();
		fTable = new DipTable(this,
				SWT.V_SCROLL | SWT.MULTI | SWT.FULL_SELECTION | SWTX.FILL_WITH_LASTCOL | SWTX.EDIT_ON_KEY);
		fDipTableModel = new DipTableModel(fTable, fEditor.model(), this);
		fCellEditorsManager = new CellEditorManager(fTable, fDipTableModel);
		
		createTable();
		applyTableProperties();
		fTable.addCellResizeListener(fTableCellResizeListener = new KTableCellResizeListener() {

			@Override
			public void rowResized(int row, int newHeight) {}

			@Override
			public void columnResized(int col, int newWidth) {
				if (!fCtrlPressed) {
					fDipTableModel.checkNeedUpdateColumnWidth();
				}
			}
		});
		fSizeInteractor = new TableSizeInteractor(this);
		fSizeInteractor.readSavedMaxMinSizes();
		fSizeInteractor.readSavedColumnsWidth();
		
		addControlListener(fCompositeControlLlistener = fSizeInteractor.getCompositeControlListener());
	}

	
	private void initLayout() {
		setLayout(new FillLayout());
		setLayoutData(new GridData(GridData.FILL_BOTH));
	}
	
	
	public void setViewModeProperties() {
		fEditor.getButtonManager().setListMode(fOneListMode);
		fEditor.getButtonManager().setHighlightGlossMode(fHighlightGlossMode);

		fFixedContent = fModel.dipProject().getProjectProperties().isFixedContentMode();
		fEditor.getButtonManager().setFixContentMode(fFixedContent);

		fShowNumeration = fModel.dipProject().getProjectProperties().isNumeration();

		fShowFormNumeration = fModel.dipProject().getProjectProperties().isFormNumeration();

		fHighlightGlossMode = fModel.dipProject().getProjectProperties().isHighlightGlossMode();
		fShowMDComment = fModel.dipProject().getProjectProperties().isMdComment();
		fShowStrictMDComment = fModel.dipProject().getProjectProperties().isStrictMdComment();
		fFormShowPreferenciesEnable = fModel.dipProject().getProjectProperties().isFormShowPreferenciesEnable();

		fCheckSpellingEnable = fModel.dipProject().getProjectProperties().isCheckSpellingEnable();
		fEditor.getButtonManager().setCheckEnable(fCheckSpellingEnable);
		fHideDisableObjEnable = fModel.dipProject().getProjectProperties().isHideDisableObjsEnable();
		fEditor.getButtonManager().setHideDisableObjs(fHideDisableObjEnable);
		fShowFormVersion = fModel.dipProject().getProjectProperties().isShowFormVersion();
	}
	
	private void applyTableProperties() {
		boolean refresh = false;
		if (fOneListMode != fModel.dipProject().getProjectProperties().isOneListMode()) {
			fOneListMode = fModel.dipProject().getProjectProperties().isOneListMode();
			applyListMode();
			refresh = true;
		}
		if (fDipTableModel.checkIdColumnMode()) {
			refresh = true;
		}

		if (fDipTableModel.checkReviewMode()) {
			refresh = true;
		}

		boolean fixedContent = fModel.dipProject().getProjectProperties().isFixedContentMode();
		if (fixedContent != fFixedContent) {
			refresh = true;
		}
		if (refresh) {
			asyncRefreshTree();
		}
	}
	
	private void createTable() {
		fTable.setModel(fDipTableModel);
		fDipTableModel.updateAllElements();
		// hide borders
		fTable.setColorLeftBorder(getBackground());
		fTable.setColorRightBorder(getBackground());
		fTable.setColorTopBorder(getBackground());

		updateBackgrouColor();
		// добавляет слушатель на изменение размера внутри PaintListener
		addCompositePaintListener();
		// слушатель нажатия Ctrl/Ctrl-shift
		addCtrlKeyListener();
		// scroll listener
		addScrolListener();
		// drag'n drop (только создание dndaction - использует в команде into folder)
		addDragDrop();
		// слушатель на мышь (выделение, folding), double-click - редактирование		
		addMouseListener();
		// слушатель - верх, вниз, f2, delete, ctrl+c, ctrl-shift+c
		addKeyListener();
		// контекстное меню
		fTableActionInteractor.addContextMenu();
		// сохранение размеров при закрытии
		addDisposeListener();
	}

	public void setNodeExpand(ITableNode node, boolean expand) {
		node.setExpand(expand);
		fDipTableModel.computeNewElements(node);
		fDipTableModel.applyNewElements();
		fColorInteractor.expandElement(node);
	}
	

	// =============================
	// paint listener (добавляет при первой отрисовке, если раньше, то ломает ширину
	// столбцов при старте)

	private void addCompositePaintListener() {
		addPaintListener(new PaintListener() {
			@Override
			public void paintControl(PaintEvent e) {
				// слушатель на изменение размеров таблицы
				addCompositeControlListener();
				removePaintListener(this);
			}
		});
	}

	// =============================
	// control listener (обновление ширины столбцов, при изменении общих размеров)
	
	private void addCompositeControlListener() {
		fTable.addControlListener(controlListener = new ControlListener() {

			@Override
			public void controlResized(ControlEvent e) {
				fDipTableModel.checkWidth();
			}

			@Override
			public void controlMoved(ControlEvent e) {

			}
		});
	}

	// ======================================
	// Key listener (ctrlmode/ctrlshift mode)

	
	private Display fListenerDisplay;
	
	private void addCtrlKeyListener() {
		fKeyUpListener = new KeyUpListener();
		fKeyDownListner = new KeyDownListener();
		fListenerDisplay = Display.getDefault();
		fListenerDisplay.addFilter(SWT.KeyUp, fKeyUpListener);
		fListenerDisplay.addFilter(SWT.KeyDown, fKeyDownListner);
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
		if (!fEditor.isFocus()) {
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
		if (!fEditor.isFocus()) {
			fCtrlPressed = false;
		} else {
			fCtrlPressed = newValue;
		}

		if (!fCtrlPressed) {
			fDipTableModel.checkNeedUpdateColumnWidth();
		}
		updateHeaders();
		showHelpDialog();
	}

	// =====================================
	// ctrl help dialog

	private void showHelpDialog() {
		if (fCtrlPressed) {
			if (isEditorVisible()) {
				HelpJob job = new HelpJob();
				job.schedule(500);
			}
		} else {
			if (fHelpDialog != null && fHelpDialog.isVisible()) {
				fHelpDialog.setVisible(false);
			}
		}
	}

	private boolean isEditorVisible() {
		if (isDisposed()) {
			return false;
		}
		IEditorPart part = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
		if (!part.equals(fEditor)) {
			return false;
		}
		return true;
	}

	private class HelpJob extends WorkbenchJob {

		public HelpJob() {
			super("Ctrl help"); //$NON-NLS-1$
		}

		@Override
		public IStatus runInUIThread(IProgressMonitor monitor) {
			if (fCtrlPressed) {
				if (fHelpDialog == null) {
					IWorkbenchPart part = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
							.getActivePart();
					if (part instanceof DipTableEditor) {
						fHelpDialog = new HelpCtrlDialog(getShell());
						fHelpDialog.open();
					}
				} else {
					IWorkbenchPart part = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
							.getActivePart();
					if (part instanceof DipTableEditor) {
						fHelpDialog.setVisible(true);
					}
				}
			}
			return Status.OK_STATUS;
		}
	}

	// ====================================
	// scroll listener


	
	private void addScrolListener() {
		fTable.addMouseWheelListener(fTableMouseWheelListener = new TableMouseWheelListener());
		//Display.getDefault().addFilter(SWT.KeyUp, fKeyUpListener);
		//Display.getDefault().addFilter(SWT.KeyDown, fKeyDownListner);
		fTable.addFocusListener(fFocusLostListener = new FocusLostListener());
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
		fDipTableModel.updatePresentationFont();
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
		fDipTableModel.updateTableFont();
		//refreshTable();
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

	// ======================================
	// Drap & Drop

	private void addDragDrop() {
		TableModelProvider modelProvider = new TableModelProvider();
		modelProvider.setModel(fModel);
		fDropAction = new DropAction(modelProvider, getShell());
	}

	// =============================
	// mouse listener (selection/folding)
	
	
	
	private void addMouseListener() {

		fTable.addMouseListener(fMouseListener = new MouseListener() {

			@Override
			public void mouseUp(MouseEvent e) {}

			@Override
			public void mouseDown(MouseEvent e) {
				if (e.button == 3) {
					rightButtonDown(e);
				} else {
					leftButtonDown(e);
				}
			}

			private void rightButtonDown(MouseEvent e) {
				Point p = fTable.getCellForCoordinates(e.x, e.y);
				Object object = fTable.getModel().getContentAt(p.x, p.y);
				if (!fSelector.isSelect(object)) {
					doSelect(e);
				}
			}

			private void leftButtonDown(MouseEvent e) {
				doSelect(e);
			}

			private void doSelect(MouseEvent e) {
				Point p = fTable.getCellForCoordinates(e.x, e.y);
				Object object = fTable.getModel().getContentAt(p.x, p.y);
				fSelector.setDoubleClockFolderMode(false);

				if (object == null) {
					fCellEditorsManager.updateEditedCell();
					return;
				}
				// folding
				if (!fOneListMode && object instanceof TableNode) {
					TableNode node = (TableNode) object;
					boolean result = node.expandImageContains(new Point(e.x, e.y));
					if (result) {
						setNodeExpand(node, !node.expand());
					}
				}

				if (object instanceof TableElement) {
					TableElement element = (TableElement) object;
					fDipTableModel.setSelected(element);
					fSelector.setSelection(element, KeyMode.of(e));
				}
				fCellEditorsManager.updateEditedCell();								
			}

			@Override
			public void mouseDoubleClick(MouseEvent e) {
				if (e.stateMask == SWT.CTRL) {
					doCtrlDoubleClick(e);
				} else {
					Point p = fTable.getCellForCoordinates(e.x, e.y);
					doDoubleClick(p.y, p.x);
				}
			}
		});
	}
	
	
	private void doCtrlDoubleClick(MouseEvent e) {
		Point p = fTable.getCellForCoordinates(e.x, e.y);
		int row = p.y;
		int column = p.x;
				
		// если комментарий или id, то обычный doble-click
		if (fDipTableModel.presentationColumnNumber() != column) {
			doDoubleClick(row, column);
			return;
		}
				
		Object object = fDipTableModel.getContentAt(column, row);		
		if (object instanceof TableElement) {
			TableElement element = (TableElement) object;
			if (element.isDescription()) {
				doDoubleClick(row, column);
				return;
			}
			if (isEditableElement(element)){
				fCellEditorsManager.openCellEditor(column, row);
			} else {
				fSelector.doCtrlSelection(element);
			}
		}
	}
	
	private boolean isEditableElement(TableElement element){
		return TableSettings.isEditFileInTable()
				&& (element.isPresentation() ||  element.isAbstractField())
				&& !element.dipDocElement().isReadOnly();
	}
	

	private void doDoubleClick(int row, int column) {
		Object obj = fDipTableModel.getContentAt(column, row);
		if (obj instanceof TableElement) {
			TableElement element = (TableElement) obj;
					
			// если описание
			if (element.isDescription()) {
				if (fDipTableModel.presentationColumnNumber() == column) {
					doEditDescription(column, row);
				} else {
					doEditDescriptionInDialog();
				}
				return;
			}

			// если по комментарий
			if (fDipTableModel.isShowComment() && fDipTableModel.commentColumnNumber() == column) {
				doEditComment(column, row);
				return;
			}

			// если директория
			IDipDocumentElement dipDocElement = element.dipResourceElement();
			if (dipDocElement instanceof DipFolder) {
				if (fModel.isParentHeader(dipDocElement) && !fModel.isTable(dipDocElement)) {
					return;
				}
				if (fDipTableModel.presentationColumnNumber() == column) {
					doEditDescription(column, row);
				} else {
					doEditDescriptionInDialog();
				}
				return;
			}

			// если unit
			if (dipDocElement instanceof DipUnit) {
				if (dipDocElement instanceof TocRef) {
					WorkbenchUtitlities.openView(DipTocView.ID);
				} else {
					openFile((DipUnit) dipDocElement);
				}
			}		
		}
	}

	// ==============================
	// selection

	public void deselect() {
		fSelector.deselect();
	}

	@Override
	public boolean isSelect(IDipTableElement element) {
		return fSelector.isSelect(element);
	}

	public boolean isFocus() {
		return fTable.isFocusControl() || this.isFocusControl();
	}

	// ================================
	// key listener (up, down, rename, delete, ctrl + c, ctrl-shift + c)
	
	
	
	private void addKeyListener() {
		fTable.addKeyListener(fKeyListener = new KeyListener() {

			@Override
			public void keyPressed(KeyEvent e) {
				if (((e.stateMask & SWT.CTRL) == SWT.CTRL) && (((e.stateMask & SWT.SHIFT) == SWT.SHIFT))
						&& (e.keyCode == 'c' || e.keyCode == 'с')) {
					if (fSelector.isOneSelected()) {
						fCopyIdIneractor.doCopyID(fNumberClickC);
						fNumberClickC++;
					}
					return;
				} 
				fNumberClickC = 0;
				
				if (e.keyCode == SWT.DEL) {
					if (fDeleteInteractor.canDelete()) {
						doDeleteTrigger();
					}
				} else if (e.keyCode == SWT.F2) {
					if (canRename()) {
						doRenameTrigger();
					}
				} else if (e.keyCode == SWT.ARROW_UP) {
					if (!fSelector.isEmpty()) {
						doSelectUp();
					}
				} else if (e.keyCode == SWT.ARROW_DOWN) {
					if (!fSelector.isEmpty()) {
						doSelectDown();
					}
				} else if (e.keyCode == SWT.F5) {
					resourceUpdate();
					// globalUpdate();
					fEditor.udpateModelFromInput();
				}
			}
			
			@Override
			public void keyReleased(KeyEvent e) {}
		});
	}

	// ===============================
	// dispose listener


	
	private void addDisposeListener() {
		fTable.addDisposeListener(fDisposeListener = new DisposeListener() {

			@Override
			public void widgetDisposed(DisposeEvent e) {
				fSizeInteractor.saveColumnsWidth();
				fSizeInteractor.saveMaxMinSizes();

			}
		});
	}

	// ============================
	// find

	
	class SearchElemetns implements ISearchElementsHolder {

		private IDipDocumentElement[] fDipDocElements;

		private SearchElemetns() {
			computeElements();
		}

		private void computeElements() {
			fDipDocElements = fModel.getOneListChildren();
			IDipDocumentElement select = fSelector.getLastSelectDipDocElement();
			int startIndex = ArrayUtils.getIndex(fDipDocElements, select);
			if (startIndex > 0) {
				IDipDocumentElement[] start = Arrays.copyOf(fDipDocElements, startIndex);
				IDipDocumentElement[] end = Arrays.copyOfRange(fDipDocElements, startIndex, fDipDocElements.length);
				fDipDocElements = Stream.of(end, start).flatMap(Stream::of).toArray(IDipDocumentElement[]::new);
			}
		}

		@Override
		public IDipDocumentElement[] getDipDocElementsForSearch() {
			return fDipDocElements;
		}

	}
	
	/**
	 * text - искомый текст
	 * caseSensitive - чувствительность к регистру
	 * position -переход по позициям или по dipDocElement
	 */
	public IFinder find(String text, boolean caseSensitive, boolean findInId) {
		fFinder = new WordFinder(new SearchElemetns(), text, fFormSettings);
		FindSettings settings = FindSettings.builder()
			.caseSensetive(caseSensitive)
			.findInId(findInId)
			.findInDisableObjs(fHideDisableObjEnable)
			.build();
			
		fFinder.find(settings);
		return fFinder;
	}

	public void cleanFind() {
		if (fFinder == null) {
			return;
		}		
		List<IDipDocumentElement> children = fFinder.getElements();
		for (IDipDocumentElement child : children) {
			if (child instanceof IFindable) {
				((IFindable) child).cleanFind();
			}
		}
		fEditor.updater().updateElements(children);
		setFindMode(false);
		fFinder = null;
	}

	// ===============================
	// show numeration

	public void doShowNumeration() {
		fShowNumeration = !fShowNumeration;
		fModel.dipProject().getProjectProperties().setNumeration(fShowNumeration);
		fDipTableModel.updateNodes();
		refreshTable();
	}

	public void doResetNumeration() {
		boolean reset = MessageDialog.openQuestion(getShell(), Messages.KTableComposite_ResetNumertionShellTitle,
				Messages.KTableComposite_ResetNumerationMessage);
		if (reset) {
			resetNumeration(fModel);
			fDipTableModel.updateNodes();
		}
		fEditor.doSave(null);
		refreshTable();
	}

	private void resetNumeration(IDipParent parent) {
		parent.setActiveNumeration(true);
		for (IDipDocumentElement dipDocElement : parent.getDipDocChildrenList()) {
			if (dipDocElement instanceof IDipParent) {
				resetNumeration((IDipParent) dipDocElement);
			}
		}
	}

	public ActivateNumerationResult doActivateNumeration() {
		IDipDocumentElement selectedElement = fSelector.getSelectedOneDipDocElement();
		if (selectedElement instanceof IDipParent) {
			IDipParent parent = (IDipParent) selectedElement;
			return doActivateNumeration(parent);
		}
		return null;
	}
	
	public ActivateNumerationResult doActivateNumeration(IDipParent parent) {
		boolean activeNumeration = parent.isActiveNumeration();
		parent.setActiveNumeration(!activeNumeration);
		fEditor.updater().updateNodes(parent);

		return new ActivateNumerationResult(DipUtilities.relativeProjectID(parent), activeNumeration);
	}
	
	public AutoNumberingSettingResult doSetAutoNumbering() {
		IDipDocumentElement selectedElement = fSelector.getSelectedOneDipDocElement();
		if (selectedElement instanceof IDipParent) {
			AutoNumberingDialog dialog = new AutoNumberingDialog(getShell(), (IDipParent) selectedElement);
			if (dialog.open() == Dialog.OK) {
				fEditor.updater().updateAfterEnableAutoNumbering((IDipParent) selectedElement);
				return dialog.getResult();
			}
		}
		return null;
	}
	
	public ApplyAutoNumberingResult doApplyAutoNumbering() {
		try {
			return fAutoNumberingInteractor.doRenameWithDialog();
		} catch (RenameDIPException e) {
			WorkbenchUtitlities.openError("Auto Numbering Error", e.getMessage());
			e.printStackTrace();
			return null;
		}
	}

	// ====================
	// flat/nested mode

	public void changeOneListMode(boolean selection) {
		fOneListMode = selection;
		applyListMode();
		fEditor.getButtonManager().setListMode(fOneListMode);
		fModel.dipProject().getProjectProperties().setOneListMode(fOneListMode);
		updateBackgrouColor();
		asyncRefreshTree();
	}

	public void applyListMode() {
		if (fOneListMode) {
			applyListView();
		} else {
			applyExtendedView();
		}
	}

	private void applyExtendedView() {
		// fViewer.setExpandedElements(fModelProvider.getModel().getParentsList().toArray());
	}

	private void applyListView() {
		fDipTableModel.expandAll();
	}

	// ======================
	// INCLUDE

	public IncludeFolderResult doInclude() {
		try {
			return fIncludeInteractor.includeFolderWithWizard();
		} catch (IncludeFolderException e) { 
			WorkbenchUtitlities.openError("Include Folder Error", e.getMessage());
			e.printStackTrace();
		}
		return null;
	}
	
	public IncludeFolderResult doIncludeBefore() {
		try {
			return fIncludeInteractor.includeNeighbourFolderWithWizard(true);
		} catch (IncludeFolderException e) { 
			WorkbenchUtitlities.openError("Include Folder Error", e.getMessage());
			e.printStackTrace();
		}
		return null;
	}
	
	public IncludeFolderResult doIncludeAfter() {
		try {
			return fIncludeInteractor.includeNeighbourFolderWithWizard(false);
		} catch (IncludeFolderException e) { 
			WorkbenchUtitlities.openError("Include Folder Error", e.getMessage());
			e.printStackTrace();
		}
		return null;
	}
	
	/** 
	 * Установить ссылку (для сломанного include)
	 */
	public void setLinkAction() {
		IDipDocumentElement selectedElement = fSelector.getSelectedOneDipDocElement();
		if (selectedElement instanceof IncludeFolder) {
			IncludeFolder includeFolder = (IncludeFolder) selectedElement;
			SetIncludeLinkWizard wizard = new SetIncludeLinkWizard(includeFolder.getAbsoluteLinkPath());
			WizardDialog dialog = new WizardDialog(getShell(), wizard);
			if (dialog.open() == Dialog.OK) {
				try {
					includeFolder.setLink(wizard.includePath(), wizard.isReadOnly());
				} catch (CoreException e) {
					e.printStackTrace();
					WorkbenchUtitlities.openError("Set Link Error", e.getMessage());
				}
			}
			fEditor.updater().updateParent(includeFolder.parent());
			ResourcesUtilities.updateProject(fModel.resource());
		}
	}

	// ==========================
	// new file

	public void doAddNewFileTrigger() {
		fTableActionInteractor.getAddNewFileAction().run();
	}

	public CreateFileResult doAddNewFile() {		
		try {
			CreateFileResult result =  fCreateFileInteractor.createNeighbourFileWithWizard(false);			
			return result;
		} catch (CreateResourceException e) { 
			WorkbenchUtitlities.openError("Create File Error", e.getMessage());
			e.printStackTrace();
		}
		return null;
	}

	public CreateFileResult doAddNewFileBefore() {
		try {
			return fCreateFileInteractor.createNeighbourFileWithWizard(true);
		} catch (CreateResourceException e) { 
			WorkbenchUtitlities.openError("Create File Error", e.getMessage());
			e.printStackTrace();
		}
		return null;

	}

	public CreateFileResult doAddNewFileAfter() {	
		try {
			return fCreateFileInteractor.createNeighbourFileWithWizard(false);
		} catch (CreateResourceException e) { 
			WorkbenchUtitlities.openError("Create File Error", e.getMessage());
			e.printStackTrace();
		}
		return null;
	}

	// ============================
	// new folder

	public void doAddNewFolderTrigger() {
		fTableActionInteractor.getAddNewFolder().run();
	}

	public CreateFolderResult doAddNewFolder() {
		try {
			return fCreateFileInteractor.createFolderWithWizard();
		} catch (CreateResourceException e) { 
			WorkbenchUtitlities.openError("Create File Error", e.getMessage());
			e.printStackTrace();
		}
		return null;
	}
	
	public CreateFolderResult doAddNewFolderBefore() {
		try {
			return fCreateFileInteractor.createNeighbourFolderWithWizard(true);
		} catch (CreateResourceException e) { 
			WorkbenchUtitlities.openError("Create File Error", e.getMessage());
			e.printStackTrace();
		}
		return null;
	}
	
	public CreateFolderResult doAddNewFolderAfter() {
		try {
			return fCreateFileInteractor.createNeighbourFolderWithWizard(false);
		} catch (CreateResourceException e) { 
			WorkbenchUtitlities.openError("Create File Error", e.getMessage());
			e.printStackTrace();
		}
		return null;
	}
	
	// ============================
	// import file

	public ImportResult doImportFile() {
		try {
			return fImportActionInteractor.importFileWithWizard();
		} catch (ImportException e) {
			WorkbenchUtitlities.openError("Import Error", e.getMessage());
			e.printStackTrace();
			return null;
		}
	}

	public ImportResult doImportBeforeFile() {
		try {
			return fImportActionInteractor.importNeighbourFileWithWizard(true);
		} catch (ImportException e) {
			WorkbenchUtitlities.openError("Import Error", e.getMessage());
			e.printStackTrace();
			return null;
		}
	}

	public ImportResult doImportAfterFile() {
		try {
			return fImportActionInteractor.importNeighbourFileWithWizard(false);
		} catch (ImportException e) {
			WorkbenchUtitlities.openError("Import Error", e.getMessage());
			e.printStackTrace();
			return null;
		}
	}

	// ============================
	// import folder

	public ImportResult doImportFolder() {
		try {
			return fImportActionInteractor.importFolderWithWizard();
		} catch (IOException | CoreException e) {
			WorkbenchUtitlities.openError("Import Error", e.getMessage());
			e.printStackTrace();
			return null;
		}
	}

	public ImportResult doImportBeforeFolder() {
		try {
			return fImportActionInteractor.importNeighbourFolderWithWizard(true);
		} catch (IOException | CoreException e) {
			WorkbenchUtitlities.openError("Import Error", e.getMessage());
			e.printStackTrace();
			return null;
		}
	}

	public ImportResult doImportAfterFolder() {
		try {
			return fImportActionInteractor.importNeighbourFolderWithWizard(false);
		} catch (IOException | CoreException e) {
			WorkbenchUtitlities.openError("Import Error", e.getMessage());
			e.printStackTrace();
			return null;
		}
	}

	// ==============================
	// edit/remove description
	
	public EditDescriptionResult doEditDescriptionInDialog() {
		return fEditDescriptionInteractor.doEditDescriptionInDialog();
	}
	
	public void doEditDescription(int column, int row) {		
		EditDescriptionResult result = fEditDescriptionInteractor.doEditDescription(column, row);
		if (result != null) {
			fActionStack.pushUndoAction(new EditDescriptionAction(this, result));
		}
	}
	
	@Override
	public void updateDescriptionFromCellEditor(IDipTableElement endElement, IDipDocumentElement dipDocElement, String newDescription) {
		String oldDescription = dipDocElement.description();
		// update
		fEditDescriptionInteractor.updateDescription(endElement, dipDocElement, newDescription);
		// add to undo-redo stack
		if(!Objects.equals(newDescription, oldDescription)) {
			EditDescriptionResult result = new EditDescriptionResult(DipUtilities.relativeProjectID(dipDocElement),
					oldDescription, newDescription);
			fActionStack.pushUndoAction(new EditDescriptionAction(this, result));
		}		
	}
	
	public EditDescriptionResult doRemoveDescription() {
		return fEditDescriptionInteractor.doRemoveDescription();
	}

	// ==============================
	// rename

	public void doRenameTrigger() {
		fTableActionInteractor.getRenameAction().run();
	}

	public RenameResult doRename() {
		try {
			return doRenameWithDialog();
		} catch (RenameDIPException e) {
			WorkbenchUtitlities.openError("Rename Error", e.getMessage());
			e.printStackTrace();
		}
		return null;
	}
	
	public RenameResult doRenameWithDialog() throws RenameDIPException {
		IDipDocumentElement selectedElement = fSelector.getSelectedOneDipDocElement();
		RenameWizard wizard = new RenameWizard(selectedElement);
		WizardDialog wd = new WizardDialog(getShell(), wizard);
		wd.setTitle(wizard.getWindowTitle());
		if (wd.open() == WizardDialog.OK) {
			String newName = wizard.getName();
			boolean reserve = wizard.isNeedReserve();			
			return doRename(selectedElement, newName, reserve);
		}
		return null;
	}

	public RenameResult doRename(IDipDocumentElement dipDocElement,String newName, boolean reserve) throws RenameDIPException {
		String oldName = dipDocElement.name();
		doRenameWithoutResult(dipDocElement, newName, reserve);
		return new RenameResult(DipUtilities.relativeProjectID(dipDocElement.parent()), oldName, newName, reserve);
	}
	
	public void doRenameWithoutResult(IDipDocumentElement dipDocElement, String newName, boolean reserve) throws RenameDIPException {		
		RenameController controller = new RenameController(dipDocElement, newName, reserve, getShell());
		IStatus status = controller.doRename();
		if (status == null || !status.isOK()) {			
			throw new RenameDIPException("Rename Error");
		}
		fEditor.updater().updateAfterRename(dipDocElement.parent(), newName, true);
	}
	
	public boolean canRename() {
		if (fSelector.isOneSelected()) {
			IDipDocumentElement dipDocElement = fSelector.first();
			return dipDocElement.canRename();
		}
		return false;
	}

	// ===============================
	// up-down

	public void doUpTrigger() {
		fTableActionInteractor.getUpAction().run();
	}

	public UpDownResult doUp() {
		if (fSelector.isEmpty()) {
			return null;
		} else if (fSelector.isOneSelected()) {
			return doUpOneElement();
		} else {
			return doUpManyElements();
		}
	}

	public UpDownResult doUp(UpDownResult result) throws DIPException {
		if (result.isOne()) {
			Optional<IDipElement> dipElement = DipUtilities.findDipElementInProject(result.getDipDocElementId(), dipProject());
			if (dipElement.isEmpty()) {
				throw new DIPException("Undo up-down error");
			}			
			return doUpOneElement((IDipDocumentElement) dipElement.get());
		} else {
			List<IDipDocumentElement> dipDocElements = result.getDipDocElementsIds().stream()
			.map(id -> DipUtilities.findDipElementInProject(id, dipProject()))
			.map(Optional::get)
			.filter(IDipDocumentElement.class::isInstance)
			.map(IDipDocumentElement.class::cast)
			.collect(Collectors.toList());
			
			TreeSet<IDipDocumentElement> set = new TreeSet<>(KDipTableSelector.ORDER_COMPARATOR);
			set.addAll(dipDocElements);
			return doUpManyElements(set);			
		}
	}

	private UpDownResult doUpOneElement() {
		IDipTableElement selectedElement = fSelector.getLastSelectObject();
		IDipDocumentElement selectedDipDocElement = selectedElement.dipResourceElement();
		UpDownResult result =  doUpOneElement(selectedElement, selectedDipDocElement);		
		return result;
	}

	public UpDownResult doUpOneElement(IDipDocumentElement dipDocElement) {
		Optional<IDipTableElement> element = fDipTableModel.findElementByName(dipDocElement);
		if (element.isPresent()) {
			return doUpOneElement(element.get(), dipDocElement);
		}
		return null;
	}

	private UpDownResult doUpOneElement(IDipTableElement element, IDipDocumentElement dipDocElement) {
		if (dipDocElement != null) {
			DipTableUtilities.up(dipDocElement);
			element.up();
			fEditor.updater().updateFolderOrder(element.parent());
			fSelector.setTableElementSelection(element);
			return new UpDownResult(true, dipDocElement);
		}
		return null;
	}

	private UpDownResult doUpManyElements() {
		TreeSet<IDipDocumentElement> dipDocElements = fSelector.getSelectedElements();
		return doUpManyElements(dipDocElements);
	}

	private UpDownResult doUpManyElements(TreeSet<IDipDocumentElement> dipDocElements) {
		DipTableUtilities.up(dipDocElements);
		List<IDipTableElement> tableElements = getResourceDipDocElements(dipDocElements);
		ITableNode parent = tableElements.get(0).parent();
		parent.up(tableElements);
		fEditor.updater().updateFolderOrder(parent);
		fSelector.setManyTableElements(tableElements);
		return new UpDownResult(true, dipDocElements);
	}

	public void doDownTrigger() {
		fTableActionInteractor.getDownAction().run();
	}

	public UpDownResult doDown() {
		if (fSelector.isEmpty()) {
			return null;
		} else if (fSelector.isOneSelected()) {
			return doDownOneElement();
		} else {
			return doDownManyElements();
		}
	}

	public UpDownResult doDown(UpDownResult result) throws DIPException {
		if (result.isOne()) {
			Optional<IDipElement> dipElement = DipUtilities.findDipElementInProject(result.getDipDocElementId(), dipProject());
			if (dipElement.isEmpty()) {
				throw new DIPException("Undo down error");
			}
			return doDownOneElement((IDipDocumentElement) dipElement.get());
		} else {
			
			List<IDipDocumentElement> dipDocElements = result.getDipDocElementsIds().stream()
			.map(id -> DipUtilities.findDipElementInProject(id, dipProject()))
			.map(Optional::get)
			.filter(IDipDocumentElement.class::isInstance)
			.map(IDipDocumentElement.class::cast)
			.collect(Collectors.toList());
			
			TreeSet<IDipDocumentElement> set = new TreeSet<>(KDipTableSelector.ORDER_COMPARATOR);
			set.addAll(dipDocElements);
			
			return doDownManyElements(set);
		}
	}

	private UpDownResult doDownOneElement() {
		IDipTableElement element = fSelector.getLastSelectObject();
		IDipDocumentElement dipDocElement = element.dipResourceElement();
		return doDownOneElement(element, dipDocElement);
	}

	public UpDownResult doDownOneElement(IDipDocumentElement dipDocElement) {
		Optional<IDipTableElement> element = fDipTableModel.findElementByName(dipDocElement);
		if (element.isPresent()) {
			return doDownOneElement(element.get(), dipDocElement);
		}
		return null;
	}

	private UpDownResult doDownOneElement(IDipTableElement element, IDipDocumentElement dipDocElement) {
		if (dipDocElement != null) {
			DipTableUtilities.down(dipDocElement);
			element.down();
			fEditor.updater().updateFolderOrder(element.parent());
			fSelector.setTableElementSelection(element);
			return new UpDownResult(false, dipDocElement);
		}
		return null;
	}

	private UpDownResult doDownManyElements() {
		TreeSet<IDipDocumentElement> dipDocElements = fSelector.getSelectedElements();
		return doDownManyElements(dipDocElements);
	}

	public UpDownResult doDownManyElements(TreeSet<IDipDocumentElement> dipDocElements) {
		DipTableUtilities.down(dipDocElements);
		List<IDipTableElement> tableElements = getResourceDipDocElements(dipDocElements);
		IDipTableElement first = tableElements.get(0);
		first.parent().down(tableElements);
		fEditor.updater().updateFolderOrder(first.parent());
		fSelector.setManyTableElements(tableElements);
		return new UpDownResult(false, dipDocElements);
	}

	// ============================
	// into folder

	public void doIntoFolderTrigger() {
		fTableActionInteractor.getDoIntoFolderAction().run();
	}

	public MoveResult doIntoFolder() {	
		return fIntoFolderInteractor.doIntoFolder();
	}

	// =============================
	// delete

	public void doDeleteTrigger() {
		fTableActionInteractor.getDeleteAction().run();
	}
	
	public ResultOperation doDeleteOPeration() {
		try {
			return fDeleteInteractor.doDeleteOPeration();
		} catch (DeleteDIPException | CopyDIPException e) {
			WorkbenchUtitlities.openError("Delete Error", e.getMessage());
			e.printStackTrace();
			return null;
		}
	}
	
	public void deleteElementWithoutUI(IDipDocumentElement dipDocElement) throws DIPException {
		// без tmp
		fDeleteInteractor.deleteElementWithoutUI(dipDocElement);
	}
	
	public void deleteElementWithoutUI(List<IDipDocumentElement> dipDocElements) throws DIPException {
		// без tmp
		fDeleteInteractor.deleteWithoutUiForDipDocElements(dipDocElements);
	}
	
	//====================================
	// paste
		
	public PasteObjectsResult doPaste() {
		return fPasteInteractor.doPaste();
	}
	
	// ==============================
	// open commands

	public void openFile() {
		IDipDocumentElement selectedElement = fSelector.getSelectedOneDipDocElement();
		if (selectedElement instanceof DipUnit) {
			openFile((DipUnit) selectedElement);
		} else if (selectedElement instanceof DipTableContainer) {
			ReqLink.openTable(((DipTableContainer) selectedElement).getTable());
		}
	}

	private void openFile(DipUnit unit) {
		IFile file = (IFile) unit.resource();
		UnitPresentation presentation = unit.getUnitPresentation();
		if (presentation != null && presentation.getUnitType() == UnitType.REPROT_REF) {
			ReportRefPresentation reportPresentation = (ReportRefPresentation) presentation.getPresentation();
			if (reportPresentation != null) {
				file = reportPresentation.getReportFile();
			}
		}
		if (presentation != null && presentation.getUnitType() == UnitType.GLOS_REF) {
			doOpenGlossaryDialog();
			return;
		}
		if (presentation.getUnitType() == UnitType.PAGEBREAK) {
			return;
		}
		
		if (unit.isReadOnly()) {
			WorkbenchUtitlities.openReadOnlyErrorMessage(getShell(), unit);
			return;
		}

		WorkbenchUtitlities.openFile(file);
	}

	public void doOpenGlossaryDialog() {
		GlossaryFolder glosFolder = fModel.dipProject().getGlossaryFolder();
		if (glosFolder == null) {
			return;
		}
		GlossaryDialog dialog = new GlossaryDialog(getShell(), glosFolder);
		dialog.open();
	}

	
	//================================
	// comments
	
	private void doEditComment(int column, int row) {
		EditCommentResult result = fEditCommentInteractor.doEditComment(column, row);
		if (result != null) {
			fActionStack.pushUndoAction(new EditCommentAction(this, result));
		}
	}

	public EditCommentResult doEditCommentInDialog() {
		return fEditCommentInteractor.doEditCommentInDialog();
	}
	
	@Override
	public void updateCommentFromCellEditor(IDipTableElement endElement, IDipDocumentElement dipDocElement, String newComment) {
		String oldComment = dipDocElement.getCommentContent();		
		// update
		fEditCommentInteractor.updateComment(endElement, dipDocElement, newComment);
		// add to undo-redo stack
		if(!Objects.equals(newComment, oldComment)) {
			EditCommentResult result = new EditCommentResult(DipUtilities.relativeProjectID(dipDocElement),
					oldComment, newComment);
			fActionStack.pushUndoAction(new EditCommentAction(this, result));
		}		
	}

	public EditCommentResult doDeleteComment() {
		return fEditCommentInteractor.doRemoveComment();
	}

	// ========================================
	// markdown commands

	public MdExtractResult extractMarkdown() {
		try {
			 return fMdActionItenractor.extract();
		} catch (TmpCopyException e) {
			WorkbenchUtitlities.openError("Extract Markdown Error", e.getMessage());
			e.printStackTrace();
			return null;
		}
	}

	public MdExtractResult splitMarkdown() {
		try {
			return fMdActionItenractor.split();
		} catch (CoreException | IOException e) {
			WorkbenchUtitlities.openError("Split Markdown Error", e.getMessage());
			e.printStackTrace();
			return null;
		}
	}

	public MDJoinResult joinMarkdown() {
		try {
			return fMdActionItenractor.join();
		} catch (TmpCopyException | DeleteDIPException e) {
			WorkbenchUtitlities.openError("Join Markdown Error", e.getMessage());
			e.printStackTrace();
			return null;
		}
	}

	// =========================================
	// enable settings command

	public void doHighlightGlossary(boolean selection) {
		fHighlightGlossMode = selection;
		fModel.dipProject().getProjectProperties().setHighlightGlossMode(fHighlightGlossMode);
		fEditor.updater().updateTextElements();
	}

	public void doCheckSpellingEnable(boolean checkSpellingEnable) {
		fCheckSpellingEnable = checkSpellingEnable;
		fModel.dipProject().getProjectProperties().setCheckSpellingEnable(fCheckSpellingEnable);
		fEditor.updater().updateTextElements();
	}

	public void doShowMDComment() {
		fShowMDComment = !fShowMDComment;
		fModel.dipProject().getProjectProperties().setMDComment(fShowMDComment);
	}
	
	public void doShowStrictMdComments() {
		fShowStrictMDComment = !fShowStrictMDComment;
		fModel.dipProject().getProjectProperties().setStrictMDComment(fShowStrictMDComment);
	}

	public void doShowFormNumeration() {
		fShowFormNumeration = !fShowFormNumeration;
		fModel.dipProject().getProjectProperties().setFormNumeration(fShowFormNumeration);
		fEditor.updater().updateFormElements(false);
	}

	public void doFormFilterPreferenciesEnable() {
		fFormShowPreferenciesEnable = !fFormShowPreferenciesEnable;
		fModel.dipProject().getProjectProperties().setFormFilterPreferenciesEnable(fFormShowPreferenciesEnable);
		fEditor.updater().updateFormElements(true);
		updateTable();
	}

	public void changeFixContentMode(boolean selection) {
		fFixedContent = selection;
		asyncRefreshTree();
		fModel.dipProject().getProjectProperties().setFixedContentMode(fFixedContent);
	}

	public void doHideDisableObjsEnable(boolean disableObjEnable) {
		fHideDisableObjEnable = disableObjEnable;
		fModel.dipProject().getProjectProperties().setHideDisableObjsEnable(fHideDisableObjEnable);
		fDipTableModel.computeElements();
		fEditor.refreshDipToc();
		refreshTable();
	}
	
	public void setShowVersionForm(boolean selection) {
		fShowFormVersion = selection;
		fEditor.updater().updateIdentificators();
		fTable.redraw();
		fModel.dipProject().getProjectProperties().setShowFormVersion(fShowFormVersion);
	}
	
	// ==========================
	// filter
	
	private List<IFindable> findables;

	public boolean applyFilter(String filter) {
		RuleScanner scanner = new RuleScanner();
		Condition condition = scanner.scan(filter);
		
		if (isFilterMode()) {
			resetFilter();
		}
			
		if (!FilterValidator.isValidFilter(condition, fModel.dipProject())) {
			return false;
		}
		
		fDipTableModel.setFilter(condition);	
		findables = null;
		
		Set<String> findTexts = condition.getNotCaseSensetiveTexts();
		Set<String> findCaseTexts = condition.getCaseSensetiveTexts();
		Set<String> findWords = condition.getNotCaseSensetiveWords();
		Set<String> findCaseWords = condition.getCaseSensetiveWords();
				
		if (!findTexts.isEmpty() || !findCaseTexts.isEmpty() || !findWords.isEmpty() || !findCaseWords.isEmpty()) {		
			findables = fDipTableModel.getElements().stream()
					.map(IDipTableElement::dipDocElement)
					.filter(dipDocElement -> dipDocElement instanceof IFindable && !(dipDocElement instanceof IEmptyResultFindable))
					.map(IFindable.class::cast)
					.collect(Collectors.toList());
			for (IFindable findable: findables) {
				for (String str: findTexts) {
					findable.appendFind(str, false);
				}
				for (String str: findCaseTexts) {
					findable.appendFind(str, true);
				}
				for (String str: findWords) {
					findable.appendWord(str, false);
				}
				for (String str: findCaseWords) {
					findable.appendWord(str, true);
				}
			}
			setFindMode(true);			
		}

		fDipTableModel.updateAllElements();	
		refreshTable();	
		return true;
	}
	
	public void resetFilter() {
		if (fDipTableModel.isFilterMode()) {
			fDipTableModel.resetFilter();
			setFindMode(false);
			if (findables != null) {
				findables.forEach(IFindable::cleanFind);
			}
			fDipTableModel.updateAllElements();
			fSelector.updateCurrentSelection();
			refreshTable();
		}
	}

	//==========================
	// apply diff
	
	public void applyDiffMode() {
		if (!fDinamicallyDiffMode) {
			fDiffInteractor.diffDocument();
			fDinamicallyDiffMode = true;
		}
	}
	
	public OrientationResult doSetHorizontalOrientation(boolean horizontal) {
		IDipDocumentElement selectedElement = fSelector.getSelectedOneDipDocElement();
		if (selectedElement instanceof DipUnit) {
			return doSetHorizontalOrientation((DipUnit) selectedElement, horizontal);
		}
		return null;
	}
	
	public OrientationResult doSetHorizontalOrientation(DipUnit unit, boolean horizontal) {
		unit.setHorizontalOrientation(horizontal);
		fEditor.updater().saveWithAdditionalUpdate(unit.parent());
		return new OrientationResult(DipUtilities.relativeProjectID(unit), !horizontal);
	}
	
	public PageBreakResult doSetPageBreak(String pageBreak) {
		IDipDocumentElement selectedElement = fSelector.getSelectedOneDipDocElement();
		if (selectedElement instanceof IDipParent) {
			return doSetPageBreak(pageBreak, (IDipParent) selectedElement);
		}
		return null;
	}
	
	public PageBreakResult doSetPageBreak(String pageBreak, IDipParent parent) {
		String oldValue = parent.getPageBreak();
		parent.setPageBreak(pageBreak);
		fEditor.updater().saveWithAdditionalUpdate(parent);
		return new PageBreakResult(DipUtilities.relativeProjectID(parent), oldValue, pageBreak);
	}
	
	private void doSelectUp() {
		IDipTableElement element = fSelector.getLastSelectObject();
		if (element != null) {
			element = element.startElement(HideElements.EXCLUDE);
			int index = fDipTableModel.getElements().indexOf(element) - 1;
			if (index >= 0) {
				IDipTableElement upElement = fDipTableModel.getElements().get(index);
				fSelector.setTableElementSelection(upElement);
			}
		}
	}

	private void doSelectDown() {
		IDipTableElement element = fSelector.getLastSelectObject();
		if (element != null) {
			element = element.endElement(HideElements.EXCLUDE);
			int index = fDipTableModel.getElements().indexOf(element) + 1;
			if (index < fDipTableModel.getElements().size()) {
				IDipTableElement downElement = fDipTableModel.getElements().get(index);
				fSelector.setTableElementSelection(downElement);
			}
		}
	}
	
	public SortedResult doSort() {
		try {
			IDipParent tableContainer = fSelector.selectedTableContainer();
			if (tableContainer == null) {
				return null;
			}
			String message = Messages.KTableComposite_DirectoryContent + tableContainer.name()
					+ Messages.KTableComposite_WillBeSortLabel;
			boolean confirm = MessageDialog.openQuestion(getShell(), Messages.KTableComposite_SortingShellTitle,
					message);
			if (!confirm) {
				return null;
			}
			
			List<String> oldOrder = tableContainer.getDipDocChildrenList().stream()
					.map(IDipDocumentElement::name).collect(Collectors.toList());			
			doSort(tableContainer);
			MessageDialog.openInformation(getShell(), Messages.KTableComposite_SortingShellTitle2,
					Messages.KTableComposite_SortedComplete);
			
			return new SortedResult(DipUtilities.relativeProjectID(tableContainer), oldOrder);
			
		} catch (ParserConfigurationException | IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public void doSort(IDipParent parent) throws ParserConfigurationException, IOException {
		parent.sort();
		fEditor.updater().updateParent(parent);	
	}
	

	public void doOpenID() {
		fSelector.openByID();
	}

	public void doShowID() {
		fSelector.showByID();
	}

	public List<String> doDisableObject() {
		return doDisable(fSelector.getSelectedElements());
	}
	
	public List<String> doDisable(Collection<IDipDocumentElement> dipDocElements) {
		List<String> resultIds = new ArrayList<>();
		dipDocElements.forEach(dipDocElement -> {
			boolean rename = true;
			if (dipDocElement instanceof DipUnit && !TableSettings.isRenameDisableFile()) {
				rename = false;
			} else if (dipDocElement instanceof IDipParent && !TableSettings.isRenameDisableFolder()) {
				rename = false;
			}
			DipUtilities.doDisableObject(dipDocElement, getShell(), rename);
			resultIds.add(DipUtilities.relativeProjectID(dipDocElement));
		});
		fEditor.updater().updateAfterDelete(dipDocElements.stream().toArray(IDipDocumentElement[]::new));		
		deselect();
		return resultIds;
	}
	
	public void updateBackgrouColor() {
		fColorInteractor.updateBackgrouColor();
	}

	// ==========================================
	// update

	/**
	 * Обновить полность модель
	 */
	public void setTableModel(TableModel model) {
		fModel = model;
		fDipTableModel.setTableModel(fModel);
		tableUpdate();
		applyListMode();
	}

	public void resourceUpdate() {
		DipRoot.getInstance().clear();
		ResourcesUtilities.updateRoot();
		WorkbenchUtitlities.updateInputProjectExplorer();
		WorkbenchUtitlities.updateNavigatorServiceExplorer();
		WorkbenchUtitlities.updateProjectExplorer();
	}

	public void tableUpdate() {
		IWorkbench wb = PlatformUI.getWorkbench();
		IProgressService ps = wb.getProgressService();
		try {
			ps.busyCursorWhile(new IRunnableWithProgress() {
				public void run(IProgressMonitor pm) {
					Display.getDefault().asyncExec(() -> {
						updateBackgrouColor();
						fDipTableModel.globalUpdate();
						refreshTable();
						fEditor.fireUpdateTableComposite();
					});
				}
			});
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public void asyncRefreshTree() {
		IWorkbench wb = PlatformUI.getWorkbench();
		IProgressService ps = wb.getProgressService();
		try {
			ps.busyCursorWhile(new IRunnableWithProgress() {
				public void run(IProgressMonitor pm) {
					Display.getDefault().syncExec(() -> {
						updateTable();
						fEditor.fireUpdateTableComposite();
						fTable.redraw();
					});
				}
			});
		} catch (InvocationTargetException | InterruptedException e) {
			e.printStackTrace();
		}
	}

	private void updateTable() {
		if (fDipTableModel.presentationWidth() < 10) {
			return;
		}
		fDipTableModel = (DipTableModel) fTable.getModel();
		fDipTableModel.computeElements();
		fDipTableModel.updateAllElements();
		fTable.setModel(fDipTableModel);
	}

	public void refreshElements(Collection<IDipTableElement> elements) {
		fDipTableModel.updateElementsWithChild(elements);
		refreshTable();
	}

	public void refreshNode(ITableNode node) {
		fDipTableModel.updateElements(List.of(node));
		fDipTableModel.updateElementsWithChild(node.children());
		fTable.setModel(fDipTableModel);
	}

	public void refreshTable() {
		fDipTableModel = (DipTableModel) fTable.getModel();
		fTable.setModel(fDipTableModel);
	}

	
	public void prepareElement(TableElement tableElement) {
		fDipTableModel.updateElements(List.of(tableElement));
	}
	
	public void selectElement(IDipTableElement tableElement) {
		fSelector.setTableElementSelection(tableElement);
	}
		
	/**
	 * Обновить заголовки таблицы
	 */
	private void updateHeaders() {
		refreshTable();
	}

	// ================================
	// dispose

	@Override
	public void dispose() {
		disposeListeners();
		fPasteInteractor.dispose();
		removeKeyListener();
		
		//removeControlListener(compositeControlLlistener);
		fCompositeControlLlistener = null;
		
		fActionStack = null;		
		fEditor = null;
		
		fDipTableModel.dispose();		
		fDipTableModel = null;
		
		if (!fTable.isDisposed()) {
			fTable.removeControlListener(controlListener);
			fTable.removeCellResizeListener(fTableCellResizeListener);
			fTable.removeDisposeListener(fDisposeListener);
			fTable.removeMouseWheelListener(fTableMouseWheelListener);
			fTable.removeFocusListener(fFocusLostListener);
			fTable.removeMouseListener(fMouseListener);
			fTable.removeKeyListener(fKeyListener);
		}

		controlListener = null;
		fTableCellResizeListener = null;
		fTableCellResizeListener = null;
		fDisposeListener = null;
		fTableMouseWheelListener = null;
		fFocusLostListener = null;		
		fMouseListener = null;
		fKeyListener = null;
		
		fTable.dispose();
		fTable = null;
		fModel = null;
		fCellEditorsManager = null;
		fSelector = null;
		fColorInteractor = null;
		fSizeInteractor = null;
		fTableActionInteractor = null;
		fDiffInteractor = null;
		fDropAction = null;
 		fMdActionItenractor = null;
		fActionStack = null;
		fImportActionInteractor = null;
		fCreateFileInteractor = null;
		fIncludeInteractor = null;		
		fDeleteInteractor = null;
		fEditDescriptionInteractor = null;
		fEditCommentInteractor = null;
		fAutoNumberingInteractor = null;
		fIntoFolderInteractor = null;
		fPasteInteractor = null;
		fCopyIdIneractor = null;
		fFormSettings = null;
		findables = null;
		super.dispose();
	}

	private void disposeListeners() {
		Display.getDefault().removeListener(SWT.KeyUp, fKeyUpListener);
		Display.getDefault().removeListener(SWT.KeyDown, fKeyDownListner);
	}

	// ================================
	// utils

	private List<IDipTableElement> getResourceDipDocElements(Collection<IDipDocumentElement> redipDocElements) {
		return redipDocElements.stream().map(fDipTableModel::findElementByName).map(Optional::get)
				.collect(Collectors.toList());
	}

	// ===============================
	// getters & setters

	public DipTableEditor editor() {
		return fEditor;
	}

	public DipTable table() {
		return fTable;
	}

	public DipTableModel tableModel() {
		return fDipTableModel;
	}

	public DipProject dipProject() {
		return fModel.dipProject();
	}

	public TableSizeInteractor sizeInteractor() {
		return fSizeInteractor;
	}

	public TableModel model() {
		return fEditor.model();
	}

	@Override
	public KDipTableSelector selector() {
		return fSelector;
	}

	public boolean isHideDisableObjs() {
		return fHideDisableObjEnable;
	}

	@Override
	public boolean isHighlightGloss() {
		return fHighlightGlossMode;
	}

	@Override
	public boolean isFindMode() {
		return fFindMode;
	}

	public void setFindMode(boolean newValue) {
		fFindMode = newValue;
	}

	@Override
	public boolean isOneListMode() {
		return fOneListMode;
	}

	@Override
	public boolean isCheckSpellingEnable() {
		return fCheckSpellingEnable;
	}

	@Override
	public boolean isShowMdComment() {
		return fShowMDComment;
	}
	
	public boolean isShowStrictMdComment() {
		return fShowStrictMDComment;
	}

	@Override
	public boolean isShowFormNumeration() {
		return fShowFormNumeration;
	}

	@Override
	public boolean isFormShowPrefernciesEnable() {
		return fFormShowPreferenciesEnable;
	}

	@Override
	public boolean isFixedContent() {
		return fFixedContent;
	}

	@Override
	public boolean isShowNumeration() {
		return fShowNumeration;
	}
	
	@Override
	public boolean isShowFormVersion() {
		return fShowFormVersion;
	}

	public boolean isCtrlPressed() {
		return fCtrlPressed;
	}

	public boolean isFilterMode() {
		return fDipTableModel.isFilterMode();
	}

	public void setDiffMode(boolean value) {
		fDiffMode = value;
	}

	public void setOnlyDiffMode(boolean value) {
		fOnlyDiffMode = value;
	}

	public boolean isDiffMode() {
		return fDiffMode;
	}

	public boolean isOnlyDiffMode() {
		return fOnlyDiffMode;
	}

	public boolean isDinamicallyDiffMode() {
		return fDinamicallyDiffMode;
	}
	
	public void setDinamicallyDiffMode(boolean value) {
		fDinamicallyDiffMode = value;
	}
	
	public DropAction dropAction() {
		return fDropAction;
	}

	public ActionStack actionStack() {
		return fActionStack;
	}

	public ImportActionInteractor getImportActionInteractor() {
		return fImportActionInteractor;
	}
	
	public CreateFileInteractor getCreateFileInteractor() {
		return fCreateFileInteractor;
	}
	
	public IncludeInteractor getIncludeInteractor() {
		return fIncludeInteractor;
	}
	
	public DeleteFileInteractor getDeleteInteractor() {
		return fDeleteInteractor;
	}
	
	public EditDescriptionInteractor getEditDescriptionInteractor() {
		return fEditDescriptionInteractor;
	}
	
	public EditCommentInteractor getEditCommentInteractor() {
		return fEditCommentInteractor;
	}
	
	public AutoNumberingInteractor getAutoNumberingInteractor() {
		return fAutoNumberingInteractor;
	}

	public IntoFolderInteractor getIntoFolderInteractor() {
		return fIntoFolderInteractor;
	}
	
	public PasteInteractor getPasteInteractor() {
		return fPasteInteractor;
	}
	
	public CopyIdIneractor copyIdInteractor() {
		return fCopyIdIneractor;
	}
	
	@Override
 	public IFormSettings getFormSettings() {
		return fFormSettings;
	}

	public CellEditorManager cellEditorManager() {
		return fCellEditorsManager;
	}
	
	@Override
	public IFinder getFinder() {
		return fFinder;
	}

	@Override
	public int tableHeight() {
		return tableModel().tableHeight();
	}



}

