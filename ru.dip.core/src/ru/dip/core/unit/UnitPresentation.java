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
import ru.dip.core.unit.form.FormPresentation;
import ru.dip.core.unit.md.MarkDownPresentation;
import ru.dip.core.unit.md.SubMarkdownPresentation;
import ru.dip.core.utilities.TagStringUtilities;

public class UnitPresentation extends UnitExtension implements IUnitPresentation, IFindedIdPoints {

	private UnitType fUnitType;
	private TablePresentation fTablePresentation;
	
	public UnitPresentation(IDipUnit element) {
		super(element);
	}

	@Override
	public TablePresentation getPresentation(){
		if (fTablePresentation == null){
			fTablePresentation = createTablePresentation();			
		}
		return fTablePresentation;
	}
	
	private TablePresentation createTablePresentation(){
		getUnitType();
		IDipUnit unit = getDipUnit();		
		switch (fUnitType){
		case IMAGE:{
			return new ImagePresentation(unit);
		}
		case TEXT:{
			return new TextPresentation(unit);
		}
		case MARKDOWN:{
			return new MarkDownPresentation(unit);
		}
		case SUBMARKDOWN:{
			return new SubMarkdownPresentation(unit);
		}
		case HTML:{
			return new HtmlUnitPresentation(unit);
		}		
		case CSV:{
			return new CsvUnitPresentation(unit);
		}
		case TABLE:{
			return new TableUnitPresentation(unit);
		}		
		case HTML_IMAGE:{
			return new HtmlImagePresentation(unit);
		}
		case UML:{
			return new PlantUmlPresentation(unit);
		}
		case DOT:{
			return new DotPresentation(unit);
		}
		case DIA:{
			return new DiaPresentation(unit);
		}				
		case FORM:{
			return new FormPresentation(unit);
		}
		case REPROT_REF:{
			return new ReportRefPresentation(unit);
		}	
		case TOC_REF:{
			return new TocRefPresentation(unit);
		}		
		case GLOS_REF:{
			return new GlossaryPresentation(unit);
		}		
		case CHANGELOG:{
			return new ChangeLogPresentation(unit);
		}
		case PAGEBREAK:{
			return new PagebreakPresentation(unit);
		}
		case JSON:{
			return new JsonPresentation(unit);
		}	
		default:
			break;
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
	
	public void updatePresentation(){
		if (defineUnitType() != fUnitType){
			fUnitType = defineUnitType();
		}
		fTablePresentation = createTablePresentation();			
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
		
		if (fTablePresentation == null) {
			updatePresentation();
		}
		if (fTablePresentation != null) {
			boolean findPres = fTablePresentation.findWord(text, caseSensitive);
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
		
		if (fTablePresentation == null) {
			updatePresentation();
		}
		int findedPresentation = 0;
		if (fTablePresentation != null) {
			if (fTablePresentation instanceof FormPresentation) {
				findedPresentation = ((FormPresentation)fTablePresentation).findText(text, findSettings, settings);
			} else {			
				findedPresentation = fTablePresentation.findText(text, findSettings);
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
		
		if (fTablePresentation == null) {
			updatePresentation();
		}
		if (fTablePresentation != null) {
			boolean findPres = fTablePresentation.appendFind(text, caseSensitive);
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
		
		if (fTablePresentation == null) {
			updatePresentation();
		}
		if (fTablePresentation != null) {
			boolean findPres = fTablePresentation.appendWord(text, caseSensitive);
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
		if (fTablePresentation != null) {
			fTablePresentation.cleanFind();
		}
	}

	@Override
	public void removeIfFind(Collection<String> terms) {
		getPresentation();
		if (fTablePresentation != null) {			
			fTablePresentation.removeIfFind(terms);
		}
	}
	
	@Override
	public void findTerms(Set<String> terms) {
		getPresentation();
		if (fTablePresentation != null) {			
			fTablePresentation.findTerms(terms);
		}		
	}
	
	@Override
	public void findVars(Set<String> vars) {
		getPresentation();
		if (fTablePresentation != null) {			
			fTablePresentation.findVars(vars);
		}			
	}
	
}
