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
package ru.dip.core.schema;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.core.resources.IFile;
import org.xml.sax.SAXException;

import ru.dip.core.form.model.CoreFormModel;
import ru.dip.core.form.model.Field;
import ru.dip.core.model.DipProject;
import ru.dip.core.utilities.SchemaUtilities;

public class Schema {
	
	public static final String SCHEMA_FILE_NAME = "schema_"; 

	private CoreFormModel fFormModel;
	private String fFileExtension;
	private String fName;
	private boolean fExist = true;
	private boolean fDirty = false;
	
	public static Schema getInstance(String extension, String name, CoreFormModel model){
		return new Schema(extension, name, model);
	}
	
	public static Schema getInstance(String extension, String name){
		return new Schema(extension, name);
	}
	
	public static Schema getNotExistingInstance(String extension, String name, String fileName){
		Schema schema = new Schema(extension, name);
		schema.fExist = false;
		return schema;
	}
	
	private Schema(String extension, String name){
		fName = name;
		fFileExtension = extension;
	}
	
	private Schema(String extension, String name, CoreFormModel formModel){
		this(extension, name);
		fFormModel = formModel;
	}
	
	public void importSchema(Path destinationPath) throws IOException{
		Path source = Paths.get(getFileName());
		Files.copy(source, destinationPath, StandardCopyOption.REPLACE_EXISTING);
		SchemaUtilities.writeSchemaAttributes(fName, destinationPath);
		fExist = true;
	}
	
	//=========================
	// getters & setters
	
	public String getName(){
		return fName;
	}
	
	public String getFileExtension(){
		return fFileExtension;
	}
	
	public String getFileName() {
		return fFileExtension + ".xml";
	}
	
	public boolean isExist(){
		return fExist;
	}

	public void setFileExtension(String newFileExtension) {
		fFileExtension = newFileExtension;
	}
	
	public void setName(String newName){
		fName = newName;
	}
	
	public void setExist(boolean exist){
		fExist = exist;
	}

	public void setDirty() {
		fDirty = true;		
	}

	public boolean isDirty() {
		return fDirty;
	}

	public CoreFormModel getFormModel(){
		return fFormModel;
	}
	
	public void updateModel(DipProject project){	
		fFormModel = new CoreFormModel();
		IFile file = project.getSchemaModel().getSchemafile(fFileExtension);		
		try {
			SchemaReader reader = new SchemaReader(fFormModel, file, false);
			fFormModel.setFields(reader.getFields());
		} catch (ParserConfigurationException | SAXException | IOException e) {
			e.printStackTrace();
		}
	}
	
	public Field getField(String name){
		for (Field field: fFormModel.getFields()){
			if (field.getName().equals(name)){
				return field;
			}
		}
		return null;
	}
	
}
