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
package ru.dip.ui.table.editor;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IPersistableElement;

import ru.dip.core.model.DnfoTable;
import ru.dip.core.table.IContainerEditorInput;

public class TableEditorInput extends PlatformObject  implements IEditorInput, IPersistableElement, IContainerEditorInput {

	private IContainer fContainer;
	private DnfoTable fTable;
	
	public TableEditorInput(DnfoTable table){
		fContainer = (IContainer) table.parent().resource();
		fTable = table;
	}
	
	@Override
	public <T> T getAdapter(Class<T> adapter) {
		return fContainer.getAdapter(adapter);
	}

	@Override
	public boolean exists() {
		return fContainer.exists();
	}

	@Override
	public ImageDescriptor getImageDescriptor() {
		return null;
	}

	@Override
	public String getName() {
		return fContainer.getName();
	}

	@Override
	public IPersistableElement getPersistable() {
		return this;
	}

	@Override
	public String getToolTipText() {
		return null;
	}

	@Override
	public void saveState(IMemento memento) {
		if (!fTable.resource().getParent().equals(fContainer)) {
			fContainer = fTable.resource().getParent();
		}
		TableEditorFactory.saveState(memento, this);
	}

	@Override
	public String getFactoryId() {
		return TableEditorFactory.ID_FACTORY;
	}
	
	@Override
	public IContainer getContainer(){
		return fContainer;
	}
	
	public void setContainer(IContainer container) {
		fContainer = container;
	}
	
	public DnfoTable table() {
		return fTable;
	}
	
	//=====================================
	// equals  (чтобы не открывал еще один редактор при уже открытом)
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((fContainer == null) ? 0 : fContainer.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {		
		if (this == obj) {
			return true;
		}
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TableEditorInput other = (TableEditorInput) obj;		
		if (fContainer == null || other.fContainer == null) {
			return false;
		}		
		IPath path = fContainer.getLocation();
		IPath otherPath = other.fContainer.getLocation();		
		if (path == null  || otherPath == null) {
			return false;
		}	
		return path.toOSString().equals(otherPath.toOSString());
	}

}
