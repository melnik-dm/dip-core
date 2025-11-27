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
package ru.dip.core.model.interfaces;

import ru.dip.core.model.finder.FindSettings;

public interface IEmptyResultFindable extends IFindable {
	
	@Override
	default boolean appendFind(String text, boolean caseSensitive) {
		return false;
	}
	
	@Override
	default boolean findWord(String text, boolean caseSensitive) {		
		return false;
	}
	
	@Override
	default boolean appendWord(String text, boolean caseSensitive) {
		return false;
	}
	
	@Override
	default int findText(String text, FindSettings findSettings) {
		return 0;
	}
	
	@Override
	default void cleanFind() {
		
	}
	
}
