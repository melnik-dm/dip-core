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
package ru.dip.core.utilities.start;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.eclipse.core.runtime.Platform;

import ru.dip.core.DipCorePlugin;
import ru.dip.core.exception.DIPException;
import ru.dip.core.exception.ExternalCommandException;
import ru.dip.core.utilities.ExternalProcessUtilities;
import ru.dip.core.utilities.FileUtilities;
import ru.dip.core.utilities.ResourcesUtilities;

public class DipStartUtilities {
		
	private static String[] GET_DESKTOP_FILE_COMMAND = {"xdg-mime","query", "default", "user/dip"};
	private static String[] CREATE_MIME_COMMAND = {"xdg-mime","install","user-dip.xml"};
	private static String[] SET_APP_FOR_MIME_COMMAND = {"xdg-mime","default", "dip.desktop", "user/dip"};

	private static final String DIP_START_PATH = "project_content" + File.separatorChar +
			"dip_start" +  File.separatorChar+ "user-dip.xml";
	private static String DEKTOP_FILE_NAME = "dip.desktop";
	
	private String fHomePath;
	private String fEclipsePath;
	
	public DipStartUtilities() {
		initProperties();
	}
	
	private void initProperties() {
		fHomePath = System.getProperty("user.home");
		fEclipsePath = Platform.getInstallLocation().getURL().getPath();
		
		fEclipsePath = ResourcesUtilities.checkWindowsPath(fEclipsePath);
		//String os = System.getProperty("os.name");
		//if (os != null && os.startsWith("Windows") && fEclipsePath.startsWith("/")){
		/*if (isWindows && fEclipsePath.startsWith("/")) {
			fEclipsePath = fEclipsePath.substring(1);
		}*/
	}
	

	public void updateDipStartSettings() {
		if (ResourcesUtilities.isWindows) {
			return;
		}
		
		boolean needUpdate = !checkDesktopFile();
		if (needUpdate) {
			updateSettings();
		}	
	}
	
	//=================================
	// check desktop file
	
	private boolean checkDesktopFile() {
		Path desktopPath = getDesktopFilePath();
		if (desktopPath == null) {
			return false;
		}	
		if (!Files.exists(desktopPath)) {
			return false;
		}		
		return containsEclipseExecPath(desktopPath);
	}	
		
	private Path getDesktopFilePath() {	
		return  Paths.get(fHomePath,".local","share", "applications", DEKTOP_FILE_NAME);
	}
	
	/*
	 * Не изпользуется, получение desktop-файлы через вызов команды
	 */
	@SuppressWarnings("unused")
	private Path getDesktopFilePathWithCommand() {
		File homeDirectory = new File(fHomePath);
		try {
			String result = ExternalProcessUtilities.runCommand(GET_DESKTOP_FILE_COMMAND, homeDirectory);
			if (result == null || result.trim().isEmpty()) {
				return null;
			}		
			return Paths.get(fHomePath,".local","share", "applications", result.trim());
		} catch (ExternalCommandException e) {
			e.printStackTrace();
			DipCorePlugin.logError("ERROR Get desktop file path command " + e.getMessage());
		}
		return null;
	}
	
	private boolean containsEclipseExecPath(Path desktopPath) {
		try {
			String ex = "Exec=" + fEclipsePath;
			List<String> lines = Files.readAllLines(desktopPath, StandardCharsets.UTF_8);
			for (String str : lines) {
				if (str.startsWith(ex)) {
					return true;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
			DipCorePlugin.logError("ERROR Read desktop file error " + e.getMessage());
		}
		return false;
	}
	
	//=================================
	// update settings
	
	private void updateSettings() {
		try {
			createDesktopFile();
			exportMimeType();
		} catch (IOException e) {
			e.printStackTrace();
			DipCorePlugin.logError("ERROR Update DIP Start Settings " + e.getMessage());
		}
	}
	
	//===================================
	// create desktop file
	
	private void createDesktopFile() throws IOException {
		Path path = getDesktopFilePath();
		try {
			FileUtilities.writeFile(path, desktopFileContent());
		} catch (IOException e) {
			DipCorePlugin.logError("ERROR Write desktop file error " + e.getMessage());
			throw e;
		}		
		File desktopFile = new File(path.toString());
		desktopFile.setExecutable(true, false);	
	}
	
	private String desktopFileContent() {
		StringBuilder builder = new StringBuilder();
		builder.append("[Desktop Entry]\nName=DIP\nExec=");
		builder.append(eclipseExecPath());
		builder.append("Icon=");
		builder.append(eclipseIconPath());
		builder.append("NoDisplay=false\n");
		builder.append("Type=Application\n");
		builder.append("Name[ru]=DIP\n");
		builder.append("MimeType=user/dip");
		return builder.toString();
	}
	
	private StringBuilder eclipseExecPath() {
		StringBuilder builder = new StringBuilder();
		Path path = Paths.get(fEclipsePath, "eclipse");	
		builder.append(path.toString());
		builder.append(" %u\n");		
		return builder;
	}
	
	private StringBuilder eclipseIconPath() {
		StringBuilder builder = new StringBuilder();
		Path path = Paths.get(fEclipsePath, "icon.xpm");	
		builder.append(path.toString());
		builder.append("\n");
		return builder;
	}
	
	//===============================
	// Create Mime-type
		
	private void  exportMimeType() throws DIPException {
		File workDirectory = new File(getDipMimeTypePath().toString());
		String result = ExternalProcessUtilities.runCommand(CREATE_MIME_COMMAND, workDirectory);
		DipCorePlugin.logInfo("exportMimeType: create mime type: " + result);
		File homeDirectory = new File(fHomePath);
		result = ExternalProcessUtilities.runCommand(SET_APP_FOR_MIME_COMMAND, homeDirectory);
		DipCorePlugin.logInfo("exportMimeType: link to application:  " + result);
	}
	
	private Path getDipMimeTypePath() throws DIPException {
		try {
			return ResourcesUtilities.getPathFromPlugin(DipCorePlugin.getDefault(), DIP_START_PATH).getParent();
		} catch (IOException e) {
			e.printStackTrace();
			throw new DIPException("Не найден файл start.dip");
		}
	}

}
