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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;

import ru.dip.core.DipCorePlugin;
import ru.dip.core.model.interfaces.IParent;
import ru.dip.core.model.interfaces.IDipElement;
import ru.dip.core.schema.Schema;
import ru.dip.core.utilities.TagStringUtilities;
import ru.dip.core.utilities.WorkbenchUtitlities;

public class DipSchemaFolder extends DipContainer {

	public static final String SCHEMA_FOLDER_NAME = "schema";
	// for copy default schema (creating project)
	public static final String DEFAULT_SCHEMA_PATH = "project_content" + TagStringUtilities.PATH_SEPARATOR + "req.xml";
	public static final String DEFAULT_SCHEMA_FILE_NAME = "req.xml";
	public static final String DEFAULT_SCHEMA_NAME = "Requirement";

	public static DipSchemaFolder instance(IFolder container, IParent parent) {
		IDipElement element = DipRoot.getInstance().getElement(container, parent, DipElementType.SCHEMA_FOLDER);
		if (element == null) {
			DipSchemaFolder schemaFolder = new DipSchemaFolder(container, parent);
			DipRoot.getInstance().putElement(schemaFolder);
			return schemaFolder;
		} else {
			return (DipSchemaFolder) element;
		}
	}
	
	private DipSchemaFolder(IFolder folder, IParent parent) {
		super(folder, parent);
	}

	public void computeChildren() {
		fChildren = new ArrayList<>();
		try {
			for (IResource resource : resource().members()) {
				if (resource instanceof IFile) {
					DipSchemaElement schema = DipSchemaElement.instance(resource, this);
					fChildren.add(schema);
				}
			}
		} catch (CoreException e) {
			e.printStackTrace();
			WorkbenchUtitlities.openError("Read schema", "Ошибка получения схемы " + parent());
			DipCorePlugin.logError(e, "Ошибка получения схемы");
		}
	}

	@Override
	public IFolder resource() {
		return (IFolder) super.resource();
	}
	
	@Override
	public DipElementType type() {
		return DipElementType.SCHEMA_FOLDER;
	}

	public void createFolderIfNotExist() {
		Path folderPath = Paths.get(resource().getLocation().toOSString());
		if (!Files.exists(folderPath)) {
			try {
				Files.createDirectory(folderPath);
				refresh();
			} catch (IOException e) {
				e.printStackTrace();
				WorkbenchUtitlities.openError("Schema folder error", "Ошибка создания директории Schema");
				DipCorePlugin.logError(e, "Ошибка создания директории Schema");
				return;
			}
		}
	}

	public DipSchemaElement createSchema(IFile file) {
		DipSchemaElement formSchema = DipSchemaElement.instance(file, this);
		fChildren.add(formSchema);
		fChildren.sort(Comparator.comparing(IDipElement::name));		
		dipProject().getSchemaModel().addSchema(formSchema);
		return formSchema;
	}
	
	/**
	 * Найти схему
	 */
	public DipSchemaElement findFormSchema(Schema schema) {
		String name = schema.getFileName();
		for (IDipElement element : fChildren) {
			if (element instanceof DipSchemaElement && element.name().equals(name)) {
				return (DipSchemaElement) element;
			}
		}
		return null;
	}
	
	/**
	 * Список всех расширений для которых есть схемы
	 * @return
	 */
	public List<String> getAllExtensions(){
		return fChildren.stream()
				.filter(DipSchemaElement.class::isInstance)
				.map(DipSchemaElement.class::cast)
				.map(DipSchemaElement::getFormExtension)
				.collect(Collectors.toList());
	}

}
