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
package ru.dip.table.editor.text;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.part.FileEditorInput;

import ru.dip.table.editor.MultiPageTableEditor;

public class TableTextEditor extends TextEditor  {
	
	private IDocument fDocument;
	private IFile fFile;
	
	public TableTextEditor(MultiPageTableEditor multiPageTableEditor) {
	}

	@Override
	protected void doSetInput(IEditorInput input) throws CoreException {
		super.doSetInput(input);
		if (input instanceof FileEditorInput){
			FileEditorInput fileEditorInput = (FileEditorInput) input;
			fFile = fileEditorInput.getFile();
			fDocument = getDocumentProvider().getDocument(input);			
		}
	}
	
	public String content() {
		String content = fDocument.get();
		return content;
	}
	
	public void updateContent(String text) {
		if (text != null) {
			text = text.replaceAll("'", "&#39;");
			fDocument.set(text);
		}
	}
	
	public IFile file() {
		return fFile;
	}
	
}
