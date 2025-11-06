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
import java.nio.file.Path;
import java.nio.file.Paths;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Platform;
import org.osgi.framework.Bundle;

import ru.dip.core.DipCorePlugin;

public class ExportScriptProvider {

	private String fDocxExportScript;
	private String fOdtExportScript;
	
	private static ExportScriptProvider instance;
	
	public static ExportScriptProvider getInstance() {
		if (instance == null) {
			instance = new ExportScriptProvider();
		}
		return instance;
	}
	
	private ExportScriptProvider() {
		try {
			copyScriptFiles();
		} catch (IOException e) {
			DipCorePlugin.logError(e, "Ошибка при копировании скриптов экспорта");
			WorkbenchUtitlities.openError("Export error", "Ошибка при копировании скриптов экспорта");
		}
	}
		
	private void copyScriptFiles() throws IOException{
		Bundle bundle = DipCorePlugin.getDefault().getBundle();
		IPath pathMetaData = Platform.getStateLocation(bundle);
		String metaData = pathMetaData.toOSString();
		Path scriptPathFolder = ResourcesUtilities.getPathFromPlugin(DipCorePlugin.getDefault(),
				"project_content/export");
		Path newScriptPathFolder = Paths.get(metaData, "export");
		ResourcesUtilities.copyFolder(scriptPathFolder, newScriptPathFolder);

		Path docxScript = newScriptPathFolder.resolve("docx");		
		setDocxScript(docxScript);
		
		Path odtScript = newScriptPathFolder.resolve("odt");
		setOdtScript(odtScript);
	}
	
	private void setDocxScript(Path sciptPath){
		Path path = sciptPath.resolve("dip_docx.py");
		fDocxExportScript = path.toString();
		File commandFile = new File(fDocxExportScript);
		if (!commandFile.canExecute()) {
			commandFile.setExecutable(true);
		}	
	}
	
	private void setOdtScript(Path sciptPath){
		Path path = sciptPath.resolve("dip_odt.py");
		fOdtExportScript = path.toString();
		File commandFile = new File(fOdtExportScript);
		if (!commandFile.canExecute()) {
			commandFile.setExecutable(true);
		}	
	}
	
	public String getOdtPath() {
		return fOdtExportScript;
	}
	
	public String getDocxPath() {
		return fDocxExportScript;
	}
	
	public String getExec(boolean docx){
		if (docx){
			return fDocxExportScript;			
		} else {
			return fOdtExportScript;
		}
	}
	
}
