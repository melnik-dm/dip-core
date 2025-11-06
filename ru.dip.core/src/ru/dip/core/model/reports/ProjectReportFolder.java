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
package ru.dip.core.model.reports;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;

import ru.dip.core.model.DipContainer;
import ru.dip.core.model.DipElementType;
import ru.dip.core.model.DipRoot;
import ru.dip.core.model.interfaces.IDipElement;
import ru.dip.core.model.interfaces.IDipParent;
import ru.dip.core.model.interfaces.IParent;

/**
 * Папка в корне проекта (для обратной совместимости) Включает в себя
 * виртуальные контейнеры из других вложенных в проект папок
 */
public class ProjectReportFolder extends DipContainer implements IMainReportContainer {

	public static final String REPORT_FOLDER_NAME = "Reports";

	public static ProjectReportFolder instance(IFolder container, IDipParent parent) {
		IDipElement element = DipRoot.getInstance().getElement(container, parent, DipElementType.REPORT_FOLDER);
		if (element == null) {
			ProjectReportFolder reportFolder = new ProjectReportFolder(container, parent);
			DipRoot.getInstance().putElement(reportFolder);
			return reportFolder;
		} else {
			return (ProjectReportFolder) element;
		}
	}

	private List<Report> fReports;
	private List<IReportContainer> fReportContainers = new ArrayList<>();

	private ProjectReportFolder(IContainer container, IParent parent) {
		super(container, parent);
	}

	@Override
	public String getRelativePath() {
		return resource().getProjectRelativePath().toOSString();
	}

	@Override
	public DipElementType type() {
		return DipElementType.REPORT_FOLDER;
	}

	@Override
	public IFolder resource() {
		return (IFolder) super.resource();
	}

	// =============================
	// children

	@Override
	public void computeChildren() {
		fReports = new ArrayList<>();
		fChildren = new ArrayList<>();
		try {
			for (IResource resource : resource().members()) {
				if (resource instanceof IFile) {
					String extension = ((IFile) resource).getFileExtension();
					if (Report.REPORT_EXTENSION.equals(extension)) {
						Report report = Report.instance(resource, this);
						fReports.add(report);
					}
				}
			}
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}

	@Override
	public IDipElement getChild(String name) {
		for (Report report : fReports) {
			if (name.equals(report.name())) {
				return report;
			}
		}
		return null;
	}

	@Override
	public List<IDipElement> getChildren() {
		if (fReports == null) {
			computeChildren();
		}
		List<IDipElement> result = new ArrayList<>();
		result.addAll(fReports);
		if (fReportContainers != null) {
			result.addAll(fReportContainers);
		}
		return result;
	}

	@Override
	public void addContainer(IReportContainer reportContainer) {
		fReportContainers.add(reportContainer);
	}

	@Override
	public void removeContainer(IReportContainer reportContainer) {
		fReportContainers.remove(reportContainer);
	}

	@Override
	public Report loadReport(IFile file) {
		Report report = Report.instance(file, this);
		fReports.add(report);
		return report;
	}

	@Override
	public IDipParent getDipParent() {
		return (IDipParent) parent();
	}

	@Override
	public List<Report> getReports() {
		if (fReports == null) {
			computeChildren();
		}
		return fReports;
	}

	@Override
	public void removeChild(IDipElement child) {
		fReports.remove(child);
	}

	@Override
	public void addReportChild(IFile resource) {
	}

}
