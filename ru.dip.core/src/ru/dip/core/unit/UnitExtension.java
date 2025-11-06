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

import org.eclipse.core.resources.IResource;

import ru.dip.core.model.DipDescription;
import ru.dip.core.model.DipElementType;
import ru.dip.core.model.DipProject;
import ru.dip.core.model.interfaces.IFindable;
import ru.dip.core.model.interfaces.IGlossarySupport;
import ru.dip.core.model.interfaces.IParent;
import ru.dip.core.model.interfaces.IDipComment;
import ru.dip.core.model.interfaces.IDipParent;
import ru.dip.core.model.interfaces.IDipUnit;
import ru.dip.core.model.interfaces.IDipDocumentElement;
import ru.dip.core.model.interfaces.IUnitExtension;
import ru.dip.core.model.interfaces.IVariablesSupport;

public abstract class UnitExtension implements IUnitExtension, IDipDocumentElement, IFindable, IGlossarySupport, IVariablesSupport {
	
	private IDipUnit fUnit;

	public UnitExtension(IDipUnit unit) {
		fUnit = unit;
	}
	
	public IDipUnit getDipUnit(){
		return fUnit;
	}
	
	@Override
	public IDipDocumentElement strong() {
		return getDipUnit();
	}
	
	@Override
	public boolean isReadOnly() {
		return fUnit.isReadOnly();
	}
	
	@Override
	public void setReadOnly(boolean value) {
		fUnit.setReadOnly(value);
	}
	
	@Override
	public boolean isIncluded() {
		return fUnit.isIncluded();
	}
	
	@Override
	public void setIncluded(boolean value) {
		fUnit.setIncluded(value);
	}
	
	@Override
	public boolean canDelete() {
		return fUnit.canDelete();
	}
	
	@Override
	public boolean canRename() {
		return fUnit.canRename();
	}
	
	//===============
	// IDipDocElement
	
	@Override
	public String name() {
		return fUnit.name();
	}

	@Override
	public DipElementType type() {
		return DipElementType.UNDEFINE;
	}

	@Override
	public IResource resource() {
		return fUnit.resource();
	}

	@Override
	public IDipParent parent() {
		return fUnit.parent();
	}

	@Override
	public void setParent(IParent parent) {
		
	}

	@Override
	public boolean hasParent(IParent parent) {
		return fUnit.hasParent(parent);
	}

	@Override
	public String description() {
		return fUnit.description();
	}

	@Override
	public void setDescription(String description) {
		fUnit.setDescription(description);
	}
	
	@Override
	public DipDescription dipDescription() {
		return fUnit.dipDescription();
	}
	
	@Override
	public void setDipDescription(DipDescription description) {
		fUnit.setDipDescription(description);
	}
	
	@Override
	public void removeDescription() {
		fUnit.removeDescription();	
	}
	
	@Override
	public void updateDescription(String newDescriptionContent) {
		fUnit.updateDescription(newDescriptionContent);
	}
	
	@Override
	public IDipComment comment() {
		return fUnit.comment();
	}
	
	@Override
	public String getCommentContent() {
		return fUnit.getCommentContent();
	}
	
	@Override
	public void setDipComment(IDipComment comment) {
		fUnit.setDipComment(comment);
	}
	
	@Override
	public void updateDipComment(String newCommentContent) {
		fUnit.updateDipComment(newCommentContent);
	}

	@Override
	public void deleteDipComment() {
		fUnit.deleteDipComment();
	}
	

	@Override
	public <T> T getAdapter(Class<T> adapter) {
		return null;
	}

	@Override
	public String id() {
		return fUnit.id();
	}

	@Override
	public DipProject dipProject() {
		return fUnit.dipProject();
	}
	
	@Override
	public void setResource(IResource resource) {
		fUnit.setResource(resource);
	}
	
	@Override
	public void updateWithProject() {
		fUnit.updateWithProject();
	}

	@Override
	public boolean isDisabled() {
		return fUnit.isDisabled();
	}
	@Override
	public void setDisabled(boolean value) {
		fUnit.setDisabled(value);
	}
	
	@Override
	public boolean isDisabledInDocument() {
		return fUnit.isDisabledInDocument();
	}
}
