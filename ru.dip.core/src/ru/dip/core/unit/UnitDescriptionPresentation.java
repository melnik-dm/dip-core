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
import java.util.stream.Collectors;

import org.eclipse.swt.graphics.Point;

import ru.dip.core.model.DipDescription;
import ru.dip.core.model.finder.FindSettings;
import ru.dip.core.model.interfaces.IDipUnit;
import ru.dip.core.model.interfaces.IUnitDescription;
import ru.dip.core.unit.form.FormPresentation;
import ru.dip.core.utilities.TagStringUtilities;
import ru.dip.core.utilities.text.Terms;

public class UnitDescriptionPresentation extends UnitExtension implements IUnitDescription  {

	public UnitDescriptionPresentation(IDipUnit element) {
		super(element);
	}
	
	@Override
	public String fullLinkTitle() {
		String fDesciptionContent = description();
		IDipUnit unit = getDipUnit();
		UnitType type = unit.getUnitType();
		if (type.isImageType()) {
			StringBuilder builder = new StringBuilder();
			builder.append("Рисунок ");
			builder.append(unit.getNumer());
			builder.append(".");
			if (fDesciptionContent != null && !fDesciptionContent.isEmpty()) {
				builder.append(" ");
				builder.append(fDesciptionContent);
			}
			return builder.toString();
		} else if (type.isTableDescription()) {
			StringBuilder builder = new StringBuilder();
			builder.append("Таблица ");
			builder.append(unit.getNumer());
			builder.append(".");
			if (fDesciptionContent != null && !fDesciptionContent.isEmpty()) {
				builder.append(" ");
				builder.append(fDesciptionContent);
			}
			return builder.toString();
		} else if (type.isForm()) {
			StringBuilder builder = new StringBuilder();
			FormPresentation presentation = (FormPresentation) getDipUnit().getUnitPresentation()
					.getPresentation();
			builder.append(presentation.schemaName());
			builder.append(" ");
			builder.append(unit.getNumer());
			builder.append(".");
			if (fDesciptionContent != null && !fDesciptionContent.isEmpty()) {
				builder.append(" ");
				builder.append(fDesciptionContent);
			}						
			//builder.append(" - ");
			//builder.append(ReqUtilities.relativeProjectID(unit));
			return builder.toString();
		}
		return fDesciptionContent;
	}

	public String longLinkTitle() {
		String fDesciptionContent = description();
		IDipUnit unit = getDipUnit();
		UnitType type = unit.getUnitType();
		if (type.isImageType()) {
			StringBuilder builder = new StringBuilder();
			builder.append("рисунок ");
			builder.append(unit.getNumer());
			return builder.toString();
		} else if (type.isTableDescription()) {
			StringBuilder builder = new StringBuilder();
			builder.append("таблица ");
			builder.append(unit.getNumer());
			return builder.toString();
		} else if (type.isForm()) {
			StringBuilder builder = new StringBuilder();
			FormPresentation presentation = (FormPresentation) getDipUnit().getUnitPresentation()
					.getPresentation();
			builder.append(presentation.schemaName());
			builder.append(" ");
			builder.append(unit.getNumer());
			return builder.toString();
		}
		return fDesciptionContent;
	}
	
	public String shortLinkTitle() {
		String fDesciptionContent = description();
			IDipUnit unit = getDipUnit();
			UnitType type = unit.getUnitType();
			if (type.isImageType()) {
				StringBuilder builder = new StringBuilder();
				builder.append("рис. ");
				builder.append(unit.getNumer());			
				return builder.toString();
			} else if (type.isTableDescription()) {
				StringBuilder builder = new StringBuilder();
				builder.append("табл. ");
				builder.append(unit.getNumer());					
				return builder.toString();
			} else if (type.isForm()) {
				StringBuilder builder = new StringBuilder();
				builder.append("[");
				builder.append(unit.getNumer());
				builder.append("]");
				return builder.toString();
			}
		return fDesciptionContent;
	}
	
	@Override
	public boolean isNumberedDescription() {
		IDipUnit unit = getDipUnit();
		UnitType type = unit.getUnitType();
		return type.isNumerated();
	}
	
	@Override
	public String getDecriptionContent(){	
		DipDescription description = getDipUnit().dipDescription();
		if (description != null){
			String descriptionContent = description.getDescriptionContent();
			if (descriptionContent != null && !descriptionContent.isEmpty()){
				return descriptionContent;
			}
		}
		return description();	
	}

	//===========================
	// find
	
	private List<Point> fFindedPoints = null;
	private List<Point> fFindedPointsWithIndent = null;

	@Override
	public boolean contains(String text, FindSettings findSettings) {
		return find(text, findSettings.caseSensetive(), TEXT);
	}
	
	@Override
	public boolean findWord(String text, boolean caseSensitive) {
		return find(text, caseSensitive, WORD);
	}
	
	@Override
	public int findText(String text, FindSettings findSettings) {
		if (contains(text, findSettings)) {
			return fFindedPoints.size();
		}		
		return 0;
	}
	
	private boolean find(String text, boolean caseSensitive, boolean word) {
		String description = getDipUnit().description();
		if (description != null) {
			fFindedPoints = TagStringUtilities.findText(description, text, caseSensitive, word);
		}
		return fFindedPoints != null;
	}
	
	@Override
	public boolean appendFind(String text, boolean caseSensitive) {
		return appendFind(text, caseSensitive, TEXT);		
	}
	
	@Override
	public boolean appendWord(String text, boolean caseSensitive) {
		return appendFind(text, caseSensitive, WORD);
	}
	
	public boolean appendFind(String text, boolean caseSensitive, boolean word) {
		String description = getDipUnit().description();
		if (description != null) {
			List<Point> findedIdPoints = TagStringUtilities.findText(description, text, caseSensitive, word);
			if (findedIdPoints != null) {
				if (fFindedPoints == null) {
					fFindedPoints = findedIdPoints;
				} else {
					fFindedPoints.addAll(findedIdPoints);
				}
				return true;
			}
		}
		return false;
	}
	
	@Override
	public void  updateFindedPoints(String content) {
		int indent = content.length() - getDipUnit().description().length();
		fFindedPointsWithIndent = fFindedPoints.stream()
				.map(p -> new Point(p.x + indent, p.y + indent))
				.collect(Collectors.toList());		
	}

	
	@Override
	public boolean hasFindResult() {
		return fFindedPoints != null;
	}

	@Override
	public List<Point> getFindedPoints() {
		return fFindedPoints;
	}
	
	public List<Point> getFindedPointsWithIndent(){
		return fFindedPointsWithIndent;
	}

	@Override
	public void cleanFind() {
		fFindedPoints = null;
	}
	
	//============================
	// IGlossarySupport
	
	@Override
	public void removeIfFind(Collection<String> terms) {
		String description = getDipUnit().description();
		if (description != null) {
			TagStringUtilities.removeIfContains(description, terms);
		}
	}
	
	@Override
	public void findTerms(Set<String> terms) {
		String description = getDipUnit().description();
		if (description != null) {
			Terms.findTerms(description, terms);
		}		
	}
	
	//========================
	// IVariableSupport
	
	@Override
	public void findVars(Set<String> vars) {
		String description = getDipUnit().description();
		if (description != null) {
			Terms.findVarsNames(description, vars);
		}		
	}

}
