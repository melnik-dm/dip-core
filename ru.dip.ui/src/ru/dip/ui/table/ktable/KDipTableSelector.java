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

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.IntSummaryStatistics;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import ru.dip.core.model.DipProject;
import ru.dip.core.model.interfaces.IDipDocumentElement;
import ru.dip.core.model.interfaces.IDipElement;
import ru.dip.core.model.interfaces.IDipParent;
import ru.dip.core.model.interfaces.IDipUnit;
import ru.dip.core.model.interfaces.IParent;
import ru.dip.core.model.interfaces.IUnitDescription;
import ru.dip.core.model.interfaces.IUnitExtension;
import ru.dip.core.model.interfaces.IUnitPresentation;
import ru.dip.core.utilities.DipUtilities;
import ru.dip.core.utilities.WorkbenchUtitlities;
import ru.dip.core.utilities.ui.swt.KeyMode;
import ru.dip.ktable.DipTable.RowVisible;
import ru.dip.ui.Messages;
import ru.dip.ui.table.editor.DipTableEditor;
import ru.dip.ui.table.ktable.model.DipTableModel;
import ru.dip.ui.table.ktable.model.HideElements;
import ru.dip.ui.table.ktable.model.IDipTableElement;
import ru.dip.ui.table.ktable.model.ISelector;
import ru.dip.ui.table.ktable.model.ITableNode;
import ru.dip.ui.table.ktable.model.TableElement;
import ru.dip.ui.table.ktable.model.TableNode;
import ru.dip.ui.table.table.TableModel;

public class KDipTableSelector implements ISelector {

	public static final ReqComparator ORDER_COMPARATOR = new ReqComparator();
	
	public static String getIDDialog(String dialogName) {
		Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
		InputDialog dialog = new InputDialog(shell, dialogName, Messages.KDipTableSelector_InputObjectIdMessage, null,
				new IDValidator());
		if (dialog.open() == Dialog.OK) {
			String id = dialog.getValue();
			if (!Path.isValidPosixPath(id)) {
				return null;
			}
			if (id.startsWith(File.separator)) {
				return id.substring(1);
			}
			return id;
		} else {
			return ""; //$NON-NLS-1$
		}
	}

	private static class IDValidator implements IInputValidator {

		@Override
		public String isValid(String newText) {
			if (newText == null || newText.isEmpty()) {
				return ""; //$NON-NLS-1$
			}
			return null;
		}
	}
	
	
	private final KTableComposite fTableComposite;
	// selection
	private List<IDipTableElement> fSelectedTblElements = new ArrayList<>();
	private TreeSet<IDipDocumentElement> fSelectedDipDocElements = new TreeSet<>(new ReqComparator());
	private IDipTableElement fLastSelectedObj;
	private boolean fDoubleClickFolderMode = false;
	
	public KDipTableSelector(KTableComposite tableComposite) {
		fTableComposite = tableComposite;
	}
 
	public void setSelection(IDipTableElement element, KeyMode mode) {
		// elements for update
		Set<IDipTableElement> forUpdateElements = new HashSet<>();		
		removeDeletedElementsFromSelected();
		forUpdateElements.addAll(fSelectedTblElements);
		// add to selection
		addToSelection(element, mode);
		fLastSelectedObj = element;
		// mark selected dipDocElements
		selectDipDocByTableElements();
		// updates
		tableEditor().getButtonManager().applySelection(fSelectedDipDocElements);		
		forUpdateElements.addAll(fSelectedTblElements);
		fTableComposite.updateBackgrouColor();
		fTableComposite.refreshElements(forUpdateElements);
		fTableComposite.tableModel().clearFirstElement();
		fTableComposite.table().updateScroll();
		tableEditor().updateSelection();
	}
	
	private void addToSelection(IDipTableElement element, KeyMode mode) {
		switch (mode) {
		case CTRL_SHIFT:
		case CTRL:{
			addToCtrlSelection(element);
			break;
		}
		case SHIFT:{
			addToShiftSelection(element);
			break;
		}
		case NONE:{
			addToSelection(element);
		}
		}
	}
	
	private void addToShiftSelection(IDipTableElement element) {		
		List<IDipTableElement> elements =  fTableComposite.tableModel().getElements();		
		int elementIndex = elements.indexOf(element);
		
		IntSummaryStatistics statistic = fSelectedTblElements.stream().mapToInt(elements::indexOf).summaryStatistics();
		int selectMin = statistic.getMin();
		int selectMax = statistic.getMax();		
		int lastSelectedIndex = elements.indexOf(fLastSelectedObj);
	
		fSelectedTblElements.clear();

		if (elementIndex < selectMin) {
			if (lastSelectedIndex > selectMin) {
				selectBetween(elementIndex, selectMin);
			} else {
				selectBetween(elementIndex, selectMax);
			}
		} else if (elementIndex > selectMax) {
			if (lastSelectedIndex > selectMin) {
				selectBetween(selectMin, elementIndex);
			} else {
				selectBetween(selectMax, elementIndex);
			}
		} else if (elementIndex == selectMin) {
			if (lastSelectedIndex > elementIndex) {
				addToSelection(element);
			} else {
				selectBetween(selectMin, selectMax);
			}
		} else if (elementIndex == selectMax) {
			if (lastSelectedIndex < elementIndex) {;
				selectBetween(selectMin, selectMax);
			} else {
				addToSelection(element);
				selectBetween(selectMin, selectMax);
			}
		} else {
			if (lastSelectedIndex > selectMin) {
				selectBetween(selectMin, elementIndex);
			} else {
				selectBetween(elementIndex, selectMax);
			}
		}
	}
	
	private void selectBetween(int startIndex, int endIndex) {
		List<IDipTableElement> elements =  fTableComposite.tableModel().getElements();
		startIndex = elements.indexOf(elements.get(startIndex).startElement(HideElements.EXCLUDE));		
		endIndex = elements.indexOf(elements.get(endIndex).endElement(HideElements.EXCLUDE));	
		fSelectedTblElements.addAll(elements.subList(startIndex, endIndex + 1));	
	}
	
	private void addToCtrlSelection(IDipTableElement element) {
		if (element == null) {
			fSelectedTblElements.clear();
			fLastSelectedObj = null;
		} else if (fSelectedTblElements.contains(element)) {
				List<IDipTableElement> linkedElements = element.linkedWithibleElements();
				fSelectedTblElements.removeAll(linkedElements);
				if (linkedElements.contains(fLastSelectedObj)) {
					fLastSelectedObj = null;
				}
		} else {
			fSelectedTblElements.addAll(element.linkedWithibleElements());
		}
	}
	
	private void addToSelection(IDipTableElement element) {
		fSelectedTblElements.clear();
		fSelectedTblElements.addAll(element.linkedWithibleElements());		
	}
	
	private void removeDeletedElementsFromSelected() {
		fSelectedTblElements.removeIf(e -> e.dipResourceElement().resource() == null || !e.dipResourceElement().resource().exists());	
	}

	public void updateGroupSelection() {
		tableEditor().getButtonManager().applySelection(fSelectedDipDocElements);
		fTableComposite.updateBackgrouColor();
	}

	@Override
	public void setTopItemElement(Object tableElement) {
		int index = tableModel().getElements().indexOf(tableElement);
		// как отображается на экране (полностью, частично, не отображается)
		RowVisible cellVisible = (fTableComposite.table().isShowRow(index));
		if (cellVisible == RowVisible.FULL) {
			return;
		}
		int y = tableModel().getRowLocation(index);
		fTableComposite.table().scrollToY(y, index);
	}

	private void selectDipDocByTableElements() {
		fSelectedDipDocElements.clear();
		for (Object obj : fSelectedTblElements) {
			IDipDocumentElement req = getDipDocElementFromTreeObject(obj);
			if (req != null) {
				fSelectedDipDocElements.add(req);
			}
		}
	}
	
	public void deselect() {
		Set<IDipTableElement> forUpdateElements = new HashSet<>();		
		removeDeletedElementsFromSelected();
		forUpdateElements.addAll(fSelectedTblElements);
		
		fSelectedDipDocElements.clear();
		fLastSelectedObj = null;
		fSelectedTblElements.clear();
		
		tableEditor().getButtonManager().applySelection(fSelectedDipDocElements);	
		fTableComposite.updateBackgrouColor();
		fTableComposite.refreshElements(forUpdateElements);
		fTableComposite.tableModel().clearFirstElement();
		fTableComposite.table().updateScroll();
		tableEditor().updateSelection();
	}
	
	public void updateCurrentSelection() {
		tableEditor().getButtonManager().applySelection(fSelectedDipDocElements);
		fTableComposite.updateBackgrouColor();
		fTableComposite.refreshElements(fSelectedTblElements);
		fTableComposite.tableModel().clearFirstElement();
		fTableComposite.table().updateScroll();
		tableEditor().updateSelection();
	}


	private static class ReqComparator implements Comparator<IDipDocumentElement> {

		@Override
		public int compare(IDipDocumentElement dipDoc1, IDipDocumentElement dipDoc2) {
			if (dipDoc1 == null && dipDoc2 == null) {
				return 0;
			}
			if (dipDoc2 == null) {
				return -1;
			}
			if (dipDoc1 == null) {
				return 1;
			}
			if (dipDoc1 == dipDoc2 || dipDoc1.equals(dipDoc2)) {
				return 0;
			}
			ArrayList<Integer> indexes1 = getIndexes(dipDoc1);
			ArrayList<Integer> indexes2 = getIndexes(dipDoc2);
			int result = compare(indexes1, indexes2);
			return result;

		}

		private ArrayList<Integer> getIndexes(IDipDocumentElement dipDocElement) {
			ArrayList<Integer> result = new ArrayList<>();
			IDipDocumentElement currentDicDocElement = dipDocElement;
			while (true) {
				IDipParent parent = currentDicDocElement.parent();
				if (parent == null) {
					break;
				}
				int number = parent.getDipDocChildrenList().indexOf(currentDicDocElement);
				result.add(0, number);
				if (parent instanceof DipProject) {
					break;
				}
				currentDicDocElement = parent;
			}
			return result;
		}

		private int compare(ArrayList<Integer> list1, ArrayList<Integer> list2) {
			if (list1 == null && list2 == null) {
				return 0;
			}
			if (list2 == null) {
				return -1;
			}
			if (list1 == null) {
				return 1;
			}
			int index = 0;
			while (true) {
				if (index >= list1.size()) {
					return -1;
				}
				if (index >= list2.size()) {
					return 1;
				}
				int number1 = list1.get(index);
				int number2 = list2.get(index);
				if (number1 != number2) {
					return number1 - number2;
				}
				index++;
			}
		}
	}

	private IDipDocumentElement getDipDocElementFromTreeObject(Object object) {
		if (object instanceof TableElement) {
			object = ((TableElement) object).dipDocElement();
		}

		if (object instanceof IUnitExtension) {
			return ((IUnitExtension) object).getDipUnit();
		}
		if (object instanceof IDipDocumentElement) {
			return (IDipDocumentElement) object;
		}
		return null;
	}

	// ===============================
	// isSelect

	@Override
	public boolean isSelect(Object element) {
		return fSelectedTblElements.contains(element);
	}

	public boolean isSelectDipDocElement(IDipDocumentElement dipDocElement) {
		if (dipDocElement == null) {
			return false;
		}
		return fSelectedDipDocElements.contains(dipDocElement);
	}

	public boolean isReqDescription() {
		if (fLastSelectedObj != null) {
			return fLastSelectedObj.dipDocElement() instanceof IUnitDescription;
		}
		return false;
	}

	@Override
	public boolean hasParentInSelectElements(IDipUnit unit) {
		for (IDipDocumentElement dipDocElement : fSelectedDipDocElements) {
			if (dipDocElement instanceof IDipParent) {
				if (unit.hasParent((IParent) dipDocElement)) {
					return true;
				}
			}
		}
		return false;
	}

	// ================================
	// set selection

	public void setTableElementSelection(IDipTableElement element) {
		ITableNode parentNode = element.parent();
		if (parentNode != null && !parentNode.expand()) {
			expandParent(parentNode);
		}
		
		if (element != null) {
			setSelection(element, KeyMode.NONE);
			setTopItemElement(element);
		}
	}

	private void expandParent(ITableNode node) {
		if (node.expand()) {
			return;
		}
		ITableNode parentNode = node.parent();
		if (parentNode != null) {
			expandParent(parentNode);
		}
		fTableComposite.setNodeExpand(node, true);
		fTableComposite.refreshNode(node);
	}

	public void setSelection(Object object) {
		Object selObject = getObjectForSelect(object);
		if (selObject instanceof IDipDocumentElement) {
			Optional<IDipTableElement> element = fTableComposite.tableModel().findElementByName((IDipDocumentElement) selObject);
			if (element.isPresent()) {
				setTableElementSelection(element.get());
			}
		} else if (selObject instanceof IDipTableElement) {
			setTableElementSelection((IDipTableElement) selObject);
		}
	}

	private Object getObjectForSelect(Object object) {
		if (object != null && object instanceof IDipUnit) {
			IDipUnit unit = (IDipUnit) object;
			IUnitPresentation presentation = unit.getUnitPresentation();
			return presentation;
		}
		return object;
	}

	// ================================
	// ctrl selection

	public void doCtrlSelection(TableElement dipDocElement) {
		if (dipDocElement instanceof TableNode) {
			doCtrlFolderSelection((TableNode) dipDocElement);
		} else {
			doCtrlFileSelection(dipDocElement);
		}
	}

	private void doCtrlFileSelection(IDipTableElement unitExtentation) {
		setManyTableElements(unitExtentation.parent().visibleUnitElements());
	}

	public void selectManyDipDocElementss(List<IDipDocumentElement> dipDocElements) {
		List<IDipTableElement> tableElements = fTableComposite.tableModel().findElementsByName(dipDocElements); 		
		setManyTableElements(tableElements);		
	}
	

	
	public void setManyTableElements(List<IDipTableElement> tableElements) {
		
		// если не учтены TableField элементы (т.е. не все поля для форм)
		List<IDipTableElement> notIncludedFieldsElements = tableElements.stream()
			.filter(IDipTableElement::isAbstractField)
			.flatMap(te -> te.linkedWithibleElements().stream().filter(IDipTableElement::isAbstractField))
			.distinct()
			.filter(te -> !tableElements.contains(te))
			.collect(Collectors.toList());
		
		if (!notIncludedFieldsElements.isEmpty()) {
			tableElements.addAll(notIncludedFieldsElements);
		}
		
		List<IDipTableElement> forUpdateElements = new ArrayList<>();
		fSelectedTblElements.removeIf(el -> !el.isExists()); // если старые выделенные элементы были удалены
		forUpdateElements.addAll(fSelectedTblElements);

		fLastSelectedObj = null;
		fSelectedTblElements.clear();
		fSelectedDipDocElements.clear();

		for (IDipTableElement element : tableElements) {
			fSelectedTblElements.add(element);
			fLastSelectedObj = element;
			
			ITableNode parentNode = element.parent();
			if (parentNode != null && !parentNode.expand()) {
				expandParent(parentNode);
			}
		}
		selectDipDocByTableElements();

		tableEditor().getButtonManager().applySelection(fSelectedDipDocElements);
		fTableComposite.updateBackgrouColor();
		forUpdateElements.addAll(fSelectedTblElements);
		fTableComposite.refreshElements(forUpdateElements);
		tableEditor().updateSelection();
	}

	private void doCtrlFolderSelection(TableNode node) {
		fDoubleClickFolderMode = true;
		fLastSelectedObj = node;

		fSelectedTblElements.clear();
		fSelectedTblElements.add(fLastSelectedObj);

		selectDipDocByTableElements();
		if (!node.expand()) {
			fTableComposite.setNodeExpand(node, true);
		}

		fTableComposite.updateBackgrouColor();
	}

	// =======================
	// get selection

	public IDipTableElement getLastSelectObject() {
		return fLastSelectedObj;
	}
		
	/**
	 * Возвращает выделенный IDipDocElelent 
	 * IDipExtension (не DipUnit)
	 */
	public IDipDocumentElement getLastSelectDipDocElement() {
		if (fLastSelectedObj != null) {
			return fLastSelectedObj.dipDocElement();
		}
		return null;
	}


	public ITableNode getLastSelectNode() {
		if (fLastSelectedObj == null) {
			return null;
		}
		if (fLastSelectedObj instanceof TableNode) {
			return (TableNode) fLastSelectedObj;
		}
		return fLastSelectedObj.parent();
	}

	public List<IDipTableElement> getSelectedObjects() {
		return fSelectedTblElements;
	}

	public TreeSet<IDipDocumentElement> getSelectedElements() {
		return fSelectedDipDocElements;
	}

	public IDipDocumentElement[] getArraySelectedElements() {
		IDipDocumentElement[] elements = new IDipDocumentElement[fSelectedDipDocElements.size()];
		getSelectedElements().toArray(elements);
		return elements;
	}

	public String[] getSelectedNames() {
		return fSelectedDipDocElements.stream().map(IDipDocumentElement::name).toArray(String[]::new);
	}

	public IDipDocumentElement getSelectedOneDipDocElement() {
		if (fSelectedDipDocElements != null && fSelectedDipDocElements.size() == 1) {
			return fSelectedDipDocElements.first();
		}
		return null;
	}

	public IDipParent selectedTableContainer() {
		IDipDocumentElement element = getSelectedOneDipDocElement();
		if (element instanceof IDipParent) {
			return (IDipParent) element;
		} else {
			IParent parent = element.parent();
			if (parent instanceof IDipParent) {
				return (IDipParent) parent;
			}
		}
		return null;
	}

	public IDipDocumentElement first() {
		return fSelectedDipDocElements.first();
	}

	public boolean isOneSelected() {
		return fSelectedDipDocElements != null && fSelectedDipDocElements.size() == 1;
	}

	public boolean isEmpty() {
		return fSelectedDipDocElements == null || fSelectedDipDocElements.isEmpty();
	}

	public IDipParent getSelectionParent() {
		IDipDocumentElement element = getSelectedOneDipDocElement();
		if (element instanceof IDipParent) {
			return (IDipParent) element;
		} else if (element instanceof IDipElement) {
			return element.parent();
		} else {
			return model();
		}
	}

	public ITableNode getSelectedNode() {
		if (fLastSelectedObj == null) {
			return null;
		}
		if (fLastSelectedObj instanceof TableNode) {
			return (TableNode) fLastSelectedObj;
		}
		return fLastSelectedObj.parent();
	}
	
	public boolean hasReadOnlyObjects() {
		for (IDipDocumentElement dipDocElement: fSelectedDipDocElements) {
			if (dipDocElement.isReadOnly()) {
				return true;
			}
		}
		return false;
	}

	// =============================
	// open / show by id

	public void openByID() {
		String id = getIDDialog(Messages.KDipTableSelector_OpenByIdTitle);
		if (id == null) {
			Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
			MessageDialog.openError(shell, Messages.KDipTableSelector_OpenErrorTitle, Messages.KDipTableSelector_InvalidIdMessage);
		}
		if (id.isEmpty()) {
			return;
		}
		IDipElement element = findElementByID(id);
		if (element == null) {
			Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
			MessageDialog.openError(shell, Messages.KDipTableSelector_OpenErrorTitle2, Messages.KDipTableSelector_NotFoundObjMessage);
			return;
		}

		if (element instanceof IDipUnit && element.isReadOnly()) {
			WorkbenchUtitlities.openReadOnlyErrorMessage((IDipDocumentElement) element);
			return;
		}

		IResource res = element.resource();
		if (res instanceof IFile) {
			if (res.exists()) {
				IFile file = (IFile) res;
				WorkbenchUtitlities.openFile(file);
			} else {
				Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
				MessageDialog.openError(shell, Messages.KDipTableSelector_OpenErrorTitle3, Messages.KDipTableSelector_NotFoundObjMessage2);
			}
		} else {
			Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
			MessageDialog.openError(shell, Messages.KDipTableSelector_OpenErrorTitle3, Messages.KDipTableSelector_IsNotFileMessage);
		}
	}

	public void showByID() {
		String id = getIDDialog(Messages.KDipTableSelector_ShowByIdShellTitle);
		if (id == null) {
			Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
			MessageDialog.openError(shell, Messages.KDipTableSelector_ShowErrorTitle, Messages.KDipTableSelector_InvalidIdMessage);
		}
		if (id.isEmpty()) {
			return;
		}
		IDipElement element = findElementByID(id);
		if (element != null) {
			setSelection(element);
		} else {
			Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
			MessageDialog.openError(shell, Messages.KDipTableSelector_ShowErrorTitle2, Messages.KDipTableSelector_NotFoundObjMessage);
		}
	}

	private IDipElement findElementByID(String id) {
		IDipElement element = DipUtilities.findElement(id);
		if (element == null) {
			DipProject project = fTableComposite.model().dipProject();
			element = DipUtilities.findElement(project, id);
		}
		if (element != null) {
			boolean hasParent = element.hasParent(fTableComposite.model().getContainer());
			if (hasParent) {
				return element;
			}
		}
		return null;
	}

	// ========================
	// getters

	private DipTableEditor tableEditor() {
		return fTableComposite.editor();
	}

	private TableModel model() {
		return fTableComposite.model();
	}

	private DipTableModel tableModel() {
		return fTableComposite.tableModel();
	}

	public boolean isDoubleClickFolderMode() {
		return fDoubleClickFolderMode;
	}

	public void setDoubleClockFolderMode(boolean value) {
		fDoubleClickFolderMode = value;
	}

}
