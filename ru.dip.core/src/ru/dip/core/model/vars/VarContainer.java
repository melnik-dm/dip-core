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
package ru.dip.core.model.vars;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;

import ru.dip.core.model.DipElementType;
import ru.dip.core.model.DipProject;
import ru.dip.core.model.interfaces.IDipElement;
import ru.dip.core.model.interfaces.IDipParent;
import ru.dip.core.model.interfaces.IParent;
import ru.dip.core.utilities.FileUtilities;
import ru.dip.core.utilities.ResourcesUtilities;

public class VarContainer implements IVarContainer, IParent {

	public static final String VAR_FILE = ".vars";
	public static final String VAR_FOLDER = "Variables";
	
	public static final Comparator<Variable> comparator = Comparator.comparing(Variable::name);
	
	private IFile fVarFile;
	private List<Variable> fVariables = new ArrayList<>();
	private List<IVarContainerListener> fListeners = new ArrayList<>();
	private IDipParent fDipParent;
	
	public VarContainer(IFile varFile, IDipParent dipParent) {		
		fVarFile = varFile;
		fDipParent = dipParent;
		try {
			readVariables();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public String name() {
		return VAR_FOLDER;
	}

	@Override
	public DipElementType type() {
		return DipElementType.VARIABLES_CONTAINER;
	}
	
	@Override
	public String getRelativePath() {
		return  fVarFile.getParent().getProjectRelativePath().toOSString();		
	}
	
	@Override
	public IDipParent getDipParent() {
		return fDipParent;
	}
	
	@Override
	public IParent parent() {
		return fDipParent;
	}
	
	@Override
	public DipProject dipProject() {
		return fDipParent.dipProject();
	}
	
	@Override
	public IResource resource() {
		return fVarFile;
	}

	//============================
	// read-write
	
	private void readVariables() throws IOException{		
		List<String> lines =FileUtilities.readLines(fVarFile);
		if (lines == null) {
			return;
		}
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
	}
	
	@Override
	public void saveVarContainer() throws IOException {		
		fVariables.sort(comparator);
		saveVariables();
		ResourcesUtilities.updateProject(dipProject().resource());
		updateListeners();
	}
	
	private void saveVariables() throws IOException {
		ArrayList<String> lines = new ArrayList<>();
		for (Variable field: fVariables){
			StringBuilder builder = new StringBuilder();
			builder.append(field.name());
			builder.append("=");
			builder.append(field.getValue());
			lines.add(builder.toString());
			lines.add("");
		}		
		Files.write(Paths.get(fVarFile.getLocation().toOSString()), lines, StandardCharsets.UTF_8);
	}
	
	//===========================
	// children
	
	private void addField(String str) {
		int delimiter = str.indexOf('=');
		if (delimiter > 0) {
			String name = str.substring(0, delimiter);
			String value = str.substring(delimiter + 1);
			Variable field = new Variable(name.trim(), value.trim(), this);
			fVariables.add(field);
		}
	}
	
	@Override
	public void addField(String name, String value) throws IOException{
		Variable field = new Variable(name, value, this);
		fVariables.add(field);
		saveVarContainer();
	}
	
	
	public void pasteVariables(List<Variable> addFields, List<Variable> changeFields) throws IOException{
		addFields.forEach(this::pasteVariable);
		for (Variable field: changeFields){
			Variable removeField = getChild(field.name());
			if (removeField != null){
				fVariables.remove(removeField);
			}
			pasteVariable(field);
		}
		saveVarContainer();
	}
	
	private void pasteVariable(Variable variable) {
		Variable field = new Variable(variable.name(), variable.getValue(), this);
		fVariables.add(field);
	}
	
	@Override
	public void deleteField(Variable field) throws IOException{
		if (fVariables.remove(field)){
			saveVarContainer();
		}
	}
	
	public void deleteVariables(IDipElement[] objects) throws IOException {
		for (IDipElement element: objects){
			if (element instanceof Variable){
				fVariables.remove(element);
			} else {
				throw new IOException();
			}
		}
		saveVarContainer();
	}
	
	@Override
	public boolean hasChildren() {
		return !fVariables.isEmpty();
	}
	
	@Override
	public Variable getChild(String name) {
		for (Object field: getChildren()){
			if (field instanceof Variable) {
				Variable var = (Variable) field;
				if (var.name().equals(name)) {
					return var;
				}
			}
		}
		return null;
	}
	
	@Override
	public List<Variable> getVariables(){
		return fVariables;
	}
	
	@Override
	public List<? extends IDipElement> getChildren() {
		return fVariables;
	}
	
	//============================
	// listeners
	
	public void addListener(IVarContainerListener listener) {
		fListeners.add(listener);
	}
	
	public void removeListener(IVarContainerListener listener) {
		fListeners.remove(listener);
	}
	
	private void updateListeners() {
		fListeners.forEach(IVarContainerListener::variablesChanged);
	}
	
	//===========================
	// can edit (include)
	
	@Override
	public boolean canDelete() {
		return !fDipParent.isReadOnly();
	}

	@Override
	public boolean canRename() {
		return fDipParent.canDelete();
	}

	@Override
	public boolean hasParent(IParent parent) {
		return true;
	}

	@Override
	public boolean isReadOnly() {
		return fDipParent.isReadOnly();
	}

	@Override
	public boolean isIncluded() {
		return fDipParent.isIncluded();
	}
	
	//============================
	// other
	
	@Override
	public String id() {
		return null;
	}

	@Override
	public void updateWithProject() {}
	
	@Override
	public void setParent(IParent parent) {}
	
	@Override
	public void setReadOnly(boolean value) {}
	
	@Override
	public void setIncluded(boolean value) {}

	@Override
	public <T> T getAdapter(Class<T> adapter) {
		return null;
	}

	@Override
	public void removeChild(IDipElement child) {}

	@Override
	public void refresh() {}

	@Override
	public void setResource(IResource resource) {}

	@Override
	public void dispose() {}
	
}
