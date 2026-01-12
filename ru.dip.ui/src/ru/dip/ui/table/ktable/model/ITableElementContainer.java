package ru.dip.ui.table.ktable.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import ru.dip.core.model.interfaces.IDipDocumentElement;
import ru.dip.core.unit.UnitDescriptionPresentation;
import ru.dip.core.unit.form.FieldUnity;
import ru.dip.core.unit.form.FormField;
import ru.dip.core.utilities.DipTableUtilities;

/**
 * Родительский элемент для IDipTableElement
 */
public interface ITableElementContainer {
	
	boolean filter(IDipTableElement element);
	
	List<IDipTableElement> children();
	
	//===============================
	// navigation
	
	/**
	 * @param element - начальный элемент
	 * @param hideElemetns - включать ли неотображаемые элементы
	 * @return список всех связанных элементов для соответствующего файла или папки
	 */
	default List<IDipTableElement> linkedElements(IDipTableElement element, HideElements hideElements){
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
	default IDipTableElement previousAtomicElement(IDipTableElement element, HideElements hideElements) {
		int index = children().indexOf(element);		
		if (index <= 0) {
			return null;
		}
		IDipTableElement previous =  children().get(index - 1);
		if (hideElements == HideElements.EXCLUDE &&!filter(previous)) {
			return previousAtomicElement(previous, hideElements);
		} 
		return previous;
	}
	
	/*
	 *  Следующий атомарный элемент, может быть Description, UnitPresentation, IDipParent
	 */
	default IDipTableElement nextAtomicElement(IDipTableElement element, HideElements hideElements) {
		int index = children().indexOf(element);
		if (children().size() <= index +1) {
			return null;
		}
		IDipTableElement next =  children().get(index + 1);
		if (hideElements == HideElements.EXCLUDE && !filter(next)) {
			return nextAtomicElement(next, hideElements);
		} 
		return next;
	}
	
	
	
	
	/*
	 * Первый индекс для папки 
	 */
	 default int getFirstParentIndex(){
		for (int i = 0; i < children().size(); i++){
			IDipDocTablePaintable element = children().get(i);
			if (element instanceof TableNode){
				return i;
			}
		}
		return children().size();
	}
	
	/*
	 * Последний индекс для файла 
	 */
	default int getLastUnitIndex() {
		int result = -1;
		for (int i = 0; i < children().size(); i++){
			IDipDocTablePaintable element = children().get(i);
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
	default int previousStartElementIndex(IDipTableElement startElement,  HideElements hideElements) {
		IDipTableElement startPreviousElement = previousStartElement(startElement, hideElements);
		if (startPreviousElement == null) {
			return -1;
		}		
		return children().indexOf(startPreviousElement);				
	}
	
	/*
	 * Предыдущий элемент (если файл, то UnitPresentation)
	 */
	default IDipTableElement previousStartElement(IDipTableElement startElement, HideElements hideElements) {
		IDipTableElement previousElement = previousAtomicElement(startElement, HideElements.EXCLUDE);
		if (previousElement == null) {
			return null;
		}
		IDipTableElement startPreviousElement = startElement(previousElement, hideElements);
		return startPreviousElement;
	}
	
	default  IDipTableElement startElement(IDipTableElement element, HideElements hideElements) {				
		IDipDocumentElement currentUnit = element.dipResourceElement();
		IDipTableElement currentElement = element;
		IDipTableElement previous = previousAtomicElement(element, hideElements);
		while (previous != null && currentUnit == previous.dipResourceElement()) {
			currentElement = previous;
			previous = previousAtomicElement(previous, hideElements);
		}
		return currentElement;
	}
	
	default IDipTableElement endElement(IDipTableElement element, HideElements hideElements) {
		IDipDocumentElement currentUnit = element.dipResourceElement();
		IDipTableElement currentElement = element;
		IDipTableElement next = nextAtomicElement(element, hideElements);
		while (next != null && currentUnit == next.dipResourceElement()) {
			currentElement = next;
			next = nextAtomicElement(next, hideElements);
		}
		return currentElement;
	}
	
	default int indexByReqIndex(IDipDocumentElement req, int index) {
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
		return children().indexOf(endElement(previousElement, HideElements.EXCLUDE)) + 1;
	}
	
	
	
	//===============================
	// find
	
	default IDipTableElement find(String name) {
		for (IDipTableElement element: children()) {
			if ((element.dipDocElement()).name().equals(name)) {
				return startElement(element, HideElements.EXCLUDE);
			}
		}
		return null;
	}
	
	default IDipTableElement find(IDipDocumentElement dipDocElement) {
		for (IDipTableElement element: children()) {
			if (element.dipDocElement() == dipDocElement) {
				return startElement(element, HideElements.EXCLUDE);
			}
		}
		return null;
	}
	
	default Optional<IDipTableElement> findByName(IDipDocumentElement dipDocElement) {
		if (dipDocElement instanceof FormField) {
			return findFieldByName((FormField) dipDocElement);
		}
		if (dipDocElement instanceof UnitDescriptionPresentation) {
			return findDescriptionByName((UnitDescriptionPresentation) dipDocElement);
		}
		
		String name = dipDocElement.name();
		for (IDipTableElement element: children()) {
			if (element.dipDocElement().name().equals(name)) {
				return Optional.of(element);
			}
		}
		return Optional.empty();
	}
	
	default Optional<IDipTableElement> findFieldByName(FormField field){
		String name = field.name();
		for (IDipTableElement element: children()) {
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
	
	default Optional<IDipTableElement> findDescriptionByName(UnitDescriptionPresentation description){
		String name = description.name();
		for (IDipTableElement element: children()) {
			if (!element.dipDocElement().name().equals(name)) {
				continue;
			}
			if (element.isDescription()) {
				return Optional.of(element);
			}	
		}
		return Optional.empty();
	}
	
	
}
