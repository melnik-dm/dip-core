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
package ru.dip.core.model;

import java.io.File;

import org.eclipse.core.resources.IResource;

import ru.dip.core.model.interfaces.IDisable;
import ru.dip.core.model.interfaces.IParent;
import ru.dip.core.model.interfaces.ICommentSupport;
import ru.dip.core.model.interfaces.IDescriptionSupport;
import ru.dip.core.model.interfaces.IDipComment;
import ru.dip.core.model.interfaces.IDipElement;
import ru.dip.core.utilities.DipUtilities;
import ru.dip.core.utilities.ResourcesUtilities;
import ru.dip.core.utilities.WorkbenchUtitlities;

public abstract class DipElement implements IDipElement, IDescriptionSupport, ICommentSupport, IDisable {

	private IResource fResource;
	private IParent fParent;
	private String fDescription;
	private IDipComment fDipComment;
	private DipDescription fDipDescription;
	private boolean fReadOnly = false;
	private boolean fIncluded = false;
	private boolean fDisabled = false;
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((fParent == null) ? 0 : fParent.hashCode());
		result = prime * result + ((fResource == null) ? 0 : fResource.hashCode());
		result = prime * result + type().ordinal();
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
		DipElement other = (DipElement) obj;
		if (fParent == null) {
			if (other.fParent != null)
				return false;
		} else if (!fParent.equals(other.fParent))
			return false;
		if (fResource == null) {
			if (other.fResource != null)
				return false;
		} else if (!fResource.equals(other.fResource))
			return false;
		if (type() != other.type())
			return false;
		return true;
	}

	public DipElement(IResource resource, IParent parent) {
		fResource = resource;
		fParent = parent;
		if (fParent != null) {
			setIncluded(fParent.isIncluded());
			setReadOnly(fParent.isReadOnly());
		}
	}
	
	@Override
	public <T> T getAdapter(Class<T> adapter) {
		if (this instanceof IncludeFolder) {
			return null;
		}				
		if (isReadOnly()) {
			return null;
		}
		return resource().getAdapter(adapter);		
	}

	@Override
	public String name() {
		if (resource() == null){
			return null;
		}		
		return resource().getName();
	}
	
	@Override
	public IResource resource(){
		return fResource;
	}
	
	@Override
	public void setResource(IResource resource) {
		fResource = resource;		
	}
	
	@Override
	public void updateWithProject() {
		ResourcesUtilities.updateDipElement(this);
		WorkbenchUtitlities.updateProjectExplorer();
		dipProject().refresh();
	}
	
	@Override
	public void dispose() {}
	
	@Override
	public String id() {
		if (DipUtilities.inIncludeFolder(this)) {
			return DipUtilities.includeElementFullID(this);
		}	
		StringBuilder builder = new StringBuilder();
		builder.append(dipProject().projectName());
		builder.append(File.separatorChar);
		builder.append(resource().getProjectRelativePath().toOSString());
		return builder.toString();
	}
	
	@Override
	public IParent parent() {
		return fParent;
	}
	
	@Override
	public void setParent(IParent parent) {
		fParent = parent;		
	}
	
	@Override
	public DipProject dipProject() {
		if (resource() == null) {
			if (parent() == null){
				return null;
			}									
			return parent().dipProject();
		}		
		return DipRoot.getInstance().getDipProject(resource().getProject());
	}
		
	@Override
	public boolean hasParent(IParent parent) {
		IParent currentParent = parent();
		while(currentParent != null){
			if (currentParent.equals(parent)){
				return true;
			}
			currentParent = currentParent.parent();
		}
		return false;
	}
	
	@Override
	public boolean isReadOnly() {	
		return fReadOnly;
	}
	
	@Override
	public void setReadOnly(boolean value) {
		fReadOnly = value;		
	}
	
	@Override
	public boolean isIncluded() {
		return fIncluded;
	}
	
	@Override
	public void setIncluded(boolean value) {
		fIncluded = value;	
	}
	
	@Override
	public boolean canDelete() {
		return !isReadOnly();
	}
	
	@Override
	public boolean canRename() {
		return !isReadOnly();
	}
	
	//======================
	// IDisable
	
	@Override
	public boolean isDisabled() {
		return fDisabled;
	}
	
	@Override
	public boolean isDisabledInDocument() {	
		if (isDisabled()) {
			return true;
		}
		IParent parent = parent();
		if (parent != null && parent instanceof IDisable) {
			return ((IDisable) parent).isDisabledInDocument();
		}
		return false;
	}
	
	@Override
	public void setDisabled(boolean value) {
		fDisabled = value;
	}
	
	//==============================
	// SimpleDescription 
	
	@Override
	public String description() {
		return fDescription;
	}

	@Override
	public void setDescription(String description) {
		fDescription = description;
	}
	
	//===============================
	// Description
	
	@Override
	public DipDescription dipDescription() {
		return fDipDescription;
	}
	
	@Override
	public void setDipDescription(DipDescription description) {
		fDipDescription = description;
	}
	
	@Override
	public void removeDescription() {
		
	}
	
	@Override
	public void updateDescription(String newDescriptionContent) {
		
	}
	
	//==============================
	// Comment
	
	@Override
	public IDipComment comment() {
		return fDipComment;
	}
	
	@Override
	public String getCommentContent() {
		IDipComment dipComment = comment();
		if (dipComment != null) {
			return dipComment.getCommentContent();
		}
		return null;
	}
	
	
	@Override
	public void setDipComment(IDipComment comment) {
		fDipComment = comment;
	}
	
	@Override
	public void updateDipComment(String newCommentContent) {
		
	}

	@Override
	public void deleteDipComment() {
		
	}
		
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(name());
		builder.append("[");
		builder.append(getClass().getSimpleName());
		builder.append("]");		
		builder.append(" ");
		builder.append(fResource);
		return builder.toString();
	}
}
