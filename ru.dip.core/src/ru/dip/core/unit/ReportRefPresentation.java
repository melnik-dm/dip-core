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
package ru.dip.core.unit;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;

import ru.dip.core.model.interfaces.IDipElement;
import ru.dip.core.model.interfaces.IDipUnit;
import ru.dip.core.model.interfaces.INotTextPresentation;
import ru.dip.core.model.reports.Report;
import ru.dip.core.report.checker.ReportEntryChecker;
import ru.dip.core.report.checker.ReportRuleSyntaxException;
import ru.dip.core.report.model.report.ReportEntry;
import ru.dip.core.report.scanner.ReportReader;
import ru.dip.core.utilities.FileUtilities;
import ru.dip.core.utilities.TagStringUtilities;
import ru.dip.core.utilities.ui.swt.FontDimension;

public class ReportRefPresentation extends TablePresentation implements INotTextPresentation {

	public static final String EXTENSION = ".reportref";
	public static final String EXTENSION_WITHOUT_DOT = "reportref";
	public static final String NOT_REPORT = "Отчёт не найден";

	private ReportReader fReader;
	
	private String fReportRefPath;
	private IFile fReportFile;
	
	private List<ReportEntry> fEntries;
	private List<Point> fPoints = new ArrayList<>();
	private List<Point> fFixedPoints = new ArrayList<>();
	private String fText;
	private String fFixedText;
	
	private long fRepotTimeModified;

	public ReportRefPresentation(IDipUnit unit) {
		super(unit);
		fRepotTimeModified = getReportTimeModified();
	}

	private long getReportTimeModified() {
		IFile file = getReportFile();
		if (file != null) {
			return file.getModificationStamp();
		}
		return -1;
	}

	@Override
	public boolean checkUpdate() {
		long newValue = getResource().getModificationStamp();
		long newReportValue = getReportTimeModified();
		if (newValue != getTimeModified() || fRepotTimeModified != newReportValue) {
			setTimeModified(newValue);
			fRepotTimeModified = newReportValue;
			read();
			return true;
		}
		return false;
	}

	@Override
	protected void read() {
		fReportFile = findReportFile();
		if (fReportFile == null || !fReportFile.exists()) {
			fText = NOT_REPORT;
			fFixedText = NOT_REPORT;
			return;
		}
		fReader = new ReportReader(fReportFile);
		fReader.read();
		fEntries = fReader.getEntries();
	}
	
	public String createText(int length) {
		if (fReportFile == null || !fReportFile.exists()) {
			return NOT_REPORT;
		}
		clearPoints();
		StringBuilder builder = new StringBuilder();
		StringBuilder fixedBuilder = new StringBuilder();
		for (int i = 0; i < fEntries.size(); i++) {
			ReportEntry reportEntry = fEntries.get(i);
			String name = reportEntry.getName();
			try {
				List<IDipElement> units = ReportEntryChecker.findEntry(reportEntry, getUnit().dipProject());
				addName(name, units.size(), builder, fPoints);
				addName(name, units.size(), fixedBuilder, fFixedPoints);
				addElements(units, builder, length);
				if (i < fEntries.size() - 1) {
					builder.append(TagStringUtilities.lineSeparator());
					fixedBuilder.append(TagStringUtilities.lineSeparator());
				}
			} catch (ReportRuleSyntaxException e) {
				addName(name, 0, builder, fPoints);
				addError(builder, e.getMessage());

				addName(name, 0, fixedBuilder, fFixedPoints);
				addError(fixedBuilder, e.getMessage());
			}

		}
		fText = builder.toString();
		fFixedText = fixedBuilder.toString();
		return fText;
	}

	public static String getWrapText(String text, int length) {
		if (text.length() <= length) {
			return text;
		}
		String[] lines = text.split(TagStringUtilities.LINE_SPLIT_REGEX);
		ArrayList<String> result = new ArrayList<>();
		for (String line : lines) {
			String wrapLine = FontDimension.getWrapLine(line, length);
			result.add(wrapLine);
		}
		return String.join(TagStringUtilities.lineSeparator(), result);
	}

	private void addName(String name, int unitsCount, StringBuilder builder, List<Point> points) {
		int x = builder.length();
		builder.append(name);
		int y = builder.length();
		points.add(new Point(x, y));
		builder.append(" (");
		builder.append(unitsCount);
		builder.append(")");
	}
	
	private void addError(StringBuilder builder, String message) {
		builder.append("\n");
		builder.append("Некорректное правило: " + message);
		builder.append("\n");
	}
	

	private void addElements(List<IDipElement> elements, StringBuilder builder, int length) {
		for (int i = 0; i < elements.size(); i++) {
			IDipElement element = elements.get(i);
			builder.append(TagStringUtilities.lineSeparator());
			String id = " " + element.id();
			if (length > 20) {
				id = FontDimension.getWrapLine(id, length);
			}
			builder.append(id);
		}
	}

	public IFile findReportFile() {
		fReportRefPath = readReportRelativePath();
		if (fReportRefPath != null) {
			IFile report = getReportByReference(fReportRefPath);
			if (report != null) {
				return report;
			}
		}
		return getReportByName();
	}
	
	public IFile getReportFile() {
		if (fReportFile == null) {
			findReportFile();
		}
		return fReportFile;
	}
	
	public boolean isCorrectRef() {
		return fReportFile != null && fReportFile.exists();
	}
	

	private String readReportRelativePath() {
		try {
			String result = FileUtilities.readFile(getResource());
			if (result == null || result.isEmpty()) {
				return null;
			}
			return result;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	private IFile getReportByReference(String reportPath) {
		IPath path = new Path(reportPath);
		if (reportPath.startsWith(".")) {
			path = getResource().getParent().getProjectRelativePath().append(path);
		}
		return getUnit().resource().getProject().getFile(path);
	}

	private IFile getReportByName() {
		String name = getUnit().name();
		if (name.endsWith(EXTENSION)) {
			String reportName = name.substring(0, name.length() - EXTENSION.length() + 1) + Report.REPORT_EXTENSION;
			Report report = getUnit().dipProject().getReport(reportName);
			if (report != null) {
				return report.resource();
			}
		}
		return null;
	}

	@Override
	public String getText() {
		return fText;
	}

	public String getFixedText() {
		return fFixedText;
	}

	@Override
	public Image getImage() {
		return null;
	}

	public List<Point> getPoints() {
		return fPoints;
	}

	public List<Point> getFixedPoints() {
		return fFixedPoints;
	}

	public void clearPoints() {
		fPoints.clear();
		fFixedPoints.clear();
	}

	public ReportReader getReportReader() {
		return fReader;
	}
	
	public String getRepoRefPath() {
		return fReportRefPath;
	}

}
