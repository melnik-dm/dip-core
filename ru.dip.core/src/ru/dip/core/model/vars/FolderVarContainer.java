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
import java.util.List;

import org.eclipse.core.resources.IResource;

import ru.dip.core.model.DipElementType;
import ru.dip.core.model.DipProject;
import ru.dip.core.model.interfaces.IDipElement;
import ru.dip.core.model.interfaces.IDipParent;
import ru.dip.core.model.interfaces.IParent;

/**
 * Обвертка на VarContainer Используется для отображения внутри папок
 */
public class FolderVarContainer implements IVarContainer, IDipElement {

	private final VarContainer fVarContainer;

	public FolderVarContainer(VarContainer container) {
		fVarContainer = container;
	}

	public VarContainer getOriginalVarContainer() {
		return fVarContainer;
	}
	
	@Override
	public void saveVarContainer() throws IOException {
		fVarContainer.saveVarContainer();
	}

	@Override
	public void deleteVariables(IDipElement[] objects) throws IOException {
		fVarContainer.deleteVariables(objects);
	}

	@Override
	public IDipElement getChild(String name) {
		return fVarContainer.getChild(name);
	}

	@Override
	public void addField(String name, String value) throws IOException {
		fVarContainer.addField(name, value);
	}

	@Override
	public void deleteField(Variable field) throws IOException {
		fVarContainer.deleteField(field);
	}

	@Override
	public String getRelativePath() {
		return fVarContainer.getRelativePath();
	}

	@Override
	public List<Variable> getVariables() {
		return fVarContainer.getVariables();
	}

	@Override
	public IDipParent getDipParent() {
		return fVarContainer.getDipParent();
	}

	@Override
	public <T> T getAdapter(Class<T> adapter) {
		return fVarContainer.getAdapter(adapter);
	}

	@Override
	public DipProject dipProject() {
		return fVarContainer.dipProject();
	}

	@Override
	public String name() {
		return fVarContainer.name();
	}

	@Override
	public DipElementType type() {
		return fVarContainer.type();
	}

	@Override
	public IResource resource() {
		return fVarContainer.resource();
	}

	@Override
	public void setResource(IResource resource) {
		fVarContainer.setResource(resource);
	}

	@Override
	public String id() {
		return fVarContainer.id();
	}

	@Override
	public void updateWithProject() {
		fVarContainer.updateWithProject();
	}

	@Override
	public boolean canDelete() {
		return fVarContainer.canDelete();
	}

	@Override
	public boolean canRename() {
		return fVarContainer.canRename();
	}

	@Override
	public IParent parent() {
		return fVarContainer;
	}

	@Override
	public void setParent(IParent parent) {
		fVarContainer.setParent(parent);
	}

	@Override
	public boolean hasParent(IParent parent) {
		return fVarContainer.hasParent(parent);
	}

	@Override
	public boolean isReadOnly() {
		return fVarContainer.isReadOnly();
	}

	@Override
	public void setReadOnly(boolean value) {
		fVarContainer.setReadOnly(value);
	}

	@Override
	public boolean isIncluded() {
		return fVarContainer.isIncluded();
	}

	@Override
	public void setIncluded(boolean value) {
		fVarContainer.setIncluded(value);
	}

	@Override
	public List<? extends IDipElement> getChildren() {
		return fVarContainer.getChildren();
	}

	@Override
	public boolean hasChildren() {
		return fVarContainer.hasChildren();
	}

	@Override
	public void removeChild(IDipElement child) {
		fVarContainer.removeChild(child);
	}

	@Override
	public void refresh() {
		fVarContainer.refresh();
	}

	@Override
	public void dispose() {}

}
