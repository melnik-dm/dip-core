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

import java.util.List;

import org.eclipse.core.resources.IFile;

public interface IDipComment {
	
	IDipDocumentElement getDipDocumentElement();
	
	/**
	 * Основной комментарий + дополнителльные комментарии по тексту
	 */
	String getFullContent();
	
	/**
	 * Основной комментарий
	 */
	String getCommentContent();

	/**
	 *  Дополнительные комментарии по тексту
	 */
	List<ITextComment> getTextComments();
	
	String getTextCommentsContent();
	
	boolean hasTextComments();
	
	boolean hasCommentContent();
	
	boolean isEmpty();
	
	void delete();
	
	void deleteMainContent();
	
	IFile resource();
	
	void save();

}
