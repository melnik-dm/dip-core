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
package ru.dip.table;

import org.eclipse.osgi.util.NLS;

import ru.dip.core.DipCorePlugin;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "ru.dip.table.messages"; //$NON-NLS-1$
	private  static final String RU_BUNDLE_NAME = "ru.dip.table.ru_messages"; //$NON-NLS-1$
	
	public static String MultiPageTableEditor_ID;
	public static String MultiPageTableEditor_RawTextTitle;
	public static String MultiPageTableEditor_TablePageName;
	public static String MultiPageTableEditor_TablePageTitle;
	
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
		if (DipCorePlugin.getLanguage() == 0) {
			NLS.initializeMessages(RU_BUNDLE_NAME, Messages.class);
		} else {
			NLS.initializeMessages(BUNDLE_NAME, Messages.class);
		}
	}

	private Messages() {
	}
}
