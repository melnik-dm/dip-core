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

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;

import ru.dip.core.model.glossary.GlossRef;
import ru.dip.core.model.interfaces.IParent;
import ru.dip.core.model.interfaces.IDipElement;
import ru.dip.core.model.interfaces.IDipUnit;
import ru.dip.core.model.interfaces.IDipDocumentElement;
import ru.dip.core.utilities.ResourcesUtilities;
import ru.dip.core.utilities.WorkbenchUtitlities;

public abstract class DipContainer extends DipElement implements IParent {

	protected List<IDipElement> fChildren;

	public DipContainer(IContainer container, IParent parent) {
		super(container, parent);
	}
	
	@Override
	public List<IDipElement> getChildren() {
		if (fChildren == null){
			computeChildren();
		}		
		return fChildren;
	}
	
	public abstract void computeChildren();
	
	@Override
	public boolean hasChildren() {
		return getChildren().size() > 0;
	}
	
	public IDipElement createReservedFolder(IFolder folder){
		DipReservedFolder reservedFolder = DipReservedFolder.instance(folder, this);
		fChildren.add(reservedFolder);
		return reservedFolder;
	}
	
	public IDipElement createFolder(IFolder folder){
		DipFolder dipFolder = DipFolder.instance(folder, this);
		fChildren.add(dipFolder);
		return dipFolder;
	}
	
	public IDipElement createIncludeFolder(IFolder folder) {
		if (!chechLinkFolder(folder)) {
			return null;
		}	
		IncludeFolder incFolder = IncludeFolder.instance(folder, this);
		fChildren.add(incFolder);
		dipProject().addIncludeFolder(incFolder);
		return incFolder;
	}
	
	private boolean chechLinkFolder(IFolder folder) {
		if (folder.isLinked()) {
			Path linkTarget = Paths.get(folder.getLocationURI());
			if (!Files.exists(linkTarget)) {
				try {
					ResourcesUtilities.deleteResource(folder,  WorkbenchUtitlities.getShell());
				} catch (CoreException e) {
					e.printStackTrace();
				}				
				return false;
			}
		}
		return true;
	}

	public IDipElement createReservedMarker(IFile file){
		DipReservedMarker marker = DipReservedMarker.instance(file, this);
		fChildren.add(marker);
		return marker;
	}
	
	public IDipElement createReservedUnit(IFile file){
		DipReservedUnit reservedUnit = DipReservedUnit.instance(file, this);
		fChildren.add(reservedUnit);
		return reservedUnit;
	}
	
	public IDipUnit createUnit(IFile file){
		DipUnit unit = DipUnit.instance(file, this);
		fChildren.add(unit);
		return unit;
	}
	
	public IDipElement createGlossRef(IFile file){
		GlossRef unit = GlossRef.instance(file, this);
		fChildren.add(unit);
		return unit;
	}
	
	public IDipElement createTocRef(IFile file){
		TocRef unit = TocRef.instance(file, this);
		fChildren.add(unit);
		return unit;
	}
	
	public IDipElement createChangeLog(IFile file){
		ChangeLog unit = ChangeLog.instance(file, this);
		fChildren.add(unit);
		return unit;
	}
	
	public IDipElement createDipComment(IFile file){
		DipComment comment = DipComment.createExistsDipComment(file, this);
		fChildren.add(comment);
		return comment;
	}
	
	public IDipElement createFolderDipComment(IFile file){
		DipFolderComment comment = DipFolderComment.createExistsDipComment(file, this);
		fChildren.add(comment);
		return comment;
	}
		
	public IDipElement createDipDescription(IFile file){
		DipDescription description = DipDescription.createExistsDipDescription(file, this);
		fChildren.add(description);
		return description;
	}
	
	@Override
	public IDipElement getChild(String name) {
		if (fChildren == null){
			computeChildren();
		}
		IDipElement noDipDocElement = null;  // есть проблема с Glossary, когда есть папка с таким же именем	
		for (IDipElement child: fChildren){			
			if (name.equals(child.name()) || name.equals(child.dipName())){
				if (child instanceof IDipDocumentElement) {
					return child;
				} else {
					noDipDocElement = child;
				}
			}
		}
		return noDipDocElement;
	}
	
	@Override
	public void removeChild(IDipElement child) {
		fChildren.remove(child);
	}
	
	@Override
	public void refresh() {		
		computeChildren();
	}
	
	@Override
	public IContainer resource() {
		return (IContainer) super.resource();
	}
	
}
