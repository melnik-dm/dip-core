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
package ru.dip.ui.export.diff.model;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

import ru.dip.core.unit.UnitType;
import ru.dip.core.utilities.FileUtilities;

public class UnitRevisionEntry {
	
	private DipRevisionProject fDipRevisionProject;
	private Path fRelativeDipPath;
	private Path fRelativeRepoPath;
	private UnitType fUnitType;
	
	public UnitRevisionEntry(DipRevisionProject revisionProject, Path relativeRepoPath, Path relativeDipPath) {
		fDipRevisionProject = revisionProject;
		fRelativeRepoPath = relativeRepoPath;
		fRelativeDipPath = relativeDipPath;
	}
	
	public InputStream getSchemaStream() throws IOException {
		return fDipRevisionProject.getSchemaStream(getFileExtension());
	}
	
	public String getFileExtension() {
		Path repoPath = getRepoPath();
		Path fileNamePath = repoPath.getFileName();			
		return FileUtilities.getFileExtension(fileNamePath);
	}
	
	public String getFileName() {
		Path repoPath = getRepoPath();
		Path fileNamePath = repoPath.getFileName();		
		return fileNamePath.toString();
	}
	
	public String getContent() throws IOException {
		return fDipRevisionProject.getContent(fRelativeRepoPath);
	}
	
	public InputStream getContentStream() throws IOException {
		return fDipRevisionProject.getInputStream(fRelativeRepoPath);
	}
	
	public String getId() {
		return getDipPath().toString();
	}
	
	public void setType(UnitType type) {
		fUnitType = type;
	}
		
	public UnitType getType() {
		return fUnitType;
	}
	
	public Path getDipPath() {
		return fRelativeDipPath;
	}
	
	public Path getRepoPath() {
		return fRelativeRepoPath;
	}
		
	@Override
	public String toString() {
		return fRelativeDipPath + "  "  + fUnitType;
	}

}
