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
package ru.dip.core.utilities;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.core.resources.IFile;
import org.eclipse.ui.IFileEditorMapping;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.registry.EditorDescriptor;
import org.eclipse.ui.internal.registry.EditorRegistry;
import org.eclipse.ui.internal.registry.FileEditorMapping;
import org.xml.sax.SAXException;

import ru.dip.core.DipCorePlugin;
import ru.dip.core.exception.DIPException;
import ru.dip.core.exception.ReadFileException;
import ru.dip.core.exception.SchemaReadException;
import ru.dip.core.exception.WritteFileException;
import ru.dip.core.model.DipProject;
import ru.dip.core.model.DipProjectSchemaModel;
import ru.dip.core.model.DipSchemaFolder;
import ru.dip.core.schema.Schema;
import ru.dip.core.schema.SimpleSchemaReader;

public class SchemaUtilities {
	
	private static final String EMPTY_SCHEMA = "<schema>\n</schema>";
	
	public static void writeNewScema(IFile file) throws IOException {
		FileUtilities.writeFile(file, EMPTY_SCHEMA);
	}
	
	public static void writeSchemaAttributes(String name, Path path) throws ReadFileException, SchemaReadException, WritteFileException{
		String schemaContent = null;
		try {
			schemaContent = FileUtilities.readFile(path);
		} catch (IOException e) {
			e.printStackTrace();
			throw new ReadFileException(e, "Ошибка чтения схемы " + path);
		}
		String regex = "<schema[^>]*>";
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(schemaContent);
		if (matcher.find()) {
			String tag = matcher.group();
			tag = setNullExtensionAttribute(tag);
			tag = setNameAttribute(tag, name);
			String result = matcher.replaceFirst(tag);
			try {
				FileUtilities.writeFile(path, result);
			} catch (IOException e) {
				e.printStackTrace();
				throw new WritteFileException(e, "Ошибка записи в файл " + path);
			}
		} else {
			throw new SchemaReadException("Не найден тег <schema>");
		}
	}
		
	private static String setNameAttribute(String schematag, String name){
		String regex = "name\\s*=\\s*\"[^\"]*\"";		
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(schematag);
		if (matcher.find()){
			String value = "name=\"" + name + "\"";
			String newTag = matcher.replaceFirst(value);
			return newTag;
		} else {
			StringBuilder newTag = new StringBuilder("<schema name=\"");
			newTag.append(name);
			newTag.append("\"");
			newTag.append(schematag.substring(7));
			return newTag.toString();
		}
	}
	
	private static String setNullExtensionAttribute(String schematag){
		String regex = "extension\\s*=\\s*\"[^\"]*\"";		
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(schematag);
		if (matcher.find()){	
			String newTag = matcher.replaceFirst("");
			return newTag;
		} else {			
			return schematag;
		}
	}
	
	//=====================================================
	// Обновить свойства проекта (расширения для редактора при импорте)

	public static void updateProperties(DipProject project){
		DipProjectSchemaModel schemaModel = project.getSchemaModel();
		updateProperties(schemaModel);
	}
	
	public static void updateProperties(DipProjectSchemaModel schemaModel){
		if (schemaModel != null){
			setEditorExtension(schemaModel);
		}
	}
	
	private static void setEditorExtension(DipProjectSchemaModel schemaModel){
		// get file associations
	    EditorRegistry editorReg = (EditorRegistry)PlatformUI.getWorkbench().getEditorRegistry();
	    EditorDescriptor editor = (EditorDescriptor) editorReg.findEditor("ru.dip.ui.form.editor");
	    IFileEditorMapping[] mappings = editorReg.getFileEditorMappings();
	    // add new file associations
	    ArrayList<IFileEditorMapping> newEditorMappings = new ArrayList<>();
	    Collections.addAll(newEditorMappings, mappings);
	    for (Schema schema: schemaModel.getSchemas()){
	    	if (!containsExtension(newEditorMappings, schema.getFileExtension())){
	    	    FileEditorMapping mapping = new FileEditorMapping(schema.getFileExtension());
	    	    mapping.addEditor(editor);
	    	    mapping.setDefaultEditor(editor);
	    	    newEditorMappings.add(mapping);
	    	}
	    }	    
	    // save file associations
	    FileEditorMapping[] newMappings = new FileEditorMapping[newEditorMappings.size()];
	    newEditorMappings.toArray(newMappings);
	    editorReg.setFileEditorMappings(newMappings);
	    editorReg.saveAssociations();	 
	}
	
	private static boolean containsExtension(Collection<IFileEditorMapping> mappings, String extension){
		for (IFileEditorMapping mapping: mappings){
			if (extension.equals(mapping.getExtension())){
				return true;
			}
		}	
		return false;
	}
	
	//=====================================================
	//  default schema
	//
	public static void saveDefaultSchema(Schema schema) throws DIPException {
		copyDefaultSchema(schema.getFileName());
		Path defaultShchemaPath = getWorkspaceDefaultSchemaPath();
		writeSchemaAttributes(schema.getName(), defaultShchemaPath);
	}
	
	private static void copyDefaultSchema(String filepath) throws DIPException {
		Path sourcePath = Paths.get(filepath);
		Path destinationPath = getWorkspaceDefaultSchemaPath();
		try {
			Files.copy(sourcePath, destinationPath, StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			e.printStackTrace();
			throw new DIPException("Ошибка при копирование схемы по умолчанию");
		}
	}
	
	/**
	 * Установить дефолтную схему из плагина, как дефолтную в Workspace
	 */
	public static void savePluginDefaultSchema() throws DIPException {
		Path sourcePath = getPluginDefaultSchemaPath();
		Path destinationPath = getWorkspaceDefaultSchemaPath();
		try {
			Files.copy(sourcePath, destinationPath, StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			e.printStackTrace();
			throw new DIPException("Ошибка при копирование схемы по умолчанию");
		}
	}
	
	/**
	 *  Дефолтная схема заданная в настройках Workspace
	 */
	private static Path getWorkspaceDefaultSchemaPath() {
		return ResourcesUtilities.metadataPluginPath(DipCorePlugin.getDefault(), "default_schema.xml");
	}
	
	/**
	 *  Дефолтная схема из плагина 
	 */
	public static Path getPluginDefaultSchemaPath() throws DIPException {
		try {
			return ResourcesUtilities.getPathFromPlugin(DipCorePlugin.getDefault(), DipSchemaFolder.DEFAULT_SCHEMA_PATH);
		} catch (IOException e) {
			e.printStackTrace();
			throw new DIPException("Не найдена схема по умолчанию");
		}
	}
	
	public static Path getExistDefaultSchemaPath() throws DIPException {
		Path workspaceDefault = getWorkspaceDefaultSchemaPath();
		if (Files.exists(workspaceDefault)) {
			return workspaceDefault;
		} else {
			return getPluginDefaultSchemaPath();
		}
	}
	
	public static String getDefaultSchemaName() {
		Path path = getWorkspaceDefaultSchemaPath();
		if (!Files.exists(path)) {
			return DipSchemaFolder.DEFAULT_SCHEMA_NAME;
		}		
		SimpleSchemaReader reader = new SimpleSchemaReader(path);
		try {
			reader.readSchema();
			return reader.getName();
		} catch (ParserConfigurationException | SAXException | IOException e) {
			e.printStackTrace();			
		}
		return null;
	}
	
}
