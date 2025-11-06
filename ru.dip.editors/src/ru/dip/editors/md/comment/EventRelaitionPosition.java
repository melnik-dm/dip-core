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
package ru.dip.editors.md.comment;

import ru.dip.core.exception.WTFException;

public enum EventRelaitionPosition {
	
	BEFORE,		// событие перед аннотацией 
	AFTER, 		// событие после аннотации
	PART_START, // затрагивает начало аннотации	
	COVER, 		// полностью покрывает аннотацию
	INLINE, 	// внутри аннотации
	PART_END;  	// затрагивает конец аннотации
	
	/**
	 * Определяет отноши
	 */
	public static EventRelaitionPosition getDocumentEventPosition(CommentAnnotation annotation, int eventStart, int eventEnd) {
		if (eventEnd < annotation.getOffset()) {
			return EventRelaitionPosition.BEFORE;
		}
		if (eventStart >= annotation.getEndOffset()) {
			return EventRelaitionPosition.AFTER;
		}
		if (eventStart <= annotation.getOffset()															
				&& eventEnd < annotation.getEndOffset()) {
			return EventRelaitionPosition.PART_START;
		}
		if (eventStart <= annotation.getOffset() && eventEnd >= annotation.getEndOffset()) {
			return EventRelaitionPosition.COVER;
		}
		if (eventStart >= annotation.getOffset() && eventEnd <= annotation.getEndOffset()) {
			return EventRelaitionPosition.INLINE;
		}
		if (eventStart > annotation.getOffset() && eventEnd >= annotation.getEndOffset()) {
			return EventRelaitionPosition.PART_END;
		}
		throw new WTFException("Ошибка определенеия EventRelaitionPosition");
	}
	
	public static boolean isCover(CommentAnnotation annotation, int eventStart, int eventEnd) {
		return eventStart <= annotation.getOffset() && eventEnd >= annotation.getEndOffset();
	}
	
}
