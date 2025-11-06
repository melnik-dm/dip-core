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
package ru.dip.ui.utilities.xml;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.ui.editors.text.FileDocumentProvider;

import ru.dip.core.form.partitioner.DocumentPartitioner;
import ru.dip.core.form.partitioner.PartitionScanner;

public class XmlDocumentProvider  extends FileDocumentProvider {

	@Override
	protected IDocument createDocument(Object element) throws CoreException {
		IDocument document = super.createDocument(element);
		if (document!=null){
			IDocumentPartitioner partitioner = new DocumentPartitioner(new PartitionScanner(), PartitionScanner.CONTENT_TYPES);								
			partitioner.connect(document);
			document.setDocumentPartitioner(partitioner);
		}		
		return document;
	}
	
}