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
package ru.dip.core.model;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

import ru.dip.core.model.interfaces.IDipDocumentElement;
import ru.dip.core.model.interfaces.IDipElement;
import ru.dip.core.model.interfaces.IParent;
import ru.dip.core.model.reports.IMainReportContainer;
import ru.dip.core.model.reports.ProjectReportFolder;
import ru.dip.core.utilities.ResourcesUtilities;

public class IncludeFolder extends DipFolder {
	
	public static IncludeFolder instance(IFolder container, IParent parent) {
		IDipElement element = null;
		if (container != null) {
			element = DipRoot.getInstance().getElement(container, parent, DipElementType.INCLUDE_FOLDER);
		}
		if (element == null) {
			IncludeFolder includeFolder = new IncludeFolder(container, parent);
			DipRoot.getInstance().putElement(includeFolder);
			return includeFolder;
		} else {
			return (IncludeFolder) element;
		}
	}
	
	public static DipFolder createBrokenLinkFolder(String link, IParent parent) {
		IncludeFolder includeFolder = new IncludeFolder(null , parent);
		includeFolder.fErrorLink = link;
		return includeFolder;
	}
	
	public static String getRootID(IFolder container, IParent parent) {
		return container.getLocation().toOSString() + "|" + parent.resource().getLocation().toOSString();
	}
	
	public static IFolder createLinkFolder(String name, String includePath, IContainer parentContainer) throws CoreException {
		java.nio.file.Path folderPath = Paths.get(includePath);
		java.nio.file.Path parentPath = Paths.get(parentContainer.getLocationURI());
		java.nio.file.Path relativePath = parentPath.resolve(folderPath);
		if (name == null || name.isEmpty()) {
			name = folderPath.getFileName().toString();
		}
		IFolder folder = parentContainer.getFolder(new Path(name));
		if (!Files.exists(folderPath)) {
			return null;
		}
		
		folder.createProxy();
		try {
			folder.createLink(relativePath.toUri(), IResource.NONE, null);
		} catch (Exception e) {
			
		}
		return folder;
	}

	@Override
	public List<IDipDocumentElement> getDipDocChildrenList() {
		if (resource() == null) {
			return new ArrayList<>();
		}
		return super.getDipDocChildrenList();
	}
	
	
	private ProjectReportFolder fReportFolder;
	private String fLinkName;
	private String fLinkDescription;
	private String fErrorLink;  // нерабочая ссылка, указаная в .dnfo

	protected IncludeFolder(IFolder container, IParent parent) {
		super(container, parent);
	}
	
	public void setLink(String includePath, boolean readOnly) throws CoreException {
		if (resource() != null) {
			ResourcesUtilities.deleteResource(resource(), null);
		}				
		IFolder folder = createLinkFolder(fLinkName, includePath, parent().resource());
		setResource(folder);
		setReadOnly(readOnly);
		computeChildren();
	}
	
	public String getLinkRelativePath() {
		if (resource() == null) {
			return fErrorLink;
		}		
		IPath folderPath = resource().getLocation();
		IPath parentPath = parent().resource().getLocation();
		return folderPath.makeRelativeTo(parentPath).toOSString();
	}
	
	public String getAbsoluteLinkPath() {
		if (resource() == null) {
			return null;
		}
		return resource().getLocation().toOSString();
	}
	
	
	@Override
	public void computeChildren() {
		if (fReportFolder != null) {
			IMainReportContainer mainReportFolder = dipProject().getReportFolder();
			mainReportFolder.removeContainer(fReportFolder);
		}
		super.computeChildren();
	}
	
	@Override
	public DipElementType type() {
		return DipElementType.INCLUDE_FOLDER;
	}
	
	@Override
	public boolean isIncluded() {
		return true;
	}
	
	@Override
	public boolean canRename() {
		return true;
	}
	
	@Override
	public boolean canDelete() {
		return !parent().isReadOnly();
	}
	
	@Override
	public String dipName() {
		return includeName();
	}
	
	@Override
	public boolean isRoot() {
		return true;
	}
	
	//==========================
	// include name
	
	public String includeName() {
		if (resource() == null) {
			return fLinkName;
		}
		if (fLinkName == null) {
			return super.name();
		}	
		return fLinkName;
	}
	
	@Override
	public String name() {
		return includeName();
	}
	
	public String realtiveProjectIncludeName() {
		if (resource() == null) {
			return parent().resource().getProjectRelativePath() + "/" + includeName();
		}
		IPath projectRelative = resource().getProjectRelativePath();
		projectRelative = projectRelative.removeLastSegments(1).append(includeName());
		return projectRelative.toOSString();
	}
	
	public void setLinkName(String name) {
		fLinkName = name;	
		if (fLinkName == null || fLinkName.isEmpty()) {
			fLinkName = resource().getName();
		}
	}
	
	//==========================
	// description
	
	public String linkDescription() {
		return fLinkDescription;
	}
	
	public void setLinkDescription(String newValue) {
		fLinkDescription = newValue;
	}
	
	@Override
	public void removeDescription() {
		fLinkDescription = null;
	}
	
	@Override
	public void updateDescription(String newDescriptionContent) {
		fLinkDescription = newDescriptionContent;
	}
	
	@Override
	public String description() {
		if (fLinkDescription == null || fLinkDescription.isEmpty()) {		
			return super.description();
		} else {
			return fLinkDescription;
		}
	}
	
	public String mainDescription() {
		return super.description();
	}

	public boolean isErrorLink() {
		return resource() == null;
	}
	
	//========================
	// reports
	
	@Override
	protected void createReportFolder(IFolder folder) {
		// нужно только для инклюдов (наверно перенести сюда)
		fReportFolder = ProjectReportFolder.instance(folder, this);
		fChildren.add(0, fReportFolder);
		IMainReportContainer main = dipProject().getReportFolder();
		if (main != null) {
			main.addContainer(fReportFolder);
		}
	}
}
