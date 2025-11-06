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
import java.nio.file.Path;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

public class SimpleSchemaReader {
	
	private String fName;
	private File fFile;
	
	public SimpleSchemaReader(Path path) {
		fFile = path.toFile();
	}
	
	public void readSchema() throws ParserConfigurationException, SAXException, IOException{
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setIgnoringElementContentWhitespace(true);		
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document document = builder.parse(fFile);
		Element root = document.getDocumentElement();
		fName = root.getAttribute(SchemaReader.NAME_ATTRIBUTE);
	}

	public String getName() {
		return fName;
	}
}
