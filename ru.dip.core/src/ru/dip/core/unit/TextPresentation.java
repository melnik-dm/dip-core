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

import java.io.IOException;
import java.nio.file.NoSuchFileException;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;

import ru.dip.core.DipCorePlugin;
import ru.dip.core.link.LinkInteractor;
import ru.dip.core.model.DipProject;
import ru.dip.core.model.finder.FindSettings;
import ru.dip.core.model.finder.TextFinderManager;
import ru.dip.core.model.interfaces.IDipUnit;
import ru.dip.core.model.interfaces.IGlossaryPoints;
import ru.dip.core.model.interfaces.ITextPresentation;
import ru.dip.core.model.interfaces.IVariablesSupport;
import ru.dip.core.model.vars.VariableInteractor;
import ru.dip.core.utilities.FileUtilities;
import ru.dip.core.utilities.TagStringUtilities;
import ru.dip.core.utilities.text.Terms;

public class TextPresentation extends TablePresentation implements IVariablesSupport, ITextPresentation {
	
	/**
	 * Подготовка текста:
	 * 1. Заменить ссылки
	 * 2. Применить переменные
	 * @param content
	 * @param dde
	 * @return
	 */
	public static String prepareText(String content, IDipUnit dde) {
		if (content == null || content.isEmpty()) {
			return content;
		}
		content = LinkInteractor.instance().changeLinks(content, dde);
		content = VariableInteractor.changeVar(content, dde);
		return content;
	}
	
	public static String prepareTextWithoutUnit(String content, DipProject dde) {
		if (content == null || content.isEmpty()) {
			return content;
		}
		content = LinkInteractor.instance().changeLinks(dde, content);
		content = VariableInteractor.changeVar(content, dde);
		return content;
	}
	
	public static String changeLinksForRender(String content, IDipUnit dde) {
		if (content == null || content.isEmpty()) {
			return content;
		}
		content = LinkInteractor.instance().changeLinksForRender(content, dde);
		content = VariableInteractor.changeVar(content, dde);
		return content;
	}
	
	
	protected String fText;
	private final GlossaryPoints fGlossaryPoints;
	private final TextFinderManager fFinderManager;
	
	public TextPresentation(IDipUnit unit) {
		super(unit);
		fGlossaryPoints = new GlossaryPoints(unit.dipProject());
		fFinderManager = new TextFinderManager(this);
		read();
	}
	
	@Override
	public void read() {
		try {
			fText = FileUtilities.readFile(getUnit().resource());
			fText = prepareText(fText, getUnit());
		} catch (IOException e) {
			if (e instanceof NoSuchFileException) {
				DipCorePlugin.logError(e, "Read textfile error");
				e.printStackTrace();
				getUnit().updateWithProject();
				fText =  "Read Text error";
			}
			e.printStackTrace();
		}
	}
	
	//========================
	// find
	
	@Override
	public String getContent() {
		String content = fText;
		return prepareText(content, getUnit());
	}
	
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

	
	@Override
	public void updateFindedPoints(String content) {
		fFinderManager.updateFindedPoints(content);
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
	}

	@Override
	public boolean hasFindResult() {
		return fFinderManager.hasFindResult();
	}

	@Override
	public List<Point> getFindedPoints() {
		return fFinderManager.getFindedPoints();
	}
	
	//=========================
	// glossary
	
	@Override
	public void removeIfFind(Collection<String> terms) {
		TagStringUtilities.removeIfContains(fText, terms);
	}
	
	@Override
	public void findTerms(Set<String> terms) {
		Terms.findTerms(fText, terms);
	}
	
	@Override
	public IGlossaryPoints getGlossaryPoints() {
		return fGlossaryPoints;
	}

	
	//======================
	// variables
	
	@Override
	public void findVars(Set<String> vars) {
		Terms.findVarsNames(fText, vars);
	}
	
	//======================
	// getters
	
	@Override
	public String getText() {
		return fText;
	}

	@Override
	public Image getImage() {
		return null;
	}


}
