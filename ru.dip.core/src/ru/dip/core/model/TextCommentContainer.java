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
package ru.dip.core.model;

import java.util.List;

import org.eclipse.core.resources.IFile;

import ru.dip.core.model.interfaces.IDipComment;
import ru.dip.core.model.interfaces.IDipDocumentElement;
import ru.dip.core.model.interfaces.ITextComment;

/**
 * Содержит только текстовые комментарии из md-файла
 */
public class TextCommentContainer implements IDipComment {

	private List<ITextComment> fTextComments;
	private IDipDocumentElement fDipDocumentElement;
	
	public TextCommentContainer(IDipDocumentElement dipDocumentElement, List<ITextComment> textComments) {
		fTextComments = textComments;
		fDipDocumentElement = dipDocumentElement;
	}

	@Override
	public IDipDocumentElement getDipDocumentElement() {
		return fDipDocumentElement;
	}

	@Override
	public String getFullContent() {
		StringBuilder builder = new StringBuilder();
		String mainComment = getCommentContent();
		if (mainComment != null && !mainComment.isBlank()) {
			builder.append(mainComment);
		}				
		if (fTextComments != null && !fTextComments.isEmpty()) {
			if (builder.length() > 0) {
				builder.append("\n");
			}
			builder.append(getTextCommentsContent());
		}
		return builder.toString();
	}
	
	@Override
	public String getCommentContent() {
		return null;
	}

	@Override
	public List<ITextComment> getTextComments() {
		return fTextComments;
	}

	@Override
	public String getTextCommentsContent() {
		StringBuilder builder = new StringBuilder();
		fTextComments.forEach(textComment -> {
			if (builder.length() > 0) {
				builder.append("\n\n");
			}				
			builder.append(textComment.getContent());				
		});	
		return builder.toString();
	}
	
	@Override
	public boolean hasTextComments() {
		return fTextComments != null && !fTextComments.isEmpty();
	}

	@Override
	public boolean hasCommentContent() {
		return false;
	}

	@Override
	public boolean isEmpty() {
		return fTextComments == null || fTextComments.isEmpty();
	}
	
	@Override
	public void delete() {
		
	}

	@Override
	public void deleteMainContent() {
		
	}

	@Override
	public IFile resource() {
		return null;
	}

	@Override
	public void save() {
		
	}

}
