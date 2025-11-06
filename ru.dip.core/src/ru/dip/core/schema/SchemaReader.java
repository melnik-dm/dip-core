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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import ru.dip.core.exception.DIPException;
import ru.dip.core.form.model.CheckField;
import ru.dip.core.form.model.ComboField;
import ru.dip.core.form.model.CoreFormModel;
import ru.dip.core.form.model.Field;
import ru.dip.core.form.model.Item;
import ru.dip.core.form.model.RadioField;
import ru.dip.core.form.model.TextField;
import ru.dip.core.model.DipProject;
import ru.dip.core.model.DipProjectSchemaModel;
import ru.dip.core.model.DipRoot;
import ru.dip.core.utilities.SchemaUtilities;

public class SchemaReader {

	// elements
	private static final String ITEM_ELEMENT = "item";
	// attributes
	public static final String NAME_ATTRIBUTE = "name";
	private static final String FIXED_FIELD = "fixed_field";
	private static final String TYPE_ATTRIBUTE = "type";
	private static final String DEFAULT_ATTRIBUTE = "default";
	private static final String HEIGHT_ATTRIBUTE = "height";
	private static final String WIDTH_ATTRIBUTE = "width";
	private static final String LENGTH_ATTRIBUTE = "length";
	private static final String TITLE_ATTRIBUTE = "title";
	private static final String VALUE_ATTRIBUTE = "value";
	private static final String BACKGRAUND_COLOR = "cbg";
	private static final String FOREGRAUND_COLOR = "cfont";
	private static final String HINT_ATTRIBUTE = "hint";
	private static final String SEQ_ATTRIBUTE = "seq";
	// type values
	private static final String COMBO_TYPE = "COMBO";
	private static final String CHECK_TYPE = "CHECK";
	private static final String RADIO_TYPE = "RADIO";

	private CoreFormModel fFormModel;
	private List<Field> fFields = new ArrayList<>();
	private boolean fDefault = false;
	
	public SchemaReader(CoreFormModel formModel, InputStream schemaInputStream) throws ParserConfigurationException, SAXException, IOException {
		fFormModel = formModel;
		Document document = readXmlDocument(schemaInputStream);
		readSchema(document);
	}
	
	public SchemaReader(CoreFormModel formModel, IFile formFile) throws ParserConfigurationException, SAXException, IOException {
		fFormModel = formModel;
		File schemaFile = getSchemaFile(formFile);
		if (schemaFile != null){
			Document document = readXmlDocument(schemaFile);
			readSchema(document);
		}		
	}
	
	/**
	 *  Чтение схемы из схемы (без файла)
	 * @throws IOException 
	 * @throws SAXException 
	 * @throws ParserConfigurationException 
	 */
	public SchemaReader(CoreFormModel formModel, IFile schemaFile, boolean simple) throws ParserConfigurationException, SAXException, IOException {
		fFormModel = formModel;
		Path path = Paths.get(schemaFile.getLocationURI());
		File schema =  new File(path.toUri());
		Document document = readXmlDocument(schema);		
		readSchema(document);
	}
	
	
	public SchemaReader(String schemaContent) throws SAXException, IOException, ParserConfigurationException {
		fFormModel = new CoreFormModel();
		Document document = readXmlDocument(schemaContent);
		readSchema(document);
	}
	
	private File getSchemaFile(IFile file){
		File schemaFile = getSchemaFileFromProject(file);
		if (schemaFile == null){
			schemaFile =  getDefaultSchemaFile();
			fDefault = true;
		}
		return schemaFile;
	}
	
	private File getSchemaFileFromProject(IFile file){
		IProject project = file.getProject();
		String fileExtension = file.getFileExtension();
		return getSchemaFileFromProject(project, fileExtension);
	}
	
	private File getSchemaFileFromProject(IProject project, String fileExtension){
		DipProject dipProject = DipRoot.getInstance().findDipProject(project);
		if (dipProject == null) {
			return null;
		}
		DipProjectSchemaModel schemaModel = dipProject.getSchemaModel();
		Path path = schemaModel.getSchemaFilePath(fileExtension);
		if (path != null && Files.exists(path)){
			return new File(path.toUri());
		}
		return null;
	}
	
	private File getDefaultSchemaFile(){
		try {
			return SchemaUtilities.getExistDefaultSchemaPath().toFile();
		} catch (DIPException e) {
			return null;
		}
	}
	
	private Document readXmlDocument(InputStream input) throws SAXException, IOException, ParserConfigurationException {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setIgnoringElementContentWhitespace(true);		
		DocumentBuilder builder = factory.newDocumentBuilder();
		return builder.parse(input);	
	}
	
	private Document readXmlDocument(File file) throws SAXException, IOException, ParserConfigurationException {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setIgnoringElementContentWhitespace(true);		
		DocumentBuilder builder = factory.newDocumentBuilder();
		return builder.parse(file);			
	}
	
	private Document readXmlDocument(String content) throws SAXException, IOException, ParserConfigurationException {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setIgnoringElementContentWhitespace(true);		
		DocumentBuilder builder = factory.newDocumentBuilder();
		return builder.parse(content);			
	}
	
	private void readSchema(Document document) throws ParserConfigurationException, SAXException, IOException{
		fFields = new ArrayList<>();
		Element root = document.getDocumentElement();
		String name = root.getAttribute(NAME_ATTRIBUTE);
		fFormModel.setName(name);
		String rootHint = root.getAttribute(HINT_ATTRIBUTE);
		rootHint = rootHint.replaceAll("\\\\n", "\n");
		fFormModel.setHint(rootHint);
		String fixedField = root.getAttribute(FIXED_FIELD);
		fFormModel.setFixedField(fixedField);

		NodeList children = root.getChildNodes();
		for (int i = 0; i < children.getLength(); i++){
			Node child = children.item(i);
			if (child instanceof Element){
				Element element = (Element) child;
				addField(element);
			}
		}
	}
	
	private void addField(Element element){
		String name = element.getAttribute(NAME_ATTRIBUTE);
		String type = element.getAttribute(TYPE_ATTRIBUTE);
		String title = element.getAttribute(TITLE_ATTRIBUTE);	
		String defaultValue = element.getAttribute(DEFAULT_ATTRIBUTE); 
		String foregroundColor = element.getAttribute(FOREGRAUND_COLOR);
		String backgroundColor = element.getAttribute(BACKGRAUND_COLOR);
		String hint = element.getAttribute(HINT_ATTRIBUTE);
		title = title.replaceAll("\\\\n", "\n");
		hint = hint.replaceAll("\\\\n", "\n");
		if (COMBO_TYPE.equals(type)){
			Item[] values = getComboValues(element, type);
			Field newField = new ComboField(fFormModel, name, title, values);
			newField.setDefaultValue(defaultValue);
			newField.setForegraund(foregroundColor);
			newField.setBackgroundColor(backgroundColor);
			newField.setHint(hint);

			fFields.add(newField);
		} else if (CHECK_TYPE.equals(type)){
			Item[] values = getComboValues(element, type);
			CheckField newField = new CheckField(fFormModel, name, title, values);
			Integer length = readIntAttribute(element, LENGTH_ATTRIBUTE);
			if (length != null){
				newField.setLength(length);
			}	
			newField.setDefaultValue(defaultValue);
			newField.setForegraund(foregroundColor);
			newField.setBackgroundColor(backgroundColor);
			newField.setHint(hint);
			fFields.add(newField);
		} else if (RADIO_TYPE.equals(type)){
			Item[] values = getComboValues(element, type);
			RadioField newField = new RadioField(fFormModel, name, title, values);
			Integer length = readIntAttribute(element, LENGTH_ATTRIBUTE);
			if (length != null){
				newField.setLength(length);
			}	
			newField.setDefaultValue(defaultValue);
			newField.setForegraund(foregroundColor);
			newField.setBackgroundColor(backgroundColor);
			newField.setHint(hint);
			fFields.add(newField);
		} else {
			TextField newField = new TextField(fFormModel, name, title);
			Integer height = readIntAttribute(element, HEIGHT_ATTRIBUTE);
			Integer width = readIntAttribute(element, WIDTH_ATTRIBUTE);
			newField.setDefaultValue(defaultValue);
			newField.setForegraund(foregroundColor);
			newField.setBackgroundColor(backgroundColor);
			newField.setHint(hint);
			if (height != null){
				newField.setHeight(height);
			}
			if (width != null){
				newField.setWidth(width);
			}
			fFields.add(newField);
		}	
	}
	
	private Item[] getComboValues(Element element, String type){
		List<Item> items = new ArrayList<>();
		NodeList children = element.getChildNodes();
		for (int i = 0; i < children.getLength(); i++){
			Node child = children.item(i);
			if (child instanceof Element && ITEM_ELEMENT.equals(child.getNodeName())){
				Element item = (Element) child;
				String value = item.getAttribute(VALUE_ATTRIBUTE);
				if (CHECK_TYPE.equals(type)){
					value = value.replaceAll("\\\\n", "\n");
				}
				String foregraund = item.getAttribute(FOREGRAUND_COLOR);
				String background = item.getAttribute(BACKGRAUND_COLOR);
				String seq = item.getAttribute(SEQ_ATTRIBUTE);
				String hint = item.getAttribute(HINT_ATTRIBUTE);				
				hint = hint.replaceAll("\\\\n", "\n");
				
				if (value != null && !value.isEmpty()){
					Item fieldItem = new Item(value, foregraund, background, hint);
					if (seq != null) {
						fieldItem.setSeq(seq);
					}
					items.add(fieldItem);					
				}
			}	
		}
		return items.stream().toArray(Item[]::new);
	}
	
	private Integer readIntAttribute(Element element, String atrName){
		String value = element.getAttribute(atrName);
		if (value != null && !value.isEmpty()){
			try {
				return Integer.decode(value);
			} catch (NumberFormatException e){
				return null;
			}			
		}
		return null;
	}
	
	public List<Field> getFields(){
		return fFields;
	}
	
	public boolean isDefault(){
		return fDefault;
	}
}
