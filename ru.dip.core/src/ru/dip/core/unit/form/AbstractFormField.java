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
package ru.dip.core.unit.form;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.eclipse.swt.graphics.Point;

import ru.dip.core.model.finder.FindSettings;
import ru.dip.core.model.finder.FinderIdManager;
import ru.dip.core.model.finder.IContent;
import ru.dip.core.model.finder.IFindPoints;
import ru.dip.core.model.finder.IFindResult;
import ru.dip.core.model.finder.IFindedIdPoints;
import ru.dip.core.model.finder.TextFinderManager;
import ru.dip.core.model.interfaces.IDipUnit;
import ru.dip.core.model.interfaces.IFindable;
import ru.dip.core.model.interfaces.IGlossaryPoints;
import ru.dip.core.model.interfaces.IGlossaryPointsHolder;
import ru.dip.core.model.interfaces.ISpellErrorPoints;
import ru.dip.core.schema.FormShowProperties;
import ru.dip.core.unit.GlossaryPoints;
import ru.dip.core.unit.SpellErrorsPoints;
import ru.dip.core.unit.UnitExtension;
import ru.dip.core.unit.md.MarkdownSettings;
import ru.dip.core.unit.md.MdFormatPoints;
import ru.dip.core.utilities.TagStringUtilities;
import ru.dip.core.utilities.text.Terms;

public abstract class AbstractFormField extends UnitExtension implements IFormFields, IFindable, IFindPoints, IFindedIdPoints, IGlossaryPointsHolder, ISpellErrorPoints, IContent {

	private Point fMainTitlePoint;   // заголовок всей формы (нужен только для 1-го поля)
	private MdFormatPoints fMdFormatPoints = new MdFormatPoints();
	private List<Point> fTitlePonts = new ArrayList<>();
	protected List<FormField> fFormFields;
	private final SpellErrorsPoints fSpellErrorPoints;
	private final IGlossaryPoints fGlossaryPoints;
	protected String fText;
	protected final TextFinderManager fFinderManager;
	private final FinderIdManager fFinderIdManager;

	public AbstractFormField(IDipUnit unit) {
		super(unit);
		fGlossaryPoints = unit != null ? new GlossaryPoints(unit.dipProject()) : null;
		fSpellErrorPoints = new SpellErrorsPoints();
		fFinderManager = new TextFinderManager(this);
		fFinderIdManager = new FinderIdManager(unit);
	}

	public abstract boolean isVisible(IFormSettings formSetting);
	
	public abstract boolean isFirstVisibleField(IFormSettings formSetting);
	
	public String tablePresentation(IFormSettings formSettings,
			MarkdownSettings mdSettings, int lineLength, boolean first, boolean last) {
		clearTitlePoints();		
		FormPresentationBuilder builder = new FormPresentationBuilder(this,
				fMdFormatPoints, 
				formSettings,
				getFormPresentation().getFormShowProperties(),
				mdSettings, lineLength);
		builder.build(first);
		String result = builder.toString();		
		if (formSettings.isBetweenLine() && !last) {
			result = result + "\n";
		} 			
		return result;
	}	

	protected void clearTitlePoints() {
		fMdFormatPoints.clear();
		fTitlePonts.clear();
	}

	// ===================================
	// IGlossarySupport

	@Override
	public void removeIfFind(Collection<String> terms) {
		if (fText != null) {
			TagStringUtilities.removeIfContains(fText, terms);
		}
	}

	@Override
	public void findTerms(Set<String> terms) {
		if (fText != null) {
			Terms.findTerms(fText, terms);
		}
	}
	
	// ===================================
	// IVariableSupport
	
	@Override
	public void findVars(Set<String> vars) {
		if (fText != null) {
			Terms.findVarsNames(fText, vars);
		}
	}

	// ========================
	// find
		
	@Override
	public boolean contains(String text, FindSettings findSettings) {
		return fFinderManager.contains(text, findSettings);		
	}
	
	@Override
	public boolean findWord(String text, boolean caseSensitive) {
		return fFinderManager.findWord(text, caseSensitive);
	}

	@Override
	public int findText(String text, FindSettings findSettings) {
		return fFinderManager.findText(text, findSettings);
	}
	
	public int findTextWithId(String text, FindSettings findSettings) {
		int findId = fFinderIdManager.find(text, findSettings);		
		int findField =  fFinderManager.findText(text, findSettings);	
		return findId + findField;
	}

	public void updateFindedPoints(String content) {
		// нужно отсечь точки из заголовков полей, 
		// поэтому сравниваем с первоначальным поиском, который был только по содержимому 
		int oldPointNumber = fFinderManager.size();
		fFinderManager.updateFindedPoints(content);		
		List<Point> points = fFinderManager.getFindedPoints();
		if (points != null && points.size() > oldPointNumber) {
			for (int i = 0; i < points.size() - oldPointNumber; i++) {
				points.remove(0);
			}					
		}
	}
	
	@Override
	public boolean appendFind(String text, boolean caseSensitive) {
		return fFinderManager.appendFind(text, caseSensitive);
	}

	@Override
	public boolean appendWord(String text, boolean caseSensitive) {
		return fFinderManager.appendWord(text, caseSensitive);
	}

	@Override
	public void cleanFind() {
		fFinderManager.cleanFind();
		fFinderIdManager.cleanFind();
	}

	@Override
	public boolean hasFindResult() {
		return fFinderManager.hasFindResult();
	}

	@Override
	public List<Point> getFindedPoints() {
		return fFinderManager.getFindedPoints();
	}
	
	public IFindResult getFindResult() {				
		IFindResult findResult = fFinderManager.getFindedResult();
		return findResult;
	}
	
	@Override
	public List<Point> getFindedIdPoints() {
		return fFinderIdManager.getFindedIdPoints();
	}

	//========================
	// ISpellErrorPoints
	
	@Override
	public void findErrorWords(String text) {
		fSpellErrorPoints.findErrorWords(text);
	}
	
	@Override
	public List<Point> errorsPoints(){
		return fSpellErrorPoints.errorsPoints();
	}

	// ======================
	// getters & setters

	public String getText() {
		return fText;
	}

	public void setFormTitlePoint(int length) {
		fMainTitlePoint = new Point(0, length);
	}

	public Point titleBoldPoint() {
		return fMainTitlePoint;
	}

	public List<Point> codePoints() {
		return fMdFormatPoints.codePoints();
	}

	public List<Point> fencedCodePoints() {
		return fMdFormatPoints.fencedCodePoints();
	}

	public List<Point> commentPoints() {
		return fMdFormatPoints.commentPoints();
	}

	public List<Point> boldPoints() {
		return fMdFormatPoints.boldPoints();
	}

	public List<Point> italicPoints() {
		return fMdFormatPoints.italicPoints();
	}

	public List<Point> boldItalicPoints() {
		return fMdFormatPoints.boldItalicPoints();
	}
	
	public MdFormatPoints getMdFormatPoints() {
		return fMdFormatPoints;
	}
	
	@Override
	public IGlossaryPoints getGlossaryPoints() {
		return fGlossaryPoints;
	}
	
	protected FormPresentation getFormPresentation() {
		return (FormPresentation) getDipUnit().getUnitPresentation().getPresentation();
	}
	
	@Override
	public List<FormField> getFormFields() {
		return fFormFields;
	}

	@Override
	public FormShowProperties formShowProperties() {
		return getFormPresentation().formShowProperties();
	}

	@Override
	public List<Point> getFieldTitlePoints() {
		return fTitlePonts;
	}

	@Override
	public String getFormTitleString() {
		return getFormPresentation().getFormTitleString();
	}
	
	public String getSchemaName() {
		return getFormPresentation().schemaName();
	}


}
