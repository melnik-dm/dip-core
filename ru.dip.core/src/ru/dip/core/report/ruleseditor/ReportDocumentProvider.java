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
package ru.dip.core.report.ruleseditor;

import java.io.InputStream;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.ui.editors.text.FileDocumentProvider;

import ru.dip.core.form.partitioner.DocumentPartitioner;

public class ReportDocumentProvider extends FileDocumentProvider {

	public ReportDocumentProvider() {
	}
	
	@Override
	public void setDocumentContent(IDocument document, InputStream contentStream, String encoding)
			throws CoreException {
		super.setDocumentContent(document, contentStream, encoding);
	}
	
	@Override
	protected IDocument createDocument(Object element) throws CoreException {
		IDocument document = super.createDocument(element);
		if (document!=null){
			IDocumentPartitioner partitioner = new DocumentPartitioner(new ReportScanner(document), ReportScanner.CONTENT_TYPES);								
			partitioner.connect(document);
			document.setDocumentPartitioner(partitioner);
		}		
		return document;
	}
}
