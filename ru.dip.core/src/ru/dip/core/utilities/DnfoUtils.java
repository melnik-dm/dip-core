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

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

public class DnfoUtils {
	
	public static final String DNFO_FILENAME = ".dnfo";
	
	private static final String DESCRIPTION_XPATH = "//table/@description";
	
	
	public static String getDescription(IFolder folder) {
		IFile file = folder.getFile(DNFO_FILENAME);
		if (file.exists()) {
			return getDescription(file.getLocationURI().toString());
		}
		return null;
	}
	
	public static String getDescription(Path folderPath) {
		Path dnfoPath = folderPath.resolve(DNFO_FILENAME);	
		if (Files.exists(dnfoPath)) {
			return getDescription(dnfoPath.toString());
		}
		return null;
	}
	
	private static String getDescription(String fullDnfoPath) {
		try {
			Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(fullDnfoPath);
			XPath xpath = XPathFactory.newInstance().newXPath();
			String description = xpath.evaluate(DESCRIPTION_XPATH, doc);
			return description;

		} catch (IOException | SAXException | ParserConfigurationException | XPathExpressionException e) {
			e.printStackTrace();
		}
		return null;
	}

}
