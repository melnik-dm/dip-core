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
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.xml.sax.SAXException;

import ru.dip.core.form.model.CoreFormModel;
import ru.dip.core.model.DnfoTable;
import ru.dip.core.model.interfaces.ISchemaContainer;
import ru.dip.core.schema.SchemaReader;
import ru.dip.core.schema.Schema;
import ru.dip.core.unit.UnitType;
import ru.dip.core.utilities.FileUtilities;
import ru.dip.core.utilities.GITUtilities;
import ru.dip.core.utilities.TagStringUtilities;

public class DipRevisionProject implements ISchemaContainer {
	
	private final Repository fRepo;
	private final RevCommit fCommit;
	private final Path fProjectPath;
	
	private Map<String, Schema> fSchemaByExtension = new HashMap<>();
	
	/**
	 * projectRelativePath - путь DIP проекта относительно репозитория
	 */
	public DipRevisionProject(Repository repo, RevCommit revCommit, Path projectRelativePath) {
		fRepo = repo;
		fCommit = revCommit;
		fProjectPath = projectRelativePath;
	}
	
	public UnitType getType(UnitRevisionEntry entry) {
		Path repoPath = entry.getRepoPath();
				
		Path parent = repoPath.getParent();
		if (isDnfoFolder(parent)){
			Path fileNamePath = repoPath.getFileName();			
			String extension = FileUtilities.getFileExtension(fileNamePath);
			String fileName = fileNamePath.toString();
			return UnitType.defineUnitType(extension, fileName, this);			
		} else {
			return null;
		}
	}
	
	private boolean isProjectRoot(Path path) {
		if (path == null) {
			return true;
		}
		if (path.equals(fProjectPath)) {
			return true;
		}		
		return false;
	}
	
	private boolean isDnfoFolder(Path path) {
		if (isProjectRoot(path)) {
			return true;
		}
		Path dnfoPath = path.resolve(DnfoTable.TABLE_FILE_NAME);
		return containsFile(dnfoPath);		
	}

	private boolean containsFile(Path path) {
		String unixPath = TagStringUtilities.toUnixPath(path.toString());
		try (TreeWalk treeWalk = TreeWalk.forPath(fRepo, unixPath, fCommit.getTree())) {
			return treeWalk != null;
		} catch (IOException e) {
			return false;
		}
	}
	
	//=============================
	// get content
	
	public String getContent(Path path) throws IOException {
		String unixPath = TagStringUtilities.toUnixPath(path.toString());
		return GITUtilities.getContent(fRepo, fCommit, unixPath);
	}
	
	public InputStream getInputStream(Path path) throws IOException {
		String unixPath = TagStringUtilities.toUnixPath(path.toString());
		return GITUtilities.getInputStream(fRepo, fCommit, unixPath);
	}

	//==========================
	// schema
	
	@Override
	public boolean containsSchema(String extension) {
		if (extension == null) {
			return false;
		}		
		Path path = getSchemaPath(extension);				
		return containsFile(path);
	}
	
	@Override
	public Schema getSchema(String extension) {
		Schema schema = fSchemaByExtension.get(extension);
		if (schema != null) {
			return schema;
		}
		
		try {
			CoreFormModel formModel = new CoreFormModel();
			InputStream schemaStream = getSchemaStream(extension);
			SchemaReader schemaReader = new SchemaReader(formModel, schemaStream);
			formModel.setFields(schemaReader.getFields());			
			schema = Schema.getInstance(extension, formModel.getName(), formModel);
			fSchemaByExtension.put(extension, schema);
			return schema;			
		} catch (ParserConfigurationException | SAXException | IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public InputStream getSchemaStream(String extension) throws IOException {
		Path path = getSchemaPath(extension);				
		return getInputStream(path);
	}
	
	private Path getSchemaPath(String extension) {
		return fProjectPath.resolve("schema").resolve(extension + ".xml");
	}

}
