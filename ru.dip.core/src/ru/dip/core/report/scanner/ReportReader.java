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
package ru.dip.core.report.scanner;

import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;

import ru.dip.core.form.partitioner.DocumentPartitioner;
import ru.dip.core.form.partitioner.PartitionScanner;
import ru.dip.core.report.model.report.ReportEntry;
import ru.dip.core.report.model.report.RulesModel;
import ru.dip.core.report.ruleseditor.ReportDocumentProvider;

public class ReportReader {

	private IFile fFile;
	private IDocument fDocument;
	private ReportDocumentProvider fDocumentProvider;
	private DocumentPartitioner fPartitioner;
	private RulesModel fModel;

	public ReportReader(IFile file) {
		fFile = file;
	}
	
	public void read(){
		fDocument = new Document();
		fDocumentProvider = new ReportDocumentProvider();
		try {
			fDocumentProvider.setDocumentContent(fDocument, fFile.getContents(), null);
		} catch (CoreException e) {
			e.printStackTrace();
			return;
		}
		fPartitioner = new DocumentPartitioner(new PartitionScanner(), PartitionScanner.CONTENT_TYPES);
		fPartitioner.connect(fDocument);
		fDocument.setDocumentPartitioner(fPartitioner);
		fModel = new RulesModel(fDocument);
		fModel.createModel();	
	}
	
	public List<ReportEntry> getEntries(){
		return fModel.getEntries();
	}
	
	public RulesModel getRulesModel() {
		return fModel;
	}	
}
