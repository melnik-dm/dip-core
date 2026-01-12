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
package ru.dip.editors.report.content.model;

import java.util.ArrayList;
import java.util.List;

import ru.dip.core.form.model.FormFieldType;
import ru.dip.core.model.DipUnit;
import ru.dip.core.model.interfaces.IDipElement;
import ru.dip.core.model.interfaces.IDipParent;
import ru.dip.core.model.interfaces.IDipUnit;
import ru.dip.core.report.checker.ReportEntryChecker;
import ru.dip.core.report.checker.ReportRuleSyntaxException;
import ru.dip.core.unit.UnitExtension;
import ru.dip.core.unit.form.AbstractFormField;
import ru.dip.core.unit.form.FieldUnity;
import ru.dip.core.unit.form.FormField;
import ru.dip.core.unit.form.IFormSettings;
import ru.dip.editors.report.content.ReportContentModel;
import ru.dip.ui.table.ktable.model.IDipTableElement;
import ru.dip.ui.table.ktable.model.IDipTableModel;
import ru.dip.ui.table.ktable.model.ITableNode;
import ru.dip.ui.table.ktable.model.TableElement;
import ru.dip.ui.table.table.TableSettings;

public class RceEntryElement extends RceDipElement implements ITableNode {
	
	private final ReportEntryPresentation fEntryPresentation;
	private ReportContentModel fModel;
	
	private List<IDipTableElement> fChildren;
	
	public RceEntryElement(ReportContentModel model, ReportEntryPresentation entryPresentation) {
		super(entryPresentation, null);
		fModel = model;
		fEntryPresentation = entryPresentation;
	}
	
	/**
	 * Создает список детей
	 * @throws ReportRuleSyntaxException 
	 */
	public void computeChildren() throws ReportRuleSyntaxException {
		fChildren = new ArrayList<>();
		List<IDipElement> dipElements = ReportEntryChecker.findEntry(fEntryPresentation.getEntry(), fModel.getDipProject());
		for (IDipElement element: dipElements) {
			if (element instanceof IDipUnit) {
				IDipUnit dipUnit = (IDipUnit) element;
				List<UnitExtension> unitExtensions = dipUnit.getUnionExtensions();
				for (UnitExtension unitExtension: unitExtensions) {
					fChildren.add(new RceDipElement(unitExtension, this));
				}
				
			} else {
				fChildren.add(new RceNotDocumentElement(element, this));
				
				// throw new RuntimeException();
				// надо будет решить, что делать  (тут всякие зарезервированные штуки и т.д.)
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
					joinChildren.add(new TableElement(fieldUnity, currentElement.parent()));					
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

	@Override
	public boolean filter(IDipTableElement element) {
		return false;
	}


	@Override
	public List<IDipTableElement> children() {
		return fChildren;
	}


	@Override
	public int level() {
		return 0;
	}

	@Override
	public IDipParent dipDocElement() {
		return fEntryPresentation;
	}

	@Override
	public boolean expand() {
		return true;
	}
	
	@Override
	public void setExpand(boolean expand) {
		
	}
	
	@Override
	public List<IDipTableElement> visibleUnitElements() {
		return null;
	}

	@Override
	public IDipTableModel model() {
		return null;
	}

	// ==========================
	// actions

	@Override
	public ITableNode addNewFolderToBegin(IDipParent parent) {
		return null;
	}

	@Override
	public IDipTableElement addNewUnitToStart(DipUnit unit) {
		return null;
	}

	@Override
	public IDipTableElement addNewUnit(DipUnit newUnit, IDipTableElement selectedElement, boolean b) {
		return null;
	}

	@Override
	public void down(IDipTableElement element) {
	}

	@Override
	public void down(List<IDipTableElement> elements) {
	}

	@Override
	public void up(List<IDipTableElement> elements) {

	}

	@Override
	public void up(IDipTableElement element) {
	}

	@Override
	public void delete(IDipTableElement tableElement) {
	}

	@Override
	public ITableNode addNewNeightborFolder(IDipParent dipParent, ITableNode tableNode, boolean before) {
		return null;
	}

	@Override
	public IDipTableElement addNewUnitToEnd(DipUnit unit) {
		return null;
	}
	
	
}
