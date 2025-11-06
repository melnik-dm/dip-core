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
package ru.dip.ui.export;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;

import ru.dip.core.model.DipProject;
import ru.dip.core.model.interfaces.IDipElement;
import ru.dip.core.report.checker.ReportEntryChecker;
import ru.dip.core.report.checker.ReportRuleSyntaxException;
import ru.dip.core.report.model.report.ReportEntry;
import ru.dip.core.report.scanner.ReportReader;
import ru.dip.core.utilities.DipUtilities;
import ru.dip.core.utilities.FileUtilities;

public class HtmlReportWriter {
	
	private ReportReader fReader;
	private DipProject fDipProject;
	
	public HtmlReportWriter(ReportReader reader, DipProject project) {
		fReader = reader;
		fDipProject = project;
	}
	
	public void writeReportToHtml(String  fullName) throws IOException {
		String content = getContentForWriting();		
		FileUtilities.writeFile(Paths.get(fullName), content);
	}
	
	private String getContentForWriting() {		
		StringBuilder builder = new StringBuilder();
		builder.append(""
				//+ "<table style=\"width: 100%;\" border=\"1\">\n"
				+ "<table>\n"
				+ "<tbody>\n"
				+ "<tr>\n"
				+ "<td style=\"text-align: center;\">\n<strong>ОТЧЕТ: </strong>");
		builder.append(fReader.getRulesModel().getDescription());
		builder.append("\n</td>\n</tr>\n");		
		for (ReportEntry entry: fReader.getEntries()) {
			builder.append(getEntryContent(entry));			
		}	
		builder.append("</tbody>\n</table>");
		return builder.toString();
	}
	
	private String getEntryContent(ReportEntry entry) {
		List<IDipElement> entryElements = null;
		try {
			entryElements = ReportEntryChecker.findEntry(entry, fDipProject);
		} catch (ReportRuleSyntaxException e) {
			entryElements = Collections.emptyList();
		}
		StringBuilder builder = new StringBuilder();
		builder.append("<tr>\n<td><strong>");
		builder.append(entry.getName());
		builder.append(" (");
		builder.append(entryElements.size());
		builder.append(")</strong>");
		builder.append("\n</td>\n</tr>\n");
		for (IDipElement element: entryElements) {
			builder.append(getDipElementContent(element));
			builder.append("\n");
		}
		return builder.toString();
	}
	
	private String getDipElementContent(IDipElement element) {
		StringBuilder builder = new StringBuilder();
		builder.append("<tr>\n<td>\n");
		builder.append(DipUtilities.relativeID(element, fDipProject));
		builder.append("\n</td>\n</tr>");
		return builder.toString();		
	}	
}

