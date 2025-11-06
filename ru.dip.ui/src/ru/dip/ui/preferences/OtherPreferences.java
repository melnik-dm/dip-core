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
package ru.dip.ui.preferences;

import org.eclipse.jface.preference.IPreferenceStore;

import ru.dip.core.utilities.WorkbenchUtitlities;
import ru.dip.ui.ReqUIPlugin;

public class OtherPreferences {

	private static final String COMPARE_EDITOR_SHOW_DESC_PREF = "_compare_editor_show_desc_pref";
	public static final boolean COMPARE_EDITOR_SHOW_DESC_DEFAULT = true;
	
	private static final String PROJECT_EXPLORER_SHOW_DESC_PREF = "_project_explorer_show_desc_pref";
	public static final boolean PROJECT_EXPLORER_SHOW_DESC_DEFAULT = true;
	
	private static OtherPreferences instance;

	public static void setDefaultValues(IPreferenceStore preferenceStore) {
		preferenceStore.setDefault(COMPARE_EDITOR_SHOW_DESC_PREF, COMPARE_EDITOR_SHOW_DESC_DEFAULT);
		preferenceStore.setDefault(PROJECT_EXPLORER_SHOW_DESC_PREF, PROJECT_EXPLORER_SHOW_DESC_DEFAULT);
	}

	public static void init() {
		instance = new OtherPreferences();
		instance.setValues();
	}

	public static OtherPreferences instance() {
		if (instance == null) {
			init();
		}
		return instance;
	}

	public static boolean isShowDescInCompareEditor() {
		return instance().fCompareEditorShowDesc;
	}

	public static void updateCompareEditorShowDesc(boolean showDescInCompareEditor) {
		instance().fCompareEditorShowDesc = showDescInCompareEditor;
		getStore().setValue(COMPARE_EDITOR_SHOW_DESC_PREF, showDescInCompareEditor);
	}
	
	public static boolean isShowDescInProjectExplorer() {
		return instance().fProjectExplorerShowDesc;
	}
	
	public static void updateProjectExplorerShowDesc(boolean showDescInProjectExplorer) {
		instance().fProjectExplorerShowDesc = showDescInProjectExplorer;
		getStore().setValue(PROJECT_EXPLORER_SHOW_DESC_PREF, showDescInProjectExplorer);
		WorkbenchUtitlities.getProjectExplorer().getCommonViewer().refresh();
	}
	
	private static IPreferenceStore getStore() {
		return ReqUIPlugin.getDefault().getPreferenceStore();
	}

	private boolean fCompareEditorShowDesc;
	private boolean fProjectExplorerShowDesc;

	private OtherPreferences() {}

	private void setValues() {
		IPreferenceStore store = getStore();
		fCompareEditorShowDesc = store.getBoolean(COMPARE_EDITOR_SHOW_DESC_PREF);
		fProjectExplorerShowDesc = store.getBoolean(PROJECT_EXPLORER_SHOW_DESC_PREF);
	}
	
}
