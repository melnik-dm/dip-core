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
package ru.dip.ui.imageview;

import org.eclipse.core.resources.IFile;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartReference;

import ru.dip.core.model.DipProject;
import ru.dip.core.utilities.WorkbenchUtitlities;

public class DipImageWorkbenchListener implements IPartListener2 {

	@Override
	public void partActivated(IWorkbenchPartReference partRef) {
	}
	
	@Override
	public void partVisible(IWorkbenchPartReference partRef) {
		IWorkbenchPart part = partRef.getPart(false);
		if (part instanceof ImagesView){
			ImagesView imageView = (ImagesView) part;
			imageView.createComposite();
		} else if (part instanceof TablesView) { 
			TablesView tablesView = (TablesView) part;
			tablesView.createComposite();
		} else if (part instanceof IEditorPart) {
			DipProject project = WorkbenchUtitlities.getDipProjectFromOpenedEditor(part);
			IFile file =  WorkbenchUtitlities.getFileFromOpenedEditor(part);
			updateViews(partRef, project, file);
		}
	}

	@Override
	public void partOpened(IWorkbenchPartReference partRef) {
		/*IWorkbenchPart part = partRef.getPart(false);
		if (part instanceof ImagesView){
			ImagesView imageView = (ImagesView) part;
			imageView.update();
		} else if (part instanceof TablesView) { 
			TablesView tablesView = (TablesView) part;
			tablesView.update();
		} else if (part instanceof IEditorPart){
			DipProject project = WorkbenchUtitlities.getDipProjectFromOpenedEditor(part);
			IFile file =  WorkbenchUtitlities.getFileFromOpenedEditor(part);
			updateViews(partRef, project, file);
		}*/
	}
	
	private void updateViews(IWorkbenchPartReference partRef, DipProject project, IFile file) {
		ImagesView imageView = getImagesView(partRef);
		if (imageView != null) {			
			imageView.createComposite(project, file);
		}
		TablesView tableView = getTablesView(partRef);
		if (tableView != null) {
			tableView.createComposite(project, file);
		}
	}
	
	private ImagesView getImagesView(IWorkbenchPartReference partRef){
		IViewPart part =  partRef.getPage().findView(ImagesView.ID);  
		if (part instanceof ImagesView) {
			return (ImagesView) part;
		}
		return null;
	}
	
	private TablesView getTablesView(IWorkbenchPartReference partRef){
		return (TablesView) partRef
        .getPage().findView(TablesView.ID);  
	}
	
	@Override
	public void partBroughtToTop(IWorkbenchPartReference partRef) {
		
	}

	@Override
	public void partClosed(IWorkbenchPartReference partRef) {
		
	}

	@Override
	public void partDeactivated(IWorkbenchPartReference partRef) {
		
	}

	@Override
	public void partHidden(IWorkbenchPartReference partRef) {
		
	}

	@Override
	public void partInputChanged(IWorkbenchPartReference partRef) {
		
	}

}
