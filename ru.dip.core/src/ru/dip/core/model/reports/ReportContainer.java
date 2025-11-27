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
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;

import ru.dip.core.model.DipElementType;
import ru.dip.core.model.DipProject;
import ru.dip.core.model.interfaces.IDipElement;
import ru.dip.core.model.interfaces.IDipParent;
import ru.dip.core.model.interfaces.IParent;

public class ReportContainer implements IReportContainer {
	
	public static final String REPORT_FOLDER_NAME = "Reports";
	
	private IDipParent fDipParent;
	private List<Report> fReports = new ArrayList<>();
	
	public ReportContainer(IDipParent parent) {
		fDipParent = parent;
	}
	
	@Override
	public DipElementType type() {
		return DipElementType.REPORT_FOLDER;
	}
	
	@Override
	public String getRelativePath() {
		return  fDipParent.resource().getProjectRelativePath().toOSString();		
	}
	
	@Override
	public String name() {
		return getRelativePath();
	}

	@Override
	public IDipParent getDipParent() {
		return fDipParent;
	}
	
	@Override
	public IParent parent() {
		return fDipParent;
	}
	
	@Override
	public IContainer resource() {		
		return fDipParent.resource();
	}

	@Override
	public DipProject dipProject() {
		return fDipParent.dipProject();
	}
	
	//=========================
	// children
	
	protected void computeChildren() {
		fReports.clear();
		try {
			for (IResource resource: fDipParent.resource().members()){
				if (resource instanceof IFile){
					String extension = ((IFile) resource).getFileExtension();
					if (Report.REPORT_EXTENSION.equals(extension)){
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
	public List<? extends IDipElement> getChildren() {
		return fReports;
	}

	@Override
	public Report loadReport(IFile file) {
		Report report = Report.instance(file, this);
		fReports.add(report);
		return report;
	}
	
	
	@Override
	public void addReportChild(IFile resource) {
		Report report = Report.instance(resource, this); 
		fReports.add(report);
	}

	@Override
	public boolean hasChildren() {
		return !fReports.isEmpty();
	}

	@Override
	public IDipElement getChild(String name) {
		for (Report report: fReports) {
			if (name.equals(report.name())) {
				return report;
			}
		}
		return null;
	}

	@Override
	public void removeChild(IDipElement child) {
		fReports.remove(child);
	}
	
	@Override
	public List<Report> getReports() {
		return fReports;
	}
	
	//===========================
	// can edit (include)
	
	@Override
	public boolean canDelete() {
		return !fDipParent.isReadOnly();
	}

	@Override
	public boolean canRename() {
		return fDipParent.canDelete();
	}

	@Override
	public boolean hasParent(IParent parent) {
		return true;
	}

	@Override
	public boolean isReadOnly() {
		return fDipParent.isReadOnly();
	}

	@Override
	public boolean isIncluded() {
		return fDipParent.isIncluded();
	}

	//================
	// not used

	@Override
	public void refresh() {}

	@Override
	public void setResource(IResource resource) {}

	@Override
	public String id() {
		return null;
	}

	@Override
	public void updateWithProject() {}

	@Override
	public void setParent(IParent parent) {}

	@Override
	public void setReadOnly(boolean value) {}

	@Override
	public void setIncluded(boolean value) {}

	@Override
	public <T> T getAdapter(Class<T> adapter) {
		return null;
	}

	@Override
	public void dispose() {		
	}
}
