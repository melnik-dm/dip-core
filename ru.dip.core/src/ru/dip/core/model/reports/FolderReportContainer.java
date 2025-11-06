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

import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;

import ru.dip.core.model.DipElementType;
import ru.dip.core.model.DipProject;
import ru.dip.core.model.interfaces.IDipElement;
import ru.dip.core.model.interfaces.IDipParent;
import ru.dip.core.model.interfaces.IParent;

/**
 * Обвертка на ReportContainer Используется для отображения внутри папок
 */
public class FolderReportContainer implements IReportContainer, IDipElement {

	private final ReportContainer fReportContainer;

	public FolderReportContainer(ReportContainer container) {
		fReportContainer = container;
	}

	public ReportContainer getOriginalReportContainer() {
		return fReportContainer;
	}

	@Override
	public Report loadReport(IFile file) {
		return fReportContainer.loadReport(file);
	}

	@Override
	public String getRelativePath() {
		return fReportContainer.getRelativePath();
	}

	@Override
	public List<Report> getReports() {
		return fReportContainer.getReports();
	}
	
	@Override
	public IDipElement getChild(String name) {
		return fReportContainer.getChild(name);
	}

	@Override
	public IDipParent getDipParent() {
		return fReportContainer.getDipParent();
	}

	@Override
	public <T> T getAdapter(Class<T> adapter) {
		return fReportContainer.getAdapter(adapter);
	}

	@Override
	public DipProject dipProject() {
		return fReportContainer.dipProject();
	}

	@Override
	public String name() {
		return ReportContainer.REPORT_FOLDER_NAME;
	}

	@Override
	public DipElementType type() {
		return fReportContainer.type();
	}

	@Override
	public IContainer resource() {
		return fReportContainer.resource();
	}

	@Override
	public void setResource(IResource resource) {
		fReportContainer.setResource(resource);
	}

	@Override
	public String id() {
		return fReportContainer.id();
	}

	@Override
	public void updateWithProject() {
		fReportContainer.updateWithProject();
	}

	@Override
	public boolean canDelete() {
		return fReportContainer.canDelete();
	}

	@Override
	public boolean canRename() {
		return fReportContainer.canRename();
	}

	@Override
	public IParent parent() {
		return fReportContainer;
	}

	@Override
	public void setParent(IParent parent) {
		fReportContainer.setParent(parent);
	}

	@Override
	public boolean hasParent(IParent parent) {
		return fReportContainer.hasParent(parent);
	}

	@Override
	public boolean isReadOnly() {
		return fReportContainer.isReadOnly();
	}

	@Override
	public void setReadOnly(boolean value) {
		fReportContainer.setReadOnly(value);
	}

	@Override
	public boolean isIncluded() {
		return fReportContainer.isIncluded();
	}

	@Override
	public void setIncluded(boolean value) {
		fReportContainer.setIncluded(value);
	}

	@Override
	public List<? extends IDipElement> getChildren() {
		return fReportContainer.getChildren();
	}

	@Override
	public boolean hasChildren() {
		return fReportContainer.hasChildren();
	}

	@Override
	public void removeChild(IDipElement child) {
		fReportContainer.removeChild(child);
	}

	@Override
	public void refresh() {
		fReportContainer.refresh();
	}

	@Override
	public void addReportChild(IFile resource) {
		fReportContainer.addReportChild(resource);
	}
}
