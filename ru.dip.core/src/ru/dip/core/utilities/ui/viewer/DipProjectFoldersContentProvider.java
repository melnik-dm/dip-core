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
package ru.dip.core.utilities.ui.viewer;

import org.eclipse.jface.viewers.ITreeContentProvider;

import ru.dip.core.model.interfaces.IDipParent;
import ru.dip.core.model.interfaces.IDipDocumentElement;

public enum DipProjectFoldersContentProvider implements ITreeContentProvider {
	
	INSTANCE;
	
	
	/**
	 * Если передать проект в Input, то метод getChildren не сработает
	 * Видимо getChildren не вызывается если элемент == Input
	 * Для проекта следует использовать ParentHolder 
	 */
	public static class ParentHolder {
		
		private final IDipParent fParent;
		
		public ParentHolder(IDipParent parent) {
			fParent = parent;
		}

		public IDipParent getParent() {
			return fParent;
		}		
	}
	

	@Override
	public Object[] getElements(Object inputElement) {
		if (inputElement instanceof IDipParent) {
			IDipParent dipParent = (IDipParent) inputElement;
			return dipParent.getDipDocChildrenList().stream()
				.filter(IDipParent.class::isInstance)
				.toArray();
		} else if (inputElement instanceof ParentHolder) {
			return new IDipParent[] {((ParentHolder)inputElement).fParent};
		}
		return null;
	}

	@Override
	public Object[] getChildren(Object parentElement) {
		if (parentElement instanceof IDipParent) {
			IDipParent dipParent = (IDipParent) parentElement;
			return dipParent.getDipDocChildrenList().stream()
				.filter(IDipParent.class::isInstance)
				.toArray();				
		}		
		return null;
	}

	@Override
	public Object getParent(Object element) {
		if (element instanceof IDipDocumentElement) {
			return ((IDipDocumentElement) element).parent();
		}
		return null;
	}

	@Override
	public boolean hasChildren(Object element) {
		if (element instanceof IDipParent) {
			IDipParent dipParent = (IDipParent) element;
			return dipParent.getDipDocChildrenList().stream()
				.filter(IDipParent.class::isInstance)
				.count() > 0;				
		}		
		return false;
	}

}
