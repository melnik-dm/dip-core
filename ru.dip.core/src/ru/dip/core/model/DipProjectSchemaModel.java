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
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.core.resources.IFile;
import org.xml.sax.SAXException;

import ru.dip.core.DipCorePlugin;
import ru.dip.core.exception.DIPException;
import ru.dip.core.exception.DeleteDIPException;
import ru.dip.core.exception.RenameDIPException;
import ru.dip.core.exception.TmpCopyException;
import ru.dip.core.form.model.CoreFormModel;
import ru.dip.core.model.interfaces.IDipElement;
import ru.dip.core.schema.FormShowProperties;
import ru.dip.core.schema.SchemaReader;
import ru.dip.core.schema.Schema;
import ru.dip.core.utilities.FileUtilities;
import ru.dip.core.utilities.DipUtilities;
import ru.dip.core.utilities.WorkbenchUtitlities;

public class DipProjectSchemaModel {

	private DipSchemaFolder fSchemaFolder;
	private ArrayList<Schema> fSchemas = new ArrayList<>();
	private ArrayList<FormShowProperties> fFormShowSettings = new ArrayList<>();

	public DipProjectSchemaModel(DipSchemaFolder folder) {
		fSchemaFolder = folder;
		readSchemaFolder();
		loadFormShowSettings();
	}
	
	//========================
	// compute model
	
	private void readSchemaFolder() {		
		fSchemas = new ArrayList<>();
		for (IDipElement element: fSchemaFolder.getChildren()){			
			if (element instanceof DipSchemaElement){
				DipSchemaElement formSchema = (DipSchemaElement) element;		
				Schema schema = getSchema(formSchema);
				if (schema == null){
					continue;
				}
				fSchemas.add(schema);				
			}
		}
	}
	
	public void addSchema(DipSchemaElement formSchema) {
		Schema schema = getSchema(formSchema);
		if (schema != null){
			fSchemas.add(schema);
		}
	}
		
	private Schema getSchema(DipSchemaElement formSchema){
		CoreFormModel formModel = new CoreFormModel();
		formModel.setExtension(formSchema.getFormExtension());
		try {
			SchemaReader schemaReader = new SchemaReader(formModel, formSchema.resource(), true);
			formModel.setFields(schemaReader.getFields());
		} catch (ParserConfigurationException | SAXException | IOException e) {
			DipCorePlugin.logError(e,  "READ SCHEMA " + formSchema.name() + " "  + e.getMessage());
			WorkbenchUtitlities.openError("Read schema error", "Ошибка чтения схемы " + formSchema.name() + ".  " + e.getMessage());	
			return null;
		}
		
		String name = formModel.getName();
		String extension = formModel.getExtension();
				
		if (extension == null || extension.isEmpty()){
			extension = FileUtilities.getNameWithoutExtension(formSchema.resource());						
		}
		
		return Schema.getInstance(extension, name, formModel);	
	}
	
	public Schema getRenamedShema(DipSchemaElement formSchema, String newFileName){
		if (fSchemaFolder.getChild(newFileName) != null){
			String errorMessage = "Схемы содержат одинаковые расширения.[" 
			+ formSchema.name() + ", " + newFileName + "].";
			DipCorePlugin.logError(formSchema.resource().getLocation() +  errorMessage);
			WorkbenchUtitlities.openError("Read schema error", errorMessage);		
			return null;
		} else {								
			try {
				DipUtilities.renameSchema(formSchema, newFileName, null);
				return getSchema(formSchema);
			} catch (RenameDIPException e) {
				e.printStackTrace();
				WorkbenchUtitlities.openError("Rename schema error", e.getMessage());		
				return null;
			}
		}
	}
	
	//==========================
	// Form Show Settings
		
	public void loadFormShowSettings() {
		for (Schema schema: fSchemas) {
			FormShowProperties schemaSetting = new FormShowProperties(schema);
			schemaSetting.createFieldProperties();
			schemaSetting.loadFieldsProperties(fSchemaFolder.dipProject().getProject());		
			fFormShowSettings.add(schemaSetting);
		}
	}
	
	public FormShowProperties findFormSettings(String name) {
		for (FormShowProperties formSetting: fFormShowSettings) {
			if (name.equals(formSetting.name())) {
				return formSetting;
			}
		}
		return null;
	}
	
	//===========================
	// import Schemas
		
	public boolean containsFileExtension(String fileExtension){
		for (Schema schema: fSchemas){
			if (fileExtension.equals(schema.getFileExtension())){
				return true;
			}
		}
		return false;
	}
	
	public Path getSchemaFilePath(String extension){
		String filename = getSchemaFileName(extension);
		if (filename != null){
			return Paths.get(fSchemaFolder.resource().getLocation().toOSString(), filename);
		}
		return null;
	}
	
	public Schema getSchema(String fileExtension){
		for (Schema schema: fSchemas){
			if (fileExtension.equals(schema.getFileExtension())){
				return schema;
			}
		}
		return null;
	}

	public IFile getSchemafile(String extension){
		for (Schema schema: fSchemas){
			if (extension.equals(schema.getFileExtension())){				
				IDipElement schemaElement = fSchemaFolder.getChild(schema.getFileName());
				if (schemaElement == null){
					return null;
				}
				return (IFile) schemaElement.resource();
			}
		}
		return null;				
	}
	
	private String getSchemaFileName(String extension){
		for (Schema schema: fSchemas){
			if (extension.equals(schema.getFileExtension())){
				return schema.getFileName();
			}			
		}
		return null;
	}
	
	public Path getSchemaPath(Schema schema){
		String filename = schema.getFileName();
		if (filename == null){
			return null;
		}
		if (schema.isExist()){
			return Paths.get(fSchemaFolder.resource().getLocation().toOSString(), filename);
		} else {
			return  Paths.get(filename);
		}
	}
	
	public ArrayList<String> getAllExtensions(){
		ArrayList<String> result = new ArrayList<>();
		for (Schema schema: fSchemas) {
			String extension = schema.getFileExtension();
			if (!result.contains(extension)) {
				result.add(extension);
			}
		}
		return result;
	}
	
	public void add(Schema schema) throws IOException{
		String fileExtension = schema.getFileExtension();
		if (getSchema(fileExtension) != null){
			throw new DIPException("Уже существует схема для " + schema.getFileExtension());
		}	
		fSchemaFolder.createFolderIfNotExist();	
		if (!schema.isExist()){
			String filename = schema.getFileExtension() + ".xml";
			Path path = Paths.get(fSchemaFolder.resource().getLocation().toOSString(),  filename);
			schema.importSchema(path);
		}
		fSchemas.add(schema);		
	}
	
	public void remove(Schema schema) throws DeleteDIPException, TmpCopyException {
		DipSchemaElement formSchema = fSchemaFolder.findFormSchema(schema);
		if (formSchema != null){
			DipUtilities.deleteElement(formSchema, false, null, false);
			fSchemas.remove(schema);		
		}
	}
	
	//======================
	// getters
	
	public List<FormShowProperties> getFormShowProperties(){
		return fFormShowSettings;
	}
	
	public ArrayList<Schema> getSchemas(){
		return fSchemas;
	}
	
	public DipSchemaFolder getSchemaFolder(){
		return fSchemaFolder;
	}
	
	public void refresh() {
		fSchemaFolder.refresh();
	}

	public Schema getSchemaByName(String schemaName) {
		if (schemaName == null) {
			return null;
		}
		
		for (Schema schema: fSchemas) {
			if (Objects.equals(schemaName, schema.getName())){
				return schema;
			}
		}		
		return null;
	}

	public void update() {
		readSchemaFolder();
	}
	
}
