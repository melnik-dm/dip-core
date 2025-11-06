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
import java.nio.file.Path;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import ru.dip.core.utilities.FileUtilities;
import ru.dip.ui.export.ExportPreprocessor;
import ru.dip.ui.export.TocEntry;

public class TocJsonWriter {
	
	public void writeToJson(ExportPreprocessor preprocessor, Path targetPath) throws IOException {
		GsonBuilder builder = new GsonBuilder();
		builder.registerTypeAdapter(ExportPreprocessor.class, new TocElementSerializer());
		builder.registerTypeAdapter(TocEntry.class, new TocEntrySerializer());
		builder.setPrettyPrinting();	
		Gson gson = builder.create();
		String json = gson.toJson(preprocessor);		
		FileUtilities.writeFile(targetPath, json);
	}
	
	private class TocElementSerializer implements JsonSerializer<ExportPreprocessor> {
		
		@Override
		public JsonElement serialize(ExportPreprocessor preprocessor, Type arg1, JsonSerializationContext arg2) {
			JsonArray array = new JsonArray();			
			for (TocEntry tocEntry: preprocessor.getTocEntries()) {
				JsonElement el = arg2.serialize(tocEntry);				
				array.add(el);			
			}			
			return array;
		}
	}	
	
	private class TocEntrySerializer implements JsonSerializer<TocEntry> {

		@Override
		public JsonElement serialize(TocEntry tocEntry, Type arg1, JsonSerializationContext context) {	
			JsonObject jsElement = new JsonObject();
			setNumber(jsElement, tocEntry);
			setDescription(jsElement, tocEntry);
			setLinkId(jsElement, tocEntry);
			setAppendix(jsElement, tocEntry);
			return jsElement;
		}
		
		private void setNumber(JsonObject jsElement, TocEntry tocEntry) {
			String number = tocEntry.getNumber();		
			if (number != null) {
				jsElement.addProperty("number", number);
			}
		}
	
		private void setDescription(JsonObject jsElement, TocEntry tocEntry) {
			String description = tocEntry.getDescription();
			if (description != null) {
				jsElement.addProperty("description", description);
			}
		}
		
		private void setLinkId(JsonObject jsElement, TocEntry tocEntry) {
			jsElement.addProperty("link_id", tocEntry.getLinkId());			
		}
		
		private void setAppendix(JsonObject jsElement, TocEntry tocEntry) {			
			if (tocEntry.isAppendix()) {
				jsElement.addProperty("appendix", true);
			}			
		}
	}

}
