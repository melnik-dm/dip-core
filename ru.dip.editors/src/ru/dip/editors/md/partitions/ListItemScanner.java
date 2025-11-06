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
package ru.dip.editors.md.partitions;

import java.util.List;

import org.eclipse.jface.text.IDocument;

import ru.dip.core.model.DipProject;
import ru.dip.core.utilities.md.MarkdownParagraphParser;
import ru.dip.core.utilities.md.MarkdownParagraphParser.MdStyledPosition;

public class ListItemScanner extends ParagraphScanner {
	
	public ListItemScanner(DipProject dipProject, IDocument document) {
		super(dipProject, document);
	}

	@Override
	protected List<MdStyledPosition> computePositions(String content) {
		return MarkdownParagraphParser.getListItemPositions(content);
	}
	
}
