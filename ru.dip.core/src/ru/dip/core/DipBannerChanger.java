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
package ru.dip.core;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Optional;

import ru.dip.core.utilities.ResourcesUtilities;
import ru.dip.core.utilities.TagStringUtilities;

/*
 * Меняет заставку Eclipse
 */
public class DipBannerChanger {
	
	private static final String LAUNCHER_PATH_PROPERTY = "eclipse.launcher";
	private static final String PLUGINS_FOLDER_NAME = "plugins";
	private static final String PLATFORM_PLUGIN_NAME = "org.eclipse.platform_";
	private static final String SPLASH_FILE_NAME = "splash.bmp";
	private static final String PLUGIN_SPLASH_PATH = "project_content" + TagStringUtilities.PATH_SEPARATOR + "splash.bmp";

		
	public void shangeSplash() {
		try {
			Path source = getPluginSplashPath();
			Path target = getEclipseSplashPath(PLATFORM_PLUGIN_NAME);
			if (source != null && target != null) {			
				Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}	
	}
	
	public void shangeWindowsSplash() {
		try {
			DipCorePlugin.logInfo("Start Change Splash");
			
			Path eclipsePath = getEclipsePath();
			Path dipPluginPath = getEclipsePluginPath(eclipsePath, "dip01_");
			DipCorePlugin.logInfo("dipPluginPath: " + dipPluginPath);

			if (dipPluginPath == null) {
				return;
			}
			
			// copy splash
			copyWindowsSplash(dipPluginPath);			
			// remove files (не нужные)
			removeWindowIcons(dipPluginPath);
			// изменение конфигурации
			//changeSplashConfiguration(eclipsePath);
	
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Замена заставки для плагина DIP01
	 */
	private boolean copyWindowsSplash(Path dipPluginPath) throws IOException{
		// copy splash
		Path source = getPluginSplashPath();
		if (source == null) {
			DipCorePlugin.logInfo("copyWindowsSplash error: source null");
			return false;
		}
		
		
		Path target = getEclipseSplashPath(dipPluginPath);
		if (target == null) {
			DipCorePlugin.logInfo("copyWindowsSplash error: target null");
			return false;
		}
		
		DipCorePlugin.logInfo("source: " + source);
		DipCorePlugin.logInfo("target: " + target);			
		Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
		
		DipCorePlugin.logInfo("Copy Splash");
		return true;
	}
	
	/**
	 * Удаление ненужных иконок из плагина DIP01
	 * @throws IOException 
	 */
	private void removeWindowIcons(Path dipPluginPath) throws IOException {
		String[] removeFiles = {		
				"icons" + TagStringUtilities.PATH_SEPARATOR + "rcp.png",
				"icons" + TagStringUtilities.PATH_SEPARATOR + "languages.png",
				"icons" + TagStringUtilities.PATH_SEPARATOR + "enterprise.png",
				"icons" + TagStringUtilities.PATH_SEPARATOR + "embedded.png",
				"icons" + TagStringUtilities.PATH_SEPARATOR + "af.png"
		};
		
		for (String removeFile: removeFiles) {		
			Path removeFilePath = dipPluginPath.resolve(removeFile);
			if (Files.exists(removeFilePath)) {
				Files.delete(removeFilePath);
			}
		}	
	}
	
	
	//=====================
	// utils
	
	private Path getEclipseSplashPath(String pluginName) throws IOException {
		/*String launcherProperty = System.getProperty(LAUNCHER_PATH_PROPERTY);
		if (launcherProperty == null) {
			return null;
		}							
		Path launcherPath = Paths.get(launcherProperty);
		if (!Files.exists(launcherPath)) {
			return null;
		}	
		Path pluginsPath = launcherPath.getParent().resolve(PLUGINS_FOLDER_NAME);
		if (!Files.exists(pluginsPath)) {
			return null;
		}
		Optional<Path> platformPlugin = Files.find(pluginsPath, 1, (p, a) -> p.getFileName().toString().startsWith(pluginName)).findFirst();
		*/
		
		Path eclipsePath = getEclipsePath();
		Path platformPluginPath = getEclipsePluginPath(eclipsePath, pluginName);
		return getEclipseSplashPath(platformPluginPath);
	}
	
	private Path getEclipseSplashPath(Path platformPluginPath) throws IOException {		
		if (platformPluginPath != null) {
			Path splashPath = platformPluginPath.resolve(SPLASH_FILE_NAME);
			if (Files.exists(splashPath)) {
				return splashPath;
			}			
		}
		return null;
	}
	
	private Path getEclipsePluginPath(Path eclipsePath, String pluginName) throws IOException {
		/*String launcherProperty = System.getProperty(LAUNCHER_PATH_PROPERTY);
		if (launcherProperty == null) {
			return null;
		}							
		Path launcherPath = Paths.get(launcherProperty);
		if (!Files.exists(launcherPath)) {
			return null;
		}	*/
		Path pluginsPath = eclipsePath.resolve(PLUGINS_FOLDER_NAME);
		if (!Files.exists(pluginsPath)) {
			return null;
		}
		Optional<Path> platformPlugin = Files.find(pluginsPath, 1, (p, a) -> p.getFileName().toString().startsWith(pluginName)).findFirst();
		
		if (platformPlugin.isPresent()) {
			return platformPlugin.get();
		}
		return null;
	}
	
	private Path getEclipsePath() {
		String launcherProperty = System.getProperty(LAUNCHER_PATH_PROPERTY);
		if (launcherProperty == null) {
			return null;
		}							
		Path launcherPath = Paths.get(launcherProperty);
		if (!Files.exists(launcherPath)) {
			return null;
		}	
		return launcherPath.getParent();	
	}
	
	
	private Path getPluginSplashPath() throws IOException {
		 return ResourcesUtilities.getPathFromPlugin(DipCorePlugin.getDefault(), PLUGIN_SPLASH_PATH);		
	}
	
}
