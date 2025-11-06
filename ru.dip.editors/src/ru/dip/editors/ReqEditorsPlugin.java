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
package ru.dip.editors;

import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class ReqEditorsPlugin extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "ru.dip.editors"; //$NON-NLS-1$
	
	// настройка для синхронизации вкладок с Document
	private static final String SYNHRONIZE_WITH_DOCUMENT_PREF = "_synhronize_with_document_pref";
	

	// The shared instance
	private static ReqEditorsPlugin plugin;
	
	/**
	 * The constructor
	 */
	public ReqEditorsPlugin() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static ReqEditorsPlugin getDefault() {
		return plugin;
	}

	public static boolean isDocumentSynhronize() {
		return getDefault().getPreferenceStore().getBoolean(SYNHRONIZE_WITH_DOCUMENT_PREF);
	}
	
	public static void setDocumentSynhronize() {
		getDefault().getPreferenceStore().setValue(SYNHRONIZE_WITH_DOCUMENT_PREF, !isDocumentSynhronize());
	}
	
}
