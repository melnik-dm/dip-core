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
package ru.dip.core.csv.model;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

//import com.csvreader.CsvReader;

import ru.dip.core.utilities.FileUtilities;

public class CsvModel {

	private final List<CSVRow> fRows;
	private int fNumberOfColumns;

	public CsvModel() {
		fNumberOfColumns = 1;
		fRows = new ArrayList<>();
	}

	public void readModel(Path path) throws IOException {
		//String content = FileUtilities.readFile(path);
		List<String> lines = Files.readAllLines(path);
		fRows.clear();
		for (String line: lines) {
			String[] rowValues = line.split(",");
			CSVRow csvRow = new CSVRow(rowValues);

			fRows.add(csvRow);

			if (rowValues.length > fNumberOfColumns) {
				fNumberOfColumns = rowValues.length;
			}
		}
		//readLines(content);
	}

	/*public void readModel(String content) {
		readLines(content);
	}*/

	/*private void readLines(String fileText) {
		fRows.clear();
		
		
		try {
			CsvReader csvReader = initializeReader(new StringReader(fileText));
			while (csvReader.readRecord()) {
				String[] rowValues = csvReader.getValues();
				CSVRow csvRow = new CSVRow(rowValues);

				fRows.add(csvRow);

				if (rowValues.length > fNumberOfColumns) {
					fNumberOfColumns = rowValues.length;
				}
			}
			csvReader.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}*/

	/*private CsvReader initializeReader(Reader reader) {
		CsvReader csvReader = new CsvReader(reader);
		char customDelimiter = getCustomDelimiter();
		csvReader.setDelimiter(customDelimiter);
		char commentChar = getCommentChar();
		if (commentChar != Character.UNASSIGNED) {
			csvReader.setComment(commentChar);
			// prevent loss of comment in csv source file
			csvReader.setUseComments(false);
		}

		csvReader.setTextQualifier(getTextQualifier());
		csvReader.setUseTextQualifier(true);
		return csvReader;
	}*/

	private char getCustomDelimiter() {
		return ',';
	}

	private char getCommentChar() {
		return Character.UNASSIGNED;
	}

	private char getTextQualifier() {
		return '"';
	}

	// ===============================
	//

	public String getText() {
		StringBuilder builder = new StringBuilder("CVS \n");
		for (CSVRow row : fRows) {
			builder.append("row: ");
			for (String str : row.getEntries()) {
				builder.append(str);
				builder.append("==");
			}
		}
		return builder.toString();
	}

	public String getSimpleUmlText() {
		StringBuilder builder = new StringBuilder();
		builder.append("@startsalt\n");

		for (int i = 0; i < fRows.size(); i++) {
			if (i == 0) {
				builder.append("{#");
			}
			List<String> entries = fRows.get(i).getEntries();
			for (int j = 0; j < entries.size(); j++) {
				builder.append(entries.get(j));
				if (j != entries.size() - 1) {
					builder.append(" | ");
				}
			}
			if (i == fRows.size() - 1) {
				builder.append("}");
			}
			builder.append("\n");
		}
		builder.append("@endsalt\n");
		return builder.toString();
	}

	public String getUmlText() {
		StringBuilder builder = new StringBuilder();
		builder.append("@startuml\n");
		builder.append("skinparam titleFontSize 14\n");
		builder.append("title\n");

		boolean gray = true;
		for (int i = 0; i < fRows.size(); i++) {
			if (gray) {
				builder.append("<#lightgray>");
			}
			gray = !gray;
			builder.append("|");
			List<String> entries = fRows.get(i).getEntries();
			for (int j = 0; j < entries.size(); j++) {
				builder.append(" ");
				builder.append(entries.get(j));
				builder.append(" |");
			}
			builder.append("\n");
		}
		builder.append("end title\n");
		builder.append("@enduml\n");
		return builder.toString();
	}

	public String getHtmlText() {
		StringBuilder builder = new StringBuilder();
		builder.append("<table border=\"1\">\n");
		for (CSVRow row : fRows) {
			builder.append("  <tr>\n");
			for (String cell : row.getEntries()) {
				builder.append("    <td>");
				builder.append(cell);
				builder.append("</td>\n");
			}
			builder.append("  </tr>\n");
		}
		builder.append("</table>");
		return builder.toString();
	}

	public String getHtmlText(int width, boolean wrapContent) {
		StringBuilder builder = new StringBuilder();
		builder.append("<table border=\"1\" width = \"");
		builder.append(width - 2);
		builder.append("\">\n");
		String tdTag = "    <td>";
		String thTag = "    <th>";
		if (!wrapContent) {
			int columnWidth = 100 / fNumberOfColumns;
			tdTag = "    <td width=\"" + columnWidth + "%\">";
		}
		if (!fRows.isEmpty()) {
			CSVRow firstRow = fRows.get(0);
			builder.append("  <tr>\n");
			for (String cell : firstRow.getEntries()) {
				builder.append(thTag);
				builder.append(cell.replaceAll("\\\\n", "<br>"));
				builder.append("</th>\n");
			}
			builder.append("  </tr>\n");
		}
		for (int i = 1; i < fRows.size(); i++) {
			CSVRow row = fRows.get(i);
			builder.append("  <tr>\n");
			for (String cell : row.getEntries()) {
				builder.append(tdTag);
				builder.append(cell.replaceAll("\\\\n", "<br>"));
				builder.append("</td>\n");
			}
			builder.append("  </tr>\n");
		}
		builder.append("</table>");
		return builder.toString();
	}

	public int getNumberOfColumns() {
		return fNumberOfColumns;
	}

	public int getNumberOfRows() {
		return fRows.size();
	}

	public List<CSVRow> getRows() {
		return fRows;
	}

}
