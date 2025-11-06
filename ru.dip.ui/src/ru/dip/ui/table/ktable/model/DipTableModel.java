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
package ru.dip.ui.table.ktable.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.eclipse.swt.graphics.Point;

import de.kupzog.ktable.KTableCellEditor;
import de.kupzog.ktable.KTableCellRenderer;
import de.kupzog.ktable.KTableModel;
import ru.dip.core.DipCorePlugin;
import ru.dip.core.model.DipFolder;
import ru.dip.core.model.DipProject;
import ru.dip.core.model.DipUnit;
import ru.dip.core.model.interfaces.IDipDocumentElement;
import ru.dip.core.model.interfaces.IDipElement;
import ru.dip.core.model.interfaces.IDipParent;
import ru.dip.core.model.interfaces.IDipUnit;
import ru.dip.core.model.interfaces.IUnitPresentation;
import ru.dip.core.report.model.condition.Condition;
import ru.dip.core.unit.UnitPresentation;
import ru.dip.core.unit.UnitType;
import ru.dip.ktable.DipTable;
import ru.dip.ui.table.ktable.KTableComposite;
import ru.dip.ui.table.ktable.actions.edit.EditUnitAction;
import ru.dip.ui.table.ktable.celleditors.CellInfo;
import ru.dip.ui.table.ktable.render.CommentCellRender;
import ru.dip.ui.table.ktable.render.CommentPainter;
import ru.dip.ui.table.ktable.render.HeaderCellRender;
import ru.dip.ui.table.ktable.render.IDCellRender;
import ru.dip.ui.table.ktable.render.IDPainter;
import ru.dip.ui.table.ktable.render.PresentationCellRender;
import ru.dip.ui.table.ktable.render.PresentationPainter;
import ru.dip.ui.table.table.TableModel;

public class DipTableModel implements KTableModel, IDipTableModel {
	
	private final DipTable fTable;
	private final KTableComposite fTableComposite;
	private TableNode fRootNode;
	private TableModel fRootTableModel;
	private List<ParentNode> fParentsElements; 

	// renderer & painters
	private final IDCellRender fIdRender;
	private final PresentationCellRender fPresentationRender;
	private final HeaderCellRender fHeaderRender;
	private final CommentCellRender fCommentCellRender;
	private final PresentationPainter fPainter;
	private final IDPainter fIDPainter;
	private final CommentPainter fCommentPainter;

	private int fColumnCount = 3;
	
	// ширина столбцов
	private int fIdWidth = 300;
	private int fPresentationWidth = 600;
	private int fCommentWidth = 300;

	// номера столбцов
	private int fIdColumnNumber = 0;
	private int fPresentationColumnNumber = 1;
	private int fCommentColumnNumber = 2;
	
	// первая ячейка и отступ (для IDipTableModel)
	private int fFirstCell = -1;
	private int fIndent = 0;
	
	// номер и высота ячейки открытой для редактирования
	private int fOpenEditingRow = -1;   // 
	private int fCellEditorHeight = 0;
	private CellInfo fEditedCell; // измененная ячейка после редактирования
	
	private List<IDipTableElement> fElements = new ArrayList<>();
	private boolean fNeedUpdate = false;
	private List<IDipTableElement> fNewElements;  // используются при обновлении

	// фильтр
	private Condition fCondition;
	
	public DipTableModel(DipTable table, TableModel tableModel, KTableComposite tableComposite) {
		fTable = table;
		fRootTableModel = tableModel;
		fTableComposite = tableComposite;
		fRootNode = TableNode.rootNode(this, fRootTableModel/*.getContainer()*/); 
		fRootNode.setExpand(true);
		fIdRender = new IDCellRender(this);
		fPainter = new PresentationPainter(fTableComposite);
		fIDPainter = new IDPainter(fTableComposite);
		fPresentationRender = new PresentationCellRender(this);
		fHeaderRender = new HeaderCellRender(this);
		fCommentCellRender = new CommentCellRender(this);
		fCommentPainter = new CommentPainter(fTableComposite);
	
		readIdMode();
		readCommentMode();

		updateColumnCount();
		computeElements();
	}

	// ===========================
	// модель
	
	public void computeElements() {
		// если only diff mode
		if (fTableComposite.isDiffMode() && fTableComposite.isOnlyDiffMode()) {
			computeDiffElements();
			return;
		}
		// если применен фильтр		
		if (fCondition != null) {
			computeElements(fCondition);
			return;
		}
		fElements = new ArrayList<>();
		// add parents nodes
		if (!(fRootNode.dipDocElement() instanceof DipProject)) {
			fElements.addAll(parentElements(fRootNode));
			fElements.add(fRootNode);
		}
		fElements.addAll(fRootNode.allChildren());
		clearFirstElement();
	}
	
	public void computeElements(Condition condition) {
		fElements = new ArrayList<>();
		// add parents nodes
		if (!(fRootNode.dipDocElement() instanceof DipProject)) {
			fElements.addAll(parentElements(fRootNode));
			fElements.add(fRootNode);
		}
		fElements.addAll(fRootNode.allChildren(condition));
		clearFirstElement();	
	}
			
	
	public void computeDiffElements() {
		fElements = new ArrayList<>();
		// add parents nodes
		if (!(fRootNode.dipDocElement() instanceof DipProject)) {
			fElements.addAll(parentElements(fRootNode));
			fElements.add(fRootNode);
		}
		fElements.addAll(fRootNode.allDiffChildren(null));
		clearFirstElement();
	}
	

	
	/**
	 * Новые элементы вычисляются отдельно 
	 * (для решения проблемы с потоками, не успевает отрисовать, когда идут вычисления)
	 */
	public void computeNewElements(ITableNode node){		
		// если only diff mode
		if (fTableComposite.isDiffMode() && fTableComposite.isOnlyDiffMode()) {
			computeDiffNewElements(node);
			return;
		}
		
		fNewElements = new ArrayList<>();		
		if (!(fRootNode.dipDocElement() instanceof DipProject)) {						
			fNewElements.addAll(parentElements(fRootNode));
			fNewElements.add(fRootNode);
		}
		fNewElements.addAll(fRootNode.allChildren());
	}
	
	public void computeDiffNewElements(ITableNode node) {
		fNewElements = new ArrayList<>();
		// add parents nodes
		if (!(fRootNode.dipDocElement() instanceof DipProject)) {
			fNewElements.addAll(parentElements(fRootNode));
			fNewElements.add(fRootNode);
		}
		fNewElements.addAll(fRootNode.allDiffChildren(node));
		clearFirstElement();
	}
	
	public void applyNewElements() {
		fElements = fNewElements;
	}
	
	private List<ParentNode> parentElements(ITableNode node) {
		if (fParentsElements == null) {
			fParentsElements = new ArrayList<>();
			IDipParent parentFolder = node.dipDocElement().parent();
			while (parentFolder instanceof DipFolder) {
				ParentNode parentNode = new ParentNode(this, parentFolder, null);
				fParentsElements.add(0, parentNode);
				parentFolder = parentFolder.parent();
			}
		}
		return fParentsElements;
	}

	public boolean isParent(IDipElement element) {
		return fRootTableModel.isParentHeader(element) || fRootTableModel.isTable(element);
	}
	
	@Override
 	public int getRowCount() {
		return fElements.size() + 1;
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

	/*
	 * Заменить возвращаемы тип на TableElement, создать тип для header, 
	 * чтобы не делать каст постоянно
	 */
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
	
	//============================
	// UPDATE
	
	/**
	 * Установить новую модель
	 */
	public void setTableModel(TableModel fModel) {		
		fRootTableModel = fModel; 
	}
	
	/**
	 * Глобальное обновление
	 */
	public void globalUpdate() {
		List<IDipParent> expandedNodes = expandedNodes();
		IDipDocumentElement selectReq = fTableComposite.selector().getSelectedOneDipDocElement();
		
		// из-за этой строчки обновляются dnfo, собственно проект обновляется при
		// получении фокуса, либо ставить флаг в DipEditor, что не изменен 		
		//fTableComposite.model().getContainer().dipProject().refresh();fModelChanged = false

		fParentsElements = null;
		fRootNode = TableNode.rootNode(this, fRootTableModel);
		fRootNode.setExpand(true);
			
		setExpandNodes(expandedNodes, fRootNode);	
		computeElements();
		updateAllElements();
		fTableComposite.selector().setSelection(selectReq);
	}
	
	/**
	 * Возвращает Expanded-узлы
	 */
	private List<IDipParent> expandedNodes(){
		List<IDipParent> result =  fElements.parallelStream()
		.filter(TableNode.class::isInstance)
		.map(TableNode.class::cast)
		.filter(TableNode::expand)
		.map(TableNode::dipDocElement)
		.collect(Collectors.toList());
		result.add(fRootNode.dipDocElement());
		return result;
	}
	
	/**
	 *  Устанвливает Expanded-узлы
	 */
	private void setExpandNodes(List<IDipParent>  expandedNodes, TableNode node) {		
		if (expandedNodes.contains(node.dipDocElement())) {
			expandedNodes.remove(node.dipDocElement());
			node.setExpand(true);
			for (IDipTableElement element: node.children()) {
				if (element instanceof TableNode) {
					setExpandNodes(expandedNodes, (TableNode) element);
				}
			}			
		}
	}
			
	/** 
	 * Обновить все элементы
	 */
 	public void updateAllElements() {
		updateElements(fElements);
 	}
	
 	/**
 	 * Обновить указанные элементы + дочерние 
 	 * (если включен FixedContent, т.к. меняются дочерние элементы при выделении папки)
 	 */
 	public void updateElementsWithChild(Collection<IDipTableElement> elementsToUpdate) {
		List<IDipTableElement> elements = new ArrayList<>();
		elementsToUpdate.forEach(element -> computeElementsForUpdate(elements, element));
		updateElements(elements);
	}
	
 	/**
 	 * Добавляет дочерние элементы для режима FixedContent
 	 */
 	private void computeElementsForUpdate(List<IDipTableElement> elementsToUpdate, IDipTableElement element) {
 		elementsToUpdate.add(element);
 		if (element instanceof TableNode) {
 			TableNode node = (TableNode) element;
 			if (node.expand()) {
 				for (IDipTableElement child: node.children()) {
 	 				computeElementsForUpdate(elementsToUpdate, child);
 				}
 			}
 		} 
 	}
 		
	/**
	 * Обновляет элементы
	 */
	public void updateElements(List<IDipTableElement> elements) {
		// флаг, чтобы не срабатывал слушатель на GIT
		if (dipProject().getGitRepo() != null) {
			DipCorePlugin.getDefault().setCurrentRepo(dipProject().getGitRepo().getDirectory().toString());
		}

		// подготовка идентификаторов
		if (isShowId()) {
			elements.parallelStream()
				.map(TableElement.class::cast)
				.forEach(DipTableModel.this::prepareID);
		}
		
		// подготовка комментариев
		if (isShowComment()) {
			elements.stream()
			.map(TableElement.class::cast)
			.forEach(DipTableModel.this::prepareComment);
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
			.map(TableElement.class::cast)
			.forEach(DipTableModel.this::preparePresentation);
		notParallel.stream()
			.map(TableElement.class::cast)
			.forEach(DipTableModel.this::preparePresentation);
		
		// снимаем флаг
		DipCorePlugin.getDefault().setCurrentRepo(null);
	}
	
	/**
	 * Обновляет идентификаторы (например после переименования)
	 */
	public void updateIdentificators(List<IDipTableElement> elementsToUpdate) {
		// флаг, чтобы не срабатывал слушатель на GIT
		tableComposite().editor().setFullUpdate(true);
		if (dipProject().getGitRepo() != null) {
			DipCorePlugin.getDefault().setCurrentRepo(dipProject().getGitRepo().getDirectory().toString());
		}
		
		if (isShowId()) {
			List<IDipTableElement> elements = new ArrayList<>();
			elementsToUpdate.forEach(element -> computeElementsForUpdate(elements, element));
			elements.parallelStream().forEach(el -> prepareID(el));
		}
		
		// снимаем флаг
		tableComposite().editor().setFullUpdate(false);
		DipCorePlugin.getDefault().setCurrentRepo(null);
	
		tableComposite().refreshTable();
	}
	

	private void preparePresentation(TableElement element) {
		int indent = 0;
		if (fPresentationColumnNumber == 0 && !fTableComposite.isOneListMode()) {
			indent = getIndent(element) * 16;
		}		
		if (indent > 0) {
			indent += 16;
		}
		element.prepare(presentationWidth() - indent, this);
	}
	
	private void prepareComment(TableElement element) {
		if (isShowComment()) {
			commentPainter().prepare(fCommentWidth, element);
		}
	}
	
	private void prepareID(IDipTableElement element) {
		if (isShowId()) {
			idPainter().prepare((IDipTableElement) element);
		}	
	}
	
	/**
	 * Обновление папок (на случай изменения нумерации)
	 * Только Presentation
	 */
	public void updateNodes() {
		fElements.stream()
		.filter(TableNode.class::isInstance)
		.filter(IDipTableElement::isVisible)
		.forEach(this::updatePresentation);
	}
	
	public void updateDescriptions() {
		fElements.stream()
		.filter(IDipTableElement::notEmptyDescription)
		.forEach(this::updatePresentation);
	}
	
	public void updateDescriptionsInNode(ITableNode node) {
		node.children()
		.stream()
		.filter(IDipTableElement::notEmptyDescription)
		.forEach(this::updatePresentation);
	}
	
	public void updateFormPresentations() {
		fElements.stream()
		.filter(IDipTableElement::isFormPresentation)
		.forEach(this::updatePresentation);
	}
	
	private void updatePresentation(IDipTableElement element) {
		int indent = 0;
		if (fPresentationColumnNumber == 0 && !fTableComposite.isOneListMode()) {
			indent = getIndent(element) * 16;
		}		
		if (indent > 0) {
			indent += 16;
		}
		element.prepare(presentationWidth() - indent, this);
		
		if (isShowComment()) {
			commentPainter().prepare(fCommentWidth, element);
		}	
	}
	
	public void updateTableFont() {
		if (isShowId()) {
			updateIDColumn();
		}
		if (isShowComment()) {
			updateCommentColumn();
		}
		updatePresentationFont();
	}
	
	public void updatePresentationFont() {
		fElements.stream()
			.filter(IDipTableElement::hasFontPresentation)
			.forEach(this::updatePresentation);
	}
	
	private void updateIDColumn() {
		List<IDipTableElement> elementsToUpdate = fElements.stream()
				.filter(IDipTableElement::isVisible)
				.collect(Collectors.toList());
		elementsToUpdate.parallelStream().forEach(element -> idPainter().onlyMeasureElements((IDipTableElement) element));
	}
	
	private void updateCommentColumn() {
		fElements.stream().filter(IDipTableElement::isVisible).forEach(element -> {
			commentPainter().prepare(fCommentWidth, element);
		});
	}
	
	// ========================================
	// hide/show columns

	// first column mode
	public static final int SHOW_LAST_ID = 3;
	public static final int SHOW_ID = 1;
	public static final int HIDE_ID = 2;

	// review column mode
	public static final int NOT_COMMENT = 1;
	public static final int FIXED_COMMENT = 2;
	public static final int FULL_COMMENT = 3;

	public static final int DEFAULT_HEIGHT = 30;
	
	private int fIdColumnMode = SHOW_ID;
	private int fCommentMode = FIXED_COMMENT;

	public void readIdMode() {
		fIdColumnMode = dipProject().getProjectProperties().getIDShowMode();
		if (isHideID()) {
			fIdWidth = 0;
		}
	}

	public boolean isShowFirstID() {
		return fIdColumnMode == SHOW_ID;
	}

	public boolean isHideID() {
		return fIdColumnMode == HIDE_ID;
	}

	public boolean isLastOrderID() {
		return fIdColumnMode == SHOW_LAST_ID;
	}

	public void changeIdMode(int idMode) {
		if (idMode == HIDE_ID) {
			hideId();
		} else {
			showId();
		}

		fIdColumnMode = idMode;
		updateColumnCount();
		dipProject().getProjectProperties().setIDShowMode(fIdColumnMode);
		
		fTableComposite.asyncRefreshTree();
	}

	private void hideId() {
		fPresentationWidth += fIdWidth;
		fIdWidth = 0;
	}

	private void showId() {
		if (isHideID() || fIdWidth <= 0) {
			fIdWidth = 250;
			fPresentationWidth -= fIdWidth;
		}
	}

	public boolean checkIdColumnMode() {
		if (fIdColumnMode != dipProject().getProjectProperties().getIDShowMode()) {
			fIdColumnMode = dipProject().getProjectProperties().getIDShowMode();
			updateColumnCount();
			return true;
		}
		return false;
	}
	
	public void readCommentMode() {
		fCommentMode = dipProject().getProjectProperties().getReviewMode();
		if (isNotComment()) {
			fCommentWidth = 0;
		}
	}

	public void setCommentMode(int commentMode) {
		if (commentMode == NOT_COMMENT) {
			hideComment();
		} else {
			showComment();
		}

		fCommentMode = commentMode;
		updateColumnCount();
		dipProject().getProjectProperties().setReviewMode(fCommentMode);
		fTableComposite.asyncRefreshTree();
	}

	private void hideComment() {
		fPresentationWidth += fCommentWidth;
		fCommentWidth = 0;
	}

	private void showComment() {
		if (isNotComment() || fCommentWidth <= 0) {
			fCommentWidth = 250;
			fPresentationWidth -= fCommentWidth;
		}
	}

	public boolean isNotComment() {
		return fCommentMode == NOT_COMMENT;
	}

	public boolean isFixedComment() {
		return fCommentMode == FIXED_COMMENT;
	}

	public boolean isFullComment() {
		return fCommentMode == FULL_COMMENT;
	}

	public boolean checkReviewMode() {
		if (fCommentMode != dipProject().getProjectProperties().getReviewMode()) {
			fCommentMode = dipProject().getProjectProperties().getReviewMode();
			updateColumnCount();
			return true;
		}
		return false;
	}

	/**
	 * Обновляет количество и порядок столбцов в зависимости от IdColumnMode и
	 * CommentMode
	 */
	public void updateColumnCount() {
		fColumnCount = 1;
		fCommentColumnNumber = -1;
		if (isShowFirstID()) {
			fColumnCount++;
			fIdColumnNumber = 0;
			fPresentationColumnNumber = 1;
			if (isShowComment()) {
				fColumnCount++;
				fCommentColumnNumber = 2;
			}
		} else if (isHideID()) {
			fIdColumnNumber = -1;
			fPresentationColumnNumber = 0;
			if (isShowComment()) {
				fColumnCount++;
				fCommentColumnNumber = 1;
			}
		} else {
			fColumnCount++;
			fPresentationColumnNumber = 0;
			if (isShowComment()) {
				fColumnCount++;
				fCommentColumnNumber = 1;
				fIdColumnNumber = 2;
			} else {
				fIdColumnNumber = 1;
			}
		}
		checkColumnWidth();
	}

	public void checkColumnWidth() {
		if (isShowId() && fIdWidth <= 0) {
			fIdWidth = 200;
		}
		if (isShowComment() && fCommentWidth <=0) {
			fCommentWidth = 200;
		}
		fPresentationWidth = tableWidth() - fCommentWidth - fIdWidth; 
	}
	
	@Override
	public int getColumnCount() {
		return fColumnCount;
	}

	// =========================================
	// column width

	/**
	 * 25%, 50%, 25% - для трех столбцов 30% | 70% - для двух столбцов
	 */
	public void setStandartWidth() {
		int tableWidth = fTableComposite.getBounds().width;
		if (tableWidth <= 0) {
			return;
		}
		if (fColumnCount == 3) {
			fPresentationWidth = tableWidth / 2;
			fIdWidth = tableWidth / 4;
			fCommentWidth = fIdWidth;
		} else if (fColumnCount == 2) {
			fPresentationWidth = (int) (tableWidth * 0.7);
			if (isShowId()) {
				fIdWidth = tableWidth - fPresentationWidth;
				fCommentWidth = 0;
			} else {
				fCommentWidth = tableWidth - fPresentationWidth;
				fIdWidth = 0;
			}
		} else {
			fPresentationWidth = tableWidth;
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
		
	public void checkNeedUpdateColumnWidth() {
		if (fNeedUpdate) {
			fNeedUpdate = false;
			fTableComposite.asyncRefreshTree();
		}
	}
	
	@Override
	public void setColumnWidth(int column, int width) {
		
		if (!fTableComposite.isCtrlPressed()) {
			return;
		}
		
		fNeedUpdate = true;
		
		// установить ширину столбца
		if (isShowFirstID()) {
			if (column == 0) {
				fIdWidth = width;
				fPresentationWidth = tableWidth() - fIdWidth - fCommentWidth;
			} else if (column == 1) {
				fPresentationWidth = width;
				fCommentWidth = tableWidth() - fIdWidth - fPresentationWidth;
			}
		} else if (isHideID()) {
			if (column == 0) {
				fPresentationWidth = width;
				fCommentWidth = tableWidth() - fIdWidth - fPresentationWidth;
			}
		} else {
			if (column == 0) {
				fPresentationWidth = width;
				if (isShowComment()) {
					fCommentWidth = tableWidth() - fIdWidth - fPresentationWidth;
				} else {
					fIdWidth = tableWidth() - fPresentationWidth;
				}				
			} else if (column == 1) {
				fCommentWidth = width;
				fIdWidth = tableWidth() - fCommentWidth - fPresentationWidth;
			}
		}
		checkWidth();

		if (fPresentationWidth < 0) {
			throw new RuntimeException();
		}
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

	// ========================================
	// отрисовка

	public int getIndent(IDipTableElement element) {
		if (element instanceof ParentNode) {
			ParentNode node = (ParentNode) element;
			return node.level();
		}

		if (element instanceof TableNode) {
			TableNode node = (TableNode) element;
			return node.level();
		}

		ITableNode node = element.parent();
		if (node != null) {
			return node.level() + 1;
		}
		return 0;
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
	public KTableCellRenderer getCellRenderer(int col, int row) {
		if (row == 0) {
			return fHeaderRender;
		}
		if (col == fIdColumnNumber) {
			return fIdRender;
		} else if (col == fPresentationColumnNumber) {
			return fPresentationRender;
		} else if (col == fCommentColumnNumber) {
			return fCommentCellRender;
		}
		return null;
	}

	// =========================================

	public void setColumnCount(int count) {
		fColumnCount = count;
	}

	private TableElement selected;

	public void setSelected(TableElement element) {
		if (selected != null) {
			selected.setSelection(false);
		}
		selected = element;
		element.setSelection(true);
	}

	// =============================
	// handlers

	public void expandAll() {
		fRootNode.expandAll();
		fTableComposite.updateBackgrouColor();
		fTableComposite.asyncRefreshTree();
		fTableComposite.selector().updateCurrentSelection();
	}

	public void collapseAll() {
		fRootNode.collapseAll();
		fRootNode.setExpand(true);
		fTableComposite.updateBackgrouColor();
		fTableComposite.asyncRefreshTree();
	}
	
	//=================================
	// elements
	
	public void removeFromElements(List<IDipTableElement> tableElement) {
		tableElement.forEach(this::removeFromElements);
	}
	
	
	private void removeFromElements(IDipTableElement tableElement) {
		if (tableElement instanceof TableNode) {
			fElements.removeAll(((TableNode) tableElement).allChildren());
		}
		fElements.remove(tableElement);
	}
	
	public TableNode findNode(IDipParent parent) {
		
		ArrayList<IDipParent> parents = new ArrayList<>();
		IDipParent current = parent;
		if (parent == fRootNode.dipDocElement()) {
			return fRootNode;
		}		
		while (current != null && !current.equals(fRootNode.dipDocElement())) {
			parents.add(0, current);
			current = current.parent();
		}
		
		TableNode currentNode = fRootNode;
		for (IDipParent dipParent: parents) {
			IDipTableElement element = currentNode.find(dipParent);
			if (element instanceof TableNode) {
				currentNode = (TableNode) element;
			} else {
				return null;
			}
		}	
		return currentNode;
	}
	
	private TableNode findInParents(IDipParent parent) {
		if (fParentsElements == null) {
			return null;
		}		
		for (TableNode node: fParentsElements) {
			if (node.dipDocElement() == parent) {
				return node;
			}
		}
		return null;
	}
	
	private Optional<IDipTableElement> findInParentsByName(IDipParent parent) {
		if (fParentsElements == null) {
			return Optional.empty();
		}		
		for (TableNode node: fParentsElements) {
			if (node.dipDocElement().name().equals(parent.name())) {
				return Optional.of(node);
			}
		}
		return Optional.empty();
	}
	
	/**
	 * Находит только при точном совпадении
	 */
	public IDipTableElement findElement(IDipDocumentElement dde) {
		if (dde instanceof IDipParent) {
			TableElement node =  findNode((IDipParent) dde);
			if (node != null) {
				return node;
			}
			return findInParents((IDipParent) dde);
		} 				
		if (dde.parent() == null) {					
			return null;
		}		
		
		if (dde instanceof DipUnit) {
			DipUnit unit = (DipUnit) dde;
			if (unit.getUnitType().isForm()) {
				dde = unit.getUnionExtensions().get(0);
			} else {
				dde = ((DipUnit) dde).getUnitPresentation();
			}
		}
		
		TableNode parentNode = findNode(dde.parent());
		if (parentNode == null) {
			return null;
		}
		return parentNode.find(dde);		
	}
	
	public List<IDipTableElement> findElements(List<IDipDocumentElement> dipDocElements){
		return dipDocElements.stream()
		.map(fTableComposite.tableModel()::findElement)
		.filter(obj-> obj != null)
		.collect(Collectors.toList());
	}
	
	/**
	 * Находит по такому же имени
	 */
	public Optional<IDipTableElement> findElementByName(IDipDocumentElement diptDocElement) {		
		if (diptDocElement instanceof IDipParent) {		
			Optional<TableNode> node = findNodeByName((IDipParent) diptDocElement);
			
			if (node.isPresent()) {
				return Optional.of(node.get());
			}
			return findInParentsByName((IDipParent) diptDocElement);
		} 				
		if (diptDocElement.parent() == null) {					
			return Optional.empty();
		}		
		
		if (diptDocElement instanceof DipUnit) {
			diptDocElement = ((DipUnit) diptDocElement).getUnitPresentation();
		}
				
		Optional<TableNode> node = findNodeByName(diptDocElement.parent());
		if (node.isEmpty()) {
			return Optional.empty();
		}
		return node.get().findByName(diptDocElement);		
	}
	
	
	public List<IDipTableElement> findElementsByName(List<IDipDocumentElement> dipDocElements){
		return dipDocElements.stream()
		.map(fTableComposite.tableModel()::findElementByName)
		.filter(Optional::isPresent)
		.map(Optional::get)
		.collect(Collectors.toList());					
	}
		
	public Optional<TableNode> findNodeByName(IDipParent parent) {
		List<IDipParent> parents = new ArrayList<>();
		IDipParent current = parent;
		if (parent == fRootNode.dipDocElement()) {
			return Optional.of(fRootNode);
		}		
		
		while (current != null && !current.name().equals(fRootNode.dipDocElement().name())) {
			parents.add(0, current);
			current = current.parent();
		}
		
		TableNode currentNode = fRootNode;
		for (IDipParent dipParent: parents) {
			Optional<IDipTableElement> element = currentNode.findByName(dipParent);
			if (element.isPresent() && element.get() instanceof TableNode){
				currentNode = (TableNode) element.get();
			} else {
				return Optional.empty();
			}
		}	
		return Optional.of(currentNode);
	}
	
	public boolean isLastDipDocElement(IDipDocumentElement dipDocElement) {
		if (!fElements.isEmpty()) {
			IDipTableElement element = fElements.get(fElements.size() - 1);
			return dipDocElement == element.dipResourceElement();		
		}
		return false;
	}

	//==========================
	// IDipTableModel
	
	@Override
	public int getRowLocation(int row) {
		int y = 0;
		for (int i = 0; i < row + 1; i++) {
			y+= getFullRowHeight(i);
		}		
		return y;
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
	
	public void clearFirstElement() {
		fFirstCell = -1;
		fIndent = 0;
	}
	
	@Override
	public boolean isCtrlPressed() {
		return tableComposite().isCtrlPressed();
	}
		
	//=========================== 
	// фильтр
	
	public void setFilter(Condition condition) {
		fCondition = condition;
		computeElements(fCondition);
	}
	
	public void resetFilter() {
		fCondition = null;
		computeElements();
	}
	
	public Condition getCondition() {
		return fCondition;
	}
	
	public boolean isFilterMode() {
		return fCondition != null;
	}
		
	// ==============================
	// getters & setters

	public KTableComposite tableComposite() {
		return fTableComposite;
	}
	
	public TableNode getRoot() {
		return fRootNode;
	}
	
	public DipProject dipProject() {
		return fTableComposite.dipProject();
	}

	public PresentationPainter reqPainter() {
		return fPainter;
	}
	
	public IDPainter idPainter() {
		return fIDPainter;
	}
	
	public CommentPainter commentPainter() {
		return fCommentPainter;
	}

	public int tableWidth() {
		return fTable.getBounds().width;
	}
	
	public int tableHeight() {
		return fTable.getBounds().height;
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

	public void setPresentationWidth(int width) {
		fPresentationWidth = width;
		if (fPresentationWidth < 0) {
			throw new RuntimeException();
		}
	}

	public void setIdWidth(int width) {
		fIdWidth = width;
	}

	public void setCommentWidth(int width) {
		fCommentWidth = width;
	}

	public int presentationWidth() {
		int verticalBarSize = fTable.getVerticalBarWidth();
		if (fPresentationColumnNumber == fColumnCount - 1) {
			return fPresentationWidth - verticalBarSize;
		}
		return fPresentationWidth;
	}

	public int idWidth() {
		return fIdWidth;
	}

	public int commentWidth() {
		return fCommentWidth;
	}

	public boolean isShowId() {
		return !isHideID();
	}

	public boolean isShowComment() {
		return !isNotComment();
	}

	public List<IDipTableElement> getElements() {
		return fElements;
	}

	public DipTable getTable() {
		return fTable;
	}

	public TableModel getTableModel() {
		return fRootTableModel;
	}
	
	@Override
	public void setEditRow(int row) {
		fOpenEditingRow = row;		
	}
	
	public void setEditRowHeight(int cellEditorHeight) {
		fCellEditorHeight = cellEditorHeight;
	}
	
	@Override
	public CellInfo getEditedCellInfo() {
		return fEditedCell;
	}
	
	@Override
	public void setEditedCellInfo(Object editedCell) {
		if (editedCell instanceof CellInfo) {		
			fEditedCell = (CellInfo) editedCell;
		}
	}
		
	//==================================
	// NO USED
	
	@Override
	public int getFixedHeaderColumnCount() {
		// количество фиксированных столбцов = 0
		return 0;
	}

	@Override
	public int getFixedHeaderRowCount() {
		// количество фиксированных строк = 1
		return 1;
	}

	@Override
	public int getFixedSelectableColumnCount() {
		// количество фиксированных Selectable стобцов ???
		return 0;
	}

	@Override
	public int getFixedSelectableRowCount() {
		// количество фиксированных Selectable строк ???
		return 0;
	}

	@Override
	public int getRowHeightMinimum() {
		// мнимальная высота строки
		//return 20;
		return 0;
	}

	@Override
	public String getTooltipAt(int arg0, int arg1) {
		// всплывающая подсказка
		return null;
	}
	
	@Override
	public boolean isColumnResizable(int arg0) {
		// можно ли менять ширину
		return true;
	}

	@Override
	public boolean isRowResizable(int arg0) {
		// можно ли менять высоту
		return true;
	}

	@Override
	public void setContentAt(int arg0, int arg1, Object arg2) {		
		// установить содержимое для ячейки, вроде не вызывается ?
	}

	@Override
	public void setRowHeight(int arg0, int arg1) {
		// установить высоту строки
	}

	@Override
	public KTableCellEditor getCellEditor(int arg0, int arg1) {		
		return null;
	}

	@Override
	public void saveUnit(String startContent, String newContent, Object object) {
		if (object instanceof IDipUnit) {
			new EditUnitAction(tableComposite(), startContent, newContent,  (UnitPresentation) object)
			.run();
		}
	}

}
