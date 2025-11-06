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
package ru.dip.core.table;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.core.resources.IFile;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import ru.dip.core.DipCorePlugin;

public class TableReader {

	public static final String OLD_REQ_ELEMENT = "reg";
	public static final String REQ_ELEMNT = "file";
	public static final String REQ_FOLDER = "folder";
	public static final String INCLUDE_FOLDER = "include";
	// атрибуты
	public static final String NAME_ATTRIBUTE = "name";
	public static final String LINK_ATTRIBUTE = "link";
	public static final String DESCRIPTION_ATTRIBUTE = "description";
	public static final String NUMERATION_ATTRIBUTE = "numeration";
	public static final String ORIENTATION = "orientation";	
	public static final String FILE_NM_STEP_ATTRIBUTE = "file_nm_step";
	public static final String FOLDER_NM_STEP_ATTRIBUTE = "folder_nm_step";
	public static final String PAGE_BREAK_ATTRIBUTE = "pagebreak";
	public static final String DISABLE_ATTRIBUTE = "disable";	
	public static final String READ_ONLY = "read_only";
	public static final String ROOT_ATR = "is_root";
	public static final String LANDSCAPE = "LANDSCAPE";

	public static boolean isRoot(URI uri){
		try {
			File file = new File(uri);
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setIgnoringElementContentWhitespace(true);		
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document document = builder.parse(file);
			Element root = document.getDocumentElement();			
			return "true".equalsIgnoreCase(root.getAttribute(ROOT_ATR));
		} catch (ParserConfigurationException | SAXException | IOException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	private List<TableEntry> fFiles = new ArrayList<>();
	private List<TableEntry> fFolders = new ArrayList<>();
	private String fDescription;
	private String fFileStep;
	private String fFolderStep;
	private String fPageBreak;
	private boolean fRoot;
	
	public void readModel(InputStream inputStream) throws ParserConfigurationException, SAXException, IOException {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setIgnoringElementContentWhitespace(true);		
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document document = builder.parse(inputStream);
		parseDocument(document);
	}
	
	public void readModel(IFile ifile){
		readModel(ifile.getLocationURI());
	}
	
	private void readModel(URI uri) {
		File file = new File(uri);
		try {
			readDnfo(file);
		} catch (ParserConfigurationException | SAXException | IOException e) {
			DipCorePlugin.logError(e, "TABLE_FILE_ERROR");
		}
	}
	
	private void readDnfo(File file) throws ParserConfigurationException, SAXException, IOException{
		if (!file.exists()){
			DipCorePlugin.logInfo("TABLE_FILE_NOT_FOUND");
			return;
		}
		
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setIgnoringElementContentWhitespace(true);		
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document document = builder.parse(file);
		parseDocument(document);
	}
	
	
	private void parseDocument(Document document) {
		Element root = document.getDocumentElement();
		fDescription = root.getAttribute(DESCRIPTION_ATTRIBUTE);
		fFileStep = root.getAttribute(FILE_NM_STEP_ATTRIBUTE);
		fFolderStep = root.getAttribute(FOLDER_NM_STEP_ATTRIBUTE);
		fPageBreak = root.getAttribute(PAGE_BREAK_ATTRIBUTE);
		fRoot = "true".equalsIgnoreCase(root.getAttribute(ROOT_ATR));
		if (fPageBreak.isEmpty()) {
			fPageBreak = null;
		}		
		NodeList children = root.getChildNodes();
		for (int i = 0; i < children.getLength(); i++){
			Node child = children.item(i);
			if (child instanceof Element){
				Element element = (Element) child;
				TableEntry entry = addField(element);
				if (entry != null){
					String elementName = element.getNodeName();
					if (REQ_ELEMNT.equals(elementName)){
						fFiles.add(entry);
					} else if (REQ_FOLDER.equals(elementName)){
						fFolders.add(entry);
					} else if (OLD_REQ_ELEMENT.equals(elementName)){
						fFiles.add(entry);
					} else if (INCLUDE_FOLDER.equals(elementName)) {
						fFolders.add(entry);
					}
				} 
			}
		}
	}
	
	private TableEntry addField(Element element){		
		String name = element.getAttribute(NAME_ATTRIBUTE);
		String description = element.getAttribute(DESCRIPTION_ATTRIBUTE);
		String numeration = element.getAttribute(NUMERATION_ATTRIBUTE);
		String orientation = element.getAttribute(ORIENTATION);
		String link = element.getAttribute(LINK_ATTRIBUTE);
		String disable = element.getAttribute(DISABLE_ATTRIBUTE);
		String readOnly = element.getAttribute(READ_ONLY);
		if (name != null ){
			TableEntry tableEntry =  new TableEntry(name, description, numeration);
			tableEntry.setLink(link);
			tableEntry.setReadOnly(readOnly);
			tableEntry.setDisable(disable);
			tableEntry.setOrientation(orientation);
			return tableEntry;
		}
		return null;
	}
	
	public List<TableEntry> getFiles(){
		return fFiles;
	}
	
	public List<TableEntry> getFolders(){
		return fFolders;
	}
	
	public String getDescription(){
		return fDescription;
	}
	
	public String getPageBreak() {
		return fPageBreak;
	}
	
	public String getFileStepNumeration() {
		return fFileStep;
	}
	
	public String getFolderStepNumeration() {
		return fFolderStep;
	}
	
	public boolean isRoot() {
		return fRoot;
	}

}
