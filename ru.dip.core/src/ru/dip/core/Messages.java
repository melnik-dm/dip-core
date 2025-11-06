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

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "ru.dip.core.messages"; //$NON-NLS-1$
	private  static final String RU_BUNDLE_NAME = "ru.dip.core.ru_messages"; //$NON-NLS-1$
	
	public static String Errors_CannotCreateFileInAppendixMessage;
	public static String Errors_DestianationFolderReadonlyMessage;
	public static String Errors_ErrorWithDeleteObjsMessage;
	public static String Errors_FileAlreadyExists;
	public static String Errors_FileNameReservedMessage;
	public static String Errors_FileNotExistsMessage;
	public static String Errors_FolderAlreadyExistsMessage;
	public static String Errors_FolderNameReservedMessage;
	public static String Errors_FolderNotExistsMessage;
	public static String Errors_IllegalCopingReservedObjsMessage;
	public static String Errors_IllegalMovingMessage;
	public static String Errors_IncorrectElementTypeMessage;
	public static String Errors_InputObjectName;
	public static String Errors_InvalidDestinationFolderMessage;
	public static String Errors_InvalidParentFolder;
	public static String Errors_IsNotFolderMessage;
	public static String Errors_MaxNestedLevel;
	public static String Errors_NoSelectedObjectForMovingMessage;
	public static String Errors_NotBeHide;
	public static String Errors_OnlyEnglishChars;
	public static String Errors_ProjectAlreadyExistsMessage;
	public static String FilterValidator_InvalidConditionMessage;
	
	static {
		// initialize resource bundle
		if (DipCorePlugin.getLanguage() == 0) {
			NLS.initializeMessages(RU_BUNDLE_NAME, Messages.class);
		} else {
			NLS.initializeMessages(BUNDLE_NAME, Messages.class);
		}

	}

	private Messages() {
	}
}
