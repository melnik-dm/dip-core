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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;

import ru.dip.core.form.model.FormFieldType;
import ru.dip.core.model.DipFolder;
import ru.dip.core.model.DipUnit;
import ru.dip.core.model.interfaces.IDipDocumentElement;
import ru.dip.core.model.interfaces.IDipParent;
import ru.dip.core.model.interfaces.IDipUnit;
import ru.dip.core.report.checker.DipDocElementConditionChecker;
import ru.dip.core.report.model.condition.Condition;
import ru.dip.core.unit.UnitDescriptionPresentation;
import ru.dip.core.unit.UnitExtension;
import ru.dip.core.unit.form.AbstractFormField;
import ru.dip.core.unit.form.FieldUnity;
import ru.dip.core.unit.form.FormField;
import ru.dip.core.unit.form.IFormSettings;
import ru.dip.core.utilities.DipTableUtilities;
import ru.dip.ui.table.table.TableModel;
import ru.dip.ui.table.table.TableSettings;

public class TableNode extends TableElement implements ITableNode {

	private DipTableModel fModel;
	private List<IDipTableElement> fChildren = new ArrayList<>();
	private boolean fExpand;
	private int fLevel; // уровень вложенности
	private boolean fRoot;		
	private Rectangle fExpandImageLoc;
			
	public static TableNode rootNode(DipTableModel model, TableModel dipParent) {		
		TableNode rootNode = new TableNode(dipParent.getContainer());
		rootNode.fModel = model;
		rootNode.fExpand = false;
		rootNode.setRoot();
		rootNode.computeChildren();
		return rootNode;
	}
	
	private TableNode(IDipParent dipParent) {
		super(dipParent, null);
	}
	
	public TableNode(DipTableModel model, IDipParent dipParent, TableNode parent) {
		super(dipParent, parent);
		fModel = model;
		fExpand = false;		
		if (parent != null) {
			fLevel = parent.level() + 1;
		} 		
		computeChildren();
	}

	/**
	 * Создает список детей
	 */
	public void computeChildren() {
		fChildren = new ArrayList<>();
		IDipParent dipDocElement = dipDocElement();
		Object[] reqChildren = dipDocElement.getDipChildren();
		for (Object reqChild: reqChildren) {
			if (reqChild instanceof IDipParent) {
				TableNode node = new TableNode(fModel, (IDipParent) reqChild, this);
				if (fModel.tableComposite().isOneListMode()) {
					node.setExpand(true);
				}
				fChildren.add(node);
			} else if (reqChild instanceof IDipDocumentElement) {
				TableElement unitElement = new TableElement((IDipDocumentElement) reqChild, this);
				fChildren.add(unitElement);
			}
		}
		applyFormSettings();
	}
	
	/**
	 * Отфильтровывает поля в соответсвии с настройками
	 * Устанавливает номера полей
	 */
	private void applyFormSettings() {
		IFormSettings reqSettings = fModel.tableComposite().getFormSettings();
		removeNotVisibleFormField(reqSettings);
		if (TableSettings.isWrapFields()) {
			joinFields();
		}
		setNumbers();
	}
	
	private void removeNotVisibleFormField(IFormSettings reqSettings) {
		fChildren.removeIf(e -> e.isAbstractField() 
				&& !((AbstractFormField) e.dipDocElement()).isVisible(reqSettings));
	}
		

	private void joinFields() {
		List<IDipTableElement> joinChildren = new ArrayList<>();	
		IDipTableElement currentElement = null;
		List<FormField> currentFields = new ArrayList<>();
		IDipUnit currentUnit = null;
		
		for (IDipTableElement element: fChildren) {
			if (isNoTextFormField(element) && isNotDifferentUnit(currentUnit, (FormField) element.dipDocElement())){
				FormField field = (FormField) element.dipDocElement();
				currentUnit = field.getDipUnit();
				if (currentElement == null) {
					currentElement = element;
				}
				currentFields.add(field);				
			} else {
			
				if (currentFields.size() == 1) {
					joinChildren.add(currentElement);
				} else if (!currentFields.isEmpty()) {
					FieldUnity fieldUnity = new FieldUnity(currentUnit, currentFields);
					
				//	вставляем лямбду для создания  new TableElement
				//	BiFunction<FieldUnity, TableNode>
					
					joinChildren.add(new TableElement(fieldUnity, ((TableNode)currentElement).parent()));					
				} 			
				currentElement = null;
				currentFields = new ArrayList<>();
				currentUnit = null;				
				joinChildren.add(element);					
			}
		}
		fChildren = joinChildren;
	}
	
	private boolean isNoTextFormField(IDipTableElement element) {
		return element.isFormField() 
				&& ((FormField) element.dipDocElement()).getField().getType() != FormFieldType.TEXT;
	}
	
	private boolean isNotDifferentUnit(IDipUnit current, FormField field) {
		if (current == null) {
			return true;
		}
		return current == field.getDipUnit();
	}
	
	private void setNumbers() {
		int currentNumber = 0;
		IDipUnit currentUnit = null;
		List<IDipTableElement> currentElements = new ArrayList<>();
		for (int i = 0; i < fChildren.size(); i ++) {
			IDipTableElement element = fChildren.get(i);
			if (element.isAbstractField()) {
				AbstractFormField field = ((AbstractFormField) element.dipDocElement());		
				if (currentUnit == field.getDipUnit()) {
					element.setNumber(currentNumber++);
					currentElements.add(element);
					element.setLinkedElements(currentElements);
				} else {
					currentNumber = 0;
					element.setNumber(currentNumber++);
					element.setLinkedElements(currentElements);
					currentElements.add(element);
					currentUnit = field.getDipUnit();
				}
			} else if (currentNumber != 0) {
				currentNumber = 0;
				currentUnit = null;
				currentElements = new ArrayList<>();
			}
		}
	}

	/*
	 * Нужно переделать вместе с allChildren(conditions) 
	 */
	public List<IDipTableElement> allChildren(){
		// если включе фильтр
		Condition condition = fModel.getCondition();
		if (condition != null) {
			return allChildren(condition);
		}		
		List<IDipTableElement> children = new ArrayList<>();
		for (IDipTableElement element: fChildren) {
			// filters
			if (!filter(element)){
				continue;
			}
			// add element
			children.add(element);
			// add elemetn`s children
			if (element instanceof TableNode && ((TableNode)element).expand()) {				
				List<IDipTableElement> allChildren = ((TableNode) element).allChildren();
				children.addAll(allChildren);
			}
		}
		return children;
	}
	
	private boolean filter(IDipTableElement element) {
		if (element.isEmptyDescription()) {
			return false;
		}
		if (fModel.tableComposite().isHideDisableObjs()  && element.isDisable()) {
			return false;
		}
		return true;
	}
	
	
	/**
	 * Все дети, с учетом фильтра
	 */
	public List<IDipTableElement> allChildren(Condition condition){
		List<IDipTableElement> children = new ArrayList<>();
		for (IDipTableElement element: fChildren) {					
			if (element.isEmptyDescription()) {
				continue;
			}
			if (fModel.tableComposite().isHideDisableObjs()  && element.isDisable()) {
				continue;
			}	
			
			IDipDocumentElement dipDocElement = element.dipDocElement();
			if (dipDocElement instanceof UnitExtension) {
				dipDocElement = ((UnitExtension)dipDocElement).getDipUnit();
			}	
			boolean filter = DipDocElementConditionChecker.checkDipDocElement(dipDocElement , condition);
			if (!filter) {
				continue;
			}
			if (element instanceof TableNode) {
				TableNode node = (TableNode) element;
				List<IDipTableElement> nodeChildren = node.allChildren(condition);
				if (nodeChildren.isEmpty()) {
					continue;
				}
				children.add(element);
				if (node.expand()) {
					children.addAll(nodeChildren);
				}				
			} else {
				children.add(element);			
			}
		}
		return children;
	}
	
	public List<IDipTableElement> allDiffChildren(ITableNode node) {
		List<IDipTableElement> children = new ArrayList<>();
		for (IDipTableElement element: fChildren) {
			if (element.isEmptyDescription()) {
				continue;
			}
			if (fModel.tableComposite().isHideDisableObjs()  && element.isDisable()) {
				continue;
			}
			if (element instanceof TableNode) {
				TableNode childNode = (TableNode) element;			
				List<IDipTableElement> nodeChildren = childNode.allDiffChildren(node);
				if (nodeChildren.isEmpty()) {
					if (childNode.isDiff()) {
						children.add(element);
					}
					continue;
				} 
				children.add(element);
				if (childNode.expand()) {
					children.addAll(nodeChildren);
				}
			} else if (element.isDiff()){
				children.add(element);			
			}
		}
		return children;
	}
	
	public List<IDipTableElement> visibleUnitElements(){
		return fChildren.parallelStream()
				.filter(IDipTableElement::isHacContentElement).collect(Collectors.toList());
	}
	
	//=========================
	// add tableElement (unit)
	
	public IDipTableElement addNewUnit(DipUnit unit, IDipTableElement select, boolean before) {
		int index = computeNewUnitIndex(select, before);		
		return addNewUnit(index, unit);
	}
	
	public IDipTableElement addNewUnitToEnd(DipUnit unit) {
		int index = getLastUnitIndex() + 1;
		return addNewUnit(index, unit);
	}
	
	
	public IDipTableElement addNewUnitToStart(DipUnit unit) {
		return addNewUnit(0, unit);
	}
	
	public TableElement addNewUnit(int index, DipUnit unit) {
		TableElement presentation = new TableElement(unit.getUnitPresentation(), this);
		fChildren.add(index, presentation);
		TableElement description = new TableElement(unit.getUnitDescription(), this);
		fChildren.add(index + 1, description);
		return presentation;
	}
	
	private int computeNewUnitIndex(IDipTableElement select, boolean before) {
		if (select == null) {
			return 0;
		}		
		int selectIndex = fChildren.indexOf(select);
		if (selectIndex < 0) {
			return 0;
		}
		
		boolean isDescription = select.dipDocElement() instanceof UnitDescriptionPresentation;
		
		if (before) {
			if (isDescription) {
				return selectIndex - 1;
			} else {
				return selectIndex;
			}
		} else {
			selectIndex++;
			if (isDescription) {
				return selectIndex;
			} else {				
				if (fChildren.size() == selectIndex) {
					return selectIndex;
				}
				IDipTableElement nextElement = fChildren.get(selectIndex);
				if (nextElement.dipDocElement() instanceof UnitDescriptionPresentation) {
					return selectIndex + 1;
				}
				return selectIndex;
			}
		}		
	}
	
	//==============================
	// add TableNode
	
	
	public ITableNode addNeightborFolder(IDipParent dipParent, boolean before) {
		return parent().addNewNeightborFolder(dipParent, this, before);
	}
	
	
	public ITableNode addNewNeightborFolder(IDipParent dipParent, ITableNode select, boolean before) {
		int index = fChildren.indexOf(select);
		if (!before) {
			index++;
		}
		return addNewFolder(index, dipParent);
	}
	
	public ITableNode addNewFolder(int index, IDipParent dipParent) {
		TableNode node = new TableNode(fModel, dipParent, this);
		fChildren.add(index, node);
		return node;
	}
	
	public TableNode addNewFolderToBegin(IDipParent dipParent) {
		TableNode node = new TableNode(fModel, dipParent, this);
		int index = getFirstParentIndex();
		fChildren.add(index, node);
		return node;
	}
	
	public TableNode addNewFodlerToEnd(IDipParent dipParent) {
		TableNode node = new TableNode(fModel, dipParent, this);
		fChildren.add(node);
		return node;
	}
	
	@Override
	public void delete(IDipTableElement tableElement) {
		List<IDipTableElement> allLinkedElements = linkedElements(tableElement, HideElements.INCLUDE);
		fChildren.removeAll(allLinkedElements);
		fModel.removeFromElements(allLinkedElements);
	}
	
	//=======================================
	// up - down
	
	@Override
	public void up(IDipTableElement element) {	
		IDipTableElement currentStartElement = startElement(element, HideElements.INCLUDE);
		int previousStartIndex = previousStartElementIndex(currentStartElement, HideElements.INCLUDE);
		if (element instanceof TableNode) {
			fChildren.remove(element);
			fChildren.add(previousStartIndex, element);
		} else {
			List<IDipTableElement> elementsForMove =  linkedElements(currentStartElement, HideElements.INCLUDE);	
			fChildren.removeAll(elementsForMove);
			fChildren.addAll(previousStartIndex, elementsForMove);
		}		
	}
	
	@Override
	public void up(List<IDipTableElement> elements) {
		Collections.sort(elements, new Comparator<IDipTableElement>() {
			public int compare(IDipTableElement o1, IDipTableElement o2) {
				return fChildren.indexOf(o1) - fChildren.indexOf(o2);			
			}
		});
		
		IDipTableElement firstElement = elements.get(0);
		firstElement = startElement(firstElement, HideElements.INCLUDE);	
			
		// end и start могут совпадать
		IDipTableElement previousEndElement = previousAtomicElement(firstElement, HideElements.INCLUDE);
		List<IDipTableElement> previousElements = previousEndElement.allLinkedElements();
		
		IDipTableElement lastElement = elements.get(elements.size() - 1);
		lastElement = endElement(lastElement, HideElements.INCLUDE);
		int nextIndex = fChildren.indexOf(lastElement) + 1;	
		
		for (IDipTableElement element: previousElements) {
			fChildren.remove(element);
			fChildren.add(nextIndex-1, element);
		}
	}
	
	@Override
	public void down(IDipTableElement element) {
		IDipTableElement endElement = endElement(element, HideElements.INCLUDE);
		IDipTableElement next = nextAtomicElement(endElement, HideElements.INCLUDE);
		if (next != null) {
			up(next);
		}
	}
	
	@Override
	public void down(List<IDipTableElement> elements) {
		Collections.sort(elements, new Comparator<IDipTableElement>() {
			public int compare(IDipTableElement o1, IDipTableElement o2) {
				return fChildren.indexOf(o1) - fChildren.indexOf(o2);			
			}
		});
		
		IDipTableElement firstElement = elements.get(0);
		firstElement = startElement(firstElement, HideElements.INCLUDE);	
		
		IDipTableElement lastElement = elements.get(elements.size() - 1);
		lastElement = endElement(lastElement, HideElements.INCLUDE);
			
		// end и start могут совпадать
		IDipTableElement nextStartElement = nextAtomicElement(lastElement, HideElements.INCLUDE);
		List<IDipTableElement> nextAllElement = nextStartElement.allLinkedElements();
	
		int startIndex = fChildren.indexOf(firstElement);
		
		for (IDipTableElement element: nextAllElement) {
			fChildren.remove(element);
			fChildren.add(startIndex++, element);
		}
	}

	//=============================
	// navigation 
	
	/**
	 * @param element - начальный элемент
	 * @param hideElemetns - включать ли неотображаемые элементы
	 * @return список всех связанных элементов для соответствующего файла или папки
	 */
	public List<IDipTableElement> linkedElements(IDipTableElement element, HideElements hideElements){
		List<IDipTableElement> result = new ArrayList<>();
		IDipTableElement startElement = element.startElement(hideElements);
		result.add(startElement);
		IDipDocumentElement current = startElement.dipResourceElement();
		IDipTableElement next = nextAtomicElement(startElement, hideElements);
		while (next != null && next.dipResourceElement() == current) {
			result.add(next);
			next = nextAtomicElement(next, hideElements);
		}					
		return result;
	}
	
	/*
	 *  Предыдущий атомарный элемент, может быть Description, UnitPresentation, IDipParent
	 */
	private IDipTableElement previousAtomicElement(IDipTableElement element, HideElements hideElements) {
		int index = fChildren.indexOf(element);		
		if (index <= 0) {
			return null;
		}
		IDipTableElement previous =  fChildren.get(index - 1);
		if (hideElements == HideElements.EXCLUDE &&!filter(previous)) {
			return previousAtomicElement(previous, hideElements);
		} 
		return previous;
	}
	
	/*
	 *  Следующий атомарный элемент, может быть Description, UnitPresentation, IDipParent
	 */
	private IDipTableElement nextAtomicElement(IDipTableElement element, HideElements hideElements) {
		int index = fChildren.indexOf(element);
		if (fChildren.size() <= index +1) {
			return null;
		}
		IDipTableElement next =  fChildren.get(index + 1);
		if (hideElements == HideElements.EXCLUDE && !filter(next)) {
			return nextAtomicElement(next, hideElements);
		} 
		return next;
	}
	
	/*
	 * Первый индекс для папки 
	 */
	private int getFirstParentIndex(){
		for (int i = 0; i < fChildren.size(); i++){
			IDipTableElement element = fChildren.get(i);
			if (element instanceof TableNode){
				return i;
			}
		}
		return fChildren.size();
	}
	
	/*
	 * Последний индекс для файла 
	 */
	public int getLastUnitIndex() {
		int result = -1;
		for (int i = 0; i < fChildren.size(); i++){
			IDipTableElement element = fChildren.get(i);
			if (element instanceof TableNode){
				return result;
			} else {
				result++;
			}
		}
		return result;
	}

	/*
	 * Индекс предыдущего элемента, для файлов определяет индекс для UnitPresentation 
	 */
	private int previousStartElementIndex(IDipTableElement startElement,  HideElements hideElements) {
		IDipTableElement startPreviousElement = previousStartElement(startElement, hideElements);
		if (startPreviousElement == null) {
			return -1;
		}		
		return fChildren.indexOf(startPreviousElement);				
	}
	
	/*
	 * Предыдущий элемент (если файл, то UnitPresentation)
	 */
	private IDipTableElement previousStartElement(IDipTableElement startElement, HideElements hideElements) {
		IDipTableElement previousElement = previousAtomicElement(startElement, HideElements.EXCLUDE);
		if (previousElement == null) {
			return null;
		}
		IDipTableElement startPreviousElement = startElement(previousElement, hideElements);
		return startPreviousElement;
	}
	
	public IDipTableElement startElement(IDipTableElement element, HideElements hideElements) {				
		IDipDocumentElement currentUnit = element.dipResourceElement();
		IDipTableElement currentElement = element;
		IDipTableElement previous = previousAtomicElement(element, hideElements);
		while (previous != null && currentUnit == previous.dipResourceElement()) {
			currentElement = previous;
			previous = previousAtomicElement(previous, hideElements);
		}
		return currentElement;
	}
	
	public IDipTableElement endElement(IDipTableElement element, HideElements hideElements) {
		IDipDocumentElement currentUnit = element.dipResourceElement();
		IDipTableElement currentElement = element;
		IDipTableElement next = nextAtomicElement(element, hideElements);
		while (next != null && currentUnit == next.dipResourceElement()) {
			currentElement = next;
			next = nextAtomicElement(next, hideElements);
		}
		return currentElement;
	}
	
	public int indexByReqIndex(IDipDocumentElement req, int index) {
		if (index == 0) {
			return 0;
		}
		IDipDocumentElement previousReq = DipTableUtilities.getPreviousElement(req);	
		if (previousReq == null) {
			return -1;
		}
		IDipTableElement previousElement = find(req.name());
		if (previousElement == null) {
			return -1;
		}
		return fChildren.indexOf(endElement(previousElement, HideElements.EXCLUDE)) + 1;
	}
	
	//===============================
	// find
	
	public IDipTableElement find(String name) {
		for (IDipTableElement element: fChildren) {
			if ((element.dipDocElement()).name().equals(name)) {
				return startElement(element, HideElements.EXCLUDE);
			}
		}
		return null;
	}
	
	public IDipTableElement find(IDipDocumentElement dipDocElement) {
		for (IDipTableElement element: fChildren) {
			if (element.dipDocElement() == dipDocElement) {
				return startElement(element, HideElements.EXCLUDE);
			}
		}
		return null;
	}
	
	public Optional<IDipTableElement> findByName(IDipDocumentElement dipDocElement) {
		if (dipDocElement instanceof FormField) {
			return findFieldByName((FormField) dipDocElement);
		}
		if (dipDocElement instanceof UnitDescriptionPresentation) {
			return findDescriptionByName((UnitDescriptionPresentation) dipDocElement);
		}
		
		String name = dipDocElement.name();
		for (IDipTableElement element: fChildren) {
			if (element.dipDocElement().name().equals(name)) {
				return Optional.of(element);
			}
		}
		return Optional.empty();
	}
	
	private Optional<IDipTableElement> findFieldByName(FormField field){
		String name = field.name();
		for (IDipTableElement element: fChildren) {
			if (!element.dipDocElement().name().equals(name)) {
				continue;
			}
			if (element.isFormField()) {
				if (((FormField)element.dipDocElement()).getField().getName().equals(field.getField().getName())) {
					return Optional.of(element);
				}
			} else if (element.isFormUnityField()) {
				for (FormField formField: ((FieldUnity) element.dipDocElement()).getFormFields()) {
					if (formField.getField().getName().equals(field.getField().getName())) {
						return Optional.of(element);
					}
				}
			}
		}
		return Optional.empty();
	}
	
	private Optional<IDipTableElement> findDescriptionByName(UnitDescriptionPresentation description){
		String name = description.name();
		for (IDipTableElement element: fChildren) {
			if (!element.dipDocElement().name().equals(name)) {
				continue;
			}
			if (element.isDescription()) {
				return Optional.of(element);
			}	
		}
		return Optional.empty();
	}
	
	//===============================
	// folding
	
	/**
	 * Содержит ли иконка узла (+-) указанную точку
	 */
	public boolean expandImageContains(Point p) {
		return fExpandImageLoc != null && fExpandImageLoc.contains(p);
	}
	
	void expandAll() {
		fExpand = true;
		fChildren.stream()
		.filter(TableNode.class::isInstance)
		.map(TableNode.class::cast)
		.forEach(TableNode::expandAll);
	}
	
	void collapseAll() {
		fExpand = false;
		fChildren.stream()
		.filter(TableNode.class::isInstance)
		.map(TableNode.class::cast)
		.forEach(TableNode::collapseAll);
	}
	
	//==========================
	// getters
	
	@Override
	public IDipParent dipDocElement() {
		return (IDipParent) super.dipDocElement();
	}
	
	public boolean expand() {
		return fExpand;
	}
	
	public void setExpand(boolean value) {
		fExpand = value;
	}
	
	public void setImageExpandLoc(Rectangle rectangle) {
		fExpandImageLoc = rectangle;
	}
	
	public Rectangle imageExpandLoc() {
		return fExpandImageLoc;
	}
	
	public List<IDipTableElement> children(){
		return fChildren;
	}

	public void setRoot() {
		fRoot = true;
		IDipParent req = dipDocElement().parent();
		while (req instanceof DipFolder) {
			fLevel++;
			req = req.parent();
		}
	}
	
	public boolean isRoot() {
		return fRoot;
	}
	
	public int level() {
		return fLevel;
	}

	@Override
	public DipTableModel model() {
		return fModel;
	}

	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((fChildren == null) ? 0 : fChildren.hashCode());
		result = prime * result + (fExpand ? 1231 : 1237);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		TableNode other = (TableNode) obj;
		if (fChildren == null) {
			if (other.fChildren != null)
				return false;
		} else if (!fChildren.equals(other.fChildren))
			return false;
		if (fExpand != other.fExpand)
			return false;
		return true;
	}
}
