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
package ru.dip.core.utilities;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.widgets.Shell;

import ru.dip.core.model.DipFolder;
import ru.dip.core.model.DipProject;
import ru.dip.core.model.interfaces.IDipDocumentElement;
import ru.dip.core.model.reports.IReportContainer;
import ru.dip.core.model.reports.Report;
import ru.dip.core.report.model.condition.Condition;
import ru.dip.core.report.model.condition.ConditionPart;
import ru.dip.core.report.model.report.ReportEntry;
import ru.dip.core.report.model.report.ReportRule;
import ru.dip.core.unit.ReportRefPresentation;
import ru.dip.core.unit.TablePresentation;
import ru.dip.core.unit.UnitPresentation;

public class ReportUtils {
	
	private static final String EMPTY_REPORT_CONTENT = 
			"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + 
			"<report>\n" + 
			"</report>";
	
	public static void saveEmptyReport(IFile file) throws IOException {
		FileUtilities.writeFile(file, EMPTY_REPORT_CONTENT);
	}
	
	public static void createNewReport(String fileName, IReportContainer reportContainer, Shell shell) throws CoreException {
		createNewReport(fileName, reportContainer, EMPTY_REPORT_CONTENT, shell);
	}
	
	public static void createNewReport(String fileName, IReportContainer reportContainer, String content, Shell shell)
			throws CoreException {
		if (content == null) {
			content = EMPTY_REPORT_CONTENT;
		}
		IFile file = ResourcesUtilities.createFile(reportContainer.resource(), fileName, content, shell);
		Report report = reportContainer.loadReport(file);;
		ResourcesUtilities.updateContainer(reportContainer.resource());
		WorkbenchUtitlities.updateProjectExplorer(reportContainer);
		WorkbenchUtitlities.selectAndReveal(report);
		WorkbenchUtitlities.openFile(file);

	}
	
	public static String createReportContentFromFilter(String reportName, String ruleName, String filter) {
		StringBuilder content = new StringBuilder();
		content.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
		content.append("<report desc=\"");
		content.append(reportName);
		content.append("\">\n");
		content.append("<entry title=\"");
		content.append(ruleName);
		content.append("\">\n<rule>\n");		
		content.append(filter);
		content.append("\n</rule>\n</entry>\n</report>");
		return content.toString();
	}

	public static List<ReportRefPresentation> getAllReportRefs(DipProject project) {
		List<ReportRefPresentation> result = new ArrayList<>();
		for (IDipDocumentElement dde : project.getOneListChildren()) {
			if (dde instanceof UnitPresentation) {
				TablePresentation presentation = ((UnitPresentation) dde).getPresentation();
				if (presentation instanceof ReportRefPresentation) {
					result.add((ReportRefPresentation) presentation);
				}
			}
		}
		return result;
	}
	
	public static IReportContainer getReportFolder(Object selectedElement){
        if (selectedElement instanceof IReportContainer){
        	return (IReportContainer) selectedElement;
        } else if (selectedElement instanceof DipProject){
        	DipProject project = (DipProject) selectedElement;
        	return project.getOrCreateReportContainer(); 
        } else if (selectedElement instanceof DipFolder) {
        	DipFolder folder = (DipFolder) selectedElement;
        	return folder.getOrCreateReportContainer();
        }
        return null;
	}

	/**
	 * Возвращает текстовую запись фильтра из ReportEntry
	 */
	public static String getFilterFromEntry(ReportEntry entry) {
		List<ReportRule> rules = entry.getRules();
		if (rules.size() == 1) {
			ReportRule rule = rules.get(0);
			if (rule.getCondition() != null) {
				return toFilter(rules.get(0).getCondition());
			}
		} else {
			StringBuilder builder = new StringBuilder();
			for (ReportRule rule : entry.getRules()) {
				Condition condition = rule.getCondition();
				if (condition == null) {
					continue;
				}

				if (builder.length() > 0) {
					builder.append(" OR ");
				}
				builder.append("(");
				builder.append(toFilter(condition));
				builder.append(")");
			}
			return builder.toString();
		}
		return null;
	}
	
	private static String toFilter(Condition condition) {
		StringBuilder builder = new StringBuilder();
		for (ConditionPart part: condition.getParts()){
			builder.append(part);
			builder.append(" ");
		}
		return builder.toString();
	}

}
