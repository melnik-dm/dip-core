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
package ru.dip.ui.export;

public enum ExportVersion {

	JAVA, PYTHON, BOTH;
	
	public final static String PYTHON_VERSION = "Python Version";
	public final static String JAVA_VERSION = "Java Version";
	
	public static int JAVA_VER_INDEX = JAVA.ordinal();
	public static int PYTHON_VER_INDEX = PYTHON.ordinal();
	
}
