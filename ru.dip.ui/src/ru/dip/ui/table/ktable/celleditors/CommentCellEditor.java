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
package ru.dip.ui.table.ktable.celleditors;

import org.eclipse.swt.graphics.Font;

import ru.dip.core.model.interfaces.IDipComment;
import ru.dip.ui.table.ktable.model.DipTableModel;
import ru.dip.ui.table.ktable.model.IDipTableElement;
import ru.dip.ui.table.table.TableSettings;

public final class CommentCellEditor extends SimpleCellEditor {
	
	public CommentCellEditor(DipTableModel model) {
		super(model);
	}
	
	protected String getEditorContent() {
		Object content = fModel.getContentAt(m_Col, m_Row);
		if (content instanceof IDipTableElement) {
			fElement = (IDipTableElement) content;
			fDipDocElement = fElement.dipDocElement();
			IDipComment comment = fDipDocElement.comment();
			if (comment != null) {
				return comment.getCommentContent();
			} 
		}
		return "";
	}
	
	@Override
	protected Font getFont() {
		return TableSettings.commentFont();
	}
	
	@Override
	public void save() {
		String newComment = fText.getText();
		fModel.tableComposite().updateCommentFromCellEditor(fElement, fDipDocElement, newComment);	
	}
}