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
package ru.dip.ui.navigator;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.ILabelDecorator;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IDecoratorManager;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.model.WorkbenchLabelProvider;

import ru.dip.core.manager.DipNatureManager;
import ru.dip.core.model.ChangeLog;
import ru.dip.core.model.DipProject;
import ru.dip.core.model.DipSchemaFolder;
import ru.dip.core.model.DnfoTable;
import ru.dip.core.model.DipSchemaElement;
import ru.dip.core.model.IncludeFolder;
import ru.dip.core.model.TocRef;
import ru.dip.core.model.glossary.GlossRef;
import ru.dip.core.model.glossary.GlossaryField;
import ru.dip.core.model.glossary.GlossaryFolder;
import ru.dip.core.model.interfaces.IDipElement;
import ru.dip.core.model.interfaces.IDipParent;
import ru.dip.core.model.interfaces.IParent;
import ru.dip.core.model.reports.IMainReportContainer;
import ru.dip.core.model.reports.IReportContainer;
import ru.dip.core.model.reports.ProjectReportFolder;
import ru.dip.core.model.vars.FolderVarContainer;
import ru.dip.core.model.vars.IVarContainer;
import ru.dip.core.model.vars.ProjectVarContainer;
import ru.dip.core.model.vars.VarContainer;
import ru.dip.core.model.vars.Variable;
import ru.dip.core.utilities.DipUtilities;
import ru.dip.ui.navigator.decorator.ErrorDecorator;
import ru.dip.ui.navigator.decorator.ErrorIncludeDecorator;
import ru.dip.ui.navigator.decorator.ReadOnlyDecorator;
import ru.dip.ui.utilities.image.ImageProvider;

public class NavigatorLabelProvider extends LabelProvider {

	private WorkbenchLabelProvider fWorkbenchLabelProvider = new WorkbenchLabelProvider();
	
	@Override
	public Image getImage(Object element) {
		Image image = image(element);		
		if (needIncludeError(element)) {			
			return decorateErrorInclude(image, element);
		} else if (needIncludeDecorate(element)) {
			return decorateInclude(image, (IDipElement) element);
		}
		// пока не используем
		/*if (hasError(element)) {			
			return decorateError(image, element);
		}*/		
		return image;
	}
	
	private Image image(Object element) {
		if (element instanceof DnfoTable){
			return ImageProvider.TABLE;
		} else if(element instanceof ProjectReportFolder){
			return ImageProvider.REPORT_FOLDER;
		} else if (element instanceof GlossaryFolder){
			return ImageProvider.GLOSS_FOLDER;
		} else if (element instanceof GlossaryField){
			return ImageProvider.GLOSS_FIELD;
		} else if (element instanceof GlossRef){
			return fWorkbenchLabelProvider.getImage(((GlossRef) element).resource());
		} else if (element instanceof TocRef) {
			return ImageProvider.TOC;
		} else if (element instanceof ChangeLog) {
			return fWorkbenchLabelProvider.getImage(((ChangeLog) element).resource());
		} else if (element instanceof IncludeFolder) {
			return ImageProvider.FOLDER;
		} else if (element instanceof Variable) {
			return ImageProvider.VARIABLE;
		} else if (element instanceof IVarContainer) {			
			return ImageProvider.VAR_CONTAINER;
		} else if (element instanceof IReportContainer) {
			return ImageProvider.REPORT_FOLDER;
		} else if (element instanceof DipSchemaFolder) {
			return ImageProvider.SCHEMA_FOLDER;
		}
		if (element instanceof IFolder) {
			IFolder folder = (IFolder) element;
			IContainer parent = folder.getParent();
			IDipElement parentElement = DipUtilities.findElement(parent);
			if (parentElement instanceof IDipParent && DipNatureManager.hasNature(folder)) {
				return ImageProvider.SERVICE_FOLDER;
			} 
		}
		if (element instanceof DipSchemaElement) {
			return ImageProvider.SCHEMA_FILE;
		}
		if (element instanceof IProject) {
			 IProject project = (IProject) element;
			 if (project.isOpen()) {
				return ImageProvider.PROJECT_FOLDER; 
			 } else {
				return ImageProvider.SERVICE_FOLDER; 
			 }

			

		}
		if (element instanceof DipProject) {
			 IProject project = ((DipProject) element).getProject();
			 if (project != null && project.isOpen()) {
				return ImageProvider.PROJECT_FOLDER; 
			 } else {
				return ImageProvider.SERVICE_FOLDER; 
			 }
		}
		
		if (element instanceof IDipElement && ((IDipElement) element).isReadOnly()) {
			return fWorkbenchLabelProvider.getImage(((IDipElement) element).resource());
		}
		if (element == null) {
			return null;
		}		
		if (element instanceof IDipElement) {
			IResource resource = ((IDipElement) element).resource();
			if (resource instanceof IFolder) {
				return ImageProvider.FOLDER;
			}
			return fWorkbenchLabelProvider.getImage(((IDipElement) element).resource());
		}
		return fWorkbenchLabelProvider.getImage(element);
	}
		
	/*
	 * Есть ли ошибка (пока не используется) 
	 */
	private boolean hasError(Object element) {
		if (element instanceof IDipElement && ((IDipElement) element).resource() instanceof IResource) {
			IResource res = ((IDipElement) element).resource();
			try {
				IMarker[] markers = res.findMarkers(IMarker.MARKER, true, IResource.DEPTH_INFINITE);
				return markers.length != 0;
			} catch (CoreException e) {
				e.printStackTrace();
			}
		}
		return false;
	}
	
	private boolean needIncludeError(Object element) {
		return element instanceof IncludeFolder 
				&& ((IncludeFolder)element).isErrorLink();
	}

	private boolean needIncludeDecorate(Object element) {
		return element instanceof IDipElement 
				&& ((IDipElement) element).isIncluded();
	}
	
	/*
	 * Пока не используется, декоратор для ошибки в файле (в папке) 
	 */
	private Image decorateError(Image image, Object element) {
		   IDecoratorManager decoratorManager =
				   PlatformUI.getWorkbench().getDecoratorManager();
		   ILabelDecorator decorator = decoratorManager.getLabelDecorator(ErrorDecorator.ID);
		if (decorator != null) {
			Image decorateImage = decorator.decorateImage(image, element);
			if (decorateImage != null) {
				return decorateImage;
			}
		}
		return image;
	}
	
	
	private Image decorateErrorInclude(Image image, Object element) {
		   IDecoratorManager decoratorManager =
				   PlatformUI.getWorkbench().getDecoratorManager();
		   ILabelDecorator decorator = decoratorManager.getLabelDecorator(ErrorIncludeDecorator.ID);
		if (decorator != null) {
			Image decorateImage = decorator.decorateImage(image, element);
			if (decorateImage != null) {
				return decorateImage;
			}
		}
		return image;
	}
	
	private Image decorateInclude(Image image, IDipElement element) {
		ReadOnlyDecorator decorator = ReadOnlyDecorator.getDemoDecorator();
		if (decorator != null) {
			Image decorateImage = decorator.decorateImage(image, element);
			if (decorateImage != null) {
				return decorateImage;
			}
		}
		return image;
	}
	
	@Override
	public String getText(Object element) {
		if (element instanceof DnfoTable){			
			IParent parent = ((DnfoTable) element).parent();
			if (parent instanceof DipProject){
				return DnfoTable.TABLE_DOCUMENT_LABEL;
			} else {
				return DnfoTable.TABLE_SECTION_LABEL;
			}			
		} else if (element instanceof DipProject) {
			return ((DipProject) element).decorateName();	
		} else if (element instanceof IncludeFolder) { 
			return ((IncludeFolder) element).includeName();
		} else if (element instanceof IVarContainer) { 
			if (element instanceof ProjectVarContainer) {
				return VarContainer.VAR_FOLDER;
			}
			if (element instanceof FolderVarContainer) {
				return VarContainer.VAR_FOLDER;
			}
			IVarContainer varContainer =  (IVarContainer) element;				
			return varContainer.getRelativePath();		
		} else if (element instanceof ProjectReportFolder) {			
			ProjectReportFolder reportFolder =  (ProjectReportFolder) element;
			IDipParent parent = reportFolder.getDipParent();			
			if (parent instanceof DipProject || parent instanceof IncludeFolder) {
				return ProjectReportFolder.REPORT_FOLDER_NAME;
			} else {
				return reportFolder.getRelativePath();
			}
		} else if (element instanceof IMainReportContainer) { 
			return ProjectReportFolder.REPORT_FOLDER_NAME;
		} else if (element instanceof IDipElement){	
			return ((IDipElement) element).name();
		}
		return fWorkbenchLabelProvider.getText(element);
	}

}
