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
package ru.dip.ui.table.ktable.model;

import java.util.HashMap;
import java.util.Map;

public final class Content {
	
	private final Map<ContentType, Object> fContent = new HashMap<>();
	
	@SuppressWarnings("unchecked")
	public <T> T get(ContentType type, Class<T> className){
		Object obj = fContent.get(type);
		if (obj == null) {
			return null;
		}
		if (obj.getClass() == className) {
			return (T) obj;
		}
		return null;
	}
	
	public int getInt(ContentId id, ContentType type) {
		Object obj = fContent.get(type);
		if (obj instanceof Integer) {
			return (int) obj;
		}		
		return 0;
	}
	
	public void put(ContentType type, Object object) {
		fContent.put(type, object);
	}
}
