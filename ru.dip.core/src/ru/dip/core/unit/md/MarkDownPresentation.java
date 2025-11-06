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
package ru.dip.core.unit.md;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;

import ru.dip.core.model.finder.FindSettings;
import ru.dip.core.model.finder.TextFinderManager;
import ru.dip.core.model.interfaces.IDipUnit;
import ru.dip.core.model.interfaces.IGlossaryPoints;
import ru.dip.core.model.interfaces.ITextPresentation;
import ru.dip.core.unit.GlossaryPoints;
import ru.dip.core.unit.TablePresentation;
import ru.dip.core.utilities.FileUtilities;
import ru.dip.core.utilities.TagStringUtilities;
import ru.dip.core.utilities.md.parser.MdPresentationParser;
import ru.dip.core.utilities.text.Terms;

public class MarkDownPresentation extends TablePresentation implements ITextPresentation {

	private String fText;
	private MdPresentationParser fParser;
	private final IGlossaryPoints fGlossaryPoints;
	private final TextFinderManager fFinderManager;
	
	public MarkDownPresentation(IDipUnit unit) {
		super(unit);
		fGlossaryPoints = new GlossaryPoints(unit.dipProject());
		fFinderManager = new TextFinderManager(this);
	}

	@Override
	protected void read() {
		fText = "";
	}
		
	public String parse(MarkdownSettings settings, int lineLength) {
		fParser = new MdPresentationParser(getUnit());
		return fParser.parse(settings, lineLength, getResource());
	}
	
	//=============================
	// find
	
	@Override
	public String getContent() {
		String content =  parse(MarkdownSettings.instanceForFastSearch(), -1);
		// надо проверить и в таблицах тоже
		// return LinkInteractor.instance().changeLinks(content, getUnit());
		return content;
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
	
	//=============================
	// glossary
	
	@Override
	public void removeIfFind(Collection<String> terms) {
		try {
			String filetext = FileUtilities.readFile(getUnit().resource());			
			TagStringUtilities.removeIfContains(filetext, terms);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void findTerms(Set<String> terms) {
		try {
			String filetext = FileUtilities.readFile(getUnit().resource());			
			Terms.findTerms(filetext, terms);
		} catch (IOException e) {
			e.printStackTrace();
		}		
	}
	
	//=============================
	// vars
	
	@Override
	public void findVars(Set<String> vars) {
		try {
			String filetext = FileUtilities.readFile(getUnit().resource());			
			Terms.findVarsNames(filetext, vars);
		} catch (IOException e) {
			e.printStackTrace();
		}		
	}
	
	//============================
	// getters
	
	public List<Point> codePoints(){
		return fParser.codePoints();
	}
	
	public List<Point> fencedCodePoints(){
		return fParser.fencedCodePoints();
	}
	
	public List<Point> commentPoints(){
		return fParser.commentPoints();
	}
	
	public List<Point> boldPoints(){
		return fParser.boldPoints();
	}
	
	public List<Point> italicPoints(){
		return fParser.italicPoints();
	}
	
	public List<Point> boldItalicPoints(){
		return fParser.boldItalicPoints();
	}
	
	@Override
	public String getText() {
		return fText;
	}

	@Override
	public Image getImage() {
		return null;
	}

	@Override
	public IGlossaryPoints getGlossaryPoints() {
		return fGlossaryPoints;
	}

}
