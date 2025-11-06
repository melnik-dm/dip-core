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

import java.util.Map;

public final class ContentContainer {

	private final Map<ContentId, Content> fContentById = Map.of(
			ContentId.ID, new Content(), 
			ContentId.PRESENTATION,	new Content(), 
			ContentId.COMMENT, new Content());

	public <T> T get(ContentId id, ContentType type, Class<T> className) {
		Content content = fContentById.get(id);
		return content.get(type, className);
	}

	public int getInt(ContentId id, ContentType type) {
		Content content = fContentById.get(id);
		return content.getInt(id, type);
	}

	public void put(ContentId id, ContentType type, Object object) {
		Content content = fContentById.get(id);
		content.put(type, object);
	}

}
