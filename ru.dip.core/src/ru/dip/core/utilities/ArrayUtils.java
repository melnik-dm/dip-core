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
package ru.dip.core.utilities;

import java.util.Arrays;

public class ArrayUtils {
	
	public static boolean arrayContainsElement(Object[] array, Object element){
		for (Object obj: array){
			if (element.equals(obj)){
				return true;
			}
		}
		return false;
	}
	
	public static int getIndex(Object[] array, Object element) {
		if (element == null) {
			return -1;
		}
		
		for (int i = 0; i < array.length; i++) {
			if (element.equals(array[i])) {
				return i;
			}
		}
		return -1;
	}
	
	public static String[] addElement(String[] array, String element){	
		String[] result = Arrays.copyOf(array, array.length + 1);
		result[array.length] = element;
		return result;		
	}

}
