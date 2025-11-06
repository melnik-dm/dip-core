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
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import java.util.stream.Collectors;

import org.eclipse.core.resources.IFile;

import ru.dip.core.model.DipProject;

/**
 *  Объект глоссария для проекта
 *  Содержит сет с глоссариями из других папок
 *  getChildren - общий список GlossaryFields
 */
public class ProjectGlossaryFolder extends GlossaryFolder {

	private Set<GlossaryFolder> fFolders = new HashSet<>();

	public ProjectGlossaryFolder(IFile glossFile, DipProject project) {
		super(glossFile,project);
		fFolders.add(this);
		updateFindGlossRegex();
	}
	
	@Override
	protected void readGlossary() throws IOException {
		super.readGlossary();
	}
	
	public void addFolder(GlossaryFolder glossFolder) {
		fFolders.add(glossFolder);
		updateFindGlossRegex();
	}
	
	public void removeFolder(GlossaryFolder glossFolder) {
		fFolders.remove(glossFolder);
		updateFindGlossRegex();
	}
	
	@Override
	public void deleteField(GlossaryField field) throws IOException {
		Optional<GlossaryFolder> folderOpt = findGlossaryFolder(field);
		if (folderOpt.isPresent()) {
			GlossaryFolder folder = folderOpt.get();
			if (folder == this) {
				super.deleteField(field);
			} else {
				folder.deleteField(field);
			}						
		}		
	}
	
	private Optional<GlossaryFolder> findGlossaryFolder(GlossaryField field) {
		return fFolders.stream()
				.filter(folder -> getFields().contains(field))
				.findAny();		
	}
	
	@Override
	public List<GlossaryField> getChildren() {
		if (fFolders == null) {
			return getFields();
		}
		return fFolders.stream()
				.flatMap(f -> f.getFields().stream())
				.collect(Collectors.toList());		
	}

}
