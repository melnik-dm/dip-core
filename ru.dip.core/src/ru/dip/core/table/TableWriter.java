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

import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

import ru.dip.core.DipCorePlugin;
import ru.dip.core.model.IncludeFolder;
import ru.dip.core.model.Appendix;
import ru.dip.core.model.DnfoTable;
import ru.dip.core.model.interfaces.IDipParent;
import ru.dip.core.model.interfaces.IDipUnit;
import ru.dip.core.model.interfaces.IDipDocumentElement;
import ru.dip.core.utilities.TagStringUtilities;

public class TableWriter {
	
	public static void saveModel(IDipParent parent) throws ParserConfigurationException, IOException {				
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document doc = builder.newDocument();
		Element xmlRootElement = doc.createElement("table");	
		List<IDipDocumentElement> children = parent.getDipDocChildrenList();		
		String tableDescription = parent.description();
		if (parent.isRoot()) {
			xmlRootElement.setAttribute(TableReader.ROOT_ATR, "true");
		}
		
		if (parent instanceof IncludeFolder) {
			tableDescription = ((IncludeFolder) parent).mainDescription();
		}
		if (tableDescription != null && !tableDescription.isEmpty()){
			xmlRootElement.setAttribute(TableReader.DESCRIPTION_ATTRIBUTE, tableDescription);
		}
		if (parent.isFileNumeration()){
			xmlRootElement.setAttribute(TableReader.FILE_NM_STEP_ATTRIBUTE, parent.getFileStep());
		}
		if (parent.isFolderNumeration()){
			xmlRootElement.setAttribute(TableReader.FOLDER_NM_STEP_ATTRIBUTE, parent.getFolderStep());
		}
		
		String pageBreak = parent.getPageBreak();
		if (parent.resource() instanceof IProject || parent instanceof Appendix) {												
			if (pageBreak == null) {
				pageBreak = "EACH_FOLDER";
			}
			xmlRootElement.setAttribute(TableReader.PAGE_BREAK_ATTRIBUTE, pageBreak);
		} else if (pageBreak != null) {
			xmlRootElement.setAttribute(TableReader.PAGE_BREAK_ATTRIBUTE, pageBreak);
		}
				
		Text firstText = doc.createTextNode("\n");
		xmlRootElement.appendChild(firstText);
		for (IDipDocumentElement dipDocumentElement: children){
			Element xmlDipElement;
			if (dipDocumentElement instanceof IncludeFolder) {
				xmlDipElement = createLinkElement(doc, (IncludeFolder) dipDocumentElement);		
			} else if (dipDocumentElement instanceof IDipParent){
				xmlDipElement = createDipParentElement(doc, (IDipParent) dipDocumentElement);
			} else {
				xmlDipElement = createUnitElement(doc, dipDocumentElement);
			}						
			xmlRootElement.appendChild(xmlDipElement);
			Text text = doc.createTextNode("\n");
			xmlRootElement.appendChild(text);
		}
		
		doc.appendChild(xmlRootElement);
		try {
			saveDoc(doc, getPath(parent).toString());
		} catch (TransformerException e) {
			DipCorePlugin.logError(e, "SAVE_MODEL_DNFO_ERROR " + parent.id());
			e.printStackTrace();
		}	
	}
	
	private static Element createUnitElement(Document doc, IDipDocumentElement dipDocumentElement) {
		Element xmlDipElement = doc.createElement(TableReader.REQ_ELEMNT);				
		xmlDipElement.setAttribute(TableReader.NAME_ATTRIBUTE, dipDocumentElement.resource().getName());
		if (dipDocumentElement.isDisabled()) {
			xmlDipElement.setAttribute(TableReader.DISABLE_ATTRIBUTE, "true");
		}		
		if (dipDocumentElement instanceof IDipParent && !((IDipParent) dipDocumentElement).isActiveNumeration()){
			xmlDipElement.setAttribute(TableReader.NUMERATION_ATTRIBUTE, "false");
		}				
		if (dipDocumentElement instanceof IDipUnit && ((IDipUnit) dipDocumentElement).isHorizontalOrientation()) {
			xmlDipElement.setAttribute(TableReader.ORIENTATION, TableReader.LANDSCAPE);
		} 
		return xmlDipElement;
	}
	
	private static Element createDipParentElement(Document doc, IDipParent dipParent) {
		Element xmlDipElement = doc.createElement(TableReader.REQ_FOLDER);
		xmlDipElement.setAttribute(TableReader.NAME_ATTRIBUTE, dipParent.resource().getName());
		if (!dipParent.isActiveNumeration()) {
			xmlDipElement.setAttribute(TableReader.NUMERATION_ATTRIBUTE, "false");
		}
		if (dipParent.isDisabled()) {
			xmlDipElement.setAttribute(TableReader.DISABLE_ATTRIBUTE, "true");
		}
		try {
			saveModel((IDipParent) dipParent);
		} catch (ParserConfigurationException | IOException e) {
			DipCorePlugin.logError(e, "SAVE_MODEL_ERROR");
			e.printStackTrace();
		}
		return xmlDipElement;
	}
	
	private static Element createLinkElement(Document doc, IncludeFolder includeParent) {
		Element xmlDipElement = doc.createElement(TableReader.INCLUDE_FOLDER);
		xmlDipElement.setAttribute(TableReader.LINK_ATTRIBUTE, includeParent.getLinkRelativePath());
		xmlDipElement.setAttribute(TableReader.NAME_ATTRIBUTE, includeParent.dipName());
		String linkDescription = includeParent.linkDescription();
		if (linkDescription != null && !linkDescription.isEmpty()) {
			xmlDipElement.setAttribute(TableReader.DESCRIPTION_ATTRIBUTE, linkDescription);
		}
		if (includeParent.isReadOnly()) {
			xmlDipElement.setAttribute(TableReader.READ_ONLY, "true");
		}		
		if (!includeParent.isActiveNumeration()) {
			xmlDipElement.setAttribute(TableReader.NUMERATION_ATTRIBUTE, "false");
		}
		if (includeParent.isDisabled()) {
			xmlDipElement.setAttribute(TableReader.DISABLE_ATTRIBUTE, "true");
		}	
		try {
			if (includeParent.resource() != null) {
				saveModel((IDipParent) includeParent);
			}
		} catch (ParserConfigurationException | IOException e) {
			DipCorePlugin.logError(e, "SAVE_MODEL_ERROR");
			e.printStackTrace();
		}
		return xmlDipElement;
	}
	
	private static Path getPath(IDipParent parent){
		return Paths.get(parent.resource().getLocation().toOSString(), DnfoTable.TABLE_FILE_NAME);
	}
	
	public static void saveEmptyModel(IFile file) throws ParserConfigurationException, IOException, TransformerException{
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document doc = builder.newDocument();
		Element xmlRootElement = doc.createElement("table");
		Text firstText = doc.createTextNode("\n");
		xmlRootElement.appendChild(firstText);	
		doc.appendChild(xmlRootElement);	
		try {
			saveDoc(doc, file.getLocation().toOSString());
		} catch (TransformerException e) {
			DipCorePlugin.logError(e, "SAVE_MODEL_DNFO_ERROR " + file.getLocation().toOSString());
			e.printStackTrace();
		}	
	}
	
	private static void saveDoc(Document doc, String path) throws TransformerException, IOException {
		TransformerFactory t_factory = TransformerFactory.newInstance();		
		Transformer transformer = t_factory.newTransformer();
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		transformer.setOutputProperty(OutputKeys.METHOD, "xml");
	    transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
	    transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "0");
		DOMSource dom_source = new DOMSource(doc);
		StringWriter writer = new StringWriter();
		StreamResult result = new StreamResult(writer);
		transformer.transform(dom_source, result);		
		List<String> lines = TagStringUtilities.removeEmptyStirng(writer.toString());
		Files.write(Paths.get(path), lines, StandardCharsets.UTF_8);
		writer.close();
	}
	
}
