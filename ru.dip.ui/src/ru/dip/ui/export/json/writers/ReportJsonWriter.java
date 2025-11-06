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
import java.util.Collections;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import ru.dip.core.model.DipProject;
import ru.dip.core.model.interfaces.IDipElement;
import ru.dip.core.report.checker.ReportEntryChecker;
import ru.dip.core.report.checker.ReportRuleSyntaxException;
import ru.dip.core.report.model.report.ReportEntry;
import ru.dip.core.report.scanner.ReportReader;
import ru.dip.core.utilities.DipUtilities;
import ru.dip.core.utilities.FileUtilities;

public class ReportJsonWriter {
	
	private final DipProject fDipProject;

	public ReportJsonWriter(DipProject project) {
		fDipProject = project;
	}
		
	public void writeToJson(ReportReader reader, Path targetPath) throws IOException {
		GsonBuilder builder = new GsonBuilder();
		builder.registerTypeAdapter(ReportReader.class, new ReportSerializer());
		builder.registerTypeAdapter(ReportEntry.class, new ReportEntrySerializer());
		builder.setPrettyPrinting();	
		Gson gson = builder.create();
		String json = gson.toJson(reader);		
		FileUtilities.writeFile(targetPath, json);
	}
	
	private class ReportSerializer implements JsonSerializer<ReportReader> {

		@Override
		public JsonElement serialize(ReportReader reader, Type arg1, JsonSerializationContext arg2) {
			JsonObject jsElement = new JsonObject();
			jsElement.addProperty("name", reader.getRulesModel().getDescription());
			JsonArray array = new JsonArray();			
			for (ReportEntry entry: reader.getEntries()) {
				JsonElement el = arg2.serialize(entry);				
				array.add(el);			
			}			
			jsElement.add("entries", array);			
			return jsElement;
		}		
	}
	
	private class ReportEntrySerializer implements JsonSerializer<ReportEntry> {

		@Override
		public JsonElement serialize(ReportEntry entry, Type arg1, JsonSerializationContext arg2) {
			JsonObject jsElement = new JsonObject();
			jsElement.addProperty("name", entry.getName());
			List<IDipElement> dipElements = null;;
			try {
				dipElements = ReportEntryChecker.findEntry(entry, fDipProject);
			} catch (ReportRuleSyntaxException e) {
				dipElements = Collections.emptyList();
			}
			jsElement.addProperty("count", dipElements.size());
			JsonArray array = new JsonArray();			
			for(IDipElement unit: dipElements) {
				array.add(DipUtilities.relativeID(unit, fDipProject));
			}
			jsElement.add("elements", array);
			return jsElement;
		}		
	}

}
