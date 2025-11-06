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

import ru.dip.core.model.interfaces.IDipParent;
import ru.dip.core.model.interfaces.IParent;

public interface IReportContainer extends IParent {

	/**
	 * Получить объект Report из фала (отчет уже сформирован в файле)
	 */
	Report loadReport(IFile file);

	/**
	 * Добавить отчет
	 */
	void addReportChild(IFile resource);
	
	List<Report> getReports();

	String getRelativePath();

	IDipParent getDipParent();

	@Override
	IContainer resource();

}
