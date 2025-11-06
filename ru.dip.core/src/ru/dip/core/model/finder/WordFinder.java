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
package ru.dip.core.model.finder;

import java.util.ArrayList;
import java.util.List;

import ru.dip.core.model.interfaces.IFindable;
import ru.dip.core.model.interfaces.IDipDocumentElement;
import ru.dip.core.model.interfaces.ISearchElementsHolder;
import ru.dip.core.model.interfaces.IUnitPresentation;
import ru.dip.core.unit.UnitExtension;
import ru.dip.core.unit.UnitPresentation;
import ru.dip.core.unit.form.AbstractFormField;
import ru.dip.core.unit.form.IFormSettings;

public class WordFinder implements IFinder {
	
	private IFormSettings fFormSettings;
	protected ISearchElementsHolder fDipTable;
	protected String fText;
	
	// result
	protected int fCurrentIndex = 0;
	protected int fCurrentPointNumber = 0;	
	protected List<IDipDocumentElement> fElements = new ArrayList<>();
	private List<Integer> fMatchCounters = new ArrayList<>(); // количество совпадений (свзяь с fElements по индексу)
	
	public WordFinder(ISearchElementsHolder table, String text, IFormSettings formSettings) {
		fDipTable = table;
		fText = text;
		fFormSettings = formSettings;
	}
	
	//=====================
	// find
	
	@Override
	public void find(FindSettings findSettings) {		
		fElements = new ArrayList<>();
		fCurrentIndex = 0;
		fCurrentPointNumber = 0;
		IDipDocumentElement[] children = fDipTable.getDipDocElementsForSearch();			
		for (int i = 0; i < children.length; i++) {
			findInDipDocElement(children[i], findSettings);
		}
	}
		
	private void findInDipDocElement(IDipDocumentElement dipDocumentElement, FindSettings findSettings) {
		if (dipDocumentElement instanceof IFindable) {
			if (!isVisible(dipDocumentElement, findSettings.findInDisableObjs())) {
				return;
			}
			
			int findResult = findResultInDipDocElement(dipDocumentElement, findSettings); 
			if (findResult > 0) {
				fElements.add(dipDocumentElement);
				fMatchCounters.add(findResult);
			}
		}	
	}
	
	private boolean isVisible(IDipDocumentElement dipDocumentElement, boolean hideDisable) {
		// если объект отключен
		if (dipDocumentElement.isDisabled() && hideDisable) {
			return false;
		}
		// если поле не отображается (для FormField)
		if (dipDocumentElement instanceof AbstractFormField) {
			AbstractFormField field = (AbstractFormField) dipDocumentElement;
			if (!field.isVisible(fFormSettings)) {
				return false;
			}
		}
		return true;
	}
	
	private int findResultInDipDocElement(IDipDocumentElement dipDocumentElement, FindSettings settins) {
		int findResult = 0;
		if (isForm(dipDocumentElement)) {				
			findResult =  findInFormPresentation((UnitPresentation) dipDocumentElement, settins);
		} else if (dipDocumentElement instanceof AbstractFormField) {
			AbstractFormField formField = (AbstractFormField) dipDocumentElement;
			if (formField.isFirstVisibleField(fFormSettings)) {
				findResult = formField.findTextWithId(fText, settins);
			} else {
				findResult = formField.findText(fText, settins);
			}
		} else {	
			findResult = ((IFindable) dipDocumentElement).findText(fText, settins);
		}
		return findResult;
	}
	
	
	private boolean isForm(IDipDocumentElement dipDocumentElement) {
		return dipDocumentElement instanceof UnitPresentation 
				&& ((UnitPresentation) dipDocumentElement).getUnitType().isForm();
	}
	
	protected int findInFormPresentation(UnitPresentation unitPresentation, FindSettings findSettings) {
		return unitPresentation.findText(fText, findSettings, fFormSettings);
	}
	
	//========================
	// results
	
	@Override
	public int size() {
		return fMatchCounters.stream().mapToInt(Integer::intValue).sum();
	}
	
	@Override
	public int unitSize() {
		return (int) fElements.stream().map(IDipDocumentElement::strong).distinct().count();
	}
	
	@Override
	public List<IDipDocumentElement> getElements(){
		return fElements;
	}
		
	
	//======================
	// navigation
	
	@Override
	public boolean hasNext() {
		return fElements.size() > 0;
	}
		
	@Override
	public IDipDocumentElement next(boolean switchPosition) {
		return switchPosition ? next() : nextElement();
	}
	
	private IDipDocumentElement next() {
		IDipDocumentElement dipDocElement = fElements.get(fCurrentIndex);
		int maxPosition = fMatchCounters.get(fCurrentIndex);

		if (fCurrentPointNumber >= maxPosition - 1) {
			fCurrentPointNumber = 0;
			incrementCurrentIndex();
			dipDocElement = fElements.get(fCurrentIndex);
		} else {
			fCurrentPointNumber++;
		}
		return dipDocElement;
	}
	
	private IDipDocumentElement nextElement() {
		cleanCurrentPosition();
		IDipDocumentElement currentDipDocElement = fElements.get(fCurrentIndex);
		if (!hasNextElement()) {
			return currentDipDocElement;
		}
		incrementCurrentIndex();
		IDipDocumentElement next = fElements.get(fCurrentIndex);
		while (next.strong() == currentDipDocElement.strong()) {
			incrementCurrentIndex();		
			next = fElements.get(fCurrentIndex);
		}
		return next;
	}
	
	private boolean hasNextElement() {		
		return fElements.stream().map(r -> r instanceof AbstractFormField ? ((AbstractFormField)r).getDipUnit()
				: r).distinct().count() > 1;
	}
	
	private void incrementCurrentIndex() {
		fCurrentIndex++;
		if (fCurrentIndex >= fElements.size()) {
			fCurrentIndex = 0;
		}
	}
	
	@Override
	public IDipDocumentElement previous(boolean switchPosition) {
		return switchPosition ? previous() : previousElement();
	}
	
	private IDipDocumentElement previousElement() {
		cleanCurrentPosition();
		IDipDocumentElement currentDipDocumentElement = fElements.get(fCurrentIndex);
		if (!hasNextElement()) {
			return currentDipDocumentElement;
		}
		decrementCurrentIndex();
		IDipDocumentElement previous = fElements.get(fCurrentIndex);
		while (previous.strong() == currentDipDocumentElement.strong()) {
			decrementCurrentIndex();		
			previous = fElements.get(fCurrentIndex);
		}
		return previous;
	}
	
	private IDipDocumentElement previous() {		
		IDipDocumentElement dipDocElement = fElements.get(fCurrentIndex);
		if (fCurrentPointNumber > 0) {
			fCurrentPointNumber--;
		} else {
			decrementCurrentIndex();			
			dipDocElement = fElements.get(fCurrentIndex);
			fCurrentPointNumber = fMatchCounters.get(fCurrentIndex) - 1; 
		}			
		return dipDocElement;
	}
	
	private void decrementCurrentIndex() {
		fCurrentIndex--;
		if (fCurrentIndex < 0) {
			fCurrentIndex = fElements.size() - 1;
		}
	}
	
	//====================
	// current
	
	@Override
	public boolean isCurrent(IDipDocumentElement dipUnit) {
		if (fElements.isEmpty()) {
			return false;
		}
		
		IDipDocumentElement dipDocElement = fElements.get(fCurrentIndex);		
		if (dipDocElement instanceof IUnitPresentation) {
			dipDocElement = ((UnitExtension) dipDocElement).getDipUnit();
		}
		// WTF ?		
		return dipDocElement == dipUnit;
	}
	
	@Override
	public IDipDocumentElement getCurrent() {
		if (fElements != null && fElements.size() > fCurrentIndex) {		
			return fElements.get(fCurrentIndex);
		}
		return null;
	}
	
	@Override
	public int getCurrentPointNumber() {
		return fCurrentPointNumber;
	}
	
	@Override
	public void cleanCurrentPosition() {
		fCurrentPointNumber = -1;
	}
	
	@Override
	public int getCurrentNumber() {
		return fCurrentPointNumber;
	}

	@Override
	public int getCurrentIndex() {
		return fCurrentIndex;
	}

}
