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

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import ru.dip.core.DipCorePlugin;

public class StatusUtils {
	
	// info
	public static final DipStatus NO_NAME = new DipStatus(IStatus.INFO, Errors.NO_NAME);	
	// errors
	public static final DipStatus CAN_NOT_CREATE_FILE_IN_APPENDIX = errorStatus(Errors.CAN_NOT_CREATE_FILE_IN_APPENDIX);
	public static final DipStatus FILE_ALREADY_EXISTS = errorStatus(Errors.FILE_ALREADY_EXISTS);
	public static final DipStatus FOLDER_ALREADY_EXISTS = errorStatus(Errors.FOLDER_ALREADY_EXISTS);
	public static final DipStatus INVALID_FOLDER = errorStatus(Errors.INVALID_FOLDER);
	public static final DipStatus INVALID_NAME = errorStatus(Errors.INVALID_NAME);
	public static final DipStatus INVALID_PARENT_FODLER = errorStatus(Errors.INVALID_PARENT_FODLER);
	public static final DipStatus INVALID_TARGET_FOLDER = errorStatus(Errors.INVALID_TARGET_FOLDER);
	public static final DipStatus INVALID_TYPE = errorStatus(Errors.INVALID_TYPE);
	public static final DipStatus HIDE_NAME = errorStatus(Errors.HIDE_NAME);
	public static final DipStatus MAX_NESTING = errorStatus(Errors.MAX_NESTING);
	public static final DipStatus MOVED_RESERVED_OBJECT = errorStatus(Errors.MOVED_RESERVED_OBJECT);
	public static final DipStatus NOT_FILE_EXISTS = errorStatus(Errors.NOT_FILE_EXISTS);
	public static final DipStatus NOT_FOLDER_EXISTS = errorStatus(Errors.NOT_FOLDER_EXISTS);
	public static final DipStatus NOT_MOVED_OBJ = errorStatus(Errors.NOT_MOVED_OBJ);
	public static final DipStatus PROJECT_ALREADY_EXISTS = errorStatus(Errors.PROJECT_ALREADY_EXISTS);
	public static final DipStatus READ_ONLY_MOVED_OBJ = errorStatus(Errors.READ_ONLY_MOVED_OBJ);
	public static final DipStatus READ_ONLY_TARGET_FOLDER = errorStatus(Errors.READ_ONLY_TARGET_FOLDER);
	public static final DipStatus RESERVED_FOLDER = errorStatus(Errors.RESERVED_FOLDER);
	public static final DipStatus RESERVED_FILE = new DipStatus(IStatus.ERROR, Errors.RESERVED_FILE);
	// warnings
	public static final DipStatus WARNING_RESERVED_FOLDER = new DipStatus(IStatus.WARNING, Errors.RESERVED_FOLDER);


	public static DipStatus errorStatus(int code) {
		return new DipStatus(IStatus.ERROR, code);
	}
	
	public static Status errorStatus(Throwable e) {		
		return new Status(Status.ERROR, DipCorePlugin.PLUGIN_ID, e.getMessage());		
	}

}
