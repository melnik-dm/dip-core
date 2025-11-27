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
package ru.dip.core.model.glossary;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.swt.graphics.Point;

import ru.dip.core.manager.DipNatureManager;
import ru.dip.core.model.DipElementType;
import ru.dip.core.model.DipProject;
import ru.dip.core.model.DipRoot;
import ru.dip.core.model.DipTableContainer;
import ru.dip.core.model.interfaces.IFindable;
import ru.dip.core.model.interfaces.IGlossarySupport;
import ru.dip.core.model.interfaces.IParent;
import ru.dip.core.model.interfaces.IDipElement;
import ru.dip.core.model.interfaces.IDipDocumentElement;
import ru.dip.core.utilities.ResourcesUtilities;
import ru.dip.core.utilities.text.Terms;

public class GlossaryFolder implements IParent {

	
	public static GlossaryFolder getFor(IFile file){
		if (!DipNatureManager.hasNature(file)){
			return null;			
		}
		DipProject dipProject = DipRoot.getInstance().getDipProject(file.getProject());
		return  dipProject.getGlossaryFolder();
	}
	
	public static final String GLOS_FILE = ".glos";
	public static final String GLOS_FOLDER = "Glossary";
	
	public static final Comparator<GlossaryField> comparator = Comparator.comparing(GlossaryField::name);
	

	private IFile fGlossaryFile;
	private Path fGlossaryPath; 
	private DipProject fDipProject;
	private List<IGlossaryListener> fListeners = new ArrayList<>();
	private List<GlossaryField> fFields = new ArrayList<>();
	private String fUppderCaseWordsRegex = null;
	private String fLowerCaseWordsRegex = null;
	
	public GlossaryFolder(IFile glossFile, DipProject project) {
		fGlossaryFile = glossFile;
		fDipProject = project;
		fGlossaryPath = Paths.get(fGlossaryFile.getLocation().toOSString());
		try {
			readGlossary();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	protected void readGlossary() throws IOException{
		List<String> lines = Files.readAllLines(fGlossaryPath, StandardCharsets.UTF_8);
		StringBuilder builder = new StringBuilder();
		for (String line: lines){
			if (line.trim().isEmpty()){
				if (builder.length() > 0){
					addField(builder.toString());	
					builder = new StringBuilder();
				}
			} else {
				builder.append(line);
			}
		}		
		if (builder.length() > 0){
			addField(builder.toString());
		}
		updateFindGlossRegex();
	}
	
	private void addField(String str){
		String[] lines = str.split("\\|");
		if (lines.length == 2){
			GlossaryField field = new GlossaryField(lines[0].trim(), lines[1].trim(), this);
			fFields.add(field);						
		} else if (lines.length > 2){
			int delemiter = str.indexOf('|');
			String value = str.substring(delemiter + 1);
			GlossaryField field = new GlossaryField(lines[0].trim(), value.trim(), this);
			fFields.add(field);
		}
	}

	public void addField(String name, String value) throws IOException{
		GlossaryField field = new GlossaryField(name, value, this);
		fFields.add(field);
		saveGlossary();
	}
	
	public void pasteFields(List<GlossaryField> addFields, List<GlossaryField> changeFields) throws IOException{
		fFields.addAll(addFields);
		for (GlossaryField field: changeFields){
			GlossaryField removeField = getChild(field.name());
			if (removeField != null){
				fFields.remove(removeField);
			}
			fFields.add(field);
		}
		saveGlossary();
	}
	
	protected List<GlossaryField> getFields() {
		return fFields;
	}
	
	public void saveGlossary() throws IOException{
		saveGlossary(fGlossaryPath);
	}
	
	public void saveGlossary(Path path) throws IOException {
		fFields.sort(comparator);
		saveFields(path, fFields);
		updateFindGlossRegex();
		ResourcesUtilities.updateProject(dipProject().resource());
		updateListeners();
	}
		
	public static void saveFields(Path path, List<GlossaryField> fields) throws IOException {
		ArrayList<String> lines = new ArrayList<>();
		for (GlossaryField field: fields){
			StringBuilder builder = new StringBuilder();
			builder.append(field.name());
			builder.append("|");
			builder.append(field.getValue());
			lines.add(builder.toString());
			lines.add("");
		}
		Files.write(path, lines, StandardCharsets.UTF_8);
	}
	
	public void addListener(IGlossaryListener listener) {
		fListeners.add(listener);
	}
	
	public void removeListener(IGlossaryListener listener) {
		fListeners.remove(listener);
	}
	
	private void updateListeners() {
		fListeners.forEach(IGlossaryListener::glossaryChanged);
	}

	public void deleteField(GlossaryField field) throws IOException{
		if (fFields.remove(field)){
			saveGlossary();
		}
	}
	
	public void deleteFields(IDipElement[] objects) throws IOException {
		for (IDipElement element: objects){
			if (element instanceof GlossaryField){
				fFields.remove(element);
			} else {
				throw new IOException();
			}
		}
		saveGlossary();
	}
	
	//===========================
	// check glossary
	
	public List<GlossaryField> findUnsedFields() {
		List<GlossaryField> fields = fDipProject.getGlossaryFolder().getChildren();		
		Map<String, GlossaryField> regs = new HashMap<>();
		for (GlossaryField field: fields) {
			String regex = Terms.createRegexForGlossEntry(field.name());
			regs.put(regex, field);			
		}		
		List<String> regList = new ArrayList<>(regs.keySet());
		findTerms(fDipProject, regList);
		return regList.stream().map(regs::get).collect(Collectors.toList());		
	}
	
	private void findTerms(DipTableContainer parent, List<String> terms) {		
		if (terms.isEmpty()) {
			return;
		}
		parent.removeIfFind(terms);
		for (IDipDocumentElement dipDocumentElement: parent.getDipChildren()) {
			if (terms.isEmpty()) {
				return;
			}
			if (dipDocumentElement instanceof DipTableContainer) {
				findTerms((DipTableContainer) dipDocumentElement, terms);
			} else if (dipDocumentElement instanceof IFindable) {
				((IGlossarySupport) dipDocumentElement).removeIfFind(terms);
			}
		}				
	}
	
	//============================
	// find abbreviations
	
	public Collection<String> findAbbreviations() {
		Set<String> terms = new HashSet<>();
		getAbrev(fDipProject, terms);						
		Collection<String>  glossTerms = getChildren()
				.stream()
				.map(GlossaryField::name)
				.collect(Collectors.toList());
		
		terms.removeAll(glossTerms);
		terms.removeIf(s -> s.length() > 6);
		terms.remove("ИЛИ");
		terms.remove("ЕСЛИ");
		return terms;
	}
	
	private void getAbrev(DipTableContainer parent, Set<String> terms) {
		for (IDipDocumentElement dipDocumentElement: parent.getDipChildren()) {
			if (dipDocumentElement instanceof DipTableContainer) {
				getAbrev((DipTableContainer) dipDocumentElement, terms);
			} else if (dipDocumentElement instanceof IGlossarySupport) {
				((IGlossarySupport) dipDocumentElement).findTerms(terms);
			}
		}
	}
	
	//============================
	// key word regex
	
	protected void updateFindGlossRegex() {
		if (fFields != null && !fFields.isEmpty()) {
			createRegex();
		} else {
			fUppderCaseWordsRegex = null;
			fLowerCaseWordsRegex = null;
		}
	}
	
	private void createRegex() {
		List<String> upperCaseWords = new ArrayList<>();
		List<String> lowerCaseWords = new ArrayList<>(); 
		
		for (GlossaryField field: getChildren()) {
			if (field.isUpperCase()) {
				upperCaseWords.add(field.name());
			} else {
				lowerCaseWords.add(field.name());
			}
		}

		fUppderCaseWordsRegex = createRegex(upperCaseWords);
		fLowerCaseWordsRegex = createRegex(lowerCaseWords);	
	}

	private String createRegex(Collection<String> words) {
		if (words.isEmpty()) {
			return null;
		}
		StringBuilder builder = new StringBuilder();
		builder.append("(^|\\W)(");
		if (words.size() == 1) {
			for (String word: words) {
				String wordRegex = Terms.createRegexForWord(word);
				builder.append(wordRegex);				
			}
		} else { 
			boolean first = true;
			for (String word: words) {
				word = word.replace("(", "\\(");
				word = word.replace(")", "\\)");
				if (!first) {
					builder.append("|");
				} else {
					first = false;
				}
				String wordRegex = Terms.createRegexForWord(word);
				builder.append(wordRegex);
			}
		}		
		builder.append(")(\\W|$)");
		return builder.toString();
	}
	
	//============================
	// search
	
	public List<Point> findKeyWords(String text){
		ArrayList<Point> result= new ArrayList<>();
		result.addAll(findUpperWords(text));
		result.addAll(findLowerWords(text));
		return result;
	}
	
	private List<Point>  findUpperWords(String text) {
		ArrayList<Point> result= new ArrayList<>();	
		if (fUppderCaseWordsRegex == null) {
			return result;
		}		
		Pattern pattern = Pattern.compile(fUppderCaseWordsRegex,  Pattern.UNICODE_CHARACTER_CLASS);
		Matcher matcher = pattern.matcher(text);
		int start = 0;
		while(matcher.find(start)) {
			int offset = matcher.start();
			int end = matcher.end();
			
			String findText = matcher.group();
			if (!Character.isLetter(findText.charAt(0))){
				offset ++;
			}
			if (!Character.isLetter(findText.charAt(findText.length()-1))){
				end--;
			}
			
			result.add(new Point(offset, end - offset));			
			start = end;
		}
		return result;
	}
	
	private List<Point>  findLowerWords(String text) {
		ArrayList<Point> result= new ArrayList<>();	
		if (fLowerCaseWordsRegex == null) {
			return result;
		}
		Pattern pattern = Pattern.compile(fLowerCaseWordsRegex, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CHARACTER_CLASS);
		Matcher matcher = pattern.matcher(text);
		int start = 0;
		while(matcher.find(start)) {
			int offset = matcher.start();
			int end = matcher.end();
			
			String findText = matcher.group();
			if (Character.isWhitespace(findText.charAt(0))){
				offset ++;
			}
			if (Character.isWhitespace(findText.charAt(findText.length()-1))){
				end--;
			}				
			result.add(new Point(offset, end - offset));
			start = end;
		}	
		return result;
	} 
	
	//============================
	// equals
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((fGlossaryPath == null) ? 0 : fGlossaryPath.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		GlossaryFolder other = (GlossaryFolder) obj;
		if (fGlossaryPath == null) {
			if (other.fGlossaryPath != null)
				return false;
		} else if (!fGlossaryPath.equals(other.fGlossaryPath))
			return false;
		return true;
	}
	
	//=============================
	// DipElement interface
	
	@Override
	public String name() {
		return GLOS_FOLDER;
	}

	@Override
	public DipElementType type() {
		return DipElementType.GLOSSARY_FOLDER;
	}

	public IFile getGlossaryFile(){
		return fGlossaryFile;
	}
	
	@Override
	public IResource resource() {
		return null;
	}

	@Override
	public void setResource(IResource resource) {
		
	}

	@Override
	public String id() {
		return null;
	}

	@Override
	public IParent parent() {
		return fDipProject;
	}

	@Override
	public void setParent(IParent parent) {
		
	}

	@Override
	public boolean hasParent(IParent parent) {
		return true;
	}

	@Override
	public DipProject dipProject() {
		return fDipProject;
	}
	
	@Override
	public List<GlossaryField> getChildren() {
		return fFields;
	}
	
	@Override
	public boolean hasChildren() {
		return !fFields.isEmpty();
	}

	@Override
	public GlossaryField getChild(String name) {
		for (GlossaryField field: getChildren()){
			if (field.isNameEquals(name)) {
				return field;
			}
		}
		return null;
	}

	@Override
	public void removeChild(IDipElement child) {
		
	}

	@Override
	public void refresh() {
		
	}

	//==================
	// not used functional IDipParent

	@Override
	public <T> T getAdapter(Class<T> adapter) {
		return null;
	}

	@Override
	public void updateWithProject() {
		
	}

	@Override
	public boolean isReadOnly() {
		return false;
	}

	@Override
	public void setReadOnly(boolean value) {
		
	}
	
	@Override
	public boolean isIncluded() {
		return false;
	}
	
	@Override
	public void setIncluded(boolean value) {

	}
	
	@Override
	public boolean canDelete() {
		return false;
	}
	
	@Override
	public boolean canRename() {
		return false;
	}

	@Override
	public void dispose() {}

}
