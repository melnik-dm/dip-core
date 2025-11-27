/********************************************************************** 
 * This file is part of DIP
 * 
 * Copyright (c) 2025 Denis Melnik.
 * Copyright (c) 2025 Ruslan Sabirov.
 * Copyright (c) 2025 Andrei Motorin.
 * 
 * This program is free software: you can redistribute it and/or modify  
 * it under the terms of the GNU General Public License as published by  
 * the Free Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU 
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License 
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
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
