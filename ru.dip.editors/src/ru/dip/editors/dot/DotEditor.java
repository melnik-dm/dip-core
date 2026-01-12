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
package ru.dip.editors.dot;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.part.FileEditorInput;

import ru.dip.core.external.editors.IDipImageRenderExtension;
import ru.dip.core.utilities.EditorUtils;
import ru.dip.core.utilities.UmlUtilities;
import ru.dip.editors.Messages;

public class DotEditor extends TextEditor implements IDipImageRenderExtension {
	
	public static final String ID = Messages.DotEditor_ID;
			
	public DotEditor() {
	
	}
	
	@Override
	public void firePropertyChange(int property) {
		super.firePropertyChange(property);
	}
	
	@Override
	protected void doSetInput(IEditorInput input) throws CoreException {
		super.doSetInput(input);	
	}
	
	@Override
	public void doSave(IProgressMonitor progressMonitor) {
		super.doSave(progressMonitor);
		firePropertyChange(EditorUtils.SAVE_EVENT);		
	}

	@Override
	public Image renderImage() {
		FileEditorInput input = (FileEditorInput) getEditorInput();
		IFile file = input.getFile();
		return UmlUtilities.getDotImage(file);	
	}
	
}
