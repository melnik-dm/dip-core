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
package ru.dip.editors.md;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.ui.editors.text.FileDocumentProvider;

import ru.dip.editors.md.partitions.MdPartitionScanner;
import ru.dip.editors.md.partitions.PartitionStyles;

public class MDDocumentProvider extends FileDocumentProvider {

	private final MDEditor fEditor;
	
	public MDDocumentProvider(MDEditor editor) {
		fEditor = editor;
	}
	
	@Override
	protected IDocument createDocument(Object element) throws CoreException {
		IDocument document = super.createDocument(element);
		if (document != null) {
			IDocumentPartitioner partitioner = new DocumentPartitioner(new MdPartitionScanner(),
					new String[] { PartitionStyles.COMMENT, 
							PartitionStyles.EMPTY_LINE, 
							PartitionStyles.PARAGRAPH, 
							PartitionStyles.NUMBER_LIST_ITEM,
							PartitionStyles.GRAPHIC_LIST_ITEM,
							PartitionStyles.CODE });
			partitioner.connect(document);
			document.setDocumentPartitioner(partitioner);
		}
		return document;
	}
	
	/**
	 * Сохраняем комментарии в документ
	 */
	@Override
	protected void doSaveDocument(IProgressMonitor monitor, Object element, IDocument document, boolean overwrite)
			throws CoreException {		
		if (fEditor.getCommentManager().hasComments()) {
			String contentWithComments = fEditor.getCommentManager().saveCommentsContent();
			IDocument docuemtnWithComments = new Document(contentWithComments);				
			super.doSaveDocument(monitor, element, docuemtnWithComments, overwrite);
		} else {
			super.doSaveDocument(monitor, element, document, overwrite);
		}		
	}

}
