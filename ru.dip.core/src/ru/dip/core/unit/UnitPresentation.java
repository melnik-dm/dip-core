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
package ru.dip.core.unit;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.swt.graphics.Point;

import ru.dip.core.model.finder.FindSettings;
import ru.dip.core.model.finder.IFindedIdPoints;
import ru.dip.core.model.interfaces.IDipUnit;
import ru.dip.core.model.interfaces.IUnitPresentation;
import ru.dip.core.unit.factory.IPresentationFactory;
import ru.dip.core.unit.factory.PresentationFactoryProvider;
import ru.dip.core.unit.form.FormPresentation;
import ru.dip.core.utilities.TagStringUtilities;

public class UnitPresentation extends UnitExtension implements IUnitPresentation, IFindedIdPoints {

	private UnitType fUnitType;

	public UnitPresentation(IDipUnit element) {
		super(element);
	}
	
	public IFile getFile() {
		return getDipUnit().resource();
	}
	
	@Override
	public TablePresentation getPresentation(){
		TablePresentation tablePresentation = UnitPresentationCache.getPresentation(getFile());
		if (tablePresentation == null) {
			tablePresentation = createTablePresentation();
			UnitPresentationCache.putPresentation(getFile(), tablePresentation);
		}
		return tablePresentation;
	}
	
	private TablePresentation createTablePresentation(){
		getUnitType();
		IDipUnit unit = getDipUnit();
		IPresentationFactory factory = PresentationFactoryProvider.getFactory(fUnitType);
		if (factory != null) {
			return factory.createPresentation(unit);
		}					
		return null;
	}
	
	@Override
	public UnitType getUnitType(){
		if (fUnitType == null){
			fUnitType = defineUnitType();
		}		
		return fUnitType;
	}
	
	private UnitType defineUnitType(){
		IFile file = (IFile) getDipUnit().resource();
		String extension = file.getFileExtension();
		String fileName = file.getName();
		return UnitType.defineUnitType(extension, fileName, dipProject());
	}
	
	public TablePresentation updatePresentation(){
		if (defineUnitType() != fUnitType){
			fUnitType = defineUnitType();
		}
		TablePresentation tablePresentation = createTablePresentation();
		UnitPresentationCache.putPresentation(getFile(), tablePresentation);
		return tablePresentation;
	}
	
	//==============================
	// find
	
	/*
	 * Надо убрать общий код для find и findWord!!!
	 */
	private List<Point> fFindedIdPoints = null;
	
	@Override
	public boolean findWord(String text, boolean caseSensitive) {
		IDipUnit unit = getDipUnit();
		fFindedIdPoints = TagStringUtilities.findWords(unit.name(), text, caseSensitive);
		
		TablePresentation tablePresentation = getPresentation();
		
		if (tablePresentation != null) {
			boolean findPres = tablePresentation.findWord(text, caseSensitive);
			if (findPres) {
				return findPres;
			}
		}
		return fFindedIdPoints != null;	
	}

	@Override
	public int findText(String text, FindSettings findSettings) {
		return findText(text, findSettings, null);
	}
	
	public int findText(String text, FindSettings findSettings, Object settings) {
		IDipUnit unit = getDipUnit();
		
		int findedId = 0;
		
		if (findSettings.findInId()) {
			fFindedIdPoints = TagStringUtilities.findText(unit.name(), text, findSettings.caseSensetive());
			if (fFindedIdPoints != null) {
				findedId = fFindedIdPoints.size();
			}
		}
		
		TablePresentation tablePresentation = getPresentation();
		int findedPresentation = 0;
		if (tablePresentation != null) {
			if (tablePresentation instanceof FormPresentation) {
				findedPresentation = ((FormPresentation)tablePresentation).findText(text, findSettings, settings);
			} else {			
				findedPresentation = tablePresentation.findText(text, findSettings);
			}
		}
		return findedId + findedPresentation;	
	}
	
	
	@Override
	public boolean appendFind(String text, boolean caseSensitive) {
		IDipUnit unit = getDipUnit();
		List<Point> findedIdPoints =TagStringUtilities.findText(unit.name(), text, caseSensitive);
		if (findedIdPoints != null) {
			if (fFindedIdPoints == null) {
				fFindedIdPoints = findedIdPoints;
			} else {
				fFindedIdPoints.addAll(findedIdPoints);
			}
		}
		
		TablePresentation tablePresentation = getPresentation();

		if (tablePresentation != null) {
			boolean findPres = tablePresentation.appendFind(text, caseSensitive);
			if (findPres) {
				return findPres;
			}
		}
		return fFindedIdPoints != null;
	}
	
	@Override
	public boolean appendWord(String text, boolean caseSensitive) {
		IDipUnit unit = getDipUnit();
		List<Point> findedIdPoints =TagStringUtilities.findWords(unit.name(), text, caseSensitive);
		if (findedIdPoints != null) {
			if (fFindedIdPoints == null) {
				fFindedIdPoints = findedIdPoints;
			} else {
				fFindedIdPoints.addAll(findedIdPoints);
			}
		}
		
		TablePresentation tablePresentation = getPresentation();

		if (tablePresentation != null) {
			boolean findPres = tablePresentation.appendWord(text, caseSensitive);
			if (findPres) {
				return findPres;
			}
		}
		return fFindedIdPoints != null;
	}
	
	public boolean isFind() {
		return fFindedIdPoints != null;
	}
	
	@Override
	public List<Point> getFindedIdPoints(){
		return fFindedIdPoints;
	}
	
	@Override
	public void cleanFind() {
		fFindedIdPoints = null;
		UnitPresentationCache.applyIfExists(getFile(), TablePresentation::cleanFind);
	}

	@Override
	public void removeIfFind(Collection<String> terms) {
		getPresentation();
		UnitPresentationCache.applyIfExists(getFile(), TablePresentation::cleanFind);
	}
	
	@Override
	public void findTerms(Set<String> terms) {
		getPresentation();
		UnitPresentationCache.applyIfExists(getFile(), tp -> tp.findTerms(terms));
	}
	
	@Override
	public void findVars(Set<String> vars) {
		TablePresentation tablePresentation = getPresentation();
		if (tablePresentation != null) {			
			tablePresentation.findVars(vars);
		}			
	}

	@Override
	public void dispose() {
	}
	
}
