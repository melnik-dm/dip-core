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
package ru.dip.ui.export.json.writers;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import ru.dip.core.link.CorrectLink;
import ru.dip.core.link.IncorrectLink;
import ru.dip.core.link.Link;
import ru.dip.core.link.LinkInteractor;
import ru.dip.core.model.interfaces.IDipElement;
import ru.dip.core.utilities.DipUtilities;
import ru.dip.core.utilities.FileUtilities;
import ru.dip.ui.export.ExportElement;
import ru.dip.ui.export.ExportElementType;
import ru.dip.ui.export.FullExportElement;
import ru.dip.ui.export.FullExportPreprocessor;
import ru.dip.ui.export.IExportElement;

public class JsonWriter {
	
	private FullExportPreprocessor fPreprocessor;
	
	public JsonWriter(FullExportPreprocessor preprocessor) {
		fPreprocessor = preprocessor;
	}
	
	public void writeToJson() throws IOException {
		//Gson gson = new Gson();
		GsonBuilder builder = new GsonBuilder();
		builder.registerTypeAdapter(ExportElement.class, new ExportElementSerializer());
		builder.registerTypeAdapter(FullExportPreprocessor.class, new ExportSerializer());
		builder.registerTypeAdapter(Link.class, new LinkSerializer());
		builder.registerTypeAdapter(IncorrectLink.class, new LinkSerializer());
		builder.registerTypeAdapter(CorrectLink.class, new LinkSerializer());
		builder.setPrettyPrinting();	
		Gson gson = builder.create();
		String json = gson.toJson(fPreprocessor);		
		FileUtilities.writeFile(fPreprocessor.getPartPath().resolve("model.json"), json);
	}
	
	private class ExportSerializer implements JsonSerializer<FullExportPreprocessor> {
		
		@Override
		public JsonElement serialize(FullExportPreprocessor preprocessor, Type arg1, JsonSerializationContext arg2) {
			JsonArray array = new JsonArray();			
			for (IExportElement element: preprocessor.getElements()) {
				JsonElement el = arg2.serialize((FullExportElement)element);				
				array.add(el);			
			}			
			return array;
		}
	}
	
	private class ExportElementSerializer implements JsonSerializer<FullExportElement> {

		@Override
		public JsonElement serialize(FullExportElement element, Type arg1, JsonSerializationContext context) {		
			JsonObject jsElement = new JsonObject();
			ExportElementType type = element.getType();			
			setType(jsElement, type);
			if (type == ExportElementType.PAGE_BREAK) {
				return jsElement;
			}			
			setId(jsElement, element);
			setDescription(jsElement, element);
			setPath(jsElement, element);
			setLinks(jsElement, element, context);
			setNumeration(jsElement, element);
			setLandscape(jsElement, element);
			setPageBreak(jsElement, element);
			setAppendix(jsElement, element);
			return jsElement;
		}
		
		private void setType(JsonObject jsElement, ExportElementType type) {
			jsElement.addProperty("type", type.toString());
		}
		
		private void setId(JsonObject jsElement, FullExportElement element) {
			jsElement.addProperty("id", element.getId());			
		}
		
		private void setDescription(JsonObject jsElement, FullExportElement element) {
			String description = element.getDescription();
			if (description != null) {
				jsElement.addProperty("description", description);
			}
		}
		
		private void setPath(JsonObject jsElement, FullExportElement element) {
			String path = element.getPath();
			if (path != null) {
				jsElement.addProperty("path", path);
			}
		}
		
		private void setLinks(JsonObject jsElement, FullExportElement element, JsonSerializationContext context) {
			List<Link> links = element.getLinks();
			if (links != null && !links.isEmpty()) {				
				JsonArray array = new JsonArray();			

				for(Link link: links) {
					array.add(context.serialize(link));
				}
				jsElement.add("links", array);
			}
		}

		private void setNumeration(JsonObject jsElement, FullExportElement element) {
			if (element.getNumber() != null) {
				jsElement.addProperty("number", element.getNumber());
			}	
		}
		
		private void setLandscape(JsonObject jsElement, FullExportElement element) {			
			if (element.isLandscape()) {
				jsElement.addProperty("landscape", true);
			}			
		}
		
		private void setPageBreak(JsonObject jsElement, FullExportElement element) {
			if (element.getType() != null 
					&& element.getType().isFolder()
					&& element.getPageBreak() != null) {
				jsElement.addProperty("pagebreak", element.getPageBreak());
			}
		}
		
		private void setAppendix(JsonObject jsElement, FullExportElement element) {			
			if (element.isAppendix()) {
				jsElement.addProperty("appendix", true);
			}			
		}
	}
	
	private class LinkSerializer implements JsonSerializer<Link> {

		@Override
		public JsonElement serialize(Link link, Type arg1, JsonSerializationContext arg2) {
			JsonObject jsElement = new JsonObject();
			if (link instanceof IncorrectLink) {
				String linkText = LinkInteractor.instance().getRef(fPreprocessor.getProject(), link.getTitle(), link.getLink());
				jsElement.addProperty("link_text", linkText);
			} else if (link instanceof CorrectLink) {
				addLinkText(link, jsElement);
				addTarget(link, jsElement);
			}
								
			return jsElement;
		}		
		
		private void addLinkText(Link link, JsonObject jsElement) {
			String linkText = LinkInteractor.instance().getRef(fPreprocessor.getProject(), link.getTitle(), link.getLink());
			jsElement.addProperty("link_text", linkText);
		}
		
		private void addTarget(Link link, JsonObject jsElement) {
			IDipElement target = ((CorrectLink)link).getTarget();
			String targetId = DipUtilities.relativeProjectID(target);
			jsElement.addProperty("link_target", targetId);
		}		
	}
}
