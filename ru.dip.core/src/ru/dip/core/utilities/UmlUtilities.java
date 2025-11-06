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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.commonmark.node.Node;
import org.commonmark.renderer.html.HtmlRenderer;
import org.eclipse.core.resources.IFile;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

import net.sourceforge.plantuml.SourceStringReader;
import ru.dip.core.model.DipUnit;
import ru.dip.core.model.interfaces.IDipElement;
import ru.dip.core.model.interfaces.IDipUnit;
import ru.dip.core.unit.TextPresentation;
import ru.dip.core.utilities.md.parser.MdParser;

public class UmlUtilities {
	
	//=============================
	//  UML
	
	public static Image getUmlImage(IFile file){
		String filename = file.getLocation().toOSString();
		File imageFile = UmlUtilities.getImageFileFromUml(filename, filename);
		if (imageFile != null) {
			return new Image(Display.getDefault(), imageFile.getAbsolutePath());
		}
		return null;
	}
	
	public static Image getUmlImage(String umlContent, IFile file){
		String filename = file.getLocation().toOSString();
		File imageFile = UmlUtilities.getImageFileFromUmlContent(umlContent, filename);
		if (imageFile != null) {
			return new Image(Display.getDefault(), imageFile.getAbsolutePath());
		}
		return null;
	}
	
	public static File getImageFileFromUml(String filename, String tempFileName){			
		Path path = Paths.get(filename);
		String content = FileUtilities.readFile(path,"");
		try {
			SourceStringReader reader = new SourceStringReader(content);		
			File file = File.createTempFile(tempFileName, "");
			reader.outputImage(Files.newOutputStream(file.toPath()));
			return file;
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static File getImageFileFromUmlContent(String content, String tempFileName){						
		SourceStringReader reader = new SourceStringReader(content);		
		try {
			File file = File.createTempFile(tempFileName, "");
			reader.outputImage(Files.newOutputStream(file.toPath()));
			return file;
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	//================================
	// DOT
	
	public static Image getDotImage(IFile file){
		String filename = file.getLocation().toOSString();
		File imageFile = UmlUtilities.getDotInputStream(filename, filename);
		if (imageFile != null) {
			return new Image(Display.getDefault(), imageFile.getAbsolutePath());
		}
		return null;
	}
	
	public static File getDotInputStream(String filename, String tempFileName){			
		Path path = Paths.get(filename);
		String content = readDotFile(path); 	
		SourceStringReader reader = new SourceStringReader(content);	
		try {
			File file = File.createTempFile(tempFileName, "");
			reader.outputImage(Files.newOutputStream(file.toPath()));
			return file;
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static String readDotFile(Path path){
		String text = FileUtilities.readFile(path,""); 
		StringBuilder builder = new StringBuilder();
		if (!text.startsWith("@startuml")){
			builder.append("@startuml\n");
		} 
		builder.append(text);
		if (!text.endsWith("@enduml")){
			builder.append("\n@enduml");
		}
		return builder.toString();	
	}
	
	//============================
	// MARKDOWN
	
	public static String getHtmlFromMDText(IFile file, IDipUnit unit){
		Path path = Paths.get(file.getLocation().toOSString());		
		String content = FileUtilities.readFile(path,"");  
		
		// change title links and variables
		IDipElement element = DipUtilities.findElement(file);
		if (element instanceof DipUnit) {
			content = TextPresentation.prepareText(content, (IDipUnit)element);
		}		
		return convertMdText(content, unit);
	}
	
	private static String convertMdText(String mdText, IDipUnit unit){		
		MdParser parser = MdParser.instance();
		Node document = parser.parse(mdText, unit);		
		HtmlRenderer renderer = HtmlRenderer.builder().build();
		return renderer.render(document);
	}
	
}
