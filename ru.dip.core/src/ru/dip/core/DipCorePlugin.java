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

import java.io.File;
import java.util.Arrays;
import java.util.Objects;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jgit.events.RefsChangedEvent;
import org.eclipse.jgit.events.RefsChangedListener;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWindowListener;
import org.eclipse.ui.IWorkbenchPreferenceConstants;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import ru.dip.core.model.DipRoot;
import ru.dip.core.model.interfaces.IDipElement;
import ru.dip.core.utilities.GITUtilities;
import ru.dip.core.utilities.ResourcesUtilities;
import ru.dip.core.utilities.WorkbenchUtitlities;
import ru.dip.core.utilities.start.DipStartUtilities;

/**
 * The activator class controls the plug-in life cycle
 */
public class DipCorePlugin extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "ru.dip.core"; //$NON-NLS-1$
	// The shared instance
	private static DipCorePlugin plugin;
	// preferences
	public static final String CHANGE_DIP_BANNER2 = "_change_dip_banner2";
	public static final String CHANGE_DIP_BANNER3 = "_change_dip_banner3";

	public static final String DEFAULT_SCHEMA_EXTENSION = "_default_schema_extension";
	public static final String GIT_UPDATE = "_git_update";
	public static final String GIT_SUBMODULE_RECURSE = "_git_submodule_recurse";
	public static final String DISABLE_RESERVATION = "_disable_reservation";
	public static final String DISABLE_PREPROCESSING = "_disable_preprocessing";
	
	// путь до DIA (в Windows)
	public static final String DIA_PATH = "_dia_path";
	public static final String DEFAULT_DIA_PATH = "dia";
	public static final String DEFAULT_DIA_WINDOWS_PATH = "C:\\Program Files (x86)\\Dia\\bin\\dia.exe";
	
	// язык интерфейса
	public static final String LANGUAGE = "_language";		
	public static final int RUSSIAN_LANGUAGE_CODE = 0;
	public static final int ENGLISH_LANGUAGE_CODE = 1;
	public static final int DEFAULT_LANGUAGE = RUSSIAN_LANGUAGE_CODE;
	
	// шрифт (в Windows)
	public static final String MONO_FONT = "_mono_font";
	public static final String DEFAULT_MONO_FONT = "System";
	
	// путь до репозитория, который в данный момент находит в процессе обновления
	// обновления при изменениях гита, будут проигнорированы
	private String fCurrentUpdatingRepo;
	
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		(new DipStartUtilities()).updateDipStartSettings();
		getLog().addLogListener(new DipLogger());
		logInfo("START ");
		addGitListener();
		checkEclipseSplash();
	}
	
	private void addGitListener() {
		try {
			Repository.getGlobalListenerList().addRefsChangedListener(new RefsChangedListener() {

				@Override
				public void onRefsChanged(RefsChangedEvent e) {
					if (Objects.equals(e.getRepository().getDirectory().toString(), fCurrentUpdatingRepo)){
						return;
					}
					
					if (isGitSubmoduleRecurse()) {
						GITUtilities.updateSubmodules(e.getRepository());
					}
					if (getGitUpdate()) {
						File repoDir = e.getRepository().getDirectory().getParentFile();
						WorkbenchUtitlities.updateAfterGitChanges(repoDir);
					}
				}
			});
		} catch (Exception e) {
			// GIT IGNORE
			logError("GIT LISTENER NOT AVAILABLE " + e.getMessage());
		}
	}
		
	private void checkEclipseSplash() {
		if (!getPreferenceStore().getBoolean(CHANGE_DIP_BANNER2)) {
			new DipBannerChanger().shangeSplash();
			getPreferenceStore().setValue(CHANGE_DIP_BANNER2, true);						
			openPerspective("ru.dip.ui.dip.perspective", ResourcesPlugin.getWorkspace());
		}
		if (ResourcesUtilities.isWindows && !getPreferenceStore().getBoolean(CHANGE_DIP_BANNER3)) {
			logInfo("Change Banner");
			new DipBannerChanger().shangeWindowsSplash();
			getPreferenceStore().setValue(CHANGE_DIP_BANNER3, true);	
		}
	}
		
	private void openPerspective(String perspId, IAdaptable input) {
		
		// при отладке надо запускать в асинхронном потоке
        Display.getDefault().syncExec(new Runnable() {

			@Override
			public void run() {
	            IPreferenceStore store = PlatformUI.getPreferenceStore();
	            store.setValue(IWorkbenchPreferenceConstants.DEFAULT_PERSPECTIVE_ID, "ru.dip.ui.dip.perspective");
				PlatformUI.getWorkbench().addWindowListener(new IWindowListener() {
					
					@Override
					public void windowOpened(IWorkbenchWindow window) {
					}
					
					@Override
					public void windowDeactivated(IWorkbenchWindow window) {						
					}
					
					@Override
					public void windowClosed(IWorkbenchWindow window) {						
					}
					
					@Override
					public void windowActivated(IWorkbenchWindow window) {
                		try {
							PlatformUI.getWorkbench().showPerspective(perspId, PlatformUI.getWorkbench().getActiveWorkbenchWindow());
                		} catch (WorkbenchException e) {
							e.printStackTrace();
						}
						PlatformUI.getWorkbench().removeWindowListener(this);
					}
				});
			}
		});
}
	
	public void stop(BundleContext context) throws Exception {
		DipRoot.getInstance().clear();
		plugin = null;
		super.stop(context);
	}
	
	public void setCurrentRepo(String repo) {
		fCurrentUpdatingRepo = repo;
	}
	
	//=============================
	// preferences
	
	@Override
	protected void initializeDefaultPluginPreferences() {
		IPreferenceStore store = getPreferenceStore();
		store.setDefault(DEFAULT_SCHEMA_EXTENSION, getPluginDefaultSchemaExtension());
		store.setDefault(GIT_UPDATE, true);
		store.setDefault(GIT_SUBMODULE_RECURSE, true);
		store.setDefault(LANGUAGE, DEFAULT_LANGUAGE);
		if (ResourcesUtilities.isWindows) {
			store.setDefault(DIA_PATH, DEFAULT_DIA_WINDOWS_PATH);
		} else {
			store.setDefault(DIA_PATH, DEFAULT_DIA_PATH);
		}
		store.setDefault(MONO_FONT, DEFAULT_MONO_FONT);		
		store.setDefault(DISABLE_RESERVATION, true);
		store.setDefault(DISABLE_PREPROCESSING, false);
		store.setDefault(CHANGE_DIP_BANNER2, false);
		store.setDefault(CHANGE_DIP_BANNER3, false);
	}
		
	public static void setDefaultSchemaExtension(String newExtension) {
		plugin.getPreferenceStore().setValue(DEFAULT_SCHEMA_EXTENSION, newExtension);
	}
	
	public static String getDefaultSchemaExtension() {
		return plugin.getPreferenceStore().getString(DEFAULT_SCHEMA_EXTENSION);
	}
	
	public static String getPluginDefaultSchemaExtension() {
		return "req";
	}
	
	public static boolean getGitUpdate() {
		return plugin.getPreferenceStore().getBoolean(GIT_UPDATE);
	}
	
	public static void setGitUpdate(boolean newValue) {
		plugin.getPreferenceStore().setValue(GIT_UPDATE, newValue);
	}
	
	public static boolean isGitSubmoduleRecurse() {
		return plugin.getPreferenceStore().getBoolean(GIT_SUBMODULE_RECURSE);
	}
	
	public static void setGitSubmoduleRecurse(boolean newValue) {
		plugin.getPreferenceStore().setValue(GIT_SUBMODULE_RECURSE, newValue);
	}
	
	public static int getLanguage() {
		return plugin.getPreferenceStore().getInt(LANGUAGE);
	}
	
	public static void setLanguage(int value) {
		plugin.getPreferenceStore().setValue(LANGUAGE, value);
	}
	
	public static boolean isEnglish() {
		return getLanguage() == ENGLISH_LANGUAGE_CODE;
	}
	
	public static void setDiaPath(String newValue) {
		plugin.getPreferenceStore().setValue(DIA_PATH, newValue);
	}
	
	public static String getDiaPath() {
		return plugin.getPreferenceStore().getString(DIA_PATH);
	}
	
	public static void setMonoFont(String newValue) {
		plugin.getPreferenceStore().setValue(MONO_FONT, newValue);
	}
	
	public static String getMonoFont() {
		return plugin.getPreferenceStore().getString(MONO_FONT);
	}
	
	public static boolean isDisableReservation() {
		return plugin.getPreferenceStore().getBoolean(DISABLE_RESERVATION);
	}
	
	public static void setDisableReservation(boolean newValue) {
		plugin.getPreferenceStore().setValue(DISABLE_RESERVATION, newValue);
	}
	
	public static boolean isDisablePreprocessing() {
		return plugin.getPreferenceStore().getBoolean(DISABLE_PREPROCESSING);
	}
	
	public static void setDisablePreprocessing(boolean newValue) {
		plugin.getPreferenceStore().setValue(DISABLE_PREPROCESSING, newValue);
	}
	
	//========================
	// loger
	
	public static void logError(Throwable e, String message){
		if (plugin != null) {
			plugin.getLog().log(new Status(IStatus.ERROR, PLUGIN_ID, message, e));
		}
	}
	
	public static void logError(String message){
		if (plugin != null) {
			plugin.getLog().log(new Status(IStatus.ERROR, PLUGIN_ID, message));
		}
	}
	
	public static void logDeleteError(IDipElement element, String message){
		if (plugin != null) {
			plugin.getLog().log(new Status(IStatus.ERROR, PLUGIN_ID, "Delete error" + element.id() + message));
		}
	}
	
	public static void logRenameError(IDipElement element, String message){
		if (plugin != null) {
			plugin.getLog().log(new Status(IStatus.ERROR, PLUGIN_ID, "Rename error" + element.id() + message));
		}
	}
	
	public static void logInfo(String message){
		if (plugin != null) {
			plugin.getLog().log(new Status(IStatus.INFO, PLUGIN_ID, message));
		}
	}
	
	public static void logCopyError(IResource resource, String message) {
		if (plugin != null) {
			plugin.getLog().log(new Status(IStatus.ERROR, PLUGIN_ID, "Copy error" + resource.getLocation() + message));
		}
	}
	
	public static DipCorePlugin getDefault() {
		return plugin;
	}

	public static void logCopyError(Object[] items, String message) {
		if (plugin != null) {
			plugin.getLog().log(new Status(IStatus.ERROR, PLUGIN_ID, "Copy error" + Arrays.toString(items) + message));
		}
	}
}

