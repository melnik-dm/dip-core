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
package ru.dip.editors.csv;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.part.FileEditorInput;

public class CsvTextEditor extends TextEditor implements IDocumentListener {
	
	private IFile fFile;
	private IDocument fDocument;
	private boolean fTableDirty;


	@Override
	protected void doSetInput(IEditorInput input) throws CoreException {
		super.doSetInput(input);
		if (input instanceof FileEditorInput){
			FileEditorInput fileEditorInput = (FileEditorInput) input;
			fFile = fileEditorInput.getFile();
			fDocument = getDocumentProvider().getDocument(input);
			fDocument.addDocumentListener(this);
		}
	}
	
	public IFile getFile(){
		return fFile;
	}
	
	public IDocument getDocument(){
		return fDocument;
	}

	@Override
	public void documentAboutToBeChanged(DocumentEvent event) {
	}

	@Override
	public void documentChanged(DocumentEvent event) {
		setTableDirty(true);
	}
	
	
	public boolean isTableDirty() {
		return fTableDirty;
	}
	
	public void setTableDirty(boolean newValue){
		fTableDirty = newValue;
	}
	
}
