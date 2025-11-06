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

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;

public final class ContentProvider {

	public static final ContentProvider PRESENTATION_CONTENT_PROVIDER = new ContentProvider(ContentId.PRESENTATION);
	public static final ContentProvider ID_CONTENT_PROVIDER = new ContentProvider(ContentId.ID);
	public static final ContentProvider COMMENT_CONTENT_PROVIDER = new ContentProvider(ContentId.COMMENT);
	
	private final ContentId fContentId;
	
	public ContentProvider(ContentId contentId) {
		fContentId = contentId;
	}
	
	public <T> T get(IContentContainer container, ContentType type, Class<T> className){
		return container.get(fContentId, type, className);
	}
	
	public void put(IContentContainer container, ContentType type, Object object) {
		container.put(fContentId, type, object);
	}
	
	public String getText(IContentContainer container) {
		return get(container, ContentType.TEXT, String.class); 
	}
	
	public void setText(IContentContainer container, String text) {
		put(container, ContentType.TEXT, text); 
	}

	public void setHeight(IContentContainer container, int height) {
		put(container, ContentType.HEIGHT, height); 
		
	}
	
	public void setFont(IContentContainer container, Font font) {
		put(container, ContentType.FONT, font); 
	}

	public void setImage(IContentContainer container, Image image) {
		put(container, ContentType.IMAGE, image);
	}

	public Color getForeground(IContentContainer container) {
		return get(container, ContentType.FOREGRAOUND, Color.class);
	}

	public void setForeground(IContentContainer container, Color color) {
		put(container, ContentType.FOREGRAOUND, color);		
	}
	
}
