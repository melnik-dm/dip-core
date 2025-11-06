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
package ru.dip.ui.table;

import java.nio.file.Path;
import java.util.List;

import ru.dip.core.model.DipProject;
import ru.dip.ui.export.ExportElement;
import ru.dip.ui.export.ExportPreprocessor;
import ru.dip.ui.export.diff.DiffExportPreprocessor;
import ru.dip.ui.export.error.IExportError;

public class ExporterHolder {
	
	private static ExporterHolder instance;
	
	public static ExporterHolder instance() {
		if (instance == null) {
			instance = new ExporterHolder();
		}
		return instance;
	}
	
	public static interface IExporter {
		
		/**
		 * Возвращает путь до файла с результатом
		 */
		String export(DipProject project, Path partsPath, Path configPath, ExportPreprocessor preprocessor, int files) throws Exception;
		
		String diffExport(DipProject project, Path partsPath, Path configPath, DiffExportPreprocessor preprocessor) throws Exception;
		
		String convertHtml(String output, ExportElement unit);

		void createGlossary(String output, DipProject project);

		List<IExportError> getExportErrors();	
	}
	
	private IExporter fExporter;
	
	public  IExporter getExporter() {
		return fExporter;
	}
	
	public void setExporter(IExporter exporter) {
		fExporter = exporter;
	}
	
}
