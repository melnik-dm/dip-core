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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CSVRow {

	private List<String> entries;

	public CSVRow(String[] lineElements) {
		this(Arrays.asList(lineElements));
	}

	public CSVRow(List<String> lines) {
		entries = new ArrayList<>(lines);
	}

	public String getComment() {
		return entries.get(0).substring(1);
	}

	// ======================
	// getters & setters

	public List<String> getEntries() {
		return entries;
	}

	public String[] getEntriesAsArray() {
		return entries.toArray(new String[entries.size()]);
	}

}
