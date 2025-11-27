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

import org.eclipse.core.resources.IResource;

import ru.dip.core.model.DipElementType;
import ru.dip.core.model.DipProject;
import ru.dip.core.model.interfaces.IParent;
import ru.dip.core.model.interfaces.IDipElement;
import ru.dip.core.utilities.text.TermRegexBuilder;

public class GlossaryField implements IDipElement {
	
	public static final int NAME_MAX_LENGTH = 60;
	
	private String fName;
	private String fValue;
	private GlossaryFolder fGlossFolder;
	
	public GlossaryField(String name, String value, GlossaryFolder folder) {
		fName = name.trim();		
		fValue = value;
		fGlossFolder = folder;
	}

	public String getValue(){
		return fValue;
	}
	
	public void setNewValue(String newName, String newValue) {
		fName = newName.trim();
		fValue = newValue;
	}
	
	/** 
	 * Сверяет имя с учетом падежей
	 */
	public boolean isNameEquals(String text) {
		String[] nameWords = fName.split(" ");
		String[] textWords = text.split(" ");
		if (nameWords.length == textWords.length) {
			for (int i = 0; i < nameWords.length; i++) {
				if (!TermRegexBuilder.isOneWordEquals(nameWords[i], textWords[i])) {
					return false;
				}
			}
			return true;			
		}
		return false;
	}
	
	@Override
	public <T> T getAdapter(Class<T> adapter) {
		return null;
	}

	@Override
	public String name() {
		return fName;
	}

	@Override
	public DipElementType type() {
		return DipElementType.GLOSSARY_FIELD;
	}

	@Override
	public IResource resource() {
		return null;
	}

	@Override
	public void setResource(IResource resource) {}
	
	public boolean isUpperCase() {
		return fName.equals(fName.toUpperCase());
	}

	@Override
	public String id() {
		return fName;
	}

	@Override
	public GlossaryFolder parent() {
		return fGlossFolder;
	}

	@Override
	public void setParent(IParent parent) {}

	@Override
	public boolean hasParent(IParent parent) {
		return true;
	}

	@Override
	public DipProject dipProject() {
		return fGlossFolder.dipProject();
	}

	@Override
	public String toString() {
		return fName;
	}

	@Override
	public void updateWithProject() {}

	@Override
	public boolean isReadOnly() {
		return false;
	}

	@Override
	public void setReadOnly(boolean value) {}
	
	@Override
	public boolean isIncluded() {
		return false;
	}
	
	@Override
	public void setIncluded(boolean value) {}
	
	@Override
	public boolean canDelete() {
		return false;
	}
	
	@Override
	public boolean canRename() {
		return false;
	}

	@Override
	public void dispose() {		
	}

}
