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
package ru.dip.core.form;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ui.editors.text.StorageDocumentProvider;
import org.xml.sax.SAXException;

import ru.dip.core.form.model.CorePositionModel;
import ru.dip.core.form.model.CoreFormModel;
import ru.dip.core.form.model.Field;
import ru.dip.core.form.partitioner.DocumentPartitioner;
import ru.dip.core.form.partitioner.PartitionScanner;
import ru.dip.core.schema.SchemaReader;

public class FormReader {

	private IFile fFile;
	private CoreFormModel fFormModel;
	private DocumentPartitioner fPartitioner;
	private IDocument fDocument;
	private DipDocProvider fDocumentProvider;

	public FormReader(IFile file) {
		fFile = file;
	}

	public void read() {
		// read schema
		fFormModel = new CoreFormModel();
		try {
			SchemaReader schemaReader = new SchemaReader(fFormModel, fFile);
			fFormModel.setFields(schemaReader.getFields());
		} catch (ParserConfigurationException | SAXException | IOException e1) {
			e1.printStackTrace();
		}
		// read file
		fDocument = new Document();
		fDocumentProvider = new DipDocProvider();
		try {
			fDocumentProvider.setDocumentContent(fDocument, fFile.getContents(), null);
		} catch (CoreException e) {
			e.printStackTrace();
			return;
		}		
		createModel();
	}
	
	public void read(InputStream stream, InputStream schemaStream) {
		fFormModel = new CoreFormModel();
		try {
			SchemaReader schemaReader = new SchemaReader(fFormModel, schemaStream);
			fFormModel.setFields(schemaReader.getFields());
		} catch (ParserConfigurationException | SAXException | IOException e1) {
			e1.printStackTrace();
		}
		// read file
		fDocument = new Document();
		fDocumentProvider = new DipDocProvider();
		try {
			fDocumentProvider.setDocumentContent(fDocument, stream, null);
		} catch (CoreException e) {
			e.printStackTrace();
			return;
		}		
		createModel();
	}
	
	private void createModel() {
		fPartitioner = new DocumentPartitioner(new PartitionScanner(),
				PartitionScanner.CONTENT_TYPES);
		fPartitioner.connect(fDocument);
		fDocument.setDocumentPartitioner(fPartitioner);
		CorePositionModel positionModel = new CorePositionModel(fFormModel, fPartitioner, fDocument);
		positionModel.createModel();
		fFormModel.setPositionModel(positionModel);
	}
	
	public void saveDocument(){
		Path path = Paths.get(fFile.getLocationURI());
		String content = fDocument.get();
		ArrayList<String> list = new ArrayList<>();
		list.add(content);
		try {
			Files.write(path, list, StandardCharsets.UTF_8);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static class DipDocProvider extends StorageDocumentProvider {
		@Override
		public void setDocumentContent(IDocument document, InputStream contentStream, String encoding)
				throws CoreException {
			/*if (encoding == null) {
				encoding = System.getProperty("file.encoding");
				//System.out.println("ENCODING: " + encoding);
			}*/
			encoding = "UTF-8";
			super.setDocumentContent(document, contentStream, encoding);
		}
	}
	
	//=======================
	// getters
	
	public String getContent(){
		return fDocument.get();
	}
	
	public List<Field> getFields(){
		return fFormModel.getFields();
	}
	
	public boolean isEmptyDocument(){
		return fDocument.get().isEmpty();
	}
	
	public String getShemaName() {
		return fFormModel.getName();
	}
	
	public String fixedField() {
		return fFormModel.getFixedField();
	}
	
	public Field getFieldByName(String name) {
		for (Field field: getFields()) {
			if (field.getName().equals(name)) {
				return field;
			}
		}
		return null;
	}

}
