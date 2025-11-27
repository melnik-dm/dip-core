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

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.editors.text.TextEditorActionContributor;

import ru.dip.core.utilities.EditorUtils;
import ru.dip.editors.Messages;
import ru.dip.editors.dot.DotEditor;
import ru.dip.ui.utilities.image.ImageProvider;

public class DotEditorContributor extends TextEditorActionContributor {

	public DotEditorContributor() {
	}
	
	@Override
	public void contributeToToolBar(IToolBarManager toolBarManager) {
		super.contributeToToolBar(toolBarManager);
		toolBarManager.add(new OpenViewAction());
	}
	
	private class OpenViewAction extends Action {
		
		public OpenViewAction() {
			setToolTipText(Messages.DotEditorContributor_OpenDipViewActionName);
			setImageDescriptor(ImageProvider.RENDER);	
		}
		
		@Override
		public void run() {
			try {
				PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView("ru.dip.editors.umlview");
				IEditorPart editorPart = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
				if (editorPart instanceof DotEditor){
					((DotEditor) editorPart).firePropertyChange(EditorUtils.VISIBLE_EVENT);
				}
			} catch (PartInitException e) {
				e.printStackTrace();
			}
		}
	}

}
