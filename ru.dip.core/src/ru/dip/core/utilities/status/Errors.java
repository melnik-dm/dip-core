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
package ru.dip.core.utilities.status;

import java.util.HashMap;
import java.util.Map;

import ru.dip.core.Messages;

public class Errors {
	
	//===============================
	// ошибки создания, перемещения
	
	public static Map<Integer, String> messages = new HashMap<>();
	private static int  counter = 2000;
	
	public static final int NO_NAME = counter++;
	static {
		messages.put(NO_NAME, Messages.Errors_InputObjectName);
	}
			
	public static final int INVALID_NAME = counter++;
	static {
		messages.put(INVALID_NAME, Messages.Errors_OnlyEnglishChars);
	}
	
	public static final int HIDE_NAME = counter++;
	static {
		messages.put(HIDE_NAME, Messages.Errors_NotBeHide);
	}
	
	public static final int INVALID_PARENT_FODLER = counter++;
	static {
		messages.put(INVALID_PARENT_FODLER, Messages.Errors_InvalidParentFolder);
	}
	
	public static final int MAX_NESTING = counter++;
	static {
		messages.put(MAX_NESTING, Messages.Errors_MaxNestedLevel);
	}
	
	public static final int FILE_ALREADY_EXISTS = counter++;
	static {
		messages.put(FILE_ALREADY_EXISTS, Messages.Errors_FileAlreadyExists);
	}	
	
	public static final int FOLDER_ALREADY_EXISTS = counter++;
	static {
		messages.put(FOLDER_ALREADY_EXISTS, Messages.Errors_FolderAlreadyExistsMessage);
	}
	
	public static final int RESERVED_FOLDER = counter++;
	static {
		messages.put(RESERVED_FOLDER, Messages.Errors_FolderNameReservedMessage);
	}	
	
	public static final int RESERVED_FILE = counter++;
	static {
		messages.put(RESERVED_FILE, Messages.Errors_FileNameReservedMessage);
	}	
	
	public static final int INVALID_TYPE = counter++;
	static {
		messages.put(INVALID_TYPE, Messages.Errors_IncorrectElementTypeMessage);
	}	
	
	public static final int INVALID_TARGET_FOLDER = counter++;
	static {
		messages.put(INVALID_TARGET_FOLDER, Messages.Errors_InvalidDestinationFolderMessage);
	}
	
	public static final int READ_ONLY_TARGET_FOLDER = counter++;
	static {
		messages.put(READ_ONLY_TARGET_FOLDER, Messages.Errors_DestianationFolderReadonlyMessage);
	}
	
	public static final int NOT_MOVED_OBJ = counter++;
	static {
		messages.put(NOT_MOVED_OBJ, Messages.Errors_NoSelectedObjectForMovingMessage);
	}
	
	public static final int READ_ONLY_MOVED_OBJ = counter++;
	static {
		messages.put(READ_ONLY_MOVED_OBJ, Messages.Errors_IllegalMovingMessage);
	}
	
	public static final int MOVED_RESERVED_OBJECT = counter++;
	static {
		messages.put(MOVED_RESERVED_OBJECT, Messages.Errors_IllegalCopingReservedObjsMessage);
	}
	
	public static final int PROJECT_ALREADY_EXISTS = counter++;
	static {
		messages.put(PROJECT_ALREADY_EXISTS, Messages.Errors_ProjectAlreadyExistsMessage);
	}
	
	public static final int NOT_FOLDER_EXISTS = counter++;
	static {
		messages.put(NOT_FOLDER_EXISTS, Messages.Errors_FolderNotExistsMessage);
	}
	
	public static final int INVALID_FOLDER = counter++;
	static {
		messages.put(INVALID_FOLDER, Messages.Errors_IsNotFolderMessage);
	}
	
	public static final int NOT_FILE_EXISTS = counter++;
	static {
		messages.put(NOT_FILE_EXISTS, Messages.Errors_FileNotExistsMessage);
	}
	
	public static final int CAN_NOT_CREATE_FILE_IN_APPENDIX = counter++;
	static {
		messages.put(CAN_NOT_CREATE_FILE_IN_APPENDIX, Messages.Errors_CannotCreateFileInAppendixMessage);
	}

	//====================================
	// Ошибки при удалении
	
	public static final int DELETE_ERROR = counter++;
	static {
		messages.put(DELETE_ERROR, Messages.Errors_ErrorWithDeleteObjsMessage);
	}
	
}
