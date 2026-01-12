package ru.dip.ui.table.ktable.model;

import java.util.List;

import ru.dip.ui.table.ktable.model.HideElements;
import ru.dip.ui.table.ktable.model.IDipTableElement;

public interface INumberElement {
	
	default List<IDipTableElement> linkedWithibleElements() {
		if (parent() == null) {
			return List.of(self());
		}
		return parent().linkedElements(self(), HideElements.EXCLUDE);
	}
	
	/**
	 * С учетом пустых Description
	 */
	default List<IDipTableElement> allLinkedElements() {
		if (parent() == null) {
			return List.of(self());
		}
		return parent().linkedElements(self(), HideElements.INCLUDE);
	}
	
	default IDipTableElement startElement(HideElements hideElements) {
		if (parent() == null) {
			return self();
		}
		return parent().startElement(self(), hideElements);
	}
	
	default IDipTableElement endElement(HideElements hideElements) {
		if (parent() == null) {
			return self();
		}
		return parent().endElement(self(), hideElements);
	}
	
	
	default boolean isFirst() {
		return getNumber() == 0;
	}
	
	default boolean isLast() {
		return getNumber() == getLinkedElements().size() - 1;
	}
	
	void setLinkedElements(List<IDipTableElement> linkedElements);
	
	List<IDipTableElement> getLinkedElements();
	
	void setNumber(int number);
	
	int getNumber();
	
	ITableElementContainer parent(); 
	
	IDipTableElement self();

}
